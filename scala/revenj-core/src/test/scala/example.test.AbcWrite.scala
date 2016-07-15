package example.test




class AbcWrite private(
	  private var _URI: String,
	  @transient private val __locator: Option[net.revenj.patterns.ServiceLocator],
	  private var _ID: Int,
	  private var _s: String,
	  private var _ii: Array[Int],
	  private var _en: example.test.En,
	  private var _en2: Option[example.test.En],
	  private var _en3: scala.collection.mutable.LinkedList[example.test.En],
	  private var _i4: scala.collection.mutable.LinkedList[Int],
	  private var _another: scala.collection.mutable.LinkedList[example.test.Another],
	  private var _v: example.test.Val,
	  private var _vv: Option[example.test.Val],
	  private var _iii: Option[Array[Int]],
	  private var _iiii: Array[Option[Int]],
	  private var _ss: Option[String],
	  private var _vvv: IndexedSeq[example.test.Val],
	  private var _a: Set[Option[example.test.Another]],
	  private var _sss: List[String],
	  private var _ssss: Option[List[Option[String]]]
	) extends net.revenj.patterns.AggregateRoot with net.revenj.patterns.DataSource {
	
	
	
	
	def URI = { 
		_URI
	}

	
	private [example] def URI_= (value: String) { 
		_URI = value
		
	}

	
	override def hashCode = URI.hashCode
	override def equals(o: Any) = o match {
		case c: AbcWrite => c.URI == URI
		case _ => false
	}

	override def toString = "AbcWrite("+ URI +")"
	
		
	 def copy(ID: Int = this._ID, s: String = this._s, ii: Array[Int] = this._ii, en: example.test.En = this._en, en2: Option[example.test.En] = this._en2, en3: scala.collection.mutable.LinkedList[example.test.En] = this._en3, i4: scala.collection.mutable.LinkedList[Int] = this._i4, another: scala.collection.mutable.LinkedList[example.test.Another] = this._another, v: example.test.Val = this._v, vv: Option[example.test.Val] = this._vv, iii: Option[Array[Int]] = this._iii, iiii: Array[Option[Int]] = this._iiii, ss: Option[String] = this._ss, vvv: IndexedSeq[example.test.Val] = this._vvv, a: Set[Option[example.test.Another]] = this._a, sss: List[String] = this._sss, ssss: Option[List[Option[String]]] = this._ssss): AbcWrite = {
		

			
		new AbcWrite(_URI = this.URI, __locator = this.__locator, _ID = ID, _s = s, _ii = ii, _en = en, _en2 = en2, _en3 = en3, _i4 = i4, _another = another, _v = v, _vv = vv, _iii = iii, _iiii = iiii, _ss = ss, _vvv = vvv, _a = a, _sss = sss, _ssss = ssss)
	}

	
	
	def ID = { 
		_ID
	}

	
	def ID_= (value: Int) { 
		_ID = value
		
	}

	
	
	def s = { 
		_s
	}

	
	def s_= (value: String) { 
		_s = value
		
	}

	
	
	def ii = { 
		_ii
	}

	
	def ii_= (value: Array[Int]) { 
		_ii = value
		
	}

	
	
	def en = { 
		_en
	}

	
	def en_= (value: example.test.En) { 
		_en = value
		
	}

	
	
	def en2 = { 
		_en2
	}

	
	def en2_= (value: Option[example.test.En]) { 
		_en2 = value
		
	}

	
	
	def en3 = { 
		_en3
	}

	
	def en3_= (value: scala.collection.mutable.LinkedList[example.test.En]) { 
		_en3 = value
		
	}

	
	
	def i4 = { 
		_i4
	}

	
	def i4_= (value: scala.collection.mutable.LinkedList[Int]) { 
		_i4 = value
		
	}

	
	
	def another = { 
		_another
	}

	
	def another_= (value: scala.collection.mutable.LinkedList[example.test.Another]) { 
		_another = value
		
	}

	
	
	def v = { 
		_v
	}

	
	def v_= (value: example.test.Val) { 
		_v = value
		
	}

	
	
	def vv = { 
		_vv
	}

	
	def vv_= (value: Option[example.test.Val]) { 
		_vv = value
		
	}

	
	
	def iii = { 
		_iii
	}

	
	def iii_= (value: Option[Array[Int]]) { 
		_iii = value
		
	}

	
	
	def iiii = { 
		_iiii
	}

	
	def iiii_= (value: Array[Option[Int]]) { 
		_iiii = value
		
	}

	
	
	def ss = { 
		_ss
	}

	
	def ss_= (value: Option[String]) { 
		_ss = value
		
	}

	
	
	def vvv = { 
		_vvv
	}

	
	def vvv_= (value: IndexedSeq[example.test.Val]) { 
		_vvv = value
		
	}

	
	
	def a = { 
		_a
	}

	
	def a_= (value: Set[Option[example.test.Another]]) { 
		_a = value
		
	}

	
	
	def sss = { 
		_sss
	}

	
	def sss_= (value: List[String]) { 
		_sss = value
		
	}

	
	
	def ssss = { 
		_ssss
	}

	
	def ssss_= (value: Option[List[Option[String]]]) { 
		_ssss = value
		
	}

}

