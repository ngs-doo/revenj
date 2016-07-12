package example.test.postgres


import net.revenj.patterns._
import net.revenj.database.postgres._
import net.revenj.database.postgres.converters._

class AbcListConverter(allColumns: List[net.revenj.database.postgres.ColumnInfo], container: net.revenj.extensibility.Container) extends Converter[example.test.AbcList] {

	private val columns = allColumns.filter(it => it.typeSchema == "test" && it.typeName == "AbcList")
	private val extendedColumns = allColumns.filter(it => it.typeSchema == "test" && it.typeName == "-ngs_AbcList_type-")

	val dbName = """"test"."AbcList""""
	def default() = null

	private val columnCount = columns.size
	private val readers = new Array[(example.test.AbcList, PostgresReader, Int) => example.test.AbcList](if (columnCount == 0) 1 else columnCount)
	private val extendedColumnCount = extendedColumns.size
	private val extendedReaders = new Array[(example.test.AbcList, PostgresReader, Int) => example.test.AbcList](if (extendedColumnCount == 0) 1 else extendedColumnCount)

	for (i <- readers.indices) {
		readers(i) = (it, rdr, ctx) => { StringConverter.skip(rdr, ctx); it }
	}
	for (i <- extendedReaders.indices) {
		extendedReaders(i) = (it, rdr, ctx) => { StringConverter.skip(rdr, ctx); it }
	}

	container.registerInstance(this, handleClose = false)
	container.registerInstance[Converter[example.test.AbcList]](this, handleClose = false)

	override def parseCollectionItem(reader: PostgresReader, context: Int): example.test.AbcList = {
		val cur = reader.read()
		if (cur == 'N') {
		  reader.read(4)
		  null
		} else {
		  from(reader, 0, context)
		}		
	}
	override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[example.test.AbcList] = {
		val cur = reader.read()
		if (cur == 'N') {
		  reader.read(4)
		  None
		} else {
		  Some(from(reader, 0, context))
		}
	}

	override def toTuple(item: example.test.AbcList): PostgresTuple = {
		val items = new Array[PostgresTuple](columnCount) 
		items(URIPos) = net.revenj.database.postgres.converters.StringConverter.toTuple(item.URI)
		items(sPos) = net.revenj.database.postgres.converters.StringConverter.toTuple(item.s)
		items(iiPos) = net.revenj.database.postgres.converters.ArrayTuple.createIndexed(item.ii, net.revenj.database.postgres.converters.IntConverter.toTuple)
		items(enPos) = converterexample_test_En.toTuple(item.en)
		items(en2Pos) = if (item.en2.isEmpty) net.revenj.database.postgres.converters.PostgresTuple.NULL else converterexample_test_En.toTuple(item.en2.get)
		items(en3Pos) = net.revenj.database.postgres.converters.ArrayTuple.createSeq(item.en3, converterexample_test_En.toTuple)
		items(i4Pos) = net.revenj.database.postgres.converters.ArrayTuple.createSeq(item.i4, net.revenj.database.postgres.converters.IntConverter.toTuple)
		items(anotherPos) = net.revenj.database.postgres.converters.ArrayTuple.createSeq(item.another, converterexample_test_Another.toTuple)
		items(vPos) = converterexample_test_Val.toTuple(item.v)
		items(vvPos) = if (item.vv.isEmpty) net.revenj.database.postgres.converters.PostgresTuple.NULL else converterexample_test_Val.toTuple(item.vv.get)
		items(xPos) = net.revenj.database.postgres.converters.IntConverter.toTuple(item.x)
		items(fPos) = net.revenj.database.postgres.converters.FloatConverter.toTuple(item.f)
		items(bytesPos) = net.revenj.database.postgres.converters.ByteaConverter.toTuple(item.bytes)
		items(bbPos) = net.revenj.database.postgres.converters.ArrayTuple.createSeq(item.bb, net.revenj.database.postgres.converters.ByteaConverter.toTuple)
		items(vvvPos) = net.revenj.database.postgres.converters.ArrayTuple.createIndexed(item.vvv, converterexample_test_Val.toTuple)
		items(hasVPos) = net.revenj.database.postgres.converters.BoolConverter.toTuple(item.hasV)
		items(hasAPos) = net.revenj.database.postgres.converters.BoolConverter.toTuple(item.hasA)
		items(ent1Pos) = converterexample_test_Ent1.toTuple(item.ent1)
		items(iPos) = net.revenj.database.postgres.converters.IntConverter.toTuple(item.i)
		items(ent2Pos) = net.revenj.database.postgres.converters.ArrayTuple.createIndexed(item.ent2, converterexample_test_Ent2.toTuple)
		items(abc1Pos) = if (item.abc1.isEmpty) net.revenj.database.postgres.converters.PostgresTuple.NULL else converterexample_test_Abc.toTuple(item.abc1.get)
		items(s2Pos) = net.revenj.database.postgres.converters.StringConverter.toTuple(item.s2)
		items(abc2Pos) = net.revenj.database.postgres.converters.ArrayTuple.createSeq(item.abc2, converterexample_test_Abc.toTuple)
		RecordTuple(items)
	}

