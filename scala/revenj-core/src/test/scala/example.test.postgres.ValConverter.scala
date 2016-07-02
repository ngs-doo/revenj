package example.test.postgres


import net.revenj.patterns._
import net.revenj.database.postgres._
import net.revenj.database.postgres.converters._

class ValConverter(allColumns: List[net.revenj.database.postgres.ColumnInfo], container: net.revenj.extensibility.Container) extends Converter[example.test.Val] {

	private val columns = allColumns.filter(it => it.typeSchema == "test" && it.typeName == "Val")
	private val extendedColumns = allColumns.filter(it => it.typeSchema == "test" && it.typeName == "-ngs_Val_type-")

	val dbName = """"test"."Val""""
	def default() = example.test.Val()

	private val columnCount = columns.size
	private val readers = new Array[(example.test.Val, PostgresReader, Int) => example.test.Val](if (columnCount == 0) 1 else columnCount)
	private val extendedColumnCount = extendedColumns.size
	private val extendedReaders = new Array[(example.test.Val, PostgresReader, Int) => example.test.Val](if (extendedColumnCount == 0) 1 else extendedColumnCount)

	for (i <- readers.indices) {
		readers(i) = (it, rdr, ctx) => { StringConverter.skip(rdr, ctx); it }
	}
	for (i <- extendedReaders.indices) {
		extendedReaders(i) = (it, rdr, ctx) => { StringConverter.skip(rdr, ctx); it }
	}

	container.registerInstance(this, handleClose = false)
	container.registerInstance[Converter[example.test.Val]](this, handleClose = false)

	override def parseCollectionItem(reader: PostgresReader, context: Int): example.test.Val = {
		val cur = reader.read()
		if (cur == 'N') {
		  reader.read(4)
		  example.test.Val()
		} else {
		  from(reader, 0, context)
		}		
	}
	override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[example.test.Val] = {
		val cur = reader.read()
		if (cur == 'N') {
		  reader.read(4)
		  None
		} else {
		  Some(from(reader, 0, context))
		}
	}

	override def toTuple(item: example.test.Val): PostgresTuple = {
		val items = new Array[PostgresTuple](columnCount) 
		items(xPos) = net.revenj.database.postgres.converters.IntConverter.toTuple(item.x)
		items(fPos) = net.revenj.database.postgres.converters.FloatConverter.toTuple(item.f)
		items(ffPos) = net.revenj.database.postgres.converters.ArrayTuple.createSetOption(item.ff, net.revenj.database.postgres.converters.FloatConverter.toTuple)
		items(aPos) = converterexample_test_Another.toTuple(item.a)
		items(aaPos) = if (item.aa.isEmpty) net.revenj.database.postgres.converters.PostgresTuple.NULL else converterexample_test_Another.toTuple(item.aa.get)
		items(aaaPos) = net.revenj.database.postgres.converters.ArrayTuple.createIndexedOption(item.aaa, converterexample_test_Another.toTuple)
		items(aaaaPos) = net.revenj.database.postgres.converters.ArrayTuple.createSeq(item.aaaa, converterexample_test_Another.toTuple)
		items(enPos) = converterexample_test_En.toTuple(item.en)
		items(en2Pos) = if (item.en2.isEmpty) net.revenj.database.postgres.converters.PostgresTuple.NULL else converterexample_test_En.toTuple(item.en2.get)
		items(en3Pos) = net.revenj.database.postgres.converters.ArrayTuple.createSeq(item.en3, converterexample_test_En.toTuple)
		items(i4Pos) = net.revenj.database.postgres.converters.ArrayTuple.createSeq(item.i4, net.revenj.database.postgres.converters.IntConverter.toTuple)
		items(anotherPos) = net.revenj.database.postgres.converters.ArrayTuple.createSeq(item.another, converterexample_test_Another.toTuple)
		items(dPos) = net.revenj.database.postgres.converters.DateConverter.toTuple(item.d)
		RecordTuple(items)
	}

