package gen.model.adt;


@com.fasterxml.jackson.annotation.JsonTypeName("adt.DigestSecurity")
public final class DigestSecurity   implements java.lang.Cloneable, java.io.Serializable, gen.model.adt.Auth<gen.model.adt.DigestSecurity>, com.dslplatform.json.JsonObject {
	
	
	
	public DigestSecurity(
			final String username,
			final byte[] passwordHash) {
			
		setUsername(username);
		setPasswordHash(passwordHash);
	}

	
	
	public DigestSecurity() {
			
		this.username = "";
		this.passwordHash = org.revenj.Utils.EMPTY_BINARY;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + 461452005;
		result = prime * result + (this.username.hashCode());
		result = prime * result + (java.util.Arrays.hashCode(this.passwordHash));
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof DigestSecurity))
			return false;
		return deepEquals((DigestSecurity) obj);
	}

	public boolean deepEquals(final DigestSecurity other) {
		if (other == null)
			return false;
		
		if(!(this.username.equals(other.username)))
			return false;
		if(!(java.util.Arrays.equals(this.passwordHash, other.passwordHash)))
			return false;
		return true;
	}

	private DigestSecurity(DigestSecurity other) {
		
		this.username = other.username;
		this.passwordHash = other.passwordHash != null ? java.util.Arrays.copyOf(other.passwordHash, other.passwordHash.length) : null;
	}

	@Override
	public Object clone() {
		return new DigestSecurity(this);
	}

	@Override
	public String toString() {
		return "DigestSecurity(" + username + ',' + passwordHash + ')';
	}
	
	@com.fasterxml.jackson.annotation.JsonCreator private DigestSecurity(
			@com.fasterxml.jackson.annotation.JsonProperty("_helper") final boolean _helper ,
			@com.fasterxml.jackson.annotation.JsonProperty("username") final String username,
			@com.fasterxml.jackson.annotation.JsonProperty("passwordHash") final byte[] passwordHash) {
		
		this.username = username == null ? "" : username;
		this.passwordHash = passwordHash == null ? org.revenj.Utils.EMPTY_BINARY : passwordHash;
	}

	private static final long serialVersionUID = 1536490670658259921L;
	
	private String username;

	
	@com.fasterxml.jackson.annotation.JsonProperty("username")
	public String getUsername()  {
		
		return username;
	}

	
	public DigestSecurity setUsername(final String value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"username\" cannot be null!");
		this.username = value;
		
		return this;
	}

	
	private byte[] passwordHash;

	
	@com.fasterxml.jackson.annotation.JsonProperty("passwordHash")
	public byte[] getPasswordHash()  {
		
		return passwordHash;
	}

	
	public DigestSecurity setPasswordHash(final byte[] value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"passwordHash\" cannot be null!");
		this.passwordHash = value;
		
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

	static public void __serializeJsonObjectMinimal(final DigestSecurity self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			if (!(self.username.length() == 0)) {
			if(hasWrittenProperty) sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
			hasWrittenProperty = true;
				sw.writeAscii("\"username\":", 11);
				sw.writeString(self.username);
			}
		
			if (!(self.passwordHash.length == 0)) {
			if(hasWrittenProperty) sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
			hasWrittenProperty = true;
				sw.writeAscii("\"passwordHash\":", 15);
				com.dslplatform.json.BinaryConverter.serialize(self.passwordHash, sw);
			}
	}

	static public void __serializeJsonObjectFull(final DigestSecurity self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			
			if(hasWrittenProperty) sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
			sw.writeAscii("\"username\":", 11);
			sw.writeString(self.username);
		
			
			sw.writeAscii(",\"passwordHash\":", 16);
			com.dslplatform.json.BinaryConverter.serialize(self.passwordHash, sw);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<DigestSecurity> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<DigestSecurity>() {
		@Override
		public DigestSecurity deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.adt.DigestSecurity(reader);
		}
	};

	private DigestSecurity(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _username_ = "";
		byte[] _passwordHash_ = org.revenj.Utils.EMPTY_BINARY;
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
					case -949286552:
						_passwordHash_ = com.dslplatform.json.BinaryConverter.deserialize(reader);
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
					case -949286552:
						_passwordHash_ = com.dslplatform.json.BinaryConverter.deserialize(reader);
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
		this.passwordHash = _passwordHash_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.adt.DigestSecurity(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public DigestSecurity(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<DigestSecurity>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<DigestSecurity> rdr : readers) {
			rdr.read(this, reader, context);
		}
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<DigestSecurity>[] readers, int __index___username, int __index___passwordHash) {
		
		readers[__index___username] = (item, reader, context) -> { item.username = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
		readers[__index___passwordHash] = (item, reader, context) -> { item.passwordHash = org.revenj.postgres.converters.ByteaConverter.parse(reader, context); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<DigestSecurity>[] readers, int __index__extended_username, int __index__extended_passwordHash) {
		
		readers[__index__extended_username] = (item, reader, context) -> { item.username = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
		readers[__index__extended_passwordHash] = (item, reader, context) -> { item.passwordHash = org.revenj.postgres.converters.ByteaConverter.parse(reader, context); return item; };
	}
}
