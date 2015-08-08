package org.revenj.postgres.jinq;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.postgresql.util.PGobject;
import org.revenj.Utils;
import org.revenj.patterns.*;
import org.revenj.postgres.PostgresWriter;
import org.revenj.postgres.jinq.transform.JPQLMultiLambdaQueryTransform;
import org.revenj.postgres.jinq.transform.JPQLNoLambdaQueryTransform;
import org.revenj.postgres.jinq.transform.JPQLOneLambdaQueryTransform;
import org.revenj.postgres.jinq.transform.JPQLQueryTransformConfiguration;
import org.revenj.postgres.jinq.transform.LambdaAnalysis;
import org.revenj.postgres.jinq.transform.LambdaInfo;
import org.revenj.postgres.jinq.transform.LimitSkipTransform;
import org.revenj.postgres.jinq.transform.MetamodelUtil;
import org.revenj.postgres.jinq.transform.QueryTransformException;
import org.revenj.postgres.jinq.transform.SortingTransform;
import org.revenj.postgres.jinq.transform.WhereTransform;
import org.revenj.postgres.ObjectConverter;
import org.revenj.postgres.PostgresReader;
import org.revenj.postgres.jinq.jpqlquery.GeneratedQueryParameter;
import org.revenj.postgres.jinq.jpqlquery.JinqPostgresQuery;

final class RevenjQueryComposer<T> {

	private static final Map<Class<?>, String> typeMapping = new HashMap<>();

	static {
		typeMapping.put(int.class, "int");
		typeMapping.put(Integer.class, "int");
		typeMapping.put(String.class, "varchar");
		typeMapping.put(long.class, "bigint");
		typeMapping.put(Long.class, "bigint");
		typeMapping.put(BigDecimal.class, "numeric");
		typeMapping.put(float.class, "real");
		typeMapping.put(Float.class, "real");
		typeMapping.put(double.class, "float");
		typeMapping.put(Double.class, "float");
		typeMapping.put(UUID.class, "uuid");
		typeMapping.put(Map.class, "hstore");
		typeMapping.put(byte[].class, "bytea");
	}


	private final MetamodelUtil metamodel;
	private final RevenjQueryComposerCache cachedQueries;
	private final Connection connection;
	private final ServiceLocator locator;
	private final JinqPostgresQuery<T> query;
	private final Class<T> manifest;

	/**
	 * Holds the chain of lambdas that were used to create this query. This is needed
	 * because query parameters (which are stored in the lambda objects) are only
	 * substituted into the query during query execution, which occurs much later
	 * than query generation.
	 */
	private final List<LambdaInfo> lambdas = new ArrayList<>();

	private RevenjQueryComposer(RevenjQueryComposer<?> base, Class<T> manifest, JinqPostgresQuery<T> query, List<LambdaInfo> chainedLambdas, LambdaInfo... additionalLambdas) {
		this(base.metamodel, manifest, base.cachedQueries, base.connection, base.locator, query, chainedLambdas, additionalLambdas);
	}

	private RevenjQueryComposer(MetamodelUtil metamodel, Class<T> manifest, RevenjQueryComposerCache cachedQueries, Connection connection, ServiceLocator locator, JinqPostgresQuery<T> query, List<LambdaInfo> chainedLambdas, LambdaInfo... additionalLambdas) {
		this.metamodel = metamodel;
		this.manifest = manifest;
		this.cachedQueries = cachedQueries;
		this.connection = connection;
		this.locator = locator;
		this.query = query;
		lambdas.addAll(chainedLambdas);
		for (LambdaInfo newLambda : additionalLambdas) {
			lambdas.add(newLambda);
		}
	}

	public static <T> RevenjQueryComposer<T> findAll(
			MetamodelUtil metamodel,
			Class<T> manifest,
			RevenjQueryComposerCache cachedQueries,
			Connection conn,
			ServiceLocator locator,
			JinqPostgresQuery<T> findAllQuery) {
		return new RevenjQueryComposer<>(metamodel, manifest, cachedQueries, conn, locator, findAllQuery, new ArrayList<>());
	}

