/*
* Created by DSL Platform
* v1.0.0.29923 
*/

package gen.model.md;



public class Child2   implements java.lang.Cloneable, java.io.Serializable, com.dslplatform.json.JsonObject {
	
	
	
	public Child2() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.d = 0.0;
		this.Detailid = java.util.UUID.randomUUID();
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
		if (obj == null || obj instanceof Child2 == false)
			return false;
		final Child2 other = (Child2) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final Child2 other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(Double.doubleToLongBits(this.d) == Double.doubleToLongBits(other.d)))
			return false;
		if(!(this.Detailid.equals(other.Detailid)))
			return false;
		if(!(this.Index == other.Index))
			return false;
		return true;
	}

	private Child2(Child2 other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.d = other.d;
		this.Detailid = other.Detailid;
		this.Index = other.Index;
	}

	@Override
	public Object clone() {
		return new Child2(this);
	}

	@Override
	public String toString() {
		return "Child2(" + URI + ')';
	}
	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	
	@com.fasterxml.jackson.annotation.JsonCreator private Child2(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("d") final double d,
			@com.fasterxml.jackson.annotation.JsonProperty("Detailid") final java.util.UUID Detailid,
			@com.fasterxml.jackson.annotation.JsonProperty("Index") final int Index) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.d = d;
		this.Detailid = Detailid == null ? org.revenj.Utils.MIN_UUID : Detailid;
		this.Index = Index;
	}

	private static final long serialVersionUID = -2360327123382568611L;
	
	private double d;

	
	@com.fasterxml.jackson.annotation.JsonProperty("d")
	public double getD()  {
		
		return d;
	}

	
	public Child2 setD(final double value) {
		
		this.d = value;
		
		return this;
	}

	
	private java.util.UUID Detailid;

	
	@com.fasterxml.jackson.annotation.JsonProperty("Detailid")
	public java.util.UUID getDetailid()  {
		
		return Detailid;
	}

	
	private Child2 setDetailid(final java.util.UUID value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"Detailid\" cannot be null!");
		this.Detailid = value;
		
		return this;
	}

	
	private int Index;

	
	@com.fasterxml.jackson.annotation.JsonProperty("Index")
	public int getIndex()  {
		
		return Index;
	}

	
	private Child2 setIndex(final int value) {
		
		this.Index = value;
		
		return this;
	}

	
	static {
		gen.model.md.Detail.__bindTochildren2(parent -> {
			int i = 0;
			for (gen.model.md.Child2 e : parent.getChildren2()) { 
				e.Detailid = parent.getId();
				e.Index = i++; 
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

	static void __serializeJsonObjectMinimal(final Child2 self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (self.d != 0.0) {
				sw.writeAscii(",\"d\":", 5);
				com.dslplatform.json.NumberConverter.serialize(self.d, sw);
			}
		
			if (!(self.Detailid.getMostSignificantBits() == 0 && self.Detailid.getLeastSignificantBits() == 0)) {
				sw.writeAscii(",\"Detailid\":", 12);
				com.dslplatform.json.UUIDConverter.serialize(self.Detailid, sw);
			}
		
			if (self.Index != 0) {
				sw.writeAscii(",\"Index\":", 9);
				com.dslplatform.json.NumberConverter.serialize(self.Index, sw);
			}
	}

	static void __serializeJsonObjectFull(final Child2 self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			sw.writeAscii(",\"d\":", 5);
			com.dslplatform.json.NumberConverter.serialize(self.d, sw);
		
			
			sw.writeAscii(",\"Detailid\":", 12);
			com.dslplatform.json.UUIDConverter.serialize(self.Detailid, sw);
		
			
			sw.writeAscii(",\"Index\":", 9);
			com.dslplatform.json.NumberConverter.serialize(self.Index, sw);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<Child2> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<Child2>() {
		@Override
		public Child2 deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.md.Child2(reader);
		}
	};

	private Child2(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		double _d_ = 0.0;
		java.util.UUID _Detailid_ = org.revenj.Utils.MIN_UUID;
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
					case -519297933:
						_d_ = com.dslplatform.json.NumberConverter.deserializeDouble(reader);
					nextToken = reader.getNextToken();
						break;
					case 1627667001:
						_Detailid_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
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
					case -519297933:
						_d_ = com.dslplatform.json.NumberConverter.deserializeDouble(reader);
					nextToken = reader.getNextToken();
						break;
					case 1627667001:
						_Detailid_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
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
		this.d = _d_;
		this.Detailid = _Detailid_;
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
				return new gen.model.md.Child2(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public Child2(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Child2>[] readers) throws java.io.IOException {
		this.__locator = reader.getLocator();
		for (org.revenj.postgres.ObjectConverter.Reader<Child2> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.md.converters.Child2Converter.buildURI(reader, this);
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Child2>[] readers, int __index___d, int __index___Detailid, int __index___Index) {
		
		readers[__index___d] = (item, reader, context) -> { item.d = org.revenj.postgres.converters.DoubleConverter.parse(reader); return item; };
		readers[__index___Detailid] = (item, reader, context) -> { item.Detailid = org.revenj.postgres.converters.UuidConverter.parse(reader, false); return item; };
		readers[__index___Index] = (item, reader, context) -> { item.Index = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Child2>[] readers, int __index__extended_d, int __index__extended_Detailid, int __index__extended_Index) {
		
		readers[__index__extended_d] = (item, reader, context) -> { item.d = org.revenj.postgres.converters.DoubleConverter.parse(reader); return item; };
		readers[__index__extended_Detailid] = (item, reader, context) -> { item.Detailid = org.revenj.postgres.converters.UuidConverter.parse(reader, false); return item; };
		readers[__index__extended_Index] = (item, reader, context) -> { item.Index = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
	}
	
	
	public Child2(
			final double d) {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		setD(d);
	}

}
