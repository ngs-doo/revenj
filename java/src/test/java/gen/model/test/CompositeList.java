/*
* Created by DSL Platform
* v1.0.0.32432 
*/

package gen.model.test;



public final class CompositeList   implements java.io.Serializable, com.dslplatform.json.JsonObject, org.revenj.patterns.Identifiable {
	
	
	@com.fasterxml.jackson.annotation.JsonCreator 
	public CompositeList(
			@com.fasterxml.jackson.annotation.JsonProperty("URI")  final String URI,
			@com.fasterxml.jackson.annotation.JsonProperty("id")  final java.util.UUID id,
			@com.fasterxml.jackson.annotation.JsonProperty("enn")  final gen.model.test.En[] enn,
			@com.fasterxml.jackson.annotation.JsonProperty("en")  final gen.model.test.En en,
			@com.fasterxml.jackson.annotation.JsonProperty("tsl")  final java.util.List<java.time.OffsetDateTime> tsl,
			@com.fasterxml.jackson.annotation.JsonProperty("change")  final java.time.LocalDate change,
			@com.fasterxml.jackson.annotation.JsonProperty("entities")  final java.util.List<gen.model.test.Entity> entities,
			@com.fasterxml.jackson.annotation.JsonProperty("simple")  final gen.model.test.Simple simple,
			@com.fasterxml.jackson.annotation.JsonProperty("number")  final int number,
			@com.fasterxml.jackson.annotation.JsonProperty("entitiesCount")  final int entitiesCount,
			@com.fasterxml.jackson.annotation.JsonProperty("hasEntities")  final boolean hasEntities,
			@com.fasterxml.jackson.annotation.JsonProperty("entityHasMoney")  final boolean[] entityHasMoney,
			@com.fasterxml.jackson.annotation.JsonProperty("indexes")  final long[] indexes) {
			
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.id = id != null ? id : org.revenj.Utils.MIN_UUID;
		this.enn = enn != null ? enn : new gen.model.test.En[] { };
		org.revenj.Guards.checkNulls(enn);
		this.en = en != null ? en : gen.model.test.En.A;
		this.tsl = tsl != null ? tsl : new java.util.ArrayList<java.time.OffsetDateTime>(4);
		org.revenj.Guards.checkNulls(tsl);
		this.change = change != null ? change : org.revenj.Utils.MIN_LOCAL_DATE;
		this.entities = entities != null ? entities : new java.util.ArrayList<gen.model.test.Entity>(4);
		org.revenj.Guards.checkNulls(entities);
		this.simple = simple != null ? simple : new gen.model.test.Simple();
		this.number = number;
		this.entitiesCount = entitiesCount;
		this.hasEntities = hasEntities;
		this.entityHasMoney = entityHasMoney != null ? entityHasMoney : new boolean[] { };
		this.indexes = indexes;
	}

	
	
	public CompositeList() {
			
		this.URI = java.util.UUID.randomUUID().toString();
		this.id = java.util.UUID.randomUUID();
		this.enn = new gen.model.test.En[] { };
		this.en = gen.model.test.En.A;
		this.tsl = new java.util.ArrayList<java.time.OffsetDateTime>(4);
		this.change = java.time.LocalDate.now();
		this.entities = new java.util.ArrayList<gen.model.test.Entity>(4);
		this.simple = new gen.model.test.Simple();
		this.number = 0;
		this.entitiesCount = 0;
		this.hasEntities = false;
		this.entityHasMoney = new boolean[] { };
		this.indexes = null;
	}

	
	private final String URI;

	
	@com.fasterxml.jackson.annotation.JsonProperty("URI")
	public String getURI()  {
		
		return this.URI;
	}

	
	@Override
	public int hashCode() {
		return URI.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;
		final CompositeList other = (CompositeList) obj;

		return URI.equals(other.URI);
	}

	@Override
	public String toString() {
		return "CompositeList(" + URI + ')';
	}
	private static final long serialVersionUID = -5241368694566029105L;
	
	private final java.util.UUID id;

	
	@com.fasterxml.jackson.annotation.JsonProperty("id")
	public java.util.UUID getId()  {
		
		return this.id;
	}

