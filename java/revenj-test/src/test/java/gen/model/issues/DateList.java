package gen.model.issues;



public class DateList   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot, com.dslplatform.json.JsonObject {
	
	
	
	public DateList() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.ID = 0L;
		this.ID = --__SequenceCounterID__;
		this.list = new java.util.ArrayList<java.time.OffsetDateTime>(4);
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
		if (obj == null || obj instanceof DateList == false)
			return false;
		final DateList other = (DateList) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final DateList other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.ID == other.ID))
			return false;
		if(!((this.list == other.list || this.list != null && this.list.equals(other.list))))
			return false;
		return true;
	}

	private DateList(DateList other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.ID = other.ID;
		this.list = new java.util.ArrayList<java.time.OffsetDateTime>(other.list);
		this.__originalValue = other.__originalValue;
	}

	@Override
	public Object clone() {
		return new DateList(this);
	}

	@Override
	public String toString() {
		return "DateList(" + URI + ')';
	}
	
	
	public DateList(
			final java.util.List<java.time.OffsetDateTime> list) {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.ID = --__SequenceCounterID__;
		setList(list);
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	private static final long serialVersionUID = 1149763702621201501L;
	
	@com.fasterxml.jackson.annotation.JsonCreator private DateList(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("ID") final long ID,
			@com.fasterxml.jackson.annotation.JsonProperty("list") final java.util.List<java.time.OffsetDateTime> list) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.ID = ID;
		this.list = list == null ? new java.util.ArrayList<java.time.OffsetDateTime>(4) : list;
	}

	
	private long ID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ID")
	public long getID()  {
		
		return ID;
	}

	
	private DateList setID(final long value) {
		
		this.ID = value;
		
		return this;
	}

	
	static {
		gen.model.issues.repositories.DateListRepository.__setupSequenceID((items, connection) -> {
			try (java.sql.PreparedStatement st = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT nextval('\"issues\".\"DateList_ID_seq\"'::regclass)::bigint FROM generate_series(1, ?)")) {
				st.setInt(1, items.size());
				try (java.sql.ResultSet rs = st.executeQuery()) {
					java.util.Iterator<DateList> iterator = items.iterator();
					while (rs.next()) {
						iterator.next().setID(rs.getLong(1));
					}
				}
			} catch (java.sql.SQLException e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	private static long __SequenceCounterID__;
	
	private java.util.List<java.time.OffsetDateTime> list;

	
	@com.fasterxml.jackson.annotation.JsonProperty("list")
	public java.util.List<java.time.OffsetDateTime> getList()  {
		
		return list;
	}

	
	public DateList setList(final java.util.List<java.time.OffsetDateTime> value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"list\" cannot be null!");
		this.list = value;
		
		return this;
	}

	private transient DateList __originalValue;
	
	static {
		gen.model.issues.repositories.DateListRepository.__setupPersist(
			(aggregates, arg) -> {
				try {
					for (gen.model.issues.DateList agg : aggregates) {
						 
						agg.URI = gen.model.issues.converters.DateListConverter.buildURI(arg, agg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(oldAggregates, newAggregates) -> {
				for (int i = 0; i < newAggregates.size(); i++) {
					gen.model.issues.DateList oldAgg = oldAggregates.get(i);
					gen.model.issues.DateList newAgg = newAggregates.get(i);
					 
				}
			},
			aggregates -> { 
				for (gen.model.issues.DateList agg : aggregates) { 
				}
			},
			agg -> { 
				
		DateList _res = agg.__originalValue;
		agg.__originalValue = (DateList)agg.clone();
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

	static void __serializeJsonObjectMinimal(final DateList self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (self.ID != 0L) {
				sw.writeAscii(",\"ID\":", 6);
				com.dslplatform.json.NumberConverter.serialize(self.ID, sw);
			}
		
		if(self.list.size() != 0) {
			sw.writeAscii(",\"list\":[", 9);
			org.revenj.json.JavaTimeConverter.serializeNullable(self.list.get(0), sw);
			for(int i = 1; i < self.list.size(); i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
				org.revenj.json.JavaTimeConverter.serializeNullable(self.list.get(i), sw);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
	}

	static void __serializeJsonObjectFull(final DateList self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			sw.writeAscii(",\"ID\":", 6);
			com.dslplatform.json.NumberConverter.serialize(self.ID, sw);
		
		if(self.list.size() != 0) {
			sw.writeAscii(",\"list\":[", 9);
			org.revenj.json.JavaTimeConverter.serializeNullable(self.list.get(0), sw);
			for(int i = 1; i < self.list.size(); i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
				org.revenj.json.JavaTimeConverter.serializeNullable(self.list.get(i), sw);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else sw.writeAscii(",\"list\":[]", 10);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<DateList> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<DateList>() {
		@Override
		public DateList deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.issues.DateList(reader);
		}
	};

	private DateList(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		long _ID_ = 0L;
		java.util.List<java.time.OffsetDateTime> _list_ = new java.util.ArrayList<java.time.OffsetDateTime>(4);
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
						_ID_ = com.dslplatform.json.NumberConverter.deserializeLong(reader);
					nextToken = reader.getNextToken();
						break;
					case 217798785:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							org.revenj.json.JavaTimeConverter.deserializeDateTimeNullableCollection(reader, _list_);
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
						_ID_ = com.dslplatform.json.NumberConverter.deserializeLong(reader);
					nextToken = reader.getNextToken();
						break;
					case 217798785:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							org.revenj.json.JavaTimeConverter.deserializeDateTimeNullableCollection(reader, _list_);
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
		this.list = _list_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.issues.DateList(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public DateList(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<DateList>[] readers) throws java.io.IOException {
		this.__locator = java.util.Optional.ofNullable(reader.locator);
		for (org.revenj.postgres.ObjectConverter.Reader<DateList> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.issues.converters.DateListConverter.buildURI(reader, this);
		this.__originalValue = (DateList)this.clone();
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<DateList>[] readers, int __index___ID, int __index___list) {
		
		readers[__index___ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.LongConverter.parse(reader); return item; };
		readers[__index___list] = (item, reader, context) -> { { java.util.List<java.time.OffsetDateTime> __list = org.revenj.postgres.converters.TimestampConverter.parseOffsetCollection(reader, context, true, true); if(__list != null) {item.list = __list;} else item.list = new java.util.ArrayList<java.time.OffsetDateTime>(4); }; return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<DateList>[] readers, int __index__extended_ID, int __index__extended_list) {
		
		readers[__index__extended_ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.LongConverter.parse(reader); return item; };
		readers[__index__extended_list] = (item, reader, context) -> { { java.util.List<java.time.OffsetDateTime> __list = org.revenj.postgres.converters.TimestampConverter.parseOffsetCollection(reader, context, true, true); if(__list != null) {item.list = __list;} else item.list = new java.util.ArrayList<java.time.OffsetDateTime>(4); }; return item; };
	}
}
