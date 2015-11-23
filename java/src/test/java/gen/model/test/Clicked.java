package gen.model.test;



public final class Clicked   implements java.io.Serializable, com.dslplatform.json.JsonObject, org.revenj.patterns.DomainEvent {
	
	
	
	public Clicked(
			 final java.time.LocalDate date,
			 final java.math.BigDecimal number,
			 final Long bigint,
			 final java.util.Set<Boolean> bool,
			 final gen.model.test.En en) {
			
		setDate(date);
		setNumber(number);
		setBigint(bigint);
		setBool(bool);
		setEn(en);
	}

	
	
	public Clicked() {
			
		this.number = java.math.BigDecimal.ZERO;
		this.bool = new java.util.LinkedHashSet<Boolean>(4);
	}

	
	private String URI;

	
	@com.fasterxml.jackson.annotation.JsonProperty("URI")
	public String getURI()  {
		
		return this.URI;
	}

	
	private java.time.OffsetDateTime ProcessedAt;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ProcessedAt")
	public java.time.OffsetDateTime getProcessedAt()  {
		
		return this.ProcessedAt;
	}

	
	private java.time.OffsetDateTime QueuedAt;

	
	@com.fasterxml.jackson.annotation.JsonProperty("QueuedAt")
	public java.time.OffsetDateTime getQueuedAt()  {
		
		return this.QueuedAt;
	}

	
	@Override
	public int hashCode() {
		return URI != null ? URI.hashCode() : super.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;
		final Clicked other = (Clicked) obj;

		return URI != null && URI.equals(other.URI);
	}

	@Override
	public String toString() {
		return URI != null ? "Clicked(" + URI + ')' : "new Clicked(" + super.hashCode() + ')';
	}
	private static final long serialVersionUID = 4451585166867280928L;
	
	private java.time.LocalDate date;

	
	@com.fasterxml.jackson.annotation.JsonProperty("date")
	public java.time.LocalDate getDate()  {
		
		return date;
	}

	
	public Clicked setDate(final java.time.LocalDate value) {
		
		this.date = value;
		
		return this;
	}

	
	private java.math.BigDecimal number;

	
	@com.fasterxml.jackson.annotation.JsonProperty("number")
	public java.math.BigDecimal getNumber()  {
		
		return number;
	}

	
	public Clicked setNumber(final java.math.BigDecimal value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"number\" cannot be null!");
		this.number = value;
		
		return this;
	}

	
	private Long bigint;

	
	@com.fasterxml.jackson.annotation.JsonProperty("bigint")
	public Long getBigint()  {
		
		return bigint;
	}

	
	public Clicked setBigint(final Long value) {
		
		this.bigint = value;
		
		return this;
	}

	
	private java.util.Set<Boolean> bool;

	
	@com.fasterxml.jackson.annotation.JsonProperty("bool")
	public java.util.Set<Boolean> getBool()  {
		
		return bool;
	}

	
	public Clicked setBool(final java.util.Set<Boolean> value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"bool\" cannot be null!");
		org.revenj.Guards.checkNulls(value);
		this.bool = value;
		
		return this;
	}

	
	private gen.model.test.En en;

	
	@com.fasterxml.jackson.annotation.JsonProperty("en")
	public gen.model.test.En getEn()  {
		
		return en;
	}

	
	public Clicked setEn(final gen.model.test.En value) {
		
		this.en = value;
		
		return this;
	}

	

public static class BetweenNumbers   implements java.io.Serializable, org.revenj.patterns.Specification<Clicked>, com.dslplatform.json.JsonObject {
	
	
	
	public BetweenNumbers(
			 final java.math.BigDecimal min,
			 final java.util.Set<java.math.BigDecimal> inSet,
			 final gen.model.test.En en) {
			
		setMin(min);
		setInSet(inSet);
		setEn(en);
	}

	
	
	public BetweenNumbers() {
			
		this.min = java.math.BigDecimal.ZERO;
		this.inSet = new java.util.LinkedHashSet<java.math.BigDecimal>(4);
	}

	private static final long serialVersionUID = 5678635092106778093L;
	
