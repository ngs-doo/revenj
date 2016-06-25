package example.test.postgres


import net.revenj.patterns._
import net.revenj.database.postgres._
import net.revenj.database.postgres.converters._

class Ent2Converter(allColumns: List[net.revenj.database.postgres.ColumnInfo], container: net.revenj.extensibility.Container) extends Converter[example.test.Ent2] {

	private val columns = allColumns.filter(it => it.typeSchema == "test" && it.typeName == "Ent2_entity")
	private val extendedColumns = allColumns.filter(it => it.typeSchema == "test" && it.typeName == "-ngs_Ent2_type-")

	val dbName = """"test"."Ent2_entity""""
	def default() = null

	private val columnCount = columns.size
	private val readers = new Array[(example.test.Ent2, PostgresReader, Int) => example.test.Ent2](if (columnCount == 0) 1 else columnCount)
	private val extendedColumnCount = extendedColumns.size
	private val extendedReaders = new Array[(example.test.Ent2, PostgresReader, Int) => example.test.Ent2](if (extendedColumnCount == 0) 1 else extendedColumnCount)

	for (i <- readers.indices) {
		readers(i) = (it, rdr, ctx) => { StringConverter.skip(rdr, ctx); it }
	}
	for (i <- extendedReaders.indices) {
		extendedReaders(i) = (it, rdr, ctx) => { StringConverter.skip(rdr, ctx); it }
	}

	container.registerInstance(this, handleClose = false)
	container.registerInstance[Converter[example.test.Ent2]](this, handleClose = false)

	override def parseCollectionItem(reader: PostgresReader, context: Int): example.test.Ent2 = {
		val cur = reader.read()
		if (cur == 'N') {
		  reader.read(4)
		  null
		} else {
		  from(reader, 0, context)
		}		
	}
	override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[example.test.Ent2] = {
		val cur = reader.read()
		if (cur == 'N') {
		  reader.read(4)
		  None
		} else {
		  Some(from(reader, 0, context))
		}
	}

	override def toTuple(item: example.test.Ent2): PostgresTuple = {
		val items = new Array[PostgresTuple](columnCount) 
		items(fPos) = net.revenj.database.postgres.converters.FloatConverter.toTuple(item.f)
		items(AbcIDPos) = net.revenj.database.postgres.converters.IntConverter.toTuple(item.AbcID)
		items(IndexPos) = net.revenj.database.postgres.converters.IntConverter.toTuple(item.Index)
		RecordTuple(items)
	}

	def toTupleExtended(item: example.test.Ent2): PostgresTuple = {
		val items = new Array[PostgresTuple](extendedColumnCount) 
		items(fPosExtended) = net.revenj.database.postgres.converters.FloatConverter.toTuple(item.f)
		items(AbcIDPosExtended) = net.revenj.database.postgres.converters.IntConverter.toTuple(item.AbcID)
		items(IndexPosExtended) = net.revenj.database.postgres.converters.IntConverter.toTuple(item.Index)
		RecordTuple(items)
	}

	def parseExtended(reader: PostgresReader, context: Int): example.test.Ent2 = {
		val cur = reader.read()
		if (cur == ',' || cur == ')') {
			default()
		} else {
			val result = fromExtended(reader, context, if (context == 0) 1 else context << 1)
			reader.read()
			result
		}
	}

	def parseOptionExtended(reader: PostgresReader, context: Int): Option[example.test.Ent2] = {
		val cur = reader.read()
		if (cur == ',' || cur == ')') {
			None
		} else {
			val result = Some(fromExtended(reader, context, if (context == 0) 1 else context << 1))
			reader.read()
			result
		}
	}

	
	private val fPos = columns.find(it => it.columnName == "f") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "f" in type test.Ent2_entity. Check if database is out of sync with code!""")
	}		
	private val fPosExtended = extendedColumns.find(it => it.columnName == "f") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "f" in type test.Ent2. Check if database is out of sync with code!""")
	}		
	private val AbcIDPos = columns.find(it => it.columnName == "AbcID") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "AbcID" in type test.Ent2_entity. Check if database is out of sync with code!""")
	}		
	private val AbcIDPosExtended = extendedColumns.find(it => it.columnName == "AbcID") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "AbcID" in type test.Ent2. Check if database is out of sync with code!""")
	}		
	private val IndexPos = columns.find(it => it.columnName == "Index") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "Index" in type test.Ent2_entity. Check if database is out of sync with code!""")
	}		
	private val IndexPosExtended = extendedColumns.find(it => it.columnName == "Index") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "Index" in type test.Ent2. Check if database is out of sync with code!""")
	}		
	
	def buildURI(_sw: net.revenj.database.postgres.PostgresBuffer, instance: example.test.Ent2): String = {
		buildURI(instance.AbcID, instance.Index, _sw)
	}
	def buildURI(AbcID: Int, Index: Int, _sw: net.revenj.database.postgres.PostgresBuffer): String = {
		_sw.initBuffer()
		val _tmp: String = null
		
		net.revenj.database.postgres.converters.IntConverter.serializeURI(_sw, AbcID)
		_sw.addToBuffer('/')
		net.revenj.database.postgres.converters.IntConverter.serializeURI(_sw, Index)
		_sw.bufferToString()
	}

	
	override def parseRaw(reader: PostgresReader, start: Int, context: Int): example.test.Ent2 = {
		val result = from(reader, context, if (context == 0) 1 else context << 1)
		reader.read()
		result
	}

	def from(reader: PostgresReader, outerContext: Int, context: Int): example.test.Ent2 = {
		reader.read(outerContext)
		val result = example.test.Ent2.buildInternal(reader, context, this, readers)
		reader.read(outerContext)
		result
	}

	def parseRawExtended(reader: PostgresReader, start: Int, context: Int): example.test.Ent2 = {
		val result = fromExtended(reader, context, if (context == 0) 1 else context << 1)
		reader.read()
		result
	}

	def fromExtended(reader: PostgresReader, outerContext: Int, context: Int): example.test.Ent2 = {
		reader.read(outerContext)
		val result = example.test.Ent2.buildInternal(reader, context, this, extendedReaders)
		reader.read(outerContext)
		result
	}

	def initialize():Unit = {
		
	
		example.test.Ent2.configureConverters(readers, fPos, AbcIDPos, IndexPos)
		example.test.Ent2.configureExtendedConverters(extendedReaders, fPosExtended, AbcIDPosExtended, IndexPosExtended)
	}
}
