package gen.model.Seq;



public class Next   implements java.io.Serializable, org.revenj.patterns.AggregateRoot {
	
	
	
	public Next() {
			
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
		final Next other = (Next) obj;

		return URI.equals(other.URI);
	}

	public boolean equals(final Next other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;

		
		if(!(this.ID == other.ID))
			return false;

		return true;
	}

	@Override
	public String toString() {
		return "Next(" + URI + ')';
	}
	
	@com.fasterxml.jackson.annotation.JsonCreator private Next(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("ID") final int ID) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.ID = ID;
	}

	
	private static final long serialVersionUID = 0x0097000a;
	
	private int ID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ID")
	public int getID()  {
		
		return ID;
	}

	
	private Next setID(final int value) {
		
		this.ID = value;
		
		return this;
	}

	
	public static void __setupSequenceID() {
		java.util.function.BiConsumer<java.util.List<Next>, java.sql.Connection> assignSequence = (items, connection) -> {
			try (java.sql.PreparedStatement st = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT nextval('\"Seq\".\"Next_ID_seq\"'::regclass)::int FROM generate_series(1, ?)")) {
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

		gen.model.Seq.repositories.NextRepository.__setupSequenceID(assignSequence);
	}
	

public static class BetweenIds   implements java.io.Serializable, org.revenj.patterns.Specification<Next> {
	
	
	
	public BetweenIds(
			 final Integer min,
			 final int max) {
			
		setMin(min);
		setMax(max);
	}

	
	
	public BetweenIds() {
			
		this.max = 0;
	}

	private static final long serialVersionUID = 0x0097000a;
	
	private Integer min;

	
	@com.fasterxml.jackson.annotation.JsonProperty("min")
	public Integer getMin()  {
		
		return min;
	}

	
	public BetweenIds setMin(final Integer value) {
		
		this.min = value;
		
		return this;
	}

	
	private int max;

	
	@com.fasterxml.jackson.annotation.JsonProperty("max")
	public int getMax()  {
		
		return max;
	}

	
	public BetweenIds setMax(final int value) {
		
		this.max = value;
		
		return this;
	}

}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator;
	
	public Next(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Next>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<Next> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.Seq.converters.NextConverter.buildURI(reader.tmp, ID);
		this.__locator = java.util.Optional.ofNullable(reader.locator);
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Next>[] readers, int __index___ID) {
		
		readers[__index___ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Next>[] readers, int __index__extended_ID) {
		
		readers[__index__extended_ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); };
	}
}
