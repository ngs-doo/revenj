package gen.model.mixinReference;



public class Person   implements java.lang.Cloneable, java.io.Serializable {
	
	
	
	public Person() {
			
		this.birth = java.time.LocalDate.now();
		this.AuthorID = 0;
		this.URI = java.lang.Integer.toString(this.AuthorID);
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
		if (obj == null || obj instanceof Person == false)
			return false;
		final Person other = (Person) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final Person other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.birth.equals(other.birth)))
			return false;
		if(!(this.AuthorID == other.AuthorID))
			return false;
		return true;
	}

	private Person(Person other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.birth = other.birth;
		this.AuthorID = other.AuthorID;
	}

	@Override
	public Object clone() {
		return new Person(this);
	}

	@Override
	public String toString() {
		return "Person(" + URI + ')';
	}
	
	
	public Person(
			final java.time.LocalDate birth) {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		setBirth(birth);
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	private static final long serialVersionUID = 8392175065152126156L;
	
	@com.fasterxml.jackson.annotation.JsonCreator private Person(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("birth") final java.time.LocalDate birth,
			@com.fasterxml.jackson.annotation.JsonProperty("yearOfBirth") final int yearOfBirth,
			@com.fasterxml.jackson.annotation.JsonProperty("dayOfBirth") final int dayOfBirth,
			@com.fasterxml.jackson.annotation.JsonProperty("bornOnOddDay") final boolean bornOnOddDay,
			@com.fasterxml.jackson.annotation.JsonProperty("bornOnEvenDay") final boolean bornOnEvenDay,
			@com.fasterxml.jackson.annotation.JsonProperty("AuthorID") final int AuthorID) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.birth = birth == null ? org.revenj.Utils.MIN_LOCAL_DATE : birth;
		this.yearOfBirth = yearOfBirth;
		this.dayOfBirth = dayOfBirth;
		this.bornOnOddDay = bornOnOddDay;
		this.bornOnEvenDay = bornOnEvenDay;
		this.AuthorID = AuthorID;
	}

	
	private java.time.LocalDate birth;

	
	@com.fasterxml.jackson.annotation.JsonProperty("birth")
	public java.time.LocalDate getBirth()  {
		
		return birth;
	}

	
	public Person setBirth(final java.time.LocalDate value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"birth\" cannot be null!");
		this.birth = value;
		
		return this;
	}

	
	private int yearOfBirth;

	
	@com.fasterxml.jackson.annotation.JsonProperty("yearOfBirth")
	public int getYearOfBirth()  {
		
		this.yearOfBirth = __calculated_yearOfBirth.apply(this);
		return this.yearOfBirth;
	}

	private static final java.util.function.Function<gen.model.mixinReference.Person, Integer> __calculated_yearOfBirth = it -> it.getBirth().getYear();
	
	private int dayOfBirth;

	
	@com.fasterxml.jackson.annotation.JsonProperty("dayOfBirth")
	public int getDayOfBirth()  {
		
		this.dayOfBirth = __calculated_dayOfBirth.apply(this);
		return this.dayOfBirth;
	}

	private static final java.util.function.Function<gen.model.mixinReference.Person, Integer> __calculated_dayOfBirth = it -> it.getBirth().getDayOfYear();
	
	private boolean bornOnOddDay;

	
	@com.fasterxml.jackson.annotation.JsonProperty("bornOnOddDay")
	public boolean getBornOnOddDay()  {
		
		this.bornOnOddDay = __calculated_bornOnOddDay.apply(this);
		return this.bornOnOddDay;
	}

	private static final java.util.function.Function<gen.model.mixinReference.Person, Boolean> __calculated_bornOnOddDay = it -> ( (it.getBirth().getDayOfYear() % 2) == 1);
	
	private boolean bornOnEvenDay;

	
	@com.fasterxml.jackson.annotation.JsonProperty("bornOnEvenDay")
	public boolean getBornOnEvenDay()  {
		
		this.bornOnEvenDay = __calculated_bornOnEvenDay.apply(this);
		return this.bornOnEvenDay;
	}

	private static final java.util.function.Function<gen.model.mixinReference.Person, Boolean> __calculated_bornOnEvenDay = it -> ( (it.getBirth().getDayOfYear() % 2) == 0);
	
	private int AuthorID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("AuthorID")
	public int getAuthorID()  {
		
		return AuthorID;
	}

	
	private Person setAuthorID(final int value) {
		
		this.AuthorID = value;
		
		return this;
	}

	
	static {
		gen.model.mixinReference.Author.__bindToperson(parent -> {
			int i = 0;
			gen.model.mixinReference.Person _r = parent.getPerson();
			if (_r != null) {
				_r.AuthorID = parent.getID();
			}
		});
	}
	
	public Person(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Person>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<Person> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.mixinReference.converters.PersonConverter.buildURI(reader, this);
		this.__locator = java.util.Optional.ofNullable(reader.locator);
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Person>[] readers, int __index___birth, int __index___dayOfBirth, int __index___bornOnEvenDay, int __index___AuthorID) {
		
		readers[__index___birth] = (item, reader, context) -> { item.birth = org.revenj.postgres.converters.DateConverter.parse(reader, false); };
		readers[__index___AuthorID] = (item, reader, context) -> { item.AuthorID = org.revenj.postgres.converters.IntConverter.parse(reader); };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Person>[] readers, int __index__extended_birth, int __index__extended_dayOfBirth, int __index__extended_bornOnEvenDay, int __index__extended_AuthorID) {
		
		readers[__index__extended_birth] = (item, reader, context) -> { item.birth = org.revenj.postgres.converters.DateConverter.parse(reader, false); };
		readers[__index__extended_AuthorID] = (item, reader, context) -> { item.AuthorID = org.revenj.postgres.converters.IntConverter.parse(reader); };
	}
}
