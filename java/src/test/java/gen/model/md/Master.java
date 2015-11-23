package gen.model.md;



public class Master   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot, com.dslplatform.json.JsonObject {
	
	
	
	public Master() {
			
		this.ID = 0;
		this.ID = --__SequenceCounterID__;
		this.details = new gen.model.md.Detail[] { };
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
		if (obj == null || obj instanceof Master == false)
			return false;
		final Master other = (Master) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final Master other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.ID == other.ID))
			return false;
		if(!(java.util.Arrays.equals(this.details, other.details)))
			return false;
		return true;
	}

	private Master(Master other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.ID = other.ID;
		this.details = new gen.model.md.Detail[other.details.length];
			if (other.details != null) {
				for (int _i = 0; _i < other.details.length; _i++) {
					this.details[_i] = (gen.model.md.Detail)other.details[_i].clone();
				}
			};
		this.__originalValue = other.__originalValue;
	}

	@Override
	public Object clone() {
		return new Master(this);
	}

	@Override
	public String toString() {
		return "Master(" + URI + ')';
	}
	
	
	public Master(
			final gen.model.md.Detail[] details) {
			
		this.ID = --__SequenceCounterID__;
		setDetails(details);
		this.URI = java.lang.Integer.toString(this.ID);
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	private static final long serialVersionUID = -4478835048626755815L;
	
	@com.fasterxml.jackson.annotation.JsonCreator private Master(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("ID") final int ID,
			@com.fasterxml.jackson.annotation.JsonProperty("details") final gen.model.md.Detail[] details) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.ID = ID;
		this.details = details == null ? new gen.model.md.Detail[] { } : details;
	}

	
	private int ID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ID")
	public int getID()  {
		
		return ID;
	}

	
	private Master setID(final int value) {
		
		this.ID = value;
		
		return this;
	}

	
	static {
		gen.model.md.repositories.MasterRepository.__setupSequenceID((items, connection) -> {
			try (java.sql.PreparedStatement st = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT nextval('\"md\".\"Master_ID_seq\"'::regclass)::int FROM generate_series(1, ?)")) {
				st.setInt(1, items.size());
				try (java.sql.ResultSet rs = st.executeQuery()) {
					java.util.Iterator<Master> iterator = items.iterator();
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
	
	static void __bindTodetails(java.util.function.Consumer<gen.model.md.Master> binder) {
		__binderdetails = binder;
	}

	private static java.util.function.Consumer<gen.model.md.Master> __binderdetails;
	private static final gen.model.md.Detail[] _defaultdetails = new gen.model.md.Detail[] { };
	
	private gen.model.md.Detail[] details;

	
	@com.fasterxml.jackson.annotation.JsonProperty("details")
	public gen.model.md.Detail[] getDetails()  {
		
		return details;
	}

	
	public Master setDetails(final gen.model.md.Detail[] value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"details\" cannot be null!");
		org.revenj.Guards.checkNulls(value);
		this.details = value;
		
		return this;
	}

	private transient Master __originalValue;
	
	static {
		gen.model.md.repositories.MasterRepository.__setupPersist(
			(aggregates, arg) -> {
				try {
					for (gen.model.md.Master agg : aggregates) {
						
						__binderdetails.accept(agg); 
						agg.URI = gen.model.md.converters.MasterConverter.buildURI(arg, agg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(oldAggregates, newAggregates) -> {
				for (int i = 0; i < newAggregates.size(); i++) {
					gen.model.md.Master oldAgg = oldAggregates.get(i);
					gen.model.md.Master newAgg = newAggregates.get(i);
					
					__binderdetails.accept(newAgg); 
				}
			},
			aggregates -> { 
				for (gen.model.md.Master agg : aggregates) { 
				}
			},
			agg -> { 
				
		Master _res = agg.__originalValue;
		agg.__originalValue = (Master)agg.clone();
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

	static void __serializeJsonObjectMinimal(final Master self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (self.ID != 0) {
				sw.writeAscii(",\"ID\":", 6);
				com.dslplatform.json.NumberConverter.serialize(self.ID, sw);
			}
		
		if(self.details.length != 0) {
			sw.writeAscii(",\"details\":[", 12);
			gen.model.md.Detail item = self.details[0];
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.md.Detail.__serializeJsonObjectMinimal(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			for(int i = 1; i < self.details.length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
				item = self.details[i];
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.md.Detail.__serializeJsonObjectMinimal(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
	}

	static void __serializeJsonObjectFull(final Master self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			sw.writeAscii(",\"ID\":", 6);
			com.dslplatform.json.NumberConverter.serialize(self.ID, sw);
		
		if(self.details.length != 0) {
			sw.writeAscii(",\"details\":[", 12);
			gen.model.md.Detail item = self.details[0];
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.md.Detail.__serializeJsonObjectFull(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			for(int i = 1; i < self.details.length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
				item = self.details[i];
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.md.Detail.__serializeJsonObjectFull(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else sw.writeAscii(",\"details\":[]", 13);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<Master> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<Master>() {
		@Override
		public Master deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.md.Master(reader);
		}
	};

	private Master(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		int _ID_ = 0;
		gen.model.md.Detail[] _details_ = _defaultdetails;
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
					case 1499984805:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken == ']') {
							_details_ = new gen.model.md.Detail[] { };
						} else {
							java.util.ArrayList<gen.model.md.Detail> __res = reader.deserializeCollection(gen.model.md.Detail.JSON_READER);
							_details_ = __res.toArray(new gen.model.md.Detail[__res.size()]);
						}
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
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
					case 1499984805:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken == ']') {
							_details_ = new gen.model.md.Detail[] { };
						} else {
							java.util.ArrayList<gen.model.md.Detail> __res = reader.deserializeCollection(gen.model.md.Detail.JSON_READER);
							_details_ = __res.toArray(new gen.model.md.Detail[__res.size()]);
						}
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
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
		this.details = _details_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.md.Master(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public Master(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Master>[] readers) throws java.io.IOException {
		this.__locator = java.util.Optional.ofNullable(reader.locator);
		for (org.revenj.postgres.ObjectConverter.Reader<Master> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.md.converters.MasterConverter.buildURI(reader, this);
		this.__originalValue = (Master)this.clone();
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Master>[] readers, int __index___ID, gen.model.md.converters.DetailConverter __converter_details, int __index___details) {
		
		readers[__index___ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
		readers[__index___details] = (item, reader, context) -> { { java.util.List<gen.model.md.Detail> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, __converter_details::from); if (__list != null) {item.details = __list.toArray(new gen.model.md.Detail[__list.size()]);} else item.details = new gen.model.md.Detail[] { }; }; return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Master>[] readers, int __index__extended_ID, final gen.model.md.converters.DetailConverter __converter_details, int __index__extended_details) {
		
		readers[__index__extended_ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
		readers[__index__extended_details] = (item, reader, context) -> { { java.util.List<gen.model.md.Detail> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, __converter_details::fromExtended); if (__list != null) {item.details = __list.toArray(new gen.model.md.Detail[__list.size()]);} else item.details = new gen.model.md.Detail[] { }; }; return item; };
	}
}
