package org.revenj.postgres;

import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import org.revenj.patterns.DataSource;
import org.revenj.patterns.OlapCubeQuery;
import org.revenj.patterns.ServiceLocator;
import org.revenj.patterns.Specification;
import org.revenj.postgres.jinq.RevenjQueryComposer;
import org.revenj.postgres.jinq.jpqlquery.*;
import org.revenj.postgres.jinq.transform.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

public abstract class PostgresOlapCubeQuery<TSource extends DataSource> implements OlapCubeQuery<TSource> {

	protected final ServiceLocator locator;
	protected final Connection transactionConnection;
	protected final javax.sql.DataSource dataSource;
	private final MetamodelUtil metamodel;

	protected abstract String getSource();

	protected final Map<String, Function<String, String>> cubeDimensions = new LinkedHashMap<>();
	protected final Map<String, Function<String, String>> cubeFacts = new LinkedHashMap<>();

	protected PostgresOlapCubeQuery(ServiceLocator locator) {
		this.locator = locator;
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
			if (!cubeDimensions.containsKey(o) && !cubeFacts.containsKey(o)) {
				throw new IllegalArgumentException("Invalid order: " + o + ". Order can be only field from used dimensions and facts.");
			}
		}
	}

	protected String getLambdaAlias(Specification<TSource> specification) {
		return "it";
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

	private RevenjQueryTransformConfiguration transformationConfig = null;

	public RevenjQueryTransformConfiguration getConfig() {
		if (transformationConfig == null) {
			transformationConfig = new RevenjQueryTransformConfiguration();
			transformationConfig.metamodel = metamodel;
			transformationConfig.alternateClassLoader = null;
			transformationConfig.isObjectEqualsSafe = true;
			transformationConfig.isCollectionContainsSafe = true;
		}
		return transformationConfig;
	}

	public SelectFromWhere<TSource> applyTransformWithLambda(
			String name,
			WhereTransform where,
			LambdaInfo lambdaInfo) {
		if (lambdaInfo == null) {
			return null;
		}
		try {
			LambdaAnalysis lambdaAnalysis = lambdaInfo.fullyAnalyze(metamodel, null, true, true, true);
			if (lambdaAnalysis == null) {
				return null;
			}
			getConfig().checkLambdaSideEffects(lambdaAnalysis);
			SelectFromWhere<TSource> query = new SelectFromWhere<>();
			From.FromDataSource from = new From.FromDataSource();
			from.name = name;
			query.cols = ColumnExpressions.singleColumn(SimpleRowReader.READER, new FromAliasExpression(from));
			query.froms.add(from);
			return where.apply(lambdaAnalysis, null, query);
		} catch (QueryTransformException | TypedValueVisitorException e) {
			throw new RuntimeException(e);
		}
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
		Map<String, Boolean> customOrder = new LinkedHashMap<>();

		if (dimensions != null) {
			usedDimensions.addAll(dimensions);
		}
		if (facts != null) {
			usedFacts.addAll(facts);
		}
		if (order != null) {
			for (Map.Entry<String, Boolean> o : order) {
				if (o.getKey() != null) {
					customOrder.put(o.getKey(), o.getValue());
				}
			}
		}

		validateInput(usedDimensions, usedFacts, customOrder.keySet());

		StringBuilder sb = new StringBuilder();
		String alias = filter != null ? getLambdaAlias(filter) : "it";
		sb.append("SELECT ");
		for (String d : usedDimensions) {
			sb.append(cubeDimensions.get(d).apply(alias)).append(" AS \"").append(d).append("\", ");
		}
		for (String f : usedFacts) {
			sb.append(cubeFacts.get(f).apply(alias)).append(" AS \"").append(f).append("\", ");
		}
		sb.setLength(sb.length() - 2);
		sb.append('\n');
		sb.append("FROM ").append(getSource()).append(" \"").append(alias).append("\"");
		sb.append('\n');

		SelectFromWhere<TSource> sfw = null;
		LambdaInfo lambdaInfo = null;
		if (filter != null) {
			lambdaInfo = LambdaInfo.analyze(filter, 0, true);
			sfw = applyTransformWithLambda(alias, new WhereTransform(getConfig(), false), lambdaInfo);
			if (sfw != null && sfw.generateWhere(alias)) {
				sb.append("WHERE\n");
				sb.append(sfw.getQueryString());
			}
		}
		sb.append('\n');
		if (!usedDimensions.isEmpty()) {
			sb.append("GROUP BY ");
			for (String d : usedDimensions) {
				sb.append(cubeDimensions.get(d).apply(alias));
				sb.append(", ");
			}
			sb.setLength(sb.length() - 2);
			sb.append('\n');
		}
		if (!customOrder.isEmpty()) {
			sb.append("ORDER BY ");
			for (Map.Entry<String, Boolean> o : customOrder.entrySet()) {
				sb.append("\"").append(o.getKey()).append("\" ").append(o.getValue() ? "ASC" : "DESC");
				sb.append(", ");
			}
			sb.setLength(sb.length() - 2);
		}
		if (limit != null) {
			sb.append("LIMIT ").append(limit).append('\n');
		}
		if (offset != null) {
			sb.append("OFFSET ").append(offset).append('\n');
		}

		Connection connection = getConnection();
		List<Map<String, Object>> result = new ArrayList<>();
		try (PreparedStatement ps = connection.prepareStatement(sb.toString())) {
			if (sfw != null) {
				RevenjQueryComposer.fillQueryParameters(
						connection,
						locator,
						ps,
						sfw.getQueryParameters(),
						Collections.singletonList(lambdaInfo));
			}
			ResultSet rs = ps.executeQuery();
			int columns = rs.getMetaData().getColumnCount();
			String[] columnNames = new String[columns];
			for (int i = 0; i < usedDimensions.size(); i++) {
				columnNames[i] = usedDimensions.get(i);
			}
			for (int i = 0; i < usedFacts.size(); i++) {
				columnNames[usedDimensions.size() + i] = usedFacts.get(i);
			}
			while (rs.next()) {
				Map<String, Object> item = new LinkedHashMap<>();
				for (int i = 0; i < columns; i++) {
					item.put(columnNames[i], rs.getObject(1 + 1));
				}
				result.add(item);
			}
			rs.close();
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}

		return result;
	}
}
