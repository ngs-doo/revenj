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
		if(!(this.xml == other.xml || this.xml != null && this.xml.equals(other.xml)))
			return false;
		if(!(this.s3 == other.s3 || this.s3 != null && this.s3.equals(other.s3)))
			return false;
		return true;
	}

	private pks(pks other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.id = new java.util.ArrayList<Integer>(other.id);
		this.xml = other.xml != null ? (org.w3c.dom.Element)other.xml.cloneNode(true) : null;
		this.s3 = other.s3;
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
			final java.util.List<Integer> id,
			final org.w3c.dom.Element xml,
			final org.revenj.storage.S3 s3) {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		setId(id);
		setXml(xml);
		setS3(s3);
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	private static final long serialVersionUID = -8152065200606509050L;
	
	@com.fasterxml.jackson.annotation.JsonCreator private pks(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("id") final java.util.List<Integer> id,
			@com.fasterxml.jackson.annotation.JsonProperty("xml") final org.w3c.dom.Element xml,
			@com.fasterxml.jackson.annotation.JsonProperty("s3") final org.revenj.storage.S3 s3) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.id = id == null ? new java.util.ArrayList<Integer>(4) : id;
		this.xml = xml;
		this.s3 = s3;
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

	
	private org.w3c.dom.Element xml;

	
	@com.fasterxml.jackson.annotation.JsonProperty("xml")
	public org.w3c.dom.Element getXml()  {
		
		return xml;
	}

	
	public pks setXml(final org.w3c.dom.Element value) {
		
		this.xml = value;
		
		return this;
	}

	
	private org.revenj.storage.S3 s3;

	
	@com.fasterxml.jackson.annotation.JsonProperty("s3")
	public org.revenj.storage.S3 getS3()  {
		
		return s3;
	}

	
	public pks setS3(final org.revenj.storage.S3 value) {
		
		this.s3 = value;
		
		return this;
	}

	private transient pks __originalValue;
	
	static {
		gen.model.egzotics.repositories.pksRepository.__setupPersist(
			(aggregates, arg) -> {
				try {
					for (gen.model.egzotics.pks agg : aggregates) {
						 
						agg.URI = gen.model.egzotics.converters.pksConverter.buildURI(arg, agg);
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
		URI = gen.model.egzotics.converters.pksConverter.buildURI(reader, this);
		this.__locator = java.util.Optional.ofNullable(reader.locator);
		this.__originalValue = (pks)this.clone();
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<pks>[] readers, int __index___id, int __index___xml, int __index___s3) {
		
		readers[__index___id] = (item, reader, context) -> { { java.util.List<Integer> __list = org.revenj.postgres.converters.IntConverter.parseCollection(reader, context, false); if(__list != null) {item.id = __list;} else item.id = new java.util.ArrayList<Integer>(4); }; };
		readers[__index___xml] = (item, reader, context) -> { item.xml = org.revenj.postgres.converters.XmlConverter.parse(reader, context); };
		readers[__index___s3] = (item, reader, context) -> { item.s3 = org.revenj.postgres.converters.S3Converter.parse(reader, context); };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<pks>[] readers, int __index__extended_id, int __index__extended_xml, int __index__extended_s3) {
		
		readers[__index__extended_id] = (item, reader, context) -> { { java.util.List<Integer> __list = org.revenj.postgres.converters.IntConverter.parseCollection(reader, context, false); if(__list != null) {item.id = __list;} else item.id = new java.util.ArrayList<Integer>(4); }; };
		readers[__index__extended_xml] = (item, reader, context) -> { item.xml = org.revenj.postgres.converters.XmlConverter.parse(reader, context); };
		readers[__index__extended_s3] = (item, reader, context) -> { item.s3 = org.revenj.postgres.converters.S3Converter.parse(reader, context); };
	}
}