	def toTupleExtended(item: example.test.Val): PostgresTuple = {
		val items = new Array[PostgresTuple](extendedColumnCount) 
		items(xPosExtended) = net.revenj.database.postgres.converters.IntConverter.toTuple(item.x)
		items(fPosExtended) = net.revenj.database.postgres.converters.FloatConverter.toTuple(item.f)
		items(ffPosExtended) = net.revenj.database.postgres.converters.ArrayTuple.createSetOption(item.ff, net.revenj.database.postgres.converters.FloatConverter.toTuple)
		items(aPosExtended) = converterexample_test_Another.toTupleExtended(item.a)
		items(aaPosExtended) = if (item.aa.isEmpty) PostgresTuple.NULL else converterexample_test_Another.toTupleExtended(item.aa.get)
		items(aaaPosExtended) = net.revenj.database.postgres.converters.ArrayTuple.createIndexedOption(item.aaa, converterexample_test_Another.toTupleExtended)
		items(aaaaPosExtended) = net.revenj.database.postgres.converters.ArrayTuple.createSeq(item.aaaa, converterexample_test_Another.toTupleExtended)
		items(enPosExtended) = converterexample_test_En.toTuple(item.en)
		items(en2PosExtended) = if (item.en2.isEmpty) PostgresTuple.NULL else converterexample_test_En.toTuple(item.en2.get)
		items(en3PosExtended) = net.revenj.database.postgres.converters.ArrayTuple.createSeq(item.en3, converterexample_test_En.toTuple)
		items(i4PosExtended) = net.revenj.database.postgres.converters.ArrayTuple.createSeq(item.i4, net.revenj.database.postgres.converters.IntConverter.toTuple)
		items(anotherPosExtended) = net.revenj.database.postgres.converters.ArrayTuple.createSeq(item.another, converterexample_test_Another.toTupleExtended)
		items(dPosExtended) = net.revenj.database.postgres.converters.DateConverter.toTuple(item.d)
		RecordTuple(items)
	}

	def parseExtended(reader: PostgresReader, context: Int): example.test.Val = {
		val cur = reader.read()
		if (cur == ',' || cur == ')') {
			default()
		} else {
			val result = fromExtended(reader, context, if (context == 0) 1 else context << 1)
			reader.read()
			result
		}
	}

