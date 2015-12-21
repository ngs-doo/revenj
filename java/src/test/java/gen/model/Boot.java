/*
* Created by DSL Platform
* v1.0.0.29923 
*/

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
		properties.setProperty("revenj.namespace", "gen.model");
		org.postgresql.ds.PGPoolingDataSource dataSource = new org.postgresql.ds.PGPoolingDataSource();
		dataSource.setUrl(jdbcUrl);
		String user = properties.getProperty("user");
		String revUser = properties.getProperty("revenj.user");
		if (revUser != null && revUser.length() > 0) {
			dataSource.setUser(revUser);
		} else if (user != null && user.length() > 0) {
			dataSource.setUser(user);
		}
		String password = properties.getProperty("password");
		String revPassword = properties.getProperty("revenj.password");
		if (revPassword != null && revPassword.length() > 0) {
			dataSource.setPassword(revPassword);
		} else if (password != null && password.length() > 0) {
			dataSource.setPassword(password);
		}
		return org.revenj.Revenj.setup(dataSource, properties, java.util.Optional.<ClassLoader>empty(), java.util.Collections.singletonList((org.revenj.extensibility.SystemAspect) new Boot()).iterator());
	}

	private java.util.List<org.revenj.postgres.ObjectConverter.ColumnInfo> loadColumnsInfo(
			org.revenj.extensibility.Container container,
			String query) throws java.sql.SQLException {
		java.util.List<org.revenj.postgres.ObjectConverter.ColumnInfo> columns = new java.util.ArrayList<>();
		try (java.sql.Connection connection = container.resolve(javax.sql.DataSource.class).getConnection();
				java.sql.Statement statement = connection.createStatement();
				java.sql.ResultSet rs = statement.executeQuery(query)) {
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
		}
		return columns;
	}

	public void configure(org.revenj.extensibility.Container container) throws java.io.IOException {
		java.util.Properties properties = container.resolve(java.util.Properties.class);
		String prevNamespace = properties.getProperty("revenj.namespace");
		if (prevNamespace != null && !"gen.model".equals(prevNamespace)) {
				throw new java.io.IOException("Different namespace already defined in Properties file. Trying to add namespace=gen.model. Found: " + prevNamespace);
		}
		properties.setProperty("revenj.namespace", "gen.model");
		java.util.List<org.revenj.postgres.ObjectConverter.ColumnInfo> columns;
		try {
			columns = loadColumnsInfo(container, "SELECT * FROM \"-NGS-\".load_type_info()");
		} catch (java.sql.SQLException ignore) {
			try {
				columns = loadColumnsInfo(container, "SELECT " +
"	ns.nspname::varchar as type_schema, " +
"	cl.relname::varchar as type_name, " +
"	atr.attname::varchar as column_name, " +
"	ns_ref.nspname::varchar as column_schema, " +
"	typ.typname::varchar as column_type, " +
"	(SELECT COUNT(*) + 1 " +
"	FROM pg_attribute atr_ord " +
"	WHERE " +
"		atr.attrelid = atr_ord.attrelid " +
"		AND atr_ord.attisdropped = false " +
"		AND atr_ord.attnum > 0 " +
"		AND atr_ord.attnum < atr.attnum)::smallint as column_index, " +
"	atr.attnotnull as is_not_null, " +
"	coalesce(d.description LIKE 'NGS generated%', false) as is_ngs_generated " +
"FROM " +
"	pg_attribute atr " +
"	INNER JOIN pg_class cl ON atr.attrelid = cl.oid " +
"	INNER JOIN pg_namespace ns ON cl.relnamespace = ns.oid " +
"	INNER JOIN pg_type typ ON atr.atttypid = typ.oid " +
"	INNER JOIN pg_namespace ns_ref ON typ.typnamespace = ns_ref.oid " +
"	LEFT JOIN pg_description d ON d.objoid = cl.oid " +
"								AND d.objsubid = atr.attnum " +
"WHERE " +
"	(cl.relkind = 'r' OR cl.relkind = 'v' OR cl.relkind = 'c') " +
"	AND ns.nspname NOT LIKE 'pg_%' " +
"	AND ns.nspname != 'information_schema' " +
"	AND atr.attnum > 0 " +
"	AND atr.attisdropped = FALSE " +
"ORDER BY 1, 2, 6");
			} catch (java.sql.SQLException e) {
				throw new java.io.IOException(e);
			}
		}
		org.revenj.postgres.jinq.JinqMetaModel metamodel = org.revenj.postgres.jinq.JinqMetaModel.configure(container);
		org.revenj.extensibility.PluginLoader plugins = container.resolve(org.revenj.extensibility.PluginLoader.class);
		
		
		gen.model.test.converters.SimpleConverter test$converter$SimpleConverter = new gen.model.test.converters.SimpleConverter(columns);
		container.registerInstance(gen.model.test.converters.SimpleConverter.class, test$converter$SimpleConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.test.Simple>>(){}.type, test$converter$SimpleConverter, false);
		
		gen.model.test.converters.EnConverter test$converter$EnConverter = new gen.model.test.converters.EnConverter();
		container.registerInstance(gen.model.test.converters.EnConverter.class, test$converter$EnConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.test.En>>(){}.type, test$converter$EnConverter, false);
		
		gen.model.test.converters.LazyLoadConverter test$converter$LazyLoadConverter = new gen.model.test.converters.LazyLoadConverter(columns);
		container.registerInstance(gen.model.test.converters.LazyLoadConverter.class, test$converter$LazyLoadConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.test.LazyLoad>>(){}.type, test$converter$LazyLoadConverter, false);
		
		gen.model.test.converters.SingleDetailConverter test$converter$SingleDetailConverter = new gen.model.test.converters.SingleDetailConverter(columns);
		container.registerInstance(gen.model.test.converters.SingleDetailConverter.class, test$converter$SingleDetailConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.test.SingleDetail>>(){}.type, test$converter$SingleDetailConverter, false);
		
		gen.model.test.converters.CompositeConverter test$converter$CompositeConverter = new gen.model.test.converters.CompositeConverter(columns);
		container.registerInstance(gen.model.test.converters.CompositeConverter.class, test$converter$CompositeConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.test.Composite>>(){}.type, test$converter$CompositeConverter, false);
		
		gen.model.test.converters.CompositeListConverter test$converter$CompositeListConverter = new gen.model.test.converters.CompositeListConverter(columns);
		container.registerInstance(gen.model.test.converters.CompositeListConverter.class, test$converter$CompositeListConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.test.CompositeList>>(){}.type, test$converter$CompositeListConverter, false);
		
		gen.model.test.converters.EntityConverter test$converter$EntityConverter = new gen.model.test.converters.EntityConverter(columns);
		container.registerInstance(gen.model.test.converters.EntityConverter.class, test$converter$EntityConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.test.Entity>>(){}.type, test$converter$EntityConverter, false);
		
		gen.model.test.converters.Detail1Converter test$converter$Detail1Converter = new gen.model.test.converters.Detail1Converter(columns);
		container.registerInstance(gen.model.test.converters.Detail1Converter.class, test$converter$Detail1Converter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.test.Detail1>>(){}.type, test$converter$Detail1Converter, false);
		
		gen.model.test.converters.Detail2Converter test$converter$Detail2Converter = new gen.model.test.converters.Detail2Converter(columns);
		container.registerInstance(gen.model.test.converters.Detail2Converter.class, test$converter$Detail2Converter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.test.Detail2>>(){}.type, test$converter$Detail2Converter, false);
		
		gen.model.test.converters.ClickedConverter test$converter$ClickedConverter = new gen.model.test.converters.ClickedConverter(columns);
		container.registerInstance(gen.model.test.converters.ClickedConverter.class, test$converter$ClickedConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.test.Clicked>>(){}.type, test$converter$ClickedConverter, false);
		
		gen.model.Seq.converters.NextConverter Seq$converter$NextConverter = new gen.model.Seq.converters.NextConverter(columns);
		container.registerInstance(gen.model.Seq.converters.NextConverter.class, Seq$converter$NextConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.Seq.Next>>(){}.type, Seq$converter$NextConverter, false);
		
		gen.model.mixinReference.converters.SpecificReportConverter mixinReference$converter$SpecificReportConverter = new gen.model.mixinReference.converters.SpecificReportConverter(columns);
		container.registerInstance(gen.model.mixinReference.converters.SpecificReportConverter.class, mixinReference$converter$SpecificReportConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.mixinReference.SpecificReport>>(){}.type, mixinReference$converter$SpecificReportConverter, false);
		
		gen.model.mixinReference.converters.AuthorConverter mixinReference$converter$AuthorConverter = new gen.model.mixinReference.converters.AuthorConverter(columns);
		container.registerInstance(gen.model.mixinReference.converters.AuthorConverter.class, mixinReference$converter$AuthorConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.mixinReference.Author>>(){}.type, mixinReference$converter$AuthorConverter, false);
		
		gen.model.mixinReference.converters.PersonConverter mixinReference$converter$PersonConverter = new gen.model.mixinReference.converters.PersonConverter(columns);
		container.registerInstance(gen.model.mixinReference.converters.PersonConverter.class, mixinReference$converter$PersonConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.mixinReference.Person>>(){}.type, mixinReference$converter$PersonConverter, false);
		
		gen.model.mixinReference.converters.ResidentConverter mixinReference$converter$ResidentConverter = new gen.model.mixinReference.converters.ResidentConverter(columns);
		container.registerInstance(gen.model.mixinReference.converters.ResidentConverter.class, mixinReference$converter$ResidentConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.mixinReference.Resident>>(){}.type, mixinReference$converter$ResidentConverter, false);
		
		gen.model.mixinReference.converters.ChildConverter mixinReference$converter$ChildConverter = new gen.model.mixinReference.converters.ChildConverter(columns);
		container.registerInstance(gen.model.mixinReference.converters.ChildConverter.class, mixinReference$converter$ChildConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.mixinReference.Child>>(){}.type, mixinReference$converter$ChildConverter, false);
		
		gen.model.mixinReference.converters.UserFilterConverter mixinReference$converter$UserFilterConverter = new gen.model.mixinReference.converters.UserFilterConverter(columns);
		container.registerInstance(gen.model.mixinReference.converters.UserFilterConverter.class, mixinReference$converter$UserFilterConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.mixinReference.UserFilter>>(){}.type, mixinReference$converter$UserFilterConverter, false);
		org.revenj.security.PermissionManager permissions = container.resolve(org.revenj.security.PermissionManager.class);
		
		gen.model.binaries.converters.DocumentConverter binaries$converter$DocumentConverter = new gen.model.binaries.converters.DocumentConverter(columns);
		container.registerInstance(gen.model.binaries.converters.DocumentConverter.class, binaries$converter$DocumentConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.binaries.Document>>(){}.type, binaries$converter$DocumentConverter, false);
		
		gen.model.binaries.converters.WritableDocumentConverter binaries$converter$WritableDocumentConverter = new gen.model.binaries.converters.WritableDocumentConverter(loadQueryInfo(container, "SELECT * FROM \"binaries\".\"Document\" sq LIMIT 0", "binaries", "WritableDocument"));
		container.registerInstance(gen.model.binaries.converters.WritableDocumentConverter.class, binaries$converter$WritableDocumentConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.binaries.WritableDocument>>(){}.type, binaries$converter$WritableDocumentConverter, false);
		
		gen.model.binaries.converters.ReadOnlyDocumentConverter binaries$converter$ReadOnlyDocumentConverter = new gen.model.binaries.converters.ReadOnlyDocumentConverter(loadQueryInfo(container, "SELECT * FROM (SELECT \"ID\", name from binaries.\"Document\") sq LIMIT 0", "binaries", "ReadOnlyDocument"));
		container.registerInstance(gen.model.binaries.converters.ReadOnlyDocumentConverter.class, binaries$converter$ReadOnlyDocumentConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.binaries.ReadOnlyDocument>>(){}.type, binaries$converter$ReadOnlyDocumentConverter, false);
		
		gen.model.security.converters.DocumentConverter security$converter$DocumentConverter = new gen.model.security.converters.DocumentConverter(columns);
		container.registerInstance(gen.model.security.converters.DocumentConverter.class, security$converter$DocumentConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.security.Document>>(){}.type, security$converter$DocumentConverter, false);
		
		gen.model.egzotics.converters.pksConverter egzotics$converter$pksConverter = new gen.model.egzotics.converters.pksConverter(columns);
		container.registerInstance(gen.model.egzotics.converters.pksConverter.class, egzotics$converter$pksConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.egzotics.pks>>(){}.type, egzotics$converter$pksConverter, false);
		
		gen.model.egzotics.converters.vConverter egzotics$converter$vConverter = new gen.model.egzotics.converters.vConverter(columns);
		container.registerInstance(gen.model.egzotics.converters.vConverter.class, egzotics$converter$vConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.egzotics.v>>(){}.type, egzotics$converter$vConverter, false);
		
		gen.model.egzotics.converters.PksVConverter egzotics$converter$PksVConverter = new gen.model.egzotics.converters.PksVConverter(columns);
		container.registerInstance(gen.model.egzotics.converters.PksVConverter.class, egzotics$converter$PksVConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.egzotics.PksV>>(){}.type, egzotics$converter$PksVConverter, false);
		
		gen.model.egzotics.converters.EConverter egzotics$converter$EConverter = new gen.model.egzotics.converters.EConverter();
		container.registerInstance(gen.model.egzotics.converters.EConverter.class, egzotics$converter$EConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.egzotics.E>>(){}.type, egzotics$converter$EConverter, false);
		
		gen.model.issues.converters.DateListConverter issues$converter$DateListConverter = new gen.model.issues.converters.DateListConverter(columns);
		container.registerInstance(gen.model.issues.converters.DateListConverter.class, issues$converter$DateListConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.issues.DateList>>(){}.type, issues$converter$DateListConverter, false);
		
		gen.model.issues.converters.TimestampPkConverter issues$converter$TimestampPkConverter = new gen.model.issues.converters.TimestampPkConverter(columns);
		container.registerInstance(gen.model.issues.converters.TimestampPkConverter.class, issues$converter$TimestampPkConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.issues.TimestampPk>>(){}.type, issues$converter$TimestampPkConverter, false);
		
		gen.model.md.converters.MasterConverter md$converter$MasterConverter = new gen.model.md.converters.MasterConverter(columns);
		container.registerInstance(gen.model.md.converters.MasterConverter.class, md$converter$MasterConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.md.Master>>(){}.type, md$converter$MasterConverter, false);
		
		gen.model.md.converters.DetailConverter md$converter$DetailConverter = new gen.model.md.converters.DetailConverter(columns);
		container.registerInstance(gen.model.md.converters.DetailConverter.class, md$converter$DetailConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.md.Detail>>(){}.type, md$converter$DetailConverter, false);
		
		gen.model.md.converters.Child1Converter md$converter$Child1Converter = new gen.model.md.converters.Child1Converter(columns);
		container.registerInstance(gen.model.md.converters.Child1Converter.class, md$converter$Child1Converter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.md.Child1>>(){}.type, md$converter$Child1Converter, false);
		
		gen.model.md.converters.Child2Converter md$converter$Child2Converter = new gen.model.md.converters.Child2Converter(columns);
		container.registerInstance(gen.model.md.converters.Child2Converter.class, md$converter$Child2Converter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.md.Child2>>(){}.type, md$converter$Child2Converter, false);
		
		gen.model.md.converters.Reference1Converter md$converter$Reference1Converter = new gen.model.md.converters.Reference1Converter(columns);
		container.registerInstance(gen.model.md.converters.Reference1Converter.class, md$converter$Reference1Converter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.md.Reference1>>(){}.type, md$converter$Reference1Converter, false);
		
		gen.model.md.converters.Reference2Converter md$converter$Reference2Converter = new gen.model.md.converters.Reference2Converter(columns);
		container.registerInstance(gen.model.md.converters.Reference2Converter.class, md$converter$Reference2Converter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.md.Reference2>>(){}.type, md$converter$Reference2Converter, false);
		
		gen.model.adt.converters.AuthConverter adt$converter$AuthConverter = new gen.model.adt.converters.AuthConverter(columns);
		container.registerInstance(gen.model.adt.converters.AuthConverter.class, adt$converter$AuthConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.adt.Auth>>(){}.type, adt$converter$AuthConverter, false);
		
		gen.model.adt.converters.BasicSecurityConverter adt$converter$BasicSecurityConverter = new gen.model.adt.converters.BasicSecurityConverter(columns);
		container.registerInstance(gen.model.adt.converters.BasicSecurityConverter.class, adt$converter$BasicSecurityConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.adt.BasicSecurity>>(){}.type, adt$converter$BasicSecurityConverter, false);
		
		gen.model.adt.converters.TokenConverter adt$converter$TokenConverter = new gen.model.adt.converters.TokenConverter(columns);
		container.registerInstance(gen.model.adt.converters.TokenConverter.class, adt$converter$TokenConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.adt.Token>>(){}.type, adt$converter$TokenConverter, false);
		
		gen.model.adt.converters.AnonymousConverter adt$converter$AnonymousConverter = new gen.model.adt.converters.AnonymousConverter(columns);
		container.registerInstance(gen.model.adt.converters.AnonymousConverter.class, adt$converter$AnonymousConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.adt.Anonymous>>(){}.type, adt$converter$AnonymousConverter, false);
		
		gen.model.adt.converters.DigestSecurityConverter adt$converter$DigestSecurityConverter = new gen.model.adt.converters.DigestSecurityConverter(columns);
		container.registerInstance(gen.model.adt.converters.DigestSecurityConverter.class, adt$converter$DigestSecurityConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.adt.DigestSecurity>>(){}.type, adt$converter$DigestSecurityConverter, false);
		
		gen.model.adt.converters.UserConverter adt$converter$UserConverter = new gen.model.adt.converters.UserConverter(columns);
		container.registerInstance(gen.model.adt.converters.UserConverter.class, adt$converter$UserConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.adt.User>>(){}.type, adt$converter$UserConverter, false);
		
		gen.model.calc.converters.InfoConverter calc$converter$InfoConverter = new gen.model.calc.converters.InfoConverter(columns);
		container.registerInstance(gen.model.calc.converters.InfoConverter.class, calc$converter$InfoConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.calc.Info>>(){}.type, calc$converter$InfoConverter, false);
		
		gen.model.calc.converters.TypeConverter calc$converter$TypeConverter = new gen.model.calc.converters.TypeConverter(columns);
		container.registerInstance(gen.model.calc.converters.TypeConverter.class, calc$converter$TypeConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.calc.Type>>(){}.type, calc$converter$TypeConverter, false);
		
		gen.model.calc.converters.RealmConverter calc$converter$RealmConverter = new gen.model.calc.converters.RealmConverter(columns);
		container.registerInstance(gen.model.calc.converters.RealmConverter.class, calc$converter$RealmConverter, false);
		container.registerInstance(new org.revenj.patterns.Generic<org.revenj.postgres.ObjectConverter<gen.model.calc.Realm>>(){}.type, calc$converter$RealmConverter, false);
		test$converter$SimpleConverter.configure(container);
		metamodel.registerDataSource(gen.model.test.Simple.class, "\"test\".\"Simple\"");
		metamodel.registerProperty(gen.model.test.Simple.class, "getNumber", "\"number\"");
		metamodel.registerProperty(gen.model.test.Simple.class, "getText", "\"text\"");
		metamodel.registerProperty(gen.model.test.Simple.class, "getEn", "\"en\"");
		metamodel.registerProperty(gen.model.test.Simple.class, "getEn2", "\"en2\"");
		metamodel.registerProperty(gen.model.test.Simple.class, "getNb", "\"nb\"");
		metamodel.registerProperty(gen.model.test.Simple.class, "getTs", "\"ts\"");
		metamodel.registerEnum(gen.model.test.En.class, "\"test\".\"En\"");
		test$converter$LazyLoadConverter.configure(container);
		metamodel.registerDataSource(gen.model.test.LazyLoad.class, "\"test\".\"LazyLoad_entity\"");
		metamodel.registerProperty(gen.model.test.LazyLoad.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.test.LazyLoad.class, "getID", "\"ID\"");
		
		container.register(gen.model.test.repositories.LazyLoadRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.test.LazyLoad>>(){}.type, gen.model.test.repositories.LazyLoadRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.test.LazyLoad>>(){}.type, gen.model.test.repositories.LazyLoadRepository::new, false);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.postgres.BulkRepository<gen.model.test.LazyLoad>>(){}.type, gen.model.test.repositories.LazyLoadRepository::new, false);
		metamodel.registerProperty(gen.model.test.LazyLoad.class, "getComp", "\"comp\"");
		metamodel.registerProperty(gen.model.test.LazyLoad.class, "getCompID", "\"compID\"");
		metamodel.registerProperty(gen.model.test.LazyLoad.class, "getSd", "\"sd\"");
		metamodel.registerProperty(gen.model.test.LazyLoad.class, "getSdID", "\"sdID\"");
		test$converter$SingleDetailConverter.configure(container);
		metamodel.registerDataSource(gen.model.test.SingleDetail.class, "\"test\".\"SingleDetail_entity\"");
		metamodel.registerProperty(gen.model.test.SingleDetail.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.test.SingleDetail.class, "getID", "\"ID\"");
		
		container.register(gen.model.test.repositories.SingleDetailRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.test.SingleDetail>>(){}.type, gen.model.test.repositories.SingleDetailRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.test.SingleDetail>>(){}.type, gen.model.test.repositories.SingleDetailRepository::new, false);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.postgres.BulkRepository<gen.model.test.SingleDetail>>(){}.type, gen.model.test.repositories.SingleDetailRepository::new, false);
		metamodel.registerProperty(gen.model.test.SingleDetail.class, "getDetails", "\"details\"");
		test$converter$CompositeConverter.configure(container);
		metamodel.registerDataSource(gen.model.test.Composite.class, "\"test\".\"Composite_entity\"");
		metamodel.registerProperty(gen.model.test.Composite.class, "getURI", "\"URI\"");
		
		container.register(gen.model.test.repositories.CompositeRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.test.Composite>>(){}.type, gen.model.test.repositories.CompositeRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.test.Composite>>(){}.type, gen.model.test.repositories.CompositeRepository::new, false);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.postgres.BulkRepository<gen.model.test.Composite>>(){}.type, gen.model.test.repositories.CompositeRepository::new, false);
		metamodel.registerProperty(gen.model.test.Composite.class, "getId", "\"id\"");
		metamodel.registerProperty(gen.model.test.Composite.class, "getEnn", "\"enn\"");
		metamodel.registerProperty(gen.model.test.Composite.class, "getEn", "\"en\"");
		metamodel.registerProperty(gen.model.test.Composite.class, "getSimple", "\"simple\"");
		metamodel.registerProperty(gen.model.test.Composite.class, "getChange", "\"change\"");
		metamodel.registerProperty(gen.model.test.Composite.class, "getTsl", "\"tsl\"");
		metamodel.registerProperty(gen.model.test.Composite.class, "getEntities", "\"entities\"");
		metamodel.registerProperty(gen.model.test.Composite.class, "getLazies", "\"lazies\"");
		metamodel.registerProperty(gen.model.test.Composite.class, "getIndexes", "\"indexes\"");
		test$converter$CompositeListConverter.configure(container);
		metamodel.registerDataSource(gen.model.test.CompositeList.class, "\"test\".\"CompositeList_snowflake\"");
		metamodel.registerProperty(gen.model.test.CompositeList.class, "getURI", "\"URI\"");
		
		container.register(gen.model.test.repositories.CompositeListRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.test.CompositeList>>(){}.type, gen.model.test.repositories.CompositeListRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.postgres.BulkRepository<gen.model.test.CompositeList>>(){}.type, gen.model.test.repositories.CompositeListRepository::new, false);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.test.CompositeList>>(){}.type, gen.model.test.repositories.CompositeListRepository::new, false);
		metamodel.registerProperty(gen.model.test.CompositeList.class, "getId", "\"id\"");
		metamodel.registerProperty(gen.model.test.CompositeList.class, "getEnn", "\"enn\"");
		metamodel.registerProperty(gen.model.test.CompositeList.class, "getEn", "\"en\"");
		metamodel.registerProperty(gen.model.test.CompositeList.class, "getTsl", "\"tsl\"");
		metamodel.registerProperty(gen.model.test.CompositeList.class, "getChange", "\"change\"");
		metamodel.registerProperty(gen.model.test.CompositeList.class, "getEntities", "\"entities\"");
		metamodel.registerProperty(gen.model.test.CompositeList.class, "getSimple", "\"simple\"");
		metamodel.registerProperty(gen.model.test.CompositeList.class, "getNumber", "\"number\"");
		metamodel.registerProperty(gen.model.test.CompositeList.class, "getEntitiesCount", "\"entitiesCount\"");
		metamodel.registerProperty(gen.model.test.CompositeList.class, "getHasEntities", "\"hasEntities\"");
		metamodel.registerProperty(gen.model.test.CompositeList.class, "getEntityHasMoney", "\"entityHasMoney\"");
		metamodel.registerProperty(gen.model.test.CompositeList.class, "getIndexes", "\"indexes\"");
		container.register(gen.model.test.CompositeCube.class, false);
		test$converter$EntityConverter.configure(container);
		metamodel.registerDataSource(gen.model.test.Entity.class, "\"test\".\"Entity_entity\"");
		metamodel.registerProperty(gen.model.test.Entity.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.test.Entity.class, "getMoney", "\"money\"");
		metamodel.registerProperty(gen.model.test.Entity.class, "getId", "\"id\"");
		metamodel.registerProperty(gen.model.test.Entity.class, "getComposite", "\"composite\"");
		metamodel.registerProperty(gen.model.test.Entity.class, "getCompositeID", "\"compositeID\"");
		metamodel.registerProperty(gen.model.test.Entity.class, "getDetail1", "\"detail1\"");
		metamodel.registerProperty(gen.model.test.Entity.class, "getDetail2", "\"detail2\"");
		test$converter$Detail1Converter.configure(container);
		metamodel.registerDataSource(gen.model.test.Detail1.class, "\"test\".\"Detail1_entity\"");
		metamodel.registerProperty(gen.model.test.Detail1.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.test.Detail1.class, "getF", "\"f\"");
		metamodel.registerProperty(gen.model.test.Detail1.class, "getFf", "\"ff\"");
		test$converter$Detail2Converter.configure(container);
		metamodel.registerDataSource(gen.model.test.Detail2.class, "\"test\".\"Detail2_entity\"");
		metamodel.registerProperty(gen.model.test.Detail2.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.test.Detail2.class, "getU", "\"u\"");
		metamodel.registerProperty(gen.model.test.Detail2.class, "getDd", "\"dd\"");
		test$converter$ClickedConverter.configure(container);
		metamodel.registerDataSource(gen.model.test.Clicked.class, "\"test\".\"Clicked_event\"");
		metamodel.registerProperty(gen.model.test.Clicked.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.test.Clicked.class, "getQueuedAt", "\"QueuedAt\"");
		metamodel.registerProperty(gen.model.test.Clicked.class, "getProcessedAt", "\"ProcessedAt\"");
		
		container.register(gen.model.test.repositories.ClickedRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.test.Clicked>>(){}.type, gen.model.test.repositories.ClickedRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.test.Clicked>>(){}.type, gen.model.test.repositories.ClickedRepository::new, false);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.postgres.BulkRepository<gen.model.test.Clicked>>(){}.type, gen.model.test.repositories.ClickedRepository::new, false);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.DomainEventStore<gen.model.test.Clicked>>(){}.type, gen.model.test.repositories.ClickedRepository::new, false);
		try {
			org.revenj.Revenj.registerEvents(container, plugins, gen.model.test.Clicked.class, gen.model.test.Clicked[].class);
		} catch (java.lang.Exception e) {
			throw new java.io.IOException(e);
		}
		metamodel.registerProperty(gen.model.test.Clicked.class, "getDate", "\"date\"");
		metamodel.registerProperty(gen.model.test.Clicked.class, "getNumber", "\"number\"");
		metamodel.registerProperty(gen.model.test.Clicked.class, "getBigint", "\"bigint\"");
		metamodel.registerProperty(gen.model.test.Clicked.class, "getBool", "\"bool\"");
		metamodel.registerProperty(gen.model.test.Clicked.class, "getEn", "\"en\"");
		Seq$converter$NextConverter.configure(container);
		metamodel.registerDataSource(gen.model.Seq.Next.class, "\"Seq\".\"Next_entity\"");
		metamodel.registerProperty(gen.model.Seq.Next.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.Seq.Next.class, "getID", "\"ID\"");
		
		container.register(gen.model.Seq.repositories.NextRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.Seq.Next>>(){}.type, gen.model.Seq.repositories.NextRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.Seq.Next>>(){}.type, gen.model.Seq.repositories.NextRepository::new, false);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.postgres.BulkRepository<gen.model.Seq.Next>>(){}.type, gen.model.Seq.repositories.NextRepository::new, false);
		mixinReference$converter$SpecificReportConverter.configure(container);
		metamodel.registerDataSource(gen.model.mixinReference.SpecificReport.class, "\"mixinReference\".\"SpecificReport_entity\"");
		metamodel.registerProperty(gen.model.mixinReference.SpecificReport.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.mixinReference.SpecificReport.class, "getID", "\"ID\"");
		
		container.register(gen.model.mixinReference.repositories.SpecificReportRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.mixinReference.SpecificReport>>(){}.type, gen.model.mixinReference.repositories.SpecificReportRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.mixinReference.SpecificReport>>(){}.type, gen.model.mixinReference.repositories.SpecificReportRepository::new, false);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.postgres.BulkRepository<gen.model.mixinReference.SpecificReport>>(){}.type, gen.model.mixinReference.repositories.SpecificReportRepository::new, false);
		mixinReference$converter$AuthorConverter.configure(container);
		metamodel.registerDataSource(gen.model.mixinReference.Author.class, "\"mixinReference\".\"Author_entity\"");
		metamodel.registerProperty(gen.model.mixinReference.Author.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.mixinReference.Author.class, "getID", "\"ID\"");
		
		container.register(gen.model.mixinReference.repositories.AuthorRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.mixinReference.Author>>(){}.type, gen.model.mixinReference.repositories.AuthorRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.mixinReference.Author>>(){}.type, gen.model.mixinReference.repositories.AuthorRepository::new, false);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.postgres.BulkRepository<gen.model.mixinReference.Author>>(){}.type, gen.model.mixinReference.repositories.AuthorRepository::new, false);
		metamodel.registerProperty(gen.model.mixinReference.Author.class, "getName", "\"name\"");
		metamodel.registerProperty(gen.model.mixinReference.Author.class, "getPerson", "\"person\"");
		metamodel.registerProperty(gen.model.mixinReference.Author.class, "getRezident", "\"rezident\"");
		metamodel.registerProperty(gen.model.mixinReference.Author.class, "getRezidentID", "\"rezidentID\"");
		metamodel.registerProperty(gen.model.mixinReference.Author.class, "getChildren", "\"children\"");
		mixinReference$converter$PersonConverter.configure(container);
		metamodel.registerDataSource(gen.model.mixinReference.Person.class, "\"mixinReference\".\"Person_entity\"");
		metamodel.registerProperty(gen.model.mixinReference.Person.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.mixinReference.Person.class, "getBirth", "\"birth\"");
		mixinReference$converter$ResidentConverter.configure(container);
		metamodel.registerDataSource(gen.model.mixinReference.Resident.class, "\"mixinReference\".\"Resident_entity\"");
		metamodel.registerProperty(gen.model.mixinReference.Resident.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.mixinReference.Resident.class, "getId", "\"id\"");
		metamodel.registerProperty(gen.model.mixinReference.Resident.class, "getBirth", "\"birth\"");
		mixinReference$converter$ChildConverter.configure(container);
		metamodel.registerDataSource(gen.model.mixinReference.Child.class, "\"mixinReference\".\"Child_entity\"");
		metamodel.registerProperty(gen.model.mixinReference.Child.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.mixinReference.Child.class, "getVersion", "\"version\"");
		mixinReference$converter$UserFilterConverter.configure(container);
		metamodel.registerDataSource(gen.model.mixinReference.UserFilter.class, "\"mixinReference\".\"UserFilter_entity\"");
		metamodel.registerProperty(gen.model.mixinReference.UserFilter.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.mixinReference.UserFilter.class, "getID", "\"ID\"");
		
		container.register(gen.model.mixinReference.repositories.UserFilterRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.mixinReference.UserFilter>>(){}.type, gen.model.mixinReference.repositories.UserFilterRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.mixinReference.UserFilter>>(){}.type, gen.model.mixinReference.repositories.UserFilterRepository::new, false);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.postgres.BulkRepository<gen.model.mixinReference.UserFilter>>(){}.type, gen.model.mixinReference.repositories.UserFilterRepository::new, false);
		metamodel.registerProperty(gen.model.mixinReference.UserFilter.class, "getName", "\"name\"");
		permissions.registerFilter(gen.model.mixinReference.UserFilter.class, it -> it.getName().equals(org.revenj.security.PermissionManager.boundPrincipal.get().getName()), "RegularUser", false);
		binaries$converter$DocumentConverter.configure(container);
		metamodel.registerDataSource(gen.model.binaries.Document.class, "\"binaries\".\"Document_entity\"");
		metamodel.registerProperty(gen.model.binaries.Document.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.binaries.Document.class, "getID", "\"ID\"");
		
		container.register(gen.model.binaries.repositories.DocumentRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.binaries.Document>>(){}.type, gen.model.binaries.repositories.DocumentRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.binaries.Document>>(){}.type, gen.model.binaries.repositories.DocumentRepository::new, false);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.postgres.BulkRepository<gen.model.binaries.Document>>(){}.type, gen.model.binaries.repositories.DocumentRepository::new, false);
		metamodel.registerProperty(gen.model.binaries.Document.class, "getName", "\"name\"");
		metamodel.registerProperty(gen.model.binaries.Document.class, "getContent", "\"content\"");
		metamodel.registerProperty(gen.model.binaries.Document.class, "getBools", "\"bools\"");
		binaries$converter$WritableDocumentConverter.configure(container);
		metamodel.registerDataSource(gen.model.binaries.WritableDocument.class, "\"binaries\".\"Document\"");
		
		container.register(gen.model.binaries.repositories.WritableDocumentRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.binaries.WritableDocument>>(){}.type, gen.model.binaries.repositories.WritableDocumentRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.binaries.WritableDocument>>(){}.type, gen.model.binaries.repositories.WritableDocumentRepository::new, false);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.postgres.BulkRepository<gen.model.binaries.WritableDocument>>(){}.type, gen.model.binaries.repositories.WritableDocumentRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.PersistableRepository<gen.model.binaries.WritableDocument>>(){}.type, gen.model.binaries.repositories.WritableDocumentRepository::new, false);
		metamodel.registerProperty(gen.model.binaries.WritableDocument.class, "getId", "\"ID\"");
		metamodel.registerProperty(gen.model.binaries.WritableDocument.class, "getName", "\"name\"");
		binaries$converter$ReadOnlyDocumentConverter.configure(container);
		metamodel.registerDataSource(gen.model.binaries.ReadOnlyDocument.class, "(SELECT \"ID\", name from binaries.\"Document\")");
		
		container.register(gen.model.binaries.repositories.ReadOnlyDocumentRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.binaries.ReadOnlyDocument>>(){}.type, gen.model.binaries.repositories.ReadOnlyDocumentRepository::new, false);
		metamodel.registerProperty(gen.model.binaries.ReadOnlyDocument.class, "getID", "\"ID\"");
		metamodel.registerProperty(gen.model.binaries.ReadOnlyDocument.class, "getName", "\"name\"");
		security$converter$DocumentConverter.configure(container);
		metamodel.registerDataSource(gen.model.security.Document.class, "\"security\".\"Document_entity\"");
		metamodel.registerProperty(gen.model.security.Document.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.security.Document.class, "getID", "\"ID\"");
		
		container.register(gen.model.security.repositories.DocumentRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.security.Document>>(){}.type, gen.model.security.repositories.DocumentRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.security.Document>>(){}.type, gen.model.security.repositories.DocumentRepository::new, false);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.postgres.BulkRepository<gen.model.security.Document>>(){}.type, gen.model.security.repositories.DocumentRepository::new, false);
		metamodel.registerProperty(gen.model.security.Document.class, "getData", "\"data\"");
		permissions.registerFilter(gen.model.security.Document.class, it -> !it.getDeactivated(), "Admin", true);
		egzotics$converter$pksConverter.configure(container);
		metamodel.registerDataSource(gen.model.egzotics.pks.class, "\"egzotics\".\"pks_entity\"");
		metamodel.registerProperty(gen.model.egzotics.pks.class, "getURI", "\"URI\"");
		
		container.register(gen.model.egzotics.repositories.pksRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.egzotics.pks>>(){}.type, gen.model.egzotics.repositories.pksRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.egzotics.pks>>(){}.type, gen.model.egzotics.repositories.pksRepository::new, false);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.postgres.BulkRepository<gen.model.egzotics.pks>>(){}.type, gen.model.egzotics.repositories.pksRepository::new, false);
		metamodel.registerProperty(gen.model.egzotics.pks.class, "getId", "\"id\"");
		metamodel.registerProperty(gen.model.egzotics.pks.class, "getXml", "\"xml\"");
		metamodel.registerProperty(gen.model.egzotics.pks.class, "getS3", "\"s3\"");
		metamodel.registerProperty(gen.model.egzotics.pks.class, "getPp", "\"pp\"");
		metamodel.registerProperty(gen.model.egzotics.pks.class, "getL", "\"l\"");
		egzotics$converter$vConverter.configure(container);
		metamodel.registerDataSource(gen.model.egzotics.v.class, "\"egzotics\".\"v\"");
		metamodel.registerProperty(gen.model.egzotics.v.class, "getX", "\"x\"");
		egzotics$converter$PksVConverter.configure(container);
		metamodel.registerDataSource(gen.model.egzotics.PksV.class, "\"egzotics\".\"PksV_entity\"");
		metamodel.registerProperty(gen.model.egzotics.PksV.class, "getURI", "\"URI\"");
		
		container.register(gen.model.egzotics.repositories.PksVRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.egzotics.PksV>>(){}.type, gen.model.egzotics.repositories.PksVRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.egzotics.PksV>>(){}.type, gen.model.egzotics.repositories.PksVRepository::new, false);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.postgres.BulkRepository<gen.model.egzotics.PksV>>(){}.type, gen.model.egzotics.repositories.PksVRepository::new, false);
		metamodel.registerProperty(gen.model.egzotics.PksV.class, "getV", "\"v\"");
		metamodel.registerProperty(gen.model.egzotics.PksV.class, "getVv", "\"vv\"");
		metamodel.registerProperty(gen.model.egzotics.PksV.class, "getE", "\"e\"");
		metamodel.registerProperty(gen.model.egzotics.PksV.class, "getEe", "\"ee\"");
		metamodel.registerProperty(gen.model.egzotics.PksV.class, "getP", "\"p\"");
		metamodel.registerProperty(gen.model.egzotics.PksV.class, "getLl", "\"ll\"");
		metamodel.registerEnum(gen.model.egzotics.E.class, "\"egzotics\".\"E\"");
		issues$converter$DateListConverter.configure(container);
		metamodel.registerDataSource(gen.model.issues.DateList.class, "\"issues\".\"DateList_entity\"");
		metamodel.registerProperty(gen.model.issues.DateList.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.issues.DateList.class, "getID", "\"ID\"");
		
		container.register(gen.model.issues.repositories.DateListRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.issues.DateList>>(){}.type, gen.model.issues.repositories.DateListRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.issues.DateList>>(){}.type, gen.model.issues.repositories.DateListRepository::new, false);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.postgres.BulkRepository<gen.model.issues.DateList>>(){}.type, gen.model.issues.repositories.DateListRepository::new, false);
		metamodel.registerProperty(gen.model.issues.DateList.class, "getList", "\"list\"");
		issues$converter$TimestampPkConverter.configure(container);
		metamodel.registerDataSource(gen.model.issues.TimestampPk.class, "\"issues\".\"TimestampPk_entity\"");
		metamodel.registerProperty(gen.model.issues.TimestampPk.class, "getURI", "\"URI\"");
		
		container.register(gen.model.issues.repositories.TimestampPkRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.issues.TimestampPk>>(){}.type, gen.model.issues.repositories.TimestampPkRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.issues.TimestampPk>>(){}.type, gen.model.issues.repositories.TimestampPkRepository::new, false);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.postgres.BulkRepository<gen.model.issues.TimestampPk>>(){}.type, gen.model.issues.repositories.TimestampPkRepository::new, false);
		metamodel.registerProperty(gen.model.issues.TimestampPk.class, "getTs", "\"ts\"");
		metamodel.registerProperty(gen.model.issues.TimestampPk.class, "getD", "\"d\"");
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.PersistableRepository<gen.model.issues.TimestampPk>>(){}.type, gen.model.issues.repositories.TimestampPkRepository::new, false);
		md$converter$MasterConverter.configure(container);
		metamodel.registerDataSource(gen.model.md.Master.class, "\"md\".\"Master_entity\"");
		metamodel.registerProperty(gen.model.md.Master.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.md.Master.class, "getID", "\"ID\"");
		
		container.register(gen.model.md.repositories.MasterRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.md.Master>>(){}.type, gen.model.md.repositories.MasterRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.md.Master>>(){}.type, gen.model.md.repositories.MasterRepository::new, false);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.postgres.BulkRepository<gen.model.md.Master>>(){}.type, gen.model.md.repositories.MasterRepository::new, false);
		metamodel.registerProperty(gen.model.md.Master.class, "getDetails", "\"details\"");
		md$converter$DetailConverter.configure(container);
		metamodel.registerDataSource(gen.model.md.Detail.class, "\"md\".\"Detail_entity\"");
		metamodel.registerProperty(gen.model.md.Detail.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.md.Detail.class, "getId", "\"id\"");
		metamodel.registerProperty(gen.model.md.Detail.class, "getMasterId", "\"masterId\"");
		metamodel.registerProperty(gen.model.md.Detail.class, "getChildren1", "\"children1\"");
		metamodel.registerProperty(gen.model.md.Detail.class, "getChildren2", "\"children2\"");
		metamodel.registerProperty(gen.model.md.Detail.class, "getReference1", "\"reference1\"");
		metamodel.registerProperty(gen.model.md.Detail.class, "getReference2", "\"reference2\"");
		md$converter$Child1Converter.configure(container);
		metamodel.registerDataSource(gen.model.md.Child1.class, "\"md\".\"Child1_entity\"");
		metamodel.registerProperty(gen.model.md.Child1.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.md.Child1.class, "getI", "\"i\"");
		md$converter$Child2Converter.configure(container);
		metamodel.registerDataSource(gen.model.md.Child2.class, "\"md\".\"Child2_entity\"");
		metamodel.registerProperty(gen.model.md.Child2.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.md.Child2.class, "getD", "\"d\"");
		md$converter$Reference1Converter.configure(container);
		metamodel.registerDataSource(gen.model.md.Reference1.class, "\"md\".\"Reference1_entity\"");
		metamodel.registerProperty(gen.model.md.Reference1.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.md.Reference1.class, "getL", "\"l\"");
		md$converter$Reference2Converter.configure(container);
		metamodel.registerDataSource(gen.model.md.Reference2.class, "\"md\".\"Reference2_entity\"");
		metamodel.registerProperty(gen.model.md.Reference2.class, "getURI", "\"URI\"");
		metamodel.registerProperty(gen.model.md.Reference2.class, "getX", "\"x\"");
		adt$converter$AuthConverter.configure(container);
		adt$converter$BasicSecurityConverter.configure(container);
		metamodel.registerDataSource(gen.model.adt.BasicSecurity.class, "\"adt\".\"BasicSecurity\"");
		metamodel.registerProperty(gen.model.adt.BasicSecurity.class, "getUsername", "\"username\"");
		metamodel.registerProperty(gen.model.adt.BasicSecurity.class, "getPassword", "\"password\"");
		adt$converter$TokenConverter.configure(container);
		metamodel.registerDataSource(gen.model.adt.Token.class, "\"adt\".\"Token\"");
		metamodel.registerProperty(gen.model.adt.Token.class, "getToken", "\"token\"");
		adt$converter$AnonymousConverter.configure(container);
		metamodel.registerDataSource(gen.model.adt.Anonymous.class, "\"adt\".\"Anonymous\"");
		adt$converter$DigestSecurityConverter.configure(container);
		metamodel.registerDataSource(gen.model.adt.DigestSecurity.class, "\"adt\".\"DigestSecurity\"");
		metamodel.registerProperty(gen.model.adt.DigestSecurity.class, "getUsername", "\"username\"");
		metamodel.registerProperty(gen.model.adt.DigestSecurity.class, "getPasswordHash", "\"passwordHash\"");
		adt$converter$UserConverter.configure(container);
		metamodel.registerDataSource(gen.model.adt.User.class, "\"adt\".\"User_entity\"");
		metamodel.registerProperty(gen.model.adt.User.class, "getURI", "\"URI\"");
		
		container.register(gen.model.adt.repositories.UserRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.adt.User>>(){}.type, gen.model.adt.repositories.UserRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.adt.User>>(){}.type, gen.model.adt.repositories.UserRepository::new, false);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.postgres.BulkRepository<gen.model.adt.User>>(){}.type, gen.model.adt.repositories.UserRepository::new, false);
		metamodel.registerProperty(gen.model.adt.User.class, "getUsername", "\"username\"");
		metamodel.registerProperty(gen.model.adt.User.class, "getAuthentication", "\"authentication\"");
		calc$converter$InfoConverter.configure(container);
		metamodel.registerDataSource(gen.model.calc.Info.class, "\"calc\".\"Info_entity\"");
		metamodel.registerProperty(gen.model.calc.Info.class, "getURI", "\"URI\"");
		
		container.register(gen.model.calc.repositories.InfoRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.calc.Info>>(){}.type, gen.model.calc.repositories.InfoRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.calc.Info>>(){}.type, gen.model.calc.repositories.InfoRepository::new, false);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.postgres.BulkRepository<gen.model.calc.Info>>(){}.type, gen.model.calc.repositories.InfoRepository::new, false);
		metamodel.registerProperty(gen.model.calc.Info.class, "getCode", "\"code\"");
		metamodel.registerProperty(gen.model.calc.Info.class, "getName", "\"name\"");
		calc$converter$TypeConverter.configure(container);
		metamodel.registerDataSource(gen.model.calc.Type.class, "\"calc\".\"Type_entity\"");
		metamodel.registerProperty(gen.model.calc.Type.class, "getURI", "\"URI\"");
		
		container.register(gen.model.calc.repositories.TypeRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.calc.Type>>(){}.type, gen.model.calc.repositories.TypeRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.calc.Type>>(){}.type, gen.model.calc.repositories.TypeRepository::new, false);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.postgres.BulkRepository<gen.model.calc.Type>>(){}.type, gen.model.calc.repositories.TypeRepository::new, false);
		metamodel.registerProperty(gen.model.calc.Type.class, "getSuffix", "\"suffix\"");
		metamodel.registerProperty(gen.model.calc.Type.class, "getDescription", "\"description\"");
		metamodel.registerProperty(gen.model.calc.Type.class, "getXml", "\"xml\"");
		calc$converter$RealmConverter.configure(container);
		metamodel.registerDataSource(gen.model.calc.Realm.class, "\"calc\".\"Realm_entity\"");
		metamodel.registerProperty(gen.model.calc.Realm.class, "getURI", "\"URI\"");
		
		container.register(gen.model.calc.repositories.RealmRepository.class);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.SearchableRepository<gen.model.calc.Realm>>(){}.type, gen.model.calc.repositories.RealmRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.Repository<gen.model.calc.Realm>>(){}.type, gen.model.calc.repositories.RealmRepository::new, false);
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.postgres.BulkRepository<gen.model.calc.Realm>>(){}.type, gen.model.calc.repositories.RealmRepository::new, false);
		metamodel.registerProperty(gen.model.calc.Realm.class, "getInfo", "\"info\"");
		metamodel.registerProperty(gen.model.calc.Realm.class, "getInfoID", "\"infoID\"");
		metamodel.registerProperty(gen.model.calc.Realm.class, "getRefType", "\"refType\"");
		metamodel.registerProperty(gen.model.calc.Realm.class, "getType", "\"type\"");
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.PersistableRepository<gen.model.test.LazyLoad>>(){}.type, gen.model.test.repositories.LazyLoadRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.PersistableRepository<gen.model.test.SingleDetail>>(){}.type, gen.model.test.repositories.SingleDetailRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.PersistableRepository<gen.model.test.Composite>>(){}.type, gen.model.test.repositories.CompositeRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.PersistableRepository<gen.model.Seq.Next>>(){}.type, gen.model.Seq.repositories.NextRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.PersistableRepository<gen.model.mixinReference.SpecificReport>>(){}.type, gen.model.mixinReference.repositories.SpecificReportRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.PersistableRepository<gen.model.mixinReference.Author>>(){}.type, gen.model.mixinReference.repositories.AuthorRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.PersistableRepository<gen.model.mixinReference.UserFilter>>(){}.type, gen.model.mixinReference.repositories.UserFilterRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.PersistableRepository<gen.model.binaries.Document>>(){}.type, gen.model.binaries.repositories.DocumentRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.PersistableRepository<gen.model.security.Document>>(){}.type, gen.model.security.repositories.DocumentRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.PersistableRepository<gen.model.egzotics.pks>>(){}.type, gen.model.egzotics.repositories.pksRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.PersistableRepository<gen.model.egzotics.PksV>>(){}.type, gen.model.egzotics.repositories.PksVRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.PersistableRepository<gen.model.issues.DateList>>(){}.type, gen.model.issues.repositories.DateListRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.PersistableRepository<gen.model.md.Master>>(){}.type, gen.model.md.repositories.MasterRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.PersistableRepository<gen.model.adt.User>>(){}.type, gen.model.adt.repositories.UserRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.PersistableRepository<gen.model.calc.Info>>(){}.type, gen.model.calc.repositories.InfoRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.PersistableRepository<gen.model.calc.Type>>(){}.type, gen.model.calc.repositories.TypeRepository::new, false);
		
		container.registerFactory(new org.revenj.patterns.Generic<org.revenj.patterns.PersistableRepository<gen.model.calc.Realm>>(){}.type, gen.model.calc.repositories.RealmRepository::new, false);
		metamodel.registerProperty(gen.model.test.Entity.class, "getCompositeid", "\"Compositeid\"");
		metamodel.registerProperty(gen.model.test.Entity.class, "getIndex", "\"Index\"");
		metamodel.registerProperty(gen.model.test.Detail1.class, "getEntityCompositeid", "\"EntityCompositeid\"");
		metamodel.registerProperty(gen.model.test.Detail1.class, "getEntityIndex", "\"EntityIndex\"");
		metamodel.registerProperty(gen.model.test.Detail1.class, "getIndex", "\"Index\"");
		metamodel.registerProperty(gen.model.test.Detail2.class, "getEntityCompositeid", "\"EntityCompositeid\"");
		metamodel.registerProperty(gen.model.test.Detail2.class, "getEntityIndex", "\"EntityIndex\"");
		metamodel.registerProperty(gen.model.test.Detail2.class, "getIndex", "\"Index\"");
		metamodel.registerProperty(gen.model.mixinReference.Person.class, "getAuthorID", "\"AuthorID\"");
		metamodel.registerProperty(gen.model.mixinReference.Child.class, "getAuthorID", "\"AuthorID\"");
		metamodel.registerProperty(gen.model.mixinReference.Child.class, "getIndex", "\"Index\"");
		metamodel.registerProperty(gen.model.md.Child1.class, "getDetailid", "\"Detailid\"");
		metamodel.registerProperty(gen.model.md.Child1.class, "getIndex", "\"Index\"");
		metamodel.registerProperty(gen.model.md.Child2.class, "getDetailid", "\"Detailid\"");
		metamodel.registerProperty(gen.model.md.Child2.class, "getIndex", "\"Index\"");
		metamodel.registerProperty(gen.model.md.Reference1.class, "getDetailid", "\"Detailid\"");
		metamodel.registerProperty(gen.model.md.Reference2.class, "getDetailid", "\"Detailid\"");
		metamodel.registerProperty(gen.model.mixinReference.SpecificReport.class, "getAuthor", "\"author\"");
		metamodel.registerProperty(gen.model.mixinReference.SpecificReport.class, "getAuthorID", "\"authorID\"");
		metamodel.registerProperty(gen.model.security.Document.class, "getDeactivated", "\"deactivated\"");
		
		gen.model.security.Document.configureStaticMEANING_OF_LIFE(container);
		metamodel.registerStatic(gen.model.security.Document.class, "MEANING_OF_LIFE", "\"security\".\"Document.MEANING_OF_LIFE\"");
	}
	
	private java.util.List<org.revenj.postgres.ObjectConverter.ColumnInfo> loadQueryInfo(
			org.revenj.extensibility.Container container,
			String query,
			String module,
			String name) throws java.io.IOException {
		java.util.List<org.revenj.postgres.ObjectConverter.ColumnInfo> columns = new java.util.ArrayList<>();
		try (java.sql.Connection connection = container.resolve(javax.sql.DataSource.class).getConnection();
				java.sql.Statement statement = connection.createStatement();
				java.sql.ResultSet rs = statement.executeQuery(query)) {
			int cols = rs.getMetaData().getColumnCount();
			for(int i = 0; i < cols; i++) {
				String[] columnType = rs.getMetaData().getColumnTypeName(i + 1).split("\\.");
				columns.add(
						new org.revenj.postgres.ObjectConverter.ColumnInfo(
								module,
								name,
								rs.getMetaData().getColumnLabel(i + 1),
								columnType.length == 1 ? "pg_catalog" : columnType[0],
								columnType[columnType.length - 1],
								(short)(i + 1),
								false,
								false
						)
				);
			}
		} catch (java.sql.SQLException e) {
			throw new java.io.IOException(e);
		}
		return columns;
	}

}
