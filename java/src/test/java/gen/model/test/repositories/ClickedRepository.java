package gen.model.test.repositories;



public class ClickedRepository   implements org.revenj.patterns.DomainEventStore<gen.model.test.Clicked> {
	
	
	
	public ClickedRepository(
			 final java.sql.Connection connection,
			 final org.revenj.postgres.ObjectConverter<gen.model.test.Clicked> converter,
			 final org.revenj.patterns.ServiceLocator locator) {
			
		
			this.connection = connection;
		
			this.converter = converter;
		
			this.locator = locator;
	}

	private final java.sql.Connection connection;
	private final org.revenj.postgres.ObjectConverter<gen.model.test.Clicked> converter;
	private final org.revenj.patterns.ServiceLocator locator;
	
	public ClickedRepository(org.revenj.patterns.ServiceLocator locator) {
		this(locator.resolve(java.sql.Connection.class), new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.test.Clicked>>(){}.resolve(locator), locator);
	}
	
	@Override
	public java.util.List<gen.model.test.Clicked> find(String[] uris) {
		try (java.sql.PreparedStatement statement = connection.prepareStatement("SELECT r FROM \"test\".\"Clicked_event\" r WHERE r._event_id = ANY(?)")) {
			Object[] ids = new Object[uris.length];
			for(int i = 0; i < uris.length; i++) {
				ids[i] = Long.parseLong(uris[i]);
			}
			statement.setArray(1, connection.createArrayOf("bigint", ids));
			java.util.ArrayList<gen.model.test.Clicked> result = new java.util.ArrayList<>(uris.length);
			org.revenj.postgres.PostgresReader reader = new org.revenj.postgres.PostgresReader(locator::resolve);
			try (java.sql.ResultSet rs = statement.executeQuery()) {
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
	public String[] submit(java.util.List<gen.model.test.Clicked> domainEvents) {
		try (java.sql.PreparedStatement statement = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT \"URI\" FROM \"test\".\"submit_Clicked\"(?)")) {
			String[] result = new String[domainEvents.size()];
			org.revenj.postgres.PostgresWriter sw = new org.revenj.postgres.PostgresWriter();
			org.revenj.postgres.converters.PostgresTuple tuple = org.revenj.postgres.converters.ArrayTuple.create(domainEvents, converter);
			org.postgresql.util.PGobject pgo = new org.postgresql.util.PGobject();
			pgo.setType("\"test\".\"Clicked_event\"[]");
			tuple.buildTuple(sw, false);
			pgo.setValue(sw.toString());
			statement.setObject(1, pgo);
			try (java.sql.ResultSet rs = statement.executeQuery()) {
				for (int i = 0; i < result.length; i++) {
					rs.next();
					result[i] = rs.getString(1);
				}
			}
			return result;
		} catch (java.sql.SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void mark(String[] uris) {
		try (java.sql.PreparedStatement statement = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT \"test\".\"mark_Clicked\"(?)")) {
			Object[] ids = new Object[uris.length];
			for(int i = 0; i < uris.length; i++) {
				ids[i] = Long.parseLong(uris[i]);
			}
			statement.setArray(1, connection.createArrayOf("bigint", ids));
		} catch (java.sql.SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
