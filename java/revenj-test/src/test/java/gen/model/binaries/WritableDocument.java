package gen.model.binaries;



public final class WritableDocument   implements java.io.Serializable, org.revenj.patterns.AggregateRoot, org.revenj.patterns.DataSource, com.dslplatform.json.JsonObject {
	
	
	@com.fasterxml.jackson.annotation.JsonCreator 
	public WritableDocument(
			@com.fasterxml.jackson.annotation.JsonProperty("id")  final java.util.UUID id,
			@com.fasterxml.jackson.annotation.JsonProperty("name")  final String name) {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		setId(id);
		setName(name);
	}

	private static final long serialVersionUID = 392268041754983565L;
	
	private String URI;

	
	public String getURI()  {
		
		return this.URI;
	}

	
	static {
		gen.model.binaries.repositories.WritableDocumentRepository.__setupPersist(
			(aggregates, arg) -> {
				try {
					for (gen.model.binaries.WritableDocument agg : aggregates) {
						agg.URI = gen.model.binaries.converters.WritableDocumentConverter.buildURI(arg, agg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		);
	}
	
	private java.util.UUID id;

	
	@com.fasterxml.jackson.annotation.JsonProperty("id")
	public java.util.UUID getId()  {
		
		return id;
	}

	
	public WritableDocument setId(final java.util.UUID value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"id\" cannot be null!");
		this.id = value;
		
		return this;
	}

	
	private String name;

	
	@com.fasterxml.jackson.annotation.JsonProperty("name")
	public String getName()  {
		
		return name;
	}

	
	public WritableDocument setName(final String value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"name\" cannot be null!");
		this.name = value;
		
		return this;
	}

	
	public WritableDocument(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<WritableDocument>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<WritableDocument> rdr : readers) {
			rdr.read(this, reader, context);
		}
		this.URI = gen.model.binaries.converters.WritableDocumentConverter.buildURI(reader, this);
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<WritableDocument>[] readers, int __index___id, int __index___name) {
		
		readers[__index___id] = (item, reader, context) -> { item.id = org.revenj.postgres.converters.UuidConverter.parse(reader, false); return item; };
		readers[__index___name] = (item, reader, context) -> { item.name = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
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

	static void __serializeJsonObjectMinimal(final WritableDocument self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			if (!(self.id.getMostSignificantBits() == 0 && self.id.getLeastSignificantBits() == 0)) {
			hasWrittenProperty = true;
				sw.writeAscii("\"id\":", 5);
				com.dslplatform.json.UUIDConverter.serialize(self.id, sw);
			}
		
			if (!(self.name.length() == 0)) {
			if(hasWrittenProperty) sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
			hasWrittenProperty = true;
				sw.writeAscii("\"name\":", 7);
				sw.writeString(self.name);
			}
	}

	static void __serializeJsonObjectFull(final WritableDocument self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			
			sw.writeAscii("\"id\":", 5);
			com.dslplatform.json.UUIDConverter.serialize(self.id, sw);
		
			
			sw.writeAscii(",\"name\":", 8);
			sw.writeString(self.name);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<WritableDocument> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<WritableDocument>() {
		@Override
		public WritableDocument deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.binaries.WritableDocument(reader);
		}
	};

	private WritableDocument(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		java.util.UUID _id_ = org.revenj.Utils.MIN_UUID;
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
					
					case 926444256:
						_id_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
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
					
					case 926444256:
						_id_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
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
		
		this.id = _id_;
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
				return new gen.model.binaries.WritableDocument(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
}
