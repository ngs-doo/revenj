package example.test




class Abc @com.fasterxml.jackson.annotation.JsonIgnore()  private(
	  private var _URI: String,
	  private var _ID: Int,
	  private var _s: String,
	  private var _ii: Array[Int],
	  private var _en: example.test.En,
	  private var _en2: Option[example.test.En],
	  private var _en3: scala.collection.mutable.LinkedList[example.test.En],
	  private var _i4: scala.collection.mutable.LinkedList[Int],
	  private var _another: scala.collection.mutable.LinkedList[example.test.Another],
	  private var _iii: Option[Array[Int]],
	  private var _iiii: Array[Option[Int]],
	  private var _ss: Option[String],
	  private var _sss: List[String],
	  private var _ssss: Option[List[Option[String]]],
	  private var _v: example.test.Val,
	  private var _vv: Option[example.test.Val],
	  private var _vvv: IndexedSeq[example.test.Val],
	  private var _a: Set[Option[example.test.Another]],
	  private var _ent1: example.test.Ent1,
	  private var _ent2: Array[example.test.Ent2],
	  @transient private var _abc1: Option[example.test.Abc],
	  private var _abc1URI: Option[String],
	  private var _abc1ID: Option[Int],
	  @transient private var _abc2: scala.collection.mutable.Queue[example.test.Abc],
	  private var _abc2URI: IndexedSeq[String],
	  @transient private val __locator: Option[net.revenj.patterns.ServiceLocator]
	) extends Serializable with net.revenj.patterns.AggregateRoot {
	
	
	
	
	@com.fasterxml.jackson.annotation.JsonProperty("URI")
	def URI = { 
		_URI
	}

	
	private [example] def URI_= (value: String) { 
		_URI = value
		
	}

	
	override def hashCode = URI.hashCode
	override def equals(o: Any) = o match {
		case c: Abc => c.URI == URI
		case _ => false
	}

	override def toString = "Abc("+ URI +")"
	
		
	 def copy(s: String = this._s, ii: Array[Int] = this._ii, en: example.test.En = this._en, en2: Option[example.test.En] = this._en2, en3: scala.collection.mutable.LinkedList[example.test.En] = this._en3, i4: scala.collection.mutable.LinkedList[Int] = this._i4, another: scala.collection.mutable.LinkedList[example.test.Another] = this._another, iii: Option[Array[Int]] = this._iii, iiii: Array[Option[Int]] = this._iiii, ss: Option[String] = this._ss, sss: List[String] = this._sss, ssss: Option[List[Option[String]]] = this._ssss, v: example.test.Val = this._v, vv: Option[example.test.Val] = this._vv, vvv: IndexedSeq[example.test.Val] = this._vvv, a: Set[Option[example.test.Another]] = this._a, ent1: example.test.Ent1 = this._ent1, ent2: Array[example.test.Ent2] = this._ent2, abc1: Option[example.test.Abc] = null, abc2: scala.collection.mutable.Queue[example.test.Abc] = null): Abc = {
		

			
	require(s ne null, "Null value was provided for property \"s\"")
	require(ii ne null, "Null value was provided for property \"ii\"")
	require(en ne null, "Null value was provided for property \"en\"")
	require(en2 ne null, "Null value was provided for property \"en2\"")
	if(en2.isDefined) require(en2.get ne null, "Null value was provided for property \"en2\"")
	require(en3 ne null, "Null value was provided for property \"en3\"")
	net.revenj.Guards.checkCollectionNulls(en3)
	require(i4 ne null, "Null value was provided for property \"i4\"")
	require(another ne null, "Null value was provided for property \"another\"")
	net.revenj.Guards.checkCollectionNulls(another)
	require(iii ne null, "Null value was provided for property \"iii\"")
	if(iii.isDefined) require(iii.get ne null, "Null value was provided for property \"iii\"")
	require(iiii ne null, "Null value was provided for property \"iiii\"")
	net.revenj.Guards.checkArrayOptionValNulls(iiii)
	require(ss ne null, "Null value was provided for property \"ss\"")
	if(ss.isDefined) require(ss.get ne null, "Null value was provided for property \"ss\"")
	require(sss ne null, "Null value was provided for property \"sss\"")
	net.revenj.Guards.checkCollectionNulls(sss)
	require(ssss ne null, "Null value was provided for property \"ssss\"")
	if(ssss.isDefined) require(ssss.get ne null, "Null value was provided for property \"ssss\"")
	net.revenj.Guards.checkCollectionOptionRefNulls(ssss)
	require(v ne null, "Null value was provided for property \"v\"")
	require(vv ne null, "Null value was provided for property \"vv\"")
	if(vv.isDefined) require(vv.get ne null, "Null value was provided for property \"vv\"")
	require(vvv ne null, "Null value was provided for property \"vvv\"")
	net.revenj.Guards.checkCollectionNulls(vvv)
	require(a ne null, "Null value was provided for property \"a\"")
	net.revenj.Guards.checkCollectionOptionRefNulls(a)
	require(ent1 ne null, "Null value was provided for property \"ent1\"")
	require(ent2 ne null, "Null value was provided for property \"ent2\"")
	net.revenj.Guards.checkArrayNulls(ent2)
	net.revenj.Guards.checkCollectionNulls(abc2)
		new Abc(_URI = this.URI, _ID = _ID, _s = s, _ii = ii, _en = en, _en2 = en2, _en3 = en3, _i4 = i4, _another = another, _iii = iii, _iiii = iiii, _ss = ss, _sss = sss, _ssss = ssss, _v = v, _vv = vv, _vvv = vvv, _a = a, _ent1 = ent1, _ent2 = ent2, _abc1 = if(abc1 != null) abc1 else _abc1, _abc1URI = if (abc1 != null) abc1.map(_.URI) else this._abc1URI, _abc1ID = if(abc1 != null) abc1.map(_.ID) else this._abc1ID, _abc2 = if(abc2 != null) abc2 else _abc2, _abc2URI = if (abc2 != null) abc2.toIndexedSeq.map(_.URI) else this._abc2URI, __locator = this.__locator)
	}

	
	@com.fasterxml.jackson.annotation.JsonCreator private def this(
		@com.fasterxml.jackson.annotation.JacksonInject("__locator") __locator__ : net.revenj.patterns.ServiceLocator
	, @com.fasterxml.jackson.annotation.JsonProperty("URI") URI: String
	, @com.fasterxml.jackson.annotation.JsonProperty("ID") ID: Int
	, @com.fasterxml.jackson.annotation.JsonProperty("s") s: String
	, @com.fasterxml.jackson.annotation.JsonProperty("ii") ii: Array[Int]
	, @com.fasterxml.jackson.annotation.JsonProperty("en") en: example.test.En
	, @com.fasterxml.jackson.annotation.JsonProperty("en2") en2: Option[example.test.En]
	, @com.fasterxml.jackson.annotation.JsonProperty("en3") en3: scala.collection.mutable.LinkedList[example.test.En]
	, @com.fasterxml.jackson.annotation.JsonProperty("i4") i4: scala.collection.mutable.LinkedList[Int]
	, @com.fasterxml.jackson.annotation.JsonProperty("another") another: scala.collection.mutable.LinkedList[example.test.Another]
	, @com.fasterxml.jackson.annotation.JsonProperty("iii") iii: Option[Array[Int]]
	, @com.fasterxml.jackson.annotation.JsonProperty("iiii") iiii: Array[Option[Int]]
	, @com.fasterxml.jackson.annotation.JsonProperty("ss") ss: Option[String]
	, @com.fasterxml.jackson.annotation.JsonProperty("sss") sss: List[String]
	, @com.fasterxml.jackson.annotation.JsonProperty("ssss") ssss: Option[List[Option[String]]]
	, @com.fasterxml.jackson.annotation.JsonProperty("v") v: example.test.Val
	, @com.fasterxml.jackson.annotation.JsonProperty("vv") vv: Option[example.test.Val]
	, @com.fasterxml.jackson.annotation.JsonProperty("vvv") vvv: IndexedSeq[example.test.Val]
	, @com.fasterxml.jackson.annotation.JsonProperty("a") a: Set[Option[example.test.Another]]
	, @com.fasterxml.jackson.annotation.JsonProperty("ent1") ent1: example.test.Ent1
	, @com.fasterxml.jackson.annotation.JsonProperty("ent2") ent2: Array[example.test.Ent2]
	, @com.fasterxml.jackson.annotation.JsonProperty("abc1URI") abc1URI: Option[String]
	, @com.fasterxml.jackson.databind.annotation.JsonDeserialize(contentAs = classOf[java.lang.Integer]) @com.fasterxml.jackson.annotation.JsonProperty("abc1ID") abc1ID: Option[Int]
	, @com.fasterxml.jackson.annotation.JsonProperty("abc2URI") abc2URI: IndexedSeq[String]
	) =
	  this(_URI = URI, __locator = Some(__locator__), _ID = ID, _s = if (s == null) "" else s, _ii = if (ii == null) Array.empty else ii, _en = if (en == null) example.test.En.A else en, _en2 = en2, _en3 = if (en3 == null) scala.collection.mutable.LinkedList.empty else en3, _i4 = if (i4 == null) scala.collection.mutable.LinkedList.empty else i4, _another = if (another == null) scala.collection.mutable.LinkedList.empty else another, _iii = iii, _iiii = if (iiii == null) Array.empty else iiii, _ss = ss, _sss = if (sss == null) List.empty else sss, _ssss = ssss, _v = if (v == null) example.test.Val() else v, _vv = vv, _vvv = if (vvv == null) IndexedSeq.empty else vvv, _a = if (a == null) Set.empty else a, _ent1 = ent1, _ent2 = if (ent2 == null) Array.empty else ent2, _abc1URI = abc1URI, _abc1 = null, _abc1ID = abc1ID, _abc2URI = if (abc2URI == null) IndexedSeq.empty else abc2URI, _abc2 = null)

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("ID")
	def ID = { 
		_ID
	}

	
	private [example] def ID_= (value: Int) { 
		_ID = value
		
		ent1.AbcID = value
		_ent2 foreach { child => child.AbcID = _ID	}
	}

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("s")
	def s = { 
		_s
	}

	
	def s_= (value: String) { 
		_s = value
		
	}

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("ii")
	def ii = { 
		_ii
	}

	
	def ii_= (value: Array[Int]) { 
		_ii = value
		
	}

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("en")
	def en = { 
		_en
	}

	
	def en_= (value: example.test.En) { 
		_en = value
		
	}

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("en2")
	def en2 = { 
		_en2
	}

	
	def en2_= (value: Option[example.test.En]) { 
		_en2 = value
		
	}

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("en3")
	def en3 = { 
		_en3
	}

	
	def en3_= (value: scala.collection.mutable.LinkedList[example.test.En]) { 
		net.revenj.Guards.checkCollectionNulls(value)
		_en3 = value
		
	}

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("i4")
	def i4 = { 
		_i4
	}

	
	def i4_= (value: scala.collection.mutable.LinkedList[Int]) { 
		_i4 = value
		
	}

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("another")
	def another = { 
		_another
	}

	
	def another_= (value: scala.collection.mutable.LinkedList[example.test.Another]) { 
		net.revenj.Guards.checkCollectionNulls(value)
		_another = value
		
	}

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("iii")
	def iii = { 
		_iii
	}

	
	def iii_= (value: Option[Array[Int]]) { 
		_iii = value
		
	}

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("iiii")
	def iiii = { 
		_iiii
	}

	
	def iiii_= (value: Array[Option[Int]]) { 
		net.revenj.Guards.checkArrayOptionValNulls(value)
		_iiii = value
		
	}

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("ss")
	def ss = { 
		_ss
	}

	
	def ss_= (value: Option[String]) { 
		_ss = value
		
	}

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("sss")
	def sss = { 
		_sss
	}

	
	def sss_= (value: List[String]) { 
		net.revenj.Guards.checkCollectionNulls(value)
		_sss = value
		
	}

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("ssss")
	def ssss = { 
		_ssss
	}

	
	def ssss_= (value: Option[List[Option[String]]]) { 
		net.revenj.Guards.checkCollectionOptionRefNulls(value)
		_ssss = value
		
	}

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("v")
	def v = { 
		_v
	}

	
	def v_= (value: example.test.Val) { 
		_v = value
		
	}

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("vv")
	def vv = { 
		_vv
	}

	
	def vv_= (value: Option[example.test.Val]) { 
		_vv = value
		
	}

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("vvv")
	def vvv = { 
		_vvv
	}

	
	def vvv_= (value: IndexedSeq[example.test.Val]) { 
		net.revenj.Guards.checkCollectionNulls(value)
		_vvv = value
		
	}

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("a")
	def a = { 
		_a
	}

	
	def a_= (value: Set[Option[example.test.Another]]) { 
		net.revenj.Guards.checkCollectionOptionRefNulls(value)
		_a = value
		
	}

	private var _hasV : Boolean = false
	
	
	@com.fasterxml.jackson.annotation.JsonProperty("hasV")
	def hasV = {
		
		_hasV = Abc.hasV(this)
		_hasV
	}

	private var _hasA : Boolean = false
	
	
	@com.fasterxml.jackson.annotation.JsonProperty("hasA")
	def hasA = {
		
		_hasA = Abc.hasA(this)
		_hasA
	}

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("ent1")
	def ent1 = { 
		_ent1
	}

	
	def ent1_= (value: example.test.Ent1) { 
		_ent1 = value
		
		
		value.AbcID = ID
	}

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("ent2")
	def ent2 = { 
		_ent2
	}

	
	def ent2_= (value: Array[example.test.Ent2]) { 
		net.revenj.Guards.checkArrayNulls(value)
		_ent2 = value
		
		
		_ent2.zipWithIndex foreach { case(child, i) =>
			child.Index = i
			child.AbcID = _ID
		}
					
	}

	
	
	@com.fasterxml.jackson.annotation.JsonIgnore
	def abc1(implicit ec: scala.concurrent.ExecutionContext, duration: scala.concurrent.duration.Duration) = { 
		if (_abc1URI.isEmpty && (_abc1 == null || _abc1.isDefined)) _abc1 = None
		if(__locator.isDefined) {
			if (_abc1URI != null && abc1URI.isDefined && (_abc1 == null || _abc1.isEmpty || _abc1.get.URI != abc1URI.get)) {
				_abc1 = scala.concurrent.Await.result(__locator.get.resolve[net.revenj.patterns.Repository[example.test.Abc]].find(abc1URI.get), duration)
				_abc1URI = null
			}
		}			
		_abc1
	}

	
	def abc1_= (value: Option[example.test.Abc]) { 
		_abc1 = value
		
		_abc1URI = value.map(_.URI)
		
		if (value.isEmpty && abc1ID != None)
			abc1ID = None
		else if(value.isDefined && abc1ID != value.map(_.ID))
			abc1ID = value.map(_.ID)
	}

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("abc1URI")
	def abc1URI = {
		
		_abc1URI
	}

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("abc1ID")
	 def abc1ID = { 
		_abc1ID
	}

	
	private [example] def abc1ID_= (value: Option[Int]) { 
		_abc1ID = value
		
	}

	
	
	@com.fasterxml.jackson.annotation.JsonIgnore
	def abc2(implicit ec: scala.concurrent.ExecutionContext, duration: scala.concurrent.duration.Duration) = { 
		if(__locator.isDefined) {
			if (_abc2URI != null && (_abc2 == null || _abc2.map(_.URI) != abc2URI)) {
				_abc2 = scala.concurrent.Await.result(__locator.get.resolve[net.revenj.patterns.Repository[example.test.Abc]].find(abc2URI).map(__col => scala.collection.mutable.Queue[example.test.Abc](__col:_*)), duration)
				_abc2URI = null
			}
		}
		_abc2
	}

	
	def abc2_= (value: scala.collection.mutable.Queue[example.test.Abc]) { 
		net.revenj.Guards.checkCollectionNulls(value)
		_abc2 = value
		
		_abc2URI = null
	}

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("abc2URI")
	def abc2URI: IndexedSeq[String] = {
		if (_abc2 != null) _abc2.toIndexedSeq.map(_.URI).toIndexedSeq
		else if (_abc2URI == null) IndexedSeq.empty[String]
		else _abc2URI
	}

}

