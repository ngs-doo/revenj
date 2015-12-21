package org.revenj;

import org.revenj.patterns.*;
import org.revenj.postgres.*;

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
	private final List<BiFunction<ResultSet, Integer, Object>> resultActions = new ArrayList<>();
	private Object[] results;
	private final Map<Class<?>, BulkRepository> repositories = new HashMap<>();
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
		return writeArguments.size() + 1;
	}

	@Override
	public void reset() {
		writer.reset();
		builder.setLength(0);
		resultActions.clear();
		writeArguments.clear();
		results = null;
		builder.append("SELECT (");
	}

	@Override
	public void addArgument(Consumer<PreparedStatement> statement) {
		writeArguments.add(statement);
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
	private <T> BulkRepository<T> getRepository(Class<T> manifest) {
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
	public void execute() throws IOException {
		results = new Object[resultActions.size()];
		builder.setLength(builder.length() - 2);
		try {
			try (PreparedStatement ps = connection.prepareStatement(builder.toString())) {
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
