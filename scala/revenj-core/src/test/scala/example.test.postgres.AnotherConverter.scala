package example.test.postgres


import net.revenj.patterns._
import net.revenj.database.postgres._
import net.revenj.database.postgres.converters._

class AnotherConverter(allColumns: List[net.revenj.database.postgres.ColumnInfo], container: net.revenj.extensibility.Container) extends Converter[example.test.Another] {

	private val columns = allColumns.filter(it => it.typeSchema == "test" && it.typeName == "Another")
	private val extendedColumns = allColumns.filter(it => it.typeSchema == "test" && it.typeName == "-ngs_Another_type-")

	val dbName = """"test"."Another""""
	def default() = example.test.Another()

	private val columnCount = columns.size
	private val readers = new Array[(example.test.Another, PostgresReader, Int) => example.test.Another](if (columnCount == 0) 1 else columnCount)
	private val extendedColumnCount = extendedColumns.size
	private val extendedReaders = new Array[(example.test.Another, PostgresReader, Int) => example.test.Another](if (extendedColumnCount == 0) 1 else extendedColumnCount)

	for (i <- readers.indices) {
		readers(i) = (it, rdr, ctx) => { StringConverter.skip(rdr, ctx); it }
	}
	for (i <- extendedReaders.indices) {
		extendedReaders(i) = (it, rdr, ctx) => { StringConverter.skip(rdr, ctx); it }
	}

	container.registerInstance(this, handleClose = false)
	container.registerInstance[Converter[example.test.Another]](this, handleClose = false)

	override def parseCollectionItem(reader: PostgresReader, context: Int): example.test.Another = {
		val cur = reader.read()
		if (cur == 'N') {
		  reader.read(4)
		  example.test.Another()
		} else {
		  from(reader, 0, context)
		}		
	}
	override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[example.test.Another] = {
		val cur = reader.read()
		if (cur == 'N') {
		  reader.read(4)
		  None
		} else {
		  Some(from(reader, 0, context))
		}
	}

	override def toTuple(item: example.test.Another): PostgresTuple = {
		val items = new Array[PostgresTuple](columnCount) 
		RecordTuple(items)
	}

	def toTupleExtended(item: example.test.Another): PostgresTuple = {
		val items = new Array[PostgresTuple](extendedColumnCount) 
		RecordTuple(items)
	}

	def parseExtended(reader: PostgresReader, context: Int): example.test.Another = {
		val cur = reader.read()
		if (cur == ',' || cur == ')') {
			default()
		} else {
			val result = fromExtended(reader, context, if (context == 0) 1 else context << 1)
			reader.read()
			result
		}
	}

	def parseOptionExtended(reader: PostgresReader, context: Int): Option[example.test.Another] = {
		val cur = reader.read()
		if (cur == ',' || cur == ')') {
			None
		} else {
			val result = Some(fromExtended(reader, context, if (context == 0) 1 else context << 1))
			reader.read()
			result
		}
	}

	

	
	override def parseRaw(reader: PostgresReader, start: Int, context: Int): example.test.Another = {
		val result = from(reader, context, if (context == 0) 1 else context << 1)
		reader.read()
		result
	}

	def from(reader: PostgresReader, outerContext: Int, context: Int): example.test.Another = {
		reader.read(outerContext)
		var i = 0
		
		while(i < readers.length) {
			val started = i
			
			if (i == started) {
				net.revenj.database.postgres.converters.StringConverter.skip(reader, context)
				i += 1
			}
		}
		reader.read(outerContext)
		example.test.Another()
	}

	def parseRawExtended(reader: PostgresReader, start: Int, context: Int): example.test.Another = {
		val result = fromExtended(reader, context, if (context == 0) 1 else context << 1)
		reader.read()
		result
	}

	def fromExtended(reader: PostgresReader, outerContext: Int, context: Int): example.test.Another = {
		reader.read(outerContext)
		var i = 0
		
		while(i < extendedReaders.length) {
			val started = i
			
			if (i == started) {
				net.revenj.database.postgres.converters.StringConverter.skip(reader, context)
				i += 1
			}
		}
		reader.read(outerContext)
		example.test.Another()
	}

	def initialize():Unit = {
		
	}
}
