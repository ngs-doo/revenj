/*
* Created by DSL Platform
* v1.0.0.27897 
*/

package gen.model.md;



public class Detail   implements java.lang.Cloneable, java.io.Serializable, com.dslplatform.json.JsonObject {
	
	
	
	public Detail() {
			
		this.id = java.util.UUID.randomUUID();
		this.masterId = 0;
		this.children1 = new gen.model.md.Child1[] { };
		this.children2 = new gen.model.md.Child2[] { };
		this.setReference1(new gen.model.md.Reference1());
		this.setReference2(new gen.model.md.Reference2());
		this.URI = this.id.toString();
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
		if (obj == null || obj instanceof Detail == false)
			return false;
		final Detail other = (Detail) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final Detail other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.id.equals(other.id)))
			return false;
		if(!(this.masterId == other.masterId))
			return false;
		if(!(java.util.Arrays.equals(this.children1, other.children1)))
			return false;
		if(!(java.util.Arrays.equals(this.children2, other.children2)))
			return false;
		if(!(this.reference1 == other.reference1 || this.reference1 != null && this.reference1.equals(other.reference1)))
			return false;
		if(!(this.reference2 == other.reference2 || this.reference2 != null && this.reference2.equals(other.reference2)))
			return false;
		return true;
	}

	private Detail(Detail other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.id = other.id;
		this.masterId = other.masterId;
		this.children1 = new gen.model.md.Child1[other.children1.length];
			if (other.children1 != null) {
				for (int _i = 0; _i < other.children1.length; _i++) {
					this.children1[_i] = (gen.model.md.Child1)other.children1[_i].clone();
				}
			};
		this.children2 = new gen.model.md.Child2[other.children2.length];
			if (other.children2 != null) {
				for (int _i = 0; _i < other.children2.length; _i++) {
					this.children2[_i] = (gen.model.md.Child2)other.children2[_i].clone();
				}
			};
		this.reference1 = other.reference1 == null ? null : (gen.model.md.Reference1)(other.reference1.clone());
		this.reference2 = other.reference2 == null ? null : (gen.model.md.Reference2)(other.reference2.clone());
	}

	@Override
	public Object clone() {
		return new Detail(this);
	}

	@Override
	public String toString() {
		return "Detail(" + URI + ')';
	}
	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	
	@com.fasterxml.jackson.annotation.JsonCreator private Detail(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("id") final java.util.UUID id,
			@com.fasterxml.jackson.annotation.JsonProperty("masterId") final int masterId,
			@com.fasterxml.jackson.annotation.JsonProperty("children1") final gen.model.md.Child1[] children1,
			@com.fasterxml.jackson.annotation.JsonProperty("children2") final gen.model.md.Child2[] children2,
			@com.fasterxml.jackson.annotation.JsonProperty("reference1") final gen.model.md.Reference1 reference1,
			@com.fasterxml.jackson.annotation.JsonProperty("reference2") final gen.model.md.Reference2 reference2) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.id = id == null ? org.revenj.Utils.MIN_UUID : id;
		this.masterId = masterId;
		this.children1 = children1 == null ? new gen.model.md.Child1[] { } : children1;
		this.children2 = children2 == null ? new gen.model.md.Child2[] { } : children2;
		this.reference1 = reference1 == null ? new gen.model.md.Reference1() : reference1;
		this.reference2 = reference2 == null ? new gen.model.md.Reference2() : reference2;
	}

	private static final long serialVersionUID = -139622226388710225L;
	
	private java.util.UUID id;

	
	@com.fasterxml.jackson.annotation.JsonProperty("id")
	public java.util.UUID getId()  {
		
		return id;
	}

	
	public Detail setId(final java.util.UUID value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"id\" cannot be null!");
		this.id = value;
		
		return this;
	}

	
	private int masterId;

	
	@com.fasterxml.jackson.annotation.JsonProperty("masterId")
	public int getMasterId()  {
		
		return masterId;
	}

	
	public Detail setMasterId(final int value) {
		
		this.masterId = value;
		
		return this;
	}

	
	static void __bindTochildren1(java.util.function.Consumer<gen.model.md.Detail> binder) {
		__binderchildren1 = binder;
	}

	private static java.util.function.Consumer<gen.model.md.Detail> __binderchildren1;
	private static final gen.model.md.Child1[] _defaultchildren1 = new gen.model.md.Child1[] { };
	
	private gen.model.md.Child1[] children1;

	
	@com.fasterxml.jackson.annotation.JsonProperty("children1")
	public gen.model.md.Child1[] getChildren1()  {
		
		return children1;
	}

	
	public Detail setChildren1(final gen.model.md.Child1[] value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"children1\" cannot be null!");
		org.revenj.Guards.checkNulls(value);
		this.children1 = value;
		
		return this;
	}

	
	static void __bindTochildren2(java.util.function.Consumer<gen.model.md.Detail> binder) {
		__binderchildren2 = binder;
	}

	private static java.util.function.Consumer<gen.model.md.Detail> __binderchildren2;
	private static final gen.model.md.Child2[] _defaultchildren2 = new gen.model.md.Child2[] { };
	
	private gen.model.md.Child2[] children2;

	
	@com.fasterxml.jackson.annotation.JsonProperty("children2")
	public gen.model.md.Child2[] getChildren2()  {
		
		return children2;
	}

	
	public Detail setChildren2(final gen.model.md.Child2[] value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"children2\" cannot be null!");
		org.revenj.Guards.checkNulls(value);
		this.children2 = value;
		
		return this;
	}

	
	static void __bindToreference1(java.util.function.Consumer<gen.model.md.Detail> binder) {
		__binderreference1 = binder;
	}

	private static java.util.function.Consumer<gen.model.md.Detail> __binderreference1;
	
	private gen.model.md.Reference1 reference1;

	
	@com.fasterxml.jackson.annotation.JsonProperty("reference1")
	public gen.model.md.Reference1 getReference1()  {
		
		return reference1;
	}

	
	public Detail setReference1(final gen.model.md.Reference1 value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"reference1\" cannot be null!");
		this.reference1 = value;
		
		return this;
	}

	
	static void __bindToreference2(java.util.function.Consumer<gen.model.md.Detail> binder) {
		__binderreference2 = binder;
	}

	private static java.util.function.Consumer<gen.model.md.Detail> __binderreference2;
	
	private gen.model.md.Reference2 reference2;

	
	@com.fasterxml.jackson.annotation.JsonProperty("reference2")
	public gen.model.md.Reference2 getReference2()  {
		
		return reference2;
	}

	
	public Detail setReference2(final gen.model.md.Reference2 value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"reference2\" cannot be null!");
		this.reference2 = value;
		
		return this;
	}

	
	static {
		gen.model.md.Master.__bindTodetails(parent -> {
			for (gen.model.md.Detail e : parent.getDetails()) { 
				e.masterId = parent.getID();
				__binderchildren1.accept(e);
				__binderchildren2.accept(e);
				__binderreference1.accept(e);
				__binderreference2.accept(e);
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

	static void __serializeJsonObjectMinimal(final Detail self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (!(self.id.getMostSignificantBits() == 0 && self.id.getLeastSignificantBits() == 0)) {
				sw.writeAscii(",\"id\":", 6);
				com.dslplatform.json.UUIDConverter.serialize(self.id, sw);
			}
		
			if (self.masterId != 0) {
				sw.writeAscii(",\"masterId\":", 12);
				com.dslplatform.json.NumberConverter.serialize(self.masterId, sw);
			}
		
		if(self.children1.length != 0) {
			sw.writeAscii(",\"children1\":[", 14);
			gen.model.md.Child1 item = self.children1[0];
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.md.Child1.__serializeJsonObjectMinimal(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			for(int i = 1; i < self.children1.length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
				item = self.children1[i];
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.md.Child1.__serializeJsonObjectMinimal(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		
		if(self.children2.length != 0) {
			sw.writeAscii(",\"children2\":[", 14);
			gen.model.md.Child2 item = self.children2[0];
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.md.Child2.__serializeJsonObjectMinimal(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			for(int i = 1; i < self.children2.length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
				item = self.children2[i];
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.md.Child2.__serializeJsonObjectMinimal(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		
		if(self.reference1 != null) {
			sw.writeAscii(",\"reference1\":{", 15);
			
					gen.model.md.Reference1.__serializeJsonObjectMinimal(self.reference1, sw, false);
					sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
		}
		
		if(self.reference2 != null) {
			sw.writeAscii(",\"reference2\":{", 15);
			
					gen.model.md.Reference2.__serializeJsonObjectMinimal(self.reference2, sw, false);
					sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
		}
	}

	static void __serializeJsonObjectFull(final Detail self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			sw.writeAscii(",\"id\":", 6);
			com.dslplatform.json.UUIDConverter.serialize(self.id, sw);
		
			
			sw.writeAscii(",\"masterId\":", 12);
			com.dslplatform.json.NumberConverter.serialize(self.masterId, sw);
		
		if(self.children1.length != 0) {
			sw.writeAscii(",\"children1\":[", 14);
			gen.model.md.Child1 item = self.children1[0];
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.md.Child1.__serializeJsonObjectFull(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			for(int i = 1; i < self.children1.length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
				item = self.children1[i];
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.md.Child1.__serializeJsonObjectFull(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else sw.writeAscii(",\"children1\":[]", 15);
		
		if(self.children2.length != 0) {
			sw.writeAscii(",\"children2\":[", 14);
			gen.model.md.Child2 item = self.children2[0];
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.md.Child2.__serializeJsonObjectFull(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			for(int i = 1; i < self.children2.length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
				item = self.children2[i];
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.md.Child2.__serializeJsonObjectFull(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else sw.writeAscii(",\"children2\":[]", 15);
		
		
		if(self.reference1 != null) {
			sw.writeAscii(",\"reference1\":{", 15);
			
					gen.model.md.Reference1.__serializeJsonObjectFull(self.reference1, sw, false);
					sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
		} else {
			sw.writeAscii(",\"reference1\":null", 18);
		}
		
		
		if(self.reference2 != null) {
			sw.writeAscii(",\"reference2\":{", 15);
			
					gen.model.md.Reference2.__serializeJsonObjectFull(self.reference2, sw, false);
					sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
		} else {
			sw.writeAscii(",\"reference2\":null", 18);
		}
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<Detail> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<Detail>() {
		@Override
		public Detail deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.md.Detail(reader);
		}
	};

	private Detail(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		java.util.UUID _id_ = org.revenj.Utils.MIN_UUID;
		int _masterId_ = 0;
		gen.model.md.Child1[] _children1_ = _defaultchildren1;
		gen.model.md.Child2[] _children2_ = _defaultchildren2;
		gen.model.md.Reference1 _reference1_ = null;
		gen.model.md.Reference2 _reference2_ = null;
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
					case 926444256:
						_id_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -1739047458:
						_masterId_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case 323539033:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken == ']') {
							_children1_ = new gen.model.md.Child1[] { };
						} else {
							java.util.ArrayList<gen.model.md.Child1> __res = reader.deserializeCollection(gen.model.md.Child1.JSON_READER);
							_children1_ = __res.toArray(new gen.model.md.Child1[__res.size()]);
						}
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case 273206176:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken == ']') {
							_children2_ = new gen.model.md.Child2[] { };
						} else {
							java.util.ArrayList<gen.model.md.Child2> __res = reader.deserializeCollection(gen.model.md.Child2.JSON_READER);
							_children2_ = __res.toArray(new gen.model.md.Child2[__res.size()]);
						}
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case 630363697:
						
					if (nextToken == '{') {
						reader.getNextToken();
						_reference1_ = gen.model.md.Reference1.JSON_READER.deserialize(reader);
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '{' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case 580030840:
						
					if (nextToken == '{') {
						reader.getNextToken();
						_reference2_ = gen.model.md.Reference2.JSON_READER.deserialize(reader);
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
					
					case 2053729053:
						_URI_ = reader.readString();
				nextToken = reader.getNextToken();
						break;
					case 926444256:
						_id_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -1739047458:
						_masterId_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case 323539033:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken == ']') {
							_children1_ = new gen.model.md.Child1[] { };
						} else {
							java.util.ArrayList<gen.model.md.Child1> __res = reader.deserializeCollection(gen.model.md.Child1.JSON_READER);
							_children1_ = __res.toArray(new gen.model.md.Child1[__res.size()]);
						}
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case 273206176:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken == ']') {
							_children2_ = new gen.model.md.Child2[] { };
						} else {
							java.util.ArrayList<gen.model.md.Child2> __res = reader.deserializeCollection(gen.model.md.Child2.JSON_READER);
							_children2_ = __res.toArray(new gen.model.md.Child2[__res.size()]);
						}
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case 630363697:
						
					if (nextToken == '{') {
						reader.getNextToken();
						_reference1_ = gen.model.md.Reference1.JSON_READER.deserialize(reader);
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '{' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case 580030840:
						
					if (nextToken == '{') {
						reader.getNextToken();
						_reference2_ = gen.model.md.Reference2.JSON_READER.deserialize(reader);
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
		
		this.URI = _URI_;
		this.id = _id_;
		this.masterId = _masterId_;
		this.children1 = _children1_;
		this.children2 = _children2_;
		if(_reference1_ == null) throw new java.io.IOException("In entity md.Detail, property reference1 can't be null");
		this.reference1 = _reference1_;
		if(_reference2_ == null) throw new java.io.IOException("In entity md.Detail, property reference2 can't be null");
		this.reference2 = _reference2_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.md.Detail(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public Detail(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Detail>[] readers) throws java.io.IOException {
		this.__locator = java.util.Optional.ofNullable(reader.locator);
		for (org.revenj.postgres.ObjectConverter.Reader<Detail> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.md.converters.DetailConverter.buildURI(reader, this);
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Detail>[] readers, int __index___id, int __index___masterId, gen.model.md.converters.Child1Converter __converter_children1, int __index___children1, gen.model.md.converters.Child2Converter __converter_children2, int __index___children2, gen.model.md.converters.Reference1Converter __converter_reference1, int __index___reference1, gen.model.md.converters.Reference2Converter __converter_reference2, int __index___reference2) {
		
		readers[__index___id] = (item, reader, context) -> { item.id = org.revenj.postgres.converters.UuidConverter.parse(reader, false); return item; };
		readers[__index___masterId] = (item, reader, context) -> { item.masterId = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
		readers[__index___children1] = (item, reader, context) -> { { java.util.List<gen.model.md.Child1> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, __converter_children1::from); if (__list != null) {item.children1 = __list.toArray(new gen.model.md.Child1[__list.size()]);} else item.children1 = new gen.model.md.Child1[] { }; }; return item; };
		readers[__index___children2] = (item, reader, context) -> { { java.util.List<gen.model.md.Child2> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, __converter_children2::from); if (__list != null) {item.children2 = __list.toArray(new gen.model.md.Child2[__list.size()]);} else item.children2 = new gen.model.md.Child2[] { }; }; return item; };
		readers[__index___reference1] = (item, reader, context) -> { item.reference1 = __converter_reference1.from(reader, context); return item; };
		readers[__index___reference2] = (item, reader, context) -> { item.reference2 = __converter_reference2.from(reader, context); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Detail>[] readers, int __index__extended_id, int __index__extended_masterId, final gen.model.md.converters.Child1Converter __converter_children1, int __index__extended_children1, final gen.model.md.converters.Child2Converter __converter_children2, int __index__extended_children2, final gen.model.md.converters.Reference1Converter __converter_reference1, int __index__extended_reference1, final gen.model.md.converters.Reference2Converter __converter_reference2, int __index__extended_reference2) {
		
		readers[__index__extended_id] = (item, reader, context) -> { item.id = org.revenj.postgres.converters.UuidConverter.parse(reader, false); return item; };
		readers[__index__extended_masterId] = (item, reader, context) -> { item.masterId = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
		readers[__index__extended_children1] = (item, reader, context) -> { { java.util.List<gen.model.md.Child1> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, __converter_children1::fromExtended); if (__list != null) {item.children1 = __list.toArray(new gen.model.md.Child1[__list.size()]);} else item.children1 = new gen.model.md.Child1[] { }; }; return item; };
		readers[__index__extended_children2] = (item, reader, context) -> { { java.util.List<gen.model.md.Child2> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, __converter_children2::fromExtended); if (__list != null) {item.children2 = __list.toArray(new gen.model.md.Child2[__list.size()]);} else item.children2 = new gen.model.md.Child2[] { }; }; return item; };
		readers[__index__extended_reference1] = (item, reader, context) -> { item.reference1 = __converter_reference1.fromExtended(reader, context); return item; };
		readers[__index__extended_reference2] = (item, reader, context) -> { item.reference2 = __converter_reference2.fromExtended(reader, context); return item; };
	}
	
	
	public Detail(
			final java.util.UUID id,
			final int masterId,
			final gen.model.md.Child1[] children1,
			final gen.model.md.Child2[] children2,
			final gen.model.md.Reference1 reference1,
			final gen.model.md.Reference2 reference2) {
			
		setId(id);
		setMasterId(masterId);
		setChildren1(children1);
		setChildren2(children2);
		setReference1(reference1);
		setReference2(reference2);
		this.URI = this.id.toString();
	}

}
