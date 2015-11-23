package gen.model.adt;


@com.fasterxml.jackson.annotation.JsonTypeName("adt.Anonymous")
public final class Anonymous   implements java.lang.Cloneable, java.io.Serializable, gen.model.adt.Auth<gen.model.adt.Anonymous>, com.dslplatform.json.JsonObject {
	
	
	
	public Anonymous() {
			
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + 316686826;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Anonymous))
			return false;
		return deepEquals((Anonymous) obj);
	}

	public boolean deepEquals(final Anonymous other) {
		if (other == null)
			return false;
		
		return true;
	}

	private Anonymous(Anonymous other) {
		
	}

	@Override
	public Object clone() {
		return new Anonymous(this);
	}

	@Override
	public String toString() {
		return "Anonymous(" + ')';
	}
	
	@com.fasterxml.jackson.annotation.JsonCreator private Anonymous(
			@com.fasterxml.jackson.annotation.JsonProperty("_helper") final boolean _helper ) {
		
	}

	private static final long serialVersionUID = -3090653141043937027L;
	
	public void serialize(final com.dslplatform.json.JsonWriter sw, final boolean minimal) {
		sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
		if (minimal) {
			__serializeJsonObjectMinimal(this, sw, false);
		} else {
			__serializeJsonObjectFull(this, sw, false);
		}
		sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
	}

	static public void __serializeJsonObjectMinimal(final Anonymous self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
	}

	static public void __serializeJsonObjectFull(final Anonymous self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<Anonymous> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<Anonymous>() {
		@Override
		public Anonymous deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.adt.Anonymous(reader);
		}
	};

	private Anonymous(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
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
					
					default:
						nextToken = reader.skip();
						break;
				}
			}
			if (nextToken != '}') {
				throw new java.io.IOException("Expecting '}' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
			}
		}
		
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.adt.Anonymous(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public Anonymous(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Anonymous>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<Anonymous> rdr : readers) {
			rdr.read(this, reader, context);
		}
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Anonymous>[] readers) {
		
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Anonymous>[] readers) {
		
	}
}
