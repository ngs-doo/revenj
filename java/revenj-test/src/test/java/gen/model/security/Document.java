package gen.model.security;


@com.fasterxml.jackson.annotation.JsonTypeName("security.Document")
public class Document   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot, gen.model.security.IsActive<gen.model.security.Document>, gen.model.security.Dummy<gen.model.security.Document> {
	
	
	
	public Document() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.ID = 0;
		this.data = new java.util.LinkedHashMap<String, String>();
		this.deactivated = false;
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
		if (obj == null || obj instanceof Document == false)
			return false;
		final Document other = (Document) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final Document other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.ID == other.ID))
			return false;
		if(!(this.data != null && this.data.equals(other.data) || this.data == null && other.data == null))
			return false;
		if(!(this.deactivated == other.deactivated))
			return false;
		return true;
	}

	private Document(Document other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.ID = other.ID;
		this.data = other.data != null ? new java.util.LinkedHashMap<String, String>(other.data) : null;
		this.__originalValue = other.__originalValue;
		this.deactivated = other.deactivated;
	}

	@Override
	public Object clone() {
		return new Document(this);
	}

	@Override
	public String toString() {
		return "Document(" + URI + ')';
	}
	
	
	public Document(
			final java.util.Map<String, String> data,
			final boolean deactivated) {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		setData(data);
		setDeactivated(deactivated);
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	private static final long serialVersionUID = 6673814813813836845L;
	
	@com.fasterxml.jackson.annotation.JsonCreator private Document(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("ID") final int ID,
			@com.fasterxml.jackson.annotation.JsonProperty("data") final java.util.Map<String, String> data,
			@com.fasterxml.jackson.annotation.JsonProperty("deactivated") final boolean deactivated) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.ID = ID;
		this.data = data == null ? new java.util.LinkedHashMap<String, String>() : data;
		this.deactivated = deactivated;
	}

	
	private int ID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ID")
	public int getID()  {
		
		return ID;
	}

	
	private Document setID(final int value) {
		
		this.ID = value;
		
		return this;
	}

	
	static {
		gen.model.security.repositories.DocumentRepository.__setupSequenceID((items, connection) -> {
			try (java.sql.PreparedStatement st = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT nextval('\"security\".\"Document_ID_seq\"'::regclass)::int FROM generate_series(1, ?)")) {
				st.setInt(1, items.size());
				try (java.sql.ResultSet rs = st.executeQuery()) {
					java.util.Iterator<Document> iterator = items.iterator();
					while (rs.next()) {
						iterator.next().setID(rs.getInt(1));
					}
				}
			} catch (java.sql.SQLException e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	private java.util.Map<String, String> data;

	
	@com.fasterxml.jackson.annotation.JsonProperty("data")
	public java.util.Map<String, String> getData()  {
		
		return data;
	}

	
	public Document setData(final java.util.Map<String, String> value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"data\" cannot be null!");
		this.data = value;
		
		return this;
	}

	private transient Document __originalValue;
	
	static {
		gen.model.security.repositories.DocumentRepository.__setupPersist(
			(aggregates, arg) -> {
				try {
					for (gen.model.security.Document agg : aggregates) {
						 
						agg.URI = gen.model.security.converters.DocumentConverter.buildURI(arg, agg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(oldAggregates, newAggregates) -> {
				for (int i = 0; i < newAggregates.size(); i++) {
					gen.model.security.Document oldAgg = oldAggregates.get(i);
					gen.model.security.Document newAgg = newAggregates.get(i);
					 
				}
			},
			aggregates -> { 
				for (gen.model.security.Document agg : aggregates) { 
				}
			},
			agg -> { 
				
		Document _res = agg.__originalValue;
		agg.__originalValue = (Document)agg.clone();
		if (_res != null) {
			return _res;
		}
				return null;
			}
		);
	}
	
	private boolean deactivated;

	
	@com.fasterxml.jackson.annotation.JsonProperty("deactivated")
	public boolean getDeactivated()  {
		
		return deactivated;
	}

	
	public Document setDeactivated(final boolean value) {
		
		this.deactivated = value;
		
		return this;
	}

	
	public Document(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Document>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<Document> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.security.converters.DocumentConverter.buildURI(reader, this);
		this.__locator = java.util.Optional.ofNullable(reader.locator);
		this.__originalValue = (Document)this.clone();
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Document>[] readers, int __index___ID, int __index___data, int __index___deactivated) {
		
		readers[__index___ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); };
		readers[__index___data] = (item, reader, context) -> { item.data = org.revenj.postgres.converters.HstoreConverter.parse(reader, context, false); };
		readers[__index___deactivated] = (item, reader, context) -> { item.deactivated = org.revenj.postgres.converters.BoolConverter.parse(reader); };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Document>[] readers, int __index__extended_ID, int __index__extended_data, int __index__extended_deactivated) {
		
		readers[__index__extended_ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); };
		readers[__index__extended_data] = (item, reader, context) -> { item.data = org.revenj.postgres.converters.HstoreConverter.parse(reader, context, false); };
		readers[__index__extended_deactivated] = (item, reader, context) -> { item.deactivated = org.revenj.postgres.converters.BoolConverter.parse(reader); };
	}
}
