package example.test




case class Struct @com.fasterxml.jackson.annotation.JsonIgnore() (
	   i: Int = 0,
	   af: Array[Float] = Array.empty,
	   a: example.test.Another = example.test.Another(),
	   vals: Set[example.test.Val] = Set.empty
	) {
	
		require(af ne null, "Null value was provided for property \"af\"")
		require(a ne null, "Null value was provided for property \"a\"")
		require(vals ne null, "Null value was provided for property \"vals\"")
		net.revenj.Guards.checkCollectionNulls(vals)
	
}

object Struct{

	
			
	@com.fasterxml.jackson.annotation.JsonCreator def jackson(
		@com.fasterxml.jackson.annotation.JsonProperty("i") i: Int,
		@com.fasterxml.jackson.annotation.JsonProperty("af") af: Array[Float],
		@com.fasterxml.jackson.annotation.JsonProperty("a") a: example.test.Another,
		@com.fasterxml.jackson.annotation.JsonProperty("vals") vals: Set[example.test.Val]) = {
		Struct(  i = i, af = if (af == null) Array.empty else af, a = if (a == null) example.test.Another() else a, vals = if (vals == null) Set.empty else vals)
	}

}
