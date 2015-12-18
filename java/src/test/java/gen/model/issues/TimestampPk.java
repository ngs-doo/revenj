/*
* Created by DSL Platform
* v1.0.0.24260 
*/

package gen.model.issues;



public class TimestampPk   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot, com.dslplatform.json.JsonObject {
	
	
	
	public TimestampPk() {
			
		this.ts = java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC);
		this.d = java.math.BigDecimal.ZERO.setScale(9);
		this.URI = this.ts.toString();
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
		if (obj == null || obj instanceof TimestampPk == false)
			return false;
		final TimestampPk other = (TimestampPk) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final TimestampPk other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.ts == other.ts || this.ts != null && other.ts != null && this.ts.equals(other.ts)))
			return false;
		if(!(this.d == other.d || this.d != null && other.d != null && this.d.compareTo(other.d) == 0))
			return false;
		return true;
	}

	private TimestampPk(TimestampPk other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.ts = other.ts;
		this.d = other.d;
		this.__originalValue = other.__originalValue;
	}

	@Override
	public Object clone() {
		return new TimestampPk(this);
	}

	@Override
	public String toString() {
		return "TimestampPk(" + URI + ')';
	}
	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	
	@com.fasterxml.jackson.annotation.JsonCreator private TimestampPk(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("ts") final java.time.OffsetDateTime ts,
			@com.fasterxml.jackson.annotation.JsonProperty("d") final java.math.BigDecimal d) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.ts = ts == null ? org.revenj.Utils.MIN_DATE_TIME : ts;
		this.d = d == null ? java.math.BigDecimal.ZERO.setScale(9) : d;
	}

	private static final long serialVersionUID = -6964291541568801858L;
	
	private java.time.OffsetDateTime ts;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ts")
	public java.time.OffsetDateTime getTs()  {
		
		return ts;
	}

	
	public TimestampPk setTs(final java.time.OffsetDateTime value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"ts\" cannot be null!");
		this.ts = value;
		
		return this;
	}

	
	private java.math.BigDecimal d;

	
	@com.fasterxml.jackson.annotation.JsonProperty("d")
	public java.math.BigDecimal getD()  {
		
		return d;
	}

	
	public TimestampPk setD(final java.math.BigDecimal value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"d\" cannot be null!");
		org.revenj.Guards.checkScale(value, 9);
		this.d = value;
		
		return this;
	}

	
	static {
		gen.model.issues.repositories.TimestampPkRepository.__setupPersist(
			(aggregates, arg) -> {
				try {
					for (gen.model.issues.TimestampPk agg : aggregates) {
						 
						agg.URI = gen.model.issues.converters.TimestampPkConverter.buildURI(arg, agg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(oldAggregates, newAggregates) -> {
				for (int i = 0; i < newAggregates.size(); i++) {
					gen.model.issues.TimestampPk oldAgg = oldAggregates.get(i);
					gen.model.issues.TimestampPk newAgg = newAggregates.get(i);
					 
				}
			},
			aggregates -> { 
				for (gen.model.issues.TimestampPk agg : aggregates) { 
				}
			},
			agg -> { 
				
		TimestampPk _res = agg.__originalValue;
		agg.__originalValue = (TimestampPk)agg.clone();
		if (_res != null) {
			return _res;
		}
				return null;
			}
		);
	}
	private transient TimestampPk __originalValue;
	
	public void serialize(final com.dslplatform.json.JsonWriter sw, final boolean minimal) {
		sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
		if (minimal) {
			__serializeJsonObjectMinimal(this, sw, false);
		} else {
			__serializeJsonObjectFull(this, sw, false);
		}
		sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
	}

	static void __serializeJsonObjectMinimal(final TimestampPk self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (self.ts != java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC)) {
				sw.writeAscii(",\"ts\":", 6);
				com.dslplatform.json.JavaTimeConverter.serialize(self.ts, sw);
			}
		
			if (!(java.math.BigDecimal.ZERO.compareTo(self.d) == 0)) {
				sw.writeAscii(",\"d\":", 5);
				com.dslplatform.json.NumberConverter.serialize(self.d, sw);
			}
	}

	static void __serializeJsonObjectFull(final TimestampPk self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			sw.writeAscii(",\"ts\":", 6);
			com.dslplatform.json.JavaTimeConverter.serialize(self.ts, sw);
		
			
			sw.writeAscii(",\"d\":", 5);
			com.dslplatform.json.NumberConverter.serialize(self.d, sw);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<TimestampPk> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<TimestampPk>() {
		@Override
		public TimestampPk deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.issues.TimestampPk(reader);
		}
	};

	private TimestampPk(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		java.time.OffsetDateTime _ts_ = org.revenj.Utils.MIN_DATE_TIME;
		java.math.BigDecimal _d_ = java.math.BigDecimal.ZERO.setScale(9);
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
					case 1178947184:
						_ts_ = com.dslplatform.json.JavaTimeConverter.deserializeDateTime(reader);
					nextToken = reader.getNextToken();
						break;
					case -519297933:
						_d_ = com.dslplatform.json.NumberConverter.deserializeDecimal(reader);
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
					case 1178947184:
						_ts_ = com.dslplatform.json.JavaTimeConverter.deserializeDateTime(reader);
					nextToken = reader.getNextToken();
						break;
					case -519297933:
						_d_ = com.dslplatform.json.NumberConverter.deserializeDecimal(reader);
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
		this.ts = _ts_;
		this.d = _d_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.issues.TimestampPk(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public TimestampPk(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<TimestampPk>[] readers) throws java.io.IOException {
		this.__locator = java.util.Optional.ofNullable(reader.locator);
		for (org.revenj.postgres.ObjectConverter.Reader<TimestampPk> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.issues.converters.TimestampPkConverter.buildURI(reader, this);
		this.__originalValue = (TimestampPk)this.clone();
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<TimestampPk>[] readers, int __index___ts, int __index___d) {
		
		readers[__index___ts] = (item, reader, context) -> { item.ts = org.revenj.postgres.converters.TimestampConverter.parseOffset(reader, context, false, true); return item; };
		readers[__index___d] = (item, reader, context) -> { item.d = org.revenj.postgres.converters.DecimalConverter.parse(reader, false); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<TimestampPk>[] readers, int __index__extended_ts, int __index__extended_d) {
		
		readers[__index__extended_ts] = (item, reader, context) -> { item.ts = org.revenj.postgres.converters.TimestampConverter.parseOffset(reader, context, false, true); return item; };
		readers[__index__extended_d] = (item, reader, context) -> { item.d = org.revenj.postgres.converters.DecimalConverter.parse(reader, false); return item; };
	}
	
	
	public TimestampPk(
			final java.time.OffsetDateTime ts,
			final java.math.BigDecimal d) {
			
		setTs(ts);
		setD(d);
		this.URI = this.ts.toString();
	}

}
