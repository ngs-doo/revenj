/*
* Created by DSL Platform
* v1.0.0.20667 
*/

package gen.model.issues;



public class TimestampPk   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot, org.revenj.patterns.ObjectHistory, com.dslplatform.json.JsonObject {
	
	
	
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

	private static final long serialVersionUID = -1174789531366006433L;
	
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
						 
						agg.URI = gen.model.issues.converters.TimestampPkConverter.buildURI(arg.getKey(), agg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(aggregates, arg) -> {
				try {
					java.util.List<gen.model.issues.TimestampPk> oldAggregates = aggregates.getKey();
					java.util.List<gen.model.issues.TimestampPk> newAggregates = aggregates.getValue();
					for (int i = 0; i < newAggregates.size(); i++) {
						gen.model.issues.TimestampPk oldAgg = oldAggregates.get(i);
						gen.model.issues.TimestampPk newAgg = newAggregates.get(i);
						 
						newAgg.URI = gen.model.issues.converters.TimestampPkConverter.buildURI(arg.getKey(), newAgg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
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
	

public static class History   implements org.revenj.patterns.History<gen.model.issues.TimestampPk>, com.dslplatform.json.JsonObject {
	
	
	
	public History(
			@com.fasterxml.jackson.annotation.JsonProperty("URI")  final String URI,
			@com.fasterxml.jackson.annotation.JsonProperty("Snapshots")  final java.util.List<gen.model.issues.TimestampPk.Snapshot> Snapshots) {
			
		this.URI = URI;
		this.Snapshots = Snapshots;
	}

	
	private final String URI;

	
	@com.fasterxml.jackson.annotation.JsonProperty("URI")
	public String getURI()  {
		
		return this.URI;
	}

	
	private final java.util.List<gen.model.issues.TimestampPk.Snapshot> Snapshots;

	
	@com.fasterxml.jackson.annotation.JsonProperty("Snapshots")
	public java.util.List<gen.model.issues.TimestampPk.Snapshot> getSnapshots()  {
		
		return this.Snapshots;
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

	static void __serializeJsonObjectMinimal(final History self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			if (!(self.URI.length() == 0)) {
				sw.writeAscii(",\"URI\":", 7);
				sw.writeString(self.URI);
			}
		
		final java.util.List<gen.model.issues.TimestampPk.Snapshot> _tmp_Snapshots_ = self.Snapshots;
		if(_tmp_Snapshots_.size() != 0) {
			sw.writeAscii(",\"Snapshots\":[", 14);
			gen.model.issues.TimestampPk.Snapshot item = _tmp_Snapshots_.get(0);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.issues.TimestampPk.Snapshot.__serializeJsonObjectMinimal(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			for(int i = 1; i < _tmp_Snapshots_.size(); i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
				item = _tmp_Snapshots_.get(i);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.issues.TimestampPk.Snapshot.__serializeJsonObjectMinimal(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
	}

	static void __serializeJsonObjectFull(final History self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			
			sw.writeAscii(",\"URI\":", 7);
			sw.writeString(self.URI);
		
		final java.util.List<gen.model.issues.TimestampPk.Snapshot> _tmp_Snapshots_ = self.Snapshots;
		if(_tmp_Snapshots_.size() != 0) {
			sw.writeAscii(",\"Snapshots\":[", 14);
			gen.model.issues.TimestampPk.Snapshot item = _tmp_Snapshots_.get(0);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.issues.TimestampPk.Snapshot.__serializeJsonObjectFull(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			for(int i = 1; i < _tmp_Snapshots_.size(); i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
				item = _tmp_Snapshots_.get(i);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.issues.TimestampPk.Snapshot.__serializeJsonObjectFull(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else sw.writeAscii(",\"Snapshots\":[]", 15);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<History> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<History>() {
		@Override
		public History deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.issues.TimestampPk.History(reader);
		}
	};

	private History(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		java.util.List<gen.model.issues.TimestampPk.Snapshot> _Snapshots_ = new java.util.ArrayList<gen.model.issues.TimestampPk.Snapshot>(4);
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
						_URI_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case 2125331066:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							reader.deserializeCollection(gen.model.issues.TimestampPk.Snapshot.JSON_READER, _Snapshots_);
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
						_URI_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case 2125331066:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							reader.deserializeCollection(gen.model.issues.TimestampPk.Snapshot.JSON_READER, _Snapshots_);
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
		this.Snapshots = _Snapshots_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.issues.TimestampPk.History(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
}

	

public static class Snapshot   implements org.revenj.patterns.Snapshot<gen.model.issues.TimestampPk>, com.dslplatform.json.JsonObject {
	
	
	
	public Snapshot(
			@com.fasterxml.jackson.annotation.JsonProperty("At")  final java.time.OffsetDateTime At,
			@com.fasterxml.jackson.annotation.JsonProperty("Action")  final String Action,
			@com.fasterxml.jackson.annotation.JsonProperty("Value")  final gen.model.issues.TimestampPk Value) {
			
		this.At = At;
		this.Action = Action;
		this.Value = Value;
		this.URI = Value.getURI() + '/' + At.getNano();
	}

	
	private final java.time.OffsetDateTime At;

	
	@com.fasterxml.jackson.annotation.JsonProperty("At")
	public java.time.OffsetDateTime getAt()  {
		
		return this.At;
	}

	
	private final String Action;

	
	@com.fasterxml.jackson.annotation.JsonProperty("Action")
	public String getAction()  {
		
		return this.Action;
	}

	
	private final gen.model.issues.TimestampPk Value;

	
	@com.fasterxml.jackson.annotation.JsonProperty("Value")
	public gen.model.issues.TimestampPk getValue()  {
		
		return this.Value;
	}

	
	private final String URI;

	
	public String getURI()  {
		
		return this.URI;
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

	static void __serializeJsonObjectMinimal(final Snapshot self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			if (self.At != java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC)) {
				sw.writeAscii(",\"At\":", 6);
				com.dslplatform.json.JavaTimeConverter.serialize(self.At, sw);
			}
		
			if (!(self.Action.length() == 0)) {
				sw.writeAscii(",\"Action\":", 10);
				sw.writeString(self.Action);
			}
		
		if(self.Value != null) {
			sw.writeAscii(",\"Value\":{", 10);
			
					gen.model.issues.TimestampPk.__serializeJsonObjectMinimal(self.Value, sw, false);
					sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
		}
	}

	static void __serializeJsonObjectFull(final Snapshot self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			
			sw.writeAscii(",\"At\":", 6);
			com.dslplatform.json.JavaTimeConverter.serialize(self.At, sw);
		
			
			sw.writeAscii(",\"Action\":", 10);
			sw.writeString(self.Action);
		
		
		if(self.Value != null) {
			sw.writeAscii(",\"Value\":{", 10);
			
					gen.model.issues.TimestampPk.__serializeJsonObjectFull(self.Value, sw, false);
					sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
		} else {
			sw.writeAscii(",\"Value\":null", 13);
		}
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<Snapshot> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<Snapshot>() {
		@Override
		public Snapshot deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.issues.TimestampPk.Snapshot(reader);
		}
	};

	private Snapshot(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		java.time.OffsetDateTime _At_ = org.revenj.Utils.MIN_DATE_TIME;
		String _Action_ = "";
		gen.model.issues.TimestampPk _Value_ = null;
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
					
					case 1456825256:
						_At_ = com.dslplatform.json.JavaTimeConverter.deserializeDateTime(reader);
					nextToken = reader.getNextToken();
						break;
					case 175614239:
						_Action_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -783812246:
						
					if (nextToken == '{') {
						reader.getNextToken();
						_Value_ = gen.model.issues.TimestampPk.JSON_READER.deserialize(reader);
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '{' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
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
					
					case 1456825256:
						_At_ = com.dslplatform.json.JavaTimeConverter.deserializeDateTime(reader);
					nextToken = reader.getNextToken();
						break;
					case 175614239:
						_Action_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -783812246:
						
					if (nextToken == '{') {
						reader.getNextToken();
						_Value_ = gen.model.issues.TimestampPk.JSON_READER.deserialize(reader);
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '{' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
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
		
		this.At = _At_;
		this.Action = _Action_;
		this.Value = _Value_;
		this.URI = this.Value.getURI() + '/' + this.At.getNano();
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.issues.TimestampPk.Snapshot(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
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
		this.__locator = reader.getLocator();
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
