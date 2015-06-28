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
		this(locator.resolve(java.sql.Connection.class), new org.revenj.patterns.GenericType<org.revenj.postgres.ObjectConverter<gen.model.test.Composite>>(){}.resolve(locator), locator);
	}
	
	@Override
	public java.util.List<gen.model.test.Composite> find(String[] uris) {
		try (java.sql.Statement statement = connection.createStatement()) {
			java.util.ArrayList<gen.model.test.Composite> result = new java.util.ArrayList<>(uris.length);
			org.revenj.postgres.PostgresReader reader = new org.revenj.postgres.PostgresReader(locator::resolve);
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
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public java.util.List<String> persist(
			java.util.List<gen.model.test.Composite> insert,
			java.util.List<java.util.Map.Entry<gen.model.test.Composite, gen.model.test.Composite>> update,
			java.util.List<gen.model.test.Composite> delete) throws java.sql.SQLException {
		try (java.sql.PreparedStatement statement = connection.prepareStatement("SELECT * FROM \"test\".\"persist_Composite\"(?, ?, ?, ?)")) {
			java.util.List<String> result;
			org.revenj.postgres.PostgresWriter sw = new org.revenj.postgres.PostgresWriter();
			if (insert != null && !insert.isEmpty()) {
				result = new java.util.ArrayList<>(insert.size());
				org.revenj.postgres.converters.PostgresTuple tuple = org.revenj.postgres.converters.ArrayTuple.create(insert, converter);
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
				for (java.util.Map.Entry<gen.model.test.Composite, gen.model.test.Composite> it : update) {
					oldUpdate.add(it.getKey());
					newUpdate.add(it.getValue());
				}
				org.revenj.postgres.converters.PostgresTuple tupleOld = org.revenj.postgres.converters.ArrayTuple.create(oldUpdate, converter);
				org.revenj.postgres.converters.PostgresTuple tupleNew = org.revenj.postgres.converters.ArrayTuple.create(newUpdate, converter);
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
				org.revenj.postgres.converters.PostgresTuple tuple = org.revenj.postgres.converters.ArrayTuple.create(delete, converter);
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
