package gen.model.mixinReference;



public class Author   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot {
	
	
	
	public Author() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.ID = 0;
		this.name = "";
		this.setPerson(new gen.model.mixinReference.Person());
		this.setRezident(new gen.model.mixinReference.Resident());
		this.children = new gen.model.mixinReference.Child[] { };
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
		if (obj == null || obj instanceof Author == false)
			return false;
		final Author other = (Author) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final Author other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.ID == other.ID))
			return false;
		if(!(this.name.equals(other.name)))
			return false;
		if(!(this.person == other.person || this.person != null && this.person.equals(other.person)))
			return false;
		if(!(this.rezident == other.rezident || this.rezident != null && this.rezident.equals(other.rezident)))
			return false;
		if(!(this.rezidentID.equals(other.rezidentID)))
			return false;
		if(!(java.util.Arrays.equals(this.children, other.children)))
			return false;
		return true;
	}

	private Author(Author other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.ID = other.ID;
		this.name = other.name;
		this.person = other.person == null ? null : (gen.model.mixinReference.Person)(other.person.clone());
		this.rezident = other.rezident == null ? null : (gen.model.mixinReference.Resident)(other.rezident.clone());
		this.rezidentID = other.rezidentID;
		this.children = new gen.model.mixinReference.Child[other.children.length];
			if (other.children != null) {
				for (int _i = 0; _i < other.children.length; _i++) {
					this.children[_i] = (gen.model.mixinReference.Child)other.children[_i].clone();
				}
			};
		this.__originalValue = other.__originalValue;
	}

	@Override
	public Object clone() {
		return new Author(this);
	}

	@Override
	public String toString() {
		return "Author(" + URI + ')';
	}
	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	
	@com.fasterxml.jackson.annotation.JsonCreator private Author(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("ID") final int ID,
			@com.fasterxml.jackson.annotation.JsonProperty("name") final String name,
			@com.fasterxml.jackson.annotation.JsonProperty("person") final gen.model.mixinReference.Person person,
			@com.fasterxml.jackson.annotation.JsonProperty("rezident") final gen.model.mixinReference.Resident rezident,
			@com.fasterxml.jackson.annotation.JsonProperty("rezidentID") final java.util.UUID rezidentID,
			@com.fasterxml.jackson.annotation.JsonProperty("children") final gen.model.mixinReference.Child[] children) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.ID = ID;
		this.name = name == null ? "" : name;
		this.person = person;
		this.rezident = rezident;
		this.rezidentID = rezidentID == null ? new java.util.UUID(0L, 0L) : rezidentID;
		this.children = children == null ? new gen.model.mixinReference.Child[] { } : children;
	}

	private static final long serialVersionUID = 321393438136777272L;
	
	private int ID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ID")
	public int getID()  {
		
		return ID;
	}

	
	private Author setID(final int value) {
		
		this.ID = value;
		
		return this;
	}

	
	static {
		gen.model.mixinReference.repositories.AuthorRepository.__setupSequenceID((items, connection) -> {
			try (java.sql.PreparedStatement st = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT nextval('\"mixinReference\".\"Author_ID_seq\"'::regclass)::int FROM generate_series(1, ?)")) {
				st.setInt(1, items.size());
				try (java.sql.ResultSet rs = st.executeQuery()) {
					java.util.Iterator<Author> iterator = items.iterator();
					while (rs.next()) {
						iterator.next().setID(rs.getInt(1));
					}
				}
			} catch (java.sql.SQLException e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	private String name;

	
	@com.fasterxml.jackson.annotation.JsonProperty("name")
	public String getName()  {
		
		return name;
	}

	
	public Author setName(final String value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"name\" cannot be null!");
		this.name = value;
		
		return this;
	}

	
	static void __bindToperson(java.util.function.Consumer<gen.model.mixinReference.Author> binder) {
		__binderperson = binder;
	}

	private static java.util.function.Consumer<gen.model.mixinReference.Author> __binderperson;
	
	private gen.model.mixinReference.Person person;

	
	@com.fasterxml.jackson.annotation.JsonProperty("person")
	public gen.model.mixinReference.Person getPerson()  {
		
		return person;
	}

	
	public Author setPerson(final gen.model.mixinReference.Person value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"person\" cannot be null!");
		this.person = value;
		
		return this;
	}

	
	private gen.model.mixinReference.Resident rezident;

	
	@com.fasterxml.jackson.annotation.JsonProperty("rezident")
	public gen.model.mixinReference.Resident getRezident()  {
		
		return rezident;
	}

	
	public Author setRezident(final gen.model.mixinReference.Resident value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"rezident\" cannot be null!");
		this.rezident = value;
		
		
		this.rezidentID = value.getId();
		return this;
	}

	
	private java.util.UUID rezidentID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("rezidentID")
	public java.util.UUID getRezidentID()  {
		
		return rezidentID;
	}

	
	private Author setRezidentID(final java.util.UUID value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"rezidentID\" cannot be null!");
		this.rezidentID = value;
		
		return this;
	}

	
	static void __bindTochildren(java.util.function.Consumer<gen.model.mixinReference.Author> binder) {
		__binderchildren = binder;
	}

	private static java.util.function.Consumer<gen.model.mixinReference.Author> __binderchildren;
	
	private gen.model.mixinReference.Child[] children;

	
	@com.fasterxml.jackson.annotation.JsonProperty("children")
	public gen.model.mixinReference.Child[] getChildren()  {
		
		return children;
	}

	
	public Author setChildren(final gen.model.mixinReference.Child[] value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"children\" cannot be null!");
		org.revenj.Guards.checkNulls(value);
		this.children = value;
		
		return this;
	}

	
	static {
		gen.model.mixinReference.repositories.AuthorRepository.__setupPersist(
			(aggregates, arg) -> {
				try {
					for (gen.model.mixinReference.Author agg : aggregates) {
						
						__binderperson.accept(agg);
						__binderchildren.accept(agg); 
						agg.URI = gen.model.mixinReference.converters.AuthorConverter.buildURI(arg, agg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(oldAggregates, newAggregates) -> {
				for (int i = 0; i < newAggregates.size(); i++) {
					gen.model.mixinReference.Author oldAgg = oldAggregates.get(i);
					gen.model.mixinReference.Author newAgg = newAggregates.get(i);
					
					__binderperson.accept(newAgg);
					__binderchildren.accept(newAgg); 
				}
			},
			aggregates -> { 
				for (gen.model.mixinReference.Author agg : aggregates) { 
				}
			},
			agg -> { 
				
		Author _res = agg.__originalValue;
		agg.__originalValue = (Author)agg.clone();
		if (_res != null) {
			return _res;
		}
				return null;
			}
		);
	}
	private transient Author __originalValue;
	
	public Author(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Author>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<Author> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.mixinReference.converters.AuthorConverter.buildURI(reader, this);
		this.__locator = java.util.Optional.ofNullable(reader.locator);
		this.__originalValue = (Author)this.clone();
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Author>[] readers, int __index___ID, int __index___name, gen.model.mixinReference.converters.PersonConverter __converter_person, int __index___person, gen.model.mixinReference.converters.ResidentConverter __converter_rezident, int __index___rezident, int __index___rezidentID, gen.model.mixinReference.converters.ChildConverter __converter_children, int __index___children) {
		
		readers[__index___ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); };
		readers[__index___name] = (item, reader, context) -> { item.name = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); };
		readers[__index___person] = (item, reader, context) -> { item.person = __converter_person.from(reader, context); };
		readers[__index___rezident] = (item, reader, context) -> { item.rezident = __converter_rezident.from(reader, context); };
		readers[__index___rezidentID] = (item, reader, context) -> { item.rezidentID = org.revenj.postgres.converters.UuidConverter.parse(reader, false); };
		readers[__index___children] = (item, reader, context) -> { { java.util.List<gen.model.mixinReference.Child> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, __converter_children::from); if (__list != null) {item.children = __list.toArray(new gen.model.mixinReference.Child[__list.size()]);} else item.children = new gen.model.mixinReference.Child[] { }; }; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Author>[] readers, int __index__extended_ID, int __index__extended_name, final gen.model.mixinReference.converters.PersonConverter __converter_person, int __index__extended_person, final gen.model.mixinReference.converters.ResidentConverter __converter_rezident, int __index__extended_rezident, int __index__extended_rezidentID, final gen.model.mixinReference.converters.ChildConverter __converter_children, int __index__extended_children) {
		
		readers[__index__extended_ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); };
		readers[__index__extended_name] = (item, reader, context) -> { item.name = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); };
		readers[__index__extended_person] = (item, reader, context) -> { item.person = __converter_person.fromExtended(reader, context); };
		readers[__index__extended_rezident] = (item, reader, context) -> { item.rezident = __converter_rezident.fromExtended(reader, context); };
		readers[__index__extended_rezidentID] = (item, reader, context) -> { item.rezidentID = org.revenj.postgres.converters.UuidConverter.parse(reader, false); };
		readers[__index__extended_children] = (item, reader, context) -> { { java.util.List<gen.model.mixinReference.Child> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, __converter_children::fromExtended); if (__list != null) {item.children = __list.toArray(new gen.model.mixinReference.Child[__list.size()]);} else item.children = new gen.model.mixinReference.Child[] { }; }; };
	}
	
	
	public Author(
			final String name,
			final gen.model.mixinReference.Person person,
			final gen.model.mixinReference.Resident rezident,
			final gen.model.mixinReference.Child[] children) {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		setName(name);
		setPerson(person);
		setRezident(rezident);
		setChildren(children);
	}

}
