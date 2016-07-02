package example.test




case class Struct(
	  @com.fasterxml.jackson.annotation.JsonProperty("ii") i: Int = 0,
	  @com.fasterxml.jackson.annotation.JsonProperty("af") af: Array[Float] = Array.empty,
	  @com.fasterxml.jackson.annotation.JsonProperty("a") a: example.test.Another = example.test.Another(),
	  @com.fasterxml.jackson.annotation.JsonProperty("v") vals: Set[example.test.Val] = Set.empty
	) {
	
		require(af ne null, "Null value was provided for property \"af\"")
		require(a ne null, "Null value was provided for property \"a\"")
		require(vals ne null, "Null value was provided for property \"vals\"")
		net.revenj.Guards.checkCollectionNulls(vals)
	
}

object Struct{

	
			
	@com.fasterxml.jackson.annotation.JsonCreator def jackson(
		@com.fasterxml.jackson.annotation.JsonProperty("ii") i: Int,
		@com.fasterxml.jackson.annotation.JsonProperty("af") af: Array[Float],
		@com.fasterxml.jackson.annotation.JsonProperty("a") a: example.test.Another,
		@com.fasterxml.jackson.annotation.JsonProperty("v") vals: Set[example.test.Val]) = {
		Struct(  i = i, af = if (af == null) Array.empty else af, a = if (a == null) example.test.Another() else a, vals = if (vals == null) Set.empty else vals)
	}

}