object AbcWrite{

	def apply(
		ID: Int = 0
	, s: String = ""
	, ii: Array[Int] = Array.empty
	, en: example.test.En = example.test.En.A
	, en2: Option[example.test.En] = None
	, en3: scala.collection.mutable.LinkedList[example.test.En] = scala.collection.mutable.LinkedList.empty
	, i4: scala.collection.mutable.LinkedList[Int] = scala.collection.mutable.LinkedList.empty
	, another: scala.collection.mutable.LinkedList[example.test.Another] = scala.collection.mutable.LinkedList.empty
	, v: example.test.Val = example.test.Val()
	, vv: Option[example.test.Val] = None
	, iii: Option[Array[Int]] = None
	, iiii: Array[Option[Int]] = Array.empty
	, ss: Option[String] = None
	, vvv: IndexedSeq[example.test.Val] = IndexedSeq.empty
	, a: Set[Option[example.test.Another]] = Set.empty
	, sss: List[String] = List.empty
	, ssss: Option[List[Option[String]]] = None
	) = {
		require(s ne null, "Null value was provided for property \"s\"")
		require(ii ne null, "Null value was provided for property \"ii\"")
		require(en ne null, "Null value was provided for property \"en\"")
		require(en2 ne null, "Null value was provided for property \"en2\"")
		if (en2.isDefined) require(en2.get ne null, "Null value was provided for property \"en2\"")
		require(en3 ne null, "Null value was provided for property \"en3\"")
		require(i4 ne null, "Null value was provided for property \"i4\"")
		require(another ne null, "Null value was provided for property \"another\"")
		require(v ne null, "Null value was provided for property \"v\"")
		require(vv ne null, "Null value was provided for property \"vv\"")
		if (vv.isDefined) require(vv.get ne null, "Null value was provided for property \"vv\"")
		require(iii ne null, "Null value was provided for property \"iii\"")
		if (iii.isDefined) require(iii.get ne null, "Null value was provided for property \"iii\"")
		require(iiii ne null, "Null value was provided for property \"iiii\"")
		require(ss ne null, "Null value was provided for property \"ss\"")
		if (ss.isDefined) require(ss.get ne null, "Null value was provided for property \"ss\"")
		require(vvv ne null, "Null value was provided for property \"vvv\"")
		require(a ne null, "Null value was provided for property \"a\"")
		require(sss ne null, "Null value was provided for property \"sss\"")
		require(ssss ne null, "Null value was provided for property \"ssss\"")
		if (ssss.isDefined) require(ssss.get ne null, "Null value was provided for property \"ssss\"")
		new AbcWrite(
			_URI = java.util.UUID.randomUUID.toString
		, __locator = None
		, _ID = ID
		, _s = s
		, _ii = ii
		, _en = en
		, _en2 = en2
		, _en3 = en3
		, _i4 = i4
		, _another = another
		, _v = v
		, _vv = vv
		, _iii = iii
		, _iiii = iiii
		, _ss = ss
		, _vvv = vvv
		, _a = a
		, _sss = sss
		, _ssss = ssss)
	}

	
		
