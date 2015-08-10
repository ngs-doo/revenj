package gen.model.mixinReference;



public class Author   implements java.io.Serializable, org.revenj.patterns.AggregateRoot {
	
	
	
	public Author() {
			
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
		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;
		final Author other = (Author) obj;

		return URI.equals(other.URI);
	}

	public boolean equals(final Author other) {
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

	@Override
	public String toString() {
		return "Author(" + URI + ')';
	}
	
	
	public Author(
			final String name) {
			
		setName(name);
	}

	private static final long serialVersionUID = -128713849998849451L;
	
	@com.fasterxml.jackson.annotation.JsonCreator private Author(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("ID") final int ID,
			@com.fasterxml.jackson.annotation.JsonProperty("name") final String name) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.ID = ID;
		this.name = name == null ? "" : name;
	}

	
	private int ID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ID")
	public int getID()  {
		
		return ID;
	}

	
	private Author setID(final int value) {
		
		this.ID = value;
		
		return this;
	}

	
	static {
		gen.model.mixinReference.repositories.AuthorRepository.__setupSequenceID((items, connection) -> {
			try (java.sql.PreparedStatement st = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT nextval('\"mixinReference\".\"Author_ID_seq\"'::regclass)::int FROM generate_series(1, ?)")) {
				st.setInt(1, items.size());
				try (java.sql.ResultSet rs = st.executeQuery()) {
					java.util.Iterator<Author> iterator = items.iterator();
					while (rs.next()) {
						iterator.next().ID = rs.getInt(1);
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

	
	public Author setName(final String value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"name\" cannot be null!");
		this.name = value;
		
		return this;
	}

	
	static {
		gen.model.mixinReference.repositories.AuthorRepository.__setupPersist(
			(aggregates, sw) -> {
				try {
					for (gen.model.mixinReference.Author agg : aggregates) {
						 
						agg.URI = gen.model.mixinReference.converters.AuthorConverter.buildURI(sw, agg.ID);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(oldAggregates, newAggregates) -> {
				for (int i = 0; i < newAggregates.size(); i++) {
					gen.model.mixinReference.Author oldAgg = oldAggregates.get(i);
					gen.model.mixinReference.Author newAgg = newAggregates.get(i);
					 
				}
			},
			(aggregates) -> { 
				for (gen.model.mixinReference.Author agg : aggregates) { 
				}
			}
		);
	}
	
	public Author(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Author>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<Author> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.mixinReference.converters.AuthorConverter.buildURI(reader, ID);
		this.__locator = java.util.Optional.ofNullable(reader.locator);
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Author>[] readers, int __index___ID, int __index___name) {
		
		readers[__index___ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); };
		readers[__index___name] = (item, reader, context) -> { item.name = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Author>[] readers, int __index__extended_ID, int __index__extended_name) {
		
		readers[__index__extended_ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); };
		readers[__index__extended_name] = (item, reader, context) -> { item.name = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); };
	}
	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
}