	private static String getTypeFor(Class<?> manifest) {
		return typeMapping.get(manifest);
	}

	private static String getElementTypeFor(Object[] elements) {
		for (Object item : elements) {
			if (item != null) {
				String type = getTypeFor(item.getClass());
				if (type != null) {
					return type;
				}
			}
		}
		return "unknown";
	}

	private void fillQueryParameters(PreparedStatement ps, List<GeneratedQueryParameter> parameters) throws SQLException {
		for (int i = 0; i < parameters.size(); i++) {
			GeneratedQueryParameter param = parameters.get(i);
			Object value;
			if (param.fieldName == null) {
				value = lambdas.get(param.lambdaIndex).getCapturedArg(param.argIndex);
			} else {
				value = lambdas.get(param.lambdaIndex).getField(param.fieldName);
			}
			if (value == null) {
				ps.setObject(i + 1, null);
				continue;
			}
			Object[] elements = null;
			if (value instanceof Collection) {
				Collection collection = (Collection) value;
				elements = new Object[collection.size()];
				int x = 0;
				for (Object item : collection) {
					elements[x++] = item;
				}
			} else if (value instanceof Object[]) {
				elements = (Object[]) value;
			}
			if (elements == null) {
				Optional<ObjectConverter> converter = getConverterFor(value.getClass());
				if (converter.isPresent()) {
					PGobject pgo = new PGobject();
					pgo.setValue(converter.get().to(value).buildTuple(false));
					pgo.setType(converter.get().getDbName());
					ps.setObject(i + 1, pgo);
				} else {
					ps.setObject(i + 1, value);
				}
			} else {
				Class<?> manifest = null;
				for (Object item : elements) {
					if (item != null) {
						manifest = item.getClass();
						break;
					}
				}
				Optional<ObjectConverter> converter = manifest != null ? getConverterFor(manifest) : Optional.<ObjectConverter>empty();
				if (converter.isPresent()) {
					ObjectConverter oc = converter.get();
					Object[] pgos = new Object[elements.length];
					try (PostgresWriter writer = PostgresWriter.create()) {
						for (int x = 0; x < pgos.length; x++) {
							Object item = elements[x];
							if (item != null) {
								PGobject pgo = new PGobject();
								oc.to(item).buildTuple(writer, false);
								pgo.setValue(writer.toString());
								writer.reset();
								pgo.setType(oc.getDbName());
								ps.setObject(i + 1, pgo);
							}
						}
					}
					java.sql.Array array = connection.createArrayOf(oc.getDbName(), pgos);
					ps.setArray(i + 1, array);
				} else {
					java.sql.Array array = connection.createArrayOf(getElementTypeFor(elements), elements);
					ps.setArray(i + 1, array);
				}
			}
		}
	}

	private static final ConcurrentMap<Class<?>, Optional<ObjectConverter>> objectConverters = new ConcurrentHashMap<>();

	private Optional<ObjectConverter> getConverterFor(Class<?> manifest) {
		return objectConverters.computeIfAbsent(manifest, clazz ->
		{
			try {
				ObjectConverter result = (ObjectConverter) locator.resolve(Utils.makeGenericType(ObjectConverter.class, clazz));
				return Optional.of(result);
			} catch (Exception ignore) {
				return Optional.empty();
			}
		});
	}

	public long count() throws SQLException {
		final String queryString = query.getQueryString();
		PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM (" + queryString + ") sq");
		fillQueryParameters(ps, query.getQueryParameters());
		try (final ResultSet rs = ps.executeQuery()) {
			if (rs.next()) {
				return rs.getLong(1);
			}
		}
		return 0;
	}

	public boolean any() throws SQLException {
		final String queryString = query.getQueryString();
		PreparedStatement ps = connection.prepareStatement("SELECT EXISTS(" + queryString + ")");
		fillQueryParameters(ps, query.getQueryParameters());
		try (final ResultSet rs = ps.executeQuery()) {
			if (rs.next()) {
				return rs.getBoolean(1);
			}
		}
		return false;
	}

