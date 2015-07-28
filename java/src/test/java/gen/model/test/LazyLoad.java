package gen.model.test;



public class LazyLoad   implements java.io.Serializable, org.revenj.patterns.AggregateRoot {
	
	
	
	public LazyLoad() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.ID = 0;
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
		final LazyLoad other = (LazyLoad) obj;

		return URI.equals(other.URI);
	}

	public boolean equals(final LazyLoad other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;

		
		if(!(this.ID == other.ID))
			return false;
		if(!(this.compURI == other.compURI || this.compURI != null && this.compURI.equals(other.compURI)))
			return false;
		if(!(this.compID == other.compID || this.compID != null && this.compID.equals(other.compID)))
			return false;

		return true;
	}

	@Override
	public String toString() {
		return "LazyLoad(" + URI + ')';
	}
	
	
	public LazyLoad(
			final gen.model.test.Composite comp) {
			
		setComp(comp);
	}

	
	private static final long serialVersionUID = 0x0097000a;
	
	@com.fasterxml.jackson.annotation.JsonCreator private LazyLoad(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("ID") final int ID,
			@com.fasterxml.jackson.annotation.JsonProperty("compURI") final String compURI,
			@com.fasterxml.jackson.annotation.JsonProperty("compID") final java.util.UUID compID) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.ID = ID;
		this.compURI = compURI;
		this.compID = compID;
	}

	
	private int ID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ID")
	public int getID()  {
		
		return ID;
	}

	
	private LazyLoad setID(final int value) {
		
		this.ID = value;
		
		return this;
	}

	
	public static void __setupSequenceID() {
		java.util.function.BiConsumer<java.util.List<LazyLoad>, java.sql.Connection> assignSequence = (items, connection) -> {
			try (java.sql.PreparedStatement st = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT nextval('\"test\".\"LazyLoad_ID_seq\"'::regclass)::int FROM generate_series(1, ?)")) {
				st.setInt(1, items.size());
				java.sql.ResultSet rs = st.executeQuery();
				int cnt = 0;
				while (rs.next()) {
					items.get(cnt++).ID = rs.getInt(1);
				}
				rs.close();
			} catch (java.sql.SQLException e) {
				throw new RuntimeException(e);
			}
		};

		gen.model.test.repositories.LazyLoadRepository.__setupSequenceID(assignSequence);
	}
	
	private gen.model.test.Composite comp;

	
	@com.fasterxml.jackson.annotation.JsonIgnore
	public gen.model.test.Composite getComp()  {
		
	if (this.compURI == null && this.comp != null) this.comp = null;
	
		if (__locator.isPresent() && (comp != null && !comp.getURI().equals(compURI) || comp == null && compURI != null)) {
			gen.model.test.repositories.CompositeRepository repository = __locator.get().resolve(gen.model.test.repositories.CompositeRepository.class);
			comp = repository.find(compURI).orElse(null);
		}
		return comp;
	}

	
	public LazyLoad setComp(final gen.model.test.Composite value) {
		
		
		if(value != null && value.getURI() == null) throw new IllegalArgumentException("Reference \"test.Composite\" for property \"comp\" must be persisted before it's assigned");
		this.comp = value;
		
		
		if (value == null && this.compID != null) {
			this.compID = null;
		} else if (value != null) {
			this.compID = value.getId();
		}
		this.compURI = value != null ? value.getURI() : null;
		return this;
	}

	
	private String compURI;

	
	@com.fasterxml.jackson.annotation.JsonProperty("compURI")
	public String getCompURI()  {
		
		return this.compURI;
	}

	
	private java.util.UUID compID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("compID")
	public java.util.UUID getCompID()  {
		
		return compID;
	}

	
	private LazyLoad setCompID(final java.util.UUID value) {
		
		this.compID = value;
		
		return this;
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator;
	
	public LazyLoad(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<LazyLoad>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<LazyLoad> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.test.converters.LazyLoadConverter.buildURI(reader.tmp, ID);
		this.__locator = java.util.Optional.ofNullable(reader.locator);
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<LazyLoad>[] readers, int __index___ID, int __index___compURI, int __index___compID) {
		
		readers[__index___ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); };
		readers[__index___compURI] = (item, reader, context) -> { item.compURI = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); };
		readers[__index___compID] = (item, reader, context) -> { item.compID = org.revenj.postgres.converters.UuidConverter.parse(reader, true); };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<LazyLoad>[] readers, int __index__extended_ID, int __index__extended_compURI, int __index__extended_compID) {
		
		readers[__index__extended_ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); };
		readers[__index__extended_compURI] = (item, reader, context) -> { item.compURI = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); };
		readers[__index__extended_compID] = (item, reader, context) -> { item.compID = org.revenj.postgres.converters.UuidConverter.parse(reader, true); };
	}
}
