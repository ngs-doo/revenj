package gen.model.test;



public class LazyLoad   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot {
	
	
	
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
		if (obj == null || obj instanceof LazyLoad == false)
			return false;
		final LazyLoad other = (LazyLoad) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final LazyLoad other) {
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
		if(!(this.sdURI == other.sdURI || this.sdURI != null && this.sdURI.equals(other.sdURI)))
			return false;
		if(!(this.sdID == other.sdID || this.sdID != null && this.sdID.equals(other.sdID)))
			return false;
		return true;
	}

	private LazyLoad(LazyLoad other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.ID = other.ID;
		this.compURI = other.compURI;
		this.compID = other.compID;
		this.sdURI = other.sdURI;
		this.sdID = other.sdID;
		this.__originalValue = other.__originalValue;
	}

	@Override
	public Object clone() {
		return new LazyLoad(this);
	}

	@Override
	public String toString() {
		return "LazyLoad(" + URI + ')';
	}
	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	
	@com.fasterxml.jackson.annotation.JsonCreator private LazyLoad(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("ID") final int ID,
			@com.fasterxml.jackson.annotation.JsonProperty("compURI") final String compURI,
			@com.fasterxml.jackson.annotation.JsonProperty("compID") final java.util.UUID compID,
			@com.fasterxml.jackson.annotation.JsonProperty("sdURI") final String sdURI,
			@com.fasterxml.jackson.annotation.JsonProperty("sdID") final Integer sdID) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.ID = ID;
		this.compURI = compURI;
		this.compID = compID;
		this.sdURI = sdURI;
		this.sdID = sdID;
	}

	private static final long serialVersionUID = -5396648296584140895L;
	
	private int ID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ID")
	public int getID()  {
		
		return ID;
	}

	
	private LazyLoad setID(final int value) {
		
		this.ID = value;
		
		return this;
	}

	
	static {
		gen.model.test.repositories.LazyLoadRepository.__setupSequenceID((items, connection) -> {
			try (java.sql.PreparedStatement st = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT nextval('\"test\".\"LazyLoad_ID_seq\"'::regclass)::int FROM generate_series(1, ?)")) {
				st.setInt(1, items.size());
				try (java.sql.ResultSet rs = st.executeQuery()) {
					java.util.Iterator<LazyLoad> iterator = items.iterator();
					while (rs.next()) {
						iterator.next().ID = rs.getInt(1);
					}
				}
			} catch (java.sql.SQLException e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	private gen.model.test.Composite comp;

	
	@com.fasterxml.jackson.annotation.JsonIgnore
	public gen.model.test.Composite getComp()  {
		
	
		if (__locator.isPresent() && (comp != null && !comp.getURI().equals(compURI) || comp == null && compURI != null)) {
			gen.model.test.repositories.CompositeRepository repository = __locator.get().resolve(gen.model.test.repositories.CompositeRepository.class);
			comp = repository.find(compURI).orElse(null);
		}
	if (this.compURI == null && this.comp != null) this.comp = null;
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

	
	private gen.model.test.SingleDetail sd;

	
	@com.fasterxml.jackson.annotation.JsonIgnore
	public gen.model.test.SingleDetail getSd()  {
		
	
		if (__locator.isPresent() && (sd != null && !sd.getURI().equals(sdURI) || sd == null && sdURI != null)) {
			gen.model.test.repositories.SingleDetailRepository repository = __locator.get().resolve(gen.model.test.repositories.SingleDetailRepository.class);
			sd = repository.find(sdURI).orElse(null);
		}
	if (this.sdURI == null && this.sd != null) this.sd = null;
		return sd;
	}

	
	public LazyLoad setSd(final gen.model.test.SingleDetail value) {
		
		
		if(value != null && value.getURI() == null) throw new IllegalArgumentException("Reference \"test.SingleDetail\" for property \"sd\" must be persisted before it's assigned");
		this.sd = value;
		
		
		if (value == null && this.sdID != null) {
			this.sdID = null;
		} else if (value != null) {
			this.sdID = value.getID();
		}
		this.sdURI = value != null ? value.getURI() : null;
		return this;
	}

	
	private String sdURI;

	
	@com.fasterxml.jackson.annotation.JsonProperty("sdURI")
	public String getSdURI()  {
		
		return this.sdURI;
	}

	
	private Integer sdID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("sdID")
	public Integer getSdID()  {
		
		return sdID;
	}

	
	private LazyLoad setSdID(final Integer value) {
		
		this.sdID = value;
		
		return this;
	}

	
	static {
		gen.model.test.repositories.LazyLoadRepository.__setupPersist(
			(aggregates, sw) -> {
				try {
					for (gen.model.test.LazyLoad agg : aggregates) {
						 
						agg.URI = gen.model.test.converters.LazyLoadConverter.buildURI(sw, agg.ID);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(oldAggregates, newAggregates) -> {
				for (int i = 0; i < newAggregates.size(); i++) {
					gen.model.test.LazyLoad oldAgg = oldAggregates.get(i);
					gen.model.test.LazyLoad newAgg = newAggregates.get(i);
					 
				}
			},
			aggregates -> { 
				for (gen.model.test.LazyLoad agg : aggregates) { 
				}
			},
			agg -> { 
				
		LazyLoad _res = agg.__originalValue;
		agg.__originalValue = new LazyLoad(agg);
		if (_res != null) {
			return _res;
		}				
				return null;
			}
		);
	}
	private transient LazyLoad __originalValue;
	
	public LazyLoad(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<LazyLoad>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<LazyLoad> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.test.converters.LazyLoadConverter.buildURI(reader, ID);
		this.__locator = java.util.Optional.ofNullable(reader.locator);
		this.__originalValue = new LazyLoad(this);
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<LazyLoad>[] readers, int __index___ID, int __index___compURI, int __index___compID, int __index___sdURI, int __index___sdID) {
		
		readers[__index___ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); };
		readers[__index___compURI] = (item, reader, context) -> { item.compURI = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); };
		readers[__index___compID] = (item, reader, context) -> { item.compID = org.revenj.postgres.converters.UuidConverter.parse(reader, true); };
		readers[__index___sdURI] = (item, reader, context) -> { item.sdURI = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); };
		readers[__index___sdID] = (item, reader, context) -> { item.sdID = org.revenj.postgres.converters.IntConverter.parseNullable(reader); };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<LazyLoad>[] readers, int __index__extended_ID, int __index__extended_compURI, int __index__extended_compID, int __index__extended_sdURI, int __index__extended_sdID) {
		
		readers[__index__extended_ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); };
		readers[__index__extended_compURI] = (item, reader, context) -> { item.compURI = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); };
		readers[__index__extended_compID] = (item, reader, context) -> { item.compID = org.revenj.postgres.converters.UuidConverter.parse(reader, true); };
		readers[__index__extended_sdURI] = (item, reader, context) -> { item.sdURI = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); };
		readers[__index__extended_sdID] = (item, reader, context) -> { item.sdID = org.revenj.postgres.converters.IntConverter.parseNullable(reader); };
	}
	
	
	public LazyLoad(
			final gen.model.test.Composite comp,
			final gen.model.test.SingleDetail sd) {
			
		setComp(comp);
		setSd(sd);
	}

}
