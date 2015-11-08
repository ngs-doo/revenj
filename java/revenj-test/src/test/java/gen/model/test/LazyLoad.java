package gen.model.test;



public class LazyLoad   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot, com.dslplatform.json.JsonObject {
	
	
	
	public LazyLoad() {
			
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
		if (obj == null || obj instanceof LazyLoad == false)
			return false;
		final LazyLoad other = (LazyLoad) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final LazyLoad other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.ID == other.ID))
			return false;
		if(!(this.compURI == other.compURI || this.compURI != null && this.compURI.equals(other.compURI)))
			return false;
		if(!(this.compID == other.compID || this.compID != null && this.compID.equals(other.compID)))
			return false;
		if(!(this.sdURI == other.sdURI || this.sdURI != null && this.sdURI.equals(other.sdURI)))
			return false;
		if(!(this.sdID == other.sdID || this.sdID != null && this.sdID.equals(other.sdID)))
			return false;
		return true;
	}

	private LazyLoad(LazyLoad other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.ID = other.ID;
		this.compURI = other.compURI;
		this.compID = other.compID;
		this.sdURI = other.sdURI;
		this.sdID = other.sdID;
		this.__originalValue = other.__originalValue;
	}

	@Override
	public Object clone() {
		return new LazyLoad(this);
	}

	@Override
	public String toString() {
		return "LazyLoad(" + URI + ')';
	}
	
	
	public LazyLoad(
			final gen.model.test.Composite comp,
			final gen.model.test.SingleDetail sd) {
			
		this.ID = --__SequenceCounterID__;
		setComp(comp);
		setSd(sd);
		this.URI = java.lang.Integer.toString(this.ID);
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	private static final long serialVersionUID = 3990865305262503785L;
	
	@com.fasterxml.jackson.annotation.JsonCreator private LazyLoad(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("ID") final int ID,
			@com.fasterxml.jackson.annotation.JsonProperty("compURI") final String compURI,
			@com.fasterxml.jackson.annotation.JsonProperty("compID") final java.util.UUID compID,
			@com.fasterxml.jackson.annotation.JsonProperty("sdURI") final String sdURI,
			@com.fasterxml.jackson.annotation.JsonProperty("sdID") final Integer sdID) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.ID = ID;
		this.compURI = compURI;
		this.compID = compID;
		this.sdURI = sdURI;
		this.sdID = sdID;
	}

	
	private int ID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ID")
	public int getID()  {
		
		return ID;
	}

	
	private LazyLoad setID(final int value) {
		
		this.ID = value;
		
		return this;
	}

	
	static {
		gen.model.test.repositories.LazyLoadRepository.__setupSequenceID((items, connection) -> {
			try (java.sql.PreparedStatement st = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT nextval('\"test\".\"LazyLoad_ID_seq\"'::regclass)::int FROM generate_series(1, ?)")) {
				st.setInt(1, items.size());
				try (java.sql.ResultSet rs = st.executeQuery()) {
					java.util.Iterator<LazyLoad> iterator = items.iterator();
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
	
	private gen.model.test.Composite comp;

	
	@com.fasterxml.jackson.annotation.JsonIgnore
	public gen.model.test.Composite getComp()  {
		
		
		if (__locator.isPresent() && (comp != null && !comp.getURI().equals(compURI) || comp == null && compURI != null)) {
			gen.model.test.repositories.CompositeRepository repository = __locator.get().resolve(gen.model.test.repositories.CompositeRepository.class);
			comp = repository.find(compURI).orElse(null);
		}
		if (this.compURI == null && this.comp != null) this.comp = null;
		return comp;
	}

	
	public LazyLoad setComp(final gen.model.test.Composite value) {
		
		
		if(value != null && value.getURI() == null) throw new IllegalArgumentException("Reference \"test.Composite\" for property \"comp\" must be persisted before it's assigned");
		this.comp = value;
		
		
		if (value == null && this.compID != null) {
			this.compID = null;
		} else if (value != null) {
			this.compID = value.getId();
		}
		this.compURI = value != null ? value.getURI() : null;
		return this;
	}

	
	private String compURI;

	
	@com.fasterxml.jackson.annotation.JsonProperty("compURI")
	public String getCompURI()  {
		
		if (this.comp != null) this.compURI = this.comp.getURI();
		return this.compURI;
	}

	
	private java.util.UUID compID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("compID")
	public java.util.UUID getCompID()  {
		
		if (this.comp != null) this.compID = this.comp.getId();
		return compID;
	}

	
	private LazyLoad setCompID(final java.util.UUID value) {
		
		this.compID = value;
		
		return this;
	}

	
	private gen.model.test.SingleDetail sd;

	
	@com.fasterxml.jackson.annotation.JsonIgnore
	public gen.model.test.SingleDetail getSd()  {
		
		
		if (__locator.isPresent() && (sd != null && !sd.getURI().equals(sdURI) || sd == null && sdURI != null)) {
			gen.model.test.repositories.SingleDetailRepository repository = __locator.get().resolve(gen.model.test.repositories.SingleDetailRepository.class);
			sd = repository.find(sdURI).orElse(null);
		}
		if (this.sdURI == null && this.sd != null) this.sd = null;
		return sd;
	}

	
	public LazyLoad setSd(final gen.model.test.SingleDetail value) {
		
		
		if(value != null && value.getURI() == null) throw new IllegalArgumentException("Reference \"test.SingleDetail\" for property \"sd\" must be persisted before it's assigned");
		this.sd = value;
		
		
		if (value == null && this.sdID != null) {
			this.sdID = null;
		} else if (value != null) {
			this.sdID = value.getID();
		}
		this.sdURI = value != null ? value.getURI() : null;
		return this;
	}

	
	private String sdURI;

	
	@com.fasterxml.jackson.annotation.JsonProperty("sdURI")
	public String getSdURI()  {
		
		if (this.sd != null) this.sdURI = this.sd.getURI();
		return this.sdURI;
	}

	
	private Integer sdID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("sdID")
	public Integer getSdID()  {
		
		if (this.sd != null) this.sdID = this.sd.getID();
		return sdID;
	}

	
	private LazyLoad setSdID(final Integer value) {
		
		this.sdID = value;
		
		return this;
	}

	private transient LazyLoad __originalValue;
	
	static {
		gen.model.test.repositories.LazyLoadRepository.__setupPersist(
			(aggregates, arg) -> {
				try {
					for (gen.model.test.LazyLoad agg : aggregates) {
						 
						agg.URI = gen.model.test.converters.LazyLoadConverter.buildURI(arg, agg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(oldAggregates, newAggregates) -> {
				for (int i = 0; i < newAggregates.size(); i++) {
					gen.model.test.LazyLoad oldAgg = oldAggregates.get(i);
					gen.model.test.LazyLoad newAgg = newAggregates.get(i);
					 
				}
			},
			aggregates -> { 
				for (gen.model.test.LazyLoad agg : aggregates) { 
				}
			},
			agg -> { 
				
		LazyLoad _res = agg.__originalValue;
		agg.__originalValue = (LazyLoad)agg.clone();
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

	static void __serializeJsonObjectMinimal(final LazyLoad self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (self.ID != 0) {
				sw.writeAscii(",\"ID\":", 6);
				com.dslplatform.json.NumberConverter.serialize(self.ID, sw);
			}
		
			if(self.compURI != null) {
				sw.writeAscii(",\"compURI\":");
				com.dslplatform.json.StringConverter.serializeShort(self.compURI, sw);
			}
		
			if (self.compID != null) {
				sw.writeAscii(",\"compID\":", 10);
				com.dslplatform.json.UUIDConverter.serialize(self.compID, sw);
			}
		
			if(self.sdURI != null) {
				sw.writeAscii(",\"sdURI\":");
				com.dslplatform.json.StringConverter.serializeShort(self.sdURI, sw);
			}
		
			if (self.sdID != null) {
				sw.writeAscii(",\"sdID\":", 8);
				com.dslplatform.json.NumberConverter.serialize(self.sdID, sw);
			}
	}

	static void __serializeJsonObjectFull(final LazyLoad self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			sw.writeAscii(",\"ID\":", 6);
			com.dslplatform.json.NumberConverter.serialize(self.ID, sw);
		
			sw.writeAscii(",\"compURI\":");
			com.dslplatform.json.StringConverter.serializeShortNullable(self.compURI, sw);
		
			
			if (self.compID != null) {
				sw.writeAscii(",\"compID\":", 10);
				com.dslplatform.json.UUIDConverter.serialize(self.compID, sw);
			} else {
				sw.writeAscii(",\"compID\":null", 14);
			}
		
			sw.writeAscii(",\"sdURI\":");
			com.dslplatform.json.StringConverter.serializeShortNullable(self.sdURI, sw);
		
			
			if (self.sdID != null) {
				sw.writeAscii(",\"sdID\":", 8);
				com.dslplatform.json.NumberConverter.serialize(self.sdID, sw);
			} else {
				sw.writeAscii(",\"sdID\":null", 12);
			}
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<LazyLoad> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<LazyLoad>() {
		@Override
		public LazyLoad deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.test.LazyLoad(reader);
		}
	};

	private LazyLoad(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		int _ID_ = 0;
		String _compURI_ = null;
		java.util.UUID _compID_ = null;
		String _sdURI_ = null;
		Integer _sdID_ = null;
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
					case -633965396:
						_compURI_ = com.dslplatform.json.StringConverter.deserialize(reader);
							nextToken = reader.getNextToken();
						break;
					case 343988967:
						_compID_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -1210200652:
						_sdURI_ = com.dslplatform.json.StringConverter.deserialize(reader);
							nextToken = reader.getNextToken();
						break;
					case -976169505:
						_sdID_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
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
					case -633965396:
						_compURI_ = com.dslplatform.json.StringConverter.deserialize(reader);
							nextToken = reader.getNextToken();
						break;
					case 343988967:
						_compID_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -1210200652:
						_sdURI_ = com.dslplatform.json.StringConverter.deserialize(reader);
							nextToken = reader.getNextToken();
						break;
					case -976169505:
						_sdID_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
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
		this.compURI = _compURI_;
		this.compID = _compID_;
		this.sdURI = _sdURI_;
		this.sdID = _sdID_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.test.LazyLoad(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public LazyLoad(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<LazyLoad>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<LazyLoad> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.test.converters.LazyLoadConverter.buildURI(reader, this);
		this.__locator = java.util.Optional.ofNullable(reader.locator);
		this.__originalValue = (LazyLoad)this.clone();
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<LazyLoad>[] readers, int __index___ID, int __index___compURI, int __index___compID, int __index___sdURI, int __index___sdID) {
		
		readers[__index___ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
		readers[__index___compURI] = (item, reader, context) -> { item.compURI = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); return item; };
		readers[__index___compID] = (item, reader, context) -> { item.compID = org.revenj.postgres.converters.UuidConverter.parse(reader, true); return item; };
		readers[__index___sdURI] = (item, reader, context) -> { item.sdURI = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); return item; };
		readers[__index___sdID] = (item, reader, context) -> { item.sdID = org.revenj.postgres.converters.IntConverter.parseNullable(reader); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<LazyLoad>[] readers, int __index__extended_ID, int __index__extended_compURI, int __index__extended_compID, int __index__extended_sdURI, int __index__extended_sdID) {
		
		readers[__index__extended_ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
		readers[__index__extended_compURI] = (item, reader, context) -> { item.compURI = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); return item; };
		readers[__index__extended_compID] = (item, reader, context) -> { item.compID = org.revenj.postgres.converters.UuidConverter.parse(reader, true); return item; };
		readers[__index__extended_sdURI] = (item, reader, context) -> { item.sdURI = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); return item; };
		readers[__index__extended_sdID] = (item, reader, context) -> { item.sdID = org.revenj.postgres.converters.IntConverter.parseNullable(reader); return item; };
	}
}