	private static final gen.model.test.En[] _defaultenn = new gen.model.test.En[] { };
	
	private final gen.model.test.En[] enn;

	
	@com.fasterxml.jackson.annotation.JsonProperty("enn")
	public gen.model.test.En[] getEnn()  {
		
		return this.enn;
	}

	
	private final gen.model.test.En en;

	
	@com.fasterxml.jackson.annotation.JsonProperty("en")
	public gen.model.test.En getEn()  {
		
		return this.en;
	}

	
	private final java.util.List<java.time.OffsetDateTime> tsl;

	
	@com.fasterxml.jackson.annotation.JsonProperty("tsl")
	public java.util.List<java.time.OffsetDateTime> getTsl()  {
		
		return this.tsl;
	}

	
	private final java.time.LocalDate change;

	
	@com.fasterxml.jackson.annotation.JsonProperty("change")
	public java.time.LocalDate getChange()  {
		
		return this.change;
	}

	
	private final java.util.List<gen.model.test.Entity> entities;

	
	@com.fasterxml.jackson.annotation.JsonProperty("entities")
	public java.util.List<gen.model.test.Entity> getEntities()  {
		
		return this.entities;
	}

	
	private final gen.model.test.Simple simple;

	
	@com.fasterxml.jackson.annotation.JsonProperty("simple")
	public gen.model.test.Simple getSimple()  {
		
		return this.simple;
	}

	
	private final int number;

	
	@com.fasterxml.jackson.annotation.JsonProperty("number")
	public int getNumber()  {
		
		return this.number;
	}

	
	private final int entitiesCount;

	
	@com.fasterxml.jackson.annotation.JsonProperty("entitiesCount")
	public int getEntitiesCount()  {
		
		return this.entitiesCount;
	}

	
	private final boolean hasEntities;

	
	@com.fasterxml.jackson.annotation.JsonProperty("hasEntities")
	public boolean getHasEntities()  {
		
		return this.hasEntities;
	}

	private static final boolean[] _defaultentityHasMoney = new boolean[] { };
	
	private final boolean[] entityHasMoney;

	
	@com.fasterxml.jackson.annotation.JsonProperty("entityHasMoney")
	public boolean[] getEntityHasMoney()  {
		
		return this.entityHasMoney;
	}

	
	private final long[] indexes;

	
	@com.fasterxml.jackson.annotation.JsonProperty("indexes")
	public long[] getIndexes()  {
		
		return this.indexes;
	}

	
	private java.util.UUID id2;

	
	public java.util.UUID getId2()  {
		
		this.id2 = __calculated_id2.apply(this);
		return this.id2;
	}

	private static final java.util.function.Function<gen.model.test.CompositeList, java.util.UUID> __calculated_id2 = it -> it.getId();
	

public static class ForSimple   implements java.io.Serializable, org.revenj.patterns.Specification<CompositeList>, com.dslplatform.json.JsonObject {
	
	
	
	public ForSimple(
			 final java.util.List<gen.model.test.Simple> simples) {
			
		setSimples(simples);
	}

	
	
	public ForSimple() {
			
		this.simples = new java.util.ArrayList<gen.model.test.Simple>(4);
	}

	private static final long serialVersionUID = 685986846977240128L;
	
	private java.util.List<gen.model.test.Simple> simples;

	
	@com.fasterxml.jackson.annotation.JsonProperty("simples")
	public java.util.List<gen.model.test.Simple> getSimples()  {
		
		return simples;
	}

	
	public ForSimple setSimples(final java.util.List<gen.model.test.Simple> value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"simples\" cannot be null!");
		org.revenj.Guards.checkNulls(value);
		this.simples = value;
		
