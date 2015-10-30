package gen.model.binaries;



public final class WritableDocument   implements java.io.Serializable, org.revenj.patterns.AggregateRoot, org.revenj.patterns.DataSource {
	
	
	@com.fasterxml.jackson.annotation.JsonCreator 
	public WritableDocument(
			@com.fasterxml.jackson.annotation.JsonProperty("id")  final java.util.UUID id,
			@com.fasterxml.jackson.annotation.JsonProperty("name")  final String name) {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		setId(id);
		setName(name);
	}

	private static final long serialVersionUID = -4747680349767553050L;
	
	private String URI;

	
	public String getURI()  {
		
		return this.URI;
	}

	
	static {
		gen.model.binaries.repositories.WritableDocumentRepository.__setupPersist(
			(aggregates, arg) -> {
				try {
					for (gen.model.binaries.WritableDocument agg : aggregates) {
						agg.URI = gen.model.binaries.converters.WritableDocumentConverter.buildURI(arg, agg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		);
	}
	
	private java.util.UUID id;

	
	@com.fasterxml.jackson.annotation.JsonProperty("id")
	public java.util.UUID getId()  {
		
		return id;
	}

	
	public WritableDocument setId(final java.util.UUID value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"id\" cannot be null!");
		this.id = value;
		
		return this;
	}

	
	private String name;

	
	@com.fasterxml.jackson.annotation.JsonProperty("name")
	public String getName()  {
		
		return name;
	}

	
	public WritableDocument setName(final String value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"name\" cannot be null!");
		this.name = value;
		
		return this;
	}

	
	public WritableDocument(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<WritableDocument>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<WritableDocument> rdr : readers) {
			rdr.read(this, reader, context);
		}
		this.URI = gen.model.binaries.converters.WritableDocumentConverter.buildURI(reader, this);
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<WritableDocument>[] readers, int __index___id, int __index___name) {
		
		readers[__index___id] = (item, reader, context) -> { item.id = org.revenj.postgres.converters.UuidConverter.parse(reader, false); };
		readers[__index___name] = (item, reader, context) -> { item.name = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); };
	}
}
