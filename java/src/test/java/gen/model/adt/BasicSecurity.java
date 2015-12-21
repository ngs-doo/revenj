/*
* Created by DSL Platform
* v1.0.0.29923 
*/

package gen.model.adt;


@com.fasterxml.jackson.annotation.JsonTypeName("adt.BasicSecurity")
public final class BasicSecurity   implements java.lang.Cloneable, java.io.Serializable, gen.model.adt.Auth<gen.model.adt.BasicSecurity>, com.dslplatform.json.JsonObject {
	
	
	
	public BasicSecurity(
			final String username,
			final String password) {
			
		setUsername(username);
		setPassword(password);
	}

	
	
	public BasicSecurity() {
			
		this.username = "";
		this.password = "";
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + 1522790071;
		result = prime * result + (this.username.hashCode());
		result = prime * result + (this.password.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof BasicSecurity))
			return false;
		return deepEquals((BasicSecurity) obj);
	}

	public boolean deepEquals(final BasicSecurity other) {
		if (other == null)
			return false;
		
		if(!(this.username.equals(other.username)))
			return false;
		if(!(this.password.equals(other.password)))
			return false;
		return true;
	}

	private BasicSecurity(BasicSecurity other) {
		
		this.username = other.username;
		this.password = other.password;
	}

	@Override
	public Object clone() {
		return new BasicSecurity(this);
	}

	@Override
	public String toString() {
		return "BasicSecurity(" + username + ',' + password + ')';
	}
	
	@com.fasterxml.jackson.annotation.JsonCreator private BasicSecurity(
			@com.fasterxml.jackson.annotation.JsonProperty("_helper") final boolean _helper ,
			@com.fasterxml.jackson.annotation.JsonProperty("username") final String username,
			@com.fasterxml.jackson.annotation.JsonProperty("password") final String password) {
		
		this.username = username == null ? "" : username;
		this.password = password == null ? "" : password;
	}

	private static final long serialVersionUID = 4102188861482037953L;
	
	private String username;

	
	@com.fasterxml.jackson.annotation.JsonProperty("username")
	public String getUsername()  {
		
		return username;
	}

	
	public BasicSecurity setUsername(final String value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"username\" cannot be null!");
		this.username = value;
		
		return this;
	}

	
	private String password;

	
	@com.fasterxml.jackson.annotation.JsonProperty("password")
	public String getPassword()  {
		
		return password;
	}

	
	public BasicSecurity setPassword(final String value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"password\" cannot be null!");
		this.password = value;
		
		return this;
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

	static public void __serializeJsonObjectMinimal(final BasicSecurity self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			if (!(self.username.length() == 0)) {
			if(hasWrittenProperty) sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
			hasWrittenProperty = true;
				sw.writeAscii("\"username\":", 11);
				sw.writeString(self.username);
			}
		
			if (!(self.password.length() == 0)) {
			if(hasWrittenProperty) sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
			hasWrittenProperty = true;
				sw.writeAscii("\"password\":", 11);
				sw.writeString(self.password);
			}
	}

	static public void __serializeJsonObjectFull(final BasicSecurity self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			
			if(hasWrittenProperty) sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
			sw.writeAscii("\"username\":", 11);
			sw.writeString(self.username);
		
			
			sw.writeAscii(",\"password\":", 12);
			sw.writeString(self.password);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<BasicSecurity> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<BasicSecurity>() {
		@Override
		public BasicSecurity deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.adt.BasicSecurity(reader);
		}
	};

	private BasicSecurity(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _username_ = "";
		String _password_ = "";
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
					
					case 1320097209:
						_username_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case 910909208:
						_password_ = com.dslplatform.json.StringConverter.deserialize(reader);
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
					
					case 1320097209:
						_username_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case 910909208:
						_password_ = com.dslplatform.json.StringConverter.deserialize(reader);
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
		
		this.username = _username_;
		this.password = _password_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.adt.BasicSecurity(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public BasicSecurity(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<BasicSecurity>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<BasicSecurity> rdr : readers) {
			rdr.read(this, reader, context);
		}
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<BasicSecurity>[] readers, int __index___username, int __index___password) {
		
		readers[__index___username] = (item, reader, context) -> { item.username = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
		readers[__index___password] = (item, reader, context) -> { item.password = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<BasicSecurity>[] readers, int __index__extended_username, int __index__extended_password) {
		
		readers[__index__extended_username] = (item, reader, context) -> { item.username = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
		readers[__index__extended_password] = (item, reader, context) -> { item.password = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
	}
}
