package example.test.postgres




class AbcSqlRepository(
	   transactionConnection: Option[java.sql.Connection],
	   dataSource: javax.sql.DataSource,
	   implicit private val converter: example.test.postgres.AbcSqlConverter,
	   implicit private val context: scala.concurrent.ExecutionContext,
	   implicit private val locator: net.revenj.patterns.ServiceLocator
	) extends java.io.Closeable with net.revenj.patterns.SearchableRepository[example.test.AbcSql] {
	
	
	
	private def getConnection() = {
		if (transactionConnection.isDefined) transactionConnection.get
		else {
			
			dataSource.getConnection()
		}
	}

	private def releaseConnection(connection: java.sql.Connection): Unit = {
		if (transactionConnection.isEmpty) {
			
			connection.close()
		}		
	}

	def this(locator: net.revenj.patterns.ServiceLocator) {
		this(locator.resolve[Option[java.sql.Connection]], locator.resolve[javax.sql.DataSource], locator.resolve[example.test.postgres.AbcSqlConverter], locator.resolve[scala.concurrent.ExecutionContext], locator)
	}
	

	private def readFromDb(statement: java.sql.PreparedStatement, buffer: scala.collection.mutable.ArrayBuffer[example.test.AbcSql]): IndexedSeq[example.test.AbcSql] = {
		val rs = statement.executeQuery()
		val reader = net.revenj.database.postgres.PostgresReader.create(locator)
		try {
			while (rs.next()) {
				reader.process(rs.getString(1))
				buffer += converter.parse(reader, 0)
			}
		} finally {
			rs.close()
			reader.close()
		}
		
		buffer.toIndexedSeq
	}

	override def search(specification: Option[net.revenj.patterns.Specification[example.test.AbcSql]], limit: Option[Int], offset: Option[Int]): scala.concurrent.Future[IndexedSeq[example.test.AbcSql]] = {
		scala.concurrent.Future {
			val selectType = "SELECT it"
			val applyFilters = new scala.collection.mutable.ArrayBuffer[java.sql.PreparedStatement => Unit]()
			val connection = getConnection()
			try {
				val pgWriter = net.revenj.database.postgres.PostgresWriter.create()
				var sql: String = null
				if (specification == null || specification.isEmpty) {
					sql = """SELECT r FROM "test"."AbcList" r"""
				} 
				else {
					throw new RuntimeException("Unable to search. Unrecognized specification")
				}
				if (limit != null && limit.isDefined) {
					sql += " LIMIT " + limit.get.toString
				}
				if (offset != null && offset.isDefined) {
					sql += " OFFSET " + offset.get.toString
				}
				val statement = connection.prepareStatement(sql)
				applyFilters.foreach(_(statement))
				try {
					readFromDb(statement, new scala.collection.mutable.ArrayBuffer[example.test.AbcSql]())
				} finally {
					statement.close()
				}
			} finally {
				releaseConnection(connection)
			}
		}
	}

	override def count(specification: Option[net.revenj.patterns.Specification[example.test.AbcSql]]): scala.concurrent.Future[Long] = {
		scala.concurrent.Future {
			val selectType = "SELECT COUNT(*)"
			val applyFilters = new scala.collection.mutable.ArrayBuffer[java.sql.PreparedStatement => Unit]()
			val connection = getConnection()
			try {
				val pgWriter = net.revenj.database.postgres.PostgresWriter.create()
				var sql: String = null
				if (specification == null || specification.isEmpty) {
					sql = """SELECT COUNT(*) FROM "test"."AbcList" r"""
				} 
				else {
					throw new RuntimeException("Unable to count. Unrecognized specification")
				}
				val statement = connection.prepareStatement(sql)
				applyFilters.foreach(_(statement))
				val rs = statement.executeQuery()
				try {
					rs.next()
					rs.getLong(1)
				} finally {
					rs.close()
				}
			} finally { 
				releaseConnection(connection)
			}
		}
	}

	override def exists(specification: Option[net.revenj.patterns.Specification[example.test.AbcSql]]): scala.concurrent.Future[Boolean] = {
		scala.concurrent.Future {
			val selectType = "SELECT exists(SELECT *"
			val applyFilters = new scala.collection.mutable.ArrayBuffer[java.sql.PreparedStatement => Unit]()
			val connection = getConnection()
			try {
				val pgWriter = net.revenj.database.postgres.PostgresWriter.create()
				var sql: String = null
				if (specification == null || specification.isEmpty) {
					sql = """SELECT exists(SELECT * FROM "test"."AbcList" r"""
				} 
				else {
					throw new RuntimeException("Unable to check. Unrecognized specification")
				}
				val statement = connection.prepareStatement(sql + ")")
				applyFilters.foreach(_(statement))
				val rs = statement.executeQuery()
				try {
					rs.next()
					rs.getBoolean(1)
				} finally {
					rs.close()
				}
			} finally { 
				releaseConnection(connection)
			}
		}
	}

	override def close() { 
	}

}

private object AbcSqlRepository{

	
		private val hasCustomSecurity = false
}
