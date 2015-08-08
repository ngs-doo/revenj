package gen.model.mixinReference;



@com.fasterxml.jackson.annotation.JsonTypeName("mixinReference.SpecificReport")
public class SpecificReport   implements java.io.Serializable, org.revenj.patterns.AggregateRoot, gen.model.mixinReference.Report<gen.model.mixinReference.SpecificReport> {
	
	
	
	public SpecificReport() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.ID = 0;
		this.authorID = 0;
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
		final SpecificReport other = (SpecificReport) obj;

		return URI.equals(other.URI);
	}

	public boolean equals(final SpecificReport other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;

		
		if(!(this.ID == other.ID))
			return false;
		if(!(this.authorURI == other.authorURI || this.authorURI != null && this.authorURI.equals(other.authorURI)))
			return false;
		if(!(this.authorID == other.authorID))
			return false;

		return true;
	}

	@Override
	public String toString() {
		return "SpecificReport(" + URI + ')';
	}
	
	@com.fasterxml.jackson.annotation.JsonCreator private SpecificReport(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("ID") final int ID,
			@com.fasterxml.jackson.annotation.JsonProperty("authorURI") final String authorURI,
			@com.fasterxml.jackson.annotation.JsonProperty("authorID") final int authorID) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.ID = ID;
		this.authorURI = authorURI;
		this.authorID = authorID;
	}

	private static final long serialVersionUID = 545643936112053386L;
	
	private int ID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ID")
	public int getID()  {
		
		return ID;
	}

	
	private SpecificReport setID(final int value) {
		
		this.ID = value;
		
		return this;
	}

	
	public static void __setupSequenceID() {
		java.util.function.BiConsumer<java.util.Collection<SpecificReport>, java.sql.Connection> assignSequence = (items, connection) -> {
			try (java.sql.PreparedStatement st = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT nextval('\"mixinReference\".\"SpecificReport_ID_seq\"'::regclass)::int FROM generate_series(1, ?)")) {
				st.setInt(1, items.size());
				try (java.sql.ResultSet rs = st.executeQuery()) {
					java.util.Iterator<SpecificReport> iterator = items.iterator();
					while (rs.next()) {
						iterator.next().ID = rs.getInt(1);
					}
				}
			} catch (java.sql.SQLException e) {
				throw new RuntimeException(e);
			}
		};

		gen.model.mixinReference.repositories.SpecificReportRepository.__setupSequenceID(assignSequence);
	}
	
	
	private gen.model.mixinReference.Author author;

	
	@com.fasterxml.jackson.annotation.JsonIgnore
	public gen.model.mixinReference.Author getAuthor()  {
		
	
		if (__locator.isPresent() && (author != null && !author.getURI().equals(authorURI) || author == null && authorURI != null)) {
			gen.model.mixinReference.repositories.AuthorRepository repository = __locator.get().resolve(gen.model.mixinReference.repositories.AuthorRepository.class);
			author = repository.find(authorURI).orElse(null);
		}
		return author;
	}

	
	public SpecificReport setAuthor(final gen.model.mixinReference.Author value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"author\" cannot be null!");
		
		if(value != null && value.getURI() == null) throw new IllegalArgumentException("Reference \"mixinReference.Author\" for property \"author\" must be persisted before it's assigned");
		this.author = value;
		
		
		this.authorID = value.getID();
		this.authorURI = value.getURI();
		return this;
	}

	
	private String authorURI;

	
	@com.fasterxml.jackson.annotation.JsonProperty("authorURI")
	public String getAuthorURI()  {
		
		return this.authorURI;
	}

	
	private int authorID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("authorID")
	public int getAuthorID()  {
		
		return authorID;
	}

	
	private SpecificReport setAuthorID(final int value) {
		
		this.authorID = value;
		
		return this;
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	
	public SpecificReport(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<SpecificReport>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<SpecificReport> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.mixinReference.converters.SpecificReportConverter.buildURI(reader.tmp, ID);
		this.__locator = java.util.Optional.ofNullable(reader.locator);
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<SpecificReport>[] readers, int __index___ID, int __index___authorURI, int __index___authorID) {
		
		readers[__index___ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); };
		readers[__index___authorURI] = (item, reader, context) -> { item.authorURI = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); };
		readers[__index___authorID] = (item, reader, context) -> { item.authorID = org.revenj.postgres.converters.IntConverter.parse(reader); };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<SpecificReport>[] readers, int __index__extended_ID, int __index__extended_authorURI, int __index__extended_authorID) {
		
		readers[__index__extended_ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); };
		readers[__index__extended_authorURI] = (item, reader, context) -> { item.authorURI = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); };
		readers[__index__extended_authorID] = (item, reader, context) -> { item.authorID = org.revenj.postgres.converters.IntConverter.parse(reader); };
	}
	
	
	public SpecificReport(
			final gen.model.mixinReference.Author author) {
			
		setAuthor(author);
	}

}
