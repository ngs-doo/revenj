/*
* Created by DSL Platform
* v1.0.0.29923 
*/

package gen.model.mixinReference;



public class Resident   implements java.lang.Cloneable, java.io.Serializable, com.dslplatform.json.JsonObject {
	
	
	
	public Resident() {
			
		this.id = java.util.UUID.randomUUID();
		this.birth = java.time.LocalDate.now();
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
		if (obj == null || obj instanceof Resident == false)
			return false;
		final Resident other = (Resident) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final Resident other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.id.equals(other.id)))
			return false;
		if(!(this.birth.equals(other.birth)))
			return false;
		return true;
	}

	private Resident(Resident other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.id = other.id;
		this.birth = other.birth;
	}

	@Override
	public Object clone() {
		return new Resident(this);
	}

	@Override
	public String toString() {
		return "Resident(" + URI + ')';
	}
	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	
	@com.fasterxml.jackson.annotation.JsonCreator private Resident(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("id") final java.util.UUID id,
			@com.fasterxml.jackson.annotation.JsonProperty("birth") final java.time.LocalDate birth) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.id = id == null ? org.revenj.Utils.MIN_UUID : id;
		this.birth = birth == null ? org.revenj.Utils.MIN_LOCAL_DATE : birth;
	}

	private static final long serialVersionUID = 7975619427772604547L;
	
	private java.util.UUID id;

	
	@com.fasterxml.jackson.annotation.JsonProperty("id")
	public java.util.UUID getId()  {
		
		return id;
	}

	
	public Resident setId(final java.util.UUID value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"id\" cannot be null!");
		this.id = value;
		
		return this;
	}

	
	private java.time.LocalDate birth;

	
	@com.fasterxml.jackson.annotation.JsonProperty("birth")
	public java.time.LocalDate getBirth()  {
		
		return birth;
	}

	
	public Resident setBirth(final java.time.LocalDate value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"birth\" cannot be null!");
		this.birth = value;
		
		return this;
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

	static void __serializeJsonObjectMinimal(final Resident self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (!(self.id.getMostSignificantBits() == 0 && self.id.getLeastSignificantBits() == 0)) {
				sw.writeAscii(",\"id\":", 6);
				com.dslplatform.json.UUIDConverter.serialize(self.id, sw);
			}
		
			if (!(self.birth.getYear() == 1 && self.birth.getMonthValue() == 1 && self.birth.getDayOfMonth() == 1)) {
				sw.writeAscii(",\"birth\":", 9);
				com.dslplatform.json.JavaTimeConverter.serialize(self.birth, sw);
			}
	}

	static void __serializeJsonObjectFull(final Resident self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			sw.writeAscii(",\"id\":", 6);
			com.dslplatform.json.UUIDConverter.serialize(self.id, sw);
		
			
			sw.writeAscii(",\"birth\":", 9);
			com.dslplatform.json.JavaTimeConverter.serialize(self.birth, sw);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<Resident> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<Resident>() {
		@Override
		public Resident deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.mixinReference.Resident(reader);
		}
	};

	private Resident(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		java.util.UUID _id_ = org.revenj.Utils.MIN_UUID;
		java.time.LocalDate _birth_ = org.revenj.Utils.MIN_LOCAL_DATE;
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
					case 558509118:
						_birth_ = com.dslplatform.json.JavaTimeConverter.deserializeLocalDate(reader);
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
					case 558509118:
						_birth_ = com.dslplatform.json.JavaTimeConverter.deserializeLocalDate(reader);
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
		this.birth = _birth_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.mixinReference.Resident(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public Resident(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Resident>[] readers) throws java.io.IOException {
		this.__locator = reader.getLocator();
		for (org.revenj.postgres.ObjectConverter.Reader<Resident> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.mixinReference.converters.ResidentConverter.buildURI(reader, this);
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Resident>[] readers, int __index___id, int __index___birth) {
		
		readers[__index___id] = (item, reader, context) -> { item.id = org.revenj.postgres.converters.UuidConverter.parse(reader, false); return item; };
		readers[__index___birth] = (item, reader, context) -> { item.birth = org.revenj.postgres.converters.DateConverter.parse(reader, false); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Resident>[] readers, int __index__extended_id, int __index__extended_birth) {
		
		readers[__index__extended_id] = (item, reader, context) -> { item.id = org.revenj.postgres.converters.UuidConverter.parse(reader, false); return item; };
		readers[__index__extended_birth] = (item, reader, context) -> { item.birth = org.revenj.postgres.converters.DateConverter.parse(reader, false); return item; };
	}
	
	
	public Resident(
			final java.util.UUID id,
			final java.time.LocalDate birth) {
			
		setId(id);
		setBirth(birth);
		this.URI = this.id.toString();
	}

}
