package gen.model.test.repositories;



public class SingleDetailRepository   implements org.revenj.patterns.Repository<gen.model.test.SingleDetail>, org.revenj.patterns.PersistableRepository<gen.model.test.SingleDetail> {
	
	
	
	public SingleDetailRepository(
			 final java.sql.Connection connection,
			 final org.revenj.postgres.QueryProvider queryProvider,
			 final org.revenj.postgres.ObjectConverter<gen.model.test.SingleDetail> converter,
			 final org.revenj.patterns.ServiceLocator locator) {
			
		this.connection = connection;
		this.queryProvider = queryProvider;
		this.converter = converter;
		this.locator = locator;
	}

	private final java.sql.Connection connection;
	private final org.revenj.postgres.QueryProvider queryProvider;
	private final org.revenj.postgres.ObjectConverter<gen.model.test.SingleDetail> converter;
	private final org.revenj.patterns.ServiceLocator locator;
	
	public SingleDetailRepository(org.revenj.patterns.ServiceLocator locator) {
		this(locator.resolve(java.sql.Connection.class), locator.resolve(org.revenj.postgres.QueryProvider.class), new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.test.SingleDetail>>(){}.resolve(locator), locator);
	}
	
	@Override
	public org.revenj.patterns.Query<gen.model.test.SingleDetail> query() {
		return queryProvider.query(connection, locator, gen.model.test.SingleDetail.class);
	}

	private java.util.ArrayList<gen.model.test.SingleDetail> readFromDb(java.sql.PreparedStatement statement, java.util.ArrayList<gen.model.test.SingleDetail> result) throws java.sql.SQLException, java.io.IOException {
		org.revenj.postgres.PostgresReader reader = new org.revenj.postgres.PostgresReader(locator);
		try (java.sql.ResultSet rs = statement.executeQuery()) {
			while (rs.next()) {
				org.postgresql.util.PGobject pgo = (org.postgresql.util.PGobject) rs.getObject(1);
				reader.process(pgo.getValue());
				result.add(converter.from(reader));
			}
		}
		return result;
	}

