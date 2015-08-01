package gen.model;


public class Boot implements org.revenj.extensibility.SystemAspect {

	public static org.revenj.patterns.ServiceLocator configure(String jdbcUrl) throws java.io.IOException {
		java.util.Properties properties = new java.util.Properties();
		java.io.File revProps = new java.io.File("revenj.properties");
		if (revProps.exists() && revProps.isFile()) {
			properties.load(new java.io.FileReader(revProps));
		}
		return configure(jdbcUrl, properties);
	}

	public static org.revenj.patterns.ServiceLocator configure(String jdbcUrl, java.util.Properties properties) throws java.io.IOException {
		properties.setProperty("namespace", "gen.model");
		java.util.function.Function<org.revenj.patterns.ServiceLocator, java.sql.Connection> factory = c -> {
			try {
				return java.sql.DriverManager.getConnection(jdbcUrl, properties);
			} catch (java.sql.SQLException e) {
				throw new RuntimeException(e);
			}
		};
		return org.revenj.Revenj.setup(factory, properties, java.util.Optional.<ClassLoader>empty(), java.util.Collections.singletonList((org.revenj.extensibility.SystemAspect) new Boot()).iterator());
	}

	public void configure(org.revenj.extensibility.Container container) throws java.io.IOException {
		java.util.List<org.revenj.postgres.ObjectConverter.ColumnInfo> columns = new java.util.ArrayList<>();
		java.util.Properties properties = container.resolve(java.util.Properties.class);
		String prevNamespace = properties.getProperty("namespace");
		if (prevNamespace != null && !"gen.model".equals(prevNamespace)) {
				throw new java.io.IOException("Different namespace already defined in Properties file. Trying to add namespace=gen.model. Found: " + prevNamespace);
		}
		properties.setProperty("namespace", "gen.model");
		try (java.sql.Connection connection = container.resolve(java.sql.Connection.class);
				java.sql.PreparedStatement statement = connection.prepareStatement("SELECT * FROM \"-NGS-\".load_type_info()");
				java.sql.ResultSet rs = statement.executeQuery()) {
			while (rs.next()) {
				columns.add(
						new org.revenj.postgres.ObjectConverter.ColumnInfo(
								rs.getString("type_schema"),
								rs.getString("type_name"),
								rs.getString("column_name"),
								rs.getString("column_schema"),
								rs.getString("column_type"),
								rs.getShort("column_index"),
								rs.getBoolean("is_not_null"),
								rs.getBoolean("is_ngs_generated")
						)
				);

			}
		} catch (java.sql.SQLException e) {
			throw new java.io.IOException(e);
		}
		container.registerInstance(org.revenj.patterns.ServiceLocator.class, container, false);
		org.revenj.postgres.jinq.JinqMetaModel metamodel = new org.revenj.postgres.jinq.JinqMetaModel();
		container.registerInstance(org.revenj.patterns.ServiceLocator.class, container, false);
		container.registerInstance(org.revenj.postgres.jinq.transform.MetamodelUtil.class, metamodel, false);
		container.registerInstance(org.revenj.postgres.QueryProvider.class, new org.revenj.postgres.jinq.RevenjQueryProvider(metamodel), false);
		
		
		gen.model.test.converters.SimpleConverter test$converter$SimpleConverter = new gen.model.test.converters.SimpleConverter(columns);
		container.register(test$converter$SimpleConverter);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.test.Simple>>(){}.type, test$converter$SimpleConverter, false);
		
