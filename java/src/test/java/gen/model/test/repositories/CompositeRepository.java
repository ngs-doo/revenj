package gen.model.test.repositories;



public class CompositeRepository   implements org.revenj.patterns.Repository<gen.model.test.Composite>, org.revenj.patterns.PersistableRepository<gen.model.test.Composite> {
	
	
	
	public CompositeRepository(
			 final java.sql.Connection connection,
			 final org.revenj.postgres.ObjectConverter<gen.model.test.Composite> converter,
			 final org.revenj.patterns.ServiceLocator locator) {
			
		
			this.connection = connection;
		
			this.converter = converter;
		
			this.locator = locator;
	}

	private final java.sql.Connection connection;
	private final org.revenj.postgres.ObjectConverter<gen.model.test.Composite> converter;
	private final org.revenj.patterns.ServiceLocator locator;
	
	public CompositeRepository(org.revenj.patterns.ServiceLocator locator) {
		this(locator.resolve(java.sql.Connection.class), new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.test.Composite>>(){}.resolve(locator), locator);
	}
	
	//@Override
	private java.util.stream.Stream<gen.model.test.Composite> stream(java.util.Optional<org.revenj.patterns.Specification<gen.model.test.Composite>> filter) {
		throw new UnsupportedOperationException();
	}

	private java.util.ArrayList<gen.model.test.Composite> readFromDb(java.sql.PreparedStatement statement, java.util.ArrayList<gen.model.test.Composite> result) throws java.sql.SQLException, java.io.IOException {
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
	public java.util.List<gen.model.test.Composite> search(java.util.Optional<org.revenj.patterns.Specification<gen.model.test.Composite>> filter, java.util.Optional<Integer> limit, java.util.Optional<Integer> offset) {
		String sql = null;
		if (filter == null || filter.orElse(null) == null) {
			sql = "SELECT r FROM \"test\".\"Composite_entity\" r";
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
		org.revenj.patterns.Specification<gen.model.test.Composite> specification = filter.get();
		java.util.function.Consumer<java.sql.PreparedStatement> applyFilters = ps -> {};
		try (org.revenj.postgres.PostgresWriter pgWriter = org.revenj.postgres.PostgresWriter.create()) {
			
		
		if (specification instanceof gen.model.test.Composite.ForSimple) {
			gen.model.test.Composite.ForSimple spec = (gen.model.test.Composite.ForSimple)specification;
			sql = "SELECT it FROM \"test\".\"Composite.ForSimple\"(?) it";
			
			applyFilters = applyFilters.andThen(ps -> {
				try {
					
				gen.model.test.converters.SimpleConverter __converter = locator.resolve(gen.model.test.converters.SimpleConverter.class);
				org.postgresql.util.PGobject __pgo = new org.postgresql.util.PGobject();
				__pgo.setType("\"test\".\"Simple\"");
				pgWriter.reset();
				__converter.to(spec.getSimple()).buildTuple(pgWriter, false);
				__pgo.setValue(pgWriter.toString());
				ps.setObject(1, __pgo);
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
			java.util.stream.Stream<gen.model.test.Composite> stream = stream(filter);
			if (offset != null && offset.orElse(null) != null) {
				stream = stream.skip(offset.get());
			}
			if (limit != null && limit.orElse(null) != null) {
				stream = stream.limit(limit.get());
			}
			return stream.collect(java.util.stream.Collectors.toList());
		}
	}

	
	@Override
	public java.util.List<gen.model.test.Composite> find(String[] uris) {
		try (java.sql.Statement statement = connection.createStatement()) {
			java.util.ArrayList<gen.model.test.Composite> result = new java.util.ArrayList<>(uris.length);
			org.revenj.postgres.PostgresReader reader = new org.revenj.postgres.PostgresReader(locator);
			StringBuilder sb = new StringBuilder("SELECT r FROM \"test\".\"Composite_entity\" r WHERE r.\"id\" IN (");
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
			java.util.List<gen.model.test.Composite> insert,
			java.util.List<java.util.Map.Entry<gen.model.test.Composite, gen.model.test.Composite>> update,
			java.util.List<gen.model.test.Composite> delete) throws java.sql.SQLException {
		try (java.sql.PreparedStatement statement = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT * FROM \"test\".\"persist_Composite\"(?, ?, ?, ?)");
			org.revenj.postgres.PostgresWriter sw = org.revenj.postgres.PostgresWriter.create()) {
			java.util.List<String> result;
			if (insert != null && !insert.isEmpty()) {
				result = new java.util.ArrayList<>(insert.size());
				org.revenj.postgres.converters.PostgresTuple tuple = org.revenj.postgres.converters.ArrayTuple.create(insert, converter::to);
				org.postgresql.util.PGobject pgo = new org.postgresql.util.PGobject();
				pgo.setType("\"test\".\"Composite_entity\"[]");
				tuple.buildTuple(sw, false);
				pgo.setValue(sw.toString());
				sw.reset();
				statement.setObject(1, pgo);
				for (gen.model.test.Composite it : insert) {
					String uri = gen.model.test.converters.CompositeConverter.buildURI(sw.tmp, it.getId());
					result.add(uri);
				}
			} else {
				statement.setArray(1, null);
				result = new java.util.ArrayList<>(0);
			}
			if (update != null && !update.isEmpty()) {
				java.util.List<gen.model.test.Composite> oldUpdate = new java.util.ArrayList<>(update.size());
				java.util.List<gen.model.test.Composite> newUpdate = new java.util.ArrayList<>(update.size());
				java.util.Map<String, Integer> missing = new java.util.HashMap<>();
				int cnt = 0;
				for (java.util.Map.Entry<gen.model.test.Composite, gen.model.test.Composite> it : update) {
					oldUpdate.add(it.getKey());
					if (it.getKey() == null) {
						missing.put(it.getValue().getURI(), cnt);
					}
					newUpdate.add(it.getValue());
					cnt++;
				}
				if (!missing.isEmpty()) {
					java.util.List<gen.model.test.Composite> found = find(missing.keySet().toArray(new String[missing.size()]));
					for (gen.model.test.Composite it : found) {
						oldUpdate.set(missing.get(it.getURI()), it);
					}
				}
				org.revenj.postgres.converters.PostgresTuple tupleOld = org.revenj.postgres.converters.ArrayTuple.create(oldUpdate, converter::to);
				org.revenj.postgres.converters.PostgresTuple tupleNew = org.revenj.postgres.converters.ArrayTuple.create(newUpdate, converter::to);
				org.postgresql.util.PGobject pgOld = new org.postgresql.util.PGobject();
				org.postgresql.util.PGobject pgNew = new org.postgresql.util.PGobject();
				pgOld.setType("\"test\".\"Composite_entity\"[]");
				pgNew.setType("\"test\".\"Composite_entity\"[]");
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
				pgo.setType("\"test\".\"Composite_entity\"[]");
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

}
