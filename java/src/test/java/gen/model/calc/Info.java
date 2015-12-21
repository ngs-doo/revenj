/*
* Created by DSL Platform
* v1.0.0.29923 
*/

package gen.model.calc;



public class Info   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot, com.dslplatform.json.JsonObject {
	
	
	
	public Info() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.code = "";
		this.name = "";
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
		if (obj == null || obj instanceof Info == false)
			return false;
		final Info other = (Info) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final Info other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.code.equals(other.code)))
			return false;
		if(!(this.name.equals(other.name)))
			return false;
		return true;
	}

	private Info(Info other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.code = other.code;
		this.name = other.name;
		this.__originalValue = other.__originalValue;
	}

	@Override
	public Object clone() {
		return new Info(this);
	}

	@Override
	public String toString() {
		return "Info(" + URI + ')';
	}
	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	
	@com.fasterxml.jackson.annotation.JsonCreator private Info(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("code") final String code,
			@com.fasterxml.jackson.annotation.JsonProperty("name") final String name) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.code = code == null ? "" : code;
		this.name = name == null ? "" : name;
	}

	private static final long serialVersionUID = -7120682101009463474L;
	
	private String code;

	
	@com.fasterxml.jackson.annotation.JsonProperty("code")
	public String getCode()  {
		
		return code;
	}

	
	public Info setCode(final String value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"code\" cannot be null!");
		this.code = value;
		
		return this;
	}

	
	private String name;

	
	@com.fasterxml.jackson.annotation.JsonProperty("name")
	public String getName()  {
		
		return name;
	}

	
	public Info setName(final String value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"name\" cannot be null!");
		this.name = value;
		
		return this;
	}

	
	static {
		gen.model.calc.repositories.InfoRepository.__setupPersist(
			(aggregates, arg) -> {
				try {
					for (gen.model.calc.Info agg : aggregates) {
						 
						agg.URI = gen.model.calc.converters.InfoConverter.buildURI(arg, agg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(oldAggregates, newAggregates) -> {
				for (int i = 0; i < newAggregates.size(); i++) {
					gen.model.calc.Info oldAgg = oldAggregates.get(i);
					gen.model.calc.Info newAgg = newAggregates.get(i);
					 
				}
			},
			aggregates -> { 
				for (gen.model.calc.Info agg : aggregates) { 
				}
			},
			agg -> { 
				
		Info _res = agg.__originalValue;
		agg.__originalValue = (Info)agg.clone();
		if (_res != null) {
			return _res;
		}
				return null;
			}
		);
	}
	private transient Info __originalValue;
	
	public void serialize(final com.dslplatform.json.JsonWriter sw, final boolean minimal) {
		sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
		if (minimal) {
			__serializeJsonObjectMinimal(this, sw, false);
		} else {
			__serializeJsonObjectFull(this, sw, false);
		}
		sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
	}

	static void __serializeJsonObjectMinimal(final Info self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (!(self.code.length() == 0)) {
				sw.writeAscii(",\"code\":", 8);
				sw.writeString(self.code);
			}
		
			if (!(self.name.length() == 0)) {
				sw.writeAscii(",\"name\":", 8);
				sw.writeString(self.name);
			}
	}

	static void __serializeJsonObjectFull(final Info self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			sw.writeAscii(",\"code\":", 8);
			sw.writeString(self.code);
		
			
			sw.writeAscii(",\"name\":", 8);
			sw.writeString(self.name);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<Info> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<Info>() {
		@Override
		public Info deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.calc.Info(reader);
		}
	};

	private Info(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		String _code_ = "";
		String _name_ = "";
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
					case -114201356:
						_code_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -1925595674:
						_name_ = com.dslplatform.json.StringConverter.deserialize(reader);
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
					case -114201356:
						_code_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -1925595674:
						_name_ = com.dslplatform.json.StringConverter.deserialize(reader);
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
		this.code = _code_;
		this.name = _name_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.calc.Info(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public Info(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Info>[] readers) throws java.io.IOException {
		this.__locator = reader.getLocator();
		for (org.revenj.postgres.ObjectConverter.Reader<Info> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.calc.converters.InfoConverter.buildURI(reader, this);
		this.__originalValue = (Info)this.clone();
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Info>[] readers, int __index___code, int __index___name) {
		
		readers[__index___code] = (item, reader, context) -> { item.code = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
		readers[__index___name] = (item, reader, context) -> { item.name = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Info>[] readers, int __index__extended_code, int __index__extended_name) {
		
		readers[__index__extended_code] = (item, reader, context) -> { item.code = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
		readers[__index__extended_name] = (item, reader, context) -> { item.name = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
	}
	
	
	public Info(
			final String code,
			final String name) {
			
		setCode(code);
		setName(name);
		this.URI = this.code;
	}

}
