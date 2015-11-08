package gen.model.adt;


@com.fasterxml.jackson.annotation.JsonTypeName("adt.Token")
public final class Token   implements java.lang.Cloneable, java.io.Serializable, gen.model.adt.Auth<gen.model.adt.Token>, com.dslplatform.json.JsonObject {
	
	
	
	public Token(
			final String token) {
			
		setToken(token);
	}

	
	
	public Token() {
			
		this.token = "";
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + 739459106;
		result = prime * result + (this.token.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Token))
			return false;
		return deepEquals((Token) obj);
	}

	public boolean deepEquals(final Token other) {
		if (other == null)
			return false;
		
		if(!(this.token.equals(other.token)))
			return false;
		return true;
	}

	private Token(Token other) {
		
		this.token = other.token;
	}

	@Override
	public Object clone() {
		return new Token(this);
	}

	@Override
	public String toString() {
		return "Token(" + token + ')';
	}
	
	@com.fasterxml.jackson.annotation.JsonCreator private Token(
			@com.fasterxml.jackson.annotation.JsonProperty("_helper") final boolean _helper ,
			@com.fasterxml.jackson.annotation.JsonProperty("token") final String token) {
		
		this.token = token == null ? "" : token;
	}

	private static final long serialVersionUID = 3506179341387785009L;
	
	private String token;

	
	@com.fasterxml.jackson.annotation.JsonProperty("token")
	public String getToken()  {
		
		return token;
	}

	
	public Token setToken(final String value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"token\" cannot be null!");
		this.token = value;
		
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

	static public void __serializeJsonObjectMinimal(final Token self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			if (!(self.token.length() == 0)) {
			if(hasWrittenProperty) sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
			hasWrittenProperty = true;
				sw.writeAscii("\"token\":", 8);
				sw.writeString(self.token);
			}
	}

	static public void __serializeJsonObjectFull(final Token self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			
			if(hasWrittenProperty) sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
			sw.writeAscii("\"token\":", 8);
			sw.writeString(self.token);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<Token> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<Token>() {
		@Override
		public Token deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.adt.Token(reader);
		}
	};

	private Token(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _token_ = "";
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
					
					case -1803949518:
						_token_ = com.dslplatform.json.StringConverter.deserialize(reader);
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
					
					case -1803949518:
						_token_ = com.dslplatform.json.StringConverter.deserialize(reader);
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
		
		this.token = _token_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.adt.Token(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public Token(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Token>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<Token> rdr : readers) {
			rdr.read(this, reader, context);
		}
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Token>[] readers, int __index___token) {
		
		readers[__index___token] = (item, reader, context) -> { item.token = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Token>[] readers, int __index__extended_token) {
		
		readers[__index__extended_token] = (item, reader, context) -> { item.token = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
	}
}
