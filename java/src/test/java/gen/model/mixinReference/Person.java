package gen.model.mixinReference;



public class Person   implements java.lang.Cloneable, java.io.Serializable, com.dslplatform.json.JsonObject {
	
	
	
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
			
		setBirth(birth);
		this.URI = java.lang.Integer.toString(this.AuthorID);
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	private static final long serialVersionUID = 4019989449839427299L;
	
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
	
	public void serialize(final com.dslplatform.json.JsonWriter sw, final boolean minimal) {
		sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
		if (minimal) {
			__serializeJsonObjectMinimal(this, sw, false);
		} else {
			__serializeJsonObjectFull(this, sw, false);
		}
		sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
	}

	static void __serializeJsonObjectMinimal(final Person self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (!(self.birth.getYear() == 1 && self.birth.getMonthValue() == 1 && self.birth.getDayOfMonth() == 1)) {
				sw.writeAscii(",\"birth\":", 9);
				org.revenj.json.JavaTimeConverter.serialize(self.birth, sw);
			}
		
			if (self.yearOfBirth != 0) {
				sw.writeAscii(",\"yearOfBirth\":", 15);
				com.dslplatform.json.NumberConverter.serialize(self.yearOfBirth, sw);
			}
		
			if (self.dayOfBirth != 0) {
				sw.writeAscii(",\"dayOfBirth\":", 14);
				com.dslplatform.json.NumberConverter.serialize(self.dayOfBirth, sw);
			}
		
			if (self.bornOnOddDay) {
				sw.writeAscii(",\"bornOnOddDay\":true");
			}
		
			if (self.bornOnEvenDay) {
				sw.writeAscii(",\"bornOnEvenDay\":true");
			}
		
			if (self.AuthorID != 0) {
				sw.writeAscii(",\"AuthorID\":", 12);
				com.dslplatform.json.NumberConverter.serialize(self.AuthorID, sw);
			}
	}

	static void __serializeJsonObjectFull(final Person self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			sw.writeAscii(",\"birth\":", 9);
			org.revenj.json.JavaTimeConverter.serialize(self.birth, sw);
		
			
			sw.writeAscii(",\"yearOfBirth\":", 15);
			com.dslplatform.json.NumberConverter.serialize(self.yearOfBirth, sw);
		
			
			sw.writeAscii(",\"dayOfBirth\":", 14);
			com.dslplatform.json.NumberConverter.serialize(self.dayOfBirth, sw);
		
			if (self.bornOnOddDay) {
				sw.writeAscii(",\"bornOnOddDay\":true");
			} else {
				sw.writeAscii(",\"bornOnOddDay\":false");
			}
		
			if (self.bornOnEvenDay) {
				sw.writeAscii(",\"bornOnEvenDay\":true");
			} else {
				sw.writeAscii(",\"bornOnEvenDay\":false");
			}
		
			
			sw.writeAscii(",\"AuthorID\":", 12);
			com.dslplatform.json.NumberConverter.serialize(self.AuthorID, sw);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<Person> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<Person>() {
		@Override
		public Person deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.mixinReference.Person(reader);
		}
	};

	private Person(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		java.time.LocalDate _birth_ = org.revenj.Utils.MIN_LOCAL_DATE;
		int _yearOfBirth_ = 0;
		int _dayOfBirth_ = 0;
		boolean _bornOnOddDay_ = false;
		boolean _bornOnEvenDay_ = false;
		int _AuthorID_ = 0;
		byte nextToken = reader.last();
		if(nextToken != '}') {
			int nameHash = reader.fillName();
			nextToken = reader.getNextToken();
			if(nextToken == 'n') {
				if (reader.wasNull()) {
					nextToken = reader.getNextToken();
				} else {
					throw new java.io.IOException("Expecting 'u' (as null) at position " + reader.positionInStream() + ". Found " + (char)nextToken);
				}
			} else {
				switch(nameHash) {
					
					case 2053729053:
						_URI_ = reader.readString();
				nextToken = reader.getNextToken();
						break;
					case 558509118:
						_birth_ = org.revenj.json.JavaTimeConverter.deserializeLocalDate(reader);
					nextToken = reader.getNextToken();
						break;
					case 2018960198:
						_yearOfBirth_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case 1594185545:
						_dayOfBirth_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case 779623722:
						_bornOnOddDay_ = com.dslplatform.json.BoolConverter.deserialize(reader); nextToken = reader.getNextToken();
						break;
					case 1988452239:
						_bornOnEvenDay_ = com.dslplatform.json.BoolConverter.deserialize(reader); nextToken = reader.getNextToken();
						break;
					case 23797067:
						_AuthorID_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					default:
						nextToken = reader.skip();
						break;
				}
			}
			while (nextToken == ',') {
				nextToken = reader.getNextToken();
				nameHash = reader.fillName();
				nextToken = reader.getNextToken();
				if(nextToken == 'n') {
					if (reader.wasNull()) {
						nextToken = reader.getNextToken();
						continue;
					} else {
						throw new java.io.IOException("Expecting 'u' (as null) at position " + reader.positionInStream() + ". Found " + (char)nextToken);
					}
				}
				switch(nameHash) {
					
					case 2053729053:
						_URI_ = reader.readString();
				nextToken = reader.getNextToken();
						break;
					case 558509118:
						_birth_ = org.revenj.json.JavaTimeConverter.deserializeLocalDate(reader);
					nextToken = reader.getNextToken();
						break;
					case 2018960198:
						_yearOfBirth_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case 1594185545:
						_dayOfBirth_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case 779623722:
						_bornOnOddDay_ = com.dslplatform.json.BoolConverter.deserialize(reader); nextToken = reader.getNextToken();
						break;
					case 1988452239:
						_bornOnEvenDay_ = com.dslplatform.json.BoolConverter.deserialize(reader); nextToken = reader.getNextToken();
						break;
					case 23797067:
						_AuthorID_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					default:
						nextToken = reader.skip();
						break;
				}
			}
			if (nextToken != '}') {
				throw new java.io.IOException("Expecting '}' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
			}
		}
		
		this.URI = _URI_;
		this.birth = _birth_;
		this.yearOfBirth = _yearOfBirth_;
		this.dayOfBirth = _dayOfBirth_;
		this.bornOnOddDay = _bornOnOddDay_;
		this.bornOnEvenDay = _bornOnEvenDay_;
		this.AuthorID = _AuthorID_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.mixinReference.Person(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public Person(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Person>[] readers) throws java.io.IOException {
		this.__locator = java.util.Optional.ofNullable(reader.locator);
		for (org.revenj.postgres.ObjectConverter.Reader<Person> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.mixinReference.converters.PersonConverter.buildURI(reader, this);
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Person>[] readers, int __index___birth, int __index___dayOfBirth, int __index___bornOnEvenDay, int __index___AuthorID) {
		
		readers[__index___birth] = (item, reader, context) -> { item.birth = org.revenj.postgres.converters.DateConverter.parse(reader, false); return item; };
		readers[__index___AuthorID] = (item, reader, context) -> { item.AuthorID = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Person>[] readers, int __index__extended_birth, int __index__extended_dayOfBirth, int __index__extended_bornOnEvenDay, int __index__extended_AuthorID) {
		
		readers[__index__extended_birth] = (item, reader, context) -> { item.birth = org.revenj.postgres.converters.DateConverter.parse(reader, false); return item; };
		readers[__index__extended_AuthorID] = (item, reader, context) -> { item.AuthorID = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
	}
}
