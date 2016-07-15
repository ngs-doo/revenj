package example.test




class Ent3 @com.fasterxml.jackson.annotation.JsonIgnore()  private(
	  private var _URI: String,
	  private var _id: Int,
	  private var _i: Int,
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
		case c: Ent3 => c.URI == URI
		case _ => false
	}

	override def toString = "Ent3("+ URI +")"
	
		
	 def copy(id: Int = this._id, i: Int = this._i): Ent3 = {
		

			
		new Ent3(_URI = this.URI, _id = id, _i = i, __locator = this.__locator)
	}

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("id")
	def id = { 
		_id
	}

	
	def id_= (value: Int) { 
		_id = value
		
	}

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("i")
	def i = { 
		_i
	}

	
	def i_= (value: Int) { 
		_i = value
		
	}

	
	@com.fasterxml.jackson.annotation.JsonCreator private def this(
		@com.fasterxml.jackson.annotation.JacksonInject("__locator") __locator__ : net.revenj.patterns.ServiceLocator
	, @com.fasterxml.jackson.annotation.JsonProperty("URI") URI: String
	, @com.fasterxml.jackson.annotation.JsonProperty("id") id: Int
	, @com.fasterxml.jackson.annotation.JsonProperty("i") i: Int
	) =
	  this(_URI = URI, __locator = Some(__locator__), _id = id, _i = i)

}

object Ent3{

	def apply(
		id: Int = 0
	, i: Int = 0
	) = {
		new Ent3(
			_URI = java.util.UUID.randomUUID.toString
		, _id = id
		, _i = i
		, __locator = None)
	}

	
		
	private[test] def buildInternal(
		reader : net.revenj.database.postgres.PostgresReader,
		context: Int,
		converter: example.test.postgres.Ent3Converter,
		converters: Array[(Ent3, net.revenj.database.postgres.PostgresReader, Int) => Ent3]) = {
		var instance = new Ent3(
			__locator = reader.locator, 
			_URI = null, 
			_id = 0, 
			_i = 0)
		var i = 0
		while (i < converters.length) {
			instance = converters(i)(instance, reader, context)
			i += 1
		} 
		instance.URI = converter.buildURI(reader, instance)
		instance
	}

	private[test] def configureConverters(
		converters: Array[(Ent3, net.revenj.database.postgres.PostgresReader, Int) => Ent3], 
			idPos: Int, 
			iPos: Int): Unit = {
		
			converters(idPos) = (item, reader, context) => { item._id = net.revenj.database.postgres.converters.IntConverter.parse(reader, context); item }
			converters(iPos) = (item, reader, context) => { item._i = net.revenj.database.postgres.converters.IntConverter.parse(reader, context); item }
	}

	private[test] def configureExtendedConverters(
		converters: Array[(Ent3, net.revenj.database.postgres.PostgresReader, Int) => Ent3], 
			idPosExtended: Int, 
			iPosExtended: Int): Unit = {
		
			converters(idPosExtended) = (item, reader, context) => { item._id = net.revenj.database.postgres.converters.IntConverter.parse(reader, context); item }
			converters(iPosExtended) = (item, reader, context) => { item._i = net.revenj.database.postgres.converters.IntConverter.parse(reader, context); item }
	}

}
