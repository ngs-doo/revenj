/*
* Created by DSL Platform
* v1.0.0.29923 
*/

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
		if(!(java.util.Arrays.equals(this.bools, other.bools)))
			return false;
		return true;
	}

	private Document(Document other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.ID = other.ID;
		this.name = other.name;
		this.content = other.content != null ? java.util.Arrays.copyOf(other.content, other.content.length) : null;
		this.bools = other.bools == null ? null : java.util.Arrays.copyOf(other.bools, other.bools.length);
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
	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	
	@com.fasterxml.jackson.annotation.JsonCreator private Document(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("ID") final java.util.UUID ID,
			@com.fasterxml.jackson.annotation.JsonProperty("name") final String name,
			@com.fasterxml.jackson.annotation.JsonProperty("content") final byte[] content,
			@com.fasterxml.jackson.annotation.JsonProperty("bools") final boolean[] bools,
			@com.fasterxml.jackson.annotation.JsonProperty("boolsCalc") final boolean[] boolsCalc) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.ID = ID == null ? org.revenj.Utils.MIN_UUID : ID;
		this.name = name == null ? "" : name;
		this.content = content == null ? org.revenj.Utils.EMPTY_BINARY : content;
		this.bools = bools;
		this.boolsCalc = boolsCalc;
	}

	private static final long serialVersionUID = -7850006447754211814L;
	
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

	
	private boolean[] bools;

	
	@com.fasterxml.jackson.annotation.JsonProperty("bools")
	public boolean[] getBools()  {
		
		return bools;
	}

	
	public Document setBools(final boolean[] value) {
		
		this.bools = value;
		
		return this;
	}

	
	private boolean[] boolsCalc;

	
	@com.fasterxml.jackson.annotation.JsonProperty("boolsCalc")
	public boolean[] getBoolsCalc()  {
		
		this.boolsCalc = __calculated_boolsCalc.apply(this);
		return this.boolsCalc;
	}

	private static final java.util.function.Function<gen.model.binaries.Document, boolean[]> __calculated_boolsCalc = it -> it.getBools();
	
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
	private transient Document __originalValue;
	
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
		
		if(self.bools != null && self.bools.length != 0) {
			sw.writeAscii(",\"bools\":[", 10);
			com.dslplatform.json.BoolConverter.serialize(self.bools[0], sw);
			for(int i = 1; i < self.bools.length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
				com.dslplatform.json.BoolConverter.serialize(self.bools[i], sw);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else if(self.bools != null) sw.writeAscii(",\"bools\":[]", 11);
		
		if(self.getBoolsCalc() != null && self.getBoolsCalc().length != 0) {
			sw.writeAscii(",\"boolsCalc\":[", 14);
			com.dslplatform.json.BoolConverter.serialize(self.getBoolsCalc()[0], sw);
			for(int i = 1; i < self.getBoolsCalc().length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
				com.dslplatform.json.BoolConverter.serialize(self.getBoolsCalc()[i], sw);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else if(self.getBoolsCalc() != null) sw.writeAscii(",\"boolsCalc\":[]", 15);
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
		
		if(self.bools != null && self.bools.length != 0) {
			sw.writeAscii(",\"bools\":[", 10);
			com.dslplatform.json.BoolConverter.serialize(self.bools[0], sw);
			for(int i = 1; i < self.bools.length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
				com.dslplatform.json.BoolConverter.serialize(self.bools[i], sw);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else if(self.bools != null) sw.writeAscii(",\"bools\":[]", 11);
		else sw.writeAscii(",\"bools\":null", 13);
		
		if(self.getBoolsCalc() != null && self.getBoolsCalc().length != 0) {
			sw.writeAscii(",\"boolsCalc\":[", 14);
			com.dslplatform.json.BoolConverter.serialize(self.getBoolsCalc()[0], sw);
			for(int i = 1; i < self.getBoolsCalc().length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
				com.dslplatform.json.BoolConverter.serialize(self.getBoolsCalc()[i], sw);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else if(self.getBoolsCalc() != null) sw.writeAscii(",\"boolsCalc\":[]", 15);
		else sw.writeAscii(",\"boolsCalc\":null", 17);
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
		boolean[] _bools_ = null;
		boolean[] _boolsCalc_ = null;
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
					case 266799562:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							java.util.ArrayList<Boolean> __res = com.dslplatform.json.BoolConverter.deserializeCollection(reader);
							boolean[] __resUnboxed = new boolean[__res.size()];
							for(int _i=0;_i<__res.size();_i++) 
								__resUnboxed[_i] = __res.get(_i);
							_bools_ = __resUnboxed;
						}
						nextToken = reader.getNextToken();
					}
					else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case -786417937:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							java.util.ArrayList<Boolean> __res = com.dslplatform.json.BoolConverter.deserializeCollection(reader);
							boolean[] __resUnboxed = new boolean[__res.size()];
							for(int _i=0;_i<__res.size();_i++) 
								__resUnboxed[_i] = __res.get(_i);
							_boolsCalc_ = __resUnboxed;
						}
						nextToken = reader.getNextToken();
					}
					else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
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
					case 266799562:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							java.util.ArrayList<Boolean> __res = com.dslplatform.json.BoolConverter.deserializeCollection(reader);
							boolean[] __resUnboxed = new boolean[__res.size()];
							for(int _i=0;_i<__res.size();_i++) 
								__resUnboxed[_i] = __res.get(_i);
							_bools_ = __resUnboxed;
						}
						nextToken = reader.getNextToken();
					}
					else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case -786417937:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							java.util.ArrayList<Boolean> __res = com.dslplatform.json.BoolConverter.deserializeCollection(reader);
							boolean[] __resUnboxed = new boolean[__res.size()];
							for(int _i=0;_i<__res.size();_i++) 
								__resUnboxed[_i] = __res.get(_i);
							_boolsCalc_ = __resUnboxed;
						}
						nextToken = reader.getNextToken();
					}
					else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
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
		this.bools = _bools_;
		this.boolsCalc = _boolsCalc_;
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
		this.__locator = reader.getLocator();
		for (org.revenj.postgres.ObjectConverter.Reader<Document> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.binaries.converters.DocumentConverter.buildURI(reader, this);
		this.__originalValue = (Document)this.clone();
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Document>[] readers, int __index___ID, int __index___name, int __index___content, int __index___bools) {
		
		readers[__index___ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.UuidConverter.parse(reader, false); return item; };
		readers[__index___name] = (item, reader, context) -> { item.name = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
		readers[__index___content] = (item, reader, context) -> { item.content = org.revenj.postgres.converters.ByteaConverter.parse(reader, context); return item; };
		readers[__index___bools] = (item, reader, context) -> { { java.util.List<Boolean> __list = org.revenj.postgres.converters.BoolConverter.parseCollection(reader, context, false); if(__list != null) {
				boolean[] __resUnboxed = new boolean[__list.size()];
				for(int _i=0;_i<__list.size();_i++) {
					__resUnboxed[_i] = __list.get(_i);
				}
				item.bools = __resUnboxed;
			} }; return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Document>[] readers, int __index__extended_ID, int __index__extended_name, int __index__extended_content, int __index__extended_bools) {
		
		readers[__index__extended_ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.UuidConverter.parse(reader, false); return item; };
		readers[__index__extended_name] = (item, reader, context) -> { item.name = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
		readers[__index__extended_content] = (item, reader, context) -> { item.content = org.revenj.postgres.converters.ByteaConverter.parse(reader, context); return item; };
		readers[__index__extended_bools] = (item, reader, context) -> { { java.util.List<Boolean> __list = org.revenj.postgres.converters.BoolConverter.parseCollection(reader, context, false); if(__list != null) {
				boolean[] __resUnboxed = new boolean[__list.size()];
				for(int _i=0;_i<__list.size();_i++) {
					__resUnboxed[_i] = __list.get(_i);
				}
				item.bools = __resUnboxed;
			} }; return item; };
	}
	
	
	public Document(
			final String name,
			final byte[] content,
			final boolean[] bools) {
			
		setName(name);
		setContent(content);
		setBools(bools);
		this.URI = this.ID.toString();
	}

}
