package gen.model.test;



public class Composite   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot {
	
	
	
	public Composite() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.id = java.util.UUID.randomUUID();
		this.enn = new gen.model.test.En[] { };
		this.en = gen.model.test.En.A;
		this.simple = new gen.model.test.Simple();
		this.change = java.time.LocalDate.now();
		this.tsl = new java.util.ArrayList<java.time.OffsetDateTime>(4);
		this.entities = new java.util.ArrayList<gen.model.test.Entity>(4);
		this.lazies = new gen.model.test.LazyLoad[] { };
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
			final java.util.List<gen.model.test.Entity> entities) {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		setId(id);
		setEnn(enn);
		setEn(en);
		setSimple(simple);
		setTsl(tsl);
		setEntities(entities);
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	private static final long serialVersionUID = -4988792854303319639L;
	
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
			@com.fasterxml.jackson.annotation.JsonProperty("laziesURI") final String[] laziesURI) {
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
	}

	
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

	
	static void __bindToentities(java.util.function.Consumer<gen.model.test.Composite> binder) {
		__binderentities = binder;
	}

	private static java.util.function.Consumer<gen.model.test.Composite> __binderentities;
	
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

	

public static class ForSimple   implements java.io.Serializable, org.revenj.patterns.Specification<Composite> {
	
	
	
	public ForSimple(
			 final gen.model.test.Simple simple) {
			
		setSimple(simple);
	}

	
	
	public ForSimple() {
			
		this.simple = new gen.model.test.Simple();
	}

	private static final long serialVersionUID = 5984785891948638748L;
	
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
}

	private transient Composite __originalValue;
	
	static {
		gen.model.test.repositories.CompositeRepository.__setupPersist(
			(aggregates, arg) -> {
				try {
					for (gen.model.test.Composite agg : aggregates) {
						
						agg.change = java.time.LocalDate.now(java.time.ZoneOffset.UTC);
						__binderentities.accept(agg); 
						agg.URI = gen.model.test.converters.CompositeConverter.buildURI(arg, agg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(oldAggregates, newAggregates) -> {
				for (int i = 0; i < newAggregates.size(); i++) {
					gen.model.test.Composite oldAgg = oldAggregates.get(i);
					gen.model.test.Composite newAgg = newAggregates.get(i);
					
					newAgg.change = java.time.LocalDate.now(java.time.ZoneOffset.UTC);
					__binderentities.accept(newAgg); 
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
	
	public Composite(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Composite>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<Composite> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.test.converters.CompositeConverter.buildURI(reader, this);
		this.__locator = java.util.Optional.ofNullable(reader.locator);
		this.__originalValue = (Composite)this.clone();
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Composite>[] readers, int __index___id, int __index___enn, int __index___en, gen.model.test.converters.SimpleConverter __converter_simple, int __index___simple, int __index___change, int __index___tsl, gen.model.test.converters.EntityConverter __converter_entities, int __index___entities, int __index___laziesURI) {
		
		readers[__index___id] = (item, reader, context) -> { item.id = org.revenj.postgres.converters.UuidConverter.parse(reader, false); };
		readers[__index___enn] = (item, reader, context) -> { { java.util.List<gen.model.test.En> __list = org.revenj.postgres.converters.EnumConverter.parseCollection(reader, context, gen.model.test.En.A, gen.model.test.converters.EnConverter::convertEnum); if (__list != null) {item.enn = __list.toArray(new gen.model.test.En[__list.size()]);} else item.enn = new gen.model.test.En[] { }; }; };
		readers[__index___en] = (item, reader, context) -> { item.en = gen.model.test.converters.EnConverter.fromReader(reader); };
		readers[__index___simple] = (item, reader, context) -> { item.simple = __converter_simple.from(reader, context); };
		readers[__index___change] = (item, reader, context) -> { item.change = org.revenj.postgres.converters.DateConverter.parse(reader, false); };
		readers[__index___tsl] = (item, reader, context) -> { { java.util.List<java.time.OffsetDateTime> __list = org.revenj.postgres.converters.TimestampConverter.parseOffsetCollection(reader, context, false, true); if(__list != null) {item.tsl = __list;} else item.tsl = new java.util.ArrayList<java.time.OffsetDateTime>(4); }; };
		readers[__index___entities] = (item, reader, context) -> { { java.util.List<gen.model.test.Entity> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, __converter_entities::from); if (__list != null) {item.entities = __list;} else item.entities = new java.util.ArrayList<gen.model.test.Entity>(4); }; };
		readers[__index___laziesURI] = (item, reader, context) -> { { 
			java.util.List<String> __list = org.revenj.postgres.converters.StringConverter.parseCollection(reader, context, true); 
			if (__list != null) item.laziesURI = __list.toArray(new String[__list.size()]); else item.laziesURI = new String[0]; 
		}; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Composite>[] readers, int __index__extended_id, int __index__extended_enn, int __index__extended_en, final gen.model.test.converters.SimpleConverter __converter_simple, int __index__extended_simple, int __index__extended_change, int __index__extended_tsl, final gen.model.test.converters.EntityConverter __converter_entities, int __index__extended_entities, int __index__extended_laziesURI) {
		
		readers[__index__extended_id] = (item, reader, context) -> { item.id = org.revenj.postgres.converters.UuidConverter.parse(reader, false); };
		readers[__index__extended_enn] = (item, reader, context) -> { { java.util.List<gen.model.test.En> __list = org.revenj.postgres.converters.EnumConverter.parseCollection(reader, context, gen.model.test.En.A, gen.model.test.converters.EnConverter::convertEnum); if (__list != null) {item.enn = __list.toArray(new gen.model.test.En[__list.size()]);} else item.enn = new gen.model.test.En[] { }; }; };
		readers[__index__extended_en] = (item, reader, context) -> { item.en = gen.model.test.converters.EnConverter.fromReader(reader); };
		readers[__index__extended_simple] = (item, reader, context) -> { item.simple = __converter_simple.fromExtended(reader, context); };
		readers[__index__extended_change] = (item, reader, context) -> { item.change = org.revenj.postgres.converters.DateConverter.parse(reader, false); };
		readers[__index__extended_tsl] = (item, reader, context) -> { { java.util.List<java.time.OffsetDateTime> __list = org.revenj.postgres.converters.TimestampConverter.parseOffsetCollection(reader, context, false, true); if(__list != null) {item.tsl = __list;} else item.tsl = new java.util.ArrayList<java.time.OffsetDateTime>(4); }; };
		readers[__index__extended_entities] = (item, reader, context) -> { { java.util.List<gen.model.test.Entity> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, __converter_entities::fromExtended); if (__list != null) {item.entities = __list;} else item.entities = new java.util.ArrayList<gen.model.test.Entity>(4); }; };
		readers[__index__extended_laziesURI] = (item, reader, context) -> { { 
			java.util.List<String> __list = org.revenj.postgres.converters.StringConverter.parseCollection(reader, context, true); 
			if (__list != null) item.laziesURI = __list.toArray(new String[__list.size()]); else item.laziesURI = new String[0]; 
		}; };
	}
}