object Abc{

	def apply(
		s: String = ""
	, ii: Array[Int] = Array.empty
	, en: example.test.En = example.test.En.A
	, en2: Option[example.test.En] = None
	, en3: scala.collection.mutable.LinkedList[example.test.En] = scala.collection.mutable.LinkedList.empty
	, i4: scala.collection.mutable.LinkedList[Int] = scala.collection.mutable.LinkedList.empty
	, another: scala.collection.mutable.LinkedList[example.test.Another] = scala.collection.mutable.LinkedList.empty
	, iii: Option[Array[Int]] = None
	, iiii: Array[Option[Int]] = Array.empty
	, ss: Option[String] = None
	, sss: List[String] = List.empty
	, ssss: Option[List[Option[String]]] = None
	, v: example.test.Val = example.test.Val()
	, vv: Option[example.test.Val] = None
	, vvv: IndexedSeq[example.test.Val] = IndexedSeq.empty
	, a: Set[Option[example.test.Another]] = Set.empty
	, ent1: example.test.Ent1 = example.test.Ent1(AbcID = 0)
	, ent2: Array[example.test.Ent2] = Array.empty
	, abc1: Option[example.test.Abc] = None
	, abc2: scala.collection.mutable.Queue[example.test.Abc] = scala.collection.mutable.Queue.empty
	) = {
		require(s ne null, "Null value was provided for property \"s\"")
		require(ii ne null, "Null value was provided for property \"ii\"")
		require(en ne null, "Null value was provided for property \"en\"")
		require(en2 ne null, "Null value was provided for property \"en2\"")
		if (en2.isDefined) require(en2.get ne null, "Null value was provided for property \"en2\"")
		require(en3 ne null, "Null value was provided for property \"en3\"")
		net.revenj.Guards.checkCollectionNulls(en3)
		require(i4 ne null, "Null value was provided for property \"i4\"")
		require(another ne null, "Null value was provided for property \"another\"")
		net.revenj.Guards.checkCollectionNulls(another)
		require(iii ne null, "Null value was provided for property \"iii\"")
		if (iii.isDefined) require(iii.get ne null, "Null value was provided for property \"iii\"")
		net.revenj.Guards.checkArrayOptionValNulls(iiii)
		require(iiii ne null, "Null value was provided for property \"iiii\"")
		require(ss ne null, "Null value was provided for property \"ss\"")
		if (ss.isDefined) require(ss.get ne null, "Null value was provided for property \"ss\"")
		net.revenj.Guards.checkCollectionNulls(sss)
		require(sss ne null, "Null value was provided for property \"sss\"")
		net.revenj.Guards.checkCollectionOptionRefNulls(ssss)
		require(ssss ne null, "Null value was provided for property \"ssss\"")
		if (ssss.isDefined) require(ssss.get ne null, "Null value was provided for property \"ssss\"")
		require(v ne null, "Null value was provided for property \"v\"")
		require(vv ne null, "Null value was provided for property \"vv\"")
		if (vv.isDefined) require(vv.get ne null, "Null value was provided for property \"vv\"")
		require(vvv ne null, "Null value was provided for property \"vvv\"")
		net.revenj.Guards.checkCollectionNulls(vvv)
		require(a ne null, "Null value was provided for property \"a\"")
		net.revenj.Guards.checkCollectionOptionRefNulls(a)
		require(ent1 ne null, "Null value was provided for property \"ent1\"")
		require(ent2 ne null, "Null value was provided for property \"ent2\"")
		net.revenj.Guards.checkArrayNulls(ent2)
		require(abc1 ne null, "Null value was provided for property \"abc1\"")
		if (abc1.isDefined) require(abc1.get ne null, "Null value was provided for property \"abc1\"")
		require(abc2 ne null, "Null value was provided for property \"abc2\"")
		net.revenj.Guards.checkCollectionNulls(abc2)
		new Abc(
			_URI = java.util.UUID.randomUUID.toString
		, _ID = 0
		, _s = s
		, _ii = ii
		, _en = en
		, _en2 = en2
		, _en3 = en3
		, _i4 = i4
		, _another = another
		, _iii = iii
		, _iiii = iiii
		, _ss = ss
		, _sss = sss
		, _ssss = ssss
		, _v = v
		, _vv = vv
		, _vvv = vvv
		, _a = a
		, _ent1 = ent1
		, _ent2 = ent2
		, _abc1 = abc1
		, _abc1URI = abc1.map(_.URI)
		, _abc1ID = abc1.map(_.ID)
		, _abc2 = abc2
		, _abc2URI = abc2.toIndexedSeq.map(_.URI)
		, __locator = None)
	}

	
		def hasV(it : example.test.Abc):Boolean = it.vv.isDefined
		def hasA(it : example.test.Abc): Boolean =  (it.a.size > 0)
		
