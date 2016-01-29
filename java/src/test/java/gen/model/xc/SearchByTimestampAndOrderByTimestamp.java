/*
* Created by DSL Platform
* v1.5.5871.15913 
*/

package gen.model.xc;



public class SearchByTimestampAndOrderByTimestamp   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot, com.dslplatform.json.JsonObject {
	
	
	
	public SearchByTimestampAndOrderByTimestamp() {
			
		this.ID = 0;
		this.ID = --__SequenceCounterID__;
		this.ondate = java.time.OffsetDateTime.now(java.time.ZoneOffset.systemDefault());
		this.marker = "";
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
		if (obj == null || obj instanceof SearchByTimestampAndOrderByTimestamp == false)
			return false;
		final SearchByTimestampAndOrderByTimestamp other = (SearchByTimestampAndOrderByTimestamp) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final SearchByTimestampAndOrderByTimestamp other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.ID == other.ID))
			return false;
		if(!(this.ondate == other.ondate || this.ondate != null && other.ondate != null && this.ondate.equals(other.ondate)))
			return false;
		if(!(this.marker.equals(other.marker)))
			return false;
		return true;
	}

	private SearchByTimestampAndOrderByTimestamp(SearchByTimestampAndOrderByTimestamp other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.ID = other.ID;
		this.ondate = other.ondate;
		this.marker = other.marker;
		this.__originalValue = other.__originalValue;
	}

	@Override
	public Object clone() {
		return new SearchByTimestampAndOrderByTimestamp(this);
	}

	@Override
	public String toString() {
		return "SearchByTimestampAndOrderByTimestamp(" + URI + ')';
	}
	
	@com.fasterxml.jackson.annotation.JsonCreator private SearchByTimestampAndOrderByTimestamp(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("ID") final int ID,
			@com.fasterxml.jackson.annotation.JsonProperty("ondate") final java.time.OffsetDateTime ondate,
			@com.fasterxml.jackson.annotation.JsonProperty("marker") final String marker) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.ID = ID;
		this.ondate = ondate == null ? org.revenj.Utils.MIN_DATE_TIME : ondate;
		this.marker = marker == null ? "" : marker;
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	private static final long serialVersionUID = 4415254124380248590L;
	
	private int ID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ID")
	public int getID()  {
		
		return ID;
	}

	
	private SearchByTimestampAndOrderByTimestamp setID(final int value) {
		
		this.ID = value;
		
		return this;
	}

	
	static {
		gen.model.xc.repositories.SearchByTimestampAndOrderByTimestampRepository.__setupSequenceID((items, connection) -> {
			try (java.sql.PreparedStatement st = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT nextval('\"xc\".\"SearchByTimestampAndOrderByTimestamp_ID_seq\"'::regclass)::int FROM generate_series(1, ?)")) {
				st.setInt(1, items.size());
				try (java.sql.ResultSet rs = st.executeQuery()) {
					java.util.Iterator<SearchByTimestampAndOrderByTimestamp> iterator = items.iterator();
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
	
	private java.time.OffsetDateTime ondate;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ondate")
	public java.time.OffsetDateTime getOndate()  {
		
		return ondate;
	}

	
	public SearchByTimestampAndOrderByTimestamp setOndate(final java.time.OffsetDateTime value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"ondate\" cannot be null!");
		this.ondate = value;
		
		return this;
	}

	
	private String marker;

	
	@com.fasterxml.jackson.annotation.JsonProperty("marker")
	public String getMarker()  {
		
		return marker;
	}

	
	public SearchByTimestampAndOrderByTimestamp setMarker(final String value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"marker\" cannot be null!");
		this.marker = value;
		
		return this;
	}

	
	static {
		gen.model.xc.repositories.SearchByTimestampAndOrderByTimestampRepository.__setupPersist(
			(aggregates, arg) -> {
				try {
					for (gen.model.xc.SearchByTimestampAndOrderByTimestamp agg : aggregates) {
						 
						agg.URI = gen.model.xc.converters.SearchByTimestampAndOrderByTimestampConverter.buildURI(arg.getKey(), agg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(aggregates, arg) -> {
				try {
					java.util.List<gen.model.xc.SearchByTimestampAndOrderByTimestamp> oldAggregates = aggregates.getKey();
					java.util.List<gen.model.xc.SearchByTimestampAndOrderByTimestamp> newAggregates = aggregates.getValue();
					for (int i = 0; i < newAggregates.size(); i++) {
						gen.model.xc.SearchByTimestampAndOrderByTimestamp oldAgg = oldAggregates.get(i);
						gen.model.xc.SearchByTimestampAndOrderByTimestamp newAgg = newAggregates.get(i);
						 
						newAgg.URI = gen.model.xc.converters.SearchByTimestampAndOrderByTimestampConverter.buildURI(arg.getKey(), newAgg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			aggregates -> { 
				for (gen.model.xc.SearchByTimestampAndOrderByTimestamp agg : aggregates) { 
				}
			},
			agg -> { 
				
		SearchByTimestampAndOrderByTimestamp _res = agg.__originalValue;
		agg.__originalValue = (SearchByTimestampAndOrderByTimestamp)agg.clone();
		if (_res != null) {
			return _res;
		}
				return null;
			}
		);
	}
	private transient SearchByTimestampAndOrderByTimestamp __originalValue;
	
	public void serialize(final com.dslplatform.json.JsonWriter sw, final boolean minimal) {
		sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
		if (minimal) {
			__serializeJsonObjectMinimal(this, sw, false);
		} else {
			__serializeJsonObjectFull(this, sw, false);
		}
		sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
	}

	static void __serializeJsonObjectMinimal(final SearchByTimestampAndOrderByTimestamp self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (self.ID != 0) {
				sw.writeAscii(",\"ID\":", 6);
				com.dslplatform.json.NumberConverter.serialize(self.ID, sw);
			}
		
			if (self.ondate != java.time.OffsetDateTime.now(java.time.ZoneOffset.systemDefault())) {
				sw.writeAscii(",\"ondate\":", 10);
				com.dslplatform.json.JavaTimeConverter.serialize(self.ondate, sw);
			}
		
			if (!(self.marker.length() == 0)) {
				sw.writeAscii(",\"marker\":", 10);
				sw.writeString(self.marker);
			}
	}

	static void __serializeJsonObjectFull(final SearchByTimestampAndOrderByTimestamp self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			sw.writeAscii(",\"ID\":", 6);
			com.dslplatform.json.NumberConverter.serialize(self.ID, sw);
		
			
			sw.writeAscii(",\"ondate\":", 10);
			com.dslplatform.json.JavaTimeConverter.serialize(self.ondate, sw);
		
			
			sw.writeAscii(",\"marker\":", 10);
			sw.writeString(self.marker);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<SearchByTimestampAndOrderByTimestamp> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<SearchByTimestampAndOrderByTimestamp>() {
		@Override
		public SearchByTimestampAndOrderByTimestamp deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.xc.SearchByTimestampAndOrderByTimestamp(reader);
		}
	};

	private SearchByTimestampAndOrderByTimestamp(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		int _ID_ = 0;
		java.time.OffsetDateTime _ondate_ = org.revenj.Utils.MIN_DATE_TIME;
		String _marker_ = "";
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
					case -1925119740:
						_ondate_ = com.dslplatform.json.JavaTimeConverter.deserializeDateTime(reader);
					nextToken = reader.getNextToken();
						break;
					case -1208471145:
						_marker_ = com.dslplatform.json.StringConverter.deserialize(reader);
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
					case -1925119740:
						_ondate_ = com.dslplatform.json.JavaTimeConverter.deserializeDateTime(reader);
					nextToken = reader.getNextToken();
						break;
					case -1208471145:
						_marker_ = com.dslplatform.json.StringConverter.deserialize(reader);
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
		this.ondate = _ondate_;
		this.marker = _marker_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.xc.SearchByTimestampAndOrderByTimestamp(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public SearchByTimestampAndOrderByTimestamp(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<SearchByTimestampAndOrderByTimestamp>[] readers) throws java.io.IOException {
		this.__locator = reader.getLocator();
		for (org.revenj.postgres.ObjectConverter.Reader<SearchByTimestampAndOrderByTimestamp> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.xc.converters.SearchByTimestampAndOrderByTimestampConverter.buildURI(reader, this);
		this.__originalValue = (SearchByTimestampAndOrderByTimestamp)this.clone();
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<SearchByTimestampAndOrderByTimestamp>[] readers, int __index___ID, int __index___ondate, int __index___marker) {
		
		readers[__index___ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
		readers[__index___ondate] = (item, reader, context) -> { item.ondate = org.revenj.postgres.converters.TimestampConverter.parseOffset(reader, context, false, false); return item; };
		readers[__index___marker] = (item, reader, context) -> { item.marker = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<SearchByTimestampAndOrderByTimestamp>[] readers, int __index__extended_ID, int __index__extended_ondate, int __index__extended_marker) {
		
		readers[__index__extended_ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
		readers[__index__extended_ondate] = (item, reader, context) -> { item.ondate = org.revenj.postgres.converters.TimestampConverter.parseOffset(reader, context, false, false); return item; };
		readers[__index__extended_marker] = (item, reader, context) -> { item.marker = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
	}
	
	
	public SearchByTimestampAndOrderByTimestamp(
			final java.time.OffsetDateTime ondate,
			final String marker) {
			
		this.ID = --__SequenceCounterID__;
		setOndate(ondate);
		setMarker(marker);
		this.URI = java.lang.Integer.toString(this.ID);
	}

}
