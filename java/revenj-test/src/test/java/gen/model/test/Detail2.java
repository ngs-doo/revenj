package gen.model.test;



public class Detail2   implements java.lang.Cloneable, java.io.Serializable, com.dslplatform.json.JsonObject {
	
	
	
	public Detail2() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.dd = new double[] { };
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
		if (obj == null || obj instanceof Detail2 == false)
			return false;
		final Detail2 other = (Detail2) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final Detail2 other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.u == other.u || this.u != null && this.u.equals(other.u)))
			return false;
		if(!(java.util.Arrays.equals(this.dd, other.dd)))
			return false;
		if(!(this.EntityCompositeid.equals(other.EntityCompositeid)))
			return false;
		if(!(this.EntityIndex == other.EntityIndex))
			return false;
		if(!(this.Index == other.Index))
			return false;
		return true;
	}

	private Detail2(Detail2 other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.u = other.u;
		this.dd = java.util.Arrays.copyOf(other.dd, other.dd.length);
		this.EntityCompositeid = other.EntityCompositeid;
		this.EntityIndex = other.EntityIndex;
		this.Index = other.Index;
	}

	@Override
	public Object clone() {
		return new Detail2(this);
	}

	@Override
	public String toString() {
		return "Detail2(" + URI + ')';
	}
	
	
	public Detail2(
			final java.net.URI u,
			final double[] dd) {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		setU(u);
		setDd(dd);
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	private static final long serialVersionUID = 7314932077351767341L;
	
	@com.fasterxml.jackson.annotation.JsonCreator private Detail2(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("u") final java.net.URI u,
			@com.fasterxml.jackson.annotation.JsonProperty("dd") final double[] dd,
			@com.fasterxml.jackson.annotation.JsonProperty("EntityCompositeid") final java.util.UUID EntityCompositeid,
			@com.fasterxml.jackson.annotation.JsonProperty("EntityIndex") final int EntityIndex,
			@com.fasterxml.jackson.annotation.JsonProperty("Index") final int Index) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.u = u;
		this.dd = dd == null ? new double[] { } : dd;
		this.EntityCompositeid = EntityCompositeid == null ? org.revenj.Utils.MIN_UUID : EntityCompositeid;
		this.EntityIndex = EntityIndex;
		this.Index = Index;
	}

	
	private java.net.URI u;

	
	@com.fasterxml.jackson.annotation.JsonProperty("u")
	public java.net.URI getU()  {
		
		return u;
	}

	
	public Detail2 setU(final java.net.URI value) {
		
		this.u = value;
		
		return this;
	}

	private static final double[] _defaultdd = new double[] { };
	
	private double[] dd;

	
	@com.fasterxml.jackson.annotation.JsonProperty("dd")
	public double[] getDd()  {
		
		return dd;
	}

	
	public Detail2 setDd(final double[] value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"dd\" cannot be null!");
		this.dd = value;
		
		return this;
	}

	
	private java.util.UUID EntityCompositeid;

	
	@com.fasterxml.jackson.annotation.JsonProperty("EntityCompositeid")
	public java.util.UUID getEntityCompositeid()  {
		
		return EntityCompositeid;
	}

	
	private Detail2 setEntityCompositeid(final java.util.UUID value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"EntityCompositeid\" cannot be null!");
		this.EntityCompositeid = value;
		
		return this;
	}

	
	private int EntityIndex;

	
	@com.fasterxml.jackson.annotation.JsonProperty("EntityIndex")
	public int getEntityIndex()  {
		
		return EntityIndex;
	}

	
	private Detail2 setEntityIndex(final int value) {
		
		this.EntityIndex = value;
		
		return this;
	}

	
	private int Index;

	
	@com.fasterxml.jackson.annotation.JsonProperty("Index")
	public int getIndex()  {
		
		return Index;
	}

	
	private Detail2 setIndex(final int value) {
		
		this.Index = value;
		
		return this;
	}

	
	static {
		gen.model.test.Entity.__bindTodetail2(parent -> {
			int i = 0;
			for (gen.model.test.Detail2 e : parent.getDetail2()) { 
				e.EntityCompositeid = parent.getCompositeid();
				e.EntityIndex = parent.getIndex();
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

	static void __serializeJsonObjectMinimal(final Detail2 self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (self.u != null) {
				sw.writeAscii(",\"u\":", 5);
				com.dslplatform.json.NetConverter.serialize(self.u, sw);
			}
		
		if(self.dd.length != 0) {
			sw.writeAscii(",\"dd\":[", 7);
			com.dslplatform.json.NumberConverter.serialize(self.dd[0], sw);
			for(int i = 1; i < self.dd.length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
				com.dslplatform.json.NumberConverter.serialize(self.dd[i], sw);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
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

	static void __serializeJsonObjectFull(final Detail2 self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			if (self.u != null) {
				sw.writeAscii(",\"u\":", 5);
				com.dslplatform.json.NetConverter.serialize(self.u, sw);
			} else {
				sw.writeAscii(",\"u\":null", 9);
			}
		
		if(self.dd.length != 0) {
			sw.writeAscii(",\"dd\":[", 7);
			com.dslplatform.json.NumberConverter.serialize(self.dd[0], sw);
			for(int i = 1; i < self.dd.length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
				com.dslplatform.json.NumberConverter.serialize(self.dd[i], sw);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else sw.writeAscii(",\"dd\":[]", 8);
		
			
			sw.writeAscii(",\"EntityCompositeid\":", 21);
			com.dslplatform.json.UUIDConverter.serialize(self.EntityCompositeid, sw);
		
			
			sw.writeAscii(",\"EntityIndex\":", 15);
			com.dslplatform.json.NumberConverter.serialize(self.EntityIndex, sw);
		
			
			sw.writeAscii(",\"Index\":", 9);
			com.dslplatform.json.NumberConverter.serialize(self.Index, sw);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<Detail2> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<Detail2>() {
		@Override
		public Detail2 deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.test.Detail2(reader);
		}
	};

	private Detail2(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		java.net.URI _u_ = null;
		double[] _dd_ = _defaultdd;
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
					case -267633648:
						_u_ = com.dslplatform.json.NetConverter.deserializeUri(reader);
					nextToken = reader.getNextToken();
						break;
					case 1562169397:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							java.util.ArrayList<Double> __res = com.dslplatform.json.NumberConverter.deserializeDoubleCollection(reader);
							double[] __resUnboxed = new double[__res.size()];
							for(int _i=0;_i<__res.size();_i++) 
								__resUnboxed[_i] = __res.get(_i);
							_dd_ = __resUnboxed;
						}
						nextToken = reader.getNextToken();
					}
					else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
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
					case -267633648:
						_u_ = com.dslplatform.json.NetConverter.deserializeUri(reader);
					nextToken = reader.getNextToken();
						break;
					case 1562169397:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							java.util.ArrayList<Double> __res = com.dslplatform.json.NumberConverter.deserializeDoubleCollection(reader);
							double[] __resUnboxed = new double[__res.size()];
							for(int _i=0;_i<__res.size();_i++) 
								__resUnboxed[_i] = __res.get(_i);
							_dd_ = __resUnboxed;
						}
						nextToken = reader.getNextToken();
					}
					else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
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
		this.u = _u_;
		this.dd = _dd_;
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
				return new gen.model.test.Detail2(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public Detail2(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Detail2>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<Detail2> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.test.converters.Detail2Converter.buildURI(reader, this);
		this.__locator = java.util.Optional.ofNullable(reader.locator);
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Detail2>[] readers, int __index___u, int __index___dd, int __index___EntityCompositeid, int __index___EntityIndex, int __index___Index) {
		
		readers[__index___u] = (item, reader, context) -> { item.u = org.revenj.postgres.converters.UrlConverter.parse(reader, context); return item; };
		readers[__index___dd] = (item, reader, context) -> { { java.util.List<Double> __list = org.revenj.postgres.converters.DoubleConverter.parseCollection(reader, context, false); if(__list != null) {
				double[] __resUnboxed = new double[__list.size()];
				for(int _i=0;_i<__list.size();_i++) {
					__resUnboxed[_i] = __list.get(_i);
				}
				item.dd = __resUnboxed;
			} else item.dd = new double[] { }; }; return item; };
		readers[__index___EntityCompositeid] = (item, reader, context) -> { item.EntityCompositeid = org.revenj.postgres.converters.UuidConverter.parse(reader, false); return item; };
		readers[__index___EntityIndex] = (item, reader, context) -> { item.EntityIndex = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
		readers[__index___Index] = (item, reader, context) -> { item.Index = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Detail2>[] readers, int __index__extended_u, int __index__extended_dd, int __index__extended_EntityCompositeid, int __index__extended_EntityIndex, int __index__extended_Index) {
		
		readers[__index__extended_u] = (item, reader, context) -> { item.u = org.revenj.postgres.converters.UrlConverter.parse(reader, context); return item; };
		readers[__index__extended_dd] = (item, reader, context) -> { { java.util.List<Double> __list = org.revenj.postgres.converters.DoubleConverter.parseCollection(reader, context, false); if(__list != null) {
				double[] __resUnboxed = new double[__list.size()];
				for(int _i=0;_i<__list.size();_i++) {
					__resUnboxed[_i] = __list.get(_i);
				}
				item.dd = __resUnboxed;
			} else item.dd = new double[] { }; }; return item; };
		readers[__index__extended_EntityCompositeid] = (item, reader, context) -> { item.EntityCompositeid = org.revenj.postgres.converters.UuidConverter.parse(reader, false); return item; };
		readers[__index__extended_EntityIndex] = (item, reader, context) -> { item.EntityIndex = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
		readers[__index__extended_Index] = (item, reader, context) -> { item.Index = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
	}
}
