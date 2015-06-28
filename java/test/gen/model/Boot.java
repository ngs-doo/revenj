package gen.model;


public class Boot implements org.revenj.Revenj.SystemAspect {

	public static org.revenj.patterns.Container start(String jdbcUrl) throws java.io.IOException {{
		java.util.Properties properties = new java.util.Properties();
		java.io.File revProps = new java.io.File("revenj.properties");
		if (revProps.exists() && revProps.isFile()) {{
			properties.load(new java.io.FileReader(revProps));
		}}
		return start(jdbcUrl, properties);
	}}

	public static org.revenj.patterns.Container start(String jdbcUrl, java.util.Properties properties) throws java.io.IOException {{
		org.revenj.patterns.Container.Factory<java.sql.Connection> factory = c -> {{
			try {{
				return java.sql.DriverManager.getConnection(jdbcUrl, properties);
			}} catch (java.sql.SQLException ignore) {{
				return null;
			}}
		}};
		return org.revenj.Revenj.setup(factory, properties, java.util.Collections.singletonList((org.revenj.Revenj.SystemAspect) new Boot()).iterator());
	}}

	public void configure(org.revenj.patterns.Container container) throws java.io.IOException {
		java.util.List<org.revenj.postgres.ObjectConverter.ColumnInfo> columns = new java.util.ArrayList<>();
		try (java.sql.Connection connection = container.resolve(java.sql.Connection.class);
				java.sql.PreparedStatement statement = connection.prepareStatement("SELECT * FROM \"-NGS-\".load_type_info()");
				java.sql.ResultSet rs = statement.executeQuery()) {
			while (rs.next()) {
				columns.add(
						new org.revenj.postgres.ObjectConverter.ColumnInfo(
								rs.getString(1),
								rs.getString(2),
								rs.getString(3),
								rs.getString(4),
								rs.getString(5),
								rs.getShort(6),
								rs.getBoolean(7),
								rs.getBoolean(8)
						)
				);

			}
		} catch (java.sql.SQLException e) {
			throw new java.io.IOException(e);
		}
		container.registerInstance(org.revenj.patterns.ServiceLocator.class, container, false);
		
		
		gen.model.test.converters.SimpleConverter test$converter$SimpleConverter = new gen.model.test.converters.SimpleConverter(columns);
		container.register(test$converter$SimpleConverter);
		container.registerInstance(new org.revenj.patterns.GenericType<org.revenj.postgres.ObjectConverter<gen.model.test.Simple>>(){}.type, test$converter$SimpleConverter, false);
		
		gen.model.test.converters.CompositeConverter test$converter$CompositeConverter = new gen.model.test.converters.CompositeConverter(columns);
		container.register(test$converter$CompositeConverter);
		container.registerInstance(new org.revenj.patterns.GenericType<org.revenj.postgres.ObjectConverter<gen.model.test.Composite>>(){}.type, test$converter$CompositeConverter, false);
		test$converter$SimpleConverter.configure(container);
		test$converter$CompositeConverter.configure(container);
		
		container.register(gen.model.test.repositories.CompositeRepository.class);
		container.registerFactory(new org.revenj.patterns.GenericType<org.revenj.patterns.Repository<gen.model.test.Composite>>(){}.type, gen.model.test.repositories.CompositeRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.GenericType<org.revenj.patterns.PersistableRepository<gen.model.test.Composite>>(){}.type, gen.model.test.repositories.CompositeRepository::new, false);
	}
}
