package gen.model.test;



public class Composite   implements java.io.Serializable {
	
	
	
	public Composite() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.id = java.util.UUID.randomUUID();
		this.simple = new gen.model.test.Simple();
	}

	
	private String URI;

	
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
		if(!(this.simple == other.simple || this.simple != null && this.simple.equals(other.simple)))
			return false;

		return true;
	}

	@Override
	public String toString() {
		return "Composite(" + URI + ')';
	}
	
	
	public Composite(
			final java.util.UUID id,
			final gen.model.test.Simple simple) {
			
		setId(id);
		setSimple(simple);
	}

	
	private static final long serialVersionUID = 0x0097000a;
	
	private java.util.UUID id;

	
	public java.util.UUID getId()  {
		
		return id;
	}

	
	public Composite setId(final java.util.UUID value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"id\" cannot be null!");
		this.id = value;
		
		return this;
	}

	
	private gen.model.test.Simple simple;

	
	public gen.model.test.Simple getSimple()  {
		
		return simple;
	}

	
	public Composite setSimple(final gen.model.test.Simple value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"simple\" cannot be null!");
		this.simple = value;
		
		return this;
	}

	
	public Composite(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Composite>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<Composite> rdr : readers) {
			rdr.read(this, reader, context);
		}
	}

	public static void configureConverter(org.revenj.postgres.ObjectConverter.Reader<Composite>[] readers, int __index___id, gen.model._DatabaseCommon.Factorytest.SimpleConverter __converter_simple, int __index___simple) {
		
		readers[__index___id] = (item, reader, context) -> { item.id = org.revenj.postgres.converters.UuidConverter.parse(reader, true); };
		readers[__index___simple] = (item, reader, context) -> { item.simple = __converter_simple.from(reader, context); };
	}
	
	public static void configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Composite>[] readers, int __index__extended_id, final gen.model._DatabaseCommon.Factorytest.SimpleConverter __converter_simple, int __index__extended_simple) {
		
		readers[__index__extended_id] = (item, reader, context) -> { item.id = org.revenj.postgres.converters.UuidConverter.parse(reader, true); };
		readers[__index__extended_simple] = (item, reader, context) -> { item.simple = __converter_simple.fromExtended(reader, context); };
	}
}
