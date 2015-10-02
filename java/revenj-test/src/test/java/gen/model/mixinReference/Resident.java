package gen.model.mixinReference;



public class Resident   implements java.lang.Cloneable, java.io.Serializable {
	
	
	
	public Resident() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.id = java.util.UUID.randomUUID();
		this.birth = java.time.LocalDate.now();
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
		if (obj == null || obj instanceof Resident == false)
			return false;
		final Resident other = (Resident) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final Resident other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.id.equals(other.id)))
			return false;
		if(!(this.birth.equals(other.birth)))
			return false;
		return true;
	}

	private Resident(Resident other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.id = other.id;
		this.birth = other.birth;
	}

	@Override
	public Object clone() {
		return new Resident(this);
	}

	@Override
	public String toString() {
		return "Resident(" + URI + ')';
	}
	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	
	@com.fasterxml.jackson.annotation.JsonCreator private Resident(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("id") final java.util.UUID id,
			@com.fasterxml.jackson.annotation.JsonProperty("birth") final java.time.LocalDate birth) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.id = id == null ? new java.util.UUID(0L, 0L) : id;
		this.birth = birth == null ? java.time.LocalDate.of(1, 1, 1) : birth;
	}

	private static final long serialVersionUID = -6120264864521536154L;
	
	private java.util.UUID id;

	
	@com.fasterxml.jackson.annotation.JsonProperty("id")
	public java.util.UUID getId()  {
		
		return id;
	}

	
	public Resident setId(final java.util.UUID value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"id\" cannot be null!");
		this.id = value;
		
		return this;
	}

	
	private java.time.LocalDate birth;

	
	@com.fasterxml.jackson.annotation.JsonProperty("birth")
	public java.time.LocalDate getBirth()  {
		
		return birth;
	}

	
	public Resident setBirth(final java.time.LocalDate value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"birth\" cannot be null!");
		this.birth = value;
		
		return this;
	}

	
	public Resident(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Resident>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<Resident> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.mixinReference.converters.ResidentConverter.buildURI(reader, this);
		this.__locator = java.util.Optional.ofNullable(reader.locator);
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Resident>[] readers, int __index___id, int __index___birth) {
		
		readers[__index___id] = (item, reader, context) -> { item.id = org.revenj.postgres.converters.UuidConverter.parse(reader, false); };
		readers[__index___birth] = (item, reader, context) -> { item.birth = org.revenj.postgres.converters.DateConverter.parse(reader, false); };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Resident>[] readers, int __index__extended_id, int __index__extended_birth) {
		
		readers[__index__extended_id] = (item, reader, context) -> { item.id = org.revenj.postgres.converters.UuidConverter.parse(reader, false); };
		readers[__index__extended_birth] = (item, reader, context) -> { item.birth = org.revenj.postgres.converters.DateConverter.parse(reader, false); };
	}
	
	
	public Resident(
			final java.util.UUID id,
			final java.time.LocalDate birth) {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		setId(id);
		setBirth(birth);
	}

}
