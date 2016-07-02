package example.test




case class AbcList @com.fasterxml.jackson.annotation.JsonIgnore() (
	   URI: String,
	   s: String,
	   ii: Array[Int],
	   en: example.test.En,
	   en2: Option[example.test.En],
	   en3: scala.collection.mutable.LinkedList[example.test.En],
	   i4: scala.collection.mutable.LinkedList[Int],
	   another: scala.collection.mutable.LinkedList[example.test.Another],
	   v: example.test.Val,
	   vv: Option[example.test.Val],
	   x: Option[Int],
	   f: Float,
	   vvv: IndexedSeq[example.test.Val],
	   hasV: Boolean,
	   hasA: Boolean,
	   ent1: example.test.Ent1,
	   i: Int,
	   ent2: Array[example.test.Ent2],
	   abc1: Option[example.test.Abc],
	   s2: Option[String],
	   abc2: scala.collection.mutable.Queue[example.test.Abc]
	) extends net.revenj.patterns.Identifiable {
	
	
	private var _hasV2 : Boolean = false
	
	
	@com.fasterxml.jackson.annotation.JsonProperty("hasV2")
	def hasV2 = {
		
		_hasV2 = AbcList.hasV2(this)
		_hasV2
	}

	private var _hasA2 : Boolean = false
	
	
	@com.fasterxml.jackson.annotation.JsonProperty("hasA2")
	def hasA2 = {
		
		_hasA2 = AbcList.hasA2(this)
		_hasA2
	}

	
	@com.fasterxml.jackson.annotation.JsonCreator private def this(
	  @com.fasterxml.jackson.annotation.JsonProperty("__helper__") __helper__ : Boolean
	, @com.fasterxml.jackson.annotation.JsonProperty("URI") URI: String
	, @com.fasterxml.jackson.annotation.JsonProperty("s") s: String
	, @com.fasterxml.jackson.annotation.JsonProperty("ii") ii: Array[Int]
	, @com.fasterxml.jackson.annotation.JsonProperty("en") en: example.test.En
	, @com.fasterxml.jackson.annotation.JsonProperty("en2") en2: Option[example.test.En]
	, @com.fasterxml.jackson.annotation.JsonProperty("en3") en3: scala.collection.mutable.LinkedList[example.test.En]
	, @com.fasterxml.jackson.annotation.JsonProperty("i4") i4: scala.collection.mutable.LinkedList[Int]
	, @com.fasterxml.jackson.annotation.JsonProperty("another") another: scala.collection.mutable.LinkedList[example.test.Another]
	, @com.fasterxml.jackson.annotation.JsonProperty("v") v: example.test.Val
	, @com.fasterxml.jackson.annotation.JsonProperty("vv") vv: Option[example.test.Val]
	, @com.fasterxml.jackson.databind.annotation.JsonDeserialize(contentAs = classOf[java.lang.Integer]) @com.fasterxml.jackson.annotation.JsonProperty("x") x: Option[Int]
	, @com.fasterxml.jackson.annotation.JsonProperty("f") f: Float
	, @com.fasterxml.jackson.annotation.JsonProperty("vvv") vvv: IndexedSeq[example.test.Val]
	, @com.fasterxml.jackson.annotation.JsonProperty("hasV") hasV: Boolean
	, @com.fasterxml.jackson.annotation.JsonProperty("hasA") hasA: Boolean
	, @com.fasterxml.jackson.annotation.JsonProperty("ent1") ent1: example.test.Ent1
	, @com.fasterxml.jackson.annotation.JsonProperty("i") i: Int
	, @com.fasterxml.jackson.annotation.JsonProperty("ent2") ent2: Array[example.test.Ent2]
	, @com.fasterxml.jackson.annotation.JsonProperty("abc1") abc1: Option[example.test.Abc]
	, @com.fasterxml.jackson.annotation.JsonProperty("s2") s2: Option[String]
	, @com.fasterxml.jackson.annotation.JsonProperty("abc2") abc2: scala.collection.mutable.Queue[example.test.Abc]
	) =
	  this(URI = URI, s = if (s == null) "" else s, ii = if (ii == null) Array.empty else ii, en = if (en == null) example.test.En.A else en, en2 = en2, en3 = if (en3 == null) scala.collection.mutable.LinkedList.empty else en3, i4 = if (i4 == null) scala.collection.mutable.LinkedList.empty else i4, another = if (another == null) scala.collection.mutable.LinkedList.empty else another, v = if (v == null) example.test.Val() else v, vv = vv, x = x, f = f, vvv = if (vvv == null) IndexedSeq.empty else vvv, hasV = hasV, hasA = hasA, ent1 = ent1, i = i, ent2 = if (ent2 == null) Array.empty else ent2, abc1 = abc1, s2 = s2, abc2 = if (abc2 == null) scala.collection.mutable.Queue.empty else abc2)

}

object AbcList{

	
			def hasV2(it : example.test.AbcList):Boolean = it.hasV
			def hasA2(it : example.test.AbcList): Boolean = it.hasA2
}