	//TODO: optimize
	public boolean all(Object lambda) throws SQLException {
		long filter = this.where(lambda).count();
		long all = this.count();
		return filter == all && all > 0;
	}

	public boolean none() throws SQLException {
		final String queryString = query.getQueryString();
		PreparedStatement ps = connection.prepareStatement("SELECT NOT EXISTS(" + queryString + ")");
		fillQueryParameters(ps, query.getQueryParameters());
		try (final ResultSet rs = ps.executeQuery()) {
			if (rs.next()) {
				return rs.getBoolean(1);
			}
		}
		return true;
	}

	public Optional<T> first() throws SQLException {
		final String queryString = query.getQueryString();
		PreparedStatement ps = connection.prepareStatement(queryString);
		fillQueryParameters(ps, query.getQueryParameters());
		final PostgresReader pr = new PostgresReader(locator);
		try {
			final ObjectConverter<T> converter = getConverterFor(manifest).get();
			try (final ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					pr.process(rs.getString(1));
					return Optional.of(converter.from(pr));
				}
			}
		} catch (IOException e) {
			throw new SQLException(e);
		}
		return Optional.empty();
	}

	public List<T> toList() throws SQLException {
		final String queryString = query.getQueryString();
		PreparedStatement ps = connection.prepareStatement(queryString);
		fillQueryParameters(ps, query.getQueryParameters());
		final PostgresReader pr = new PostgresReader(locator);
		final ArrayList<T> result = new ArrayList<>();
		try {
			final ObjectConverter<T> converter = getConverterFor(manifest).get();
			try (final ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					pr.process(rs.getString(1));
					result.add(converter.from(pr));
				}
			}
		} catch (IOException e) {
			throw new SQLException(e);
		}
		return result;
	}

	private <U> RevenjQueryComposer<U> applyTransformWithLambda(Class<U> newManifest, JPQLNoLambdaQueryTransform transform) {
		Optional<JinqPostgresQuery<?>> cachedQuery = cachedQueries.findInCache(query, transform.getTransformationTypeCachingTag(), null);
		if (cachedQuery == null) {
			cachedQuery = Optional.empty();
			JinqPostgresQuery<U> newQuery = null;
			try {
				newQuery = transform.apply(query, null);
			} catch (QueryTransformException e) {
				throw new RuntimeException(e);
			} finally {
				// Always cache the resulting query, even if it is an error
				cachedQuery = Optional.ofNullable(newQuery);
				cachedQuery = cachedQueries.cacheQuery(query, transform.getTransformationTypeCachingTag(), null, cachedQuery);
			}
		}
		if (!cachedQuery.isPresent()) {
			return null;
		}
		return new RevenjQueryComposer<>(this, newManifest, (JinqPostgresQuery<U>) cachedQuery.get(), lambdas);
	}

	public <U> RevenjQueryComposer<U> applyTransformWithLambda(Class<U> newManifest, JPQLOneLambdaQueryTransform transform, Object lambda) {
		LambdaInfo lambdaInfo = LambdaInfo.analyze(lambda, lambdas.size(), true);
		if (lambdaInfo == null) {
			return null;
		}
		Optional<JinqPostgresQuery<?>> cachedQuery =
				cachedQueries.findInCache(
						query,
						transform.getTransformationTypeCachingTag(),
						new String[]{lambdaInfo.getLambdaSourceString()});
		if (cachedQuery == null) {
			JinqPostgresQuery<U> newQuery = null;
			try {
				LambdaAnalysis lambdaAnalysis = lambdaInfo.fullyAnalyze(metamodel, null, true, true, true);
				if (lambdaAnalysis == null) {
					return null;
				}
				getConfig().checkLambdaSideEffects(lambdaAnalysis);
				newQuery = transform.apply(query, lambdaAnalysis, null);
			} catch (QueryTransformException e) {
				throw new RuntimeException(e);
			} finally {
				// Always cache the resulting query, even if it is an error
				cachedQuery = Optional.ofNullable(newQuery);
				cachedQuery = cachedQueries.cacheQuery(query, transform.getTransformationTypeCachingTag(), new String[]{lambdaInfo.getLambdaSourceString()}, cachedQuery);
			}
		}
		if (!cachedQuery.isPresent()) {
			return null;
		}
		return new RevenjQueryComposer<>(this, newManifest, (JinqPostgresQuery<U>) cachedQuery.get(), lambdas, lambdaInfo);
	}

	public <U> RevenjQueryComposer<U> applyTransformWithLambdas(Class<U> newManifest, JPQLMultiLambdaQueryTransform transform, Object[] groupingLambdas) {
		LambdaInfo[] lambdaInfos = new LambdaInfo[groupingLambdas.length];
		String[] lambdaSources = new String[lambdaInfos.length];
		for (int n = 0; n < groupingLambdas.length; n++) {
			lambdaInfos[n] = LambdaInfo.analyze(groupingLambdas[n], lambdas.size() + n, true);
			if (lambdaInfos[n] == null) {
				return null;
			}
			lambdaSources[n] = lambdaInfos[n].getLambdaSourceString();
		}

		Optional<JinqPostgresQuery<?>> cachedQuery =
				cachedQueries.findInCache(query, transform.getTransformationTypeCachingTag(), lambdaSources);
		if (cachedQuery == null) {
			JinqPostgresQuery<U> newQuery = null;
			try {
				LambdaAnalysis[] lambdaAnalyses = new LambdaAnalysis[lambdaInfos.length];
				for (int n = 0; n < lambdaInfos.length; n++) {
					lambdaAnalyses[n] = lambdaInfos[n].fullyAnalyze(metamodel, null, true, true, true);
					if (lambdaAnalyses[n] == null) {
						return null;
					}
					getConfig().checkLambdaSideEffects(lambdaAnalyses[n]);
				}
				newQuery = transform.apply(query, lambdaAnalyses, null);
			} catch (QueryTransformException e) {
				throw new RuntimeException(e);
			} finally {
				// Always cache the resulting query, even if it is an error
				cachedQuery = Optional.ofNullable(newQuery);
				cachedQuery = cachedQueries.cacheQuery(query, transform.getTransformationTypeCachingTag(), lambdaSources, cachedQuery);
			}
		}
		if (!cachedQuery.isPresent()) {
			return null;
		}
		return new RevenjQueryComposer<>(this, newManifest, (JinqPostgresQuery<U>) cachedQuery.get(), lambdas, lambdaInfos);
	}

	/**
	 * Holds configuration information used when transforming this composer to a new composer.
	 * Since a JPAQueryComposer can only be transformed once, we only need one transformationConfig
	 * (and it is instantiated lazily).
	 */
	private JPQLQueryTransformConfiguration transformationConfig = null;

	public JPQLQueryTransformConfiguration getConfig() {
		if (transformationConfig == null) {
			transformationConfig = new JPQLQueryTransformConfiguration();
			transformationConfig.metamodel = metamodel;
			transformationConfig.alternateClassLoader = null;
			transformationConfig.isObjectEqualsSafe = true;
			transformationConfig.isCollectionContainsSafe = true;
		}
		return transformationConfig;
	}

	public <E extends Exception> RevenjQueryComposer<T> where(Object testLambda) {
		return applyTransformWithLambda(manifest, new WhereTransform(getConfig(), false), testLambda);
	}

	public <V extends Comparable<V>> RevenjQueryComposer<T> sortedBy(
			Object sorter, boolean isAscending) {
		return applyTransformWithLambda(manifest, new SortingTransform(getConfig(), isAscending), sorter);
	}

	public RevenjQueryComposer<T> limit(long n) {
		return applyTransformWithLambda(manifest, new LimitSkipTransform(getConfig(), true, n));
	}

	public RevenjQueryComposer<T> skip(long n) {
		return applyTransformWithLambda(manifest, new LimitSkipTransform(getConfig(), false, n));
	}
}
