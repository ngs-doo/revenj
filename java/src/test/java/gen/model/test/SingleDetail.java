package gen.model.test;



public class SingleDetail   implements java.io.Serializable, org.revenj.patterns.AggregateRoot {
	
	
	
	public SingleDetail() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.ID = 0;
		this.details = new gen.model.test.LazyLoad[] { };
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
		final SingleDetail other = (SingleDetail) obj;

		return URI.equals(other.URI);
	}

	public boolean equals(final SingleDetail other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;

		
		if(!(this.ID == other.ID))
			return false;
		if(!(java.util.Arrays.equals(this.detailsURI, other.detailsURI)))
			return false;

		return true;
	}

	@Override
	public String toString() {
		return "SingleDetail(" + URI + ')';
	}
	
	private static final long serialVersionUID = 0x0097000a;
	
	@com.fasterxml.jackson.annotation.JsonCreator private SingleDetail(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("ID") final int ID,
			@com.fasterxml.jackson.annotation.JsonProperty("detailsURI") final String[] detailsURI) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.ID = ID;
		this.detailsURI = detailsURI == null ? new String[0] : detailsURI;
	}

	
	private int ID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ID")
	public int getID()  {
		
		return ID;
	}

	
	private SingleDetail setID(final int value) {
		
		this.ID = value;
		
		return this;
	}

	
	public static void __setupSequenceID() {
		java.util.function.BiConsumer<java.util.Collection<SingleDetail>, java.sql.Connection> assignSequence = (items, connection) -> {
			try (java.sql.PreparedStatement st = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT nextval('\"test\".\"SingleDetail_ID_seq\"'::regclass)::int FROM generate_series(1, ?)")) {
				st.setInt(1, items.size());
				try (java.sql.ResultSet rs = st.executeQuery()) {
					java.util.Iterator<SingleDetail> iterator = items.iterator();
					while (rs.next()) {
						iterator.next().ID = rs.getInt(1);
					}
				}
			} catch (java.sql.SQLException e) {
				throw new RuntimeException(e);
			}
		};

		gen.model.test.repositories.SingleDetailRepository.__setupSequenceID(assignSequence);
	}
	
	private gen.model.test.LazyLoad[] details;

	
	@com.fasterxml.jackson.annotation.JsonIgnore
	public gen.model.test.LazyLoad[] getDetails()  {
		
	
		if(this.detailsURI != null && this.detailsURI.length == 0)
		{
			this.details = new gen.model.test.LazyLoad[] { };
			this.detailsURI = null;
		}
	
		if (this.__locator.isPresent() && (this.detailsURI != null && (this.details == null || this.details.length != this.detailsURI.length))) {
			gen.model.test.repositories.LazyLoadRepository repository = this.__locator.get().resolve(gen.model.test.repositories.LazyLoadRepository.class);
			java.util.List<gen.model.test.LazyLoad> __list = repository.find(this.detailsURI);
			details = __list.toArray(new gen.model.test.LazyLoad[__list.size()]);
			this.detailsURI = null;
		}
		return this.details;
	}

	
	private String[] detailsURI;

	
	@com.fasterxml.jackson.annotation.JsonProperty("detailsURI")
	public String[] getDetailsURI()  {
		
	
			if (this.details != null) {
				final String[] _result = new String[this.details.length];
				int _i = 0;
				for (final gen.model.test.LazyLoad _it : this.details) {
					_result[_i++] = _it.getURI();
				}
				return _result;
			} 
			if (this.detailsURI == null) return new String[0];
		return this.detailsURI;
	}

	
	public SingleDetail(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<SingleDetail>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<SingleDetail> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.test.converters.SingleDetailConverter.buildURI(reader.tmp, ID);
		this.__locator = java.util.Optional.ofNullable(reader.locator);
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<SingleDetail>[] readers, int __index___ID, int __index___detailsURI) {
		
		readers[__index___ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); };
		readers[__index___detailsURI] = (item, reader, context) -> { { 
			java.util.List<String> __list = org.revenj.postgres.converters.StringConverter.parseCollection(reader, context, true); 
			if (__list != null) item.detailsURI = __list.toArray(new String[__list.size()]); else item.detailsURI = new String[0]; 
		}; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<SingleDetail>[] readers, int __index__extended_ID, int __index__extended_detailsURI) {
		
		readers[__index__extended_ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); };
		readers[__index__extended_detailsURI] = (item, reader, context) -> { { 
			java.util.List<String> __list = org.revenj.postgres.converters.StringConverter.parseCollection(reader, context, true); 
			if (__list != null) item.detailsURI = __list.toArray(new String[__list.size()]); else item.detailsURI = new String[0]; 
		}; };
	}
	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator;
}
