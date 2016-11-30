package org.revenj.database.postgres;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.patterns.DataSource;
import org.revenj.patterns.OlapCubeQuery;
import org.revenj.patterns.ServiceLocator;
import org.revenj.patterns.Specification;
import org.revenj.database.postgres.jinq.RevenjQueryComposer;
import org.revenj.database.postgres.jinq.jpqlquery.*;
import org.revenj.database.postgres.jinq.transform.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

public abstract class PostgresOlapCubeQuery<TSource extends DataSource> implements OlapCubeQuery<TSource> {

	protected final ServiceLocator locator;
	protected final PostgresReader reader;
	protected final Connection transactionConnection;
	protected final javax.sql.DataSource dataSource;
	private final MetamodelUtil metamodel;
	private final ClassLoader loader;

	protected abstract String getSource();

	protected final Map<String, Function<String, String>> cubeDimensions = new LinkedHashMap<>();
	protected final Map<String, Function<String, String>> cubeFacts = new LinkedHashMap<>();
	protected final Map<String, Converter> cubeConverters = new LinkedHashMap<>();

	@FunctionalInterface
	public interface Converter {
		Object convert(PostgresReader reader, int context) throws IOException;
	}

	protected PostgresOlapCubeQuery(ServiceLocator locator) {
		this.locator = locator;
		this.reader = new PostgresReader(locator);
		this.loader = locator.resolve(ClassLoader.class);
		this.transactionConnection = locator.tryResolve(Connection.class).orElse(null);
		this.dataSource = transactionConnection != null ? null : locator.resolve(javax.sql.DataSource.class);
		this.metamodel = locator.resolve(MetamodelUtil.class);
	}

	@Override
	public Set<String> getDimensions() {
		return cubeDimensions.keySet();
	}

	@Override
	public Set<String> getFacts() {
		return cubeFacts.keySet();
	}

	private void validateInput(List<String> usedDimensions, List<String> usedFacts, Collection<String> customOrder) {
		if (usedDimensions.size() == 0 && usedFacts.size() == 0) {
			throw new IllegalArgumentException("Cube must have at least one dimension or fact.");
		}
		for (String d : usedDimensions) {
			if (!cubeDimensions.containsKey(d)) {
				throw new IllegalArgumentException("Unknown dimension: " + d + ". Use getDimensions method for available dimensions");
			}
		}
		for (String f : usedFacts) {
			if (!cubeFacts.containsKey(f)) {
				throw new IllegalArgumentException("Unknown fact: " + f + ". Use getFacts method for available facts");
			}
		}
		for (String o : customOrder) {
			if (!usedDimensions.contains(o) && !usedFacts.contains(o)) {
				throw new IllegalArgumentException("Invalid order: " + o + ". Order can be only field from used dimensions and facts.");
			}
		}
	}

	protected Connection getConnection() {
		if (transactionConnection != null) {
			return transactionConnection;
		}
		try {
			return dataSource.getConnection();
		} catch (SQLException e) {
			throw new RuntimeException("Unable to resolve connection for cube query. " + e.getMessage());
		}
	}

	protected void releaseConnection(Connection connection) throws SQLException {
		if (transactionConnection == null) {
			connection.close();
		}
	}

	private static RevenjQueryTransformConfiguration buildConfig(MetamodelUtil metamodel) {
		RevenjQueryTransformConfiguration config = new RevenjQueryTransformConfiguration();
		config.metamodel = metamodel;
		config.alternateClassLoader = null;
		config.isObjectEqualsSafe = true;
		config.isCollectionContainsSafe = true;
		return config;
	}

	private SelectFromWhere<TSource> applyTransformWithLambda(String name, LambdaInfo lambdaInfo) {
		if (lambdaInfo == null) {
			return null;
		}
		try {
			RevenjQueryTransformConfiguration config = buildConfig(metamodel);
			LambdaAnalysis lambdaAnalysis = lambdaInfo.fullyAnalyze(metamodel, loader, true, true, true, true);
			if (lambdaAnalysis == null) {
				return null;
			}
			config.checkLambdaSideEffects(lambdaAnalysis);
			SelectFromWhere<TSource> query = new SelectFromWhere<>();
			From.FromDataSource from = new From.FromDataSource();
			from.name = name;
			query.cols = ColumnExpressions.singleColumn(SimpleRowReader.READER, new FromAliasExpression(from));
			query.froms.add(from);
			WhereTransform where = new WhereTransform(config, false);
			return where.apply(lambdaAnalysis, null, query);
		} catch (QueryTransformException | TypedValueVisitorException e) {
			throw new RuntimeException(e);
		}
	}

	protected Specification<TSource> rewriteSpecification(Specification<TSource> specification) {
		return specification;
	}

