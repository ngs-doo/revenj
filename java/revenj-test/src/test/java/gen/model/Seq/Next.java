package gen.model.Seq;



public class Next   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot, com.dslplatform.json.JsonObject {
	
	
	
	public Next() {
			
		this.ID = 0;
		this.ID = --__SequenceCounterID__;
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
		if (obj == null || obj instanceof Next == false)
			return false;
		final Next other = (Next) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final Next other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.ID == other.ID))
			return false;
		return true;
	}

	private Next(Next other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.ID = other.ID;
		this.__originalValue = other.__originalValue;
	}

	@Override
	public Object clone() {
		return new Next(this);
	}

	@Override
	public String toString() {
		return "Next(" + URI + ')';
	}
	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	private static final long serialVersionUID = 8358354269631389691L;
	
	@com.fasterxml.jackson.annotation.JsonCreator private Next(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("ID") final int ID) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.ID = ID;
	}

	
	private int ID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ID")
	public int getID()  {
		
		return ID;
	}

	
	private Next setID(final int value) {
		
		this.ID = value;
		
		return this;
	}

	
	static {
		gen.model.Seq.repositories.NextRepository.__setupSequenceID((items, connection) -> {
			try (java.sql.PreparedStatement st = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT nextval('\"Seq\".\"Next_ID_seq\"'::regclass)::int FROM generate_series(1, ?)")) {
				st.setInt(1, items.size());
				try (java.sql.ResultSet rs = st.executeQuery()) {
					java.util.Iterator<Next> iterator = items.iterator();
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
	

public static class BetweenIds   implements java.io.Serializable, org.revenj.patterns.Specification<Next>, com.dslplatform.json.JsonObject {
	
	
	
	public BetweenIds(
			 final Integer min,
			 final int max) {
			
		setMin(min);
		setMax(max);
	}

	
	
	public BetweenIds() {
			
		this.max = 0;
	}

	private static final long serialVersionUID = -6595073311794175271L;
	
	private Integer min;

	
	@com.fasterxml.jackson.annotation.JsonProperty("min")
	public Integer getMin()  {
		
		return min;
	}

	
	public BetweenIds setMin(final Integer value) {
		
		this.min = value;
		
		return this;
	}

	
	private int max;

	
	@com.fasterxml.jackson.annotation.JsonProperty("max")
	public int getMax()  {
		
		return max;
	}

	
	public BetweenIds setMax(final int value) {
		
		this.max = value;
		
		return this;
	}

	
		public boolean test(gen.model.Seq.Next it) {
			return ( this.getMin() == null ||  ( (it.getID() >= this.getMin()) &&  (it.getID() <= this.getMax())));
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

	static void __serializeJsonObjectMinimal(final BetweenIds self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			if (self.min != null) {
			hasWrittenProperty = true;
				sw.writeAscii("\"min\":", 6);
				com.dslplatform.json.NumberConverter.serialize(self.min, sw);
			}
		
			if (self.max != 0) {
			if(hasWrittenProperty) sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
			hasWrittenProperty = true;
				sw.writeAscii("\"max\":", 6);
				com.dslplatform.json.NumberConverter.serialize(self.max, sw);
			}
	}

	static void __serializeJsonObjectFull(final BetweenIds self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			
			if (self.min != null) {
				sw.writeAscii("\"min\":", 6);
				com.dslplatform.json.NumberConverter.serialize(self.min, sw);
			} else {
				sw.writeAscii("\"min\":null", 10);
			}
		
			
			sw.writeAscii(",\"max\":", 7);
			com.dslplatform.json.NumberConverter.serialize(self.max, sw);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<BetweenIds> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<BetweenIds>() {
		@Override
		public BetweenIds deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.Seq.Next.BetweenIds(reader);
		}
	};

	private BetweenIds(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		Integer _min_ = null;
		int _max_ = 0;
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
					
					case -913357481:
						_min_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case -677190887:
						_max_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
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
					
					case -913357481:
						_min_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case -677190887:
						_max_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
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
		
		this.min = _min_;
		this.max = _max_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.Seq.Next.BetweenIds(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
}

	private transient Next __originalValue;
	
	static {
		gen.model.Seq.repositories.NextRepository.__setupPersist(
			(aggregates, arg) -> {
				try {
					for (gen.model.Seq.Next agg : aggregates) {
						 
						agg.URI = gen.model.Seq.converters.NextConverter.buildURI(arg, agg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(oldAggregates, newAggregates) -> {
				for (int i = 0; i < newAggregates.size(); i++) {
					gen.model.Seq.Next oldAgg = oldAggregates.get(i);
					gen.model.Seq.Next newAgg = newAggregates.get(i);
					 
				}
			},
			aggregates -> { 
				for (gen.model.Seq.Next agg : aggregates) { 
				}
			},
			agg -> { 
				
		Next _res = agg.__originalValue;
		agg.__originalValue = (Next)agg.clone();
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

	static void __serializeJsonObjectMinimal(final Next self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (self.ID != 0) {
				sw.writeAscii(",\"ID\":", 6);
				com.dslplatform.json.NumberConverter.serialize(self.ID, sw);
			}
	}

	static void __serializeJsonObjectFull(final Next self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			sw.writeAscii(",\"ID\":", 6);
			com.dslplatform.json.NumberConverter.serialize(self.ID, sw);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<Next> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<Next>() {
		@Override
		public Next deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.Seq.Next(reader);
		}
	};

	private Next(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		int _ID_ = 0;
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
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.Seq.Next(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public Next(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Next>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<Next> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.Seq.converters.NextConverter.buildURI(reader, this);
		this.__locator = java.util.Optional.ofNullable(reader.locator);
		this.__originalValue = (Next)this.clone();
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Next>[] readers, int __index___ID) {
		
		readers[__index___ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Next>[] readers, int __index__extended_ID) {
		
		readers[__index__extended_ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
	}
}
