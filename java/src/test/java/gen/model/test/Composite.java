/*
* Created by DSL Platform
* v1.0.0.15576 
*/

package gen.model.test;



public class Composite   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot, com.dslplatform.json.JsonObject {
	
	
	
	public Composite() {
			
		this.id = java.util.UUID.randomUUID();
		this.enn = new gen.model.test.En[] { };
		this.en = gen.model.test.En.A;
		this.simple = new gen.model.test.Simple();
		this.change = java.time.LocalDate.now();
		this.tsl = new java.util.ArrayList<java.time.OffsetDateTime>(4);
		this.entities = new java.util.ArrayList<gen.model.test.Entity>(4);
		this.lazies = new gen.model.test.LazyLoad[] { };
		this.URI = this.id.toString();
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
		if (obj == null || obj instanceof Composite == false)
			return false;
		final Composite other = (Composite) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final Composite other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.id.equals(other.id)))
			return false;
		if(!(java.util.Arrays.equals(this.enn, other.enn)))
			return false;
		if(!(this.en == other.en || this.en != null && this.en.equals(other.en)))
			return false;
		if(!(this.simple == other.simple || this.simple != null && this.simple.equals(other.simple)))
			return false;
		if(!(this.change.equals(other.change)))
			return false;
		if(!((this.tsl == other.tsl || this.tsl != null && this.tsl.equals(other.tsl))))
			return false;
		if(!((this.entities == other.entities || this.entities != null && this.entities.equals(other.entities))))
			return false;
		if(!(java.util.Arrays.equals(this.laziesURI, other.laziesURI)))
			return false;
		if(!(java.util.Arrays.equals(this.indexes, other.indexes)))
			return false;
		return true;
	}

	private Composite(Composite other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.id = other.id;
		this.enn = java.util.Arrays.copyOf(other.enn, other.enn.length);
		this.en = other.en;
		this.simple = other.simple == null ? null : (gen.model.test.Simple)(other.simple.clone());
		this.change = other.change;
		this.tsl = new java.util.ArrayList<java.time.OffsetDateTime>(other.tsl);
		this.entities = new java.util.ArrayList<gen.model.test.Entity>(other.entities.size());
			if (other.entities != null) {
				for (gen.model.test.Entity it : other.entities) {
					this.entities.add((gen.model.test.Entity)it.clone());
				}
			};
		this.laziesURI = other.getLaziesURI();
		this.indexes = other.indexes == null ? null : java.util.Arrays.copyOf(other.indexes, other.indexes.length);
		this.__originalValue = other.__originalValue;
	}

	@Override
	public Object clone() {
		return new Composite(this);
	}

	@Override
	public String toString() {
		return "Composite(" + URI + ')';
	}
	
	
	public Composite(
			final java.util.UUID id,
			final gen.model.test.En[] enn,
			final gen.model.test.En en,
			final gen.model.test.Simple simple,
			final java.util.List<java.time.OffsetDateTime> tsl,
			final java.util.List<gen.model.test.Entity> entities,
			final long[] indexes) {
			
		setId(id);
		setEnn(enn);
		setEn(en);
		setSimple(simple);
		setTsl(tsl);
		setEntities(entities);
		setIndexes(indexes);
		this.URI = this.id.toString();
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	
	@com.fasterxml.jackson.annotation.JsonCreator private Composite(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("id") final java.util.UUID id,
			@com.fasterxml.jackson.annotation.JsonProperty("enn") final gen.model.test.En[] enn,
			@com.fasterxml.jackson.annotation.JsonProperty("en") final gen.model.test.En en,
			@com.fasterxml.jackson.annotation.JsonProperty("simple") final gen.model.test.Simple simple,
			@com.fasterxml.jackson.annotation.JsonProperty("change") final java.time.LocalDate change,
			@com.fasterxml.jackson.annotation.JsonProperty("tsl") final java.util.List<java.time.OffsetDateTime> tsl,
			@com.fasterxml.jackson.annotation.JsonProperty("entities") final java.util.List<gen.model.test.Entity> entities,
			@com.fasterxml.jackson.annotation.JsonProperty("laziesURI") final String[] laziesURI,
			@com.fasterxml.jackson.annotation.JsonProperty("entitiesCount") final int entitiesCount,
			@com.fasterxml.jackson.annotation.JsonProperty("entityHasMoney") final boolean[] entityHasMoney,
			@com.fasterxml.jackson.annotation.JsonProperty("indexes") final long[] indexes,
			@com.fasterxml.jackson.annotation.JsonProperty("hasEntities") final boolean hasEntities) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.id = id == null ? org.revenj.Utils.MIN_UUID : id;
		this.enn = enn == null ? new gen.model.test.En[] { } : enn;
		this.en = en == null ? gen.model.test.En.A : en;
		this.simple = simple == null ? new gen.model.test.Simple() : simple;
		this.change = change == null ? org.revenj.Utils.MIN_LOCAL_DATE : change;
		this.tsl = tsl == null ? new java.util.ArrayList<java.time.OffsetDateTime>(4) : tsl;
		this.entities = entities == null ? new java.util.ArrayList<gen.model.test.Entity>(4) : entities;
		this.laziesURI = laziesURI == null ? new String[0] : laziesURI;
		this.entitiesCount = entitiesCount;
		this.entityHasMoney = entityHasMoney == null ? new boolean[] { } : entityHasMoney;
		this.indexes = indexes;
		this.hasEntities = hasEntities;
	}

	private static final long serialVersionUID = 5330807536990535264L;
	
	private java.util.UUID id;

	
	@com.fasterxml.jackson.annotation.JsonProperty("id")
	public java.util.UUID getId()  {
		
		return id;
	}

	
	public Composite setId(final java.util.UUID value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"id\" cannot be null!");
		this.id = value;
		
		return this;
	}

	private static final gen.model.test.En[] _defaultenn = new gen.model.test.En[] { };
	
	private gen.model.test.En[] enn;

	
	@com.fasterxml.jackson.annotation.JsonProperty("enn")
	public gen.model.test.En[] getEnn()  {
		
		return enn;
	}

	
	public Composite setEnn(final gen.model.test.En[] value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"enn\" cannot be null!");
		org.revenj.Guards.checkNulls(value);
		this.enn = value;
		
		return this;
	}

	
	private gen.model.test.En en;

	
	@com.fasterxml.jackson.annotation.JsonProperty("en")
	public gen.model.test.En getEn()  {
		
		return en;
	}

	
	public Composite setEn(final gen.model.test.En value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"en\" cannot be null!");
		this.en = value;
		
		return this;
	}

	
	private gen.model.test.Simple simple;

	
	@com.fasterxml.jackson.annotation.JsonProperty("simple")
	public gen.model.test.Simple getSimple()  {
		
		return simple;
	}

	
	public Composite setSimple(final gen.model.test.Simple value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"simple\" cannot be null!");
		this.simple = value;
		
		return this;
	}

	
	private java.time.LocalDate change;

	
	@com.fasterxml.jackson.annotation.JsonProperty("change")
	public java.time.LocalDate getChange()  {
		
		return change;
	}

	
	private Composite setChange(final java.time.LocalDate value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"change\" cannot be null!");
		this.change = value;
		
		return this;
	}

	
	private java.util.List<java.time.OffsetDateTime> tsl;

	
	@com.fasterxml.jackson.annotation.JsonProperty("tsl")
	public java.util.List<java.time.OffsetDateTime> getTsl()  {
		
		return tsl;
	}

	
	public Composite setTsl(final java.util.List<java.time.OffsetDateTime> value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"tsl\" cannot be null!");
		org.revenj.Guards.checkNulls(value);
		this.tsl = value;
		
		return this;
	}

	
	static void __bindToentities(java.util.function.BiConsumer<gen.model.test.Composite, java.util.Map.Entry<org.revenj.postgres.PostgresWriter, org.revenj.patterns.ServiceLocator>> binder) {
		__binderentities = binder;
	}

	private static java.util.function.BiConsumer<gen.model.test.Composite, java.util.Map.Entry<org.revenj.postgres.PostgresWriter, org.revenj.patterns.ServiceLocator>> __binderentities;
	
	private java.util.List<gen.model.test.Entity> entities;

	
	@com.fasterxml.jackson.annotation.JsonProperty("entities")
	public java.util.List<gen.model.test.Entity> getEntities()  {
		
		return entities;
	}

	
	public Composite setEntities(final java.util.List<gen.model.test.Entity> value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"entities\" cannot be null!");
		org.revenj.Guards.checkNulls(value);
		this.entities = value;
		
		return this;
	}

	private static final String[] _defaultlaziesURI = new String[0];
	
	private gen.model.test.LazyLoad[] lazies;

	
	@com.fasterxml.jackson.annotation.JsonIgnore
	public gen.model.test.LazyLoad[] getLazies()  {
		
		
		if(this.laziesURI != null && this.laziesURI.length == 0)
		{
			this.lazies = new gen.model.test.LazyLoad[] { };
			this.laziesURI = null;
		}
		
		if (this.__locator.isPresent() && (this.laziesURI != null && (this.lazies == null || this.lazies.length != this.laziesURI.length))) {
			gen.model.test.repositories.LazyLoadRepository repository = this.__locator.get().resolve(gen.model.test.repositories.LazyLoadRepository.class);
			java.util.List<gen.model.test.LazyLoad> __list = repository.find(this.laziesURI);
			lazies = __list.toArray(new gen.model.test.LazyLoad[__list.size()]);
			this.laziesURI = null;
		}
		return this.lazies;
	}

	
	private String[] laziesURI;

	
	@com.fasterxml.jackson.annotation.JsonProperty("laziesURI")
	public String[] getLaziesURI()  {
		
		
			if (this.lazies != null) {
				final String[] _result = new String[this.lazies.length];
				int _i = 0;
				for (final gen.model.test.LazyLoad _it : this.lazies) {
					_result[_i++] = _it.getURI();
				}
				return _result;
			} 
			if (this.laziesURI == null) return new String[0];
		return this.laziesURI;
	}

	
	private int entitiesCount;

	
	@com.fasterxml.jackson.annotation.JsonProperty("entitiesCount")
	public int getEntitiesCount()  {
		
		this.entitiesCount = __calculated_entitiesCount.apply(this);
		return this.entitiesCount;
	}

	private static final java.util.function.Function<gen.model.test.Composite, Integer> __calculated_entitiesCount = it -> (it.getEntities().size());
	private static final boolean[] _defaultentityHasMoney = new boolean[] { };
	
	private boolean[] entityHasMoney;

	
	@com.fasterxml.jackson.annotation.JsonProperty("entityHasMoney")
	public boolean[] getEntityHasMoney()  {
		
		this.entityHasMoney = __calculated_entityHasMoney.apply(this);
		return this.entityHasMoney;
	}

	private static final java.util.function.Function<gen.model.test.Composite, boolean[]> __calculated_entityHasMoney = it -> org.revenj.Guards.toBooleanArray((it.getEntities().stream().map(e ->  e.getMoney().compareTo(java.math.BigDecimal.valueOf(0)) > 0)).collect(java.util.stream.Collectors.toList()));
	
	private long[] indexes;

	
	@com.fasterxml.jackson.annotation.JsonProperty("indexes")
	public long[] getIndexes()  {
		
		return indexes;
	}

	
	public Composite setIndexes(final long[] value) {
		
		this.indexes = value;
		
		return this;
	}

	
	private boolean hasEntities;

	
	@com.fasterxml.jackson.annotation.JsonProperty("hasEntities")
	public boolean getHasEntities()  {
		
		this.hasEntities = __calculated_hasEntities.apply(this);
		return this.hasEntities;
	}

	private static final java.util.function.Function<gen.model.test.Composite, Boolean> __calculated_hasEntities = it -> ((it.getEntities().size()) > 0);
	

