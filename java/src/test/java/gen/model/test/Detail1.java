package gen.model.test;



public class Detail1   implements java.lang.Cloneable, java.io.Serializable {
	
	
	
	public Detail1() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.ff = 0.0f;
		this.EntityCompositeid = java.util.UUID.randomUUID();
		this.EntityIndex = 0;
		this.Index = 0;
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
		if (obj == null || obj instanceof Detail1 == false)
			return false;
		final Detail1 other = (Detail1) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final Detail1 other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.f == other.f || this.f != null && this.f.equals(other.f)))
			return false;
		if(!(Float.floatToIntBits(this.ff) == Float.floatToIntBits(other.ff)))
			return false;
		if(!(this.EntityCompositeid.equals(other.EntityCompositeid)))
			return false;
		if(!(this.EntityIndex == other.EntityIndex))
			return false;
		if(!(this.Index == other.Index))
			return false;
		return true;
	}

	private Detail1(Detail1 other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.f = other.f;
		this.ff = other.ff;
		this.EntityCompositeid = other.EntityCompositeid;
		this.EntityIndex = other.EntityIndex;
		this.Index = other.Index;
	}

	@Override
	public Object clone() {
		return new Detail1(this);
	}

	@Override
	public String toString() {
		return "Detail1(" + URI + ')';
	}
	
	
	public Detail1(
			final Float f,
			final float ff) {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		setF(f);
		setFf(ff);
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	private static final long serialVersionUID = -738259099534030581L;
	
	@com.fasterxml.jackson.annotation.JsonCreator private Detail1(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("f") final Float f,
			@com.fasterxml.jackson.annotation.JsonProperty("ff") final float ff,
			@com.fasterxml.jackson.annotation.JsonProperty("EntityCompositeid") final java.util.UUID EntityCompositeid,
			@com.fasterxml.jackson.annotation.JsonProperty("EntityIndex") final int EntityIndex,
			@com.fasterxml.jackson.annotation.JsonProperty("Index") final int Index) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.f = f;
		this.ff = ff;
		this.EntityCompositeid = EntityCompositeid == null ? new java.util.UUID(0L, 0L) : EntityCompositeid;
		this.EntityIndex = EntityIndex;
		this.Index = Index;
	}

	
	private Float f;

	
	@com.fasterxml.jackson.annotation.JsonProperty("f")
	public Float getF()  {
		
		return f;
	}

	
	public Detail1 setF(final Float value) {
		
		this.f = value;
		
		return this;
	}

	
	private float ff;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ff")
	public float getFf()  {
		
		return ff;
	}

	
	public Detail1 setFf(final float value) {
		
		this.ff = value;
		
		return this;
	}

	
	private java.util.UUID EntityCompositeid;

	
	@com.fasterxml.jackson.annotation.JsonProperty("EntityCompositeid")
	public java.util.UUID getEntityCompositeid()  {
		
		return EntityCompositeid;
	}

	
	private Detail1 setEntityCompositeid(final java.util.UUID value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"EntityCompositeid\" cannot be null!");
		this.EntityCompositeid = value;
		
		return this;
	}

	
	private int EntityIndex;

	
	@com.fasterxml.jackson.annotation.JsonProperty("EntityIndex")
	public int getEntityIndex()  {
		
		return EntityIndex;
	}

	
	private Detail1 setEntityIndex(final int value) {
		
		this.EntityIndex = value;
		
		return this;
	}

	
	private int Index;

	
	@com.fasterxml.jackson.annotation.JsonProperty("Index")
	public int getIndex()  {
		
		return Index;
	}

	
	private Detail1 setIndex(final int value) {
		
		this.Index = value;
		
		return this;
	}

	
	static {
		gen.model.test.Entity.__bindTodetail1(parent -> {
			int i = 0;
			for (gen.model.test.Detail1 e : parent.getDetail1()) { 
				e.EntityCompositeid = parent.getCompositeid();
				e.EntityIndex = parent.getIndex();
				e.Index = i++; 
			}
		});
	}
	
	public Detail1(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Detail1>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<Detail1> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.test.converters.Detail1Converter.buildURI(reader, EntityCompositeid, EntityIndex, Index);
		this.__locator = java.util.Optional.ofNullable(reader.locator);
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Detail1>[] readers, int __index___f, int __index___ff, int __index___EntityCompositeid, int __index___EntityIndex, int __index___Index) {
		
		readers[__index___f] = (item, reader, context) -> { item.f = org.revenj.postgres.converters.FloatConverter.parseNullable(reader); };
		readers[__index___ff] = (item, reader, context) -> { item.ff = org.revenj.postgres.converters.FloatConverter.parse(reader); };
		readers[__index___EntityCompositeid] = (item, reader, context) -> { item.EntityCompositeid = org.revenj.postgres.converters.UuidConverter.parse(reader, false); };
		readers[__index___EntityIndex] = (item, reader, context) -> { item.EntityIndex = org.revenj.postgres.converters.IntConverter.parse(reader); };
		readers[__index___Index] = (item, reader, context) -> { item.Index = org.revenj.postgres.converters.IntConverter.parse(reader); };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Detail1>[] readers, int __index__extended_f, int __index__extended_ff, int __index__extended_EntityCompositeid, int __index__extended_EntityIndex, int __index__extended_Index) {
		
		readers[__index__extended_f] = (item, reader, context) -> { item.f = org.revenj.postgres.converters.FloatConverter.parseNullable(reader); };
		readers[__index__extended_ff] = (item, reader, context) -> { item.ff = org.revenj.postgres.converters.FloatConverter.parse(reader); };
		readers[__index__extended_EntityCompositeid] = (item, reader, context) -> { item.EntityCompositeid = org.revenj.postgres.converters.UuidConverter.parse(reader, false); };
		readers[__index__extended_EntityIndex] = (item, reader, context) -> { item.EntityIndex = org.revenj.postgres.converters.IntConverter.parse(reader); };
		readers[__index__extended_Index] = (item, reader, context) -> { item.Index = org.revenj.postgres.converters.IntConverter.parse(reader); };
	}
}
