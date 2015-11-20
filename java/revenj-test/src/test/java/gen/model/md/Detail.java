package gen.model.md;



public class Detail   implements java.lang.Cloneable, java.io.Serializable, com.dslplatform.json.JsonObject {
	
	
	
	public Detail() {
			
		this.id = java.util.UUID.randomUUID();
		this.masterId = 0;
		this.URI = this.id.toString();
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
		if (obj == null || obj instanceof Detail == false)
			return false;
		final Detail other = (Detail) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final Detail other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.id.equals(other.id)))
			return false;
		if(!(this.masterId == other.masterId))
			return false;
		return true;
	}

	private Detail(Detail other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.id = other.id;
		this.masterId = other.masterId;
	}

	@Override
	public Object clone() {
		return new Detail(this);
	}

	@Override
	public String toString() {
		return "Detail(" + URI + ')';
	}
	
	
	public Detail(
			final java.util.UUID id,
			final int masterId) {
			
		setId(id);
		setMasterId(masterId);
		this.URI = this.id.toString();
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	private static final long serialVersionUID = -989559492655186851L;
	
	@com.fasterxml.jackson.annotation.JsonCreator private Detail(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("id") final java.util.UUID id,
			@com.fasterxml.jackson.annotation.JsonProperty("masterId") final int masterId) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.id = id == null ? org.revenj.Utils.MIN_UUID : id;
		this.masterId = masterId;
	}

	
	private java.util.UUID id;

	
	@com.fasterxml.jackson.annotation.JsonProperty("id")
	public java.util.UUID getId()  {
		
		return id;
	}

	
	public Detail setId(final java.util.UUID value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"id\" cannot be null!");
		this.id = value;
		
		return this;
	}

	
	private int masterId;

	
	@com.fasterxml.jackson.annotation.JsonProperty("masterId")
	public int getMasterId()  {
		
		return masterId;
	}

	
	public Detail setMasterId(final int value) {
		
		this.masterId = value;
		
		return this;
	}

	
	static {
		gen.model.md.Master.__bindTodetails(parent -> {
			for (gen.model.md.Detail e : parent.getDetails()) { 
				e.masterId = parent.getID();
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

	static void __serializeJsonObjectMinimal(final Detail self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (!(self.id.getMostSignificantBits() == 0 && self.id.getLeastSignificantBits() == 0)) {
				sw.writeAscii(",\"id\":", 6);
				com.dslplatform.json.UUIDConverter.serialize(self.id, sw);
			}
		
			if (self.masterId != 0) {
				sw.writeAscii(",\"masterId\":", 12);
				com.dslplatform.json.NumberConverter.serialize(self.masterId, sw);
			}
	}

	static void __serializeJsonObjectFull(final Detail self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			sw.writeAscii(",\"id\":", 6);
			com.dslplatform.json.UUIDConverter.serialize(self.id, sw);
		
			
			sw.writeAscii(",\"masterId\":", 12);
			com.dslplatform.json.NumberConverter.serialize(self.masterId, sw);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<Detail> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<Detail>() {
		@Override
		public Detail deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.md.Detail(reader);
		}
	};

	private Detail(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		java.util.UUID _id_ = org.revenj.Utils.MIN_UUID;
		int _masterId_ = 0;
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
					case 926444256:
						_id_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -1739047458:
						_masterId_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
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
					case 926444256:
						_id_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -1739047458:
						_masterId_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
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
		this.id = _id_;
		this.masterId = _masterId_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.md.Detail(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public Detail(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Detail>[] readers) throws java.io.IOException {
		this.__locator = java.util.Optional.ofNullable(reader.locator);
		for (org.revenj.postgres.ObjectConverter.Reader<Detail> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.md.converters.DetailConverter.buildURI(reader, this);
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Detail>[] readers, int __index___id, int __index___masterId) {
		
		readers[__index___id] = (item, reader, context) -> { item.id = org.revenj.postgres.converters.UuidConverter.parse(reader, false); return item; };
		readers[__index___masterId] = (item, reader, context) -> { item.masterId = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Detail>[] readers, int __index__extended_id, int __index__extended_masterId) {
		
		readers[__index__extended_id] = (item, reader, context) -> { item.id = org.revenj.postgres.converters.UuidConverter.parse(reader, false); return item; };
		readers[__index__extended_masterId] = (item, reader, context) -> { item.masterId = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
	}
}
