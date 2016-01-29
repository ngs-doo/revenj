/*
* Created by DSL Platform
* v1.5.5871.15913 
*/

package gen.model.education;



public class SortUUIDRoot   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot, com.dslplatform.json.JsonObject {
	
	
	
	public SortUUIDRoot() {
			
		this.ID = 0;
		this.ID = --__SequenceCounterID__;
		this.pero = java.util.UUID.randomUUID();
		this.marker = java.util.UUID.randomUUID();
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
		if (obj == null || obj instanceof SortUUIDRoot == false)
			return false;
		final SortUUIDRoot other = (SortUUIDRoot) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final SortUUIDRoot other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.ID == other.ID))
			return false;
		if(!(this.pero.equals(other.pero)))
			return false;
		if(!(this.marker.equals(other.marker)))
			return false;
		return true;
	}

	private SortUUIDRoot(SortUUIDRoot other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.ID = other.ID;
		this.pero = other.pero;
		this.marker = other.marker;
		this.__originalValue = other.__originalValue;
	}

	@Override
	public Object clone() {
		return new SortUUIDRoot(this);
	}

	@Override
	public String toString() {
		return "SortUUIDRoot(" + URI + ')';
	}
	
	@com.fasterxml.jackson.annotation.JsonCreator private SortUUIDRoot(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("ID") final int ID,
			@com.fasterxml.jackson.annotation.JsonProperty("pero") final java.util.UUID pero,
			@com.fasterxml.jackson.annotation.JsonProperty("marker") final java.util.UUID marker) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.ID = ID;
		this.pero = pero == null ? org.revenj.Utils.MIN_UUID : pero;
		this.marker = marker == null ? org.revenj.Utils.MIN_UUID : marker;
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	private static final long serialVersionUID = -8767804842318347192L;
	
	private int ID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ID")
	public int getID()  {
		
		return ID;
	}

	
	private SortUUIDRoot setID(final int value) {
		
		this.ID = value;
		
		return this;
	}

	
	static {
		gen.model.education.repositories.SortUUIDRootRepository.__setupSequenceID((items, connection) -> {
			try (java.sql.PreparedStatement st = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT nextval('\"education\".\"SortUUIDRoot_ID_seq\"'::regclass)::int FROM generate_series(1, ?)")) {
				st.setInt(1, items.size());
				try (java.sql.ResultSet rs = st.executeQuery()) {
					java.util.Iterator<SortUUIDRoot> iterator = items.iterator();
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
	
	private java.util.UUID pero;

	
	@com.fasterxml.jackson.annotation.JsonProperty("pero")
	public java.util.UUID getPero()  {
		
		return pero;
	}

	
	public SortUUIDRoot setPero(final java.util.UUID value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"pero\" cannot be null!");
		this.pero = value;
		
		return this;
	}

	
	private java.util.UUID marker;

	
	@com.fasterxml.jackson.annotation.JsonProperty("marker")
	public java.util.UUID getMarker()  {
		
		return marker;
	}

	
	public SortUUIDRoot setMarker(final java.util.UUID value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"marker\" cannot be null!");
		this.marker = value;
		
		return this;
	}

	
	static {
		gen.model.education.repositories.SortUUIDRootRepository.__setupPersist(
			(aggregates, arg) -> {
				try {
					for (gen.model.education.SortUUIDRoot agg : aggregates) {
						 
						agg.URI = gen.model.education.converters.SortUUIDRootConverter.buildURI(arg.getKey(), agg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(aggregates, arg) -> {
				try {
					java.util.List<gen.model.education.SortUUIDRoot> oldAggregates = aggregates.getKey();
					java.util.List<gen.model.education.SortUUIDRoot> newAggregates = aggregates.getValue();
					for (int i = 0; i < newAggregates.size(); i++) {
						gen.model.education.SortUUIDRoot oldAgg = oldAggregates.get(i);
						gen.model.education.SortUUIDRoot newAgg = newAggregates.get(i);
						 
						newAgg.URI = gen.model.education.converters.SortUUIDRootConverter.buildURI(arg.getKey(), newAgg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			aggregates -> { 
				for (gen.model.education.SortUUIDRoot agg : aggregates) { 
				}
			},
			agg -> { 
				
		SortUUIDRoot _res = agg.__originalValue;
		agg.__originalValue = (SortUUIDRoot)agg.clone();
		if (_res != null) {
			return _res;
		}
				return null;
			}
		);
	}
	private transient SortUUIDRoot __originalValue;
	
	public void serialize(final com.dslplatform.json.JsonWriter sw, final boolean minimal) {
		sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
		if (minimal) {
			__serializeJsonObjectMinimal(this, sw, false);
		} else {
			__serializeJsonObjectFull(this, sw, false);
		}
		sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
	}

	static void __serializeJsonObjectMinimal(final SortUUIDRoot self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (self.ID != 0) {
				sw.writeAscii(",\"ID\":", 6);
				com.dslplatform.json.NumberConverter.serialize(self.ID, sw);
			}
		
			if (!(self.pero.getMostSignificantBits() == 0 && self.pero.getLeastSignificantBits() == 0)) {
				sw.writeAscii(",\"pero\":", 8);
				com.dslplatform.json.UUIDConverter.serialize(self.pero, sw);
			}
		
			if (!(self.marker.getMostSignificantBits() == 0 && self.marker.getLeastSignificantBits() == 0)) {
				sw.writeAscii(",\"marker\":", 10);
				com.dslplatform.json.UUIDConverter.serialize(self.marker, sw);
			}
	}

	static void __serializeJsonObjectFull(final SortUUIDRoot self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			sw.writeAscii(",\"ID\":", 6);
			com.dslplatform.json.NumberConverter.serialize(self.ID, sw);
		
			
			sw.writeAscii(",\"pero\":", 8);
			com.dslplatform.json.UUIDConverter.serialize(self.pero, sw);
		
			
			sw.writeAscii(",\"marker\":", 10);
			com.dslplatform.json.UUIDConverter.serialize(self.marker, sw);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<SortUUIDRoot> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<SortUUIDRoot>() {
		@Override
		public SortUUIDRoot deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.education.SortUUIDRoot(reader);
		}
	};

	private SortUUIDRoot(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		int _ID_ = 0;
		java.util.UUID _pero_ = org.revenj.Utils.MIN_UUID;
		java.util.UUID _marker_ = org.revenj.Utils.MIN_UUID;
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
					case -1292880239:
						_pero_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -1208471145:
						_marker_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
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
					case -1292880239:
						_pero_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -1208471145:
						_marker_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
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
		this.pero = _pero_;
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
				return new gen.model.education.SortUUIDRoot(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public SortUUIDRoot(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<SortUUIDRoot>[] readers) throws java.io.IOException {
		this.__locator = reader.getLocator();
		for (org.revenj.postgres.ObjectConverter.Reader<SortUUIDRoot> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.education.converters.SortUUIDRootConverter.buildURI(reader, this);
		this.__originalValue = (SortUUIDRoot)this.clone();
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<SortUUIDRoot>[] readers, int __index___ID, int __index___pero, int __index___marker) {
		
		readers[__index___ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
		readers[__index___pero] = (item, reader, context) -> { item.pero = org.revenj.postgres.converters.UuidConverter.parse(reader, false); return item; };
		readers[__index___marker] = (item, reader, context) -> { item.marker = org.revenj.postgres.converters.UuidConverter.parse(reader, false); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<SortUUIDRoot>[] readers, int __index__extended_ID, int __index__extended_pero, int __index__extended_marker) {
		
		readers[__index__extended_ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
		readers[__index__extended_pero] = (item, reader, context) -> { item.pero = org.revenj.postgres.converters.UuidConverter.parse(reader, false); return item; };
		readers[__index__extended_marker] = (item, reader, context) -> { item.marker = org.revenj.postgres.converters.UuidConverter.parse(reader, false); return item; };
	}
	
	
	public SortUUIDRoot(
			final java.util.UUID pero,
			final java.util.UUID marker) {
			
		this.ID = --__SequenceCounterID__;
		setPero(pero);
		setMarker(marker);
		this.URI = java.lang.Integer.toString(this.ID);
	}

}
