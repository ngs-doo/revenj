/*
* Created by DSL Platform
* v1.0.0.190 
*/

package gen.model.test;



public class CompositeCube  extends org.revenj.postgres.PostgresOlapCubeQuery<gen.model.test.CompositeList> {
	
	public final static String number = "number";
	public final static String enn = "enn";
	public final static String hasEntities = "hasEntities";
	public final static String simple = "simple";
	public final static String indexes = "indexes";
	public final static String max = "max";
	public final static String min = "min";
	public final static String count = "count";
	public final static String hasSum = "hasSum";
	public final static String avgInd = "avgInd";
	

public static class FilterMax   implements java.io.Serializable, org.revenj.patterns.Specification<gen.model.test.CompositeList>, com.dslplatform.json.JsonObject {
	
	
	
	public FilterMax(
			 final java.time.LocalDate value) {
			
		setValue(value);
	}

	
	
	public FilterMax() {
			
		this.value = java.time.LocalDate.now();
	}

	private static final long serialVersionUID = -1244956969352411466L;
	
	private java.time.LocalDate value;

	
	@com.fasterxml.jackson.annotation.JsonProperty("value")
	public java.time.LocalDate getValue()  {
		
		return value;
	}

	
	public FilterMax setValue(final java.time.LocalDate value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"value\" cannot be null!");
		this.value = value;
		
