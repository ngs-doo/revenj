/*
* Created by DSL Platform
* v1.0.0.20667 
*/

package gen.model.issues.repositories;



public class TimestampPkHistoryRepository   implements java.io.Closeable, org.revenj.patterns.Repository<org.revenj.patterns.History<gen.model.issues.TimestampPk>> {
	
	
	
	public TimestampPkHistoryRepository(
			 final java.util.Optional<java.sql.Connection> transactionContext,
			 final javax.sql.DataSource dataSource,
			 final org.revenj.postgres.QueryProvider queryProvider,
			 final gen.model.issues.converters.TimestampPkConverter converter,
			 final org.revenj.patterns.ServiceLocator locator) {
			
		this.transactionContext = transactionContext;
		this.dataSource = dataSource;
		this.queryProvider = queryProvider;
		this.transactionConnection = transactionContext.orElse(null);
		this.converter = converter;
		this.locator = locator;
	}

	private final java.util.Optional<java.sql.Connection> transactionContext;
	private final javax.sql.DataSource dataSource;
	private final org.revenj.postgres.QueryProvider queryProvider;
	private final java.sql.Connection transactionConnection;
	private final gen.model.issues.converters.TimestampPkConverter converter;
	private final org.revenj.patterns.ServiceLocator locator;
	
	private java.sql.Connection getConnection() {
		if (transactionConnection != null) return transactionConnection;
		try {
			return dataSource.getConnection();
		} catch (java.sql.SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private void releaseConnection(java.sql.Connection connection) {
		if (this.transactionConnection != null) return;
		try {
			connection.close();
		} catch (java.sql.SQLException ignore) {
		}		
	}

	public TimestampPkHistoryRepository(org.revenj.patterns.ServiceLocator locator) {
		this(locator.tryResolve(java.sql.Connection.class), locator.resolve(javax.sql.DataSource.class), locator.resolve(org.revenj.postgres.QueryProvider.class), locator.resolve(gen.model.issues.converters.TimestampPkConverter.class), locator);
	}
	
	@Override
	public java.util.List<org.revenj.patterns.History<gen.model.issues.TimestampPk>> find(String[] uris) {
		java.sql.Connection connection = getConnection();
		try (java.sql.PreparedStatement statement = connection.prepareStatement("SELECT r.uri, r.at, r.tuple, r.operation FROM \"issues\".\"-ngs_TimestampPk_history-\" r WHERE r.uri = ANY(?)")) {
			statement.setArray(1, connection.createArrayOf("text", uris));
			try (java.sql.ResultSet rs = statement.executeQuery();
				org.revenj.postgres.PostgresReader reader = org.revenj.postgres.PostgresReader.create(locator)) {
				java.util.Map<String, java.util.List<gen.model.issues.TimestampPk.Snapshot>> tmp = new java.util.HashMap<>();
				while (rs.next()) {
					String uri = rs.getString(1);
					java.time.OffsetDateTime _dt = java.time.OffsetDateTime.ofInstant(rs.getTimestamp(2).toInstant(), java.time.ZoneOffset.UTC);
					reader.process(rs.getString(3));
					String action = rs.getString(4);
					gen.model.issues.TimestampPk.Snapshot s = new gen.model.issues.TimestampPk.Snapshot(_dt, action, converter.from(reader));
					java.util.List<gen.model.issues.TimestampPk.Snapshot> snapshots = tmp.get(uri);
					if (snapshots == null) {
						snapshots = new java.util.ArrayList<>();
						tmp.put(uri, snapshots);
					}
					snapshots.add(s);					
				}
				java.util.List<org.revenj.patterns.History<gen.model.issues.TimestampPk>> result = new java.util.ArrayList<>();
				for(java.util.Map.Entry<String, java.util.List<gen.model.issues.TimestampPk.Snapshot>> it : tmp.entrySet()) {
					it.getValue().sort((a, b) -> a.getAt().compareTo(b.getAt()));
					result.add(new gen.model.issues.TimestampPk.History(it.getKey(), it.getValue()));
				}
				return result;
			}
		} catch (java.sql.SQLException | java.io.IOException e) {
			throw new RuntimeException(e);
		} finally {
			releaseConnection(connection);
		}
	}

	@Override
	public void close() throws java.io.IOException { 
	}

}
