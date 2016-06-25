package example


class Boot extends net.revenj.extensibility.SystemAspect {

	def configure(container: net.revenj.extensibility.Container): Unit = {
		val properties = container.resolve[java.util.Properties]
		val prevNamespace = properties.getProperty("revenj.namespace")
		if (prevNamespace != null && "example" != prevNamespace) {
				throw new java.io.IOException("Different namespace already defined in Properties file. Trying to add namespace=example. Found: " + prevNamespace)
		}
		properties.setProperty("revenj.namespace", "example")
		val columns = {
			try {
				Boot.loadColumnsInfo(container, """SELECT * FROM "-NGS-".load_type_info()""")
			} catch {
				case _ : Throwable => Boot.loadColumnsInfo(container, """SELECT 
	ns.nspname::varchar as type_schema,
	cl.relname::varchar as type_name,
	atr.attname::varchar as column_name,
	ns_ref.nspname::varchar as column_schema,
	typ.typname::varchar as column_type,
	(SELECT COUNT(*) + 1
	FROM pg_attribute atr_ord
	WHERE
		atr.attrelid = atr_ord.attrelid
		AND atr_ord.attisdropped = false
		AND atr_ord.attnum > 0
		AND atr_ord.attnum < atr.attnum)::smallint as column_index,
	atr.attnotnull as is_not_null,
	coalesce(d.description LIKE 'NGS generated%', false) as is_ngs_generated
FROM
	pg_attribute atr
	INNER JOIN pg_class cl ON atr.attrelid = cl.oid
	INNER JOIN pg_namespace ns ON cl.relnamespace = ns.oid
	INNER JOIN pg_type typ ON atr.atttypid = typ.oid
	INNER JOIN pg_namespace ns_ref ON typ.typnamespace = ns_ref.oid
	LEFT JOIN pg_description d ON d.objoid = cl.oid
								AND d.objsubid = atr.attnum
WHERE
	(cl.relkind = 'r' OR cl.relkind = 'v' OR cl.relkind = 'c')
	AND ns.nspname NOT LIKE 'pg_%'
	AND ns.nspname != 'information_schema'
	AND atr.attnum > 0
	AND atr.attisdropped = FALSE
ORDER BY 1, 2, 6""")
			}
		}
		
		
		container.registerInstance[net.revenj.database.postgres.converters.Converter[example.test.En]](example.test.postgres.EnConverter, handleClose = false)
		val converter$test$Abc = new example.test.postgres.AbcConverter(columns, container)
		val converter$test$Ent1 = new example.test.postgres.Ent1Converter(columns, container)
		val converter$test$Ent2 = new example.test.postgres.Ent2Converter(columns, container)
		val converter$test$Ent3 = new example.test.postgres.Ent3Converter(columns, container)
		val converter$test$Val = new example.test.postgres.ValConverter(columns, container)
		val converter$test$Another = new example.test.postgres.AnotherConverter(columns, container)
		converter$test$Abc.initialize()
		converter$test$Ent1.initialize()
		converter$test$Ent2.initialize()
		converter$test$Ent3.initialize()
	}
}

object Boot {

	def configure(jdbcUrl: String): net.revenj.patterns.ServiceLocator = {
		val properties = new java.util.Properties
		val revProps = new java.io.File("revenj.properties")
		if (revProps.exists && revProps.isFile) {
			properties.load(new java.io.FileReader(revProps))
		}
		configure(jdbcUrl, properties)
	}

	def configure(jdbcUrl: String, properties: java.util.Properties): net.revenj.patterns.ServiceLocator = {
		properties.setProperty("revenj.namespace", "example")
		val dataSource = new org.postgresql.ds.PGPoolingDataSource
		dataSource.setUrl(jdbcUrl)
		val user = properties.getProperty("user")
		val revUser = properties.getProperty("revenj.user")
		if (revUser != null && revUser.length > 0) {
			dataSource.setUser(revUser)
		} else if (user != null && user.length > 0) {
			dataSource.setUser(user)
		}
		val password = properties.getProperty("password")
		val revPassword = properties.getProperty("revenj.password")
		if (revPassword != null && revPassword.length > 0) {
			dataSource.setPassword(revPassword)
		} else if (password != null && password.length > 0) {
			dataSource.setPassword(password)
		}
		net.revenj.Revenj.setup(dataSource, properties, None, None, java.util.Collections.singletonList[net.revenj.extensibility.SystemAspect](new Boot).iterator)
	}

	private def loadColumnsInfo(
			container: net.revenj.extensibility.Container,
			query: String): List[net.revenj.database.postgres.ColumnInfo] = {
		val columns = new scala.collection.mutable.ListBuffer[net.revenj.database.postgres.ColumnInfo]()
		val connection = container.resolve[javax.sql.DataSource].getConnection()
		val statement = connection.createStatement
		val rs = statement.executeQuery(query)
		try {
			while (rs.next()) {
				columns += net.revenj.database.postgres.ColumnInfo(
								rs.getString("type_schema"),
								rs.getString("type_name"),
								rs.getString("column_name"),
								rs.getString("column_schema"),
								rs.getString("column_type"),
								rs.getShort("column_index"),
								rs.getBoolean("is_not_null"),
								rs.getBoolean("is_ngs_generated"))
			}
		} finally {
			rs.close()
			statement.close()
			connection.close()
		}
		columns.result
	}
}