	def parseOptionExtended(reader: PostgresReader, context: Int): Option[example.test.Val] = {
		val cur = reader.read()
		if (cur == ',' || cur == ')') {
			None
		} else {
			val result = Some(fromExtended(reader, context, if (context == 0) 1 else context << 1))
			reader.read()
			result
		}
	}

	
	private val xPos = columns.find(it => it.columnName == "x") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "x" in type test.Val. Check if database is out of sync with code!""")
	}		
	private val xPosExtended = extendedColumns.find(it => it.columnName == "x") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "x" in type test.Val. Check if database is out of sync with code!""")
	}		
	private val fPos = columns.find(it => it.columnName == "f") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "f" in type test.Val. Check if database is out of sync with code!""")
	}		
	private val fPosExtended = extendedColumns.find(it => it.columnName == "f") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "f" in type test.Val. Check if database is out of sync with code!""")
	}		
	private val ffPos = columns.find(it => it.columnName == "ff") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "ff" in type test.Val. Check if database is out of sync with code!""")
	}		
	private val ffPosExtended = extendedColumns.find(it => it.columnName == "ff") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "ff" in type test.Val. Check if database is out of sync with code!""")
	}		
	lazy val converterexample_test_Another = container.resolve[example.test.postgres.AnotherConverter]
	private val aPos = columns.find(it => it.columnName == "a") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "a" in type test.Val. Check if database is out of sync with code!""")
	}		
	private val aPosExtended = extendedColumns.find(it => it.columnName == "a") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "a" in type test.Val. Check if database is out of sync with code!""")
	}		
	private val aaPos = columns.find(it => it.columnName == "aa") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "aa" in type test.Val. Check if database is out of sync with code!""")
	}		
	private val aaPosExtended = extendedColumns.find(it => it.columnName == "aa") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "aa" in type test.Val. Check if database is out of sync with code!""")
	}		
	private val aaaPos = columns.find(it => it.columnName == "aaa") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "aaa" in type test.Val. Check if database is out of sync with code!""")
	}		
	private val aaaPosExtended = extendedColumns.find(it => it.columnName == "aaa") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "aaa" in type test.Val. Check if database is out of sync with code!""")
	}		
	private val aaaaPos = columns.find(it => it.columnName == "aaaa") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "aaaa" in type test.Val. Check if database is out of sync with code!""")
	}		
	private val aaaaPosExtended = extendedColumns.find(it => it.columnName == "aaaa") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "aaaa" in type test.Val. Check if database is out of sync with code!""")
	}		
	lazy val converterexample_test_En = example.test.postgres.EnConverter
	private val enPos = columns.find(it => it.columnName == "en") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "en" in type test.Val. Check if database is out of sync with code!""")
	}		
	private val enPosExtended = extendedColumns.find(it => it.columnName == "en") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "en" in type test.Val. Check if database is out of sync with code!""")
	}		
	private val en2Pos = columns.find(it => it.columnName == "en2") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "en2" in type test.Val. Check if database is out of sync with code!""")
	}		
	private val en2PosExtended = extendedColumns.find(it => it.columnName == "en2") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "en2" in type test.Val. Check if database is out of sync with code!""")
	}		
	private val en3Pos = columns.find(it => it.columnName == "en3") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "en3" in type test.Val. Check if database is out of sync with code!""")
	}		
	private val en3PosExtended = extendedColumns.find(it => it.columnName == "en3") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "en3" in type test.Val. Check if database is out of sync with code!""")
	}		
	private val i4Pos = columns.find(it => it.columnName == "i4") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "i4" in type test.Val. Check if database is out of sync with code!""")
	}		
	private val i4PosExtended = extendedColumns.find(it => it.columnName == "i4") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "i4" in type test.Val. Check if database is out of sync with code!""")
	}		
	private val anotherPos = columns.find(it => it.columnName == "another") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "another" in type test.Val. Check if database is out of sync with code!""")
	}		
	private val anotherPosExtended = extendedColumns.find(it => it.columnName == "another") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "another" in type test.Val. Check if database is out of sync with code!""")
	}		
	private val dPos = columns.find(it => it.columnName == "d") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "d" in type test.Val. Check if database is out of sync with code!""")
	}		
	private val dPosExtended = extendedColumns.find(it => it.columnName == "d") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "d" in type test.Val. Check if database is out of sync with code!""")
	}		

	
	override def parseRaw(reader: PostgresReader, start: Int, context: Int): example.test.Val = {
		val result = from(reader, context, if (context == 0) 1 else context << 1)
		reader.read()
		result
	}

	def from(reader: PostgresReader, outerContext: Int, context: Int): example.test.Val = {
		reader.read(outerContext)
		var i = 0
		
		var _x_ :Option[Int] = None
		var _f_ :Float = 0.0f
		var _ff_ :Set[Option[Float]] = null
		var _a_ :example.test.Another = null
		var _aa_ :Option[example.test.Another] = None
		var _aaa_ :Array[Option[example.test.Another]] = null
		var _aaaa_ :List[example.test.Another] = null
		var _en_ :example.test.En = null
		var _en2_ :Option[example.test.En] = None
		var _en3_ :scala.collection.mutable.LinkedList[example.test.En] = null
		var _i4_ :scala.collection.mutable.LinkedList[Int] = null
		var _another_ :scala.collection.mutable.LinkedList[example.test.Another] = null
		var _d_ :Option[java.time.LocalDate] = None
		while(i < readers.length) {
			val started = i
			
			if (xPos == i) { _x_ = net.revenj.database.postgres.converters.IntConverter.parseOption(reader, context); i += 1 }
			if (fPos == i) { _f_ = net.revenj.database.postgres.converters.FloatConverter.parse(reader, context); i += 1 }
			if (ffPos == i) { _ff_ = net.revenj.database.postgres.converters.FloatConverter.parseNullableCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[Option[Float]](0)).toSet; i += 1 }
			if (aPos == i) { _a_ = converterexample_test_Another.parse(reader, context); i += 1 }
			if (aaPos == i) { _aa_ = converterexample_test_Another.parseOption(reader, context); i += 1 }
			if (aaaPos == i) { _aaa_ = net.revenj.database.postgres.converters.ArrayTuple.parseOption(reader, context, (rdr, ctx) => converterexample_test_Another.from(rdr, 0, ctx)).getOrElse(new scala.collection.mutable.ArrayBuffer[Option[example.test.Another]](0)).toArray; i += 1 }
			if (aaaaPos == i) { _aaaa_ = net.revenj.database.postgres.converters.ArrayTuple.parse(reader, context, (rdr, ctx) => converterexample_test_Another.from(rdr, 0, ctx), () => example.test.Another()).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.Another](0)).toList; i += 1 }
			if (enPos == i) { _en_ = converterexample_test_En.parse(reader, context); i += 1 }
			if (en2Pos == i) { _en2_ = converterexample_test_En.parseOption(reader, context); i += 1 }
			if (en3Pos == i) { _en3_ = scala.collection.mutable.LinkedList[example.test.En](example.test.postgres.EnConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.En](0)) :_*); i += 1 }
			if (i4Pos == i) { _i4_ = scala.collection.mutable.LinkedList[Int](net.revenj.database.postgres.converters.IntConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[Int](0)) :_*); i += 1 }
			if (anotherPos == i) { _another_ = scala.collection.mutable.LinkedList[example.test.Another](net.revenj.database.postgres.converters.ArrayTuple.parse(reader, context, (rdr, ctx) => converterexample_test_Another.from(rdr, 0, ctx), () => example.test.Another()).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.Another](0)) :_*); i += 1 }
			if (dPos == i) { _d_ = net.revenj.database.postgres.converters.DateConverter.parseOption(reader, context); i += 1 }
			if (i == started) {
				net.revenj.database.postgres.converters.StringConverter.skip(reader, context)
				i += 1
			}
		}
		reader.read(outerContext)
		example.test.Val(x = _x_, f = _f_, ff = _ff_, a = _a_, aa = _aa_, aaa = _aaa_, aaaa = _aaaa_, en = _en_, en2 = _en2_, en3 = _en3_, i4 = _i4_, another = _another_, d = _d_)
	}

	def parseRawExtended(reader: PostgresReader, start: Int, context: Int): example.test.Val = {
		val result = fromExtended(reader, context, if (context == 0) 1 else context << 1)
		reader.read()
		result
	}

	def fromExtended(reader: PostgresReader, outerContext: Int, context: Int): example.test.Val = {
		reader.read(outerContext)
		var i = 0
		
		var _x_ :Option[Int] = None
		var _f_ :Float = 0.0f
		var _ff_ :Set[Option[Float]] = null
		var _a_ :example.test.Another = null
		var _aa_ :Option[example.test.Another] = None
		var _aaa_ :Array[Option[example.test.Another]] = null
		var _aaaa_ :List[example.test.Another] = null
		var _en_ :example.test.En = null
		var _en2_ :Option[example.test.En] = None
		var _en3_ :scala.collection.mutable.LinkedList[example.test.En] = null
		var _i4_ :scala.collection.mutable.LinkedList[Int] = null
		var _another_ :scala.collection.mutable.LinkedList[example.test.Another] = null
		var _d_ :Option[java.time.LocalDate] = None
		while(i < extendedReaders.length) {
			val started = i
			
			if (xPosExtended == i) { _x_ = net.revenj.database.postgres.converters.IntConverter.parseOption(reader, context); i += 1 }
			if (fPosExtended == i) { _f_ = net.revenj.database.postgres.converters.FloatConverter.parse(reader, context); i += 1 }
			if (ffPosExtended == i) { _ff_ = net.revenj.database.postgres.converters.FloatConverter.parseNullableCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[Option[Float]](0)).toSet; i += 1 }
			if (aPosExtended == i) { _a_ = converterexample_test_Another.parseExtended(reader, context); i += 1 }
			if (aaPosExtended == i) { _aa_ = converterexample_test_Another.parseOptionExtended(reader, context); i += 1 }
			if (aaaPosExtended == i) { _aaa_ = net.revenj.database.postgres.converters.ArrayTuple.parseOption(reader, context, (rdr, ctx) => converterexample_test_Another.fromExtended(rdr, 0, ctx)).getOrElse(new scala.collection.mutable.ArrayBuffer[Option[example.test.Another]](0)).toArray; i += 1 }
			if (aaaaPosExtended == i) { _aaaa_ = net.revenj.database.postgres.converters.ArrayTuple.parse(reader, context, (rdr, ctx) => converterexample_test_Another.fromExtended(rdr, 0, ctx), () => example.test.Another()).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.Another](0)).toList; i += 1 }
			if (enPosExtended == i) { _en_ = converterexample_test_En.parse(reader, context); i += 1 }
			if (en2PosExtended == i) { _en2_ = converterexample_test_En.parseOption(reader, context); i += 1 }
			if (en3PosExtended == i) { _en3_ = scala.collection.mutable.LinkedList[example.test.En](example.test.postgres.EnConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.En](0)) :_*); i += 1 }
			if (i4PosExtended == i) { _i4_ = scala.collection.mutable.LinkedList[Int](net.revenj.database.postgres.converters.IntConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[Int](0)) :_*); i += 1 }
			if (anotherPosExtended == i) { _another_ = scala.collection.mutable.LinkedList[example.test.Another](net.revenj.database.postgres.converters.ArrayTuple.parse(reader, context, (rdr, ctx) => converterexample_test_Another.fromExtended(rdr, 0, ctx), () => example.test.Another()).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.Another](0)) :_*); i += 1 }
			if (dPosExtended == i) { _d_ = net.revenj.database.postgres.converters.DateConverter.parseOption(reader, context); i += 1 }
			if (i == started) {
				net.revenj.database.postgres.converters.StringConverter.skip(reader, context)
				i += 1
			}
		}
		reader.read(outerContext)
		example.test.Val(x = _x_, f = _f_, ff = _ff_, a = _a_, aa = _aa_, aaa = _aaa_, aaaa = _aaaa_, en = _en_, en2 = _en2_, en3 = _en3_, i4 = _i4_, another = _another_, d = _d_)
	}

	def initialize():Unit = {
		
	}
}
