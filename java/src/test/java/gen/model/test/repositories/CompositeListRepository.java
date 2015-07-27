package gen.model.test.repositories;



public class CompositeListRepository   implements org.revenj.patterns.Repository<gen.model.test.CompositeList> {
	
	
	
	public CompositeListRepository(
			 final java.sql.Connection connection,
			 final org.revenj.postgres.ObjectConverter<gen.model.test.CompositeList> converter,
			 final org.revenj.patterns.ServiceLocator locator) {
			
		
			this.connection = connection;
		
			this.converter = converter;
		
			this.locator = locator;
	}

	private final java.sql.Connection connection;
	private final org.revenj.postgres.ObjectConverter<gen.model.test.CompositeList> converter;
	private final org.revenj.patterns.ServiceLocator locator;
	
	public CompositeListRepository(org.revenj.patterns.ServiceLocator locator) {
		this(locator.resolve(java.sql.Connection.class), new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.test.CompositeList>>(){}.resolve(locator), locator);
	}
	
	//@Override
	private java.util.stream.Stream<gen.model.test.CompositeList> stream(java.util.Optional<org.revenj.patterns.Specification<gen.model.test.CompositeList>> filter) {
		throw new UnsupportedOperationException();
	}

	private java.util.ArrayList<gen.model.test.CompositeList> readFromDb(java.sql.PreparedStatement statement, java.util.ArrayList<gen.model.test.CompositeList> result) throws java.sql.SQLException, java.io.IOException {
		org.revenj.postgres.PostgresReader reader = new org.revenj.postgres.PostgresReader(locator::resolve);
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
	public java.util.List<gen.model.test.CompositeList> search(java.util.Optional<org.revenj.patterns.Specification<gen.model.test.CompositeList>> filter, java.util.Optional<Integer> limit, java.util.Optional<Integer> offset) {
		String sql = null;
		if (filter == null || !filter.isPresent() || filter.get() == null) {
			sql = "SELECT r FROM \"test\".\"CompositeList_snowflake\" r";
			if (limit != null && limit.isPresent() && limit.get() != null) {
				sql += " LIMIT " + Integer.toString(limit.get());
			}
			if (offset != null && offset.isPresent() && offset.get() != null) {
				sql += " OFFSET " + Integer.toString(offset.get());
			}
			try (java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {
				return readFromDb(statement, new java.util.ArrayList<>());
			} catch (java.sql.SQLException | java.io.IOException e) {
				throw new RuntimeException(e);
			}
		}
		org.revenj.patterns.Specification<gen.model.test.CompositeList> specification = filter.orElse(null);
		java.util.function.Consumer<java.sql.PreparedStatement> applyFilters = ps -> {};
		try (org.revenj.postgres.PostgresWriter pgWriter = org.revenj.postgres.PostgresWriter.create()) {
			
			if (sql != null) {
				if (limit != null && limit.isPresent() && limit.get() != null) {
					sql += " LIMIT " + Integer.toString(limit.get());
				}
				if (offset != null && offset.isPresent() && offset.get() != null) {
					sql += " OFFSET " + Integer.toString(offset.get());
				}
				try (java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {
					applyFilters.accept(statement);
					return readFromDb(statement, new java.util.ArrayList<>());
				} catch (java.sql.SQLException | java.io.IOException e) {
					throw new RuntimeException(e);
				}
			}
			java.util.stream.Stream<gen.model.test.CompositeList> stream = stream(filter);
			if (offset != null && offset.isPresent() && offset.get() != null) {
				stream = stream.skip(offset.get());
			}
			if (limit != null && limit.isPresent() && limit.get() != null) {
				stream = stream.limit(limit.get());
			}
			return stream.collect(java.util.stream.Collectors.toList());
		}
	}

	
	@Override
	public java.util.List<gen.model.test.CompositeList> find(String[] uris) {
		try (java.sql.PreparedStatement statement = connection.prepareStatement("SELECT r FROM \"test\".\"CompositeList_snowflake\" r WHERE r.URI = ANY(?)")) {
			statement.setArray(1, connection.createArrayOf("text", uris));
			return readFromDb(statement, new java.util.ArrayList<>(uris.length));			
		} catch (java.sql.SQLException | java.io.IOException e) {
			throw new RuntimeException(e);
		}
	}


}
