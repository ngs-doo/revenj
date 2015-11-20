package gen.model.binaries;



public final class ReadOnlyDocument   implements java.io.Serializable, org.revenj.patterns.DataSource, com.dslplatform.json.JsonObject {
	
	
	@com.fasterxml.jackson.annotation.JsonCreator 
	public ReadOnlyDocument(
			@com.fasterxml.jackson.annotation.JsonProperty("ID")  final java.util.UUID ID,
			@com.fasterxml.jackson.annotation.JsonProperty("Name")  final String Name) {
			
		this.ID = ID == null ? java.util.UUID.randomUUID() : ID;
		this.Name = Name == null ? "" : Name;
	}

	private static final long serialVersionUID = 6942982387979454501L;
	
	private final java.util.UUID ID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ID")
	public java.util.UUID getID()  {
		
		return this.ID;
	}

	
	private final String Name;

	
	@com.fasterxml.jackson.annotation.JsonProperty("Name")
	public String getName()  {
		
		return this.Name;
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

	static void __serializeJsonObjectMinimal(final ReadOnlyDocument self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			if (!(self.ID.getMostSignificantBits() == 0 && self.ID.getLeastSignificantBits() == 0)) {
			hasWrittenProperty = true;
				sw.writeAscii("\"ID\":", 5);
				com.dslplatform.json.UUIDConverter.serialize(self.ID, sw);
			}
		
			if (!(self.Name.length() == 0)) {
			if(hasWrittenProperty) sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
			hasWrittenProperty = true;
				sw.writeAscii("\"Name\":", 7);
				sw.writeString(self.Name);
			}
	}

	static void __serializeJsonObjectFull(final ReadOnlyDocument self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			
			sw.writeAscii("\"ID\":", 5);
			com.dslplatform.json.UUIDConverter.serialize(self.ID, sw);
		
			
			sw.writeAscii(",\"Name\":", 8);
			sw.writeString(self.Name);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<ReadOnlyDocument> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<ReadOnlyDocument>() {
		@Override
		public ReadOnlyDocument deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.binaries.ReadOnlyDocument(reader);
		}
	};

	private ReadOnlyDocument(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		java.util.UUID _ID_ = org.revenj.Utils.MIN_UUID;
		String _Name_ = "";
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
					
					case 1458105184:
						_ID_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case 266367750:
						_Name_ = com.dslplatform.json.StringConverter.deserialize(reader);
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
					
					case 1458105184:
						_ID_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case 266367750:
						_Name_ = com.dslplatform.json.StringConverter.deserialize(reader);
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
		
		this.ID = _ID_;
		this.Name = _Name_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.binaries.ReadOnlyDocument(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
}