	private [test] def insertLoop(aggregates: Seq[example.test.Abc], writer: net.revenj.database.postgres.PostgresWriter, locator: net.revenj.patterns.ServiceLocator, converter: example.test.postgres.AbcConverter, connection: java.sql.Connection): Unit = {
		
		val st = connection.prepareStatement("""/*NO LOAD BALANCE*/SELECT nextval('"test"."Abc_ID_seq"'::regclass)::int FROM generate_series(1, ?)""")
		st.setInt(1, aggregates.size)
		val rs = st.executeQuery()
		val iterator = aggregates.iterator
		while (rs.next()) {
			iterator.next().ID = rs.getInt(1)
		}
		rs.close()
		st.close()

		val iter = aggregates.iterator
		while (iter.hasNext) {
			val agg = iter.next()
			
						example.test.Ent1.bindToent1(agg, writer, locator)
						example.test.Ent2.bindToent2(agg, writer, locator) 
			agg.URI = converter.buildURI(writer, agg)
		}
	}
	private [test] def updateLoop(oldAggregates: Array[example.test.Abc], newAggregates: Array[example.test.Abc], writer: net.revenj.database.postgres.PostgresWriter, locator: net.revenj.patterns.ServiceLocator, converter: example.test.postgres.AbcConverter): Unit = {
		var i = 0
		while (i < newAggregates.length) {
			val oldAgg = oldAggregates(i)
			val newAgg = newAggregates(i)
			
						example.test.Ent1.bindToent1(newAgg, writer, locator)
						example.test.Ent2.bindToent2(newAgg, writer, locator) 
			newAgg.URI = converter.buildURI(writer, newAgg)
			i += 1
		}
	}
	private [test] def deleteLoop(aggregates: Seq[example.test.Abc], locator: net.revenj.patterns.ServiceLocator): Unit = {
		val iter = aggregates.iterator
		while (iter.hasNext) {
			val agg = iter.next() 
		}
	}
	private [test] def trackChanges(aggregate: example.test.Abc, locator: net.revenj.patterns.ServiceLocator): example.test.Abc = {
		var result: example.test.Abc = null
		result
	}
		
