package example.test.postgres


import net.revenj.patterns._
import net.revenj.database.postgres._
import net.revenj.database.postgres.converters._

class Ent3Converter(allColumns: List[net.revenj.database.postgres.ColumnInfo], container: net.revenj.extensibility.Container) extends Converter[example.test.Ent3] {

	private val columns = allColumns.filter(it => it.typeSchema == "test" && it.typeName == "Ent3_entity")
	private val extendedColumns = allColumns.filter(it => it.typeSchema == "test" && it.typeName == "-ngs_Ent3_type-")

	val dbName = """"test"."Ent3_entity""""
	def default() = null

	private val columnCount = columns.size
	private val readers = new Array[(example.test.Ent3, PostgresReader, Int) => example.test.Ent3](if (columnCount == 0) 1 else columnCount)
	private val extendedColumnCount = extendedColumns.size
	private val extendedReaders = new Array[(example.test.Ent3, PostgresReader, Int) => example.test.Ent3](if (extendedColumnCount == 0) 1 else extendedColumnCount)

	for (i <- readers.indices) {
		readers(i) = (it, rdr, ctx) => { StringConverter.skip(rdr, ctx); it }
	}
	for (i <- extendedReaders.indices) {
		extendedReaders(i) = (it, rdr, ctx) => { StringConverter.skip(rdr, ctx); it }
	}

	container.registerInstance(this, handleClose = false)
	container.registerInstance[Converter[example.test.Ent3]](this, handleClose = false)

	override def parseCollectionItem(reader: PostgresReader, context: Int): example.test.Ent3 = {
		val cur = reader.read()
		if (cur == 'N') {
		  reader.read(4)
		  null
		} else {
		  from(reader, 0, context)
		}		
	}
	override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[example.test.Ent3] = {
		val cur = reader.read()
		if (cur == 'N') {
		  reader.read(4)
		  None
		} else {
		  Some(from(reader, 0, context))
		}
	}

	override def toTuple(item: example.test.Ent3): PostgresTuple = {
		val items = new Array[PostgresTuple](columnCount) 
		items(idPos) = net.revenj.database.postgres.converters.IntConverter.toTuple(item.id)
		items(iPos) = net.revenj.database.postgres.converters.IntConverter.toTuple(item.i)
		RecordTuple(items)
	}

	def toTupleExtended(item: example.test.Ent3): PostgresTuple = {
		val items = new Array[PostgresTuple](extendedColumnCount) 
		items(idPosExtended) = net.revenj.database.postgres.converters.IntConverter.toTuple(item.id)
		items(iPosExtended) = net.revenj.database.postgres.converters.IntConverter.toTuple(item.i)
		RecordTuple(items)
	}

	def parseExtended(reader: PostgresReader, context: Int): example.test.Ent3 = {
		val cur = reader.read()
		if (cur == ',' || cur == ')') {
			default()
		} else {
			val result = fromExtended(reader, context, if (context == 0) 1 else context << 1)
			reader.read()
			result
		}
	}

	def parseOptionExtended(reader: PostgresReader, context: Int): Option[example.test.Ent3] = {
		val cur = reader.read()
		if (cur == ',' || cur == ')') {
			None
		} else {
			val result = Some(fromExtended(reader, context, if (context == 0) 1 else context << 1))
			reader.read()
			result
		}
	}

	
	
	def buildURI(_sw: net.revenj.database.postgres.PostgresBuffer, instance: example.test.Ent3): String = {
		buildURI(instance.id, _sw)
	}
	def buildURI(id: Int, _sw: net.revenj.database.postgres.PostgresBuffer): String = {
		_sw.initBuffer()
		val _tmp: String = null
		
		net.revenj.database.postgres.converters.IntConverter.serializeURI(_sw, id)
		_sw.bufferToString()
	}
	private val idPos = columns.find(it => it.columnName == "id") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "id" in type test.Ent3_entity. Check if database is out of sync with code!""")
	}		
	private val idPosExtended = extendedColumns.find(it => it.columnName == "id") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "id" in type test.Ent3. Check if database is out of sync with code!""")
	}		
	private val iPos = columns.find(it => it.columnName == "i") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "i" in type test.Ent3_entity. Check if database is out of sync with code!""")
	}		
	private val iPosExtended = extendedColumns.find(it => it.columnName == "i") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "i" in type test.Ent3. Check if database is out of sync with code!""")
	}		

	
	override def parseRaw(reader: PostgresReader, start: Int, context: Int): example.test.Ent3 = {
		val result = from(reader, context, if (context == 0) 1 else context << 1)
		reader.read()
		result
	}

	def from(reader: PostgresReader, outerContext: Int, context: Int): example.test.Ent3 = {
		reader.read(outerContext)
		val result = example.test.Ent3.buildInternal(reader, context, this, readers)
		reader.read(outerContext)
		result
	}

	def parseRawExtended(reader: PostgresReader, start: Int, context: Int): example.test.Ent3 = {
		val result = fromExtended(reader, context, if (context == 0) 1 else context << 1)
		reader.read()
		result
	}

	def fromExtended(reader: PostgresReader, outerContext: Int, context: Int): example.test.Ent3 = {
		reader.read(outerContext)
		val result = example.test.Ent3.buildInternal(reader, context, this, extendedReaders)
		reader.read(outerContext)
		result
	}

	def initialize():Unit = {
		
	
		example.test.Ent3.configureConverters(readers, idPos, iPos)
		example.test.Ent3.configureExtendedConverters(extendedReaders, idPosExtended, iPosExtended)
	}
}
