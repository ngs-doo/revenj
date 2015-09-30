package gen.model.test.repositories;



public class CompositeListRepository   implements java.io.Closeable, org.revenj.patterns.Repository<gen.model.test.CompositeList> {
	
	
	
	public CompositeListRepository(
			 final java.util.Optional<java.sql.Connection> transactionContext,
			 final javax.sql.DataSource dataSource,
			 final org.revenj.postgres.QueryProvider queryProvider,
			 final org.revenj.postgres.ObjectConverter<gen.model.test.CompositeList> converter,
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
	private final org.revenj.postgres.ObjectConverter<gen.model.test.CompositeList> converter;
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

	private static final org.revenj.patterns.Generic<java.util.Optional<java.sql.Connection>> genericOptionalConnection = 
		new org.revenj.patterns.Generic<java.util.Optional<java.sql.Connection>>(){};

	public CompositeListRepository(org.revenj.patterns.ServiceLocator locator) {
		this(genericOptionalConnection.resolve(locator), locator.resolve(javax.sql.DataSource.class), locator.resolve(org.revenj.postgres.QueryProvider.class), locator.resolve(gen.model.test.converters.CompositeListConverter.class), locator);
	}
	
	@Override
	public org.revenj.patterns.Query<gen.model.test.CompositeList> query(org.revenj.patterns.Specification<gen.model.test.CompositeList> filter) {
		org.revenj.patterns.Query<gen.model.test.CompositeList> query = queryProvider.query(transactionConnection, locator, gen.model.test.CompositeList.class);
		if (filter == null) { }
		else if (filter instanceof gen.model.test.CompositeList.ForSimple) {
			gen.model.test.CompositeList.ForSimple _spec_ = (gen.model.test.CompositeList.ForSimple)filter;
			java.util.List<gen.model.test.Simple> _spec_simples_ = _spec_.getSimples();
			query = query.filter(it -> (_spec_simples_.contains(it.getSimple())));
		}
		else query = query.filter(filter);
		
		return query;
	}

	private java.util.List<gen.model.test.CompositeList> readFromDb(java.sql.PreparedStatement statement, java.util.List<gen.model.test.CompositeList> result) throws java.sql.SQLException, java.io.IOException {
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
	public java.util.List<gen.model.test.CompositeList> search(org.revenj.patterns.Specification<gen.model.test.CompositeList> specification, Integer limit, Integer offset) {
		final String selectType = "SELECT it";
		java.util.function.Consumer<java.sql.PreparedStatement> applyFilters = ps -> {};
		java.sql.Connection connection = getConnection();
		try (org.revenj.postgres.PostgresWriter pgWriter = org.revenj.postgres.PostgresWriter.create()) {
			String sql;
			if (specification == null) {
				sql = "SELECT r FROM \"test\".\"CompositeList_snowflake\" r";
			} 
			else if (specification instanceof gen.model.test.CompositeList.ForSimple) {
				gen.model.test.CompositeList.ForSimple spec = (gen.model.test.CompositeList.ForSimple)specification;
				sql = selectType + " FROM \"test\".\"CompositeList.ForSimple\"(?) it";
				
				applyFilters = applyFilters.andThen(ps -> {
					try {
						
				Object[] __arr = new Object[spec.getSimples().size()];
				if (__arr.length > 0) {
					gen.model.test.converters.SimpleConverter __converter = locator.resolve(gen.model.test.converters.SimpleConverter.class);
					int __ind = 0;
					for (gen.model.test.Simple __it : spec.getSimples()) {
						org.postgresql.util.PGobject __pgo = new org.postgresql.util.PGobject();
						__pgo.setType("\"test\".\"Simple\"");
						pgWriter.reset();
						__converter.to(__it).buildTuple(pgWriter, false);
						__pgo.setValue(pgWriter.toString());
						__arr[__ind++] = __pgo;
					}
				}
				ps.setArray(1, connection.createArrayOf("\"test\".\"Simple\"", __arr));
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
			}
			else {
				org.revenj.patterns.Query<gen.model.test.CompositeList> query = query(specification);
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
	public long count(org.revenj.patterns.Specification<gen.model.test.CompositeList> specification) {
		final String selectType = "SELECT COUNT(*)";
		java.util.function.Consumer<java.sql.PreparedStatement> applyFilters = ps -> {};
		java.sql.Connection connection = getConnection();
		try (org.revenj.postgres.PostgresWriter pgWriter = org.revenj.postgres.PostgresWriter.create()) {
			String sql;
			if (specification == null) {
				sql = "SELECT COUNT(*) FROM \"test\".\"CompositeList_snowflake\" r";
			} 
			else if (specification instanceof gen.model.test.CompositeList.ForSimple) {
				gen.model.test.CompositeList.ForSimple spec = (gen.model.test.CompositeList.ForSimple)specification;
				sql = selectType + " FROM \"test\".\"CompositeList.ForSimple\"(?) it";
				
				applyFilters = applyFilters.andThen(ps -> {
					try {
						
				Object[] __arr = new Object[spec.getSimples().size()];
				if (__arr.length > 0) {
					gen.model.test.converters.SimpleConverter __converter = locator.resolve(gen.model.test.converters.SimpleConverter.class);
					int __ind = 0;
					for (gen.model.test.Simple __it : spec.getSimples()) {
						org.postgresql.util.PGobject __pgo = new org.postgresql.util.PGobject();
						__pgo.setType("\"test\".\"Simple\"");
						pgWriter.reset();
						__converter.to(__it).buildTuple(pgWriter, false);
						__pgo.setValue(pgWriter.toString());
						__arr[__ind++] = __pgo;
					}
				}
				ps.setArray(1, connection.createArrayOf("\"test\".\"Simple\"", __arr));
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
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
	public boolean exists(org.revenj.patterns.Specification<gen.model.test.CompositeList> specification) {
		final String selectType = "SELECT exists(SELECT *";
		java.util.function.Consumer<java.sql.PreparedStatement> applyFilters = ps -> {};
		java.sql.Connection connection = getConnection();
		try (org.revenj.postgres.PostgresWriter pgWriter = org.revenj.postgres.PostgresWriter.create()) {
			String sql = null;
			if (specification == null) {
				sql = "SELECT exists(SELECT * FROM \"test\".\"CompositeList_snowflake\" r";
			} 
			else if (specification instanceof gen.model.test.CompositeList.ForSimple) {
				gen.model.test.CompositeList.ForSimple spec = (gen.model.test.CompositeList.ForSimple)specification;
				sql = selectType + " FROM \"test\".\"CompositeList.ForSimple\"(?) it";
				
				applyFilters = applyFilters.andThen(ps -> {
					try {
						
				Object[] __arr = new Object[spec.getSimples().size()];
				if (__arr.length > 0) {
					gen.model.test.converters.SimpleConverter __converter = locator.resolve(gen.model.test.converters.SimpleConverter.class);
					int __ind = 0;
					for (gen.model.test.Simple __it : spec.getSimples()) {
						org.postgresql.util.PGobject __pgo = new org.postgresql.util.PGobject();
						__pgo.setType("\"test\".\"Simple\"");
						pgWriter.reset();
						__converter.to(__it).buildTuple(pgWriter, false);
						__pgo.setValue(pgWriter.toString());
						__arr[__ind++] = __pgo;
					}
				}
				ps.setArray(1, connection.createArrayOf("\"test\".\"Simple\"", __arr));
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
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
	public java.util.List<gen.model.test.CompositeList> find(String[] uris) {
		java.sql.Connection connection = getConnection();
		try (java.sql.PreparedStatement statement = connection.prepareStatement("SELECT r FROM \"test\".\"CompositeList_snowflake\" r WHERE r.\"URI\" = ANY(?)")) {
			statement.setArray(1, connection.createArrayOf("text", uris));
			return readFromDb(statement, new java.util.ArrayList<>(uris.length));			
		} catch (java.sql.SQLException | java.io.IOException e) {
			throw new RuntimeException(e);
		} finally {
			releaseConnection(connection);
		}
	}


}
