package org.revenj.patterns

import scala.collection.mutable.LinkedHashMap

import org.pgscala._
import org.slf4j.LoggerFactory

class PostgresUtils(sf: PGSessionFactory) {
  private lazy val logger = LoggerFactory.getLogger(getClass)

// -----------------------------------------------------------------------------

  private val typeInfo = loadTypeInfo(sf)

  private def loadNamedTypeInfo(sf: PGSessionFactory) =
    sf.using(
      _.bag("""SELECT * FROM "-NGS-".load_type_info();""")(rS =>
        SchemaElement(
          rS.get[String]("type_schema")
        , rS.get[String]("type_name")
        ) ->
        ElementColumn(
          rS.get[String]("column_name")
        , rS.get[String]("column_type")
        , rS.get[Int]("column_index")
        , rS.get[Boolean]("is_not_null")
        )
      )
    )

  private def loadTypeInfo(sf: PGSessionFactory) = {
    try {
      logger.debug("Loading type info...")
      val typeInfo = loadNamedTypeInfo(sf)

      if (logger.isTraceEnabled) {
        typeInfo.foreach { case (se, cols) =>
          val sb = new StringBuilder(se.name) += '.' ++= se.element ++= ": \n"

          for (c <- cols) {
            (sb ++= "  ") append(c.columnIndex) ++= ": " ++=
            c.columnName ++= " (" ++= c.columnType ++= ")\n"
          }

          logger.trace(sb += '\n' toString)
        }
      }

      typeInfo
    } catch {
      case e: org.postgresql.util.PSQLException =>
        logger.error("Failed trying to load type info", e)
        throw e
    }
  }

  private case class SchemaElement(name: String, element: String)

  private case class ElementColumn(
      columnName: String
    , columnType: String
    , columnIndex: Int
    , isNotNull: Boolean)

// -----------------------------------------------------------------------------

  private def getColumns(schema: String, element: String): IndexedSeq[ElementColumn] =
    typeInfo.get(SchemaElement(schema, element)).getOrElse{
      logger.error(""""Database element "%s"."%s" could not be found!""" format(schema, element))
      IndexedSeq.empty
    }

  def getColumnCount(schema: String, element: String): Int =
    getColumns(schema, element).size

  def getIndexes(schema: String, element: String): Map[String, Int] =
    getColumns(schema, element).map(c => c.columnName -> c.columnIndex).toMap
}
