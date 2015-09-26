package gen.model.egzotics;



public class PksV   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot {
	
	
	
	public PksV() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.v = new gen.model.egzotics.v();
		this.vv = new gen.model.egzotics.v[] { };
		this.e = gen.model.egzotics.E.A;
		this.ee = new java.util.HashSet<gen.model.egzotics.E>(4);
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
		if (obj == null || obj instanceof PksV == false)
			return false;
		final PksV other = (PksV) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final PksV other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.v == other.v || this.v != null && this.v.equals(other.v)))
			return false;
		if(!(java.util.Arrays.equals(this.vv, other.vv)))
			return false;
		if(!(this.e == other.e || this.e != null && this.e.equals(other.e)))
			return false;
		if(!((this.ee == other.ee || this.ee != null && this.ee.equals(other.ee))))
			return false;
		return true;
	}

	private PksV(PksV other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.v = other.v == null ? null : (gen.model.egzotics.v)(other.v.clone());
		this.vv = new gen.model.egzotics.v[other.vv.length];
			if (other.vv != null) {
				for (int _i = 0; _i < other.vv.length; _i++) {
					this.vv[_i] = (gen.model.egzotics.v)other.vv[_i].clone();
				}
			};
		this.e = other.e;
		this.ee = new java.util.HashSet<gen.model.egzotics.E>(other.ee);
		this.__originalValue = other.__originalValue;
	}

	@Override
	public Object clone() {
		return new PksV(this);
	}

	@Override
	public String toString() {
		return "PksV(" + URI + ')';
	}
	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	
	@com.fasterxml.jackson.annotation.JsonCreator private PksV(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("v") final gen.model.egzotics.v v,
			@com.fasterxml.jackson.annotation.JsonProperty("vv") final gen.model.egzotics.v[] vv,
			@com.fasterxml.jackson.annotation.JsonProperty("e") final gen.model.egzotics.E e,
			@com.fasterxml.jackson.annotation.JsonProperty("ee") final java.util.Set<gen.model.egzotics.E> ee) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.v = v == null ? new gen.model.egzotics.v() : v;
		this.vv = vv == null ? new gen.model.egzotics.v[] { } : vv;
		this.e = e == null ? gen.model.egzotics.E.A : e;
		this.ee = ee == null ? new java.util.HashSet<gen.model.egzotics.E>(4) : ee;
	}

	private static final long serialVersionUID = -2545546561469625373L;
	
	private gen.model.egzotics.v v;

	
	@com.fasterxml.jackson.annotation.JsonProperty("v")
	public gen.model.egzotics.v getV()  {
		
		return v;
	}

	
	public PksV setV(final gen.model.egzotics.v value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"v\" cannot be null!");
		this.v = value;
		
		return this;
	}

	
	private gen.model.egzotics.v[] vv;

	
	@com.fasterxml.jackson.annotation.JsonProperty("vv")
	public gen.model.egzotics.v[] getVv()  {
		
		return vv;
	}

	
	public PksV setVv(final gen.model.egzotics.v[] value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"vv\" cannot be null!");
		org.revenj.Guards.checkNulls(value);
		this.vv = value;
		
		return this;
	}

	
	private gen.model.egzotics.E e;

	
	@com.fasterxml.jackson.annotation.JsonProperty("e")
	public gen.model.egzotics.E getE()  {
		
		return e;
	}

	
	public PksV setE(final gen.model.egzotics.E value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"e\" cannot be null!");
		this.e = value;
		
		return this;
	}

	
	private java.util.Set<gen.model.egzotics.E> ee;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ee")
	public java.util.Set<gen.model.egzotics.E> getEe()  {
		
		return ee;
	}

	
	public PksV setEe(final java.util.Set<gen.model.egzotics.E> value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"ee\" cannot be null!");
		org.revenj.Guards.checkNulls(value);
		this.ee = value;
		
		return this;
	}

	
	static {
		gen.model.egzotics.repositories.PksVRepository.__setupPersist(
			(aggregates, arg) -> {
				try {
					for (gen.model.egzotics.PksV agg : aggregates) {
						 
						agg.URI = arg.getValue().buildURI(arg.getKey(), agg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(oldAggregates, newAggregates) -> {
				for (int i = 0; i < newAggregates.size(); i++) {
					gen.model.egzotics.PksV oldAgg = oldAggregates.get(i);
					gen.model.egzotics.PksV newAgg = newAggregates.get(i);
					 
				}
			},
			aggregates -> { 
				for (gen.model.egzotics.PksV agg : aggregates) { 
				}
			},
			agg -> { 
				
		PksV _res = agg.__originalValue;
		agg.__originalValue = (PksV)agg.clone();
		if (_res != null) {
			return _res;
		}
				return null;
			}
		);
	}
	private transient PksV __originalValue;
	
	public PksV(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<PksV>[] readers, gen.model.egzotics.converters.PksVConverter converter) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<PksV> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = converter.buildURI(reader, this);
		this.__locator = java.util.Optional.ofNullable(reader.locator);
		this.__originalValue = (PksV)this.clone();
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<PksV>[] readers, gen.model.egzotics.converters.vConverter __converter_v, int __index___v, gen.model.egzotics.converters.vConverter __converter_vv, int __index___vv, int __index___e, int __index___ee) {
		
		readers[__index___v] = (item, reader, context) -> { item.v = __converter_v.from(reader, context); };
		readers[__index___vv] = (item, reader, context) -> { { java.util.List<gen.model.egzotics.v> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, __converter_vv::from); if (__list != null) {item.vv = __list.toArray(new gen.model.egzotics.v[__list.size()]);} else item.vv = new gen.model.egzotics.v[] { }; }; };
		readers[__index___e] = (item, reader, context) -> { item.e = gen.model.egzotics.converters.EConverter.fromReader(reader); };
		readers[__index___ee] = (item, reader, context) -> { { java.util.List<gen.model.egzotics.E> __list = org.revenj.postgres.converters.EnumConverter.parseCollection(reader, context, gen.model.egzotics.E.A, gen.model.egzotics.converters.EConverter::convertEnum); if (__list != null) {item.ee = new java.util.HashSet<gen.model.egzotics.E>(__list);} else item.ee = new java.util.HashSet<gen.model.egzotics.E>(4); }; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<PksV>[] readers, final gen.model.egzotics.converters.vConverter __converter_v, int __index__extended_v, final gen.model.egzotics.converters.vConverter __converter_vv, int __index__extended_vv, int __index__extended_e, int __index__extended_ee) {
		
		readers[__index__extended_v] = (item, reader, context) -> { item.v = __converter_v.fromExtended(reader, context); };
		readers[__index__extended_vv] = (item, reader, context) -> { { java.util.List<gen.model.egzotics.v> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, __converter_vv::fromExtended); if (__list != null) {item.vv = __list.toArray(new gen.model.egzotics.v[__list.size()]);} else item.vv = new gen.model.egzotics.v[] { }; }; };
		readers[__index__extended_e] = (item, reader, context) -> { item.e = gen.model.egzotics.converters.EConverter.fromReader(reader); };
		readers[__index__extended_ee] = (item, reader, context) -> { { java.util.List<gen.model.egzotics.E> __list = org.revenj.postgres.converters.EnumConverter.parseCollection(reader, context, gen.model.egzotics.E.A, gen.model.egzotics.converters.EConverter::convertEnum); if (__list != null) {item.ee = new java.util.HashSet<gen.model.egzotics.E>(__list);} else item.ee = new java.util.HashSet<gen.model.egzotics.E>(4); }; };
	}
	
	
	public PksV(
			final gen.model.egzotics.v v,
			final gen.model.egzotics.v[] vv,
			final gen.model.egzotics.E e,
			final java.util.Set<gen.model.egzotics.E> ee) {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		setV(v);
		setVv(vv);
		setE(e);
		setEe(ee);
	}

}
