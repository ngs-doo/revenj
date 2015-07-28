package gen.model.test;



public class Composite   implements java.io.Serializable, org.revenj.patterns.AggregateRoot {
	
	
	
	public Composite() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.id = java.util.UUID.randomUUID();
		this.enn = new gen.model.test.En[] { };
		this.simple = new gen.model.test.Simple();
		this.entities = new java.util.ArrayList<gen.model.test.Entity>(4);
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
		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;
		final Composite other = (Composite) obj;

		return URI.equals(other.URI);
	}

	public boolean equals(final Composite other) {
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
		if(!(this.simple == other.simple || this.simple != null && this.simple.equals(other.simple)))
			return false;
		if(!((this.entities == other.entities || this.entities != null && this.entities.equals(other.entities))))
			return false;

		return true;
	}

	@Override
	public String toString() {
		return "Composite(" + URI + ')';
	}
	
	
	public Composite(
			final java.util.UUID id,
			final gen.model.test.En[] enn,
			final gen.model.test.Simple simple,
			final java.util.List<gen.model.test.Entity> entities) {
			
		setId(id);
		setEnn(enn);
		setSimple(simple);
		setEntities(entities);
	}

	
	private static final long serialVersionUID = 0x0097000a;
	
	@com.fasterxml.jackson.annotation.JsonCreator private Composite(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("id") final java.util.UUID id,
			@com.fasterxml.jackson.annotation.JsonProperty("enn") final gen.model.test.En[] enn,
			@com.fasterxml.jackson.annotation.JsonProperty("simple") final gen.model.test.Simple simple,
			@com.fasterxml.jackson.annotation.JsonProperty("entities") final java.util.List<gen.model.test.Entity> entities) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.id = id == null ? new java.util.UUID(0L, 0L) : id;
		this.enn = enn == null ? new gen.model.test.En[] { } : enn;
		this.simple = simple == null ? new gen.model.test.Simple() : simple;
		this.entities = entities == null ? new java.util.ArrayList<gen.model.test.Entity>(4) : entities;
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

	

public static class ForSimple   implements java.io.Serializable, org.revenj.patterns.Specification<Composite> {
	
	
	
	public ForSimple(
			 final gen.model.test.Simple simple) {
			
		setSimple(simple);
	}

	
	
	public ForSimple() {
			
		this.simple = new gen.model.test.Simple();
	}

	private static final long serialVersionUID = 0x0097000a;
	
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

}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator;
	
	public Composite(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Composite>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<Composite> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.test.converters.CompositeConverter.buildURI(reader.tmp, id);
		this.__locator = java.util.Optional.ofNullable(reader.locator);
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Composite>[] readers, int __index___id, int __index___enn, gen.model.test.converters.SimpleConverter __converter_simple, int __index___simple, gen.model.test.converters.EntityConverter __converter_entities, int __index___entities) {
		
		readers[__index___id] = (item, reader, context) -> { item.id = org.revenj.postgres.converters.UuidConverter.parse(reader, false); };
		readers[__index___enn] = (item, reader, context) -> { { java.util.List<gen.model.test.En> __list = org.revenj.postgres.converters.EnumConverter.parseCollection(reader, context, gen.model.test.En.A, gen.model.test.converters.EnConverter::convertEnum); if (__list != null) item.enn = __list.toArray(new gen.model.test.En[__list.size()]); else item.enn = new gen.model.test.En[] { }; }; };
		readers[__index___simple] = (item, reader, context) -> { item.simple = __converter_simple.from(reader, context); };
		readers[__index___entities] = (item, reader, context) -> { { java.util.List<gen.model.test.Entity> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, (rdr, ctx) -> __converter_entities.from(rdr, ctx)); if (__list != null) item.entities = __list; else item.entities = new java.util.ArrayList<gen.model.test.Entity>(4); }; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Composite>[] readers, int __index__extended_id, int __index__extended_enn, final gen.model.test.converters.SimpleConverter __converter_simple, int __index__extended_simple, final gen.model.test.converters.EntityConverter __converter_entities, int __index__extended_entities) {
		
		readers[__index__extended_id] = (item, reader, context) -> { item.id = org.revenj.postgres.converters.UuidConverter.parse(reader, false); };
		readers[__index__extended_enn] = (item, reader, context) -> { { java.util.List<gen.model.test.En> __list = org.revenj.postgres.converters.EnumConverter.parseCollection(reader, context, gen.model.test.En.A, gen.model.test.converters.EnConverter::convertEnum); if (__list != null) item.enn = __list.toArray(new gen.model.test.En[__list.size()]); else item.enn = new gen.model.test.En[] { }; }; };
		readers[__index__extended_simple] = (item, reader, context) -> { item.simple = __converter_simple.fromExtended(reader, context); };
		readers[__index__extended_entities] = (item, reader, context) -> { { java.util.List<gen.model.test.Entity> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, (rdr, ctx) -> __converter_entities.fromExtended(rdr, ctx)); if (__list != null) item.entities = __list; else item.entities = new java.util.ArrayList<gen.model.test.Entity>(4); }; };
	}
}
