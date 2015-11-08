package gen.model.egzotics;



public class PksV   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot, com.dslplatform.json.JsonObject {
	
	
	
	public PksV() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.v = new gen.model.egzotics.v();
		this.vv = new gen.model.egzotics.v[] { };
		this.e = gen.model.egzotics.E.A;
		this.ee = new java.util.LinkedHashSet<gen.model.egzotics.E>(4);
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
		if (obj == null || obj instanceof PksV == false)
			return false;
		final PksV other = (PksV) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final PksV other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.v == other.v || this.v != null && this.v.equals(other.v)))
			return false;
		if(!(java.util.Arrays.equals(this.vv, other.vv)))
			return false;
		if(!(this.e == other.e || this.e != null && this.e.equals(other.e)))
			return false;
		if(!((this.ee == other.ee || this.ee != null && this.ee.equals(other.ee))))
			return false;
		return true;
	}

	private PksV(PksV other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.v = other.v == null ? null : (gen.model.egzotics.v)(other.v.clone());
		this.vv = new gen.model.egzotics.v[other.vv.length];
			if (other.vv != null) {
				for (int _i = 0; _i < other.vv.length; _i++) {
					this.vv[_i] = (gen.model.egzotics.v)other.vv[_i].clone();
				}
			};
		this.e = other.e;
		this.ee = new java.util.LinkedHashSet<gen.model.egzotics.E>(other.ee);
		this.__originalValue = other.__originalValue;
	}

	@Override
	public Object clone() {
		return new PksV(this);
	}

	@Override
	public String toString() {
		return "PksV(" + URI + ')';
	}
	
	
	public PksV(
			final gen.model.egzotics.v v,
			final gen.model.egzotics.v[] vv,
			final gen.model.egzotics.E e,
			final java.util.Set<gen.model.egzotics.E> ee) {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		setV(v);
		setVv(vv);
		setE(e);
		setEe(ee);
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	private static final long serialVersionUID = 8161909294022938712L;
	
	@com.fasterxml.jackson.annotation.JsonCreator private PksV(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("v") final gen.model.egzotics.v v,
			@com.fasterxml.jackson.annotation.JsonProperty("vv") final gen.model.egzotics.v[] vv,
			@com.fasterxml.jackson.annotation.JsonProperty("e") final gen.model.egzotics.E e,
			@com.fasterxml.jackson.annotation.JsonProperty("ee") final java.util.Set<gen.model.egzotics.E> ee) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.v = v == null ? new gen.model.egzotics.v() : v;
		this.vv = vv == null ? new gen.model.egzotics.v[] { } : vv;
		this.e = e == null ? gen.model.egzotics.E.A : e;
		this.ee = ee == null ? new java.util.LinkedHashSet<gen.model.egzotics.E>(4) : ee;
	}

	
	private gen.model.egzotics.v v;

	
	@com.fasterxml.jackson.annotation.JsonProperty("v")
	public gen.model.egzotics.v getV()  {
		
		return v;
	}

	
	public PksV setV(final gen.model.egzotics.v value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"v\" cannot be null!");
		this.v = value;
		
		return this;
	}

	private static final gen.model.egzotics.v[] _defaultvv = new gen.model.egzotics.v[] { };
	
	private gen.model.egzotics.v[] vv;

	
	@com.fasterxml.jackson.annotation.JsonProperty("vv")
	public gen.model.egzotics.v[] getVv()  {
		
		return vv;
	}

	
	public PksV setVv(final gen.model.egzotics.v[] value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"vv\" cannot be null!");
		org.revenj.Guards.checkNulls(value);
		this.vv = value;
		
		return this;
	}

	
	private gen.model.egzotics.E e;

	
	@com.fasterxml.jackson.annotation.JsonProperty("e")
	public gen.model.egzotics.E getE()  {
		
		return e;
	}

	
	public PksV setE(final gen.model.egzotics.E value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"e\" cannot be null!");
		this.e = value;
		
		return this;
	}

	
	private java.util.Set<gen.model.egzotics.E> ee;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ee")
	public java.util.Set<gen.model.egzotics.E> getEe()  {
		
		return ee;
	}

	
	public PksV setEe(final java.util.Set<gen.model.egzotics.E> value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"ee\" cannot be null!");
		org.revenj.Guards.checkNulls(value);
		this.ee = value;
		
		return this;
	}

	private transient PksV __originalValue;
	
	static {
		gen.model.egzotics.repositories.PksVRepository.__setupPersist(
			(aggregates, arg) -> {
				try {
					for (gen.model.egzotics.PksV agg : aggregates) {
						 
						agg.URI = arg.getValue().buildURI(arg.getKey(), agg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(oldAggregates, newAggregates) -> {
				for (int i = 0; i < newAggregates.size(); i++) {
					gen.model.egzotics.PksV oldAgg = oldAggregates.get(i);
					gen.model.egzotics.PksV newAgg = newAggregates.get(i);
					 
				}
			},
			aggregates -> { 
				for (gen.model.egzotics.PksV agg : aggregates) { 
				}
			},
			agg -> { 
				
		PksV _res = agg.__originalValue;
		agg.__originalValue = (PksV)agg.clone();
		if (_res != null) {
			return _res;
		}
				return null;
			}
		);
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

	static void __serializeJsonObjectMinimal(final PksV self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
		
		sw.writeAscii(",\"v\":{", 6);
		
					gen.model.egzotics.v.__serializeJsonObjectMinimal(self.v, sw, false);
					sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
		
		if(self.vv.length != 0) {
			sw.writeAscii(",\"vv\":[", 7);
			gen.model.egzotics.v item = self.vv[0];
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.egzotics.v.__serializeJsonObjectMinimal(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			for(int i = 1; i < self.vv.length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
				item = self.vv[i];
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.egzotics.v.__serializeJsonObjectMinimal(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		
		if(self.e != gen.model.egzotics.E.A) {
			sw.writeAscii(",\"e\":\"", 6);
			sw.writeAscii(self.e.name());
			sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
		}
		
		if(self.ee.size() != 0) {
			sw.writeAscii(",\"ee\":[", 7);
			gen.model.egzotics.E item;
			java.util.Iterator<gen.model.egzotics.E> iterator = self.ee.iterator();
			int total = self.ee.size() - 1;
			for(int i = 0; i < total; i++) {
				item = iterator.next();
				sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
				sw.writeAscii(item.name());
				sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
			}
			item = iterator.next();
				sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
				sw.writeAscii(item.name());
				sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
	}

	static void __serializeJsonObjectFull(final PksV self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
		
		sw.writeAscii(",\"v\":{", 6);
		
					gen.model.egzotics.v.__serializeJsonObjectFull(self.v, sw, false);
					sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
		
		if(self.vv.length != 0) {
			sw.writeAscii(",\"vv\":[", 7);
			gen.model.egzotics.v item = self.vv[0];
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.egzotics.v.__serializeJsonObjectFull(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			for(int i = 1; i < self.vv.length; i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
				item = self.vv[i];
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.egzotics.v.__serializeJsonObjectFull(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else sw.writeAscii(",\"vv\":[]", 8);
		
		
		sw.writeAscii(",\"e\":\"", 6);
		sw.writeAscii(self.e.name());
		sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
		
		if(self.ee.size() != 0) {
			sw.writeAscii(",\"ee\":[", 7);
			gen.model.egzotics.E item;
			java.util.Iterator<gen.model.egzotics.E> iterator = self.ee.iterator();
			int total = self.ee.size() - 1;
			for(int i = 0; i < total; i++) {
				item = iterator.next();
				sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
				sw.writeAscii(item.name());
				sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
			}
			item = iterator.next();
				sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
				sw.writeAscii(item.name());
				sw.writeByte(com.dslplatform.json.JsonWriter.QUOTE);
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else sw.writeAscii(",\"ee\":[]", 8);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<PksV> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<PksV>() {
		@Override
		public PksV deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.egzotics.PksV(reader);
		}
	};

	private PksV(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		gen.model.egzotics.v _v_ = null;
		gen.model.egzotics.v[] _vv_ = _defaultvv;
		gen.model.egzotics.E _e_ = gen.model.egzotics.E.A;
		java.util.Set<gen.model.egzotics.E> _ee_ = new java.util.LinkedHashSet<gen.model.egzotics.E>(4);
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
					case -217300791:
						
					if (nextToken == '{') {
						reader.getNextToken();
						_v_ = gen.model.egzotics.v.JSON_READER.deserialize(reader);
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '{' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case 1531571373:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken == ']') {
							_vv_ = new gen.model.egzotics.v[] { };
						} else {
							java.util.ArrayList<gen.model.egzotics.v> __res = reader.deserializeCollection(gen.model.egzotics.v.JSON_READER);
							_vv_ = __res.toArray(new gen.model.egzotics.v[__res.size()]);
						}
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case -536075552:
						
					if (nextToken == '"') {
						switch(reader.calcHash()) {
							case -1005848884: _e_ = gen.model.egzotics.E.A; break;
							case -955516027: _e_ = gen.model.egzotics.E.B; break;
							case -972293646: _e_ = gen.model.egzotics.E.C; break;
							default:
								throw new java.io.IOException("Unknown enum value: '" + reader.getLastName() + "' at position " + reader.positionInStream());
						}
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '\"' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case 941250399:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							gen.model.egzotics.E __inst;
							String __val;
							
					if (nextToken == '"') {
						switch(reader.calcHash()) {
							case -1005848884: __inst = gen.model.egzotics.E.A; break;
							case -955516027: __inst = gen.model.egzotics.E.B; break;
							case -972293646: __inst = gen.model.egzotics.E.C; break;
							default:
								throw new java.io.IOException("Unknown enum value: '" + reader.getLastName() + "' at position " + reader.positionInStream());
						}
						_ee_.add(__inst);
					} else throw new java.io.IOException("Expecting '\"' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
							while ((nextToken = reader.getNextToken()) == ',') {
								nextToken = reader.getNextToken();
								
					if (nextToken == '"') {
						switch(reader.calcHash()) {
							case -1005848884: __inst = gen.model.egzotics.E.A; break;
							case -955516027: __inst = gen.model.egzotics.E.B; break;
							case -972293646: __inst = gen.model.egzotics.E.C; break;
							default:
								throw new java.io.IOException("Unknown enum value: '" + reader.getLastName() + "' at position " + reader.positionInStream());
						}
						_ee_.add(__inst);
					} else throw new java.io.IOException("Expecting '\"' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
							}
							if (nextToken != ']') throw new java.io.IOException("Expecting ']' at position " + reader.positionInStream() + ". Found " + (char) nextToken);
						}
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
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
					case -217300791:
						
					if (nextToken == '{') {
						reader.getNextToken();
						_v_ = gen.model.egzotics.v.JSON_READER.deserialize(reader);
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '{' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case 1531571373:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken == ']') {
							_vv_ = new gen.model.egzotics.v[] { };
						} else {
							java.util.ArrayList<gen.model.egzotics.v> __res = reader.deserializeCollection(gen.model.egzotics.v.JSON_READER);
							_vv_ = __res.toArray(new gen.model.egzotics.v[__res.size()]);
						}
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case -536075552:
						
					if (nextToken == '"') {
						switch(reader.calcHash()) {
							case -1005848884: _e_ = gen.model.egzotics.E.A; break;
							case -955516027: _e_ = gen.model.egzotics.E.B; break;
							case -972293646: _e_ = gen.model.egzotics.E.C; break;
							default:
								throw new java.io.IOException("Unknown enum value: '" + reader.getLastName() + "' at position " + reader.positionInStream());
						}
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '\"' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case 941250399:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							gen.model.egzotics.E __inst;
							String __val;
							
					if (nextToken == '"') {
						switch(reader.calcHash()) {
							case -1005848884: __inst = gen.model.egzotics.E.A; break;
							case -955516027: __inst = gen.model.egzotics.E.B; break;
							case -972293646: __inst = gen.model.egzotics.E.C; break;
							default:
								throw new java.io.IOException("Unknown enum value: '" + reader.getLastName() + "' at position " + reader.positionInStream());
						}
						_ee_.add(__inst);
					} else throw new java.io.IOException("Expecting '\"' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
							while ((nextToken = reader.getNextToken()) == ',') {
								nextToken = reader.getNextToken();
								
					if (nextToken == '"') {
						switch(reader.calcHash()) {
							case -1005848884: __inst = gen.model.egzotics.E.A; break;
							case -955516027: __inst = gen.model.egzotics.E.B; break;
							case -972293646: __inst = gen.model.egzotics.E.C; break;
							default:
								throw new java.io.IOException("Unknown enum value: '" + reader.getLastName() + "' at position " + reader.positionInStream());
						}
						_ee_.add(__inst);
					} else throw new java.io.IOException("Expecting '\"' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
							}
							if (nextToken != ']') throw new java.io.IOException("Expecting ']' at position " + reader.positionInStream() + ". Found " + (char) nextToken);
						}
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
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
		if(_v_ == null) throw new java.io.IOException("In entity egzotics.PksV, property v can't be null");
		this.v = _v_;
		this.vv = _vv_;
		this.e = _e_;
		this.ee = _ee_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.egzotics.PksV(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public PksV(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<PksV>[] readers, gen.model.egzotics.converters.PksVConverter converter) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<PksV> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = converter.buildURI(reader, this);
		this.__locator = java.util.Optional.ofNullable(reader.locator);
		this.__originalValue = (PksV)this.clone();
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<PksV>[] readers, gen.model.egzotics.converters.vConverter __converter_v, int __index___v, gen.model.egzotics.converters.vConverter __converter_vv, int __index___vv, int __index___e, int __index___ee) {
		
		readers[__index___v] = (item, reader, context) -> { item.v = __converter_v.from(reader, context); return item; };
		readers[__index___vv] = (item, reader, context) -> { { java.util.List<gen.model.egzotics.v> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, __converter_vv::from); if (__list != null) {item.vv = __list.toArray(new gen.model.egzotics.v[__list.size()]);} else item.vv = new gen.model.egzotics.v[] { }; }; return item; };
		readers[__index___e] = (item, reader, context) -> { item.e = gen.model.egzotics.converters.EConverter.fromReader(reader); return item; };
		readers[__index___ee] = (item, reader, context) -> { { java.util.List<gen.model.egzotics.E> __list = org.revenj.postgres.converters.EnumConverter.parseCollection(reader, context, gen.model.egzotics.E.A, gen.model.egzotics.converters.EConverter::convertEnum); if (__list != null) {item.ee = new java.util.LinkedHashSet<gen.model.egzotics.E>(__list);} else item.ee = new java.util.LinkedHashSet<gen.model.egzotics.E>(4); }; return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<PksV>[] readers, final gen.model.egzotics.converters.vConverter __converter_v, int __index__extended_v, final gen.model.egzotics.converters.vConverter __converter_vv, int __index__extended_vv, int __index__extended_e, int __index__extended_ee) {
		
		readers[__index__extended_v] = (item, reader, context) -> { item.v = __converter_v.fromExtended(reader, context); return item; };
		readers[__index__extended_vv] = (item, reader, context) -> { { java.util.List<gen.model.egzotics.v> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, __converter_vv::fromExtended); if (__list != null) {item.vv = __list.toArray(new gen.model.egzotics.v[__list.size()]);} else item.vv = new gen.model.egzotics.v[] { }; }; return item; };
		readers[__index__extended_e] = (item, reader, context) -> { item.e = gen.model.egzotics.converters.EConverter.fromReader(reader); return item; };
		readers[__index__extended_ee] = (item, reader, context) -> { { java.util.List<gen.model.egzotics.E> __list = org.revenj.postgres.converters.EnumConverter.parseCollection(reader, context, gen.model.egzotics.E.A, gen.model.egzotics.converters.EConverter::convertEnum); if (__list != null) {item.ee = new java.util.LinkedHashSet<gen.model.egzotics.E>(__list);} else item.ee = new java.util.LinkedHashSet<gen.model.egzotics.E>(4); }; return item; };
	}
}
