package example.test.postgres


import net.revenj.patterns._
import net.revenj.database.postgres._
import net.revenj.database.postgres.converters._

class AbcSqlConverter(allColumns: List[net.revenj.database.postgres.ColumnInfo], container: net.revenj.extensibility.Container) extends Converter[example.test.AbcSql] {

	private val columns = allColumns.filter(it => it.typeSchema == "test" && it.typeName == "AbcSql")
	private val extendedColumns = allColumns.filter(it => it.typeSchema == "test" && it.typeName == "AbcSql")

	val dbName = """"test"."AbcList""""
	def default() = example.test.AbcSql()

	private val columnCount = columns.size
	private val readers = new Array[(example.test.AbcSql, PostgresReader, Int) => example.test.AbcSql](if (columnCount == 0) 1 else columnCount)
	private val extendedColumnCount = extendedColumns.size
	private val extendedReaders = new Array[(example.test.AbcSql, PostgresReader, Int) => example.test.AbcSql](if (extendedColumnCount == 0) 1 else extendedColumnCount)

	for (i <- readers.indices) {
		readers(i) = (it, rdr, ctx) => { StringConverter.skip(rdr, ctx); it }
	}
	for (i <- extendedReaders.indices) {
		extendedReaders(i) = (it, rdr, ctx) => { StringConverter.skip(rdr, ctx); it }
	}

	container.registerInstance(this, handleClose = false)
	container.registerInstance[Converter[example.test.AbcSql]](this, handleClose = false)

	override def parseCollectionItem(reader: PostgresReader, context: Int): example.test.AbcSql = {
		val cur = reader.read()
		if (cur == 'N') {
		  reader.read(4)
		  example.test.AbcSql()
		} else {
		  from(reader, 0, context)
		}		
	}
	override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[example.test.AbcSql] = {
		val cur = reader.read()
		if (cur == 'N') {
		  reader.read(4)
		  None
		} else {
		  Some(from(reader, 0, context))
		}
	}

	override def toTuple(item: example.test.AbcSql): PostgresTuple = {
		val items = new Array[PostgresTuple](columnCount) 
		items(sPos) = net.revenj.database.postgres.converters.StringConverter.toTuple(item.s)
		items(iiPos) = net.revenj.database.postgres.converters.ArrayTuple.createIndexed(item.ii, net.revenj.database.postgres.converters.IntConverter.toTuple)
		items(enPos) = converterexample_test_En.toTuple(item.en)
		items(en2Pos) = if (item.en2.isEmpty) net.revenj.database.postgres.converters.PostgresTuple.NULL else converterexample_test_En.toTuple(item.en2.get)
		items(en3Pos) = net.revenj.database.postgres.converters.ArrayTuple.createSeq(item.en3, example.test.postgres.EnConverter.toTuple)
		items(i4Pos) = net.revenj.database.postgres.converters.ArrayTuple.createSeq(item.i4, net.revenj.database.postgres.converters.IntConverter.toTuple)
		RecordTuple(items)
	}

	def toTupleExtended(item: example.test.AbcSql): PostgresTuple = {
		val items = new Array[PostgresTuple](extendedColumnCount) 
		items(sPosExtended) = net.revenj.database.postgres.converters.StringConverter.toTuple(item.s)
		items(iiPosExtended) = net.revenj.database.postgres.converters.ArrayTuple.createIndexed(item.ii, net.revenj.database.postgres.converters.IntConverter.toTuple)
		items(enPosExtended) = converterexample_test_En.toTuple(item.en)
		items(en2PosExtended) = if (item.en2.isEmpty) PostgresTuple.NULL else converterexample_test_En.toTuple(item.en2.get)
		items(en3PosExtended) = net.revenj.database.postgres.converters.ArrayTuple.createSeq(item.en3, example.test.postgres.EnConverter.toTuple)
		items(i4PosExtended) = net.revenj.database.postgres.converters.ArrayTuple.createSeq(item.i4, net.revenj.database.postgres.converters.IntConverter.toTuple)
		RecordTuple(items)
	}

	def parseExtended(reader: PostgresReader, context: Int): example.test.AbcSql = {
		val cur = reader.read()
		if (cur == ',' || cur == ')') {
			default()
		} else {
			val result = fromExtended(reader, context, if (context == 0) 1 else context << 1)
			reader.read()
			result
		}
	}