		return this;
	}

	
		public boolean test(gen.model.test.CompositeList it) {
			return it.getChange().compareTo(this.getValue()) >= 0;
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

	static void __serializeJsonObjectMinimal(final FilterMax self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			if (!(self.value.getYear() == 1 && self.value.getMonthValue() == 1 && self.value.getDayOfMonth() == 1)) {
			hasWrittenProperty = true;
				sw.writeAscii("\"value\":", 8);
				com.dslplatform.json.JavaTimeConverter.serialize(self.value, sw);
			}
	}

	static void __serializeJsonObjectFull(final FilterMax self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			
			sw.writeAscii("\"value\":", 8);
			com.dslplatform.json.JavaTimeConverter.serialize(self.value, sw);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<FilterMax> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<FilterMax>() {
		@Override
		public FilterMax deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.test.CompositeCube.FilterMax(reader);
		}
	};

	private FilterMax(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		java.time.LocalDate _value_ = org.revenj.Utils.MIN_LOCAL_DATE;
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
					
					case 1113510858:
						_value_ = com.dslplatform.json.JavaTimeConverter.deserializeLocalDate(reader);
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
					
					case 1113510858:
						_value_ = com.dslplatform.json.JavaTimeConverter.deserializeLocalDate(reader);
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
		
		this.value = _value_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.test.CompositeCube.FilterMax(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
}

	

public static class _Resultnumbersimplecount_   implements com.dslplatform.json.JsonObject {
	
	
	
	public _Resultnumbersimplecount_() {
			
	}

	
	private int number;

	
	public int getNumber()  {
		
		return number;
	}

	
	public _Resultnumbersimplecount_ setNumber(final int value) {
		
		this.number = value;
		
		return this;
	}

	
	private gen.model.test.Simple simple;

	
	public gen.model.test.Simple getSimple()  {
		
		return simple;
	}

	
	public _Resultnumbersimplecount_ setSimple(final gen.model.test.Simple value) {
		
		this.simple = value;
		
		return this;
	}

	
	private gen.model.test.En[] count;

	
	public gen.model.test.En[] getCount()  {
		
		return count;
	}

	
	public _Resultnumbersimplecount_ setCount(final gen.model.test.En[] value) {
		
		this.count = value;
		
		return this;
	}

	
	public static _Resultnumbersimplecount_[] fromMap(java.util.List<java.util.Map<String, Object>> list) {
		_Resultnumbersimplecount_[] result = new _Resultnumbersimplecount_[list.size()];
		for (int i = 0; i < list.size(); i++) {
			java.util.Map<String, Object> map = list.get(i);
			_Resultnumbersimplecount_ instance = new _Resultnumbersimplecount_();
			instance.setNumber((int)map.get("number"));
			instance.setSimple((gen.model.test.Simple)map.get("simple"));
			instance.setCount((gen.model.test.En[])map.get("count"));
			result[i] = instance;
		}
		return result;
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

	static void __serializeJsonObjectMinimal(final _Resultnumbersimplecount_ self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
	}

	static void __serializeJsonObjectFull(final _Resultnumbersimplecount_ self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<_Resultnumbersimplecount_> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<_Resultnumbersimplecount_>() {
		@Override
		public _Resultnumbersimplecount_ deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.test.CompositeCube._Resultnumbersimplecount_(reader);
		}
	};

	private _Resultnumbersimplecount_(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
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
					
					default:
						nextToken = reader.skip();
						break;
				}
			}
			if (nextToken != '}') {
				throw new java.io.IOException("Expecting '}' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
			}
		}
		
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.test.CompositeCube._Resultnumbersimplecount_(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
}

	
	
	public CompositeCube(
			org.revenj.patterns.ServiceLocator locator) {
		super(locator);
			final int context = 1;	
		
			cubeDimensions.put("number", n -> "\"" + n + "\".\"number\"");
			cubeConverters.put("number", reader -> org.revenj.postgres.converters.IntConverter.parse(reader));
		
			cubeDimensions.put("enn", n -> "\"" + n + "\".\"enn\"");
			cubeConverters.put("enn", rdr -> { gen.model.test.En[] __arg = null; java.util.List<gen.model.test.En> __list = org.revenj.postgres.converters.EnumConverter.parseCollection(rdr, 1, gen.model.test.En.A, gen.model.test.converters.EnConverter::convertEnum); if (__list != null) {__arg = __list.toArray(new gen.model.test.En[__list.size()]);} else __arg = new gen.model.test.En[] { }; return __arg; });
		
			cubeDimensions.put("hasEntities", n -> "\"" + n + "\".\"hasEntities\"");
			cubeConverters.put("hasEntities", reader -> org.revenj.postgres.converters.BoolConverter.parse(reader));
		
			cubeDimensions.put("simple", n -> "\"" + n + "\".\"simple\"");
			cubeConverters.put("simple", rdr -> locator.resolve(gen.model.test.converters.SimpleConverter.class).from(rdr, 1));
		
			cubeDimensions.put("indexes", n -> "\"" + n + "\".\"indexes\"");
			cubeConverters.put("indexes", reader -> { Long[] __arg = null; { java.util.List<Long> __list = org.revenj.postgres.converters.LongConverter.parseCollection(reader, context, true); if(__list != null) {__arg = __list.toArray(new Long[__list.size()]);} } return __arg; });
		
			cubeFacts.put("max", n -> "MAX(\"" + n + "\".\"change\")");
			cubeConverters.put("max", reader -> org.revenj.postgres.converters.DateConverter.parse(reader, true));
		
			cubeFacts.put("min", n -> "MIN(\"" + n + "\".\"change\")");
			cubeConverters.put("min", reader -> org.revenj.postgres.converters.DateConverter.parse(reader, true));
		
			cubeFacts.put("count", n -> "COUNT(\"" + n + "\".\"enn\")");
			cubeConverters.put("count", org.revenj.postgres.converters.LongConverter::parse);
		
			cubeFacts.put("hasSum", n -> "SUM(\"" + n + "\".\"hasEntities\"::int)");
			cubeConverters.put("hasSum", org.revenj.postgres.converters.IntConverter::parse);
		
			cubeFacts.put("avgInd", n -> "AVG((SELECT AVG(x) FROM UNNEST(\"" + n + "\".\"indexes\") x))");
			cubeConverters.put("avgInd", rdr -> org.revenj.postgres.converters.DecimalConverter.parse(rdr, true));
	}

	
	@Override
	protected org.revenj.patterns.Specification<gen.model.test.CompositeList> rewriteSpecification(org.revenj.patterns.Specification<gen.model.test.CompositeList> specification) {
		return gen.model.test.repositories.CompositeListRepository.rewriteSpecificationToLambda(specification);
	}

	@Override
	protected String getSource() { return "\"test\".\"CompositeList\""; }
}
