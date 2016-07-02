package example.test




class Ent2 @com.fasterxml.jackson.annotation.JsonIgnore()  private(
	  private var _URI: String,
	  private var _f: Float,
	  private var _AbcID: Int,
	  private var _Index: Int,
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
		case c: Ent2 => c.URI == URI
		case _ => false
	}

	override def toString = "Ent2("+ URI +")"
	
		
	 def copy(f: Float = this._f, AbcID: Int = this._AbcID, Index: Int = this._Index): Ent2 = {
		

			
		new Ent2(_URI = this.URI, _f = f, _AbcID = AbcID, _Index = Index, __locator = this.__locator)
	}

	
	@com.fasterxml.jackson.annotation.JsonCreator private def this(
		@com.fasterxml.jackson.annotation.JacksonInject("__locator") __locator__ : net.revenj.patterns.ServiceLocator
	, @com.fasterxml.jackson.annotation.JsonProperty("URI") URI: String
	, @com.fasterxml.jackson.annotation.JsonProperty("f") f: Float
	, @com.fasterxml.jackson.annotation.JsonProperty("AbcID") AbcID: Int
	, @com.fasterxml.jackson.annotation.JsonProperty("Index") Index: Int
	) =
	  this(_URI = URI, __locator = Some(__locator__), _f = f, _AbcID = AbcID, _Index = Index)

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("f")
	def f = { 
		_f
	}

	
	def f_= (value: Float) { 
		_f = value
		
	}

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("AbcID")
	def AbcID = { 
		_AbcID
	}

	
	def AbcID_= (value: Int) { 
		_AbcID = value
		
	}

	
	
	@com.fasterxml.jackson.annotation.JsonProperty("Index")
	def Index = { 
		_Index
	}

	
	def Index_= (value: Int) { 
		_Index = value
		
	}

}

object Ent2{

	def apply(
		f: Float = 0.0f
	, AbcID: Int
	, Index: Int = 0
	) = {
		new Ent2(
			_URI = java.util.UUID.randomUUID.toString
		, _f = f
		, _AbcID = AbcID
		, _Index = Index
		, __locator = None)
	}

	
		
	private [test] def bindToent2(parent: example.test.Abc, writer: net.revenj.database.postgres.PostgresWriter, locator: net.revenj.patterns.ServiceLocator): Unit = {
		val converter = locator.resolve[example.test.postgres.Ent2Converter]
		var i = 0
		val iter = parent.ent2.iterator
		while (iter.hasNext) { 
			val e = iter.next()
			e.AbcID = parent.ID
			e.Index = i
			i += 1
			e.URI = converter.buildURI(writer, e)
		}
	}
		
	private[test] def buildInternal(
		reader : net.revenj.database.postgres.PostgresReader,
		context: Int,
		converter: example.test.postgres.Ent2Converter,
		converters: Array[(Ent2, net.revenj.database.postgres.PostgresReader, Int) => Ent2]) = {
		var instance = new Ent2(
			__locator = reader.locator, 
			_URI = null, 
			_f = 0.0f, 
			_AbcID = 0, 
			_Index = 0)
		var i = 0
		while (i < converters.length) {
			instance = converters(i)(instance, reader, context)
			i += 1
		} 
		instance.URI = converter.buildURI(reader, instance)
		instance
	}

	private[test] def configureConverters(
		converters: Array[(Ent2, net.revenj.database.postgres.PostgresReader, Int) => Ent2], 
			fPos: Int, 
			AbcIDPos: Int, 
			IndexPos: Int): Unit = {
		
			converters(fPos) = (item, reader, context) => { item._f = net.revenj.database.postgres.converters.FloatConverter.parse(reader, context); item }
			converters(AbcIDPos) = (item, reader, context) => { item._AbcID = net.revenj.database.postgres.converters.IntConverter.parse(reader, context); item }
			converters(IndexPos) = (item, reader, context) => { item._Index = net.revenj.database.postgres.converters.IntConverter.parse(reader, context); item }
	}

	private[test] def configureExtendedConverters(
		converters: Array[(Ent2, net.revenj.database.postgres.PostgresReader, Int) => Ent2], 
			fPosExtended: Int, 
			AbcIDPosExtended: Int, 
			IndexPosExtended: Int): Unit = {
		
			converters(fPosExtended) = (item, reader, context) => { item._f = net.revenj.database.postgres.converters.FloatConverter.parse(reader, context); item }
			converters(AbcIDPosExtended) = (item, reader, context) => { item._AbcID = net.revenj.database.postgres.converters.IntConverter.parse(reader, context); item }
			converters(IndexPosExtended) = (item, reader, context) => { item._Index = net.revenj.database.postgres.converters.IntConverter.parse(reader, context); item }
	}

}
