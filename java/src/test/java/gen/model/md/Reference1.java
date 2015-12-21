/*
* Created by DSL Platform
* v1.0.0.29923 
*/

package gen.model.md;



public class Reference1   implements java.lang.Cloneable, java.io.Serializable, com.dslplatform.json.JsonObject {
	
	
	
	public Reference1() {
			
		this.l = 0L;
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
		if (obj == null || obj instanceof Reference1 == false)
			return false;
		final Reference1 other = (Reference1) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final Reference1 other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.l == other.l))
			return false;
		if(!(this.Detailid.equals(other.Detailid)))
			return false;
		return true;
	}

	private Reference1(Reference1 other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.l = other.l;
		this.Detailid = other.Detailid;
	}

	@Override
	public Object clone() {
		return new Reference1(this);
	}

	@Override
	public String toString() {
		return "Reference1(" + URI + ')';
	}
	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	
	@com.fasterxml.jackson.annotation.JsonCreator private Reference1(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("l") final long l,
			@com.fasterxml.jackson.annotation.JsonProperty("Detailid") final java.util.UUID Detailid) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.l = l;
		this.Detailid = Detailid == null ? org.revenj.Utils.MIN_UUID : Detailid;
	}

	private static final long serialVersionUID = 6375644459176421648L;
	
	private long l;

	
	@com.fasterxml.jackson.annotation.JsonProperty("l")
	public long getL()  {
		
		return l;
	}

	
	public Reference1 setL(final long value) {
		
		this.l = value;
		
		return this;
	}

	
	private java.util.UUID Detailid;

	
	@com.fasterxml.jackson.annotation.JsonProperty("Detailid")
	public java.util.UUID getDetailid()  {
		
		return Detailid;
	}

	
	private Reference1 setDetailid(final java.util.UUID value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"Detailid\" cannot be null!");
		this.Detailid = value;
		
		return this;
	}

	
	static {
		gen.model.md.Detail.__bindToreference1(parent -> {
			gen.model.md.Reference1 e = parent.getReference1();
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

	static void __serializeJsonObjectMinimal(final Reference1 self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (self.l != 0L) {
				sw.writeAscii(",\"l\":", 5);
				com.dslplatform.json.NumberConverter.serialize(self.l, sw);
			}
		
			if (!(self.Detailid.getMostSignificantBits() == 0 && self.Detailid.getLeastSignificantBits() == 0)) {
				sw.writeAscii(",\"Detailid\":", 12);
				com.dslplatform.json.UUIDConverter.serialize(self.Detailid, sw);
			}
	}

	static void __serializeJsonObjectFull(final Reference1 self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			sw.writeAscii(",\"l\":", 5);
			com.dslplatform.json.NumberConverter.serialize(self.l, sw);
		
			
			sw.writeAscii(",\"Detailid\":", 12);
			com.dslplatform.json.UUIDConverter.serialize(self.Detailid, sw);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<Reference1> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<Reference1>() {
		@Override
		public Reference1 deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.md.Reference1(reader);
		}
	};

	private Reference1(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		long _l_ = 0L;
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
					case -385076981:
						_l_ = com.dslplatform.json.NumberConverter.deserializeLong(reader);
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
					case -385076981:
						_l_ = com.dslplatform.json.NumberConverter.deserializeLong(reader);
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
		this.l = _l_;
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
				return new gen.model.md.Reference1(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public Reference1(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Reference1>[] readers) throws java.io.IOException {
		this.__locator = reader.getLocator();
		for (org.revenj.postgres.ObjectConverter.Reader<Reference1> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.md.converters.Reference1Converter.buildURI(reader, this);
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Reference1>[] readers, int __index___l, int __index___Detailid) {
		
		readers[__index___l] = (item, reader, context) -> { item.l = org.revenj.postgres.converters.LongConverter.parse(reader); return item; };
		readers[__index___Detailid] = (item, reader, context) -> { item.Detailid = org.revenj.postgres.converters.UuidConverter.parse(reader, false); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Reference1>[] readers, int __index__extended_l, int __index__extended_Detailid) {
		
		readers[__index__extended_l] = (item, reader, context) -> { item.l = org.revenj.postgres.converters.LongConverter.parse(reader); return item; };
		readers[__index__extended_Detailid] = (item, reader, context) -> { item.Detailid = org.revenj.postgres.converters.UuidConverter.parse(reader, false); return item; };
	}
	
	
	public Reference1(
			final long l) {
			
		setL(l);
		this.URI = this.Detailid.toString();
	}

}
