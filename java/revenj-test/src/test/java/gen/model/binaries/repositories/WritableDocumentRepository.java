package gen.model.binaries.repositories;



public class WritableDocumentRepository   implements java.io.Closeable, org.revenj.patterns.SearchableRepository<gen.model.binaries.WritableDocument>, org.revenj.patterns.Repository<gen.model.binaries.WritableDocument>, org.revenj.patterns.PersistableRepository<gen.model.binaries.WritableDocument> {
	
	
	
	public WritableDocumentRepository(
			 final java.util.Optional<java.sql.Connection> transactionContext,
			 final javax.sql.DataSource dataSource,
			 final org.revenj.postgres.QueryProvider queryProvider,
			 final org.revenj.postgres.ObjectConverter<gen.model.binaries.WritableDocument> converter,
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
	private final org.revenj.postgres.ObjectConverter<gen.model.binaries.WritableDocument> converter;
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

	public WritableDocumentRepository(org.revenj.patterns.ServiceLocator locator) {
		this(locator.tryResolve(java.sql.Connection.class), locator.resolve(javax.sql.DataSource.class), locator.resolve(org.revenj.postgres.QueryProvider.class), locator.resolve(gen.model.binaries.converters.WritableDocumentConverter.class), locator);
	}
	
	@Override
	public org.revenj.patterns.Query<gen.model.binaries.WritableDocument> query(org.revenj.patterns.Specification<gen.model.binaries.WritableDocument> filter) {
		org.revenj.patterns.Query<gen.model.binaries.WritableDocument> query = queryProvider.query(transactionConnection, locator, gen.model.binaries.WritableDocument.class);
		if (filter == null) { }
		else query = query.filter(filter);
		
		return query;
	}

	private java.util.List<gen.model.binaries.WritableDocument> readFromDb(java.sql.PreparedStatement statement, java.util.List<gen.model.binaries.WritableDocument> result) throws java.sql.SQLException, java.io.IOException {
		try (java.sql.ResultSet rs = statement.executeQuery();
			org.revenj.postgres.PostgresReader reader = org.revenj.postgres.PostgresReader.create(locator)) {
			while (rs.next()) {
				reader.process(rs.getString(1));
				result.add(converter.from(reader));
			}
		}
		
		return result;
	}

	@Override
	public java.util.List<gen.model.binaries.WritableDocument> search(org.revenj.patterns.Specification<gen.model.binaries.WritableDocument> specification, Integer limit, Integer offset) {
		final String selectType = "SELECT it";
		java.util.function.Consumer<java.sql.PreparedStatement> applyFilters = ps -> {};
		java.sql.Connection connection = getConnection();
		try (org.revenj.postgres.PostgresWriter pgWriter = org.revenj.postgres.PostgresWriter.create()) {
			String sql;
			if (specification == null) {
				sql = "SELECT r FROM \"binaries\".\"Document\" r";
			} 
			else {
				org.revenj.patterns.Query<gen.model.binaries.WritableDocument> query = query(specification);
				if (offset != null) {
					query = query.skip(offset);
				}
				if (limit != null) {
					query = query.limit(limit);
				}
				try {
					return query.list();
				} catch (java.io.IOException e) {
					throw new RuntimeException(e);
				}
			}
			if (limit != null) {
				sql += " LIMIT " + Integer.toString(limit);
			}
			if (offset != null) {
				sql += " OFFSET " + Integer.toString(offset);
			}
			try (java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {
				applyFilters.accept(statement);
				return readFromDb(statement, new java.util.ArrayList<>());
			} catch (java.sql.SQLException | java.io.IOException e) {
				throw new RuntimeException(e);
			}
		} finally {
			releaseConnection(connection);
		}
	}

	@Override
	public long count(org.revenj.patterns.Specification<gen.model.binaries.WritableDocument> specification) {
		final String selectType = "SELECT COUNT(*)";
		java.util.function.Consumer<java.sql.PreparedStatement> applyFilters = ps -> {};
		java.sql.Connection connection = getConnection();
		try (org.revenj.postgres.PostgresWriter pgWriter = org.revenj.postgres.PostgresWriter.create()) {
			String sql;
			if (specification == null) {
				sql = "SELECT COUNT(*) FROM \"binaries\".\"Document\" r";
			} 
			else {
				try {
					return query(specification).count();
				} catch (java.io.IOException e) {
					throw new RuntimeException(e);
				}
			}
			try (java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {
				applyFilters.accept(statement);
				try (java.sql.ResultSet rs = statement.executeQuery()) {
					rs.next();
					return rs.getLong(1);
				}
			} catch (java.sql.SQLException e) {
				throw new RuntimeException(e);
			}
		} finally { 
			releaseConnection(connection); 
		}
	}

	@Override
	public boolean exists(org.revenj.patterns.Specification<gen.model.binaries.WritableDocument> specification) {
		final String selectType = "SELECT exists(SELECT *";
		java.util.function.Consumer<java.sql.PreparedStatement> applyFilters = ps -> {};
		java.sql.Connection connection = getConnection();
		try (org.revenj.postgres.PostgresWriter pgWriter = org.revenj.postgres.PostgresWriter.create()) {
			String sql = null;
			if (specification == null) {
				sql = "SELECT exists(SELECT * FROM \"binaries\".\"Document\" r";
			} 
			else {
				try {
					return query(specification).any();
				} catch (java.io.IOException e) {
					throw new RuntimeException(e);
				}
			}
			try (java.sql.PreparedStatement statement = connection.prepareStatement(sql + ")")) {
				applyFilters.accept(statement);
				try (java.sql.ResultSet rs = statement.executeQuery()) {
					rs.next();
					return rs.getBoolean(1);
				}
			} catch (java.sql.SQLException e) {
				throw new RuntimeException(e);
			}
		} finally { 
			releaseConnection(connection); 
		}
	}

	@Override
	public void close() throws java.io.IOException { 
	}

	
	@Override
	public java.util.List<gen.model.binaries.WritableDocument> find(String[] uris) {
		java.sql.Connection connection = getConnection();
		try (java.sql.Statement statement = connection.createStatement();
			org.revenj.postgres.PostgresReader reader = org.revenj.postgres.PostgresReader.create(locator)) {
			java.util.List<gen.model.binaries.WritableDocument> result = new java.util.ArrayList<>(uris.length);
			StringBuilder sb = new StringBuilder("SELECT _r FROM \"binaries\".\"Document\" _r WHERE _r.\"ID\" IN (");
			org.revenj.postgres.PostgresWriter.writeSimpleUriList(sb, uris);
			sb.append(")");
			try (java.sql.ResultSet rs = statement.executeQuery(sb.toString())) {
				while (rs.next()) {
					reader.process(rs.getString(1));
					result.add(converter.from(reader));
				}
			}
			
			return result;
		} catch (java.sql.SQLException | java.io.IOException e) {
			throw new RuntimeException(e);
		} finally { 
			releaseConnection(connection); 
		}
	}
	
	public static void __setupPersist(java.util.function.BiConsumer<java.util.Collection<gen.model.binaries.WritableDocument>, org.revenj.postgres.PostgresWriter> insert) {
		insertLoop = insert;
	}

	private static java.util.function.BiConsumer<java.util.Collection<gen.model.binaries.WritableDocument>, org.revenj.postgres.PostgresWriter> insertLoop;

	private static final String[] EMPTY_URI = new String[0];

	private static final org.postgresql.util.PGobject EMPTY_PGO = new org.postgresql.util.PGobject();
	static {
		EMPTY_PGO.setType("\"binaries\".\"Document\"[]");
		try { EMPTY_PGO.setValue("{}"); } catch (java.sql.SQLException ignore) {}
	}

	@Override
	public String[] persist(
			java.util.Collection<gen.model.binaries.WritableDocument> insert,
			java.util.Collection<java.util.Map.Entry<gen.model.binaries.WritableDocument, gen.model.binaries.WritableDocument>> update,
			java.util.Collection<gen.model.binaries.WritableDocument> delete) throws java.io.IOException {
		java.sql.Connection connection = getConnection();
		try (java.sql.PreparedStatement statement = connection.prepareStatement("WITH ins AS (INSERT INTO \"binaries\".\"Document\" SELECT * FROM unnest(?)), upd AS (UPDATE \"binaries\".\"Document\" AS _t SET \"ID\" = (_sq._new).\"ID\", \"name\" = (_sq._new).\"name\" FROM (SELECT unnest(?) as _old, unnest(?) as _new) _sq  WHERE _t.\"ID\" = (_sq._old).\"ID\") DELETE FROM \"binaries\".\"Document\" WHERE (\"ID\") IN (SELECT \"ID\" FROM unnest(?))");
			org.revenj.postgres.PostgresWriter sw = org.revenj.postgres.PostgresWriter.create()) {
			String[] result;
			if (insert != null && !insert.isEmpty()) {
				insertLoop.accept(insert, sw);
				sw.reset();
				org.revenj.postgres.converters.PostgresTuple tuple = org.revenj.postgres.converters.ArrayTuple.create(insert, converter::to);
				org.postgresql.util.PGobject pgo = new org.postgresql.util.PGobject();
				pgo.setType("\"binaries\".\"Document\"[]");
				sw.reset();
				tuple.buildTuple(sw, false);
				pgo.setValue(sw.toString());
				statement.setObject(1, pgo);
				result = new String[insert.size()];
				int i = 0;
				for (gen.model.binaries.WritableDocument it : insert) {
					result[i++] = it.getURI();
				}
			} else {
				statement.setObject(1, EMPTY_PGO);
				result = EMPTY_URI;
			}
			if (update != null && !update.isEmpty()) {
				java.util.List<gen.model.binaries.WritableDocument> oldUpdate = new java.util.ArrayList<>(update.size());
				java.util.List<gen.model.binaries.WritableDocument> newUpdate = new java.util.ArrayList<>(update.size());
				java.util.Map<String, Integer> missing = new java.util.HashMap<>();
				for (java.util.Map.Entry<gen.model.binaries.WritableDocument, gen.model.binaries.WritableDocument> it : update) {
					oldUpdate.add(it.getKey() != null ? it.getKey() : it.getValue());
					newUpdate.add(it.getValue());
				}
				org.revenj.postgres.converters.PostgresTuple tupleOld = org.revenj.postgres.converters.ArrayTuple.create(oldUpdate, converter::to);
				org.revenj.postgres.converters.PostgresTuple tupleNew = org.revenj.postgres.converters.ArrayTuple.create(newUpdate, converter::to);
				org.postgresql.util.PGobject pgOld = new org.postgresql.util.PGobject();
				org.postgresql.util.PGobject pgNew = new org.postgresql.util.PGobject();
				pgOld.setType("\"binaries\".\"Document\"[]");
				pgNew.setType("\"binaries\".\"Document\"[]");
				tupleOld.buildTuple(sw, false);
				pgOld.setValue(sw.toString());
				sw.reset();
				tupleNew.buildTuple(sw, false);
				pgNew.setValue(sw.toString());
				sw.reset();
				statement.setObject(2, pgOld);
				statement.setObject(3, pgNew);
			} else {
				statement.setObject(2, EMPTY_PGO);
				statement.setObject(3, EMPTY_PGO);
			}
			if (delete != null && !delete.isEmpty()) {
				org.revenj.postgres.converters.PostgresTuple tuple = org.revenj.postgres.converters.ArrayTuple.create(delete, converter::to);
				org.postgresql.util.PGobject pgo = new org.postgresql.util.PGobject();
				pgo.setType("\"binaries\".\"Document\"[]");
				tuple.buildTuple(sw, false);
				pgo.setValue(sw.toString());
				statement.setObject(4, pgo);
			} else {
				statement.setObject(4, EMPTY_PGO);
			}
			statement.executeUpdate();
			return result;
		} catch (java.sql.SQLException e) {
			throw new java.io.IOException(e);
		} finally { 
			releaseConnection(connection); 
		}
	}

}