	private java.math.BigDecimal min;

	
	@com.fasterxml.jackson.annotation.JsonProperty("min")
	public java.math.BigDecimal getMin()  {
		
		return min;
	}

	
	public BetweenNumbers setMin(final java.math.BigDecimal value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"min\" cannot be null!");
		this.min = value;
		
		return this;
	}

	
	private java.util.Set<java.math.BigDecimal> inSet;

	
	@com.fasterxml.jackson.annotation.JsonProperty("inSet")
	public java.util.Set<java.math.BigDecimal> getInSet()  {
		
		return inSet;
	}

	
	public BetweenNumbers setInSet(final java.util.Set<java.math.BigDecimal> value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"inSet\" cannot be null!");
		org.revenj.Guards.checkNulls(value);
		this.inSet = value;
		
		return this;
	}

	
	private gen.model.test.En en;

	
	@com.fasterxml.jackson.annotation.JsonProperty("en")
	public gen.model.test.En getEn()  {
		
		return en;
	}

	
	public BetweenNumbers setEn(final gen.model.test.En value) {
		
		this.en = value;
		
		return this;
	}

	
		public boolean test(gen.model.test.Clicked it) {
			return ( ( it.getNumber().compareTo(this.getMin()) >= 0 && (this.getInSet().contains(it.getNumber()))) &&  it.getEn().equals(this.getEn()));
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

	static void __serializeJsonObjectMinimal(final BetweenNumbers self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			if (!(java.math.BigDecimal.ZERO.compareTo(self.min) == 0)) {
			hasWrittenProperty = true;
				sw.writeAscii("\"min\":", 6);
				com.dslplatform.json.NumberConverter.serialize(self.min, sw);
			}
		
		if(self.inSet.size() != 0) {
			if(hasWrittenProperty) sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
			hasWrittenProperty = true;
			sw.writeAscii("\"inSet\":[", 9);
			java.math.BigDecimal item;
			java.util.Iterator<java.math.BigDecimal> iterator = self.inSet.iterator();
			int total = self.inSet.size() - 1;
			for(int i = 0; i < total; i++) {
				item = iterator.next();com.dslplatform.json.NumberConverter.serialize(item, sw);
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
			}
			item = iterator.next();com.dslplatform.json.NumberConverter.serialize(item, sw);
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		
		if(self.en != null) {
			if(hasWrittenProperty) sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
			hasWrittenProperty = true;
			sw.writeAscii("\"en\":\"", 6);
			sw.writeAscii(self.en.name());
			sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
		}
	}

	static void __serializeJsonObjectFull(final BetweenNumbers self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			
			sw.writeAscii("\"min\":", 6);
			com.dslplatform.json.NumberConverter.serialize(self.min, sw);
		
		if(self.inSet.size() != 0) {
			sw.writeAscii(",\"inSet\":[", 10);
			java.math.BigDecimal item;
			java.util.Iterator<java.math.BigDecimal> iterator = self.inSet.iterator();
			int total = self.inSet.size() - 1;
			for(int i = 0; i < total; i++) {
				item = iterator.next();com.dslplatform.json.NumberConverter.serialize(item, sw);
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
			}
			item = iterator.next();com.dslplatform.json.NumberConverter.serialize(item, sw);
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else sw.writeAscii(",\"inSet\":[]", 11);
		
		
		if(self.en != null) {
			sw.writeAscii(",\"en\":\"", 7);
			sw.writeAscii(self.en.name());
			sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
		} else {
			sw.writeAscii(",\"en\":null", 10);
		}
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<BetweenNumbers> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<BetweenNumbers>() {
		@Override
		public BetweenNumbers deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.test.Clicked.BetweenNumbers(reader);
		}
	};

	private BetweenNumbers(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		java.math.BigDecimal _min_ = java.math.BigDecimal.ZERO;
		java.util.Set<java.math.BigDecimal> _inSet_ = new java.util.LinkedHashSet<java.math.BigDecimal>(4);
		gen.model.test.En _en_ = null;
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
						_min_ = com.dslplatform.json.NumberConverter.deserializeDecimal(reader);
					nextToken = reader.getNextToken();
						break;
					case -505761338:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							com.dslplatform.json.NumberConverter.deserializeDecimalCollection(reader, _inSet_);
						}
						nextToken = reader.getNextToken();
					}
					else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case 1092248970:
						
					if (nextToken == '"') {
						switch(reader.calcHash()) {
							case -1005848884: _en_ = gen.model.test.En.A; break;
							case -955516027: _en_ = gen.model.test.En.B; break;
							default:
								throw new java.io.IOException("Unknown enum value: '" + reader.getLastName() + "' at position " + reader.positionInStream());
						}
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '\"' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
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
						_min_ = com.dslplatform.json.NumberConverter.deserializeDecimal(reader);
					nextToken = reader.getNextToken();
						break;
					case -505761338:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							com.dslplatform.json.NumberConverter.deserializeDecimalCollection(reader, _inSet_);
						}
						nextToken = reader.getNextToken();
					}
					else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case 1092248970:
						
					if (nextToken == '"') {
						switch(reader.calcHash()) {
							case -1005848884: _en_ = gen.model.test.En.A; break;
							case -955516027: _en_ = gen.model.test.En.B; break;
							default:
								throw new java.io.IOException("Unknown enum value: '" + reader.getLastName() + "' at position " + reader.positionInStream());
						}
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '\"' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
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
		this.inSet = _inSet_;
		this.en = _en_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.test.Clicked.BetweenNumbers(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
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

	static void __serializeJsonObjectMinimal(final Clicked self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
		com.dslplatform.json.StringConverter.serializeShortNullable(self.URI, sw);
		if (self.ProcessedAt != null) {
			sw.writeAscii(",\"ProcessedAt\":");
			org.revenj.json.JavaTimeConverter.serialize(self.ProcessedAt, sw);
		}
		if (self.QueuedAt != null) {
			sw.writeAscii(",\"QueuedAt\":");
			org.revenj.json.JavaTimeConverter.serialize(self.QueuedAt, sw);
		}
		
			if (self.date != null) {
				sw.writeAscii(",\"date\":", 8);
				org.revenj.json.JavaTimeConverter.serialize(self.date, sw);
			}
		
			if (!(java.math.BigDecimal.ZERO.compareTo(self.number) == 0)) {
				sw.writeAscii(",\"number\":", 10);
				com.dslplatform.json.NumberConverter.serialize(self.number, sw);
			}
		
			if (self.bigint != null) {
				sw.writeAscii(",\"bigint\":", 10);
				com.dslplatform.json.NumberConverter.serialize(self.bigint, sw);
			}
		
		if(self.bool.size() != 0) {
			sw.writeAscii(",\"bool\":[", 9);
			Boolean item;
			java.util.Iterator<Boolean> iterator = self.bool.iterator();
			int total = self.bool.size() - 1;
			for(int i = 0; i < total; i++) {
				item = iterator.next();com.dslplatform.json.BoolConverter.serialize(item, sw);
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
			}
			item = iterator.next();com.dslplatform.json.BoolConverter.serialize(item, sw);
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		
		if(self.en != null) {
			sw.writeAscii(",\"en\":\"", 7);
			sw.writeAscii(self.en.name());
			sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
		}
	}

	static void __serializeJsonObjectFull(final Clicked self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
		com.dslplatform.json.StringConverter.serializeShortNullable(self.URI, sw);
		if (self.ProcessedAt != null) {
			sw.writeAscii(",\"ProcessedAt\":");
			org.revenj.json.JavaTimeConverter.serialize(self.ProcessedAt, sw);
		} else {
			sw.writeAscii(",\"ProcessedAt\":null");
		}
		if (self.QueuedAt != null) {
			sw.writeAscii(",\"QueuedAt\":");
			org.revenj.json.JavaTimeConverter.serialize(self.QueuedAt, sw);
		} else {
			sw.writeAscii(",\"QueuedAt\":null");
		}
		
			
			if (self.date != null) {
				sw.writeAscii(",\"date\":", 8);
				org.revenj.json.JavaTimeConverter.serialize(self.date, sw);
			} else {
				sw.writeAscii(",\"date\":null", 12);
			}
		
			
			sw.writeAscii(",\"number\":", 10);
			com.dslplatform.json.NumberConverter.serialize(self.number, sw);
		
			
			if (self.bigint != null) {
				sw.writeAscii(",\"bigint\":", 10);
				com.dslplatform.json.NumberConverter.serialize(self.bigint, sw);
			} else {
				sw.writeAscii(",\"bigint\":null", 14);
			}
		
		if(self.bool.size() != 0) {
			sw.writeAscii(",\"bool\":[", 9);
			Boolean item;
			java.util.Iterator<Boolean> iterator = self.bool.iterator();
			int total = self.bool.size() - 1;
			for(int i = 0; i < total; i++) {
				item = iterator.next();com.dslplatform.json.BoolConverter.serialize(item, sw);
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
			}
			item = iterator.next();com.dslplatform.json.BoolConverter.serialize(item, sw);
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else sw.writeAscii(",\"bool\":[]", 10);
		
		
		if(self.en != null) {
			sw.writeAscii(",\"en\":\"", 7);
			sw.writeAscii(self.en.name());
			sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
		} else {
			sw.writeAscii(",\"en\":null", 10);
		}
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<Clicked> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<Clicked>() {
		@Override
		public Clicked deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.test.Clicked(reader);
		}
	};

	private Clicked(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
			java.time.OffsetDateTime _ProcessedAt_ = null;
			java.time.OffsetDateTime _QueuedAt_ = null;
		java.time.LocalDate _date_ = null;
		java.math.BigDecimal _number_ = java.math.BigDecimal.ZERO;
		Long _bigint_ = null;
		java.util.Set<Boolean> _bool_ = new java.util.LinkedHashSet<Boolean>(4);
		gen.model.test.En _en_ = null;
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
					case -497530082:
						_ProcessedAt_ = org.revenj.json.JavaTimeConverter.deserializeDateTime(reader);
				nextToken = reader.getNextToken();
						break;
					case -1790398591:
						_QueuedAt_ = org.revenj.json.JavaTimeConverter.deserializeDateTime(reader);
				nextToken = reader.getNextToken();
						break;
					case -730669991:
						_date_ = org.revenj.json.JavaTimeConverter.deserializeLocalDate(reader);
					nextToken = reader.getNextToken();
						break;
					case 467038368:
						_number_ = com.dslplatform.json.NumberConverter.deserializeDecimal(reader);
					nextToken = reader.getNextToken();
						break;
					case -1972918838:
						_bigint_ = com.dslplatform.json.NumberConverter.deserializeLong(reader);
					nextToken = reader.getNextToken();
						break;
					case -929786563:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							com.dslplatform.json.BoolConverter.deserializeCollection(reader, _bool_);
						}
						nextToken = reader.getNextToken();
					}
					else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case 1092248970:
						
					if (nextToken == '"') {
						switch(reader.calcHash()) {
							case -1005848884: _en_ = gen.model.test.En.A; break;
							case -955516027: _en_ = gen.model.test.En.B; break;
							default:
								throw new java.io.IOException("Unknown enum value: '" + reader.getLastName() + "' at position " + reader.positionInStream());
						}
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '\"' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
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
					case -497530082:
						_ProcessedAt_ = org.revenj.json.JavaTimeConverter.deserializeDateTime(reader);
				nextToken = reader.getNextToken();
						break;
					case -1790398591:
						_QueuedAt_ = org.revenj.json.JavaTimeConverter.deserializeDateTime(reader);
				nextToken = reader.getNextToken();
						break;
					case -730669991:
						_date_ = org.revenj.json.JavaTimeConverter.deserializeLocalDate(reader);
					nextToken = reader.getNextToken();
						break;
					case 467038368:
						_number_ = com.dslplatform.json.NumberConverter.deserializeDecimal(reader);
					nextToken = reader.getNextToken();
						break;
					case -1972918838:
						_bigint_ = com.dslplatform.json.NumberConverter.deserializeLong(reader);
					nextToken = reader.getNextToken();
						break;
					case -929786563:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							com.dslplatform.json.BoolConverter.deserializeCollection(reader, _bool_);
						}
						nextToken = reader.getNextToken();
					}
					else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case 1092248970:
						
					if (nextToken == '"') {
						switch(reader.calcHash()) {
							case -1005848884: _en_ = gen.model.test.En.A; break;
							case -955516027: _en_ = gen.model.test.En.B; break;
							default:
								throw new java.io.IOException("Unknown enum value: '" + reader.getLastName() + "' at position " + reader.positionInStream());
						}
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '\"' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
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
			this.ProcessedAt = _ProcessedAt_;
			this.QueuedAt = _QueuedAt_;
		this.date = _date_;
		this.number = _number_;
		this.bigint = _bigint_;
		this.bool = _bool_;
		this.en = _en_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.test.Clicked(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	@com.fasterxml.jackson.annotation.JsonCreator private Clicked(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JsonProperty("ProcessedAt") final java.time.OffsetDateTime ProcessedAt,
			@com.fasterxml.jackson.annotation.JsonProperty("QueuedAt") final java.time.OffsetDateTime QueuedAt,
			@com.fasterxml.jackson.annotation.JsonProperty("date") final java.time.LocalDate date,
			@com.fasterxml.jackson.annotation.JsonProperty("number") final java.math.BigDecimal number,
			@com.fasterxml.jackson.annotation.JsonProperty("bigint") final Long bigint,
			@com.fasterxml.jackson.annotation.JsonProperty("bool") final java.util.Set<Boolean> bool,
			@com.fasterxml.jackson.annotation.JsonProperty("en") final gen.model.test.En en) {
		this.URI = URI != null ? URI : "new " + new java.util.UUID(0L, 0L).toString();
		this.ProcessedAt = ProcessedAt == null ? null : ProcessedAt;
		this.QueuedAt = QueuedAt == null ? null : QueuedAt;
		this.date = date;
		this.number = number == null ? java.math.BigDecimal.ZERO : number;
		this.bigint = bigint;
		this.bool = bool == null ? new java.util.LinkedHashSet<Boolean>(4) : bool;
		this.en = en;
	}

	
	public Clicked(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Clicked>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<Clicked> rdr : readers) {
			rdr.read(this, reader, context);
		}
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Clicked>[] readers, int __index____event_id, int __index___QueuedAt, int __index___ProcessedAt, int __index___date, int __index___number, int __index___bigint, int __index___bool, int __index___en) {
		
		readers[__index____event_id] = (item, reader, context) -> { item.URI = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
		readers[__index___QueuedAt] = (item, reader, context) -> { item.QueuedAt = org.revenj.postgres.converters.TimestampConverter.parseOffset(reader, context, false, true); return item; };
		readers[__index___ProcessedAt] = (item, reader, context) -> { item.ProcessedAt = org.revenj.postgres.converters.TimestampConverter.parseOffset(reader, context, true, true); return item; };
		readers[__index___date] = (item, reader, context) -> { item.date = org.revenj.postgres.converters.DateConverter.parse(reader, true); return item; };
		readers[__index___number] = (item, reader, context) -> { item.number = org.revenj.postgres.converters.DecimalConverter.parse(reader, false); return item; };
		readers[__index___bigint] = (item, reader, context) -> { item.bigint = org.revenj.postgres.converters.LongConverter.parseNullable(reader); return item; };
		readers[__index___bool] = (item, reader, context) -> { { java.util.List<Boolean> __list = org.revenj.postgres.converters.BoolConverter.parseCollection(reader, context, false); if(__list != null) {item.bool = new java.util.LinkedHashSet<Boolean>(__list);} else item.bool = new java.util.LinkedHashSet<Boolean>(4); }; return item; };
		readers[__index___en] = (item, reader, context) -> { item.en = gen.model.test.converters.EnConverter.fromReader(reader); return item; };
	}
	
	static {
		gen.model.test.repositories.ClickedRepository.__configure(
			events -> {
				java.time.OffsetDateTime now = java.time.OffsetDateTime.now();
				for (gen.model.test.Clicked eve : events) {
					eve.URI = null;
					eve.QueuedAt = now;eve.ProcessedAt = now;
				}
			},
			(events, uris) -> {
				int _i = 0;
				for (gen.model.test.Clicked eve : events) {
					eve.URI = uris[_i++];
				}
			}
		);
	}
}