	def parseOptionExtended(reader: PostgresReader, context: Int): Option[example.test.AbcSql] = {
		val cur = reader.read()
		if (cur == ',' || cur == ')') {
			None
		} else {
			val result = Some(fromExtended(reader, context, if (context == 0) 1 else context << 1))
			reader.read()
			result
		}
	}

	
	private val sPos = columns.find(it => it.columnName == "s") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "s" in type test.AbcSql. Check if database is out of sync with code!""")
	}		
	private val sPosExtended = extendedColumns.find(it => it.columnName == "s") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "s" in type test.AbcSql. Check if database is out of sync with code!""")
	}		
	private val iiPos = columns.find(it => it.columnName == "ii") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "ii" in type test.AbcSql. Check if database is out of sync with code!""")
	}		
	private val iiPosExtended = extendedColumns.find(it => it.columnName == "ii") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "ii" in type test.AbcSql. Check if database is out of sync with code!""")
	}		
	lazy val converterexample_test_En = example.test.postgres.EnConverter
	private val enPos = columns.find(it => it.columnName == "en") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "en" in type test.AbcSql. Check if database is out of sync with code!""")
	}		
	private val enPosExtended = extendedColumns.find(it => it.columnName == "en") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "en" in type test.AbcSql. Check if database is out of sync with code!""")
	}		
	private val en2Pos = columns.find(it => it.columnName == "en2") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "en2" in type test.AbcSql. Check if database is out of sync with code!""")
	}		
	private val en2PosExtended = extendedColumns.find(it => it.columnName == "en2") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "en2" in type test.AbcSql. Check if database is out of sync with code!""")
	}		
	private val en3Pos = columns.find(it => it.columnName == "en3") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "en3" in type test.AbcSql. Check if database is out of sync with code!""")
	}		
	private val en3PosExtended = extendedColumns.find(it => it.columnName == "en3") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "en3" in type test.AbcSql. Check if database is out of sync with code!""")
	}		
	private val i4Pos = columns.find(it => it.columnName == "i4") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "i4" in type test.AbcSql. Check if database is out of sync with code!""")
	}		
	private val i4PosExtended = extendedColumns.find(it => it.columnName == "i4") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "i4" in type test.AbcSql. Check if database is out of sync with code!""")
	}		
	
	container.registerClass[example.test.postgres.AbcSqlRepository](classOf[example.test.postgres.AbcSqlRepository], singleton = false)
	container.registerFactory[net.revenj.patterns.SearchableRepository[example.test.AbcSql]](c => new example.test.postgres.AbcSqlRepository(c), singleton = false)

	
	override def parseRaw(reader: PostgresReader, start: Int, context: Int): example.test.AbcSql = {
		val result = from(reader, context, if (context == 0) 1 else context << 1)
		reader.read()
		result
	}

	def from(reader: PostgresReader, outerContext: Int, context: Int): example.test.AbcSql = {
		reader.read(outerContext)
		var i = 0
		
		var _s_ :String = ""
		var _ii_ :Array[Int] = null
		var _en_ :example.test.En = null
		var _en2_ :Option[example.test.En] = None
		var _en3_ :scala.collection.mutable.LinkedList[example.test.En] = null
		var _i4_ :scala.collection.mutable.LinkedList[Int] = null
		while(i < readers.length) {
			val started = i
			
			if (sPos == i) { _s_ = net.revenj.database.postgres.converters.StringConverter.parse(reader, context); i += 1 }
			if (iiPos == i) { _ii_ = net.revenj.database.postgres.converters.IntConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[Int](0)).toArray; i += 1 }
			if (enPos == i) { _en_ = example.test.postgres.EnConverter.parse(reader, context); i += 1 }
			if (en2Pos == i) { _en2_ = example.test.postgres.EnConverter.parseOption(reader, context); i += 1 }
			if (en3Pos == i) { _en3_ = scala.collection.mutable.LinkedList[example.test.En](example.test.postgres.EnConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.En](0)) :_*); i += 1 }
			if (i4Pos == i) { _i4_ = scala.collection.mutable.LinkedList[Int](net.revenj.database.postgres.converters.IntConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[Int](0)) :_*); i += 1 }
			if (i == started) {
				net.revenj.database.postgres.converters.StringConverter.skip(reader, context)
				i += 1
			}
		}
		reader.read(outerContext)
		example.test.AbcSql(s = _s_, ii = _ii_, en = _en_, en2 = _en2_, en3 = _en3_, i4 = _i4_)
	}

	def parseRawExtended(reader: PostgresReader, start: Int, context: Int): example.test.AbcSql = {
		val result = fromExtended(reader, context, if (context == 0) 1 else context << 1)
		reader.read()
		result
	}

	def fromExtended(reader: PostgresReader, outerContext: Int, context: Int): example.test.AbcSql = {
		reader.read(outerContext)
		var i = 0
		
		var _s_ :String = ""
		var _ii_ :Array[Int] = null
		var _en_ :example.test.En = null
		var _en2_ :Option[example.test.En] = None
		var _en3_ :scala.collection.mutable.LinkedList[example.test.En] = null
		var _i4_ :scala.collection.mutable.LinkedList[Int] = null
		while(i < extendedReaders.length) {
			val started = i
			
			if (sPosExtended == i) { _s_ = net.revenj.database.postgres.converters.StringConverter.parse(reader, context); i += 1 }
			if (iiPosExtended == i) { _ii_ = net.revenj.database.postgres.converters.IntConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[Int](0)).toArray; i += 1 }
			if (enPosExtended == i) { _en_ = example.test.postgres.EnConverter.parse(reader, context); i += 1 }
			if (en2PosExtended == i) { _en2_ = example.test.postgres.EnConverter.parseOption(reader, context); i += 1 }
			if (en3PosExtended == i) { _en3_ = scala.collection.mutable.LinkedList[example.test.En](example.test.postgres.EnConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.En](0)) :_*); i += 1 }
			if (i4PosExtended == i) { _i4_ = scala.collection.mutable.LinkedList[Int](net.revenj.database.postgres.converters.IntConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[Int](0)) :_*); i += 1 }
			if (i == started) {
				net.revenj.database.postgres.converters.StringConverter.skip(reader, context)
				i += 1
			}
		}
		reader.read(outerContext)
		example.test.AbcSql(s = _s_, ii = _ii_, en = _en_, en2 = _en2_, en3 = _en3_, i4 = _i4_)
	}

	def initialize():Unit = {
		
	}
}