		gen.model.test.converters.LazyLoadConverter test$converter$LazyLoadConverter = new gen.model.test.converters.LazyLoadConverter(columns);
		container.register(test$converter$LazyLoadConverter);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.test.LazyLoad>>(){}.type, test$converter$LazyLoadConverter, false);
		
		gen.model.test.converters.SingleDetailConverter test$converter$SingleDetailConverter = new gen.model.test.converters.SingleDetailConverter(columns);
		container.register(test$converter$SingleDetailConverter);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.test.SingleDetail>>(){}.type, test$converter$SingleDetailConverter, false);
		
		gen.model.test.converters.CompositeConverter test$converter$CompositeConverter = new gen.model.test.converters.CompositeConverter(columns);
		container.register(test$converter$CompositeConverter);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.test.Composite>>(){}.type, test$converter$CompositeConverter, false);
		
		gen.model.test.converters.CompositeListConverter test$converter$CompositeListConverter = new gen.model.test.converters.CompositeListConverter(columns);
		container.register(test$converter$CompositeListConverter);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.test.CompositeList>>(){}.type, test$converter$CompositeListConverter, false);
		
		gen.model.test.converters.EntityConverter test$converter$EntityConverter = new gen.model.test.converters.EntityConverter(columns);
		container.register(test$converter$EntityConverter);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.test.Entity>>(){}.type, test$converter$EntityConverter, false);
		
		gen.model.test.converters.ClickedConverter test$converter$ClickedConverter = new gen.model.test.converters.ClickedConverter(columns);
		container.register(test$converter$ClickedConverter);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.test.Clicked>>(){}.type, test$converter$ClickedConverter, false);
		
		gen.model.Seq.converters.NextConverter Seq$converter$NextConverter = new gen.model.Seq.converters.NextConverter(columns);
		container.register(Seq$converter$NextConverter);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.Seq.Next>>(){}.type, Seq$converter$NextConverter, false);
		
		gen.model.mixinReference.converters.SpecificReportConverter mixinReference$converter$SpecificReportConverter = new gen.model.mixinReference.converters.SpecificReportConverter(columns);
		container.register(mixinReference$converter$SpecificReportConverter);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.mixinReference.SpecificReport>>(){}.type, mixinReference$converter$SpecificReportConverter, false);
		
		gen.model.mixinReference.converters.AuthorConverter mixinReference$converter$AuthorConverter = new gen.model.mixinReference.converters.AuthorConverter(columns);
		container.register(mixinReference$converter$AuthorConverter);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.mixinReference.Author>>(){}.type, mixinReference$converter$AuthorConverter, false);
		test$converter$SimpleConverter.configure(container);
		metamodel.registerDataSource(gen.model.test.Simple.class, "\"test\".\"Simple\"");
		metamodel.registerProperty(gen.model.test.Simple.class, "getNumber", "\"number\"");
		metamodel.registerProperty(gen.model.test.Simple.class, "getText", "\"text\"");
		metamodel.registerProperty(gen.model.test.Simple.class, "getEn", "\"en\"");
		metamodel.registerProperty(gen.model.test.Simple.class, "getEn2", "\"en2\"");
		metamodel.registerProperty(gen.model.test.Simple.class, "getNb", "\"nb\"");
		test$converter$LazyLoadConverter.configure(container);
		metamodel.registerDataSource(gen.model.test.LazyLoad.class, "\"test\".\"LazyLoad_entity\"");
		metamodel.registerProperty(gen.model.test.LazyLoad.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.test.LazyLoad.class, "getID", "\"ID\"");
		gen.model.test.LazyLoad.__setupSequenceID();
		
		container.register(gen.model.test.repositories.LazyLoadRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.test.LazyLoad>>(){}.type, gen.model.test.repositories.LazyLoadRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.test.LazyLoad>>(){}.type, gen.model.test.repositories.LazyLoadRepository::new, false);
		metamodel.registerProperty(gen.model.test.LazyLoad.class, "getComp", "\"comp\"");
		metamodel.registerProperty(gen.model.test.LazyLoad.class, "getCompID", "\"compID\"");
		metamodel.registerProperty(gen.model.test.LazyLoad.class, "getSd", "\"sd\"");
		metamodel.registerProperty(gen.model.test.LazyLoad.class, "getSdID", "\"sdID\"");
		test$converter$SingleDetailConverter.configure(container);
		metamodel.registerDataSource(gen.model.test.SingleDetail.class, "\"test\".\"SingleDetail_entity\"");
		metamodel.registerProperty(gen.model.test.SingleDetail.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.test.SingleDetail.class, "getID", "\"ID\"");
		gen.model.test.SingleDetail.__setupSequenceID();
		
		container.register(gen.model.test.repositories.SingleDetailRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.test.SingleDetail>>(){}.type, gen.model.test.repositories.SingleDetailRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.test.SingleDetail>>(){}.type, gen.model.test.repositories.SingleDetailRepository::new, false);
		metamodel.registerProperty(gen.model.test.SingleDetail.class, "getDetails", "\"details\"");
		test$converter$CompositeConverter.configure(container);
		metamodel.registerDataSource(gen.model.test.Composite.class, "\"test\".\"Composite_entity\"");
		metamodel.registerProperty(gen.model.test.Composite.class, "getURI", "\"URI\"");
		
		container.register(gen.model.test.repositories.CompositeRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.test.Composite>>(){}.type, gen.model.test.repositories.CompositeRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.test.Composite>>(){}.type, gen.model.test.repositories.CompositeRepository::new, false);
		metamodel.registerProperty(gen.model.test.Composite.class, "getId", "\"id\"");
		metamodel.registerProperty(gen.model.test.Composite.class, "getEnn", "\"enn\"");
		metamodel.registerProperty(gen.model.test.Composite.class, "getSimple", "\"simple\"");
		metamodel.registerProperty(gen.model.test.Composite.class, "getEntities", "\"entities\"");
		metamodel.registerProperty(gen.model.test.Composite.class, "getLazies", "\"lazies\"");
		test$converter$CompositeListConverter.configure(container);
		metamodel.registerDataSource(gen.model.test.CompositeList.class, "\"test\".\"CompositeList_snowflake\"");
		metamodel.registerProperty(gen.model.test.CompositeList.class, "getURI", "\"URI\"");
		
		container.register(gen.model.test.repositories.CompositeListRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.test.CompositeList>>(){}.type, gen.model.test.repositories.CompositeListRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.test.CompositeList>>(){}.type, gen.model.test.repositories.CompositeListRepository::new, false);
		metamodel.registerProperty(gen.model.test.CompositeList.class, "getId", "\"id\"");
		metamodel.registerProperty(gen.model.test.CompositeList.class, "getEnn", "\"enn\"");
		metamodel.registerProperty(gen.model.test.CompositeList.class, "getEntities", "\"entities\"");
		metamodel.registerProperty(gen.model.test.CompositeList.class, "getSimple", "\"simple\"");
		test$converter$EntityConverter.configure(container);
		metamodel.registerDataSource(gen.model.test.Entity.class, "\"test\".\"Entity_entity\"");
		metamodel.registerProperty(gen.model.test.Entity.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.test.Entity.class, "getMoney", "\"money\"");
		metamodel.registerProperty(gen.model.test.Entity.class, "getId", "\"id\"");
		metamodel.registerProperty(gen.model.test.Entity.class, "getComposite", "\"composite\"");
		metamodel.registerProperty(gen.model.test.Entity.class, "getCompositeID", "\"compositeID\"");
		test$converter$ClickedConverter.configure(container);
		metamodel.registerDataSource(gen.model.test.Clicked.class, "\"test\".\"Clicked_event\"");
		metamodel.registerProperty(gen.model.test.Clicked.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.test.Clicked.class, "getQueuedAt", "\"QueuedAt\"");
		metamodel.registerProperty(gen.model.test.Clicked.class, "getProcessedAt", "\"ProcessedAt\"");
		
		container.register(gen.model.test.repositories.ClickedRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.test.Clicked>>(){}.type, gen.model.test.repositories.ClickedRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.test.Clicked>>(){}.type, gen.model.test.repositories.ClickedRepository::new, false);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.DomainEventStore<gen.model.test.Clicked>>(){}.type, gen.model.test.repositories.ClickedRepository::new, false);
		metamodel.registerProperty(gen.model.test.Clicked.class, "getDate", "\"date\"");
		metamodel.registerProperty(gen.model.test.Clicked.class, "getNumber", "\"number\"");
		metamodel.registerProperty(gen.model.test.Clicked.class, "getBigint", "\"bigint\"");
		metamodel.registerProperty(gen.model.test.Clicked.class, "getBool", "\"bool\"");
		metamodel.registerProperty(gen.model.test.Clicked.class, "getEn", "\"en\"");
		Seq$converter$NextConverter.configure(container);
		metamodel.registerDataSource(gen.model.Seq.Next.class, "\"Seq\".\"Next_entity\"");
		metamodel.registerProperty(gen.model.Seq.Next.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.Seq.Next.class, "getID", "\"ID\"");
		gen.model.Seq.Next.__setupSequenceID();
		
		container.register(gen.model.Seq.repositories.NextRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.Seq.Next>>(){}.type, gen.model.Seq.repositories.NextRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.Seq.Next>>(){}.type, gen.model.Seq.repositories.NextRepository::new, false);
		mixinReference$converter$SpecificReportConverter.configure(container);
		metamodel.registerDataSource(gen.model.mixinReference.SpecificReport.class, "\"mixinReference\".\"SpecificReport_entity\"");
		metamodel.registerProperty(gen.model.mixinReference.SpecificReport.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.mixinReference.SpecificReport.class, "getID", "\"ID\"");
		gen.model.mixinReference.SpecificReport.__setupSequenceID();
		
		container.register(gen.model.mixinReference.repositories.SpecificReportRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.mixinReference.SpecificReport>>(){}.type, gen.model.mixinReference.repositories.SpecificReportRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.mixinReference.SpecificReport>>(){}.type, gen.model.mixinReference.repositories.SpecificReportRepository::new, false);
		mixinReference$converter$AuthorConverter.configure(container);
		metamodel.registerDataSource(gen.model.mixinReference.Author.class, "\"mixinReference\".\"Author_entity\"");
		metamodel.registerProperty(gen.model.mixinReference.Author.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.mixinReference.Author.class, "getID", "\"ID\"");
		gen.model.mixinReference.Author.__setupSequenceID();
		
		container.register(gen.model.mixinReference.repositories.AuthorRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.mixinReference.Author>>(){}.type, gen.model.mixinReference.repositories.AuthorRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.mixinReference.Author>>(){}.type, gen.model.mixinReference.repositories.AuthorRepository::new, false);
		metamodel.registerProperty(gen.model.mixinReference.Author.class, "getName", "\"name\"");
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.PersistableRepository<gen.model.test.LazyLoad>>(){}.type, gen.model.test.repositories.LazyLoadRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.PersistableRepository<gen.model.test.SingleDetail>>(){}.type, gen.model.test.repositories.SingleDetailRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.PersistableRepository<gen.model.test.Composite>>(){}.type, gen.model.test.repositories.CompositeRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.PersistableRepository<gen.model.Seq.Next>>(){}.type, gen.model.Seq.repositories.NextRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.PersistableRepository<gen.model.mixinReference.SpecificReport>>(){}.type, gen.model.mixinReference.repositories.SpecificReportRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.PersistableRepository<gen.model.mixinReference.Author>>(){}.type, gen.model.mixinReference.repositories.AuthorRepository::new, false);
		metamodel.registerProperty(gen.model.test.Entity.class, "getCompositeid", "\"Compositeid\"");
		metamodel.registerProperty(gen.model.test.Entity.class, "getIndex", "\"Index\"");
		metamodel.registerProperty(gen.model.mixinReference.SpecificReport.class, "getAuthor", "\"author\"");
		metamodel.registerProperty(gen.model.mixinReference.SpecificReport.class, "getAuthorID", "\"authorID\"");
	}
}
