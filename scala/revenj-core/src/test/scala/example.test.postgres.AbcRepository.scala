package example.test.postgres




class AbcRepository(
	   transactionConnection: Option[java.sql.Connection],
	   dataSource: javax.sql.DataSource,
	   implicit private val converter: example.test.postgres.AbcConverter,
	   implicit private val context: scala.concurrent.ExecutionContext,
	   implicit private val locator: net.revenj.patterns.ServiceLocator
	) extends java.io.Closeable with net.revenj.patterns.SearchableRepository[example.test.Abc] with net.revenj.patterns.Repository[example.test.Abc] with net.revenj.patterns.PersistableRepository[example.test.Abc] {
	
	
	
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
		this(locator.resolve[Option[java.sql.Connection]], locator.resolve[javax.sql.DataSource], locator.resolve[example.test.postgres.AbcConverter], locator.resolve[scala.concurrent.ExecutionContext], locator)
	}
	

	private def readFromDb(statement: java.sql.PreparedStatement, buffer: scala.collection.mutable.ArrayBuffer[example.test.Abc]): IndexedSeq[example.test.Abc] = {
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

	override def search(specification: Option[net.revenj.patterns.Specification[example.test.Abc]], limit: Option[Int], offset: Option[Int]): scala.concurrent.Future[IndexedSeq[example.test.Abc]] = {
		scala.concurrent.Future {
			val selectType = "SELECT it"
			val applyFilters = new scala.collection.mutable.ArrayBuffer[java.sql.PreparedStatement => Unit]()
			val connection = getConnection()
			try {
				val pgWriter = net.revenj.database.postgres.PostgresWriter.create()
				var sql: String = null
				if (specification == null || specification.isEmpty) {
					sql = """SELECT r FROM "test"."Abc_entity" r"""
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
					readFromDb(statement, new scala.collection.mutable.ArrayBuffer[example.test.Abc]())
				} finally {
					statement.close()
				}
			} finally {
				releaseConnection(connection)
			}
		}
	}

	override def count(specification: Option[net.revenj.patterns.Specification[example.test.Abc]]): scala.concurrent.Future[Long] = {
		scala.concurrent.Future {
			val selectType = "SELECT COUNT(*)"
			val applyFilters = new scala.collection.mutable.ArrayBuffer[java.sql.PreparedStatement => Unit]()
			val connection = getConnection()
			try {
				val pgWriter = net.revenj.database.postgres.PostgresWriter.create()
				var sql: String = null
				if (specification == null || specification.isEmpty) {
					sql = """SELECT COUNT(*) FROM "test"."Abc_entity" r"""
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

	override def exists(specification: Option[net.revenj.patterns.Specification[example.test.Abc]]): scala.concurrent.Future[Boolean] = {
		scala.concurrent.Future {
			val selectType = "SELECT exists(SELECT *"
			val applyFilters = new scala.collection.mutable.ArrayBuffer[java.sql.PreparedStatement => Unit]()
			val connection = getConnection()
			try {
				val pgWriter = net.revenj.database.postgres.PostgresWriter.create()
				var sql: String = null
				if (specification == null || specification.isEmpty) {
					sql = """SELECT exists(SELECT * FROM "test"."Abc_entity" r"""
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

	
	override def find(uris: Seq[String]): scala.concurrent.Future[IndexedSeq[example.test.Abc]] = {
		scala.concurrent.Future {
			val connection = getConnection()
			try {
				val statement = connection.createStatement()
				val reader = net.revenj.database.postgres.PostgresReader.create(locator)
				val result = new scala.collection.mutable.ArrayBuffer[example.test.Abc](uris.size)
				val sb = new StringBuilder("""SELECT _r FROM "test"."Abc_entity" _r WHERE r."ID" IN (""")
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

	

	override def persist(
			insert: Seq[example.test.Abc],
			update: Seq[(example.test.Abc, example.test.Abc)],
			delete: Seq[example.test.Abc]): scala.concurrent.Future[IndexedSeq[String]] = {
		scala.concurrent.Future {
			val connection = getConnection()
			try {
				val statement = connection.prepareStatement("""/*NO LOAD BALANCE*/SELECT "test"."persist_Abc"(?, ?, ?, ?)""")
				val sw = net.revenj.database.postgres.PostgresWriter.create()
				val result = new scala.collection.mutable.ArrayBuffer[String](insert.size)
				if (insert != null && insert.nonEmpty) {
					example.test.Abc.insertLoop(insert, sw, locator, converter, connection)
					sw.reset()
					val tuple = net.revenj.database.postgres.converters.ArrayTuple.createSeq(insert, converter.toTuple)
					val pgo = new org.postgresql.util.PGobject
					pgo.setType(""""test"."Abc_entity"[]""")
					sw.reset()
					tuple.buildTuple(sw, quote = false)
					pgo.setValue(sw.toString)
					statement.setObject(1, pgo)
					var iter = insert.iterator
					while (iter.hasNext) {
						val it = iter.next()
						result += it.URI
						example.test.Abc.trackChanges(it, locator)
					}
				} else {
					statement.setArray(1, null)
				}
				if (update != null && update.nonEmpty) {
					val oldUpdate = new Array[example.test.Abc](update.size)
					val newUpdate = new Array[example.test.Abc](update.size)
					val missing = new scala.collection.mutable.HashMap[String, Int]()
					var cnt = 0
					update foreach { case (oldIt, newIt) =>
						var oldValue = example.test.Abc.trackChanges(newIt, locator)
						if (oldIt != null) {
							oldValue = oldIt
						}
						oldUpdate(cnt) = oldValue
						if (oldValue == null) {
							missing += newIt.URI -> cnt
						}
						newUpdate(cnt) = newIt
						cnt += 1
					}
					if (missing.nonEmpty) {
						val found = scala.concurrent.Await.result(find(missing.keys.toSeq), scala.concurrent.duration.Duration.Inf)
						for (it <- found) {
							oldUpdate.update(missing.get(it.URI).get, it)
						}
					}
					example.test.Abc.updateLoop(oldUpdate, newUpdate, sw, locator, converter)
					val tupleOld = net.revenj.database.postgres.converters.ArrayTuple.createSeq(oldUpdate, converter.toTuple)
					val tupleNew = net.revenj.database.postgres.converters.ArrayTuple.createSeq(newUpdate, converter.toTuple)
					val pgOld = new org.postgresql.util.PGobject
					val pgNew = new org.postgresql.util.PGobject
					pgOld.setType(""""test"."Abc_entity"[]""")
					pgNew.setType(""""test"."Abc_entity"[]""")
					tupleOld.buildTuple(sw, quote = false)
					pgOld.setValue(sw.toString)
					sw.reset()
					tupleNew.buildTuple(sw, quote = false)
					pgNew.setValue(sw.toString)
					sw.reset()
					statement.setObject(2, pgOld)
					statement.setObject(3, pgNew)
				} else {
					statement.setArray(2, null)
					statement.setArray(3, null)
				}
				if (delete != null && delete.nonEmpty) {
					example.test.Abc.deleteLoop(delete, locator)
					val tuple = net.revenj.database.postgres.converters.ArrayTuple.createSeq(delete, converter.toTuple)
					val pgo = new org.postgresql.util.PGobject
					pgo.setType(""""test"."Abc_entity"[]""")
					tuple.buildTuple(sw, quote = false)
					pgo.setValue(sw.toString)
					statement.setObject(4, pgo)
				} else {
					statement.setArray(4, null)
				}
				val rs = statement.executeQuery()
				try {
					rs.next()
					val message = rs.getString(1)
					if (message != null) throw new java.io.IOException(message)
				} finally {
					rs.close()
				}
				
				result
			} finally { 
				releaseConnection(connection)
			}
		}
	}

}

private object AbcRepository{

	
			private val hasCustomSecurity = false
}
