package gen.model.mixinReference;



public class Child   implements java.lang.Cloneable, java.io.Serializable {
	
	
	
	public Child() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.version = 0L;
		this.AuthorID = 0;
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
		if (obj == null || obj instanceof Child == false)
			return false;
		final Child other = (Child) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final Child other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.version == other.version))
			return false;
		if(!(this.AuthorID == other.AuthorID))
			return false;
		if(!(this.Index == other.Index))
			return false;
		return true;
	}

	private Child(Child other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.version = other.version;
		this.AuthorID = other.AuthorID;
		this.Index = other.Index;
	}

	@Override
	public Object clone() {
		return new Child(this);
	}

	@Override
	public String toString() {
		return "Child(" + URI + ')';
	}
	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	
	@com.fasterxml.jackson.annotation.JsonCreator private Child(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("version") final long version,
			@com.fasterxml.jackson.annotation.JsonProperty("AuthorID") final int AuthorID,
			@com.fasterxml.jackson.annotation.JsonProperty("Index") final int Index) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.version = version;
		this.AuthorID = AuthorID;
		this.Index = Index;
	}

	private static final long serialVersionUID = -4861345868240523535L;
	
	private long version;

	
	@com.fasterxml.jackson.annotation.JsonProperty("version")
	public long getVersion()  {
		
		return version;
	}

	
	public Child setVersion(final long value) {
		
		this.version = value;
		
		return this;
	}

	
	private int AuthorID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("AuthorID")
	public int getAuthorID()  {
		
		return AuthorID;
	}

	
	private Child setAuthorID(final int value) {
		
		this.AuthorID = value;
		
		return this;
	}

	
	private int Index;

	
	@com.fasterxml.jackson.annotation.JsonProperty("Index")
	public int getIndex()  {
		
		return Index;
	}

	
	private Child setIndex(final int value) {
		
		this.Index = value;
		
		return this;
	}

	
	static {
		gen.model.mixinReference.Author.__bindTochildren(parent -> {
			int i = 0;
			for (gen.model.mixinReference.Child e : parent.getChildren()) { 
				e.AuthorID = parent.getID();
				e.Index = i++; 
			}
		});
	}
	
	public Child(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Child>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<Child> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.mixinReference.converters.ChildConverter.buildURI(reader, this);
		this.__locator = java.util.Optional.ofNullable(reader.locator);
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Child>[] readers, int __index___version, int __index___AuthorID, int __index___Index) {
		
		readers[__index___version] = (item, reader, context) -> { item.version = org.revenj.postgres.converters.LongConverter.parse(reader); };
		readers[__index___AuthorID] = (item, reader, context) -> { item.AuthorID = org.revenj.postgres.converters.IntConverter.parse(reader); };
		readers[__index___Index] = (item, reader, context) -> { item.Index = org.revenj.postgres.converters.IntConverter.parse(reader); };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Child>[] readers, int __index__extended_version, int __index__extended_AuthorID, int __index__extended_Index) {
		
		readers[__index__extended_version] = (item, reader, context) -> { item.version = org.revenj.postgres.converters.LongConverter.parse(reader); };
		readers[__index__extended_AuthorID] = (item, reader, context) -> { item.AuthorID = org.revenj.postgres.converters.IntConverter.parse(reader); };
		readers[__index__extended_Index] = (item, reader, context) -> { item.Index = org.revenj.postgres.converters.IntConverter.parse(reader); };
	}
	
	
	public Child(
			final long version) {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		setVersion(version);
	}

}
