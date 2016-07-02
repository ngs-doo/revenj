package example.test




class Ent1 @com.fasterxml.jackson.annotation.JsonIgnore()  private(
	  private var _URI: String,
	  private var _i: Int,
	  private var _AbcID: Int,
	  @transient private val __locator: Option[net.revenj.patterns.ServiceLocator]
	) extends Serializable {
	
	
	
	
	@com.fasterxml.jackson.annotation.JsonProperty("URI")
	def URI = { 
		_URI
	}

	
	private [example] def URI_= (value: String) { 
		_URI = value
		
	}

	
	override def hashCode = URI.hashCode
	override def equals(o: Any) = o match {
		case c: Ent1 => c.URI == URI
		case _ => false
	}

	override def toString = "Ent1("+ URI +")"
	
		
	 def copy(i: Int = this._i, AbcID: Int = this._AbcID): Ent1 = {
		

			
		new Ent1(_URI = this.URI, _i = i, _AbcID = AbcID, __locator = this.__locator)
	}

	
	@com.fasterxml.jackson.annotation.JsonCreator private def this(
		@com.fasterxml.jackson.annotation.JacksonInject("__locator") __locator__ : net.revenj.patterns.ServiceLocator
	, @com.fasterxml.jackson.annotation.JsonProperty("URI") URI: String
	, @com.fasterxml.jackson.annotation.JsonProperty("i") i: Int
	, @com.fasterxml.jackson.annotation.JsonProperty("AbcID") AbcID: Int
	) =
	  this(_URI = URI, __locator = Some(__locator__), _i = i, _AbcID = AbcID)

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("i")
	def i = { 
		_i
	}

	
	def i_= (value: Int) { 
		_i = value
		
	}

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("AbcID")
	 def AbcID = { 
		_AbcID
	}

	
	private [example] def AbcID_= (value: Int) { 
		_AbcID = value
		
	}

}

object Ent1{

	def apply(
		i: Int = 0
	, AbcID: Int
	) = {
		new Ent1(
			_URI = java.util.UUID.randomUUID.toString
		, _i = i
		, _AbcID = AbcID
		, __locator = None)
	}

	
			
	private [test] def bindToent1(parent: example.test.Abc, writer: net.revenj.database.postgres.PostgresWriter, locator: net.revenj.patterns.ServiceLocator): Unit = {
		val e = parent.ent1
		if (e != null) {
			e.AbcID = parent.ID
			e.URI = locator.resolve[example.test.postgres.Ent1Converter].buildURI(writer, e)
		}
	}
			
	private[test] def buildInternal(
		reader : net.revenj.database.postgres.PostgresReader,
		context: Int,
		converter: example.test.postgres.Ent1Converter,
		converters: Array[(Ent1, net.revenj.database.postgres.PostgresReader, Int) => Ent1]) = {
		var instance = new Ent1(
			__locator = reader.locator, 
			_URI = null, 
			_i = 0, 
			_AbcID = 0)
		var i = 0
		while (i < converters.length) {
			instance = converters(i)(instance, reader, context)
			i += 1
		} 
		instance.URI = converter.buildURI(reader, instance)
		instance
	}

	private[test] def configureConverters(
		converters: Array[(Ent1, net.revenj.database.postgres.PostgresReader, Int) => Ent1], 
			iPos: Int, 
			AbcIDPos: Int): Unit = {
		
			converters(iPos) = (item, reader, context) => { item._i = net.revenj.database.postgres.converters.IntConverter.parse(reader, context); item }
			converters(AbcIDPos) = (item, reader, context) => { item._AbcID = net.revenj.database.postgres.converters.IntConverter.parse(reader, context); item }
	}

	private[test] def configureExtendedConverters(
		converters: Array[(Ent1, net.revenj.database.postgres.PostgresReader, Int) => Ent1], 
			iPosExtended: Int, 
			AbcIDPosExtended: Int): Unit = {
		
			converters(iPosExtended) = (item, reader, context) => { item._i = net.revenj.database.postgres.converters.IntConverter.parse(reader, context); item }
			converters(AbcIDPosExtended) = (item, reader, context) => { item._AbcID = net.revenj.database.postgres.converters.IntConverter.parse(reader, context); item }
	}

}
