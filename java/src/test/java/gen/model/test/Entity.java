package gen.model.test;



public class Entity   implements java.io.Serializable {
	
	
	
	public Entity() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.money = java.math.BigDecimal.ZERO.setScale(2);
		this.id = "";
		this.detail = new java.util.HashSet<gen.model.test.Detail>(4);
		this.Compositeid = java.util.UUID.randomUUID();
		this.Index = 0;
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
		final Entity other = (Entity) obj;

		return URI.equals(other.URI);
	}

	public boolean equals(final Entity other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;

		
		if(!(this.money == other.money || this.money != null && other.money != null && this.money.compareTo(other.money) == 0))
			return false;
		if(!(this.id.equals(other.id)))
			return false;
		if(!(this.compositeURI == other.compositeURI || this.compositeURI != null && this.compositeURI.equals(other.compositeURI)))
			return false;
		if(!(this.compositeID == other.compositeID || this.compositeID != null && this.compositeID.equals(other.compositeID)))
			return false;
		if(!((this.detail == other.detail || this.detail != null && this.detail.equals(other.detail))))
			return false;
		if(!(this.Compositeid.equals(other.Compositeid)))
			return false;
		if(!(this.Index == other.Index))
			return false;

		return true;
	}

	@Override
	public String toString() {
		return "Entity(" + URI + ')';
	}
	
	
	public Entity(
			final java.math.BigDecimal money,
			final String id,
			final gen.model.test.Composite composite,
			final java.util.Set<gen.model.test.Detail> detail) {
			
		setMoney(money);
		setId(id);
		setComposite(composite);
		setDetail(detail);
	}

	private static final long serialVersionUID = -2177394063124542140L;
	
	@com.fasterxml.jackson.annotation.JsonCreator private Entity(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("money") final java.math.BigDecimal money,
			@com.fasterxml.jackson.annotation.JsonProperty("id") final String id,
			@com.fasterxml.jackson.annotation.JsonProperty("compositeURI") final String compositeURI,
			@com.fasterxml.jackson.annotation.JsonProperty("compositeID") final java.util.UUID compositeID,
			@com.fasterxml.jackson.annotation.JsonProperty("detail") final java.util.Set<gen.model.test.Detail> detail,
			@com.fasterxml.jackson.annotation.JsonProperty("Compositeid") final java.util.UUID Compositeid,
			@com.fasterxml.jackson.annotation.JsonProperty("Index") final int Index) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.money = money == null ? java.math.BigDecimal.ZERO.setScale(2) : money;
		this.id = id == null ? "" : id;
		this.compositeURI = compositeURI;
		this.compositeID = compositeID;
		this.detail = detail == null ? new java.util.HashSet<gen.model.test.Detail>(4) : detail;
		this.Compositeid = Compositeid == null ? new java.util.UUID(0L, 0L) : Compositeid;
		this.Index = Index;
	}

	
	private java.math.BigDecimal money;

	
	@com.fasterxml.jackson.annotation.JsonProperty("money")
	public java.math.BigDecimal getMoney()  {
		
		return money;
	}

	
	public Entity setMoney(final java.math.BigDecimal value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"money\" cannot be null!");
		this.money = value;
		
		this.money = org.revenj.Guards.setScale(this.money, 2);
		return this;
	}

	
	private String id;

	
	@com.fasterxml.jackson.annotation.JsonProperty("id")
	public String getId()  {
		
		return id;
	}

	
	public Entity setId(final String value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"id\" cannot be null!");
		org.revenj.Guards.checkLength(value, 10);
		this.id = value;
		
		return this;
	}

	
	private gen.model.test.Composite composite;

	
	@com.fasterxml.jackson.annotation.JsonIgnore
	public gen.model.test.Composite getComposite()  {
		
	if (this.compositeURI == null && this.composite != null) this.composite = null;
	
		if (__locator.isPresent() && (composite != null && !composite.getURI().equals(compositeURI) || composite == null && compositeURI != null)) {
			gen.model.test.repositories.CompositeRepository repository = __locator.get().resolve(gen.model.test.repositories.CompositeRepository.class);
			composite = repository.find(compositeURI).orElse(null);
		}
		return composite;
	}

	
	public Entity setComposite(final gen.model.test.Composite value) {
		
		
		if(value != null && value.getURI() == null) throw new IllegalArgumentException("Reference \"test.Composite\" for property \"composite\" must be persisted before it's assigned");
		this.composite = value;
		
		
		if (value == null && this.compositeID != null) {
			this.compositeID = null;
		} else if (value != null) {
			this.compositeID = value.getId();
		}
		this.compositeURI = value != null ? value.getURI() : null;
		return this;
	}

	
	private String compositeURI;

	
	@com.fasterxml.jackson.annotation.JsonProperty("compositeURI")
	public String getCompositeURI()  {
		
		return this.compositeURI;
	}

	
	private java.util.UUID compositeID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("compositeID")
	public java.util.UUID getCompositeID()  {
		
		return compositeID;
	}

	
	private Entity setCompositeID(final java.util.UUID value) {
		
		this.compositeID = value;
		
		return this;
	}

	
	static void __bindTodetail(java.util.function.Consumer<gen.model.test.Entity> binder) {
		__binderdetail = binder;
	}

	private static java.util.function.Consumer<gen.model.test.Entity> __binderdetail;
	
	private java.util.Set<gen.model.test.Detail> detail;

	
	@com.fasterxml.jackson.annotation.JsonProperty("detail")
	public java.util.Set<gen.model.test.Detail> getDetail()  {
		
		return detail;
	}

	
	public Entity setDetail(final java.util.Set<gen.model.test.Detail> value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"detail\" cannot be null!");
		org.revenj.Guards.checkNulls(value);
		this.detail = value;
		
		return this;
	}

	
	private java.util.UUID Compositeid;

	
	@com.fasterxml.jackson.annotation.JsonProperty("Compositeid")
	public java.util.UUID getCompositeid()  {
		
		return Compositeid;
	}

	
	private Entity setCompositeid(final java.util.UUID value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"Compositeid\" cannot be null!");
		this.Compositeid = value;
		
		return this;
	}

	
	private int Index;

	
	@com.fasterxml.jackson.annotation.JsonProperty("Index")
	public int getIndex()  {
		
		return Index;
	}

	
	private Entity setIndex(final int value) {
		
		this.Index = value;
		
		return this;
	}

	
	static {
		gen.model.test.Composite.__bindToentities(parent -> {
			int i = 0;
			for (gen.model.test.Entity e : parent.getEntities()) { 
				e.Compositeid = parent.getId();
				e.Index = i++; 
				__binderdetail.accept(e);
			}
		});
	}
	
	public Entity(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Entity>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<Entity> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.test.converters.EntityConverter.buildURI(reader, Compositeid, Index);
		this.__locator = java.util.Optional.ofNullable(reader.locator);
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Entity>[] readers, int __index___money, int __index___id, int __index___compositeURI, int __index___compositeID, gen.model.test.converters.DetailConverter __converter_detail, int __index___detail, int __index___Compositeid, int __index___Index) {
		
		readers[__index___money] = (item, reader, context) -> { item.money = org.revenj.postgres.converters.DecimalConverter.parse(reader, false); };
		readers[__index___id] = (item, reader, context) -> { item.id = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); };
		readers[__index___compositeURI] = (item, reader, context) -> { item.compositeURI = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); };
		readers[__index___compositeID] = (item, reader, context) -> { item.compositeID = org.revenj.postgres.converters.UuidConverter.parse(reader, true); };
		readers[__index___detail] = (item, reader, context) -> { { java.util.List<gen.model.test.Detail> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, __converter_detail::from); if (__list != null) item.detail = new java.util.HashSet<gen.model.test.Detail>(__list); else item.detail = new java.util.HashSet<gen.model.test.Detail>(4); }; };
		readers[__index___Compositeid] = (item, reader, context) -> { item.Compositeid = org.revenj.postgres.converters.UuidConverter.parse(reader, false); };
		readers[__index___Index] = (item, reader, context) -> { item.Index = org.revenj.postgres.converters.IntConverter.parse(reader); };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Entity>[] readers, int __index__extended_money, int __index__extended_id, int __index__extended_compositeURI, int __index__extended_compositeID, final gen.model.test.converters.DetailConverter __converter_detail, int __index__extended_detail, int __index__extended_Compositeid, int __index__extended_Index) {
		
		readers[__index__extended_money] = (item, reader, context) -> { item.money = org.revenj.postgres.converters.DecimalConverter.parse(reader, false); };
		readers[__index__extended_id] = (item, reader, context) -> { item.id = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); };
		readers[__index__extended_compositeURI] = (item, reader, context) -> { item.compositeURI = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); };
		readers[__index__extended_compositeID] = (item, reader, context) -> { item.compositeID = org.revenj.postgres.converters.UuidConverter.parse(reader, true); };
		readers[__index__extended_detail] = (item, reader, context) -> { { java.util.List<gen.model.test.Detail> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, __converter_detail::fromExtended); if (__list != null) item.detail = new java.util.HashSet<gen.model.test.Detail>(__list); else item.detail = new java.util.HashSet<gen.model.test.Detail>(4); }; };
		readers[__index__extended_Compositeid] = (item, reader, context) -> { item.Compositeid = org.revenj.postgres.converters.UuidConverter.parse(reader, false); };
		readers[__index__extended_Index] = (item, reader, context) -> { item.Index = org.revenj.postgres.converters.IntConverter.parse(reader); };
	}
	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
}