	def toTupleExtended(item: example.test.AbcList): PostgresTuple = {
		val items = new Array[PostgresTuple](extendedColumnCount) 
		items(URIPosExtended) = net.revenj.database.postgres.converters.StringConverter.toTuple(item.URI)
		items(sPosExtended) = net.revenj.database.postgres.converters.StringConverter.toTuple(item.s)
		items(iiPosExtended) = net.revenj.database.postgres.converters.ArrayTuple.createIndexed(item.ii, net.revenj.database.postgres.converters.IntConverter.toTuple)
		items(enPosExtended) = converterexample_test_En.toTuple(item.en)
		items(en2PosExtended) = if (item.en2.isEmpty) PostgresTuple.NULL else converterexample_test_En.toTuple(item.en2.get)
		items(en3PosExtended) = net.revenj.database.postgres.converters.ArrayTuple.createSeq(item.en3, converterexample_test_En.toTuple)
		items(i4PosExtended) = net.revenj.database.postgres.converters.ArrayTuple.createSeq(item.i4, net.revenj.database.postgres.converters.IntConverter.toTuple)
		items(anotherPosExtended) = net.revenj.database.postgres.converters.ArrayTuple.createSeq(item.another, converterexample_test_Another.toTupleExtended)
		items(vPosExtended) = converterexample_test_Val.toTupleExtended(item.v)
		items(vvPosExtended) = if (item.vv.isEmpty) PostgresTuple.NULL else converterexample_test_Val.toTupleExtended(item.vv.get)
		items(xPosExtended) = net.revenj.database.postgres.converters.IntConverter.toTuple(item.x)
		items(fPosExtended) = net.revenj.database.postgres.converters.FloatConverter.toTuple(item.f)
		items(bytesPosExtended) = net.revenj.database.postgres.converters.ByteaConverter.toTuple(item.bytes)
		items(bbPosExtended) = net.revenj.database.postgres.converters.ArrayTuple.createSeq(item.bb, net.revenj.database.postgres.converters.ByteaConverter.toTuple)
		items(vvvPosExtended) = net.revenj.database.postgres.converters.ArrayTuple.createIndexed(item.vvv, converterexample_test_Val.toTupleExtended)
		items(hasVPosExtended) = net.revenj.database.postgres.converters.BoolConverter.toTuple(item.hasV)
		items(hasAPosExtended) = net.revenj.database.postgres.converters.BoolConverter.toTuple(item.hasA)
		items(ent1PosExtended) = converterexample_test_Ent1.toTupleExtended(item.ent1)
		items(iPosExtended) = net.revenj.database.postgres.converters.IntConverter.toTuple(item.i)
		items(ent2PosExtended) = net.revenj.database.postgres.converters.ArrayTuple.createIndexed(item.ent2, converterexample_test_Ent2.toTupleExtended)
		items(abc1PosExtended) = if (item.abc1.isEmpty) PostgresTuple.NULL else converterexample_test_Abc.toTupleExtended(item.abc1.get)
		items(s2PosExtended) = net.revenj.database.postgres.converters.StringConverter.toTuple(item.s2)
		items(abc2PosExtended) = net.revenj.database.postgres.converters.ArrayTuple.createSeq(item.abc2, converterexample_test_Abc.toTupleExtended)
		RecordTuple(items)
	}

	def parseExtended(reader: PostgresReader, context: Int): example.test.AbcList = {
		val cur = reader.read()
		if (cur == ',' || cur == ')') {
			default()
		} else {
			val result = fromExtended(reader, context, if (context == 0) 1 else context << 1)
			reader.read()
			result
		}
	}

