package org.revenj;

import org.revenj.patterns.*;
import org.revenj.database.postgres.*;
import org.revenj.database.postgres.converters.ArrayTuple;
import org.revenj.database.postgres.jinq.RevenjQueryComposer;
import org.revenj.database.postgres.jinq.jpqlquery.GeneratedQueryParameter;
import org.revenj.database.postgres.jinq.transform.LambdaInfo;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Consumer;

class PostgresBulkReader implements RepositoryBulkReader, BulkReaderQuery, AutoCloseable {

	private final ServiceLocator locator;
	private final Connection connection;
	private final PostgresReader reader;
	private final PostgresWriter writer;
	private final StringBuilder builder;
	private final List<Consumer<PreparedStatement>> writeArguments = new ArrayList<>();
	private int totalArguments;
	private final List<BiFunction<ResultSet, Integer, Object>> resultActions = new ArrayList<>();
	private Object[] results;
	private final Map<Class<?>, BulkRepository> repositories = new HashMap<>();
	private final Map<Class<?>, PostgresOlapCubeQuery> cubes = new HashMap<>();
	private final boolean closeConnection;

	public PostgresBulkReader(ServiceLocator locator, Connection connection, boolean closeConnection) {
		this.locator = locator;
		this.connection = connection;
		this.closeConnection = closeConnection;
		this.reader = PostgresReader.create(locator);
		this.writer = PostgresWriter.create();
		this.builder = new StringBuilder("SELECT (");
	}