		return this;
	}

	
		public boolean test(gen.model.test.CompositeList it) {
			return (this.getSimples().contains(it.getSimple()));
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

	static void __serializeJsonObjectMinimal(final ForSimple self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
		if(self.simples.size() != 0) {
			hasWrittenProperty = true;
			sw.writeAscii("\"simples\":[", 11);
			gen.model.test.Simple item = self.simples.get(0);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.test.Simple.__serializeJsonObjectMinimal(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			for(int i = 1; i < self.simples.size(); i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
				item = self.simples.get(i);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.test.Simple.__serializeJsonObjectMinimal(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
	}

	static void __serializeJsonObjectFull(final ForSimple self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
		if(self.simples.size() != 0) {
			sw.writeAscii("\"simples\":[", 11);
			gen.model.test.Simple item = self.simples.get(0);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.test.Simple.__serializeJsonObjectFull(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			for(int i = 1; i < self.simples.size(); i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
				item = self.simples.get(i);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.test.Simple.__serializeJsonObjectFull(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else sw.writeAscii("\"simples\":[]", 12);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<ForSimple> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<ForSimple>() {
		@Override
		public ForSimple deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.test.CompositeList.ForSimple(reader);
		}
	};

	private ForSimple(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		java.util.List<gen.model.test.Simple> _simples_ = new java.util.ArrayList<gen.model.test.Simple>(4);
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
					
					case 1331401444:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							reader.deserializeCollection(gen.model.test.Simple.JSON_READER, _simples_);
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
					
					case 1331401444:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							reader.deserializeCollection(gen.model.test.Simple.JSON_READER, _simples_);
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
		
		this.simples = _simples_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.test.CompositeList.ForSimple(reader);
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

	static void __serializeJsonObjectMinimal(final CompositeList self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
		com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (!(self.id.getMostSignificantBits() == 0 && self.id.getLeastSignificantBits() == 0)) {
				sw.writeAscii(",\"id\":", 6);
				com.dslplatform.json.UUIDConverter.serialize(self.id, sw);
			}
		
		if(self.enn.length != 0) {
			sw.writeAscii(",\"enn\":[", 8);
			gen.model.test.En item = self.enn[0];
				sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
				sw.writeAscii(item.name());
				sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
			for(int i = 1; i < self.enn.length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
				item = self.enn[i];
				sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
				sw.writeAscii(item.name());
				sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		
		if(self.en != gen.model.test.En.A) {
			sw.writeAscii(",\"en\":\"B\"", 9);
		}
		
		if(self.tsl.size() != 0) {
			sw.writeAscii(",\"tsl\":[", 8);
			com.dslplatform.json.JavaTimeConverter.serializeNullable(self.tsl.get(0), sw);
			for(int i = 1; i < self.tsl.size(); i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
				com.dslplatform.json.JavaTimeConverter.serializeNullable(self.tsl.get(i), sw);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		
			if (!(self.change.getYear() == 1 && self.change.getMonthValue() == 1 && self.change.getDayOfMonth() == 1)) {
				sw.writeAscii(",\"change\":", 10);
				com.dslplatform.json.JavaTimeConverter.serialize(self.change, sw);
			}
		
		if(self.entities.size() != 0) {
			sw.writeAscii(",\"entities\":[", 13);
			gen.model.test.Entity item = self.entities.get(0);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.test.Entity.__serializeJsonObjectMinimal(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			for(int i = 1; i < self.entities.size(); i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
				item = self.entities.get(i);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.test.Entity.__serializeJsonObjectMinimal(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		
		
		sw.writeAscii(",\"simple\":{", 11);
		
					gen.model.test.Simple.__serializeJsonObjectMinimal(self.simple, sw, false);
					sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
		
			if (self.number != 0) {
				sw.writeAscii(",\"number\":", 10);
				com.dslplatform.json.NumberConverter.serialize(self.number, sw);
			}
		
			if (self.entitiesCount != 0) {
				sw.writeAscii(",\"entitiesCount\":", 17);
				com.dslplatform.json.NumberConverter.serialize(self.entitiesCount, sw);
			}
		
			if (self.hasEntities) {
				sw.writeAscii(",\"hasEntities\":true");
			}
		
		if(self.entityHasMoney.length != 0) {
			sw.writeAscii(",\"entityHasMoney\":[", 19);
			com.dslplatform.json.BoolConverter.serialize(self.entityHasMoney[0], sw);
			for(int i = 1; i < self.entityHasMoney.length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
				com.dslplatform.json.BoolConverter.serialize(self.entityHasMoney[i], sw);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		
		if(self.indexes != null && self.indexes.length != 0) {
			sw.writeAscii(",\"indexes\":[", 12);
			com.dslplatform.json.NumberConverter.serialize(self.indexes[0], sw);
			for(int i = 1; i < self.indexes.length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
				com.dslplatform.json.NumberConverter.serialize(self.indexes[i], sw);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else if(self.indexes != null) sw.writeAscii(",\"indexes\":[]", 13);
		
			if (!(self.getId2().getMostSignificantBits() == 0 && self.getId2().getLeastSignificantBits() == 0)) {
				sw.writeAscii(",\"id2\":", 7);
				com.dslplatform.json.UUIDConverter.serialize(self.getId2(), sw);
			}
	}

	static void __serializeJsonObjectFull(final CompositeList self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
		com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			sw.writeAscii(",\"id\":", 6);
			com.dslplatform.json.UUIDConverter.serialize(self.id, sw);
		
		if(self.enn.length != 0) {
			sw.writeAscii(",\"enn\":[", 8);
			gen.model.test.En item = self.enn[0];
				sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
				sw.writeAscii(item.name());
				sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
			for(int i = 1; i < self.enn.length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
				item = self.enn[i];
				sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
				sw.writeAscii(item.name());
				sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else sw.writeAscii(",\"enn\":[]", 9);
		
		
		sw.writeAscii(",\"en\":\"", 7);
		sw.writeAscii(self.en.name());
		sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
		
		if(self.tsl.size() != 0) {
			sw.writeAscii(",\"tsl\":[", 8);
			com.dslplatform.json.JavaTimeConverter.serializeNullable(self.tsl.get(0), sw);
			for(int i = 1; i < self.tsl.size(); i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
				com.dslplatform.json.JavaTimeConverter.serializeNullable(self.tsl.get(i), sw);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else sw.writeAscii(",\"tsl\":[]", 9);
		
			
			sw.writeAscii(",\"change\":", 10);
			com.dslplatform.json.JavaTimeConverter.serialize(self.change, sw);
		
		if(self.entities.size() != 0) {
			sw.writeAscii(",\"entities\":[", 13);
			gen.model.test.Entity item = self.entities.get(0);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.test.Entity.__serializeJsonObjectFull(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			for(int i = 1; i < self.entities.size(); i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
				item = self.entities.get(i);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.test.Entity.__serializeJsonObjectFull(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else sw.writeAscii(",\"entities\":[]", 14);
		
		
		sw.writeAscii(",\"simple\":{", 11);
		
					gen.model.test.Simple.__serializeJsonObjectFull(self.simple, sw, false);
					sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
		
			
			sw.writeAscii(",\"number\":", 10);
			com.dslplatform.json.NumberConverter.serialize(self.number, sw);
		
			
			sw.writeAscii(",\"entitiesCount\":", 17);
			com.dslplatform.json.NumberConverter.serialize(self.entitiesCount, sw);
		
			if (self.hasEntities) {
				sw.writeAscii(",\"hasEntities\":true");
			} else {
				sw.writeAscii(",\"hasEntities\":false");
			}
		
		if(self.entityHasMoney.length != 0) {
			sw.writeAscii(",\"entityHasMoney\":[", 19);
			com.dslplatform.json.BoolConverter.serialize(self.entityHasMoney[0], sw);
			for(int i = 1; i < self.entityHasMoney.length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
				com.dslplatform.json.BoolConverter.serialize(self.entityHasMoney[i], sw);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else sw.writeAscii(",\"entityHasMoney\":[]", 20);
		
		if(self.indexes != null && self.indexes.length != 0) {
			sw.writeAscii(",\"indexes\":[", 12);
			com.dslplatform.json.NumberConverter.serialize(self.indexes[0], sw);
			for(int i = 1; i < self.indexes.length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
				com.dslplatform.json.NumberConverter.serialize(self.indexes[i], sw);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else if(self.indexes != null) sw.writeAscii(",\"indexes\":[]", 13);
		else sw.writeAscii(",\"indexes\":null", 15);
		
			
			sw.writeAscii(",\"id2\":", 7);
			com.dslplatform.json.UUIDConverter.serialize(self.getId2(), sw);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<CompositeList> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<CompositeList>() {
		@Override
		public CompositeList deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.test.CompositeList(reader);
		}
	};

	private CompositeList(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		java.util.UUID _id_ = org.revenj.Utils.MIN_UUID;
		gen.model.test.En[] _enn_ = _defaultenn;
		gen.model.test.En _en_ = gen.model.test.En.A;
		java.util.List<java.time.OffsetDateTime> _tsl_ = new java.util.ArrayList<java.time.OffsetDateTime>(4);
		java.time.LocalDate _change_ = org.revenj.Utils.MIN_LOCAL_DATE;
		java.util.List<gen.model.test.Entity> _entities_ = new java.util.ArrayList<gen.model.test.Entity>(4);
		gen.model.test.Simple _simple_ = null;
		int _number_ = 0;
		int _entitiesCount_ = 0;
		boolean _hasEntities_ = false;
		boolean[] _entityHasMoney_ = _defaultentityHasMoney;
		long[] _indexes_ = null;
		java.util.UUID _id2_ = org.revenj.Utils.MIN_UUID;
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
					case 926444256:
						_id_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case 1619944940:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken == ']') {
							_enn_ = new gen.model.test.En[] { };
						} else {
							java.util.ArrayList<gen.model.test.En> __res = new java.util.ArrayList<gen.model.test.En>(4);
							gen.model.test.En __inst;
							String __val;
							
					if (nextToken == '"') {
						switch(reader.calcHash()) {
							case -1005848884: __inst = gen.model.test.En.A; break;
							case -955516027: __inst = gen.model.test.En.B; break;
							default:
								throw new java.io.IOException("Unknown enum value: '" + reader.getLastName() + "' at position " + reader.positionInStream());
						}
						__res.add(__inst);
					} else throw new java.io.IOException("Expecting '\"' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
							while ((nextToken = reader.getNextToken()) == ',') {
								nextToken = reader.getNextToken();
								
					if (nextToken == '"') {
						switch(reader.calcHash()) {
							case -1005848884: __inst = gen.model.test.En.A; break;
							case -955516027: __inst = gen.model.test.En.B; break;
							default:
								throw new java.io.IOException("Unknown enum value: '" + reader.getLastName() + "' at position " + reader.positionInStream());
						}
						__res.add(__inst);
					} else throw new java.io.IOException("Expecting '\"' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
							}
							if (nextToken != ']') throw new java.io.IOException("Expecting ']' at position " + reader.positionInStream() + ". Found " + (char) nextToken);
							_enn_ = __res.toArray(new gen.model.test.En[__res.size()]);
						}
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
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
					case -1155926508:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							com.dslplatform.json.JavaTimeConverter.deserializeDateTimeNullableCollection(reader, _tsl_);
						}
						nextToken = reader.getNextToken();
					}
					else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case 1922892221:
						_change_ = com.dslplatform.json.JavaTimeConverter.deserializeLocalDate(reader);
					nextToken = reader.getNextToken();
						break;
					case -922096406:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							reader.deserializeCollection(gen.model.test.Entity.JSON_READER, _entities_);
						}
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case 375816319:
						
					if (nextToken == '{') {
						reader.getNextToken();
						_simple_ = gen.model.test.Simple.JSON_READER.deserialize(reader);
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '{' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case 467038368:
						_number_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case -39305343:
						_entitiesCount_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case -1925893080:
						_hasEntities_ = com.dslplatform.json.BoolConverter.deserialize(reader); nextToken = reader.getNextToken();
						break;
					case -1029283846:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							java.util.ArrayList<Boolean> __res = com.dslplatform.json.BoolConverter.deserializeCollection(reader);
							boolean[] __resUnboxed = new boolean[__res.size()];
							for(int _i=0;_i<__res.size();_i++) 
								__resUnboxed[_i] = __res.get(_i);
							_entityHasMoney_ = __resUnboxed;
						}
						nextToken = reader.getNextToken();
					}
					else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case -1595427141:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							java.util.ArrayList<Long> __res = com.dslplatform.json.NumberConverter.deserializeLongCollection(reader);
							long[] __resUnboxed = new long[__res.size()];
							for(int _i=0;_i<__res.size();_i++) 
								__resUnboxed[_i] = __res.get(_i);
							_indexes_ = __resUnboxed;
						}
						nextToken = reader.getNextToken();
					}
					else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case -1076877162:
						_id2_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
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
					case 926444256:
						_id_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case 1619944940:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken == ']') {
							_enn_ = new gen.model.test.En[] { };
						} else {
							java.util.ArrayList<gen.model.test.En> __res = new java.util.ArrayList<gen.model.test.En>(4);
							gen.model.test.En __inst;
							String __val;
							
					if (nextToken == '"') {
						switch(reader.calcHash()) {
							case -1005848884: __inst = gen.model.test.En.A; break;
							case -955516027: __inst = gen.model.test.En.B; break;
							default:
								throw new java.io.IOException("Unknown enum value: '" + reader.getLastName() + "' at position " + reader.positionInStream());
						}
						__res.add(__inst);
					} else throw new java.io.IOException("Expecting '\"' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
							while ((nextToken = reader.getNextToken()) == ',') {
								nextToken = reader.getNextToken();
								
					if (nextToken == '"') {
						switch(reader.calcHash()) {
							case -1005848884: __inst = gen.model.test.En.A; break;
							case -955516027: __inst = gen.model.test.En.B; break;
							default:
								throw new java.io.IOException("Unknown enum value: '" + reader.getLastName() + "' at position " + reader.positionInStream());
						}
						__res.add(__inst);
					} else throw new java.io.IOException("Expecting '\"' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
							}
							if (nextToken != ']') throw new java.io.IOException("Expecting ']' at position " + reader.positionInStream() + ". Found " + (char) nextToken);
							_enn_ = __res.toArray(new gen.model.test.En[__res.size()]);
						}
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
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
					case -1155926508:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							com.dslplatform.json.JavaTimeConverter.deserializeDateTimeNullableCollection(reader, _tsl_);
						}
						nextToken = reader.getNextToken();
					}
					else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case 1922892221:
						_change_ = com.dslplatform.json.JavaTimeConverter.deserializeLocalDate(reader);
					nextToken = reader.getNextToken();
						break;
					case -922096406:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							reader.deserializeCollection(gen.model.test.Entity.JSON_READER, _entities_);
						}
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case 375816319:
						
					if (nextToken == '{') {
						reader.getNextToken();
						_simple_ = gen.model.test.Simple.JSON_READER.deserialize(reader);
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '{' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case 467038368:
						_number_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case -39305343:
						_entitiesCount_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case -1925893080:
						_hasEntities_ = com.dslplatform.json.BoolConverter.deserialize(reader); nextToken = reader.getNextToken();
						break;
					case -1029283846:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							java.util.ArrayList<Boolean> __res = com.dslplatform.json.BoolConverter.deserializeCollection(reader);
							boolean[] __resUnboxed = new boolean[__res.size()];
							for(int _i=0;_i<__res.size();_i++) 
								__resUnboxed[_i] = __res.get(_i);
							_entityHasMoney_ = __resUnboxed;
						}
						nextToken = reader.getNextToken();
					}
					else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case -1595427141:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							java.util.ArrayList<Long> __res = com.dslplatform.json.NumberConverter.deserializeLongCollection(reader);
							long[] __resUnboxed = new long[__res.size()];
							for(int _i=0;_i<__res.size();_i++) 
								__resUnboxed[_i] = __res.get(_i);
							_indexes_ = __resUnboxed;
						}
						nextToken = reader.getNextToken();
					}
					else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case -1076877162:
						_id2_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
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
		this.id = _id_;
		this.enn = _enn_;
		this.en = _en_;
		this.tsl = _tsl_;
		this.change = _change_;
		this.entities = _entities_;
		this.simple = _simple_;
		this.number = _number_;
		this.entitiesCount = _entitiesCount_;
		this.hasEntities = _hasEntities_;
		this.entityHasMoney = _entityHasMoney_;
		this.indexes = _indexes_;
		this.id2 = _id2_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.test.CompositeList(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
}
