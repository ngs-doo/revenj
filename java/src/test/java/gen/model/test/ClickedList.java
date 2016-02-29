/*
* Created by DSL Platform
* v1.0.0.15576 
*/

package gen.model.test;



public final class ClickedList   implements java.io.Serializable, org.revenj.patterns.DataSource, com.dslplatform.json.JsonObject {
	
	
	@com.fasterxml.jackson.annotation.JsonCreator 
	public ClickedList(
			@com.fasterxml.jackson.annotation.JsonProperty("date")  final java.time.LocalDate date,
			@com.fasterxml.jackson.annotation.JsonProperty("number")  final java.math.BigDecimal number) {
			
		this.date = date;
		this.number = number;
	}

	private static final long serialVersionUID = 4098161761673684978L;
	
	private final java.time.LocalDate date;

	
	@com.fasterxml.jackson.annotation.JsonProperty("date")
	public java.time.LocalDate getDate()  {
		
		return this.date;
	}

	
	private final java.math.BigDecimal number;

	
	@com.fasterxml.jackson.annotation.JsonProperty("number")
	public java.math.BigDecimal getNumber()  {
		
		return this.number;
	}

	

public static class FindAt   implements java.io.Serializable, org.revenj.patterns.Specification<ClickedList>, com.dslplatform.json.JsonObject {
	
	
	
	public FindAt(
			 final java.time.LocalDate date) {
			
		setDate(date);
	}

	
	
	public FindAt() {
			
		this.date = java.time.LocalDate.now();
	}

	private static final long serialVersionUID = -8350995197025769892L;
	
	private java.time.LocalDate date;

	
	@com.fasterxml.jackson.annotation.JsonProperty("date")
	public java.time.LocalDate getDate()  {
		
		return date;
	}

	
	public FindAt setDate(final java.time.LocalDate value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"date\" cannot be null!");
		this.date = value;
		
		return this;
	}

	
		public boolean test(gen.model.test.ClickedList it) {
			return it.getDate().equals(this.getDate());
		}
	
		public org.revenj.patterns.Specification<ClickedList> rewriteLambda() {
			java.time.LocalDate _date_ = this.getDate();
			return it -> it.getDate().equals(_date_);
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

	static void __serializeJsonObjectMinimal(final FindAt self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			if (!(self.date.getYear() == 1 && self.date.getMonthValue() == 1 && self.date.getDayOfMonth() == 1)) {
			hasWrittenProperty = true;
				sw.writeAscii("\"date\":", 7);
				com.dslplatform.json.JavaTimeConverter.serialize(self.date, sw);
			}
	}

	static void __serializeJsonObjectFull(final FindAt self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			
			sw.writeAscii("\"date\":", 7);
			com.dslplatform.json.JavaTimeConverter.serialize(self.date, sw);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<FindAt> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<FindAt>() {
		@Override
		public FindAt deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.test.ClickedList.FindAt(reader);
		}
	};

	private FindAt(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		java.time.LocalDate _date_ = org.revenj.Utils.MIN_LOCAL_DATE;
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
					
					case -730669991:
						_date_ = com.dslplatform.json.JavaTimeConverter.deserializeLocalDate(reader);
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
					
					case -730669991:
						_date_ = com.dslplatform.json.JavaTimeConverter.deserializeLocalDate(reader);
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
		
		this.date = _date_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.test.ClickedList.FindAt(reader);
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

	static void __serializeJsonObjectMinimal(final ClickedList self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			if (self.date != null) {
			hasWrittenProperty = true;
				sw.writeAscii("\"date\":", 7);
				com.dslplatform.json.JavaTimeConverter.serialize(self.date, sw);
			}
		
			if (self.number != null) {
			if(hasWrittenProperty) sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
			hasWrittenProperty = true;
				sw.writeAscii("\"number\":", 9);
				com.dslplatform.json.NumberConverter.serialize(self.number, sw);
			}
	}

	static void __serializeJsonObjectFull(final ClickedList self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			
			if (self.date != null) {
				sw.writeAscii("\"date\":", 7);
				com.dslplatform.json.JavaTimeConverter.serialize(self.date, sw);
			} else {
				sw.writeAscii("\"date\":null", 11);
			}
		
			
			if (self.number != null) {
				sw.writeAscii(",\"number\":", 10);
				com.dslplatform.json.NumberConverter.serialize(self.number, sw);
			} else {
				sw.writeAscii(",\"number\":null", 14);
			}
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<ClickedList> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<ClickedList>() {
		@Override
		public ClickedList deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.test.ClickedList(reader);
		}
	};

	private ClickedList(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		java.time.LocalDate _date_ = null;
		java.math.BigDecimal _number_ = null;
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
					
					case -730669991:
						_date_ = com.dslplatform.json.JavaTimeConverter.deserializeLocalDate(reader);
					nextToken = reader.getNextToken();
						break;
					case 467038368:
						_number_ = com.dslplatform.json.NumberConverter.deserializeDecimal(reader);
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
					
					case -730669991:
						_date_ = com.dslplatform.json.JavaTimeConverter.deserializeLocalDate(reader);
					nextToken = reader.getNextToken();
						break;
					case 467038368:
						_number_ = com.dslplatform.json.NumberConverter.deserializeDecimal(reader);
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
		
		this.date = _date_;
		this.number = _number_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.test.ClickedList(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
}
