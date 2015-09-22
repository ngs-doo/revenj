package gen.model.mixinReference;



@com.fasterxml.jackson.annotation.JsonTypeName("mixinReference.SpecificReport")
public class SpecificReport   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot, gen.model.mixinReference.Report<gen.model.mixinReference.SpecificReport> {
	
	
	
	public SpecificReport() {
			
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
		if (obj == null || obj instanceof SpecificReport == false)
			return false;
		final SpecificReport other = (SpecificReport) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final SpecificReport other) {
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

	private SpecificReport(SpecificReport other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.ID = other.ID;
		this.__originalValue = other.__originalValue;
		this.authorURI = other.authorURI;
		this.authorID = other.authorID;
	}

	@Override
	public Object clone() {
		return new SpecificReport(this);
	}

	@Override
	public String toString() {
		return "SpecificReport(" + URI + ')';
	}
	
	
	public SpecificReport(
			final gen.model.mixinReference.Author author) {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		setAuthor(author);
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	private static final long serialVersionUID = -7109746652338275702L;
	
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

	
	private int ID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ID")
	public int getID()  {
		
		return ID;
	}

	
	private SpecificReport setID(final int value) {
		
		this.ID = value;
		
		return this;
	}

	
	static {
		gen.model.mixinReference.repositories.SpecificReportRepository.__setupSequenceID((items, connection) -> {
			try (java.sql.PreparedStatement st = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT nextval('\"mixinReference\".\"SpecificReport_ID_seq\"'::regclass)::int FROM generate_series(1, ?)")) {
				st.setInt(1, items.size());
				try (java.sql.ResultSet rs = st.executeQuery()) {
					java.util.Iterator<SpecificReport> iterator = items.iterator();
					while (rs.next()) {
						iterator.next().setID(rs.getInt(1));
					}
				}
			} catch (java.sql.SQLException e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	private transient SpecificReport __originalValue;
	
	static {
		gen.model.mixinReference.repositories.SpecificReportRepository.__setupPersist(
			(aggregates, sw) -> {
				try {
					for (gen.model.mixinReference.SpecificReport agg : aggregates) {
						 
						agg.URI = gen.model.mixinReference.converters.SpecificReportConverter.buildURI(sw, agg.ID);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(oldAggregates, newAggregates) -> {
				for (int i = 0; i < newAggregates.size(); i++) {
					gen.model.mixinReference.SpecificReport oldAgg = oldAggregates.get(i);
					gen.model.mixinReference.SpecificReport newAgg = newAggregates.get(i);
					 
				}
			},
			aggregates -> { 
				for (gen.model.mixinReference.SpecificReport agg : aggregates) { 
				}
			},
			agg -> { 
				
		SpecificReport _res = agg.__originalValue;
		agg.__originalValue = (SpecificReport)agg.clone();
		if (_res != null) {
			return _res;
		}				
				return null;
			}
		);
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

	
	public SpecificReport(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<SpecificReport>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<SpecificReport> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.mixinReference.converters.SpecificReportConverter.buildURI(reader, ID);
		this.__locator = java.util.Optional.ofNullable(reader.locator);
		this.__originalValue = (SpecificReport)this.clone();
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
}