	private[test] def buildInternal(
		reader : net.revenj.database.postgres.PostgresReader,
		context: Int,
		converter: example.test.postgres.AbcConverter,
		converters: Array[(Abc, net.revenj.database.postgres.PostgresReader, Int) => Abc]) = {
		var instance = new Abc(
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
			_iii = None, 
			_iiii = null, 
			_ss = None, 
			_sss = null, 
			_ssss = None, 
			_v = null, 
			_vv = None, 
			_vvv = null, 
			_a = null, 
			_ent1 = null, 
			_ent2 = null, 
			_abc1 = None, 
			_abc1URI = None, 
			_abc1ID = None, 
			_abc2 = null, 
			_abc2URI = null)
		var i = 0
		while (i < converters.length) {
			instance = converters(i)(instance, reader, context)
			i += 1
		} 
		instance.URI = converter.buildURI(reader, instance)
		instance
	}

	private[test] def configureConverters(
		converters: Array[(Abc, net.revenj.database.postgres.PostgresReader, Int) => Abc], 
			IDPos: Int, 
			sPos: Int, 
			iiPos: Int, 
			enPos: Int, 
			en2Pos: Int, 
			en3Pos: Int, 
			i4Pos: Int, 
			anotherPos: Int,
		_converteranother : example.test.postgres.AnotherConverter, 
			iiiPos: Int, 
			iiiiPos: Int, 
			ssPos: Int, 
			sssPos: Int, 
			ssssPos: Int, 
			vPos: Int,
		_converterv : example.test.postgres.ValConverter, 
			vvPos: Int,
		_convertervv : example.test.postgres.ValConverter, 
			vvvPos: Int,
		_convertervvv : example.test.postgres.ValConverter, 
			aPos: Int,
		_convertera : example.test.postgres.AnotherConverter, 
			ent1Pos: Int,
		_converterent1 : example.test.postgres.Ent1Converter, 
			ent2Pos: Int,
		_converterent2 : example.test.postgres.Ent2Converter, 
			abc1URIPos: Int, 
			abc1IDPos: Int, 
			abc2URIPos: Int): Unit = {
		
			converters(IDPos) = (item, reader, context) => { item._ID = net.revenj.database.postgres.converters.IntConverter.parse(reader, context); item }
			converters(sPos) = (item, reader, context) => { item._s = net.revenj.database.postgres.converters.StringConverter.parse(reader, context); item }
			converters(iiPos) = (item, reader, context) => { item._ii = net.revenj.database.postgres.converters.IntConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[Int](0)).toArray; item }
			converters(enPos) = (item, reader, context) => { item._en = example.test.postgres.EnConverter.parse(reader, context); item }
			converters(en2Pos) = (item, reader, context) => { item._en2 = example.test.postgres.EnConverter.parseOption(reader, context); item }
			converters(en3Pos) = (item, reader, context) => { item._en3 = scala.collection.mutable.LinkedList[example.test.En](example.test.postgres.EnConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.En](0)) :_*); item }
			converters(i4Pos) = (item, reader, context) => { item._i4 = scala.collection.mutable.LinkedList[Int](net.revenj.database.postgres.converters.IntConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[Int](0)) :_*); item }
			converters(anotherPos) = (item, reader, context) => { item._another = scala.collection.mutable.LinkedList[example.test.Another](net.revenj.database.postgres.converters.ArrayTuple.parse(reader, context, (rdr, ctx) => _converteranother.from(rdr, 0, ctx), () => example.test.Another()).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.Another](0)) :_*); item }
			converters(iiiPos) = (item, reader, context) => { item._iii = net.revenj.database.postgres.converters.IntConverter.parseCollectionOption(reader, context).map(_.toArray); item }
			converters(iiiiPos) = (item, reader, context) => { item._iiii = net.revenj.database.postgres.converters.IntConverter.parseNullableCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[Option[Int]](0)).toArray; item }
			converters(ssPos) = (item, reader, context) => { item._ss = net.revenj.database.postgres.converters.StringConverter.parseOption(reader, context); item }
			converters(sssPos) = (item, reader, context) => { item._sss = net.revenj.database.postgres.converters.StringConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[String](0)).toList; item }
			converters(ssssPos) = (item, reader, context) => { item._ssss = net.revenj.database.postgres.converters.StringConverter.parseNullableCollectionOption(reader, context).map(_.toList); item }
			converters(vPos) = (item, reader, context) => { item._v = _converterv.parse(reader, context); item }
			converters(vvPos) = (item, reader, context) => { item._vv = _convertervv.parseOption(reader, context); item }
			converters(vvvPos) = (item, reader, context) => { item._vvv = net.revenj.database.postgres.converters.ArrayTuple.parse(reader, context, (rdr, ctx) => _convertervvv.from(rdr, 0, ctx), () => example.test.Val()).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.Val](0)).toIndexedSeq; item }
			converters(aPos) = (item, reader, context) => { item._a = net.revenj.database.postgres.converters.ArrayTuple.parseOption(reader, context, (rdr, ctx) => _convertera.from(rdr, 0, ctx)).getOrElse(new scala.collection.mutable.ArrayBuffer[Option[example.test.Another]](0)).toSet; item }
			converters(ent1Pos) = (item, reader, context) => { item._ent1 = _converterent1.parse(reader, context); item }
			converters(ent2Pos) = (item, reader, context) => { item._ent2 = net.revenj.database.postgres.converters.ArrayTuple.parse(reader, context, (rdr, ctx) => _converterent2.from(rdr, 0, ctx), () => null).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.Ent2](0)).toArray; item }
			converters(abc1URIPos) = (item, reader, context) => { item._abc1URI = net.revenj.database.postgres.converters.StringConverter.parseOption(reader, context); item }
			converters(abc1IDPos) = (item, reader, context) => { item._abc1ID = net.revenj.database.postgres.converters.IntConverter.parseOption(reader, context); item }
			converters(abc2URIPos) = (item, reader, context) => { item._abc2URI = net.revenj.database.postgres.converters.StringConverter.parseCollection(reader, context); item }
	}

	private[test] def configureExtendedConverters(
		converters: Array[(Abc, net.revenj.database.postgres.PostgresReader, Int) => Abc], 
			IDPosExtended: Int, 
			sPosExtended: Int, 
			iiPosExtended: Int, 
			enPosExtended: Int, 
			en2PosExtended: Int, 
			en3PosExtended: Int, 
			i4PosExtended: Int, 
			anotherPosExtended: Int,
		_converteranother : example.test.postgres.AnotherConverter, 
			iiiPosExtended: Int, 
			iiiiPosExtended: Int, 
			ssPosExtended: Int, 
			sssPosExtended: Int, 
			ssssPosExtended: Int, 
			vPosExtended: Int,
		_converterv : example.test.postgres.ValConverter, 
			vvPosExtended: Int,
		_convertervv : example.test.postgres.ValConverter, 
			vvvPosExtended: Int,
		_convertervvv : example.test.postgres.ValConverter, 
			aPosExtended: Int,
		_convertera : example.test.postgres.AnotherConverter, 
			ent1PosExtended: Int,
		_converterent1 : example.test.postgres.Ent1Converter, 
			ent2PosExtended: Int,
		_converterent2 : example.test.postgres.Ent2Converter, 
			abc1URIPosExtended: Int, 
			abc1IDPosExtended: Int, 
			abc2URIPosExtended: Int): Unit = {
		
			converters(IDPosExtended) = (item, reader, context) => { item._ID = net.revenj.database.postgres.converters.IntConverter.parse(reader, context); item }
			converters(sPosExtended) = (item, reader, context) => { item._s = net.revenj.database.postgres.converters.StringConverter.parse(reader, context); item }
			converters(iiPosExtended) = (item, reader, context) => { item._ii = net.revenj.database.postgres.converters.IntConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[Int](0)).toArray; item }
			converters(enPosExtended) = (item, reader, context) => { item._en = example.test.postgres.EnConverter.parse(reader, context); item }
			converters(en2PosExtended) = (item, reader, context) => { item._en2 = example.test.postgres.EnConverter.parseOption(reader, context); item }
			converters(en3PosExtended) = (item, reader, context) => { item._en3 = scala.collection.mutable.LinkedList[example.test.En](example.test.postgres.EnConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.En](0)) :_*); item }
			converters(i4PosExtended) = (item, reader, context) => { item._i4 = scala.collection.mutable.LinkedList[Int](net.revenj.database.postgres.converters.IntConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[Int](0)) :_*); item }
			converters(anotherPosExtended) = (item, reader, context) => { item._another = scala.collection.mutable.LinkedList[example.test.Another](net.revenj.database.postgres.converters.ArrayTuple.parse(reader, context, (rdr, ctx) => _converteranother.fromExtended(rdr, 0, ctx), () => example.test.Another()).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.Another](0)) :_*); item }
			converters(iiiPosExtended) = (item, reader, context) => { item._iii = net.revenj.database.postgres.converters.IntConverter.parseCollectionOption(reader, context).map(_.toArray); item }
			converters(iiiiPosExtended) = (item, reader, context) => { item._iiii = net.revenj.database.postgres.converters.IntConverter.parseNullableCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[Option[Int]](0)).toArray; item }
			converters(ssPosExtended) = (item, reader, context) => { item._ss = net.revenj.database.postgres.converters.StringConverter.parseOption(reader, context); item }
			converters(sssPosExtended) = (item, reader, context) => { item._sss = net.revenj.database.postgres.converters.StringConverter.parseCollectionOption(reader, context).getOrElse(new scala.collection.mutable.ArrayBuffer[String](0)).toList; item }
			converters(ssssPosExtended) = (item, reader, context) => { item._ssss = net.revenj.database.postgres.converters.StringConverter.parseNullableCollectionOption(reader, context).map(_.toList); item }
			converters(vPosExtended) = (item, reader, context) => { item._v = _converterv.parseExtended(reader, context); item }
			converters(vvPosExtended) = (item, reader, context) => { item._vv = _convertervv.parseOptionExtended(reader, context); item }
			converters(vvvPosExtended) = (item, reader, context) => { item._vvv = net.revenj.database.postgres.converters.ArrayTuple.parse(reader, context, (rdr, ctx) => _convertervvv.fromExtended(rdr, 0, ctx), () => example.test.Val()).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.Val](0)).toIndexedSeq; item }
			converters(aPosExtended) = (item, reader, context) => { item._a = net.revenj.database.postgres.converters.ArrayTuple.parseOption(reader, context, (rdr, ctx) => _convertera.fromExtended(rdr, 0, ctx)).getOrElse(new scala.collection.mutable.ArrayBuffer[Option[example.test.Another]](0)).toSet; item }
			converters(ent1PosExtended) = (item, reader, context) => { item._ent1 = _converterent1.parseExtended(reader, context); item }
			converters(ent2PosExtended) = (item, reader, context) => { item._ent2 = net.revenj.database.postgres.converters.ArrayTuple.parse(reader, context, (rdr, ctx) => _converterent2.fromExtended(rdr, 0, ctx), () => null).getOrElse(new scala.collection.mutable.ArrayBuffer[example.test.Ent2](0)).toArray; item }
			converters(abc1URIPosExtended) = (item, reader, context) => { item._abc1URI = net.revenj.database.postgres.converters.StringConverter.parseOption(reader, context); item }
			converters(abc1IDPosExtended) = (item, reader, context) => { item._abc1ID = net.revenj.database.postgres.converters.IntConverter.parseOption(reader, context); item }
			converters(abc2URIPosExtended) = (item, reader, context) => { item._abc2URI = net.revenj.database.postgres.converters.StringConverter.parseCollection(reader, context); item }
	}

}