	public void prepareSql(
			StringBuilder sb,
			boolean asRecord,
			List<String> usedDimensions,
			List<String> usedFacts,
			Collection<Map.Entry<String, Boolean>> order,
			Specification<TSource> filter,
			Integer limit,
			Integer offset,
			List<GeneratedQueryParameter> parameters,
			List<LambdaInfo> lambdas) {
		Map<String, Boolean> customOrder = new LinkedHashMap<>();
		if (order != null) {
			for (Map.Entry<String, Boolean> o : order) {
				if (o.getKey() != null) {
					customOrder.put(o.getKey(), o.getValue());
				}
			}
		}

		validateInput(usedDimensions, usedFacts, customOrder.keySet());

		String alias = "_it";
		sb.append("SELECT ");
		if (asRecord) {
			sb.append("ROW(");
		}
		for (String d : usedDimensions) {
			sb.append(cubeDimensions.get(d).apply(alias)).append(',');
		}
		for (String f : usedFacts) {
			sb.append(cubeFacts.get(f).apply(alias)).append(',');
		}
		sb.setLength(sb.length() - 1);
		if (asRecord) {
			sb.append(")");
		}
		sb.append(" FROM ").append(getSource()).append(" \"").append(alias).append("\"");

		if (filter != null) {
			LambdaInfo lambdaInfo = LambdaInfo.analyze(rewriteSpecification(filter), 0, true);
			SelectFromWhere<?> sfw = applyTransformWithLambda(alias, lambdaInfo);
			if (sfw != null && sfw.generateWhere("\"" + alias + "\"")) {
				sb.append(" WHERE ");
				sb.append(sfw.getQueryString());
				parameters.addAll(sfw.getQueryParameters());
			}
			lambdas.add(lambdaInfo);
		}
		if (!usedDimensions.isEmpty()) {
			sb.append(" GROUP BY ");
			for (String d : usedDimensions) {
				sb.append(cubeDimensions.get(d).apply(alias));
				sb.append(", ");
			}
			sb.setLength(sb.length() - 2);
			sb.append('\n');
		}
		if (!customOrder.isEmpty()) {
			sb.append(" ORDER BY ");
			for (Map.Entry<String, Boolean> o : customOrder.entrySet()) {
				if (cubeDimensions.containsKey(o.getKey())) {
					sb.append(cubeDimensions.get(o.getKey()).apply(alias));
				} else if (cubeFacts.containsKey(o.getKey())) {
					sb.append(cubeFacts.get(o.getKey()).apply(alias));
				} else {
					sb.append("\"").append(o.getKey()).append("\"");
				}
				sb.append(o.getValue() ? "" : "DESC");
				sb.append(", ");
			}
			sb.setLength(sb.length() - 2);
		}
		if (limit != null) {
			sb.append(" LIMIT ").append(Integer.toString(limit));
		}
		if (offset != null) {
			sb.append(" OFFSET ").append(Integer.toString(offset));
		}
	}

	public Converter[] prepareConverters(List<String> dimensions, List<String> facts) {
		@SuppressWarnings("unchecked")
		Converter[] converters = new Converter[dimensions.size() + facts.size()];
		for (int i = 0; i < dimensions.size(); i++) {
			converters[i] = cubeConverters.get(dimensions.get(i));
		}
		for (int i = 0; i < facts.size(); i++) {
			converters[i + dimensions.size()] = cubeConverters.get(facts.get(i));
		}
		return converters;
	}

	@Override
	public List<Map<String, Object>> analyze(
			List<String> dimensions,
			List<String> facts,
			Collection<Map.Entry<String, Boolean>> order,
			Specification<TSource> filter,
			Integer limit,
			Integer offset) {
		List<String> usedDimensions = new ArrayList<>();
		List<String> usedFacts = new ArrayList<>();

		if (dimensions != null) {
			usedDimensions.addAll(dimensions);
		}
		if (facts != null) {
			usedFacts.addAll(facts);
		}

		List<GeneratedQueryParameter> parameters = filter != null ? new ArrayList<>() : null;
		List<LambdaInfo> lambdas = filter != null ? new ArrayList<>(1) : null;
		StringBuilder sb = new StringBuilder();
		prepareSql(sb, true, usedDimensions, usedFacts, order, filter, limit, offset, parameters, lambdas);
		Converter[] converters = prepareConverters(usedDimensions, usedFacts);

		Connection connection = getConnection();
		List<Map<String, Object>> result = new ArrayList<>();
		try (PreparedStatement ps = connection.prepareStatement(sb.toString())) {
			if (parameters != null && parameters.size() > 0) {
				RevenjQueryComposer.fillQueryParameters(
						connection,
						locator,
						ps,
						0,
						parameters,
						lambdas);
			}
			ResultSet rs = ps.executeQuery();
			String[] columnNames = new String[usedFacts.size() + usedDimensions.size()];
			for (int i = 0; i < usedDimensions.size(); i++) {
				columnNames[i] = usedDimensions.get(i);
			}
			for (int i = 0; i < usedFacts.size(); i++) {
				columnNames[usedDimensions.size() + i] = usedFacts.get(i);
			}
			while (rs.next()) {
				reader.process(rs.getString(1));
				reader.read();
				Map<String, Object> item = new LinkedHashMap<>();
				for (int i = 0; i < columnNames.length; i++) {
					item.put(columnNames[i], converters[i].convert(reader, 1));
				}
				result.add(item);
			}
			rs.close();
		} catch (SQLException | IOException ex) {
			throw new RuntimeException(ex);
		}
		return result;
	}

	public ResultSet stream(
			Connection connection,
			List<String> dimensions,
			List<String> facts,
			Collection<Map.Entry<String, Boolean>> order,
			Specification<TSource> filter,
			Integer limit,
			Integer offset) throws SQLException {
		List<String> usedDimensions = new ArrayList<>();
		List<String> usedFacts = new ArrayList<>();

		if (dimensions != null) {
			usedDimensions.addAll(dimensions);
		}
		if (facts != null) {
			usedFacts.addAll(facts);
		}

		List<GeneratedQueryParameter> parameters = filter != null ? new ArrayList<>() : null;
		List<LambdaInfo> lambdas = filter != null ? new ArrayList<>(1) : null;
		StringBuilder sb = new StringBuilder();
		prepareSql(sb, false, usedDimensions, usedFacts, order, filter, limit, offset, parameters, lambdas);

		PreparedStatement ps = connection.prepareStatement(sb.toString());
		if (parameters != null && parameters.size() > 0) {
			RevenjQueryComposer.fillQueryParameters(
					connection,
					locator,
					ps,
					0,
					parameters,
					lambdas);
		}
		return ps.executeQuery();
	}
}