	public static PostgresBulkReader create(ServiceLocator locator) {
		Optional<Connection> tryConnection = locator.tryResolve(Connection.class);
		boolean closeConnection = tryConnection.isPresent();
		Connection connection;
		if (!closeConnection) {
			javax.sql.DataSource ds = locator.resolve(javax.sql.DataSource.class);
			try {
				connection = ds.getConnection();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		} else {
			connection = tryConnection.get();
		}
		return new PostgresBulkReader(locator, connection, closeConnection);
	}

	@Override
	public PostgresReader getReader() {
		return reader;
	}

	@Override
	public PostgresWriter getWriter() {
		return writer;
	}

	@Override
	public StringBuilder getBuilder() {
		return builder;
	}

	@Override
	public int getArgumentIndex() {
		return totalArguments + 1;
	}

	@Override
	public void reset() {
		writer.reset();
		builder.setLength(0);
		resultActions.clear();
		writeArguments.clear();
		totalArguments = 0;
		results = null;
		builder.append("SELECT (");
	}

	@Override
	public void addArgument(Consumer<PreparedStatement> statement) {
		writeArguments.add(statement);
		totalArguments++;
	}

	@SuppressWarnings("unchecked")
	private <T> Callable<T> add(BiFunction<ResultSet, Integer, T> reader) {
		builder.append("),(");
		int i = resultActions.size();
		resultActions.add(reader::apply);
		return () -> {
			if (results == null) {
				execute();
			}
			return (T) results[i];
		};
	}

	@SuppressWarnings("unchecked")
	private BulkRepository getRepository(Class<?> manifest) {
		BulkRepository repository = repositories.get(manifest);
		if (repository == null) {
			try {
				repository = locator.resolve(BulkRepository.class, manifest);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("Specified type: " + manifest + " doesn't support bulk reading", e);
			}
			repositories.put(manifest, repository);
		}
		return repository;
	}

	@SuppressWarnings("unchecked")
	private <TSource extends DataSource, TCube extends OlapCubeQuery<TSource>> PostgresOlapCubeQuery<TSource> getCube(Class<TCube> manifest) {
		PostgresOlapCubeQuery cube = cubes.get(manifest);
		if (cube == null) {
			cube = (PostgresOlapCubeQuery) locator.resolve(manifest);
			cubes.put(manifest, cube);
		}
		return cube;
	}

	@Override
	public <T extends Identifiable> Callable<Optional<T>> find(Class<T> manifest, String uri) {
		return add(getRepository(manifest).find(this, uri));
	}

	@Override
	public <T extends Identifiable> Callable<List<T>> find(Class<T> manifest, String[] uri) {
		return add(getRepository(manifest).find(this, uri));
	}

	@Override
	public <T extends DataSource> Callable<List<T>> search(Class<T> manifest, Specification<T> filter, Integer limit, Integer offset) {
		return add(getRepository(manifest).search(this, filter, limit, offset));
	}

	@Override
	public <T extends DataSource> Callable<Long> count(Class<T> manifest, Specification<T> filter) {
		return add(getRepository(manifest).count(this, filter));
	}

	@Override
	public <T extends DataSource> Callable<Boolean> exists(Class<T> manifest, Specification<T> filter) {
		return add(getRepository(manifest).exists(this, filter));
	}

	@Override
	public <TSource extends DataSource, TCube extends OlapCubeQuery<TSource>> Callable<List<Map<String, Object>>> analyze(
			Class<TCube> manifest,
			List<String> dimensionsAndFacts,
			Collection<Map.Entry<String, Boolean>> order,
			Specification<TSource> filter,
			Integer limit,
			Integer offset) {
		PostgresOlapCubeQuery<TSource> cube = getCube(manifest);
		builder.append("SELECT array_agg(_x) FROM (");
		List<String> dimensions = new ArrayList<>(dimensionsAndFacts.size());
		List<String> facts = new ArrayList<>(dimensionsAndFacts.size());
		for (String dof : dimensionsAndFacts) {
			if (cube.getDimensions().contains(dof)) {
				dimensions.add(dof);
			} else {
				facts.add(dof);
			}
		}
		List<GeneratedQueryParameter> parameters = filter != null ? new ArrayList<>() : null;
		List<LambdaInfo> lambdas = filter != null ? new ArrayList<>(1) : null;
		cube.prepareSql(builder, true, dimensions, facts, order, filter, limit, offset, parameters, lambdas);
		PostgresOlapCubeQuery.Converter[] converters = cube.prepareConverters(dimensions, facts);
		String[] columnNames = new String[dimensionsAndFacts.size()];
		for (int x = 0; x < dimensions.size(); x++) {
			columnNames[x] = dimensions.get(x);
		}
		for (int x = 0; x < facts.size(); x++) {
			columnNames[dimensions.size() + x] = facts.get(x);
		}
		builder.append(") _x),(");
		int i = resultActions.size();
		List<Map<String, Object>> result = new ArrayList<>();
		int args = getArgumentIndex();
		writeArguments.add(ps -> {
			try {
				RevenjQueryComposer.fillQueryParameters(
						connection,
						locator,
						ps,
						args,
						parameters,
						lambdas);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		});
		totalArguments += parameters != null ? parameters.size() : 0;
		resultActions.add((rs, ind) -> {
			try {
				reader.process(rs.getString(ind));
				ArrayTuple.parse(reader, 0, (rdr, outCtx, ctx) -> {
					Map<String, Object> map = new LinkedHashMap<>();
					rdr.read(3);
					for (int x = 0; x < converters.length; x++) {
						map.put(columnNames[x], converters[x].convert(rdr, 1));
					}
					rdr.read(3);
					result.add(map);
					return map;
				});
				return result;
			} catch (SQLException | IOException ex) {
				throw new RuntimeException(ex);
			}
		});
		return () -> {
			if (results == null) {
				execute();
			}
			return result;
		};
	}

	@Override
	public void execute() throws IOException {
		results = new Object[resultActions.size()];
		try {
			try (PreparedStatement ps = connection.prepareStatement(builder.substring(0, builder.length() - 2))) {
				for (Consumer<PreparedStatement> writeArgument : writeArguments) {
					writeArgument.accept(ps);
				}
				ps.setEscapeProcessing(false);
				ResultSet rs = ps.executeQuery();
				rs.next();
				for (int i = 0; i < resultActions.size(); i++) {
					results[i] = resultActions.get(i).apply(rs, i + 1);
				}
			}
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void close() throws Exception {
		if (closeConnection) {
			connection.close();
		}
		reader.close();
		writer.close();
	}
}
