/*
* Created by DSL Platform
* v1.0.0.29923 
*/

package gen.model.mixinReference;



public final class Test   implements java.lang.Cloneable, java.io.Serializable, com.dslplatform.json.JsonObject {
	
	
	
	public Test(
			final int x,
			final gen.model.mixinReference.Author author) {
			
		setX(x);
		setAuthor(author);
	}

	
	
	public Test() {
			
		this.x = 0;
		this.author = new gen.model.mixinReference.Author();
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + 23844169;
		result = prime * result + (this.x);
		result = prime * result + (this.author.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Test))
			return false;
		return deepEquals((Test) obj);
	}

	public boolean deepEquals(final Test other) {
		if (other == null)
			return false;
		
		if(!(this.x == other.x))
			return false;
		if(!(this.author.equals(other.author)))
			return false;
		return true;
	}

	private Test(Test other) {
		
		this.x = other.x;
		this.author = other.author;
	}

	@Override
	public Object clone() {
		return new Test(this);
	}

	@Override
	public String toString() {
		return "Test(" + x + ',' + author + ')';
	}
	
	@com.fasterxml.jackson.annotation.JsonCreator private Test(
			@com.fasterxml.jackson.annotation.JsonProperty("_helper") final boolean _helper ,
			@com.fasterxml.jackson.annotation.JsonProperty("x") final int x,
			@com.fasterxml.jackson.annotation.JsonProperty("author") final gen.model.mixinReference.Author author) {
		
		this.x = x;
		this.author = author == null ? new gen.model.mixinReference.Author() : author;
	}

	private static final long serialVersionUID = -4911931088387240981L;
	
	private int x;

	
	@com.fasterxml.jackson.annotation.JsonProperty("x")
	public int getX()  {
		
		return x;
	}

	
	public Test setX(final int value) {
		
		this.x = value;
		
		return this;
	}

	
	private gen.model.mixinReference.Author author;

	
	@com.fasterxml.jackson.annotation.JsonProperty("author")
	public gen.model.mixinReference.Author getAuthor()  {
		
		return author;
	}

	
	public Test setAuthor(final gen.model.mixinReference.Author value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"author\" cannot be null!");
		this.author = value;
		
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

	static void __serializeJsonObjectMinimal(final Test self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			if (self.x != 0) {
			hasWrittenProperty = true;
				sw.writeAscii("\"x\":", 4);
				com.dslplatform.json.NumberConverter.serialize(self.x, sw);
			}
		
		if(self.author != null) {
			if(hasWrittenProperty) sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
			hasWrittenProperty = true;
			sw.writeAscii("\"author\":{", 10);
			
					gen.model.mixinReference.Author.__serializeJsonObjectMinimal(self.author, sw, false);
					sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
		}
	}

	static void __serializeJsonObjectFull(final Test self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			
			sw.writeAscii("\"x\":", 4);
			com.dslplatform.json.NumberConverter.serialize(self.x, sw);
		
		
		if(self.author != null) {
			sw.writeAscii(",\"author\":{", 11);
			
					gen.model.mixinReference.Author.__serializeJsonObjectFull(self.author, sw, false);
					sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
		} else {
			sw.writeAscii(",\"author\":null", 14);
		}
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<Test> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<Test>() {
		@Override
		public Test deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.mixinReference.Test(reader);
		}
	};

	private Test(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		int _x_ = 0;
		gen.model.mixinReference.Author _author_ = null;
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
					
					case -49524601:
						_x_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case 1333443158:
						
					if (nextToken == '{') {
						reader.getNextToken();
						_author_ = gen.model.mixinReference.Author.JSON_READER.deserialize(reader);
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
					
					case -49524601:
						_x_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case 1333443158:
						
					if (nextToken == '{') {
						reader.getNextToken();
						_author_ = gen.model.mixinReference.Author.JSON_READER.deserialize(reader);
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
		
		this.x = _x_;
		this.author = _author_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.mixinReference.Test(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
}
