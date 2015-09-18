package gen.model.test.repositories;



public class CompositeListRepository   implements org.revenj.patterns.Repository<gen.model.test.CompositeList> {
	
	
	
	public CompositeListRepository(
			 final java.sql.Connection connection,
			 final org.revenj.postgres.QueryProvider queryProvider,
			 final org.revenj.postgres.ObjectConverter<gen.model.test.CompositeList> converter,
			 final org.revenj.patterns.ServiceLocator locator) {
			
		this.connection = connection;
		this.queryProvider = queryProvider;
		this.converter = converter;
		this.locator = locator;
	}

	private final java.sql.Connection connection;
	private final org.revenj.postgres.QueryProvider queryProvider;
	private final org.revenj.postgres.ObjectConverter<gen.model.test.CompositeList> converter;
	private final org.revenj.patterns.ServiceLocator locator;
	
	public CompositeListRepository(org.revenj.patterns.ServiceLocator locator) {
		this(locator.resolve(java.sql.Connection.class), locator.resolve(org.revenj.postgres.QueryProvider.class), new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.test.CompositeList>>(){}.resolve(locator), locator);
	}
	
	@Override
	public org.revenj.patterns.Query<gen.model.test.CompositeList> query(org.revenj.patterns.Specification<gen.model.test.CompositeList> filter) {
		org.revenj.patterns.Query<gen.model.test.CompositeList> query = queryProvider.query(connection, locator, gen.model.test.CompositeList.class);
		if (filter == null) return query;
		
		
		if (filter instanceof gen.model.test.CompositeList.ForSimple) {
			gen.model.test.CompositeList.ForSimple _spec_ = (gen.model.test.CompositeList.ForSimple)filter;
			java.util.List<gen.model.test.Simple> _spec_simples_ = _spec_.getSimples();
			return query.filter(it -> (_spec_simples_.contains(it.getSimple())));
		}		
		return query.filter(filter);
	}

	private java.util.ArrayList<gen.model.test.CompositeList> readFromDb(java.sql.PreparedStatement statement, java.util.ArrayList<gen.model.test.CompositeList> result) throws java.sql.SQLException, java.io.IOException {
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
	public java.util.List<gen.model.test.CompositeList> search(java.util.Optional<org.revenj.patterns.Specification<gen.model.test.CompositeList>> filter, java.util.Optional<Integer> limit, java.util.Optional<Integer> offset) {
		String sql = null;
		if (filter == null || filter.orElse(null) == null) {
			sql = "SELECT r FROM \"test\".\"CompositeList_snowflake\" r";
			if (limit != null && limit.orElse(null) != null) {
				sql += " LIMIT " + Integer.toString(limit.get());
			}
			if (offset != null && offset.orElse(null) != null) {
				sql += " OFFSET " + Integer.toString(offset.get());
			}
			try (java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {
				return readFromDb(statement, new java.util.ArrayList<>());
			} catch (java.sql.SQLException | java.io.IOException e) {
				throw new RuntimeException(e);
			}
		}
		final String selectType = "SELECT it";
		org.revenj.patterns.Specification<gen.model.test.CompositeList> specification = filter.get();
		java.util.function.Consumer<java.sql.PreparedStatement> applyFilters = ps -> {};
		try (org.revenj.postgres.PostgresWriter pgWriter = org.revenj.postgres.PostgresWriter.create()) {
			
		
		if (specification instanceof gen.model.test.CompositeList.ForSimple) {
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
			if (sql != null) {
				if (limit != null && limit.orElse(null) != null) {
					sql += " LIMIT " + Integer.toString(limit.get());
				}
				if (offset != null && offset.orElse(null) != null) {
					sql += " OFFSET " + Integer.toString(offset.get());
				}
				try (java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {
					applyFilters.accept(statement);
					return readFromDb(statement, new java.util.ArrayList<>());
				} catch (java.sql.SQLException | java.io.IOException e) {
					throw new RuntimeException(e);
				}
			}
			org.revenj.patterns.Query<gen.model.test.CompositeList> query = query(specification);
			if (offset != null && offset.orElse(null) != null) {
				query = query.skip(offset.get());
			}
			if (limit != null && limit.orElse(null) != null) {
				query = query.limit(limit.get());
			}
			try {
				return query.list();
			} catch (java.io.IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public long count(java.util.Optional<org.revenj.patterns.Specification<gen.model.test.CompositeList>> filter) {
		if (filter == null || filter.orElse(null) == null) {
			try (java.sql.PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM \"test\".\"CompositeList_snowflake\" r");
				java.sql.ResultSet rs = statement.executeQuery()) {
				rs.next();
				return rs.getLong(1);
			} catch (java.sql.SQLException e) {
				throw new RuntimeException(e);
			}
		}
		String sql = null;
		final String selectType = "SELECT COUNT(*)";
		org.revenj.patterns.Specification<gen.model.test.CompositeList> specification = filter.get();
		java.util.function.Consumer<java.sql.PreparedStatement> applyFilters = ps -> {};
		try (org.revenj.postgres.PostgresWriter pgWriter = org.revenj.postgres.PostgresWriter.create()) {
			
		
		if (specification instanceof gen.model.test.CompositeList.ForSimple) {
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
			if (sql != null) {
				try (java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {
					applyFilters.accept(statement);
					try (java.sql.ResultSet rs = statement.executeQuery()) {
						rs.next();
						return rs.getLong(1);
					}
				} catch (java.sql.SQLException e) {
					throw new RuntimeException(e);
				}
			}
			try {
				return query(specification).count();
			} catch (java.io.IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public boolean exists(java.util.Optional<org.revenj.patterns.Specification<gen.model.test.CompositeList>> filter) {
		if (filter == null || filter.orElse(null) == null) {
			try (java.sql.PreparedStatement statement = connection.prepareStatement("SELECT exists(SELECT * FROM \"test\".\"CompositeList_snowflake\" r)");
				java.sql.ResultSet rs = statement.executeQuery()) {
				rs.next();
				return rs.getBoolean(1);
			} catch (java.sql.SQLException e) {
				throw new RuntimeException(e);
			}
		}
		String sql = null;
		final String selectType = "SELECT exists(SELECT *";
		org.revenj.patterns.Specification<gen.model.test.CompositeList> specification = filter.get();
		java.util.function.Consumer<java.sql.PreparedStatement> applyFilters = ps -> {};
		try (org.revenj.postgres.PostgresWriter pgWriter = org.revenj.postgres.PostgresWriter.create()) {
			
		
		if (specification instanceof gen.model.test.CompositeList.ForSimple) {
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
			if (sql != null) {
				try (java.sql.PreparedStatement statement = connection.prepareStatement(sql + ")")) {
					applyFilters.accept(statement);
					try (java.sql.ResultSet rs = statement.executeQuery()) {
						rs.next();
						return rs.getBoolean(1);
					}
				} catch (java.sql.SQLException e) {
					throw new RuntimeException(e);
				}
			}
			try {
				return query(specification).any();
			} catch (java.io.IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	
	@Override
	public java.util.List<gen.model.test.CompositeList> find(String[] uris) {
		try (java.sql.PreparedStatement statement = connection.prepareStatement("SELECT r FROM \"test\".\"CompositeList_snowflake\" r WHERE r.\"URI\" = ANY(?)")) {
			statement.setArray(1, connection.createArrayOf("text", uris));
			return readFromDb(statement, new java.util.ArrayList<>(uris.length));			
		} catch (java.sql.SQLException | java.io.IOException e) {
			throw new RuntimeException(e);
		}
	}


}