	@com.fasterxml.jackson.annotation.JsonCreator def jackson(
		@com.fasterxml.jackson.annotation.JsonProperty("ID") ID: Int,
		@com.fasterxml.jackson.annotation.JsonProperty("s") s: String,
		@com.fasterxml.jackson.annotation.JsonProperty("ii") ii: Array[Int],
		@com.fasterxml.jackson.annotation.JsonProperty("en") en: example.test.En,
		@com.fasterxml.jackson.annotation.JsonProperty("en2") en2: Option[example.test.En],
		@com.fasterxml.jackson.annotation.JsonProperty("en3") en3: scala.collection.mutable.LinkedList[example.test.En],
		@com.fasterxml.jackson.annotation.JsonProperty("i4") i4: scala.collection.mutable.LinkedList[Int],
		@com.fasterxml.jackson.annotation.JsonProperty("another") another: scala.collection.mutable.LinkedList[example.test.Another],
		@com.fasterxml.jackson.annotation.JsonProperty("v") v: example.test.Val,
		@com.fasterxml.jackson.annotation.JsonProperty("vv") vv: Option[example.test.Val],
		@com.fasterxml.jackson.annotation.JsonProperty("iii") iii: Option[Array[Int]],
		@com.fasterxml.jackson.annotation.JsonProperty("iiii") iiii: Array[Option[Int]],
		@com.fasterxml.jackson.annotation.JsonProperty("ss") ss: Option[String],
		@com.fasterxml.jackson.annotation.JsonProperty("vvv") vvv: IndexedSeq[example.test.Val],
		@com.fasterxml.jackson.annotation.JsonProperty("a") a: Set[Option[example.test.Another]],
		@com.fasterxml.jackson.annotation.JsonProperty("sss") sss: List[String],
		@com.fasterxml.jackson.annotation.JsonProperty("ssss") ssss: Option[List[Option[String]]]) = {
		AbcWrite(  ID = ID, s = if (s == null) "" else s, ii = if (ii == null) Array.empty else ii, en = if (en == null) example.test.En.A else en, en2 = en2, en3 = if (en3 == null) scala.collection.mutable.LinkedList.empty else en3, i4 = if (i4 == null) scala.collection.mutable.LinkedList.empty else i4, another = if (another == null) scala.collection.mutable.LinkedList.empty else another, v = if (v == null) example.test.Val() else v, vv = vv, iii = iii, iiii = if (iiii == null) Array.empty else iiii, ss = ss, vvv = if (vvv == null) IndexedSeq.empty else vvv, a = if (a == null) Set.empty else a, sss = if (sss == null) List.empty else sss, ssss = ssss)
	}

		
	private [test] def insertLoop(aggregates: Seq[example.test.AbcWrite], writer: net.revenj.database.postgres.PostgresWriter, locator: net.revenj.patterns.ServiceLocator, converter: example.test.postgres.AbcWriteConverter, connection: java.sql.Connection): Unit = {
		val iter = aggregates.iterator
		while (iter.hasNext) {
			val agg = iter.next()
			 
			agg.URI = converter.buildURI(writer, agg)
		}
	}
	private [test] def updateLoop(oldAggregates: Array[example.test.AbcWrite], newAggregates: Array[example.test.AbcWrite], writer: net.revenj.database.postgres.PostgresWriter, locator: net.revenj.patterns.ServiceLocator, converter: example.test.postgres.AbcWriteConverter): Unit = {
		var i = 0
		while (i < newAggregates.length) {
			val oldAgg = oldAggregates(i)
			val newAgg = newAggregates(i)
			 
			newAgg.URI = converter.buildURI(writer, newAgg)
			i += 1
		}
	}
	private [test] def deleteLoop(aggregates: Seq[example.test.AbcWrite], locator: net.revenj.patterns.ServiceLocator): Unit = {
		val iter = aggregates.iterator
		while (iter.hasNext) {
			val agg = iter.next() 
		}
	}
	private [test] def trackChanges(aggregate: example.test.AbcWrite, locator: net.revenj.patterns.ServiceLocator): example.test.AbcWrite = {
		var result: example.test.AbcWrite = null
		result
	}
		
	private[test] def buildInternal(
		reader : net.revenj.database.postgres.PostgresReader,
		context: Int,
		converter: example.test.postgres.AbcWriteConverter,
		converters: Array[(AbcWrite, net.revenj.database.postgres.PostgresReader, Int) => AbcWrite]) = {
		var instance = new AbcWrite(
			__locator = reader.locator, 
			_URI = null, 
			_ID = 0, 
			_s = "", 
			_ii = null, 
			_en = null, 
			_en2 = None, 
			_en3 = null, 
			_i4 = null, 
			_another = null, 
			_v = null, 
			_vv = None, 
			_iii = None, 
			_iiii = null, 
			_ss = None, 
			_vvv = null, 
			_a = null, 
			_sss = null, 
			_ssss = None)
		var i = 0
		while (i < converters.length) {
			instance = converters(i)(instance, reader, context)
			i += 1
		} 
		instance.URI = converter.buildURI(reader, instance)
		instance
	}

