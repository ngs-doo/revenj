package example.test



@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using=classOf[_EnDeserializer])
@com.fasterxml.jackson.databind.annotation.JsonSerialize(using=classOf[_EnSerializer])
sealed trait En
object En { 
case object A extends En
case object B extends En
case object C extends En
  val values = IndexedSeq[En](A, B, C)
}
private class _EnSerializer extends com.fasterxml.jackson.databind.JsonSerializer[En] {
	def serialize(value: En, jgen: com.fasterxml.jackson.core.JsonGenerator, provider: com.fasterxml.jackson.databind.SerializerProvider) {
		value match {
			case En.A => jgen.writeString("A")
			case En.B => jgen.writeString("B")
			case En.C => jgen.writeString("C")
			case _ => jgen.writeString(String.valueOf(value))
		}
	}
}
private class _EnDeserializer extends com.fasterxml.jackson.databind.JsonDeserializer[En] {
	override def getNullValue() = En.A
	override def getEmptyValue() = En.A

	def deserialize(parser: com.fasterxml.jackson.core.JsonParser, context: com.fasterxml.jackson.databind.DeserializationContext): En = {
		parser.getText() match {
			case "A" => En.A
			case "B" => En.B
			case "C" => En.C
			case value => throw new com.fasterxml.jackson.databind.JsonMappingException("""Could not deserialize "En", got: """ + value)
		}
	}
}