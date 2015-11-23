package gen.model.mixinReference;



public class Child   implements java.lang.Cloneable, java.io.Serializable, com.dslplatform.json.JsonObject {
	
	
	
	public Child() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.version = 0L;
		this.AuthorID = 0;
		this.Index = 0;
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
		if (obj == null || obj instanceof Child == false)
			return false;
		final Child other = (Child) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final Child other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.version == other.version))
			return false;
		if(!(this.AuthorID == other.AuthorID))
			return false;
		if(!(this.Index == other.Index))
			return false;
		return true;
	}

	private Child(Child other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.version = other.version;
		this.AuthorID = other.AuthorID;
		this.Index = other.Index;
	}

	@Override
	public Object clone() {
		return new Child(this);
	}

	@Override
	public String toString() {
		return "Child(" + URI + ')';
	}
	
	
	public Child(
			final long version) {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		setVersion(version);
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	private static final long serialVersionUID = 7096374922428969482L;
	
	@com.fasterxml.jackson.annotation.JsonCreator private Child(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("version") final long version,
			@com.fasterxml.jackson.annotation.JsonProperty("AuthorID") final int AuthorID,
			@com.fasterxml.jackson.annotation.JsonProperty("Index") final int Index) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.version = version;
		this.AuthorID = AuthorID;
		this.Index = Index;
	}

	
	private long version;

	
	@com.fasterxml.jackson.annotation.JsonProperty("version")
	public long getVersion()  {
		
		return version;
	}

	
	public Child setVersion(final long value) {
		
		this.version = value;
		
		return this;
	}

	
	private int AuthorID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("AuthorID")
	public int getAuthorID()  {
		
		return AuthorID;
	}

	
	private Child setAuthorID(final int value) {
		
		this.AuthorID = value;
		
		return this;
	}

	
	private int Index;

	
	@com.fasterxml.jackson.annotation.JsonProperty("Index")
	public int getIndex()  {
		
		return Index;
	}

	
	private Child setIndex(final int value) {
		
		this.Index = value;
		
		return this;
	}

	
	static {
		gen.model.mixinReference.Author.__bindTochildren(parent -> {
			int i = 0;
			for (gen.model.mixinReference.Child e : parent.getChildren()) { 
				e.AuthorID = parent.getID();
				e.Index = i++; 
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

	static void __serializeJsonObjectMinimal(final Child self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (self.version != 0L) {
				sw.writeAscii(",\"version\":", 11);
				com.dslplatform.json.NumberConverter.serialize(self.version, sw);
			}
		
			if (self.AuthorID != 0) {
				sw.writeAscii(",\"AuthorID\":", 12);
				com.dslplatform.json.NumberConverter.serialize(self.AuthorID, sw);
			}
		
			if (self.Index != 0) {
				sw.writeAscii(",\"Index\":", 9);
				com.dslplatform.json.NumberConverter.serialize(self.Index, sw);
			}
	}

	static void __serializeJsonObjectFull(final Child self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			sw.writeAscii(",\"version\":", 11);
			com.dslplatform.json.NumberConverter.serialize(self.version, sw);
		
			
			sw.writeAscii(",\"AuthorID\":", 12);
			com.dslplatform.json.NumberConverter.serialize(self.AuthorID, sw);
		
			
			sw.writeAscii(",\"Index\":", 9);
			com.dslplatform.json.NumberConverter.serialize(self.Index, sw);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<Child> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<Child>() {
		@Override
		public Child deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.mixinReference.Child(reader);
		}
	};

	private Child(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		long _version_ = 0L;
		int _AuthorID_ = 0;
		int _Index_ = 0;
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
					case 1181855383:
						_version_ = com.dslplatform.json.NumberConverter.deserializeLong(reader);
					nextToken = reader.getNextToken();
						break;
					case 23797067:
						_AuthorID_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case -1362645429:
						_Index_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
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
					case 1181855383:
						_version_ = com.dslplatform.json.NumberConverter.deserializeLong(reader);
					nextToken = reader.getNextToken();
						break;
					case 23797067:
						_AuthorID_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case -1362645429:
						_Index_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
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
		this.version = _version_;
		this.AuthorID = _AuthorID_;
		this.Index = _Index_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.mixinReference.Child(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public Child(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Child>[] readers) throws java.io.IOException {
		this.__locator = java.util.Optional.ofNullable(reader.locator);
		for (org.revenj.postgres.ObjectConverter.Reader<Child> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.mixinReference.converters.ChildConverter.buildURI(reader, this);
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Child>[] readers, int __index___version, int __index___AuthorID, int __index___Index) {
		
		readers[__index___version] = (item, reader, context) -> { item.version = org.revenj.postgres.converters.LongConverter.parse(reader); return item; };
		readers[__index___AuthorID] = (item, reader, context) -> { item.AuthorID = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
		readers[__index___Index] = (item, reader, context) -> { item.Index = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Child>[] readers, int __index__extended_version, int __index__extended_AuthorID, int __index__extended_Index) {
		
		readers[__index__extended_version] = (item, reader, context) -> { item.version = org.revenj.postgres.converters.LongConverter.parse(reader); return item; };
		readers[__index__extended_AuthorID] = (item, reader, context) -> { item.AuthorID = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
		readers[__index__extended_Index] = (item, reader, context) -> { item.Index = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
	}
}
