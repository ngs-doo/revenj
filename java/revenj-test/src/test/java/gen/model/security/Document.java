package gen.model.security;


@com.fasterxml.jackson.annotation.JsonTypeName("security.Document")
public class Document   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot, gen.model.security.IsActive<gen.model.security.Document>, gen.model.security.Dummy<gen.model.security.Document>, com.dslplatform.json.JsonObject {
	
	
	
	public Document() {
			
		this.ID = 0;
		this.ID = --__SequenceCounterID__;
		this.data = new java.util.LinkedHashMap<String, String>();
		this.deactivated = false;
		this.URI = java.lang.Integer.toString(this.ID);
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
		
		if(!(this.ID == other.ID))
			return false;
		if(!(this.data != null && this.data.equals(other.data) || this.data == null && other.data == null))
			return false;
		if(!(this.deactivated == other.deactivated))
			return false;
		return true;
	}

	private Document(Document other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.ID = other.ID;
		this.data = other.data != null ? new java.util.LinkedHashMap<String, String>(other.data) : null;
		this.__originalValue = other.__originalValue;
		this.deactivated = other.deactivated;
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
			final java.util.Map<String, String> data,
			final boolean deactivated) {
			
		this.ID = --__SequenceCounterID__;
		setData(data);
		setDeactivated(deactivated);
		this.URI = java.lang.Integer.toString(this.ID);
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	private static final long serialVersionUID = -3678225434841192404L;
	
	@com.fasterxml.jackson.annotation.JsonCreator private Document(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("ID") final int ID,
			@com.fasterxml.jackson.annotation.JsonProperty("data") final java.util.Map<String, String> data,
			@com.fasterxml.jackson.annotation.JsonProperty("deactivated") final boolean deactivated) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.ID = ID;
		this.data = data == null ? new java.util.LinkedHashMap<String, String>() : data;
		this.deactivated = deactivated;
	}

	
	private int ID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ID")
	public int getID()  {
		
		return ID;
	}

	
	private Document setID(final int value) {
		
		this.ID = value;
		
		return this;
	}

	
	static {
		gen.model.security.repositories.DocumentRepository.__setupSequenceID((items, connection) -> {
			try (java.sql.PreparedStatement st = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT nextval('\"security\".\"Document_ID_seq\"'::regclass)::int FROM generate_series(1, ?)")) {
				st.setInt(1, items.size());
				try (java.sql.ResultSet rs = st.executeQuery()) {
					java.util.Iterator<Document> iterator = items.iterator();
					while (rs.next()) {
						iterator.next().setID(rs.getInt(1));
					}
				}
			} catch (java.sql.SQLException e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	private static int __SequenceCounterID__;
	
	private java.util.Map<String, String> data;

	
	@com.fasterxml.jackson.annotation.JsonProperty("data")
	public java.util.Map<String, String> getData()  {
		
		return data;
	}

	
	public Document setData(final java.util.Map<String, String> value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"data\" cannot be null!");
		this.data = value;
		
		return this;
	}

	private transient Document __originalValue;
	
	static {
		gen.model.security.repositories.DocumentRepository.__setupPersist(
			(aggregates, arg) -> {
				try {
					for (gen.model.security.Document agg : aggregates) {
						 
						agg.URI = gen.model.security.converters.DocumentConverter.buildURI(arg, agg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(oldAggregates, newAggregates) -> {
				for (int i = 0; i < newAggregates.size(); i++) {
					gen.model.security.Document oldAgg = oldAggregates.get(i);
					gen.model.security.Document newAgg = newAggregates.get(i);
					 
				}
			},
			aggregates -> { 
				for (gen.model.security.Document agg : aggregates) { 
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
	
	private boolean deactivated;

	
	@com.fasterxml.jackson.annotation.JsonProperty("deactivated")
	public boolean getDeactivated()  {
		
		return deactivated;
	}

	
	public Document setDeactivated(final boolean value) {
		
		this.deactivated = value;
		
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

	static public void __serializeJsonObjectMinimal(final Document self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (self.ID != 0) {
				sw.writeAscii(",\"ID\":", 6);
				com.dslplatform.json.NumberConverter.serialize(self.ID, sw);
			}
		
			if (!(self.data.size() == 0)) {
				sw.writeAscii(",\"data\":", 8);
				com.dslplatform.json.MapConverter.serialize(self.data, sw);
			}
		
			if (self.deactivated != false) {
				sw.writeAscii(",\"deactivated\":", 15);
				com.dslplatform.json.BoolConverter.serialize(self.deactivated, sw);
			}
	}

	static public void __serializeJsonObjectFull(final Document self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			sw.writeAscii(",\"ID\":", 6);
			com.dslplatform.json.NumberConverter.serialize(self.ID, sw);
		
			
			sw.writeAscii(",\"data\":", 8);
			com.dslplatform.json.MapConverter.serialize(self.data, sw);
		
			
			sw.writeAscii(",\"deactivated\":", 15);
			com.dslplatform.json.BoolConverter.serialize(self.deactivated, sw);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<Document> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<Document>() {
		@Override
		public Document deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.security.Document(reader);
		}
	};

	private Document(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		int _ID_ = 0;
		java.util.Map<String, String> _data_ = new java.util.LinkedHashMap<String, String>();
		boolean _deactivated_ = false;
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
						_ID_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case -663559515:
						_data_ = com.dslplatform.json.MapConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case 857792277:
						_deactivated_ = com.dslplatform.json.BoolConverter.deserialize(reader);
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
						_ID_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case -663559515:
						_data_ = com.dslplatform.json.MapConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case 857792277:
						_deactivated_ = com.dslplatform.json.BoolConverter.deserialize(reader);
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
		this.data = _data_;
		this.deactivated = _deactivated_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.security.Document(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public Document(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Document>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<Document> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.security.converters.DocumentConverter.buildURI(reader, this);
		this.__locator = java.util.Optional.ofNullable(reader.locator);
		this.__originalValue = (Document)this.clone();
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Document>[] readers, int __index___ID, int __index___data, int __index___deactivated) {
		
		readers[__index___ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
		readers[__index___data] = (item, reader, context) -> { item.data = org.revenj.postgres.converters.HstoreConverter.parse(reader, context, false); return item; };
		readers[__index___deactivated] = (item, reader, context) -> { item.deactivated = org.revenj.postgres.converters.BoolConverter.parse(reader); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Document>[] readers, int __index__extended_ID, int __index__extended_data, int __index__extended_deactivated) {
		
		readers[__index__extended_ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
		readers[__index__extended_data] = (item, reader, context) -> { item.data = org.revenj.postgres.converters.HstoreConverter.parse(reader, context, false); return item; };
		readers[__index__extended_deactivated] = (item, reader, context) -> { item.deactivated = org.revenj.postgres.converters.BoolConverter.parse(reader); return item; };
	}
}
