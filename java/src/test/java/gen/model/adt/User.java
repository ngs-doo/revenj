/*
* Created by DSL Platform
* v1.0.0.29923 
*/

package gen.model.adt;



public class User   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot, com.dslplatform.json.JsonObject {
	
	
	
	public User() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.username = "";
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
		if (obj == null || obj instanceof User == false)
			return false;
		final User other = (User) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final User other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.username.equals(other.username)))
			return false;
		if(!(this.authentication == other.authentication || this.authentication != null && this.authentication.equals(other.authentication)))
			return false;
		return true;
	}

	private User(User other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.username = other.username;
		this.authentication = other.authentication;
		this.__originalValue = other.__originalValue;
	}

	@Override
	public Object clone() {
		return new User(this);
	}

	@Override
	public String toString() {
		return "User(" + URI + ')';
	}
	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	
	@com.fasterxml.jackson.annotation.JsonCreator private User(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("username") final String username,
			@com.fasterxml.jackson.annotation.JsonTypeInfo(use=com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME, include=com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY, property="$type") @com.fasterxml.jackson.annotation.JsonProperty("authentication") final gen.model.adt.Auth authentication) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.username = username == null ? "" : username;
		this.authentication = authentication;
	}

	private static final long serialVersionUID = 5496763966582113604L;
	
	private String username;

	
	@com.fasterxml.jackson.annotation.JsonProperty("username")
	public String getUsername()  {
		
		return username;
	}

	
	public User setUsername(final String value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"username\" cannot be null!");
		this.username = value;
		
		return this;
	}

	
	private gen.model.adt.Auth authentication;

	
	@com.fasterxml.jackson.annotation.JsonProperty("authentication")
	@com.fasterxml.jackson.annotation.JsonTypeInfo(use=com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME, include=com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY, property="$type")
	public gen.model.adt.Auth getAuthentication()  {
		
		return authentication;
	}

	
	public User setAuthentication(final gen.model.adt.Auth value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"authentication\" cannot be null!");
		this.authentication = value;
		
		return this;
	}

	
	static {
		gen.model.adt.repositories.UserRepository.__setupPersist(
			(aggregates, arg) -> {
				try {
					for (gen.model.adt.User agg : aggregates) {
						 
						agg.URI = gen.model.adt.converters.UserConverter.buildURI(arg, agg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(oldAggregates, newAggregates) -> {
				for (int i = 0; i < newAggregates.size(); i++) {
					gen.model.adt.User oldAgg = oldAggregates.get(i);
					gen.model.adt.User newAgg = newAggregates.get(i);
					 
				}
			},
			aggregates -> { 
				for (gen.model.adt.User agg : aggregates) { 
				}
			},
			agg -> { 
				
		User _res = agg.__originalValue;
		agg.__originalValue = (User)agg.clone();
		if (_res != null) {
			return _res;
		}
				return null;
			}
		);
	}
	private transient User __originalValue;
	
	public void serialize(final com.dslplatform.json.JsonWriter sw, final boolean minimal) {
		sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
		if (minimal) {
			__serializeJsonObjectMinimal(this, sw, false);
		} else {
			__serializeJsonObjectFull(this, sw, false);
		}
		sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
	}

	static void __serializeJsonObjectMinimal(final User self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (!(self.username.length() == 0)) {
				sw.writeAscii(",\"username\":", 12);
				sw.writeString(self.username);
			}
		
		if(self.authentication != null) {
			sw.writeAscii(",\"authentication\":{", 19);
			if (self.authentication instanceof gen.model.adt.DigestSecurity) {
				sw.writeAscii("\"$type\":\"adt.DigestSecurity\"");
				gen.model.adt.DigestSecurity.__serializeJsonObjectMinimal((gen.model.adt.DigestSecurity)self.authentication, sw, true);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			} else if (self.authentication instanceof gen.model.adt.Anonymous) {
				sw.writeAscii("\"$type\":\"adt.Anonymous\"");
				gen.model.adt.Anonymous.__serializeJsonObjectMinimal((gen.model.adt.Anonymous)self.authentication, sw, true);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			} else if (self.authentication instanceof gen.model.adt.Token) {
				sw.writeAscii("\"$type\":\"adt.Token\"");
				gen.model.adt.Token.__serializeJsonObjectMinimal((gen.model.adt.Token)self.authentication, sw, true);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			} else if (self.authentication instanceof gen.model.adt.BasicSecurity) {
				sw.writeAscii("\"$type\":\"adt.BasicSecurity\"");
				gen.model.adt.BasicSecurity.__serializeJsonObjectMinimal((gen.model.adt.BasicSecurity)self.authentication, sw, true);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			}
		}
	}

	static void __serializeJsonObjectFull(final User self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			sw.writeAscii(",\"username\":", 12);
			sw.writeString(self.username);
		
		
		if(self.authentication != null) {
			sw.writeAscii(",\"authentication\":{", 19);
			if (self.authentication instanceof gen.model.adt.DigestSecurity) {
				sw.writeAscii("\"$type\":\"adt.DigestSecurity\"");
				gen.model.adt.DigestSecurity.__serializeJsonObjectFull((gen.model.adt.DigestSecurity)self.authentication, sw, true);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			} else if (self.authentication instanceof gen.model.adt.Anonymous) {
				sw.writeAscii("\"$type\":\"adt.Anonymous\"");
				gen.model.adt.Anonymous.__serializeJsonObjectFull((gen.model.adt.Anonymous)self.authentication, sw, true);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			} else if (self.authentication instanceof gen.model.adt.Token) {
				sw.writeAscii("\"$type\":\"adt.Token\"");
				gen.model.adt.Token.__serializeJsonObjectFull((gen.model.adt.Token)self.authentication, sw, true);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			} else if (self.authentication instanceof gen.model.adt.BasicSecurity) {
				sw.writeAscii("\"$type\":\"adt.BasicSecurity\"");
				gen.model.adt.BasicSecurity.__serializeJsonObjectFull((gen.model.adt.BasicSecurity)self.authentication, sw, true);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			}
		} else {
			sw.writeAscii(",\"authentication\":null", 22);
		}
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<User> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<User>() {
		@Override
		public User deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.adt.User(reader);
		}
	};

	private User(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		String _username_ = "";
		gen.model.adt.Auth _authentication_ = null;
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
					case 1320097209:
						_username_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -1694823839:
						
					if (nextToken == '{') {
						String __val;
						int typeNameHash;
						reader.getNextToken();
					typeNameHash = reader.fillName();
					if (typeNameHash != 362775563) throw new java.io.IOException("Expecting '$type' at position " + reader.positionInStream());
					reader.getNextToken();
					int __calcHash = reader.calcHash();
					nextToken = reader.getNextToken();
					if (nextToken == ',') reader.getNextToken();
					switch (__calcHash) { 
						case -1935686960 : _authentication_ = gen.model.adt.DigestSecurity.JSON_READER.deserialize(reader); break;
						case 2008568515 : _authentication_ = gen.model.adt.Anonymous.JSON_READER.deserialize(reader); break;
						case -519644629 : _authentication_ = gen.model.adt.Token.JSON_READER.deserialize(reader); break;
						case 1117328874 : _authentication_ = gen.model.adt.BasicSecurity.JSON_READER.deserialize(reader); break;
						default: throw new java.io.IOException("Unknown mixin type: '" + reader.getLastName() + "' at position " + reader.positionInStream());
					}
					
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '{' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
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
					case 1320097209:
						_username_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -1694823839:
						
					if (nextToken == '{') {
						String __val;
						int typeNameHash;
						reader.getNextToken();
					typeNameHash = reader.fillName();
					if (typeNameHash != 362775563) throw new java.io.IOException("Expecting '$type' at position " + reader.positionInStream());
					reader.getNextToken();
					int __calcHash = reader.calcHash();
					nextToken = reader.getNextToken();
					if (nextToken == ',') reader.getNextToken();
					switch (__calcHash) { 
						case -1935686960 : _authentication_ = gen.model.adt.DigestSecurity.JSON_READER.deserialize(reader); break;
						case 2008568515 : _authentication_ = gen.model.adt.Anonymous.JSON_READER.deserialize(reader); break;
						case -519644629 : _authentication_ = gen.model.adt.Token.JSON_READER.deserialize(reader); break;
						case 1117328874 : _authentication_ = gen.model.adt.BasicSecurity.JSON_READER.deserialize(reader); break;
						default: throw new java.io.IOException("Unknown mixin type: '" + reader.getLastName() + "' at position " + reader.positionInStream());
					}
					
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '{' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
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
		this.username = _username_;
		if(_authentication_ == null) throw new java.io.IOException("In entity adt.User, property authentication can't be null");
		this.authentication = _authentication_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.adt.User(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public User(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<User>[] readers) throws java.io.IOException {
		this.__locator = reader.getLocator();
		for (org.revenj.postgres.ObjectConverter.Reader<User> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.adt.converters.UserConverter.buildURI(reader, this);
		this.__originalValue = (User)this.clone();
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<User>[] readers, int __index___username, gen.model.adt.converters.AuthConverter __converter_authentication, int __index___authentication) {
		
		readers[__index___username] = (item, reader, context) -> { item.username = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
		readers[__index___authentication] = (item, reader, context) -> { item.authentication = __converter_authentication.from(reader, context); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<User>[] readers, int __index__extended_username, final gen.model.adt.converters.AuthConverter __converter_authentication, int __index__extended_authentication) {
		
		readers[__index__extended_username] = (item, reader, context) -> { item.username = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
		readers[__index__extended_authentication] = (item, reader, context) -> { item.authentication = __converter_authentication.fromExtended(reader, context); return item; };
	}
	
	
	public User(
			final String username,
			final gen.model.adt.Auth authentication) {
			
		setUsername(username);
		setAuthentication(authentication);
		this.URI = this.username;
	}

}