public static class ForSimple   implements java.io.Serializable, org.revenj.patterns.Specification<Composite>, com.dslplatform.json.JsonObject {
	
	
	
	public ForSimple(
			 final gen.model.test.Simple simple) {
			
		setSimple(simple);
	}

	
	
	public ForSimple() {
			
		this.simple = new gen.model.test.Simple();
	}

	private static final long serialVersionUID = -7251827779934751500L;
	
	private gen.model.test.Simple simple;

	
	@com.fasterxml.jackson.annotation.JsonProperty("simple")
	public gen.model.test.Simple getSimple()  {
		
		return simple;
	}

	
	public ForSimple setSimple(final gen.model.test.Simple value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"simple\" cannot be null!");
		this.simple = value;
		
		return this;
	}

	
		public boolean test(gen.model.test.Composite it) {
			return (it.getSimple().getNumber() == this.getSimple().getNumber());
		}
	
		public org.revenj.patterns.Specification<Composite> rewriteLambda() {
			gen.model.test.Simple _simple_ = this.getSimple();
			return it -> (it.getSimple().getNumber() == _simple_.getNumber());
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
		
		
		
		sw.writeAscii("\"simple\":{", 10);
		
					gen.model.test.Simple.__serializeJsonObjectMinimal(self.simple, sw, false);
					sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
	}

	static void __serializeJsonObjectFull(final ForSimple self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
		
		sw.writeAscii("\"simple\":{", 10);
		
					gen.model.test.Simple.__serializeJsonObjectFull(self.simple, sw, false);
					sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<ForSimple> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<ForSimple>() {
		@Override
		public ForSimple deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.test.Composite.ForSimple(reader);
		}
	};

	private ForSimple(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		gen.model.test.Simple _simple_ = null;
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
					
					case 375816319:
						
					if (nextToken == '{') {
						reader.getNextToken();
						_simple_ = gen.model.test.Simple.JSON_READER.deserialize(reader);
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
					
					case 375816319:
						
					if (nextToken == '{') {
						reader.getNextToken();
						_simple_ = gen.model.test.Simple.JSON_READER.deserialize(reader);
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
		
		this.simple = _simple_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.test.Composite.ForSimple(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
}

	private transient Composite __originalValue;
	
	static {
		gen.model.test.repositories.CompositeRepository.__setupPersist(
			(aggregates, arg) -> {
				try {
					for (gen.model.test.Composite agg : aggregates) {
						
						agg.change = java.time.LocalDate.now(java.time.ZoneOffset.UTC);
						__binderentities.accept(agg, arg); 
						agg.URI = gen.model.test.converters.CompositeConverter.buildURI(arg.getKey(), agg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(aggregates, arg) -> {
				try {
					java.util.List<gen.model.test.Composite> oldAggregates = aggregates.getKey();
					java.util.List<gen.model.test.Composite> newAggregates = aggregates.getValue();
					for (int i = 0; i < newAggregates.size(); i++) {
						gen.model.test.Composite oldAgg = oldAggregates.get(i);
						gen.model.test.Composite newAgg = newAggregates.get(i);
						
					newAgg.change = java.time.LocalDate.now(java.time.ZoneOffset.UTC);
					__binderentities.accept(newAgg, arg); 
						newAgg.URI = gen.model.test.converters.CompositeConverter.buildURI(arg.getKey(), newAgg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			aggregates -> { 
				for (gen.model.test.Composite agg : aggregates) { 
				}
			},
			agg -> { 
				
		Composite _res = agg.__originalValue;
		agg.__originalValue = (Composite)agg.clone();
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

	static void __serializeJsonObjectMinimal(final Composite self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (!(self.id.getMostSignificantBits() == 0 && self.id.getLeastSignificantBits() == 0)) {
				sw.writeAscii(",\"id\":", 6);
				com.dslplatform.json.UUIDConverter.serialize(self.id, sw);
			}
		
		final gen.model.test.En[] _tmp_enn_ = self.enn;
		if(_tmp_enn_.length != 0) {
			sw.writeAscii(",\"enn\":[", 8);
			gen.model.test.En item = _tmp_enn_[0];
				sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
				sw.writeAscii(item.name());
				sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
			for(int i = 1; i < _tmp_enn_.length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
				item = _tmp_enn_[i];
				sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
				sw.writeAscii(item.name());
				sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		
		if(self.en != gen.model.test.En.A) {
			sw.writeAscii(",\"en\":\"B\"", 9);
		}
		
		
		sw.writeAscii(",\"simple\":{", 11);
		
					gen.model.test.Simple.__serializeJsonObjectMinimal(self.simple, sw, false);
					sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
		
			if (!(self.change.getYear() == 1 && self.change.getMonthValue() == 1 && self.change.getDayOfMonth() == 1)) {
				sw.writeAscii(",\"change\":", 10);
				com.dslplatform.json.JavaTimeConverter.serialize(self.change, sw);
			}
		
		final java.util.List<java.time.OffsetDateTime> _tmp_tsl_ = self.tsl;
		if(_tmp_tsl_.size() != 0) {
			sw.writeAscii(",\"tsl\":[", 8);
			com.dslplatform.json.JavaTimeConverter.serialize(_tmp_tsl_.get(0), sw);
			for(int i = 1; i < _tmp_tsl_.size(); i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
				com.dslplatform.json.JavaTimeConverter.serialize(_tmp_tsl_.get(i), sw);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		
		final java.util.List<gen.model.test.Entity> _tmp_entities_ = self.entities;
		if(_tmp_entities_.size() != 0) {
			sw.writeAscii(",\"entities\":[", 13);
			gen.model.test.Entity item = _tmp_entities_.get(0);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.test.Entity.__serializeJsonObjectMinimal(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			for(int i = 1; i < _tmp_entities_.size(); i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
				item = _tmp_entities_.get(i);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.test.Entity.__serializeJsonObjectMinimal(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		
			final String[] laziesURI = self.getLaziesURI();
			if(self.laziesURI != null && laziesURI.length != 0) {
				sw.writeAscii(",\"laziesURI\":[");
				com.dslplatform.json.StringConverter.serializeShort(laziesURI[0], sw);
				for(int i = 1; i < laziesURI.length; i++) {
					sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
					com.dslplatform.json.StringConverter.serializeShort(laziesURI[i], sw);
				}
				sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
			}
		
			if (self.getEntitiesCount() != 0) {
				sw.writeAscii(",\"entitiesCount\":", 17);
				com.dslplatform.json.NumberConverter.serialize(self.getEntitiesCount(), sw);
			}
		
		final boolean[] _tmp_entityHasMoney_ = self.getEntityHasMoney();
		if(_tmp_entityHasMoney_.length != 0) {
			sw.writeAscii(",\"entityHasMoney\":[", 19);
			com.dslplatform.json.BoolConverter.serialize(_tmp_entityHasMoney_[0], sw);
			for(int i = 1; i < _tmp_entityHasMoney_.length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
				com.dslplatform.json.BoolConverter.serialize(_tmp_entityHasMoney_[i], sw);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		
		final long[] _tmp_indexes_ = self.indexes;
		if(_tmp_indexes_ != null && _tmp_indexes_.length != 0) {
			sw.writeAscii(",\"indexes\":[", 12);
			com.dslplatform.json.NumberConverter.serialize(_tmp_indexes_[0], sw);
			for(int i = 1; i < _tmp_indexes_.length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
				com.dslplatform.json.NumberConverter.serialize(_tmp_indexes_[i], sw);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else if(self.indexes != null) sw.writeAscii(",\"indexes\":[]", 13);
		
			if (self.getHasEntities()) {
				sw.writeAscii(",\"hasEntities\":true");
			}
	}

	static void __serializeJsonObjectFull(final Composite self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			sw.writeAscii(",\"id\":", 6);
			com.dslplatform.json.UUIDConverter.serialize(self.id, sw);
		
		final gen.model.test.En[] _tmp_enn_ = self.enn;
		if(_tmp_enn_.length != 0) {
			sw.writeAscii(",\"enn\":[", 8);
			gen.model.test.En item = _tmp_enn_[0];
				sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
				sw.writeAscii(item.name());
				sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
			for(int i = 1; i < _tmp_enn_.length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
				item = _tmp_enn_[i];
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
		
		
		sw.writeAscii(",\"simple\":{", 11);
		
					gen.model.test.Simple.__serializeJsonObjectFull(self.simple, sw, false);
					sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
		
			
			sw.writeAscii(",\"change\":", 10);
			com.dslplatform.json.JavaTimeConverter.serialize(self.change, sw);
		
		final java.util.List<java.time.OffsetDateTime> _tmp_tsl_ = self.tsl;
		if(_tmp_tsl_.size() != 0) {
			sw.writeAscii(",\"tsl\":[", 8);
			com.dslplatform.json.JavaTimeConverter.serialize(_tmp_tsl_.get(0), sw);
			for(int i = 1; i < _tmp_tsl_.size(); i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
				com.dslplatform.json.JavaTimeConverter.serialize(_tmp_tsl_.get(i), sw);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else sw.writeAscii(",\"tsl\":[]", 9);
		
		final java.util.List<gen.model.test.Entity> _tmp_entities_ = self.entities;
		if(_tmp_entities_.size() != 0) {
			sw.writeAscii(",\"entities\":[", 13);
			gen.model.test.Entity item = _tmp_entities_.get(0);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.test.Entity.__serializeJsonObjectFull(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			for(int i = 1; i < _tmp_entities_.size(); i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
				item = _tmp_entities_.get(i);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.test.Entity.__serializeJsonObjectFull(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else sw.writeAscii(",\"entities\":[]", 14);
		
			final String[] laziesURI = self.getLaziesURI();
			if(laziesURI != null && laziesURI.length != 0) {
				sw.writeAscii(",\"laziesURI\":[");
				com.dslplatform.json.StringConverter.serializeShort(laziesURI[0], sw);
				for(int i = 1; i < laziesURI.length; i++) {
					sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
					com.dslplatform.json.StringConverter.serializeShort(laziesURI[i], sw);
				}
				sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
			} else if(laziesURI != null) {
				sw.writeAscii(",\"laziesURI\":[]");
			} else {
				sw.writeAscii(",\"laziesURI\":null");
			}
		
			
			sw.writeAscii(",\"entitiesCount\":", 17);
			com.dslplatform.json.NumberConverter.serialize(self.getEntitiesCount(), sw);
		
		final boolean[] _tmp_entityHasMoney_ = self.getEntityHasMoney();
		if(_tmp_entityHasMoney_.length != 0) {
			sw.writeAscii(",\"entityHasMoney\":[", 19);
			com.dslplatform.json.BoolConverter.serialize(_tmp_entityHasMoney_[0], sw);
			for(int i = 1; i < _tmp_entityHasMoney_.length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
				com.dslplatform.json.BoolConverter.serialize(_tmp_entityHasMoney_[i], sw);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else sw.writeAscii(",\"entityHasMoney\":[]", 20);
		
		final long[] _tmp_indexes_ = self.indexes;
		if(_tmp_indexes_ != null && _tmp_indexes_.length != 0) {
			sw.writeAscii(",\"indexes\":[", 12);
			com.dslplatform.json.NumberConverter.serialize(_tmp_indexes_[0], sw);
			for(int i = 1; i < _tmp_indexes_.length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
				com.dslplatform.json.NumberConverter.serialize(_tmp_indexes_[i], sw);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else if(self.indexes != null) sw.writeAscii(",\"indexes\":[]", 13);
		else sw.writeAscii(",\"indexes\":null", 15);
		
			
			if (self.getHasEntities()) {
				sw.writeAscii(",\"hasEntities\":true");
			} else {
				sw.writeAscii(",\"hasEntities\":false");
			}
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<Composite> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<Composite>() {
		@Override
		public Composite deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.test.Composite(reader);
		}
	};

	private Composite(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		java.util.UUID _id_ = org.revenj.Utils.MIN_UUID;
		gen.model.test.En[] _enn_ = _defaultenn;
		gen.model.test.En _en_ = gen.model.test.En.A;
		gen.model.test.Simple _simple_ = null;
		java.time.LocalDate _change_ = org.revenj.Utils.MIN_LOCAL_DATE;
		java.util.List<java.time.OffsetDateTime> _tsl_ = new java.util.ArrayList<java.time.OffsetDateTime>(4);
		java.util.List<gen.model.test.Entity> _entities_ = new java.util.ArrayList<gen.model.test.Entity>(4);
		String[] _laziesURI_ = _defaultlaziesURI;
		int _entitiesCount_ = 0;
		boolean[] _entityHasMoney_ = _defaultentityHasMoney;
		long[] _indexes_ = null;
		boolean _hasEntities_ = false;
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
					case 375816319:
						
					if (nextToken == '{') {
						reader.getNextToken();
						_simple_ = gen.model.test.Simple.JSON_READER.deserialize(reader);
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '{' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case 1922892221:
						_change_ = com.dslplatform.json.JavaTimeConverter.deserializeLocalDate(reader);
					nextToken = reader.getNextToken();
						break;
					case -1155926508:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							com.dslplatform.json.JavaTimeConverter.deserializeDateTimeCollection(reader, _tsl_);
						}
						nextToken = reader.getNextToken();
					}
					else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
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
					case -63442465:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken == ']') {
							_laziesURI_ = new String[0];
						} else {
							java.util.ArrayList<String> _tmplaziesURI_ = com.dslplatform.json.StringConverter.deserializeCollection(reader);
							_laziesURI_ = _tmplaziesURI_.toArray(new String[_tmplaziesURI_.size()]);
						}
						nextToken = reader.getNextToken();
					}
					else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case -39305343:
						_entitiesCount_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
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
					case -1925893080:
						_hasEntities_ = com.dslplatform.json.BoolConverter.deserialize(reader); nextToken = reader.getNextToken();
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
					case 375816319:
						
					if (nextToken == '{') {
						reader.getNextToken();
						_simple_ = gen.model.test.Simple.JSON_READER.deserialize(reader);
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '{' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case 1922892221:
						_change_ = com.dslplatform.json.JavaTimeConverter.deserializeLocalDate(reader);
					nextToken = reader.getNextToken();
						break;
					case -1155926508:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							com.dslplatform.json.JavaTimeConverter.deserializeDateTimeCollection(reader, _tsl_);
						}
						nextToken = reader.getNextToken();
					}
					else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
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
					case -63442465:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken == ']') {
							_laziesURI_ = new String[0];
						} else {
							java.util.ArrayList<String> _tmplaziesURI_ = com.dslplatform.json.StringConverter.deserializeCollection(reader);
							_laziesURI_ = _tmplaziesURI_.toArray(new String[_tmplaziesURI_.size()]);
						}
						nextToken = reader.getNextToken();
					}
					else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case -39305343:
						_entitiesCount_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
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
					case -1925893080:
						_hasEntities_ = com.dslplatform.json.BoolConverter.deserialize(reader); nextToken = reader.getNextToken();
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
		if(_simple_ == null) throw new java.io.IOException("In entity test.Composite, property simple can't be null");
		this.simple = _simple_;
		this.change = _change_;
		this.tsl = _tsl_;
		this.entities = _entities_;
		this.laziesURI = _laziesURI_;
		this.entitiesCount = _entitiesCount_;
		this.entityHasMoney = _entityHasMoney_;
		this.indexes = _indexes_;
		this.hasEntities = _hasEntities_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.test.Composite(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public Composite(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Composite>[] readers) throws java.io.IOException {
		this.__locator = reader.getLocator();
		for (org.revenj.postgres.ObjectConverter.Reader<Composite> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.test.converters.CompositeConverter.buildURI(reader, this);
		this.__originalValue = (Composite)this.clone();
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Composite>[] readers, int __index___id, int __index___enn, int __index___en, gen.model.test.converters.SimpleConverter __converter_simple, int __index___simple, int __index___change, int __index___tsl, gen.model.test.converters.EntityConverter __converter_entities, int __index___entities, int __index___laziesURI, int __index___indexes) {
		
		readers[__index___id] = (item, reader, context) -> { item.id = org.revenj.postgres.converters.UuidConverter.parse(reader, false); return item; };
		readers[__index___enn] = (item, reader, context) -> { { java.util.List<gen.model.test.En> __list = org.revenj.postgres.converters.EnumConverter.parseCollection(reader, context, gen.model.test.En.A, gen.model.test.converters.EnConverter::convertEnum); if (__list != null) {item.enn = __list.toArray(new gen.model.test.En[__list.size()]);} else item.enn = new gen.model.test.En[] { }; }; return item; };
		readers[__index___en] = (item, reader, context) -> { item.en = gen.model.test.converters.EnConverter.fromReader(reader); return item; };
		readers[__index___simple] = (item, reader, context) -> { item.simple = __converter_simple.from(reader, context); return item; };
		readers[__index___change] = (item, reader, context) -> { item.change = org.revenj.postgres.converters.DateConverter.parse(reader, false); return item; };
		readers[__index___tsl] = (item, reader, context) -> { { java.util.List<java.time.OffsetDateTime> __list = org.revenj.postgres.converters.TimestampConverter.parseOffsetCollection(reader, context, false, true); if(__list != null) {item.tsl = __list;} else item.tsl = new java.util.ArrayList<java.time.OffsetDateTime>(4); }; return item; };
		readers[__index___entities] = (item, reader, context) -> { { java.util.List<gen.model.test.Entity> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, __converter_entities::from); if (__list != null) {item.entities = __list;} else item.entities = new java.util.ArrayList<gen.model.test.Entity>(4); }; return item; };
		readers[__index___laziesURI] = (item, reader, context) -> { { 
			java.util.List<String> __list = org.revenj.postgres.converters.StringConverter.parseCollection(reader, context, true); 
			if (__list != null) item.laziesURI = __list.toArray(new String[__list.size()]); else item.laziesURI = new String[0]; 
		}; return item; };
		readers[__index___indexes] = (item, reader, context) -> { { java.util.List<Long> __list = org.revenj.postgres.converters.LongConverter.parseCollection(reader, context, false); if(__list != null) {
				long[] __resUnboxed = new long[__list.size()];
				for(int _i=0;_i<__list.size();_i++) {
					__resUnboxed[_i] = __list.get(_i);
				}
				item.indexes = __resUnboxed;
			} }; return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Composite>[] readers, int __index__extended_id, int __index__extended_enn, int __index__extended_en, final gen.model.test.converters.SimpleConverter __converter_simple, int __index__extended_simple, int __index__extended_change, int __index__extended_tsl, final gen.model.test.converters.EntityConverter __converter_entities, int __index__extended_entities, int __index__extended_laziesURI, int __index__extended_indexes) {
		
		readers[__index__extended_id] = (item, reader, context) -> { item.id = org.revenj.postgres.converters.UuidConverter.parse(reader, false); return item; };
		readers[__index__extended_enn] = (item, reader, context) -> { { java.util.List<gen.model.test.En> __list = org.revenj.postgres.converters.EnumConverter.parseCollection(reader, context, gen.model.test.En.A, gen.model.test.converters.EnConverter::convertEnum); if (__list != null) {item.enn = __list.toArray(new gen.model.test.En[__list.size()]);} else item.enn = new gen.model.test.En[] { }; }; return item; };
		readers[__index__extended_en] = (item, reader, context) -> { item.en = gen.model.test.converters.EnConverter.fromReader(reader); return item; };
		readers[__index__extended_simple] = (item, reader, context) -> { item.simple = __converter_simple.fromExtended(reader, context); return item; };
		readers[__index__extended_change] = (item, reader, context) -> { item.change = org.revenj.postgres.converters.DateConverter.parse(reader, false); return item; };
		readers[__index__extended_tsl] = (item, reader, context) -> { { java.util.List<java.time.OffsetDateTime> __list = org.revenj.postgres.converters.TimestampConverter.parseOffsetCollection(reader, context, false, true); if(__list != null) {item.tsl = __list;} else item.tsl = new java.util.ArrayList<java.time.OffsetDateTime>(4); }; return item; };
		readers[__index__extended_entities] = (item, reader, context) -> { { java.util.List<gen.model.test.Entity> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, __converter_entities::fromExtended); if (__list != null) {item.entities = __list;} else item.entities = new java.util.ArrayList<gen.model.test.Entity>(4); }; return item; };
		readers[__index__extended_laziesURI] = (item, reader, context) -> { { 
			java.util.List<String> __list = org.revenj.postgres.converters.StringConverter.parseCollection(reader, context, true); 
			if (__list != null) item.laziesURI = __list.toArray(new String[__list.size()]); else item.laziesURI = new String[0]; 
		}; return item; };
		readers[__index__extended_indexes] = (item, reader, context) -> { { java.util.List<Long> __list = org.revenj.postgres.converters.LongConverter.parseCollection(reader, context, false); if(__list != null) {
				long[] __resUnboxed = new long[__list.size()];
				for(int _i=0;_i<__list.size();_i++) {
					__resUnboxed[_i] = __list.get(_i);
				}
				item.indexes = __resUnboxed;
			} }; return item; };
	}
}
