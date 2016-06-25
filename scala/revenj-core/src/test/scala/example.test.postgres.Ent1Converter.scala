package example.test.postgres


import net.revenj.patterns._
import net.revenj.database.postgres._
import net.revenj.database.postgres.converters._

class Ent1Converter(allColumns: List[net.revenj.database.postgres.ColumnInfo], container: net.revenj.extensibility.Container) extends Converter[example.test.Ent1] {

	private val columns = allColumns.filter(it => it.typeSchema == "test" && it.typeName == "Ent1_entity")
	private val extendedColumns = allColumns.filter(it => it.typeSchema == "test" && it.typeName == "-ngs_Ent1_type-")

	val dbName = """"test"."Ent1_entity""""
	def default() = null

	private val columnCount = columns.size
	private val readers = new Array[(example.test.Ent1, PostgresReader, Int) => example.test.Ent1](if (columnCount == 0) 1 else columnCount)
	private val extendedColumnCount = extendedColumns.size
	private val extendedReaders = new Array[(example.test.Ent1, PostgresReader, Int) => example.test.Ent1](if (extendedColumnCount == 0) 1 else extendedColumnCount)

	for (i <- readers.indices) {
		readers(i) = (it, rdr, ctx) => { StringConverter.skip(rdr, ctx); it }
	}
	for (i <- extendedReaders.indices) {
		extendedReaders(i) = (it, rdr, ctx) => { StringConverter.skip(rdr, ctx); it }
	}

	container.registerInstance(this, handleClose = false)
	container.registerInstance[Converter[example.test.Ent1]](this, handleClose = false)

	override def parseCollectionItem(reader: PostgresReader, context: Int): example.test.Ent1 = {
		val cur = reader.read()
		if (cur == 'N') {
		  reader.read(4)
		  null
		} else {
		  from(reader, 0, context)
		}		
	}
	override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[example.test.Ent1] = {
		val cur = reader.read()
		if (cur == 'N') {
		  reader.read(4)
		  None
		} else {
		  Some(from(reader, 0, context))
		}
	}

	override def toTuple(item: example.test.Ent1): PostgresTuple = {
		val items = new Array[PostgresTuple](columnCount) 
		items(iPos) = net.revenj.database.postgres.converters.IntConverter.toTuple(item.i)
		items(AbcIDPos) = net.revenj.database.postgres.converters.IntConverter.toTuple(item.AbcID)
		RecordTuple(items)
	}

	def toTupleExtended(item: example.test.Ent1): PostgresTuple = {
		val items = new Array[PostgresTuple](extendedColumnCount) 
		items(iPosExtended) = net.revenj.database.postgres.converters.IntConverter.toTuple(item.i)
		items(AbcIDPosExtended) = net.revenj.database.postgres.converters.IntConverter.toTuple(item.AbcID)
		RecordTuple(items)
	}

	def parseExtended(reader: PostgresReader, context: Int): example.test.Ent1 = {
		val cur = reader.read()
		if (cur == ',' || cur == ')') {
			default()
		} else {
			val result = fromExtended(reader, context, if (context == 0) 1 else context << 1)
			reader.read()
			result
		}
	}

	def parseOptionExtended(reader: PostgresReader, context: Int): Option[example.test.Ent1] = {
		val cur = reader.read()
		if (cur == ',' || cur == ')') {
			None
		} else {
			val result = Some(fromExtended(reader, context, if (context == 0) 1 else context << 1))
			reader.read()
			result
		}
	}

	
	private val iPos = columns.find(it => it.columnName == "i") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "i" in type test.Ent1_entity. Check if database is out of sync with code!""")
	}		
	private val iPosExtended = extendedColumns.find(it => it.columnName == "i") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "i" in type test.Ent1. Check if database is out of sync with code!""")
	}		
	private val AbcIDPos = columns.find(it => it.columnName == "AbcID") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "AbcID" in type test.Ent1_entity. Check if database is out of sync with code!""")
	}		
	private val AbcIDPosExtended = extendedColumns.find(it => it.columnName == "AbcID") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "AbcID" in type test.Ent1. Check if database is out of sync with code!""")
	}		
	
	def buildURI(_sw: net.revenj.database.postgres.PostgresBuffer, instance: example.test.Ent1): String = {
		buildURI(instance.AbcID, _sw)
	}
	def buildURI(AbcID: Int, _sw: net.revenj.database.postgres.PostgresBuffer): String = {
		_sw.initBuffer()
		val _tmp: String = null
		
		net.revenj.database.postgres.converters.IntConverter.serializeURI(_sw, AbcID)
		_sw.bufferToString()
	}

	
	override def parseRaw(reader: PostgresReader, start: Int, context: Int): example.test.Ent1 = {
		val result = from(reader, context, if (context == 0) 1 else context << 1)
		reader.read()
		result
	}

	def from(reader: PostgresReader, outerContext: Int, context: Int): example.test.Ent1 = {
		reader.read(outerContext)
		val result = example.test.Ent1.buildInternal(reader, context, this, readers)
		reader.read(outerContext)
		result
	}

	def parseRawExtended(reader: PostgresReader, start: Int, context: Int): example.test.Ent1 = {
		val result = fromExtended(reader, context, if (context == 0) 1 else context << 1)
		reader.read()
		result
	}

	def fromExtended(reader: PostgresReader, outerContext: Int, context: Int): example.test.Ent1 = {
		reader.read(outerContext)
		val result = example.test.Ent1.buildInternal(reader, context, this, extendedReaders)
		reader.read(outerContext)
		result
	}

	def initialize():Unit = {
		
	
		example.test.Ent1.configureConverters(readers, iPos, AbcIDPos)
		example.test.Ent1.configureExtendedConverters(extendedReaders, iPosExtended, AbcIDPosExtended)
	}
}
