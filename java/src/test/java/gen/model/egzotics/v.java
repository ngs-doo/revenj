/*
* Created by DSL Platform
* v1.0.0.32432 
*/

package gen.model.egzotics;



public final class v   implements java.lang.Cloneable, java.io.Serializable, com.dslplatform.json.JsonObject {
	
	
	
	public v(
			final int x) {
			
		setX(x);
	}

	
	
	public v() {
			
		this.x = 0;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + 831;
		result = prime * result + (this.x);
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof v))
			return false;
		return deepEquals((v) obj);
	}

	public boolean deepEquals(final v other) {
		if (other == null)
			return false;
		
		if(!(this.x == other.x))
			return false;
		return true;
	}

	private v(v other) {
		
		this.x = other.x;
	}

	@Override
	public Object clone() {
		return new v(this);
	}

	@Override
	public String toString() {
		return "v(" + x + ')';
	}
	
	@com.fasterxml.jackson.annotation.JsonCreator private v(
			@com.fasterxml.jackson.annotation.JsonProperty("_helper") final boolean _helper ,
			@com.fasterxml.jackson.annotation.JsonProperty("x") final int x) {
		
		this.x = x;
	}

	private static final long serialVersionUID = 4303057795046559537L;
	
	private int x;

	
	@com.fasterxml.jackson.annotation.JsonProperty("x")
	public int getX()  {
		
		return x;
	}

	
	public v setX(final int value) {
		
		this.x = value;
		
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

	static void __serializeJsonObjectMinimal(final v self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			if (self.x != 0) {
			hasWrittenProperty = true;
				sw.writeAscii("\"x\":", 4);
				com.dslplatform.json.NumberConverter.serialize(self.x, sw);
			}
	}

	static void __serializeJsonObjectFull(final v self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			
			sw.writeAscii("\"x\":", 4);
			com.dslplatform.json.NumberConverter.serialize(self.x, sw);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<v> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<v>() {
		@Override
		public v deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.egzotics.v(reader);
		}
	};

	private v(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		int _x_ = 0;
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
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.egzotics.v(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public v(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<v>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<v> rdr : readers) {
			rdr.read(this, reader, context);
		}
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<v>[] readers, int __index___x) {
		
		readers[__index___x] = (item, reader, context) -> { item.x = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<v>[] readers, int __index__extended_x) {
		
		readers[__index__extended_x] = (item, reader, context) -> { item.x = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
	}
}
