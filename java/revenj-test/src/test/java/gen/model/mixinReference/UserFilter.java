package gen.model.mixinReference;



public class UserFilter   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot {
	
	
	
	public UserFilter() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.ID = 0;
		this.name = "";
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
		if (obj == null || obj instanceof UserFilter == false)
			return false;
		final UserFilter other = (UserFilter) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final UserFilter other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.ID == other.ID))
			return false;
		if(!(this.name.equals(other.name)))
			return false;
		return true;
	}

	private UserFilter(UserFilter other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.ID = other.ID;
		this.name = other.name;
		this.__originalValue = other.__originalValue;
	}

	@Override
	public Object clone() {
		return new UserFilter(this);
	}

	@Override
	public String toString() {
		return "UserFilter(" + URI + ')';
	}
	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	
	@com.fasterxml.jackson.annotation.JsonCreator private UserFilter(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("ID") final int ID,
			@com.fasterxml.jackson.annotation.JsonProperty("name") final String name) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.ID = ID;
		this.name = name == null ? "" : name;
	}

	private static final long serialVersionUID = -4668965649899643135L;
	
	private int ID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ID")
	public int getID()  {
		
		return ID;
	}

	
	private UserFilter setID(final int value) {
		
		this.ID = value;
		
		return this;
	}

	
	static {
		gen.model.mixinReference.repositories.UserFilterRepository.__setupSequenceID((items, connection) -> {
			try (java.sql.PreparedStatement st = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT nextval('\"mixinReference\".\"UserFilter_ID_seq\"'::regclass)::int FROM generate_series(1, ?)")) {
				st.setInt(1, items.size());
				try (java.sql.ResultSet rs = st.executeQuery()) {
					java.util.Iterator<UserFilter> iterator = items.iterator();
					while (rs.next()) {
						iterator.next().setID(rs.getInt(1));
					}
				}
			} catch (java.sql.SQLException e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	private String name;

	
	@com.fasterxml.jackson.annotation.JsonProperty("name")
	public String getName()  {
		
		return name;
	}

	
	public UserFilter setName(final String value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"name\" cannot be null!");
		this.name = value;
		
		return this;
	}

	
	static {
		gen.model.mixinReference.repositories.UserFilterRepository.__setupPersist(
			(aggregates, arg) -> {
				try {
					for (gen.model.mixinReference.UserFilter agg : aggregates) {
						 
						agg.URI = gen.model.mixinReference.converters.UserFilterConverter.buildURI(arg, agg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(oldAggregates, newAggregates) -> {
				for (int i = 0; i < newAggregates.size(); i++) {
					gen.model.mixinReference.UserFilter oldAgg = oldAggregates.get(i);
					gen.model.mixinReference.UserFilter newAgg = newAggregates.get(i);
					 
				}
			},
			aggregates -> { 
				for (gen.model.mixinReference.UserFilter agg : aggregates) { 
				}
			},
			agg -> { 
				
		UserFilter _res = agg.__originalValue;
		agg.__originalValue = (UserFilter)agg.clone();
		if (_res != null) {
			return _res;
		}
				return null;
			}
		);
	}
	private transient UserFilter __originalValue;
	
	public UserFilter(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<UserFilter>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<UserFilter> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.mixinReference.converters.UserFilterConverter.buildURI(reader, this);
		this.__locator = java.util.Optional.ofNullable(reader.locator);
		this.__originalValue = (UserFilter)this.clone();
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<UserFilter>[] readers, int __index___ID, int __index___name) {
		
		readers[__index___ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); };
		readers[__index___name] = (item, reader, context) -> { item.name = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<UserFilter>[] readers, int __index__extended_ID, int __index__extended_name) {
		
		readers[__index__extended_ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); };
		readers[__index__extended_name] = (item, reader, context) -> { item.name = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); };
	}
	
	
	public UserFilter(
			final String name) {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		setName(name);
	}

}
