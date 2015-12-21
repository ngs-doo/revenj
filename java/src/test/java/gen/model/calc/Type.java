/*
* Created by DSL Platform
* v1.0.0.29923 
*/

package gen.model.calc;



public class Type   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot, com.dslplatform.json.JsonObject {
	
	
	
	public Type() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.suffix = "";
		this.description = "";
	}

	
	private String URI;

	
	@com.fasterxml.jackson.annotation.JsonProperty("URI")
	public String getURI()  {
		
		return this.URI;
	}

	
	@Override
	public int hashCode() {
		return URI.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null || obj instanceof Type == false)
			return false;
		final Type other = (Type) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final Type other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.suffix.equals(other.suffix)))
			return false;
		if(!(this.description.equals(other.description)))
			return false;
		if(!(this.xml == other.xml || this.xml != null && other.xml != null && this.xml.isEqualNode(other.xml)))
			return false;
		return true;
	}

	private Type(Type other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.suffix = other.suffix;
		this.description = other.description;
		this.xml = other.xml != null ? (org.w3c.dom.Element)other.xml.cloneNode(true) : null;
		this.__originalValue = other.__originalValue;
	}

	@Override
	public Object clone() {
		return new Type(this);
	}

	@Override
	public String toString() {
		return "Type(" + URI + ')';
	}
	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	
	@com.fasterxml.jackson.annotation.JsonCreator private Type(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("suffix") final String suffix,
			@com.fasterxml.jackson.annotation.JsonProperty("description") final String description,
			@com.fasterxml.jackson.annotation.JsonProperty("xml") final org.w3c.dom.Element xml) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.suffix = suffix == null ? "" : suffix;
		this.description = description == null ? "" : description;
		this.xml = xml;
	}

	private static final long serialVersionUID = -5388562044707411683L;
	
	private String suffix;

	
	@com.fasterxml.jackson.annotation.JsonProperty("suffix")
	public String getSuffix()  {
		
		return suffix;
	}

	
	public Type setSuffix(final String value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"suffix\" cannot be null!");
		this.suffix = value;
		
		return this;
	}

	
	private String description;

	
	@com.fasterxml.jackson.annotation.JsonProperty("description")
	public String getDescription()  {
		
		return description;
	}

	
	public Type setDescription(final String value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"description\" cannot be null!");
		this.description = value;
		
		return this;
	}

	
	private org.w3c.dom.Element xml;

	
	@com.fasterxml.jackson.annotation.JsonProperty("xml")
	public org.w3c.dom.Element getXml()  {
		
		return xml;
	}

	
	public Type setXml(final org.w3c.dom.Element value) {
		
		this.xml = value;
		
		return this;
	}

	
	static {
		gen.model.calc.repositories.TypeRepository.__setupPersist(
			(aggregates, arg) -> {
				try {
					for (gen.model.calc.Type agg : aggregates) {
						 
						agg.URI = gen.model.calc.converters.TypeConverter.buildURI(arg, agg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(oldAggregates, newAggregates) -> {
				for (int i = 0; i < newAggregates.size(); i++) {
					gen.model.calc.Type oldAgg = oldAggregates.get(i);
					gen.model.calc.Type newAgg = newAggregates.get(i);
					 
				}
			},
			aggregates -> { 
				for (gen.model.calc.Type agg : aggregates) { 
				}
			},
			agg -> { 
				
		Type _res = agg.__originalValue;
		agg.__originalValue = (Type)agg.clone();
		if (_res != null) {
			return _res;
		}
				return null;
			}
		);
	}
	private transient Type __originalValue;
	
	public void serialize(final com.dslplatform.json.JsonWriter sw, final boolean minimal) {
		sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
		if (minimal) {
			__serializeJsonObjectMinimal(this, sw, false);
		} else {
			__serializeJsonObjectFull(this, sw, false);
		}
		sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
	}

	static void __serializeJsonObjectMinimal(final Type self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (!(self.suffix.length() == 0)) {
				sw.writeAscii(",\"suffix\":", 10);
				sw.writeString(self.suffix);
			}
		
			if (!(self.description.length() == 0)) {
				sw.writeAscii(",\"description\":", 15);
				sw.writeString(self.description);
			}
		
			if (self.xml != null) {
				sw.writeAscii(",\"xml\":", 7);
				com.dslplatform.json.XmlConverter.serialize(self.xml, sw);
			}
	}

	static void __serializeJsonObjectFull(final Type self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			sw.writeAscii(",\"suffix\":", 10);
			sw.writeString(self.suffix);
		
			
			sw.writeAscii(",\"description\":", 15);
			sw.writeString(self.description);
		
			
			if (self.xml != null) {
				sw.writeAscii(",\"xml\":", 7);
				com.dslplatform.json.XmlConverter.serialize(self.xml, sw);
			} else {
				sw.writeAscii(",\"xml\":null", 11);
			}
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<Type> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<Type>() {
		@Override
		public Type deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.calc.Type(reader);
		}
	};

	private Type(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		String _suffix_ = "";
		String _description_ = "";
		org.w3c.dom.Element _xml_ = null;
		byte nextToken = reader.last();
		if(nextToken != '}') {
			int nameHash = reader.fillName();
			nextToken = reader.getNextToken();
			if(nextToken == 'n') {
				if (reader.wasNull()) {
					nextToken = reader.getNextToken();
				} else {
					throw new java.io.IOException("Expecting 'u' (as null) at position " + reader.positionInStream() + ". Found " + (char)nextToken);
				}
			} else {
				switch(nameHash) {
					
					case 2053729053:
						_URI_ = reader.readString();
				nextToken = reader.getNextToken();
						break;
					case -393329588:
						_suffix_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case 879704937:
						_description_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -630165834:
						_xml_ = com.dslplatform.json.XmlConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					default:
						nextToken = reader.skip();
						break;
				}
			}
			while (nextToken == ',') {
				nextToken = reader.getNextToken();
				nameHash = reader.fillName();
				nextToken = reader.getNextToken();
				if(nextToken == 'n') {
					if (reader.wasNull()) {
						nextToken = reader.getNextToken();
						continue;
					} else {
						throw new java.io.IOException("Expecting 'u' (as null) at position " + reader.positionInStream() + ". Found " + (char)nextToken);
					}
				}
				switch(nameHash) {
					
					case 2053729053:
						_URI_ = reader.readString();
				nextToken = reader.getNextToken();
						break;
					case -393329588:
						_suffix_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case 879704937:
						_description_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -630165834:
						_xml_ = com.dslplatform.json.XmlConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					default:
						nextToken = reader.skip();
						break;
				}
			}
			if (nextToken != '}') {
				throw new java.io.IOException("Expecting '}' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
			}
		}
		
		this.URI = _URI_;
		this.suffix = _suffix_;
		this.description = _description_;
		this.xml = _xml_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.calc.Type(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public Type(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Type>[] readers) throws java.io.IOException {
		this.__locator = reader.getLocator();
		for (org.revenj.postgres.ObjectConverter.Reader<Type> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.calc.converters.TypeConverter.buildURI(reader, this);
		this.__originalValue = (Type)this.clone();
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Type>[] readers, int __index___suffix, int __index___description, int __index___xml) {
		
		readers[__index___suffix] = (item, reader, context) -> { item.suffix = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
		readers[__index___description] = (item, reader, context) -> { item.description = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
		readers[__index___xml] = (item, reader, context) -> { item.xml = org.revenj.postgres.converters.XmlConverter.parse(reader, context); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Type>[] readers, int __index__extended_suffix, int __index__extended_description, int __index__extended_xml) {
		
		readers[__index__extended_suffix] = (item, reader, context) -> { item.suffix = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
		readers[__index__extended_description] = (item, reader, context) -> { item.description = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
		readers[__index__extended_xml] = (item, reader, context) -> { item.xml = org.revenj.postgres.converters.XmlConverter.parse(reader, context); return item; };
	}
	
	
	public Type(
			final String suffix,
			final String description,
			final org.w3c.dom.Element xml) {
			
		setSuffix(suffix);
		setDescription(description);
		setXml(xml);
		this.URI = this.suffix;
	}

}
