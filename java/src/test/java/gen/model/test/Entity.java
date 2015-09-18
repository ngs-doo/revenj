package gen.model.test;



public class Entity   implements java.lang.Cloneable, java.io.Serializable {
	
	
	
	public Entity() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.money = java.math.BigDecimal.ZERO.setScale(2);
		this.id = "";
		this.detail1 = new java.util.HashSet<gen.model.test.Detail1>(4);
		this.detail2 = new java.util.HashSet<gen.model.test.Detail2>(4);
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
		if (obj == null || obj instanceof Entity == false)
			return false;
		final Entity other = (Entity) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final Entity other) {
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
		if(!((this.detail1 == other.detail1 || this.detail1 != null && this.detail1.equals(other.detail1))))
			return false;
		if(!((this.detail2 == other.detail2 || this.detail2 != null && this.detail2.equals(other.detail2))))
			return false;
		if(!(this.Compositeid.equals(other.Compositeid)))
			return false;
		if(!(this.Index == other.Index))
			return false;
		return true;
	}

	private Entity(Entity other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.money = other.money;
		this.id = other.id;
		this.compositeURI = other.compositeURI;
		this.compositeID = other.compositeID;
		this.detail1 = new java.util.HashSet<gen.model.test.Detail1>(other.detail1.size());
			if (other.detail1 != null) {
				for (gen.model.test.Detail1 it : other.detail1) {
					this.detail1.add((gen.model.test.Detail1)it.clone());
				}
			};
		this.detail2 = new java.util.HashSet<gen.model.test.Detail2>(other.detail2.size());
			if (other.detail2 != null) {
				for (gen.model.test.Detail2 it : other.detail2) {
					this.detail2.add((gen.model.test.Detail2)it.clone());
				}
			};
		this.Compositeid = other.Compositeid;
		this.Index = other.Index;
	}

	@Override
	public Object clone() {
		return new Entity(this);
	}

	@Override
	public String toString() {
		return "Entity(" + URI + ')';
	}
	
	
	public Entity(
			final java.math.BigDecimal money,
			final String id,
			final gen.model.test.Composite composite,
			final java.util.Set<gen.model.test.Detail1> detail1,
			final java.util.Set<gen.model.test.Detail2> detail2) {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		setMoney(money);
		setId(id);
		setComposite(composite);
		setDetail1(detail1);
		setDetail2(detail2);
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	
	@com.fasterxml.jackson.annotation.JsonCreator private Entity(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("money") final java.math.BigDecimal money,
			@com.fasterxml.jackson.annotation.JsonProperty("id") final String id,
			@com.fasterxml.jackson.annotation.JsonProperty("compositeURI") final String compositeURI,
			@com.fasterxml.jackson.annotation.JsonProperty("compositeID") final java.util.UUID compositeID,
			@com.fasterxml.jackson.annotation.JsonProperty("detail1") final java.util.Set<gen.model.test.Detail1> detail1,
			@com.fasterxml.jackson.annotation.JsonProperty("detail2") final java.util.Set<gen.model.test.Detail2> detail2,
			@com.fasterxml.jackson.annotation.JsonProperty("Compositeid") final java.util.UUID Compositeid,
			@com.fasterxml.jackson.annotation.JsonProperty("Index") final int Index) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.money = money == null ? java.math.BigDecimal.ZERO.setScale(2) : money;
		this.id = id == null ? "" : id;
		this.compositeURI = compositeURI;
		this.compositeID = compositeID;
		this.detail1 = detail1 == null ? new java.util.HashSet<gen.model.test.Detail1>(4) : detail1;
		this.detail2 = detail2 == null ? new java.util.HashSet<gen.model.test.Detail2>(4) : detail2;
		this.Compositeid = Compositeid == null ? new java.util.UUID(0L, 0L) : Compositeid;
		this.Index = Index;
	}

	private static final long serialVersionUID = 8799130154036125147L;
	
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
		
	
		if (__locator.isPresent() && (composite != null && !composite.getURI().equals(compositeURI) || composite == null && compositeURI != null)) {
			gen.model.test.repositories.CompositeRepository repository = __locator.get().resolve(gen.model.test.repositories.CompositeRepository.class);
			composite = repository.find(compositeURI).orElse(null);
		}
	if (this.compositeURI == null && this.composite != null) this.composite = null;
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

	
	static void __bindTodetail1(java.util.function.Consumer<gen.model.test.Entity> binder) {
		__binderdetail1 = binder;
	}

	private static java.util.function.Consumer<gen.model.test.Entity> __binderdetail1;
	
	private java.util.Set<gen.model.test.Detail1> detail1;

	
	@com.fasterxml.jackson.annotation.JsonProperty("detail1")
	public java.util.Set<gen.model.test.Detail1> getDetail1()  {
		
		return detail1;
	}

	
	public Entity setDetail1(final java.util.Set<gen.model.test.Detail1> value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"detail1\" cannot be null!");
		org.revenj.Guards.checkNulls(value);
		this.detail1 = value;
		
		return this;
	}

	
	static void __bindTodetail2(java.util.function.Consumer<gen.model.test.Entity> binder) {
		__binderdetail2 = binder;
	}

	private static java.util.function.Consumer<gen.model.test.Entity> __binderdetail2;
	
	private java.util.Set<gen.model.test.Detail2> detail2;

	
	@com.fasterxml.jackson.annotation.JsonProperty("detail2")
	public java.util.Set<gen.model.test.Detail2> getDetail2()  {
		
		return detail2;
	}

	
	public Entity setDetail2(final java.util.Set<gen.model.test.Detail2> value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"detail2\" cannot be null!");
		org.revenj.Guards.checkNulls(value);
		this.detail2 = value;
		
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
				__binderdetail1.accept(e);
				__binderdetail2.accept(e);
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

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Entity>[] readers, int __index___money, int __index___id, int __index___compositeURI, int __index___compositeID, gen.model.test.converters.Detail1Converter __converter_detail1, int __index___detail1, gen.model.test.converters.Detail2Converter __converter_detail2, int __index___detail2, int __index___Compositeid, int __index___Index) {
		
		readers[__index___money] = (item, reader, context) -> { item.money = org.revenj.postgres.converters.DecimalConverter.parse(reader, false); };
		readers[__index___id] = (item, reader, context) -> { item.id = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); };
		readers[__index___compositeURI] = (item, reader, context) -> { item.compositeURI = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); };
		readers[__index___compositeID] = (item, reader, context) -> { item.compositeID = org.revenj.postgres.converters.UuidConverter.parse(reader, true); };
		readers[__index___detail1] = (item, reader, context) -> { { java.util.List<gen.model.test.Detail1> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, __converter_detail1::from); if (__list != null) {item.detail1 = new java.util.HashSet<gen.model.test.Detail1>(__list);} else item.detail1 = new java.util.HashSet<gen.model.test.Detail1>(4); }; };
		readers[__index___detail2] = (item, reader, context) -> { { java.util.List<gen.model.test.Detail2> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, __converter_detail2::from); if (__list != null) {item.detail2 = new java.util.HashSet<gen.model.test.Detail2>(__list);} else item.detail2 = new java.util.HashSet<gen.model.test.Detail2>(4); }; };
		readers[__index___Compositeid] = (item, reader, context) -> { item.Compositeid = org.revenj.postgres.converters.UuidConverter.parse(reader, false); };
		readers[__index___Index] = (item, reader, context) -> { item.Index = org.revenj.postgres.converters.IntConverter.parse(reader); };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Entity>[] readers, int __index__extended_money, int __index__extended_id, int __index__extended_compositeURI, int __index__extended_compositeID, final gen.model.test.converters.Detail1Converter __converter_detail1, int __index__extended_detail1, final gen.model.test.converters.Detail2Converter __converter_detail2, int __index__extended_detail2, int __index__extended_Compositeid, int __index__extended_Index) {
		
		readers[__index__extended_money] = (item, reader, context) -> { item.money = org.revenj.postgres.converters.DecimalConverter.parse(reader, false); };
		readers[__index__extended_id] = (item, reader, context) -> { item.id = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); };
		readers[__index__extended_compositeURI] = (item, reader, context) -> { item.compositeURI = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); };
		readers[__index__extended_compositeID] = (item, reader, context) -> { item.compositeID = org.revenj.postgres.converters.UuidConverter.parse(reader, true); };
		readers[__index__extended_detail1] = (item, reader, context) -> { { java.util.List<gen.model.test.Detail1> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, __converter_detail1::fromExtended); if (__list != null) {item.detail1 = new java.util.HashSet<gen.model.test.Detail1>(__list);} else item.detail1 = new java.util.HashSet<gen.model.test.Detail1>(4); }; };
		readers[__index__extended_detail2] = (item, reader, context) -> { { java.util.List<gen.model.test.Detail2> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, __converter_detail2::fromExtended); if (__list != null) {item.detail2 = new java.util.HashSet<gen.model.test.Detail2>(__list);} else item.detail2 = new java.util.HashSet<gen.model.test.Detail2>(4); }; };
		readers[__index__extended_Compositeid] = (item, reader, context) -> { item.Compositeid = org.revenj.postgres.converters.UuidConverter.parse(reader, false); };
		readers[__index__extended_Index] = (item, reader, context) -> { item.Index = org.revenj.postgres.converters.IntConverter.parse(reader); };
	}
}
