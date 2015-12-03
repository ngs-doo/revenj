/*
* Created by DSL Platform
* v1.0.0.27897 
*/

package gen.model.md;



public class Reference2   implements java.lang.Cloneable, java.io.Serializable, com.dslplatform.json.JsonObject {
	
	
	
	public Reference2() {
			
		this.x = java.math.BigDecimal.ZERO;
		this.Detailid = java.util.UUID.randomUUID();
		this.URI = this.Detailid.toString();
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
		if (obj == null || obj instanceof Reference2 == false)
			return false;
		final Reference2 other = (Reference2) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final Reference2 other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.x == other.x || this.x != null && other.x != null && this.x.compareTo(other.x) == 0))
			return false;
		if(!(this.Detailid.equals(other.Detailid)))
			return false;
		return true;
	}

	private Reference2(Reference2 other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.x = other.x;
		this.Detailid = other.Detailid;
	}

	@Override
	public Object clone() {
		return new Reference2(this);
	}

	@Override
	public String toString() {
		return "Reference2(" + URI + ')';
	}
	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	
	@com.fasterxml.jackson.annotation.JsonCreator private Reference2(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("x") final java.math.BigDecimal x,
			@com.fasterxml.jackson.annotation.JsonProperty("Detailid") final java.util.UUID Detailid) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.x = x == null ? java.math.BigDecimal.ZERO : x;
		this.Detailid = Detailid == null ? org.revenj.Utils.MIN_UUID : Detailid;
	}

	private static final long serialVersionUID = -7642544836401195251L;
	
	private java.math.BigDecimal x;

	
	@com.fasterxml.jackson.annotation.JsonProperty("x")
	public java.math.BigDecimal getX()  {
		
		return x;
	}

	
	public Reference2 setX(final java.math.BigDecimal value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"x\" cannot be null!");
		this.x = value;
		
		return this;
	}

	
	private java.util.UUID Detailid;

	
	@com.fasterxml.jackson.annotation.JsonProperty("Detailid")
	public java.util.UUID getDetailid()  {
		
		return Detailid;
	}

	
	private Reference2 setDetailid(final java.util.UUID value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"Detailid\" cannot be null!");
		this.Detailid = value;
		
		return this;
	}

	
	static {
		gen.model.md.Detail.__bindToreference2(parent -> {
			gen.model.md.Reference2 e = parent.getReference2();
			if (e != null) {
				e.Detailid = parent.getId();
			}
		});
	}
	
	public void serialize(final com.dslplatform.json.JsonWriter sw, final boolean minimal) {
		sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
		if (minimal) {
			__serializeJsonObjectMinimal(this, sw, false);
		} else {
			__serializeJsonObjectFull(this, sw, false);
		}
		sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
	}

	static void __serializeJsonObjectMinimal(final Reference2 self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (!(java.math.BigDecimal.ZERO.compareTo(self.x) == 0)) {
				sw.writeAscii(",\"x\":", 5);
				com.dslplatform.json.NumberConverter.serialize(self.x, sw);
			}
		
			if (!(self.Detailid.getMostSignificantBits() == 0 && self.Detailid.getLeastSignificantBits() == 0)) {
				sw.writeAscii(",\"Detailid\":", 12);
				com.dslplatform.json.UUIDConverter.serialize(self.Detailid, sw);
			}
	}

	static void __serializeJsonObjectFull(final Reference2 self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			sw.writeAscii(",\"x\":", 5);
			com.dslplatform.json.NumberConverter.serialize(self.x, sw);
		
			
			sw.writeAscii(",\"Detailid\":", 12);
			com.dslplatform.json.UUIDConverter.serialize(self.Detailid, sw);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<Reference2> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<Reference2>() {
		@Override
		public Reference2 deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.md.Reference2(reader);
		}
	};

	private Reference2(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		java.math.BigDecimal _x_ = java.math.BigDecimal.ZERO;
		java.util.UUID _Detailid_ = org.revenj.Utils.MIN_UUID;
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
					case -49524601:
						_x_ = com.dslplatform.json.NumberConverter.deserializeDecimal(reader);
					nextToken = reader.getNextToken();
						break;
					case 1627667001:
						_Detailid_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
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
					case -49524601:
						_x_ = com.dslplatform.json.NumberConverter.deserializeDecimal(reader);
					nextToken = reader.getNextToken();
						break;
					case 1627667001:
						_Detailid_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
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
		this.x = _x_;
		this.Detailid = _Detailid_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.md.Reference2(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public Reference2(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Reference2>[] readers) throws java.io.IOException {
		this.__locator = java.util.Optional.ofNullable(reader.locator);
		for (org.revenj.postgres.ObjectConverter.Reader<Reference2> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.md.converters.Reference2Converter.buildURI(reader, this);
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Reference2>[] readers, int __index___x, int __index___Detailid) {
		
		readers[__index___x] = (item, reader, context) -> { item.x = org.revenj.postgres.converters.DecimalConverter.parse(reader, false); return item; };
		readers[__index___Detailid] = (item, reader, context) -> { item.Detailid = org.revenj.postgres.converters.UuidConverter.parse(reader, false); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Reference2>[] readers, int __index__extended_x, int __index__extended_Detailid) {
		
		readers[__index__extended_x] = (item, reader, context) -> { item.x = org.revenj.postgres.converters.DecimalConverter.parse(reader, false); return item; };
		readers[__index__extended_Detailid] = (item, reader, context) -> { item.Detailid = org.revenj.postgres.converters.UuidConverter.parse(reader, false); return item; };
	}
	
	
	public Reference2(
			final java.math.BigDecimal x) {
			
		setX(x);
		this.URI = this.Detailid.toString();
	}

}
