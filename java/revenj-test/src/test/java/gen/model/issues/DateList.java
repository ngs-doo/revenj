package gen.model.issues;



public class DateList   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot {
	
	
	
	public DateList() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.ID = 0L;
		this.ID = --__SequenceCounterID__;
		this.list = new java.util.ArrayList<java.time.OffsetDateTime>(4);
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
		if (obj == null || obj instanceof DateList == false)
			return false;
		final DateList other = (DateList) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final DateList other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.ID == other.ID))
			return false;
		if(!((this.list == other.list || this.list != null && this.list.equals(other.list))))
			return false;
		return true;
	}

	private DateList(DateList other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.ID = other.ID;
		this.list = new java.util.ArrayList<java.time.OffsetDateTime>(other.list);
		this.__originalValue = other.__originalValue;
	}

	@Override
	public Object clone() {
		return new DateList(this);
	}

	@Override
	public String toString() {
		return "DateList(" + URI + ')';
	}
	
	
	public DateList(
			final java.util.List<java.time.OffsetDateTime> list) {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		setList(list);
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	private static final long serialVersionUID = 7866065455683476606L;
	
	@com.fasterxml.jackson.annotation.JsonCreator private DateList(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("ID") final long ID,
			@com.fasterxml.jackson.annotation.JsonProperty("list") final java.util.List<java.time.OffsetDateTime> list) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.ID = ID;
		this.list = list == null ? new java.util.ArrayList<java.time.OffsetDateTime>(4) : list;
	}

	
	private long ID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ID")
	public long getID()  {
		
		return ID;
	}

	
	private DateList setID(final long value) {
		
		this.ID = value;
		
		return this;
	}

	
	static {
		gen.model.issues.repositories.DateListRepository.__setupSequenceID((items, connection) -> {
			try (java.sql.PreparedStatement st = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT nextval('\"issues\".\"DateList_ID_seq\"'::regclass)::bigint FROM generate_series(1, ?)")) {
				st.setInt(1, items.size());
				try (java.sql.ResultSet rs = st.executeQuery()) {
					java.util.Iterator<DateList> iterator = items.iterator();
					while (rs.next()) {
						iterator.next().setID(rs.getLong(1));
					}
				}
			} catch (java.sql.SQLException e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	private static long __SequenceCounterID__;
	
	private java.util.List<java.time.OffsetDateTime> list;

	
	@com.fasterxml.jackson.annotation.JsonProperty("list")
	public java.util.List<java.time.OffsetDateTime> getList()  {
		
		return list;
	}

	
	public DateList setList(final java.util.List<java.time.OffsetDateTime> value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"list\" cannot be null!");
		this.list = value;
		
		return this;
	}

	private transient DateList __originalValue;
	
	static {
		gen.model.issues.repositories.DateListRepository.__setupPersist(
			(aggregates, arg) -> {
				try {
					for (gen.model.issues.DateList agg : aggregates) {
						 
						agg.URI = gen.model.issues.converters.DateListConverter.buildURI(arg, agg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(oldAggregates, newAggregates) -> {
				for (int i = 0; i < newAggregates.size(); i++) {
					gen.model.issues.DateList oldAgg = oldAggregates.get(i);
					gen.model.issues.DateList newAgg = newAggregates.get(i);
					 
				}
			},
			aggregates -> { 
				for (gen.model.issues.DateList agg : aggregates) { 
				}
			},
			agg -> { 
				
		DateList _res = agg.__originalValue;
		agg.__originalValue = (DateList)agg.clone();
		if (_res != null) {
			return _res;
		}
				return null;
			}
		);
	}
	
	public DateList(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<DateList>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<DateList> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.issues.converters.DateListConverter.buildURI(reader, this);
		this.__locator = java.util.Optional.ofNullable(reader.locator);
		this.__originalValue = (DateList)this.clone();
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<DateList>[] readers, int __index___ID, int __index___list) {
		
		readers[__index___ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.LongConverter.parse(reader); };
		readers[__index___list] = (item, reader, context) -> { { java.util.List<java.time.OffsetDateTime> __list = org.revenj.postgres.converters.TimestampConverter.parseOffsetCollection(reader, context, true, true); if(__list != null) {item.list = __list;} else item.list = new java.util.ArrayList<java.time.OffsetDateTime>(4); }; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<DateList>[] readers, int __index__extended_ID, int __index__extended_list) {
		
		readers[__index__extended_ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.LongConverter.parse(reader); };
		readers[__index__extended_list] = (item, reader, context) -> { { java.util.List<java.time.OffsetDateTime> __list = org.revenj.postgres.converters.TimestampConverter.parseOffsetCollection(reader, context, true, true); if(__list != null) {item.list = __list;} else item.list = new java.util.ArrayList<java.time.OffsetDateTime>(4); }; };
	}
}
