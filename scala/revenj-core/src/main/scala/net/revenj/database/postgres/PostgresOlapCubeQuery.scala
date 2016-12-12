package net.revenj.database.postgres

import java.sql.{Connection, PreparedStatement, ResultSet}

import net.revenj.patterns.{DataSource, OlapCubeQuery, ServiceLocator, Specification}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ExecutionContext, Future}

abstract class PostgresOlapCubeQuery[T <: DataSource](locator: ServiceLocator) extends OlapCubeQuery[T] {

  protected val reader = new PostgresReader(Some(locator))
  protected lazy val transactionConnection = locator.tryResolve[Connection].toOption
  protected lazy val dataSource = transactionConnection match {
    case Some(_) => None
    case _ => Some(locator.resolve[javax.sql.DataSource])
  }
  protected lazy val loader = locator.resolve[ClassLoader]
  private lazy val executionContext = locator.resolve[ExecutionContext]

  val source: String
  val dimensions: Set[String]
  val facts: Set[String]

  val cubeDimensions: Map[String, Function[String, String]]
  val cubeFacts: Map[String, Function[String, String]]
  val cubeConverters: Map[String, (PostgresReader, Int) => Any]

  private def validateInput(usedDimensions: Seq[String], usedFacts: Seq[String], customOrder: Seq[String]): Unit = {
    if (usedDimensions.isEmpty && usedFacts.isEmpty) throw new IllegalArgumentException("Cube must have at least one dimension or fact.")
    usedDimensions foreach { d =>
      if (!cubeDimensions.contains(d)) throw new IllegalArgumentException(s"Unknown dimension: $d. Use getDimensions method for available dimensions")
    }
    usedFacts foreach { f =>
      if (!cubeFacts.contains(f)) throw new IllegalArgumentException(s"Unknown fact: $f. Use getFacts method for available facts")
    }
    customOrder foreach { o =>
      if (!usedDimensions.contains(o) && !usedFacts.contains(o)) throw new IllegalArgumentException(s"Invalid order: $o. Order can be only field from used dimensions and facts.")
    }
  }

  protected def getConnection(): Connection = {
    transactionConnection match {
      case Some(conn) => conn
      case _ =>
        val ds = dataSource.getOrElse(throw new RuntimeException("Data source not available"))
        try {
          ds.getConnection
        } catch {
          case e: Throwable =>
            throw new RuntimeException(s"Unable to resolve connection for cube query. ${e.getMessage}")
        }
    }
  }

  protected def releaseConnection(connection: Connection): Unit = {
    if (transactionConnection.isEmpty) {
      connection.close()
    }
  }

  protected def handleFilter(sb: StringBuilder, filter: Specification[T], parameters: ArrayBuffer[PreparedStatement => Unit]): Unit = {
    throw new IllegalArgumentException(s"Unable to handle filter: $filter")
  }

  def prepareSql(
    sb: StringBuilder,
    asRecord: Boolean,
    usedDimensions: Seq[String],
    usedFacts: Seq[String],
    order: Seq[(String, Boolean)],
    filter: Option[Specification[T]],
    limit: Option[Int],
    offset: Option[Int],
    parameters: ArrayBuffer[PreparedStatement => Unit]): Unit = {

    validateInput(usedDimensions, usedFacts, order.map(_._1))
    val alias = "_it"
    sb.append("SELECT ")
    if (asRecord) {
      sb.append("ROW(")
    }
    usedDimensions foreach { d =>
      sb.append(cubeDimensions(d)(alias)).append(',')
    }
    usedFacts foreach { f =>
      sb.append(cubeFacts(f)(alias)).append(',')
    }
    sb.setLength(sb.length - 1)
    if (asRecord) {
      sb.append(")")
    }
    sb.append(" ")
    if (filter.isDefined) {
      handleFilter(sb, filter.get, parameters)
    } else {
      sb.append(" FROM ").append(source).append(" \"").append(alias).append("\"")
    }
    if (usedDimensions.nonEmpty) {
      sb.append(" GROUP BY ")
      usedDimensions foreach { d =>
        sb.append(cubeDimensions(d)(alias))
        sb.append(", ")
      }
      sb.setLength(sb.length - 2)
      sb.append('\n')
    }
    if (order.nonEmpty) {
      sb.append(" ORDER BY ")
      order foreach { case (k, v) =>
        cubeDimensions.get(k) match {
          case Some(dim) =>
            sb.append(dim(alias))
          case _ =>
            cubeFacts.get(k) match {
              case Some(fact) =>
                sb.append(fact(alias))
              case _ =>
                sb.append("\"").append(k).append("\"")
            }
        }
        sb.append(if (v) "" else "DESC")
        sb.append(", ")
      }
      sb.setLength(sb.length - 2)
    }
    if (limit.nonEmpty) {
      sb.append(" LIMIT ").append(limit.get)
    }
    if (offset.nonEmpty) {
      sb.append(" OFFSET ").append(offset.get)
    }
  }

