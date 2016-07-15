package example.test.postgres



import net.revenj.database.postgres._
import net.revenj.database.postgres.converters._

object EnConverter extends Converter[example.test.En] {
	private val DEFAULT_TUPLE = new net.revenj.database.postgres.converters.EnumTuple("A")
	
	val TUPLE_A = new net.revenj.database.postgres.converters.EnumTuple("A")
	val TUPLE_B = new net.revenj.database.postgres.converters.EnumTuple("B")
	val TUPLE_C = new net.revenj.database.postgres.converters.EnumTuple("C")

	val dbName = "\"test\".\"En\""
	def default() = example.test.En.A

	override def serializeURI(sw: PostgresBuffer, value: example.test.En): Unit = {
		sw.addToBuffer(EnConverter.stringValue(value))
	}

	override def parseRaw(reader: PostgresReader, start: Int, context: Int): example.test.En = {
		reader.initBuffer(start.toChar)
		reader.fillUntil(',', ')')
		val result = EnConverter.convertEnum(reader)
		reader.read()
		result
	}

	override def parseCollectionItem(reader: PostgresReader, context: Int): example.test.En = {
		reader.initBuffer(reader.last.toChar)
		reader.fillUntil(',', '}')
		val result = EnConverter.convertEnum(reader)
		reader.read()
		result
	}

	override def parseNullableCollectionItem(reader: PostgresReader, context: Int): Option[example.test.En] = Some(parseCollectionItem(reader, context))

	
	

	override def toTuple(instance: example.test.En): PostgresTuple = {
		instance match {
			
			case example.test.En.A => TUPLE_A
			case example.test.En.B => TUPLE_B
			case example.test.En.C => TUPLE_C
			case _ => DEFAULT_TUPLE
		}	
	}

	def stringValue(item: example.test.En): String = {
		item match {
			
			case example.test.En.A => "A"
			case example.test.En.B => "B"
			case example.test.En.C => "C"
			case _ => ""
		}
	}

	def convertEnum(reader: PostgresReader): example.test.En = {
		
		reader.bufferHash match { 
			case -1005848884 => example.test.En.A
			case -955516027 => example.test.En.B
			case -972293646 => example.test.En.C
			case _ => example.test.En.A
		}
	}
}
