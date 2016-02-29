/*
* Created by DSL Platform
* v1.0.0.15576 
*/

package gen.model.test;



public class Detail1   implements java.lang.Cloneable, java.io.Serializable, com.dslplatform.json.JsonObject {
	
	
	
	public Detail1() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.ff = 0.0f;
		this.EntityCompositeid = java.util.UUID.randomUUID();
		this.EntityIndex = 0;
		this.Index = 0;
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
		if (obj == null || obj instanceof Detail1 == false)
			return false;
		final Detail1 other = (Detail1) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final Detail1 other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.f == other.f || this.f != null && this.f.equals(other.f)))
			return false;
		if(!(Float.floatToIntBits(this.ff) == Float.floatToIntBits(other.ff)))
			return false;
		if(!(this.EntityCompositeid.equals(other.EntityCompositeid)))
			return false;
		if(!(this.EntityIndex == other.EntityIndex))
			return false;
		if(!(this.Index == other.Index))
			return false;
		return true;
	}

	private Detail1(Detail1 other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.f = other.f;
		this.ff = other.ff;
		this.EntityCompositeid = other.EntityCompositeid;
		this.EntityIndex = other.EntityIndex;
		this.Index = other.Index;
	}

	@Override
	public Object clone() {
		return new Detail1(this);
	}

	@Override
	public String toString() {
		return "Detail1(" + URI + ')';
	}
	
	
	public Detail1(
			final Float f,
			final float ff) {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		setF(f);
		setFf(ff);
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	
	@com.fasterxml.jackson.annotation.JsonCreator private Detail1(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("f") final Float f,
			@com.fasterxml.jackson.annotation.JsonProperty("ff") final float ff,
			@com.fasterxml.jackson.annotation.JsonProperty("EntityCompositeid") final java.util.UUID EntityCompositeid,
			@com.fasterxml.jackson.annotation.JsonProperty("EntityIndex") final int EntityIndex,
			@com.fasterxml.jackson.annotation.JsonProperty("Index") final int Index) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.f = f;
		this.ff = ff;
		this.EntityCompositeid = EntityCompositeid == null ? org.revenj.Utils.MIN_UUID : EntityCompositeid;
		this.EntityIndex = EntityIndex;
		this.Index = Index;
	}

	private static final long serialVersionUID = -8013771845972487135L;
	
	private Float f;

	
	@com.fasterxml.jackson.annotation.JsonProperty("f")
	public Float getF()  {
		
		return f;
	}

	
	public Detail1 setF(final Float value) {
		
		this.f = value;
		
		return this;
	}

	
	private float ff;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ff")
	public float getFf()  {
		
		return ff;
	}

	
	public Detail1 setFf(final float value) {
		
		this.ff = value;
		
		return this;
	}

	
	private java.util.UUID EntityCompositeid;

	
	@com.fasterxml.jackson.annotation.JsonProperty("EntityCompositeid")
	public java.util.UUID getEntityCompositeid()  {
		
		return EntityCompositeid;
	}

	
	private Detail1 setEntityCompositeid(final java.util.UUID value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"EntityCompositeid\" cannot be null!");
		this.EntityCompositeid = value;
		
		return this;
	}

	
	private int EntityIndex;

	
	@com.fasterxml.jackson.annotation.JsonProperty("EntityIndex")
	public int getEntityIndex()  {
		
		return EntityIndex;
	}

	
	private Detail1 setEntityIndex(final int value) {
		
		this.EntityIndex = value;
		
		return this;
	}

	
	private int Index;

	
	@com.fasterxml.jackson.annotation.JsonProperty("Index")
	public int getIndex()  {
		
		return Index;
	}

	
	private Detail1 setIndex(final int value) {
		
		this.Index = value;
		
		return this;
	}

	
	static {
		gen.model.test.Entity.__bindTodetail1((parent, arg) -> {
			try {
				int i = 0;
				for (gen.model.test.Detail1 e : parent.getDetail1()) { 
					e.EntityCompositeid = parent.getCompositeid();
					e.EntityIndex = parent.getIndex();
					e.Index = i++; 
					e.URI = gen.model.test.converters.Detail1Converter.buildURI(arg.getKey(), e);
				}
			} catch (java.io.IOException ex) {
				throw new RuntimeException(ex);
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

	static void __serializeJsonObjectMinimal(final Detail1 self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (self.f != null) {
				sw.writeAscii(",\"f\":", 5);
				com.dslplatform.json.NumberConverter.serialize(self.f, sw);
			}
		
			if (self.ff != 0.0f) {
				sw.writeAscii(",\"ff\":", 6);
				com.dslplatform.json.NumberConverter.serialize(self.ff, sw);
			}
		
			if (!(self.EntityCompositeid.getMostSignificantBits() == 0 && self.EntityCompositeid.getLeastSignificantBits() == 0)) {
				sw.writeAscii(",\"EntityCompositeid\":", 21);
				com.dslplatform.json.UUIDConverter.serialize(self.EntityCompositeid, sw);
			}
		
			if (self.EntityIndex != 0) {
				sw.writeAscii(",\"EntityIndex\":", 15);
				com.dslplatform.json.NumberConverter.serialize(self.EntityIndex, sw);
			}
		
			if (self.Index != 0) {
				sw.writeAscii(",\"Index\":", 9);
				com.dslplatform.json.NumberConverter.serialize(self.Index, sw);
			}
	}

	static void __serializeJsonObjectFull(final Detail1 self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			if (self.f != null) {
				sw.writeAscii(",\"f\":", 5);
				com.dslplatform.json.NumberConverter.serialize(self.f, sw);
			} else {
				sw.writeAscii(",\"f\":null", 9);
			}
		
			
			sw.writeAscii(",\"ff\":", 6);
			com.dslplatform.json.NumberConverter.serialize(self.ff, sw);
		
			
			sw.writeAscii(",\"EntityCompositeid\":", 21);
			com.dslplatform.json.UUIDConverter.serialize(self.EntityCompositeid, sw);
		
			
			sw.writeAscii(",\"EntityIndex\":", 15);
			com.dslplatform.json.NumberConverter.serialize(self.EntityIndex, sw);
		
			
			sw.writeAscii(",\"Index\":", 9);
			com.dslplatform.json.NumberConverter.serialize(self.Index, sw);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<Detail1> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<Detail1>() {
		@Override
		public Detail1 deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.test.Detail1(reader);
		}
	};

	private Detail1(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		Float _f_ = null;
		float _ff_ = 0.0f;
		java.util.UUID _EntityCompositeid_ = org.revenj.Utils.MIN_UUID;
		int _EntityIndex_ = 0;
		int _Index_ = 0;
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
					case -485742695:
						_f_ = com.dslplatform.json.NumberConverter.deserializeFloat(reader);
					nextToken = reader.getNextToken();
						break;
					case 1797453421:
						_ff_ = com.dslplatform.json.NumberConverter.deserializeFloat(reader);
					nextToken = reader.getNextToken();
						break;
					case -1909446172:
						_EntityCompositeid_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case 956507086:
						_EntityIndex_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case -1362645429:
						_Index_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
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
					case -485742695:
						_f_ = com.dslplatform.json.NumberConverter.deserializeFloat(reader);
					nextToken = reader.getNextToken();
						break;
					case 1797453421:
						_ff_ = com.dslplatform.json.NumberConverter.deserializeFloat(reader);
					nextToken = reader.getNextToken();
						break;
					case -1909446172:
						_EntityCompositeid_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case 956507086:
						_EntityIndex_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case -1362645429:
						_Index_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
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
		this.f = _f_;
		this.ff = _ff_;
		this.EntityCompositeid = _EntityCompositeid_;
		this.EntityIndex = _EntityIndex_;
		this.Index = _Index_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.test.Detail1(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public Detail1(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Detail1>[] readers) throws java.io.IOException {
		this.__locator = reader.getLocator();
		for (org.revenj.postgres.ObjectConverter.Reader<Detail1> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.test.converters.Detail1Converter.buildURI(reader, this);
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Detail1>[] readers, int __index___f, int __index___ff, int __index___EntityCompositeid, int __index___EntityIndex, int __index___Index) {
		
		readers[__index___f] = (item, reader, context) -> { item.f = org.revenj.postgres.converters.FloatConverter.parseNullable(reader); return item; };
		readers[__index___ff] = (item, reader, context) -> { item.ff = org.revenj.postgres.converters.FloatConverter.parse(reader); return item; };
		readers[__index___EntityCompositeid] = (item, reader, context) -> { item.EntityCompositeid = org.revenj.postgres.converters.UuidConverter.parse(reader, false); return item; };
		readers[__index___EntityIndex] = (item, reader, context) -> { item.EntityIndex = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
		readers[__index___Index] = (item, reader, context) -> { item.Index = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Detail1>[] readers, int __index__extended_f, int __index__extended_ff, int __index__extended_EntityCompositeid, int __index__extended_EntityIndex, int __index__extended_Index) {
		
		readers[__index__extended_f] = (item, reader, context) -> { item.f = org.revenj.postgres.converters.FloatConverter.parseNullable(reader); return item; };
		readers[__index__extended_ff] = (item, reader, context) -> { item.ff = org.revenj.postgres.converters.FloatConverter.parse(reader); return item; };
		readers[__index__extended_EntityCompositeid] = (item, reader, context) -> { item.EntityCompositeid = org.revenj.postgres.converters.UuidConverter.parse(reader, false); return item; };
		readers[__index__extended_EntityIndex] = (item, reader, context) -> { item.EntityIndex = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
		readers[__index__extended_Index] = (item, reader, context) -> { item.Index = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
	}
}
