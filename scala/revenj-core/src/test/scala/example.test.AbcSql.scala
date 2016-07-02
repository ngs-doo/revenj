package example.test




case class AbcSql @com.fasterxml.jackson.annotation.JsonIgnore() (
	  @com.fasterxml.jackson.annotation.JsonProperty("s") s: String = "",
	  @com.fasterxml.jackson.annotation.JsonProperty("ii") ii: Array[Int] = Array.empty,
	  @com.fasterxml.jackson.annotation.JsonProperty("en") en: example.test.En = example.test.En.A,
	  @com.fasterxml.jackson.annotation.JsonProperty("en2") en2: Option[example.test.En] = None,
	  @com.fasterxml.jackson.annotation.JsonProperty("en3") en3: scala.collection.mutable.LinkedList[example.test.En] = scala.collection.mutable.LinkedList.empty,
	  @com.fasterxml.jackson.annotation.JsonProperty("i4") i4: scala.collection.mutable.LinkedList[Int] = scala.collection.mutable.LinkedList.empty
	) extends net.revenj.patterns.DataSource {
	
	
}

object AbcSql{

	
		
	@com.fasterxml.jackson.annotation.JsonCreator def jackson(
		@com.fasterxml.jackson.annotation.JsonProperty("s") s: String,
		@com.fasterxml.jackson.annotation.JsonProperty("ii") ii: Array[Int],
		@com.fasterxml.jackson.annotation.JsonProperty("en") en: example.test.En,
		@com.fasterxml.jackson.annotation.JsonProperty("en2") en2: Option[example.test.En],
		@com.fasterxml.jackson.annotation.JsonProperty("en3") en3: scala.collection.mutable.LinkedList[example.test.En],
		@com.fasterxml.jackson.annotation.JsonProperty("i4") i4: scala.collection.mutable.LinkedList[Int]) = {
		AbcSql(  s = if (s == null) "" else s, ii = if (ii == null) Array.empty else ii, en = if (en == null) example.test.En.A else en, en2 = en2, en3 = if (en3 == null) scala.collection.mutable.LinkedList.empty else en3, i4 = if (i4 == null) scala.collection.mutable.LinkedList.empty else i4)
	}

}