  def prepareConverters(usedDimensions: Seq[String], usedFacts: Seq[String]): Array[(PostgresReader, Int) => Any] = {
    ((usedDimensions map cubeConverters.apply) ++ (usedFacts map cubeConverters.apply)).toArray
  }

  override def analyze(
    usedDimensions: Seq[String],
    usedFacts: Seq[String],
    order: Seq[(String, Boolean)],
    filter: Option[Specification[T]],
    limit: Option[Int],
    offset: Option[Int]): Future[IndexedSeq[Map[String, Any]]] = {
    Future {
      val connection = getConnection()
      try {
        analyze(connection, usedDimensions, usedFacts, order, filter, limit, offset)
      } finally {
        releaseConnection(connection)
      }
    }(executionContext)
  }

  def analyzeAsMap(
    connection: Connection,
    usedDimensions: Seq[String],
    usedFacts: Seq[String],
    order: Seq[(String, Boolean)] = Nil,
    filter: Option[Specification[T]] = None,
    limit: Option[Int] = None,
    offset: Option[Int] = None): IndexedSeq[Map[String, Any]] = {

    analyze(
      connection,
      usedDimensions,
      usedFacts,
      order,
      filter,
      limit,
      offset
    )
  }

  def analyze(
    connection: Connection,
    usedDimensions: Seq[String],
    usedFacts: Seq[String],
    order: Seq[(String, Boolean)],
    filter: Option[Specification[T]],
    limit: Option[Int],
    offset: Option[Int]): IndexedSeq[Map[String, Any]] = {

    val sb = new StringBuilder
    val params = new ArrayBuffer[PreparedStatement => Unit]()
    prepareSql(sb, true, usedDimensions, usedFacts, order, filter, limit, offset, params)
    val converters = prepareConverters(usedDimensions, usedFacts)
    val result = new ArrayBuffer[Map[String, Any]]()
    val ps = connection.prepareStatement(sb.toString)
    try {
      params foreach { p => p(ps) }
      val rs = ps.executeQuery()
      val columnNames = (usedDimensions ++ usedFacts).toArray
      while (rs.next()) {
        reader.process(rs.getString(1))
        reader.read()
        val item = new mutable.LinkedHashMap[String, Any]()
        var i = 0
        while (i < columnNames.length) {
          item.put(columnNames(i), converters(i)(reader, 1))
          i += 1
        }
        result.append(item.toMap)
      }
      rs.close()
    } finally {
      ps.close()
    }

    result
  }

  def stream(
    connection: Connection,
    usedDimensions: Seq[String],
    usedFacts: Seq[String],
    order: Seq[(String, Boolean)] = Nil,
    filter: Option[Specification[T]] = None,
    limit: Option[Int] = None,
    offset: Option[Int] = None): ResultSet = {

    val sb = new StringBuilder
    val params = new ArrayBuffer[PreparedStatement => Unit]()
    prepareSql(sb, false, usedDimensions, usedFacts, order, filter, limit, offset, params)
    val ps = connection.prepareStatement(sb.toString)
    params foreach { p => p(ps) }
    ps.executeQuery()
  }

  def analyzeWith[R](
    builder: ResultSet => R,
    connection: Connection,
    usedDimensions: Seq[String],
    usedFacts: Seq[String],
    order: Seq[(String, Boolean)] = Nil,
    filter: Option[Specification[T]] = None,
    limit: Option[Int] = None,
    offset: Option[Int] = None): Seq[R] = {

    analyze(
      builder,
      connection,
      usedDimensions,
      usedFacts,
      order,
      filter,
      limit,
      offset
    )
  }

  def analyze[R](
    builder: ResultSet => R,
    connection: Connection,
    usedDimensions: Seq[String],
    usedFacts: Seq[String],
    order: Seq[(String, Boolean)],
    filter: Option[Specification[T]],
    limit: Option[Int],
    offset: Option[Int]): Seq[R] = {

    val sb = new StringBuilder
    val params = new ArrayBuffer[PreparedStatement => Unit]()
    prepareSql(sb, false, usedDimensions, usedFacts, order, filter, limit, offset, params)
    val ps = connection.prepareStatement(sb.toString)
    params foreach { p => p(ps) }
    val buffer = new ArrayBuffer[R]()
    val rs = ps.executeQuery()
    while (rs.next()) {
      buffer += builder(rs)
    }
    rs.close()
    buffer
  }
}
