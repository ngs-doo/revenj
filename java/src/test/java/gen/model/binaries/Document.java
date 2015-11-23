package gen.model.binaries;



public class Document   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot, com.dslplatform.json.JsonObject {
	
	
	
	public Document() {
			
		this.ID = java.util.UUID.randomUUID();
		this.name = "";
		this.content = org.revenj.Utils.EMPTY_BINARY;
		this.URI = this.ID.toString();
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
		if (obj == null || obj instanceof Document == false)
			return false;
		final Document other = (Document) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final Document other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.ID.equals(other.ID)))
			return false;
		if(!(this.name.equals(other.name)))
			return false;
		if(!(java.util.Arrays.equals(this.content, other.content)))
			return false;
		return true;
	}

	private Document(Document other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.ID = other.ID;
		this.name = other.name;
		this.content = other.content != null ? java.util.Arrays.copyOf(other.content, other.content.length) : null;
		this.__originalValue = other.__originalValue;
	}

	@Override
	public Object clone() {
		return new Document(this);
	}

	@Override
	public String toString() {
		return "Document(" + URI + ')';
	}
	
	
	public Document(
			final String name,
			final byte[] content) {
			
		setName(name);
		setContent(content);
		this.URI = this.ID.toString();
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	private static final long serialVersionUID = -203251078371730699L;
	
	@com.fasterxml.jackson.annotation.JsonCreator private Document(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("ID") final java.util.UUID ID,
			@com.fasterxml.jackson.annotation.JsonProperty("name") final String name,
			@com.fasterxml.jackson.annotation.JsonProperty("content") final byte[] content) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.ID = ID == null ? org.revenj.Utils.MIN_UUID : ID;
		this.name = name == null ? "" : name;
		this.content = content == null ? org.revenj.Utils.EMPTY_BINARY : content;
	}

	
	private java.util.UUID ID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ID")
	public java.util.UUID getID()  {
		
		return ID;
	}

	
	private Document setID(final java.util.UUID value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"ID\" cannot be null!");
		this.ID = value;
		
		return this;
	}

	
	private String name;

	
	@com.fasterxml.jackson.annotation.JsonProperty("name")
	public String getName()  {
		
		return name;
	}

	
	public Document setName(final String value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"name\" cannot be null!");
		org.revenj.Guards.checkLength(value, 20);
		this.name = value;
		
		return this;
	}

	
	private byte[] content;

	
	@com.fasterxml.jackson.annotation.JsonProperty("content")
	public byte[] getContent()  {
		
		return content;
	}

	
	public Document setContent(final byte[] value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"content\" cannot be null!");
		this.content = value;
		
		return this;
	}

	private transient Document __originalValue;
	
	static {
		gen.model.binaries.repositories.DocumentRepository.__setupPersist(
			(aggregates, arg) -> {
				try {
					for (gen.model.binaries.Document agg : aggregates) {
						 
						agg.URI = gen.model.binaries.converters.DocumentConverter.buildURI(arg, agg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(oldAggregates, newAggregates) -> {
				for (int i = 0; i < newAggregates.size(); i++) {
					gen.model.binaries.Document oldAgg = oldAggregates.get(i);
					gen.model.binaries.Document newAgg = newAggregates.get(i);
					 
				}
			},
			aggregates -> { 
				for (gen.model.binaries.Document agg : aggregates) { 
				}
			},
			agg -> { 
				
		Document _res = agg.__originalValue;
		agg.__originalValue = (Document)agg.clone();
		if (_res != null) {
			return _res;
		}
				return null;
			}
		);
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

	static void __serializeJsonObjectMinimal(final Document self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (!(self.ID.getMostSignificantBits() == 0 && self.ID.getLeastSignificantBits() == 0)) {
				sw.writeAscii(",\"ID\":", 6);
				com.dslplatform.json.UUIDConverter.serialize(self.ID, sw);
			}
		
			if (!(self.name.length() == 0)) {
				sw.writeAscii(",\"name\":", 8);
				com.dslplatform.json.StringConverter.serializeShort(self.name, sw);
			}
		
			if (!(self.content.length == 0)) {
				sw.writeAscii(",\"content\":", 11);
				com.dslplatform.json.BinaryConverter.serialize(self.content, sw);
			}
	}

	static void __serializeJsonObjectFull(final Document self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			sw.writeAscii(",\"ID\":", 6);
			com.dslplatform.json.UUIDConverter.serialize(self.ID, sw);
		
			
			sw.writeAscii(",\"name\":", 8);
			com.dslplatform.json.StringConverter.serializeShort(self.name, sw);
		
			
			sw.writeAscii(",\"content\":", 11);
			com.dslplatform.json.BinaryConverter.serialize(self.content, sw);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<Document> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<Document>() {
		@Override
		public Document deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.binaries.Document(reader);
		}
	};

	private Document(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		java.util.UUID _ID_ = org.revenj.Utils.MIN_UUID;
		String _name_ = "";
		byte[] _content_ = org.revenj.Utils.EMPTY_BINARY;
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
					case 1458105184:
						_ID_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -1925595674:
						_name_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -1866546238:
						_content_ = com.dslplatform.json.BinaryConverter.deserialize(reader);
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
					case 1458105184:
						_ID_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -1925595674:
						_name_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -1866546238:
						_content_ = com.dslplatform.json.BinaryConverter.deserialize(reader);
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
		this.ID = _ID_;
		this.name = _name_;
		this.content = _content_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.binaries.Document(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public Document(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Document>[] readers) throws java.io.IOException {
		this.__locator = java.util.Optional.ofNullable(reader.locator);
		for (org.revenj.postgres.ObjectConverter.Reader<Document> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.binaries.converters.DocumentConverter.buildURI(reader, this);
		this.__originalValue = (Document)this.clone();
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Document>[] readers, int __index___ID, int __index___name, int __index___content) {
		
		readers[__index___ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.UuidConverter.parse(reader, false); return item; };
		readers[__index___name] = (item, reader, context) -> { item.name = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
		readers[__index___content] = (item, reader, context) -> { item.content = org.revenj.postgres.converters.ByteaConverter.parse(reader, context); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Document>[] readers, int __index__extended_ID, int __index__extended_name, int __index__extended_content) {
		
		readers[__index__extended_ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.UuidConverter.parse(reader, false); return item; };
		readers[__index__extended_name] = (item, reader, context) -> { item.name = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
		readers[__index__extended_content] = (item, reader, context) -> { item.content = org.revenj.postgres.converters.ByteaConverter.parse(reader, context); return item; };
	}
}