	def parseOptionExtended(reader: PostgresReader, context: Int): Option[example.test.AbcList] = {
		val cur = reader.read()
		if (cur == ',' || cur == ')') {
			None
		} else {
			val result = Some(fromExtended(reader, context, if (context == 0) 1 else context << 1))
			reader.read()
			result
		}
	}

	
	private val URIPos = columns.find(it => it.columnName == "URI") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "URI" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val URIPosExtended = extendedColumns.find(it => it.columnName == "URI") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "URI" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val sPos = columns.find(it => it.columnName == "s") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "s" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val sPosExtended = extendedColumns.find(it => it.columnName == "s") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "s" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val iiPos = columns.find(it => it.columnName == "ii") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "ii" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val iiPosExtended = extendedColumns.find(it => it.columnName == "ii") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "ii" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	lazy val converterexample_test_En = example.test.postgres.EnConverter
	private val enPos = columns.find(it => it.columnName == "en") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "en" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val enPosExtended = extendedColumns.find(it => it.columnName == "en") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "en" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val en2Pos = columns.find(it => it.columnName == "en2") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "en2" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val en2PosExtended = extendedColumns.find(it => it.columnName == "en2") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "en2" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val en3Pos = columns.find(it => it.columnName == "en3") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "en3" in type test.AbcList_entity. Check if database is out of sync with code!""")
	}		
	private val en3PosExtended = extendedColumns.find(it => it.columnName == "en3") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "en3" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val i4Pos = columns.find(it => it.columnName == "i4") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "i4" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val i4PosExtended = extendedColumns.find(it => it.columnName == "i4") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "i4" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	lazy val converterexample_test_Another = container.resolve[example.test.postgres.AnotherConverter]
	private val anotherPos = columns.find(it => it.columnName == "another") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "another" in type test.AbcList_entity. Check if database is out of sync with code!""")
	}		
	private val anotherPosExtended = extendedColumns.find(it => it.columnName == "another") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "another" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	lazy val converterexample_test_Val = container.resolve[example.test.postgres.ValConverter]
	private val vPos = columns.find(it => it.columnName == "v") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "v" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val vPosExtended = extendedColumns.find(it => it.columnName == "v") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "v" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val vvPos = columns.find(it => it.columnName == "vv") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "vv" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val vvPosExtended = extendedColumns.find(it => it.columnName == "vv") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "vv" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val xPos = columns.find(it => it.columnName == "x") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "x" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val xPosExtended = extendedColumns.find(it => it.columnName == "x") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "x" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val fPos = columns.find(it => it.columnName == "f") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "f" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val fPosExtended = extendedColumns.find(it => it.columnName == "f") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "f" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val bytesPos = columns.find(it => it.columnName == "bytes") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "bytes" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val bytesPosExtended = extendedColumns.find(it => it.columnName == "bytes") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "bytes" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val bbPos = columns.find(it => it.columnName == "bb") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "bb" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val bbPosExtended = extendedColumns.find(it => it.columnName == "bb") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "bb" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val vvvPos = columns.find(it => it.columnName == "vvv") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "vvv" in type test.AbcList_entity. Check if database is out of sync with code!""")
	}		
	private val vvvPosExtended = extendedColumns.find(it => it.columnName == "vvv") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "vvv" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val hasVPos = columns.find(it => it.columnName == "hasV") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "hasV" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val hasVPosExtended = extendedColumns.find(it => it.columnName == "hasV") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "hasV" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val hasAPos = columns.find(it => it.columnName == "hasA") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "hasA" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val hasAPosExtended = extendedColumns.find(it => it.columnName == "hasA") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "hasA" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	lazy val converterexample_test_Ent1 = container.resolve[example.test.postgres.Ent1Converter]
	private val ent1Pos = columns.find(it => it.columnName == "ent1") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "ent1" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val ent1PosExtended = extendedColumns.find(it => it.columnName == "ent1") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "ent1" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val iPos = columns.find(it => it.columnName == "i") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "i" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val iPosExtended = extendedColumns.find(it => it.columnName == "i") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "i" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	lazy val converterexample_test_Ent2 = container.resolve[example.test.postgres.Ent2Converter]
	private val ent2Pos = columns.find(it => it.columnName == "ent2") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "ent2" in type test.AbcList_entity. Check if database is out of sync with code!""")
	}		
	private val ent2PosExtended = extendedColumns.find(it => it.columnName == "ent2") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "ent2" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	lazy val converterexample_test_Abc = container.resolve[example.test.postgres.AbcConverter]
	private val abc1Pos = columns.find(it => it.columnName == "abc1") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "abc1" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val abc1PosExtended = extendedColumns.find(it => it.columnName == "abc1") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "abc1" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val s2Pos = columns.find(it => it.columnName == "s2") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "s2" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val s2PosExtended = extendedColumns.find(it => it.columnName == "s2") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "s2" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	private val abc2Pos = columns.find(it => it.columnName == "abc2") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "abc2" in type test.AbcList_entity. Check if database is out of sync with code!""")
	}		
	private val abc2PosExtended = extendedColumns.find(it => it.columnName == "abc2") match {
		case Some(col) => col.order - 1
		case None => throw new IllegalArgumentException("""Couldn't find column "abc2" in type test.AbcList. Check if database is out of sync with code!""")
	}		
	
	container.registerClass[example.test.postgres.AbcListRepository](classOf[example.test.postgres.AbcListRepository], singleton = false)
	container.registerFactory[net.revenj.patterns.SearchableRepository[example.test.AbcList]](c => new example.test.postgres.AbcListRepository(c), singleton = false)
	container.registerFactory[net.revenj.patterns.Repository[example.test.AbcList]](c => new example.test.postgres.AbcListRepository(c), singleton = false)

	
	override def parseRaw(reader: PostgresReader, start: Int, context: Int): example.test.AbcList = {
		val result = from(reader, context, if (context == 0) 1 else context << 1)
		reader.read()
		result
	}

	def from(reader: PostgresReader, outerContext: Int, context: Int): example.test.AbcList = {
		reader.read(outerContext)
		var i = 0
		
		var _URI_ :String = null
		var _s_ :String = ""
		var _ii_ :Array[Int] = null
		var _en_ :example.test.En = null
		var _en2_ :Option[example.test.En] = None
		var _en3_ :scala.collection.mutable.LinkedList[example.test.En] = null
		var _i4_ :scala.collection.mutable.LinkedList[Int] = null
		var _another_ :scala.collection.mutable.LinkedList[example.test.Another] = null
		var _v_ :example.test.Val = null
		var _vv_ :Option[example.test.Val] = None
		var _x_ :Option[Int] = None
		var _f_ :Float = 0.0f
		var _bytes_ :Array[Byte] = Array[Byte]()
		var _bb_ :List[Array[Byte]] = null
		var _vvv_ :IndexedSeq[example.test.Val] = null
		var _hasV_ :Boolean = false
		var _hasA_ :Boolean = false
		var _ent1_ :example.test.Ent1 = null
		var _i_ :Int = 0
		var _ent2_ :Array[example.test.Ent2] = null
		var _abc1_ :Option[example.test.Abc] = None
		var _s2_ :Option[String] = None
		var _abc2_ :scala.collection.mutable.Queue[example.test.Abc] = null
		while(i < readers.length) {
			val started = i
			
			if (URIPos == i) { _URI_ = net.revenj.database.postgres.converters.StringConverter.parse(reader, context); i += 1 }
			if (sPos == i) { _s_ = net.revenj.database.postgres.converters.StringConverter.parse(reader, context); i += 1 }
			if (iiPos == i) { _ii_ = net.revenj.database.postgres.converters.IntConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[Int](0)).toArray; i += 1 }
			if (enPos == i) { _en_ = converterexample_test_En.parse(reader, context); i += 1 }
			if (en2Pos == i) { _en2_ = converterexample_test_En.parseOption(reader, context); i += 1 }
			if (en3Pos == i) { _en3_ = scala.collection.mutable.LinkedList[example.test.En](example.test.postgres.EnConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.En](0)) :_*); i += 1 }
			if (i4Pos == i) { _i4_ = scala.collection.mutable.LinkedList[Int](net.revenj.database.postgres.converters.IntConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[Int](0)) :_*); i += 1 }
			if (anotherPos == i) { _another_ = scala.collection.mutable.LinkedList[example.test.Another](net.revenj.database.postgres.converters.ArrayTuple.parse(reader, context, (rdr, ctx) => converterexample_test_Another.from(rdr, 0, ctx), () => example.test.Another()).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.Another](0)) :_*); i += 1 }
			if (vPos == i) { _v_ = converterexample_test_Val.parse(reader, context); i += 1 }
			if (vvPos == i) { _vv_ = converterexample_test_Val.parseOption(reader, context); i += 1 }
			if (xPos == i) { _x_ = net.revenj.database.postgres.converters.IntConverter.parseOption(reader, context); i += 1 }
			if (fPos == i) { _f_ = net.revenj.database.postgres.converters.FloatConverter.parse(reader, context); i += 1 }
			if (bytesPos == i) { _bytes_ = net.revenj.database.postgres.converters.ByteaConverter.parse(reader, context); i += 1 }
			if (bbPos == i) { _bb_ = net.revenj.database.postgres.converters.ByteaConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[Array[Byte]](0)).toList; i += 1 }
			if (vvvPos == i) { _vvv_ = net.revenj.database.postgres.converters.ArrayTuple.parse(reader, context, (rdr, ctx) => converterexample_test_Val.from(rdr, 0, ctx), () => example.test.Val()).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.Val](0)).toIndexedSeq; i += 1 }
			if (hasVPos == i) { _hasV_ = net.revenj.database.postgres.converters.BoolConverter.parse(reader, context); i += 1 }
			if (hasAPos == i) { _hasA_ = net.revenj.database.postgres.converters.BoolConverter.parse(reader, context); i += 1 }
			if (ent1Pos == i) { _ent1_ = converterexample_test_Ent1.parse(reader, context); i += 1 }
			if (iPos == i) { _i_ = net.revenj.database.postgres.converters.IntConverter.parse(reader, context); i += 1 }
			if (ent2Pos == i) { _ent2_ = net.revenj.database.postgres.converters.ArrayTuple.parse(reader, context, (rdr, ctx) => converterexample_test_Ent2.from(rdr, 0, ctx), () => null).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.Ent2](0)).toArray; i += 1 }
			if (abc1Pos == i) { _abc1_ = converterexample_test_Abc.parseOption(reader, context); i += 1 }
			if (s2Pos == i) { _s2_ = net.revenj.database.postgres.converters.StringConverter.parseOption(reader, context); i += 1 }
			if (abc2Pos == i) { _abc2_ = scala.collection.mutable.Queue[example.test.Abc](net.revenj.database.postgres.converters.ArrayTuple.parse(reader, context, (rdr, ctx) => converterexample_test_Abc.from(rdr, 0, ctx), () => example.test.Abc()).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.Abc](0)) :_*); i += 1 }
			if (i == started) {
				net.revenj.database.postgres.converters.StringConverter.skip(reader, context)
				i += 1
			}
		}
		reader.read(outerContext)
		example.test.AbcList(URI = _URI_, s = _s_, ii = _ii_, en = _en_, en2 = _en2_, en3 = _en3_, i4 = _i4_, another = _another_, v = _v_, vv = _vv_, x = _x_, f = _f_, bytes = _bytes_, bb = _bb_, vvv = _vvv_, hasV = _hasV_, hasA = _hasA_, ent1 = _ent1_, i = _i_, ent2 = _ent2_, abc1 = _abc1_, s2 = _s2_, abc2 = _abc2_)
	}

	def parseRawExtended(reader: PostgresReader, start: Int, context: Int): example.test.AbcList = {
		val result = fromExtended(reader, context, if (context == 0) 1 else context << 1)
		reader.read()
		result
	}

	def fromExtended(reader: PostgresReader, outerContext: Int, context: Int): example.test.AbcList = {
		reader.read(outerContext)
		var i = 0
		
		var _URI_ :String = null
		var _s_ :String = ""
		var _ii_ :Array[Int] = null
		var _en_ :example.test.En = null
		var _en2_ :Option[example.test.En] = None
		var _en3_ :scala.collection.mutable.LinkedList[example.test.En] = null
		var _i4_ :scala.collection.mutable.LinkedList[Int] = null
		var _another_ :scala.collection.mutable.LinkedList[example.test.Another] = null
		var _v_ :example.test.Val = null
		var _vv_ :Option[example.test.Val] = None
		var _x_ :Option[Int] = None
		var _f_ :Float = 0.0f
		var _bytes_ :Array[Byte] = Array[Byte]()
		var _bb_ :List[Array[Byte]] = null
		var _vvv_ :IndexedSeq[example.test.Val] = null
		var _hasV_ :Boolean = false
		var _hasA_ :Boolean = false
		var _ent1_ :example.test.Ent1 = null
		var _i_ :Int = 0
		var _ent2_ :Array[example.test.Ent2] = null
		var _abc1_ :Option[example.test.Abc] = None
		var _s2_ :Option[String] = None
		var _abc2_ :scala.collection.mutable.Queue[example.test.Abc] = null
		while(i < extendedReaders.length) {
			val started = i
			
			if (URIPosExtended == i) { _URI_ = net.revenj.database.postgres.converters.StringConverter.parse(reader, context); i += 1 }
			if (sPosExtended == i) { _s_ = net.revenj.database.postgres.converters.StringConverter.parse(reader, context); i += 1 }
			if (iiPosExtended == i) { _ii_ = net.revenj.database.postgres.converters.IntConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[Int](0)).toArray; i += 1 }
			if (enPosExtended == i) { _en_ = converterexample_test_En.parse(reader, context); i += 1 }
			if (en2PosExtended == i) { _en2_ = converterexample_test_En.parseOption(reader, context); i += 1 }
			if (en3PosExtended == i) { _en3_ = scala.collection.mutable.LinkedList[example.test.En](example.test.postgres.EnConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.En](0)) :_*); i += 1 }
			if (i4PosExtended == i) { _i4_ = scala.collection.mutable.LinkedList[Int](net.revenj.database.postgres.converters.IntConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[Int](0)) :_*); i += 1 }
			if (anotherPosExtended == i) { _another_ = scala.collection.mutable.LinkedList[example.test.Another](net.revenj.database.postgres.converters.ArrayTuple.parse(reader, context, (rdr, ctx) => converterexample_test_Another.fromExtended(rdr, 0, ctx), () => example.test.Another()).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.Another](0)) :_*); i += 1 }
			if (vPosExtended == i) { _v_ = converterexample_test_Val.parseExtended(reader, context); i += 1 }
			if (vvPosExtended == i) { _vv_ = converterexample_test_Val.parseOptionExtended(reader, context); i += 1 }
			if (xPosExtended == i) { _x_ = net.revenj.database.postgres.converters.IntConverter.parseOption(reader, context); i += 1 }
			if (fPosExtended == i) { _f_ = net.revenj.database.postgres.converters.FloatConverter.parse(reader, context); i += 1 }
			if (bytesPosExtended == i) { _bytes_ = net.revenj.database.postgres.converters.ByteaConverter.parse(reader, context); i += 1 }
			if (bbPosExtended == i) { _bb_ = net.revenj.database.postgres.converters.ByteaConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[Array[Byte]](0)).toList; i += 1 }
			if (vvvPosExtended == i) { _vvv_ = net.revenj.database.postgres.converters.ArrayTuple.parse(reader, context, (rdr, ctx) => converterexample_test_Val.fromExtended(rdr, 0, ctx), () => example.test.Val()).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.Val](0)).toIndexedSeq; i += 1 }
			if (hasVPosExtended == i) { _hasV_ = net.revenj.database.postgres.converters.BoolConverter.parse(reader, context); i += 1 }
			if (hasAPosExtended == i) { _hasA_ = net.revenj.database.postgres.converters.BoolConverter.parse(reader, context); i += 1 }
			if (ent1PosExtended == i) { _ent1_ = converterexample_test_Ent1.parseExtended(reader, context); i += 1 }
			if (iPosExtended == i) { _i_ = net.revenj.database.postgres.converters.IntConverter.parse(reader, context); i += 1 }
			if (ent2PosExtended == i) { _ent2_ = net.revenj.database.postgres.converters.ArrayTuple.parse(reader, context, (rdr, ctx) => converterexample_test_Ent2.fromExtended(rdr, 0, ctx), () => null).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.Ent2](0)).toArray; i += 1 }
			if (abc1PosExtended == i) { _abc1_ = converterexample_test_Abc.parseOptionExtended(reader, context); i += 1 }
			if (s2PosExtended == i) { _s2_ = net.revenj.database.postgres.converters.StringConverter.parseOption(reader, context); i += 1 }
			if (abc2PosExtended == i) { _abc2_ = scala.collection.mutable.Queue[example.test.Abc](net.revenj.database.postgres.converters.ArrayTuple.parse(reader, context, (rdr, ctx) => converterexample_test_Abc.fromExtended(rdr, 0, ctx), () => example.test.Abc()).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.Abc](0)) :_*); i += 1 }
			if (i == started) {
				net.revenj.database.postgres.converters.StringConverter.skip(reader, context)
				i += 1
			}
		}
		reader.read(outerContext)
		example.test.AbcList(URI = _URI_, s = _s_, ii = _ii_, en = _en_, en2 = _en2_, en3 = _en3_, i4 = _i4_, another = _another_, v = _v_, vv = _vv_, x = _x_, f = _f_, bytes = _bytes_, bb = _bb_, vvv = _vvv_, hasV = _hasV_, hasA = _hasA_, ent1 = _ent1_, i = _i_, ent2 = _ent2_, abc1 = _abc1_, s2 = _s2_, abc2 = _abc2_)
	}

	def initialize():Unit = {
		
	}
}