	private[test] def configureConverters(
		converters: Array[(AbcWrite, net.revenj.database.postgres.PostgresReader, Int) => AbcWrite], 
			IDPos: Int, 
			sPos: Int, 
			iiPos: Int, 
			enPos: Int, 
			en2Pos: Int, 
			en3Pos: Int, 
			i4Pos: Int, 
			anotherPos: Int,
		_converteranother : example.test.postgres.AnotherConverter, 
			vPos: Int,
		_converterv : example.test.postgres.ValConverter, 
			vvPos: Int,
		_convertervv : example.test.postgres.ValConverter, 
			iiiPos: Int, 
			iiiiPos: Int, 
			ssPos: Int, 
			vvvPos: Int,
		_convertervvv : example.test.postgres.ValConverter, 
			aPos: Int,
		_convertera : example.test.postgres.AnotherConverter, 
			sssPos: Int, 
			ssssPos: Int): Unit = {
		
			converters(IDPos) = (item, reader, context) => { item._ID = net.revenj.database.postgres.converters.IntConverter.parse(reader, context); item }
			converters(sPos) = (item, reader, context) => { item._s = net.revenj.database.postgres.converters.StringConverter.parse(reader, context); item }
			converters(iiPos) = (item, reader, context) => { item._ii = net.revenj.database.postgres.converters.IntConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[Int](0)).toArray; item }
			converters(enPos) = (item, reader, context) => { item._en = example.test.postgres.EnConverter.parse(reader, context); item }
			converters(en2Pos) = (item, reader, context) => { item._en2 = example.test.postgres.EnConverter.parseOption(reader, context); item }
			converters(en3Pos) = (item, reader, context) => { item._en3 = scala.collection.mutable.LinkedList[example.test.En](example.test.postgres.EnConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.En](0)) :_*); item }
			converters(i4Pos) = (item, reader, context) => { item._i4 = scala.collection.mutable.LinkedList[Int](net.revenj.database.postgres.converters.IntConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[Int](0)) :_*); item }
			converters(anotherPos) = (item, reader, context) => { item._another = scala.collection.mutable.LinkedList[example.test.Another](net.revenj.database.postgres.converters.ArrayTuple.parse(reader, context, (rdr, ctx) => _converteranother.from(rdr, 0, ctx), () => example.test.Another()).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.Another](0)) :_*); item }
			converters(vPos) = (item, reader, context) => { item._v = _converterv.parse(reader, context); item }
			converters(vvPos) = (item, reader, context) => { item._vv = _convertervv.parseOption(reader, context); item }
			converters(iiiPos) = (item, reader, context) => { item._iii = net.revenj.database.postgres.converters.IntConverter.parseCollectionOption(reader, context).map(_.toArray); item }
			converters(iiiiPos) = (item, reader, context) => { item._iiii = net.revenj.database.postgres.converters.IntConverter.parseNullableCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[Option[Int]](0)).toArray; item }
			converters(ssPos) = (item, reader, context) => { item._ss = net.revenj.database.postgres.converters.StringConverter.parseOption(reader, context); item }
			converters(vvvPos) = (item, reader, context) => { item._vvv = net.revenj.database.postgres.converters.ArrayTuple.parse(reader, context, (rdr, ctx) => _convertervvv.from(rdr, 0, ctx), () => example.test.Val()).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.Val](0)).toIndexedSeq; item }
			converters(aPos) = (item, reader, context) => { item._a = net.revenj.database.postgres.converters.ArrayTuple.parseOption(reader, context, (rdr, ctx) => _convertera.from(rdr, 0, ctx)).getOrElse(new scala.collection.mutable.ArrayBuffer[Option[example.test.Another]](0)).toSet; item }
			converters(sssPos) = (item, reader, context) => { item._sss = net.revenj.database.postgres.converters.StringConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[String](0)).toList; item }
			converters(ssssPos) = (item, reader, context) => { item._ssss = net.revenj.database.postgres.converters.StringConverter.parseNullableCollectionOption(reader, context).map(_.toList); item }
	}

	private[test] def configureExtendedConverters(
		converters: Array[(AbcWrite, net.revenj.database.postgres.PostgresReader, Int) => AbcWrite], 
			IDPosExtended: Int, 
			sPosExtended: Int, 
			iiPosExtended: Int, 
			enPosExtended: Int, 
			en2PosExtended: Int, 
			en3PosExtended: Int, 
			i4PosExtended: Int, 
			anotherPosExtended: Int,
		_converteranother : example.test.postgres.AnotherConverter, 
			vPosExtended: Int,
		_converterv : example.test.postgres.ValConverter, 
			vvPosExtended: Int,
		_convertervv : example.test.postgres.ValConverter, 
			iiiPosExtended: Int, 
			iiiiPosExtended: Int, 
			ssPosExtended: Int, 
			vvvPosExtended: Int,
		_convertervvv : example.test.postgres.ValConverter, 
			aPosExtended: Int,
		_convertera : example.test.postgres.AnotherConverter, 
			sssPosExtended: Int, 
			ssssPosExtended: Int): Unit = {
		
			converters(IDPosExtended) = (item, reader, context) => { item._ID = net.revenj.database.postgres.converters.IntConverter.parse(reader, context); item }
			converters(sPosExtended) = (item, reader, context) => { item._s = net.revenj.database.postgres.converters.StringConverter.parse(reader, context); item }
			converters(iiPosExtended) = (item, reader, context) => { item._ii = net.revenj.database.postgres.converters.IntConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[Int](0)).toArray; item }
			converters(enPosExtended) = (item, reader, context) => { item._en = example.test.postgres.EnConverter.parse(reader, context); item }
			converters(en2PosExtended) = (item, reader, context) => { item._en2 = example.test.postgres.EnConverter.parseOption(reader, context); item }
			converters(en3PosExtended) = (item, reader, context) => { item._en3 = scala.collection.mutable.LinkedList[example.test.En](example.test.postgres.EnConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.En](0)) :_*); item }
			converters(i4PosExtended) = (item, reader, context) => { item._i4 = scala.collection.mutable.LinkedList[Int](net.revenj.database.postgres.converters.IntConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[Int](0)) :_*); item }
			converters(anotherPosExtended) = (item, reader, context) => { item._another = scala.collection.mutable.LinkedList[example.test.Another](net.revenj.database.postgres.converters.ArrayTuple.parse(reader, context, (rdr, ctx) => _converteranother.fromExtended(rdr, 0, ctx), () => example.test.Another()).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.Another](0)) :_*); item }
			converters(vPosExtended) = (item, reader, context) => { item._v = _converterv.parseExtended(reader, context); item }
			converters(vvPosExtended) = (item, reader, context) => { item._vv = _convertervv.parseOptionExtended(reader, context); item }
			converters(iiiPosExtended) = (item, reader, context) => { item._iii = net.revenj.database.postgres.converters.IntConverter.parseCollectionOption(reader, context).map(_.toArray); item }
			converters(iiiiPosExtended) = (item, reader, context) => { item._iiii = net.revenj.database.postgres.converters.IntConverter.parseNullableCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[Option[Int]](0)).toArray; item }
			converters(ssPosExtended) = (item, reader, context) => { item._ss = net.revenj.database.postgres.converters.StringConverter.parseOption(reader, context); item }
			converters(vvvPosExtended) = (item, reader, context) => { item._vvv = net.revenj.database.postgres.converters.ArrayTuple.parse(reader, context, (rdr, ctx) => _convertervvv.fromExtended(rdr, 0, ctx), () => example.test.Val()).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.Val](0)).toIndexedSeq; item }
			converters(aPosExtended) = (item, reader, context) => { item._a = net.revenj.database.postgres.converters.ArrayTuple.parseOption(reader, context, (rdr, ctx) => _convertera.fromExtended(rdr, 0, ctx)).getOrElse(new scala.collection.mutable.ArrayBuffer[Option[example.test.Another]](0)).toSet; item }
			converters(sssPosExtended) = (item, reader, context) => { item._sss = net.revenj.database.postgres.converters.StringConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[String](0)).toList; item }
			converters(ssssPosExtended) = (item, reader, context) => { item._ssss = net.revenj.database.postgres.converters.StringConverter.parseNullableCollectionOption(reader, context).map(_.toList); item }
	}

}