	@Override
	public java.util.List<gen.model.test.SingleDetail> search(java.util.Optional<org.revenj.patterns.Specification<gen.model.test.SingleDetail>> filter, java.util.Optional<Integer> limit, java.util.Optional<Integer> offset) {
		String sql = null;
		if (filter == null || filter.orElse(null) == null) {
			sql = "SELECT r FROM \"test\".\"SingleDetail_entity\" r";
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
		org.revenj.patterns.Specification<gen.model.test.SingleDetail> specification = filter.get();
		java.util.function.Consumer<java.sql.PreparedStatement> applyFilters = ps -> {};
		try (org.revenj.postgres.PostgresWriter pgWriter = org.revenj.postgres.PostgresWriter.create()) {
			
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
			org.revenj.patterns.Query<gen.model.test.SingleDetail> query = query().filter(specification::test);
			if (offset != null && offset.orElse(null) != null) {
				query = query.skip(offset.get());
			}
			if (limit != null && limit.orElse(null) != null) {
				query = query.limit(limit.get());
			}
			try {
				return query.list();
			} catch (java.sql.SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	
	@Override
	public java.util.List<gen.model.test.SingleDetail> find(String[] uris) {
		try (java.sql.Statement statement = connection.createStatement()) {
			java.util.ArrayList<gen.model.test.SingleDetail> result = new java.util.ArrayList<>(uris.length);
			org.revenj.postgres.PostgresReader reader = new org.revenj.postgres.PostgresReader(locator);
			StringBuilder sb = new StringBuilder("SELECT r FROM \"test\".\"SingleDetail_entity\" r WHERE r.\"ID\" IN (");
			org.revenj.postgres.PostgresWriter.writeSimpleUriList(sb, uris);
			sb.append(")");
			try (java.sql.ResultSet rs = statement.executeQuery(sb.toString())) {
				while (rs.next()) {
					org.postgresql.util.PGobject pgo = (org.postgresql.util.PGobject) rs.getObject(1);
					reader.process(pgo.getValue());
					result.add(converter.from(reader));
				}
			}
			return result;
		} catch (java.sql.SQLException | java.io.IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public java.util.List<String> persist(
			java.util.List<gen.model.test.SingleDetail> insert,
			java.util.List<java.util.Map.Entry<gen.model.test.SingleDetail, gen.model.test.SingleDetail>> update,
			java.util.List<gen.model.test.SingleDetail> delete) throws java.sql.SQLException {
		try (java.sql.PreparedStatement statement = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT * FROM \"test\".\"persist_SingleDetail\"(?, ?, ?, ?)");
			org.revenj.postgres.PostgresWriter sw = org.revenj.postgres.PostgresWriter.create()) {
			java.util.List<String> result;
			if (insert != null && !insert.isEmpty()) {
		
				if (assignSequenceID == null) throw new RuntimeException("SingleDetail repository has not been properly set up. Static __setupSequenceID method not called");
				assignSequenceID.accept(insert, connection);
				result = new java.util.ArrayList<>(insert.size());
				org.revenj.postgres.converters.PostgresTuple tuple = org.revenj.postgres.converters.ArrayTuple.create(insert, converter::to);
				org.postgresql.util.PGobject pgo = new org.postgresql.util.PGobject();
				pgo.setType("\"test\".\"SingleDetail_entity\"[]");
				tuple.buildTuple(sw, false);
				pgo.setValue(sw.toString());
				sw.reset();
				statement.setObject(1, pgo);
				for (gen.model.test.SingleDetail it : insert) {
					String uri = gen.model.test.converters.SingleDetailConverter.buildURI(sw.tmp, it.getID());
					result.add(uri);
				}
			} else {
				statement.setArray(1, null);
				result = new java.util.ArrayList<>(0);
			}
			if (update != null && !update.isEmpty()) {
				java.util.List<gen.model.test.SingleDetail> oldUpdate = new java.util.ArrayList<>(update.size());
				java.util.List<gen.model.test.SingleDetail> newUpdate = new java.util.ArrayList<>(update.size());
				java.util.Map<String, Integer> missing = new java.util.HashMap<>();
				int cnt = 0;
				for (java.util.Map.Entry<gen.model.test.SingleDetail, gen.model.test.SingleDetail> it : update) {
					oldUpdate.add(it.getKey());
					if (it.getKey() == null) {
						missing.put(it.getValue().getURI(), cnt);
					}
					newUpdate.add(it.getValue());
					cnt++;
				}
				if (!missing.isEmpty()) {
					java.util.List<gen.model.test.SingleDetail> found = find(missing.keySet().toArray(new String[missing.size()]));
					for (gen.model.test.SingleDetail it : found) {
						oldUpdate.set(missing.get(it.getURI()), it);
					}
				}
				org.revenj.postgres.converters.PostgresTuple tupleOld = org.revenj.postgres.converters.ArrayTuple.create(oldUpdate, converter::to);
				org.revenj.postgres.converters.PostgresTuple tupleNew = org.revenj.postgres.converters.ArrayTuple.create(newUpdate, converter::to);
				org.postgresql.util.PGobject pgOld = new org.postgresql.util.PGobject();
				org.postgresql.util.PGobject pgNew = new org.postgresql.util.PGobject();
				pgOld.setType("\"test\".\"SingleDetail_entity\"[]");
				pgNew.setType("\"test\".\"SingleDetail_entity\"[]");
				tupleOld.buildTuple(sw, false);
				pgOld.setValue(sw.toString());
				sw.reset();
				tupleNew.buildTuple(sw, false);
				pgNew.setValue(sw.toString());
				sw.reset();
				statement.setObject(2, pgOld);
				statement.setObject(3, pgNew);
			} else {
				statement.setArray(2, null);
				statement.setArray(3, null);
			}
			if (delete != null && !delete.isEmpty()) {
				org.revenj.postgres.converters.PostgresTuple tuple = org.revenj.postgres.converters.ArrayTuple.create(delete, converter::to);
				org.postgresql.util.PGobject pgo = new org.postgresql.util.PGobject();
				pgo.setType("\"test\".\"SingleDetail_entity\"[]");
				tuple.buildTuple(sw, false);
				pgo.setValue(sw.toString());
				statement.setObject(4, pgo);
			} else {
				statement.setArray(4, null);
			}
			try (java.sql.ResultSet rs = statement.executeQuery()) {
				rs.next();
				String message = rs.getString(1);
				if (message != null) throw new java.sql.SQLException(message);
			}
			return result;
		} catch (java.io.IOException e) {
			throw new java.sql.SQLException(e);
		}
	}

	
	public static void __setupSequenceID(java.util.function.BiConsumer<java.util.List<gen.model.test.SingleDetail>, java.sql.Connection> sequence) {
		assignSequenceID = sequence;
	}

	private static java.util.function.BiConsumer<java.util.List<gen.model.test.SingleDetail>, java.sql.Connection> assignSequenceID;
}
