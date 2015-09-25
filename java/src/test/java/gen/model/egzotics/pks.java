package gen.model.egzotics;



public class pks   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot {
	
	
	
	public pks() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.id = new java.util.ArrayList<Integer>(4);
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
		if (obj == null || obj instanceof pks == false)
			return false;
		final pks other = (pks) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final pks other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!((this.id == other.id || this.id != null && this.id.equals(other.id))))
			return false;
		return true;
	}

	private pks(pks other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.id = new java.util.ArrayList<Integer>(other.id);
		this.__originalValue = other.__originalValue;
	}

	@Override
	public Object clone() {
		return new pks(this);
	}

	@Override
	public String toString() {
		return "pks(" + URI + ')';
	}
	
	
	public pks(
			final java.util.List<Integer> id) {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		setId(id);
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	private static final long serialVersionUID = 2220090785750092308L;
	
	@com.fasterxml.jackson.annotation.JsonCreator private pks(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("id") final java.util.List<Integer> id) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.id = id == null ? new java.util.ArrayList<Integer>(4) : id;
	}

	
	private java.util.List<Integer> id;

	
	@com.fasterxml.jackson.annotation.JsonProperty("id")
	public java.util.List<Integer> getId()  {
		
		return id;
	}

	
	public pks setId(final java.util.List<Integer> value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"id\" cannot be null!");
		org.revenj.Guards.checkNulls(value);
		this.id = value;
		
		return this;
	}

	private transient pks __originalValue;
	
	static {
		gen.model.egzotics.repositories.pksRepository.__setupPersist(
			(aggregates, sw) -> {
				try {
					for (gen.model.egzotics.pks agg : aggregates) {
						 
						agg.URI = gen.model.egzotics.converters.pksConverter.buildURI(sw, agg.id);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(oldAggregates, newAggregates) -> {
				for (int i = 0; i < newAggregates.size(); i++) {
					gen.model.egzotics.pks oldAgg = oldAggregates.get(i);
					gen.model.egzotics.pks newAgg = newAggregates.get(i);
					 
				}
			},
			aggregates -> { 
				for (gen.model.egzotics.pks agg : aggregates) { 
				}
			},
			agg -> { 
				
		pks _res = agg.__originalValue;
		agg.__originalValue = (pks)agg.clone();
		if (_res != null) {
			return _res;
		}				
				return null;
			}
		);
	}
	
	public pks(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<pks>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<pks> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.egzotics.converters.pksConverter.buildURI(reader, id);
		this.__locator = java.util.Optional.ofNullable(reader.locator);
		this.__originalValue = (pks)this.clone();
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<pks>[] readers, int __index___id) {
		
		readers[__index___id] = (item, reader, context) -> { { java.util.List<Integer> __list = org.revenj.postgres.converters.IntConverter.parseCollection(reader, context, false); if(__list != null) {item.id = __list;} else item.id = new java.util.ArrayList<Integer>(4); }; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<pks>[] readers, int __index__extended_id) {
		
		readers[__index__extended_id] = (item, reader, context) -> { { java.util.List<Integer> __list = org.revenj.postgres.converters.IntConverter.parseCollection(reader, context, false); if(__list != null) {item.id = __list;} else item.id = new java.util.ArrayList<Integer>(4); }; };
	}
}
