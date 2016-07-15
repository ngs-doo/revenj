package example.test.postgres




class AbcWriteRepository(
	   transactionConnection: Option[java.sql.Connection],
	   dataSource: javax.sql.DataSource,
	   implicit private val converter: example.test.postgres.AbcWriteConverter,
	   implicit private val context: scala.concurrent.ExecutionContext,
	   implicit private val locator: net.revenj.patterns.ServiceLocator
	) extends java.io.Closeable with net.revenj.patterns.SearchableRepository[example.test.AbcWrite] with net.revenj.patterns.PersistableRepository[example.test.AbcWrite] {
	
	
	
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
		this(locator.resolve[Option[java.sql.Connection]], locator.resolve[javax.sql.DataSource], locator.resolve[example.test.postgres.AbcWriteConverter], locator.resolve[scala.concurrent.ExecutionContext], locator)
	}
	

	private def readFromDb(statement: java.sql.PreparedStatement, buffer: scala.collection.mutable.ArrayBuffer[example.test.AbcWrite]): IndexedSeq[example.test.AbcWrite] = {
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

	override def search(specification: Option[net.revenj.patterns.Specification[example.test.AbcWrite]], limit: Option[Int], offset: Option[Int]): scala.concurrent.Future[IndexedSeq[example.test.AbcWrite]] = {
		scala.concurrent.Future {
			val selectType = "SELECT it"
			val applyFilters = new scala.collection.mutable.ArrayBuffer[java.sql.PreparedStatement => Unit]()
			val connection = getConnection()
			try {
				val pgWriter = net.revenj.database.postgres.PostgresWriter.create()
				var sql: String = null
				if (specification == null || specification.isEmpty) {
					sql = """SELECT r FROM "test"."Abc" r"""
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
					readFromDb(statement, new scala.collection.mutable.ArrayBuffer[example.test.AbcWrite]())
				} finally {
					statement.close()
				}
			} finally {
				releaseConnection(connection)
			}
		}
	}

	override def count(specification: Option[net.revenj.patterns.Specification[example.test.AbcWrite]]): scala.concurrent.Future[Long] = {
		scala.concurrent.Future {
			val selectType = "SELECT COUNT(*)"
			val applyFilters = new scala.collection.mutable.ArrayBuffer[java.sql.PreparedStatement => Unit]()
			val connection = getConnection()
			try {
				val pgWriter = net.revenj.database.postgres.PostgresWriter.create()
				var sql: String = null
				if (specification == null || specification.isEmpty) {
					sql = """SELECT COUNT(*) FROM "test"."Abc" r"""
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

	override def exists(specification: Option[net.revenj.patterns.Specification[example.test.AbcWrite]]): scala.concurrent.Future[Boolean] = {
		scala.concurrent.Future {
			val selectType = "SELECT exists(SELECT *"
			val applyFilters = new scala.collection.mutable.ArrayBuffer[java.sql.PreparedStatement => Unit]()
			val connection = getConnection()
			try {
				val pgWriter = net.revenj.database.postgres.PostgresWriter.create()
				var sql: String = null
				if (specification == null || specification.isEmpty) {
					sql = """SELECT exists(SELECT * FROM "test"."Abc" r"""
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

	
	override def find(uris: Seq[String]): scala.concurrent.Future[IndexedSeq[example.test.AbcWrite]] = {
		scala.concurrent.Future {
			val connection = getConnection()
			try {
				val statement = connection.createStatement()
				val reader = net.revenj.database.postgres.PostgresReader.create(locator)
				val result = new scala.collection.mutable.ArrayBuffer[example.test.AbcWrite](uris.size)
				val sb = new StringBuilder("""SELECT _r FROM "test"."Abc" _r WHERE _r."ID" IN (""")
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
			insert: Seq[example.test.AbcWrite],
			update: Seq[(example.test.AbcWrite, example.test.AbcWrite)],
			delete: Seq[example.test.AbcWrite]): scala.concurrent.Future[IndexedSeq[String]] = {
		scala.concurrent.Future {
			val connection = getConnection()
			try {
				val statement = connection.prepareStatement("""WITH ins AS (INSERT INTO "test"."Abc" SELECT * FROM unnest(?)), upd AS (UPDATE "test"."Abc" AS _t SET "ID" = (_sq._new)."ID", "s" = (_sq._new)."s", "ii" = (_sq._new)."ii", "en" = (_sq._new)."en", "en2" = (_sq._new)."en2", "en3" = (_sq._new)."en3", "i4" = (_sq._new)."i4", "another" = (_sq._new)."another", "v" = (_sq._new)."v", "vv" = (_sq._new)."vv", "iii" = (_sq._new)."iii", "iiii" = (_sq._new)."iiii", "ss" = (_sq._new)."ss", "vvv" = (_sq._new)."vvv", "a" = (_sq._new)."a", "sss" = (_sq._new)."sss", "ssss" = (_sq._new)."ssss" FROM (SELECT unnest(?) as _old, unnest(?) as _new) _sq  WHERE _t."ID" = (_sq._old)."ID") DELETE FROM "test"."Abc" WHERE ("ID") IN (SELECT "ID" FROM unnest(?))""")
				val sw = net.revenj.database.postgres.PostgresWriter.create()
				val result = new scala.collection.mutable.ArrayBuffer[String](insert.size)
				if (insert != null && insert.nonEmpty) {
					example.test.AbcWrite.insertLoop(insert, sw, locator, converter, connection)
					sw.reset()
					val tuple = net.revenj.database.postgres.converters.ArrayTuple.createSeq(insert, converter.toTuple)
					val pgo = new org.postgresql.util.PGobject
					pgo.setType(""""test"."Abc"[]""")
					sw.reset()
					tuple.buildTuple(sw, quote = false)
					pgo.setValue(sw.toString)
					statement.setObject(1, pgo)
					val iter = insert.iterator
					while (iter.hasNext) {
						val it = iter.next()
						result += it.URI
					}
				} else {
					statement.setObject(1, AbcWriteRepository.EMPTY_PGO)
				}
				if (update != null && update.nonEmpty) {
					val oldUpdate = new Array[example.test.AbcWrite](update.size)
					val newUpdate = new Array[example.test.AbcWrite](update.size)
					val missing = new scala.collection.mutable.HashMap[String, Int]()
					var cnt = 0
					update foreach { case (oldIt, newIt) =>
						oldUpdate(cnt) = if (oldIt != null) oldIt else newIt
						newUpdate(cnt) = newIt
						cnt += 1
					}
					example.test.AbcWrite.updateLoop(oldUpdate, newUpdate, sw, locator, converter)
					val tupleOld = net.revenj.database.postgres.converters.ArrayTuple.createSeq(oldUpdate, converter.toTuple)
					val tupleNew = net.revenj.database.postgres.converters.ArrayTuple.createSeq(newUpdate, converter.toTuple)
					val pgOld = new org.postgresql.util.PGobject
					val pgNew = new org.postgresql.util.PGobject
					pgOld.setType(""""test"."Abc"[]""")
					pgNew.setType(""""test"."Abc"[]""")
					tupleOld.buildTuple(sw, quote = false)
					pgOld.setValue(sw.toString)
					sw.reset()
					tupleNew.buildTuple(sw, quote = false)
					pgNew.setValue(sw.toString)
					sw.reset()
					statement.setObject(2, pgOld)
					statement.setObject(3, pgNew)
				} else {
					statement.setObject(2, AbcWriteRepository.EMPTY_PGO)
					statement.setObject(3, AbcWriteRepository.EMPTY_PGO)
				}
				if (delete != null && delete.nonEmpty) {
					example.test.AbcWrite.deleteLoop(delete, locator)
					val tuple = net.revenj.database.postgres.converters.ArrayTuple.createSeq(delete, converter.toTuple)
					val pgo = new org.postgresql.util.PGobject
					pgo.setType(""""test"."Abc"[]""")
					tuple.buildTuple(sw, quote = false)
					pgo.setValue(sw.toString)
					statement.setObject(4, pgo)
				} else {
					statement.setObject(4, AbcWriteRepository.EMPTY_PGO)
				}
				statement.executeUpdate()
				
				result
			} finally { 
				releaseConnection(connection)
			}
		}
	}

}

private object AbcWriteRepository{

	
		private val hasCustomSecurity = false
		
	private val EMPTY_PGO = {
		val pgo = new org.postgresql.util.PGobject
		pgo.setType(""""test"."Abc"[]""")
		pgo.setValue("""{}""")
		pgo
	}
}
