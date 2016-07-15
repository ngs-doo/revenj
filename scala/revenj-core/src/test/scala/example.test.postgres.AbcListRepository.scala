package example.test.postgres




class AbcListRepository(
	   transactionConnection: Option[java.sql.Connection],
	   dataSource: javax.sql.DataSource,
	   implicit private val converter: example.test.postgres.AbcListConverter,
	   implicit private val context: scala.concurrent.ExecutionContext,
	   implicit private val locator: net.revenj.patterns.ServiceLocator
	) extends java.io.Closeable with net.revenj.patterns.SearchableRepository[example.test.AbcList] with net.revenj.patterns.Repository[example.test.AbcList] {
	
	
	
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
		this(locator.resolve[Option[java.sql.Connection]], locator.resolve[javax.sql.DataSource], locator.resolve[example.test.postgres.AbcListConverter], locator.resolve[scala.concurrent.ExecutionContext], locator)
	}
	

	private def readFromDb(statement: java.sql.PreparedStatement, buffer: scala.collection.mutable.ArrayBuffer[example.test.AbcList]): IndexedSeq[example.test.AbcList] = {
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

	override def search(specification: Option[net.revenj.patterns.Specification[example.test.AbcList]], limit: Option[Int], offset: Option[Int]): scala.concurrent.Future[IndexedSeq[example.test.AbcList]] = {
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
					readFromDb(statement, new scala.collection.mutable.ArrayBuffer[example.test.AbcList]())
				} finally {
					statement.close()
				}
			} finally {
				releaseConnection(connection)
			}
		}
	}

	override def count(specification: Option[net.revenj.patterns.Specification[example.test.AbcList]]): scala.concurrent.Future[Long] = {
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

	override def exists(specification: Option[net.revenj.patterns.Specification[example.test.AbcList]]): scala.concurrent.Future[Boolean] = {
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

	
	override def find(uris: Seq[String]): scala.concurrent.Future[IndexedSeq[example.test.AbcList]] = {
		scala.concurrent.Future {
			val connection = getConnection()
			try {
				val statement = connection.createStatement()
				val reader = net.revenj.database.postgres.PostgresReader.create(locator)
				val result = new scala.collection.mutable.ArrayBuffer[example.test.AbcList](uris.size)
				val sb = new StringBuilder("""SELECT _s FROM "test"."AbcList" _s WHERE _s."URI" IN (""")
				net.revenj.database.postgres.PostgresWriter.writeSimpleUriList(sb, uris.toArray)
				sb.append(")")
				statement.setEscapeProcessing(false)
				val rs = statement.executeQuery(sb.toString())
				try {
					while (rs.next()) {
						reader.process(rs.getString(1))
						result += converter.parse(reader, 0)
					}
				} finally {
					rs.close()
				}
				
				result.toIndexedSeq
			} finally { 
				releaseConnection(connection)
			}
		}
	}

}

private object AbcListRepository{

	
		private val hasCustomSecurity = false
}
