package gen.model.calc;



public class Realm   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot, com.dslplatform.json.JsonObject {
	
	
	
	public Realm() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
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
		if (obj == null || obj instanceof Realm == false)
			return false;
		final Realm other = (Realm) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final Realm other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.infoURI == other.infoURI || this.infoURI != null && this.infoURI.equals(other.infoURI)))
			return false;
		if(!(this.infoID.equals(other.infoID)))
			return false;
		if(!(this.refTypeURI == other.refTypeURI || this.refTypeURI != null && this.refTypeURI.equals(other.refTypeURI)))
			return false;
		if(!(this.type.equals(other.type)))
			return false;
		return true;
	}

	private Realm(Realm other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.infoURI = other.infoURI;
		this.infoID = other.infoID;
		this.refTypeURI = other.refTypeURI;
		this.type = other.type;
		this.__originalValue = other.__originalValue;
	}

	@Override
	public Object clone() {
		return new Realm(this);
	}

	@Override
	public String toString() {
		return "Realm(" + URI + ')';
	}
	
	
	public Realm(
			final gen.model.calc.Info info,
			final gen.model.calc.Type refType) {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		setInfo(info);
		setRefType(refType);
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	private static final long serialVersionUID = 329511653106833079L;
	
	@com.fasterxml.jackson.annotation.JsonCreator private Realm(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("infoURI") final String infoURI,
			@com.fasterxml.jackson.annotation.JsonProperty("infoID") final String infoID,
			@com.fasterxml.jackson.annotation.JsonProperty("refTypeURI") final String refTypeURI,
			@com.fasterxml.jackson.annotation.JsonProperty("type") final String type,
			@com.fasterxml.jackson.annotation.JsonProperty("id") final String id,
			@com.fasterxml.jackson.annotation.JsonProperty("name") final String name) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.infoURI = infoURI;
		this.infoID = infoID == null ? "" : infoID;
		this.refTypeURI = refTypeURI;
		this.type = type == null ? "" : type;
		this.id = id == null ? "" : id;
		this.name = name == null ? "" : name;
	}

	
	private gen.model.calc.Info info;

	
	@com.fasterxml.jackson.annotation.JsonIgnore
	public gen.model.calc.Info getInfo()  {
		
		
		if (__locator.isPresent() && (info != null && !info.getURI().equals(infoURI) || info == null && infoURI != null)) {
			gen.model.calc.repositories.InfoRepository repository = __locator.get().resolve(gen.model.calc.repositories.InfoRepository.class);
			info = repository.find(infoURI).orElse(null);
		}
		return info;
	}

	
	public Realm setInfo(final gen.model.calc.Info value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"info\" cannot be null!");
		
		if(value != null && value.getURI() == null) throw new IllegalArgumentException("Reference \"calc.Info\" for property \"info\" must be persisted before it's assigned");
		this.info = value;
		
		
		this.infoID = value.getCode();
		this.infoURI = value.getURI();
		return this;
	}

	
	private String infoURI;

	
	@com.fasterxml.jackson.annotation.JsonProperty("infoURI")
	public String getInfoURI()  {
		
		if (this.info != null) this.infoURI = this.info.getURI();
		return this.infoURI;
	}

	
	private String infoID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("infoID")
	public String getInfoID()  {
		
		if (this.info != null) this.infoID = this.info.getCode();
		return infoID;
	}

	
	private Realm setInfoID(final String value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"infoID\" cannot be null!");
		this.infoID = value;
		
		return this;
	}

	
	private gen.model.calc.Type refType;

	
	@com.fasterxml.jackson.annotation.JsonIgnore
	public gen.model.calc.Type getRefType()  {
		
		
		if (__locator.isPresent() && (refType != null && !refType.getURI().equals(refTypeURI) || refType == null && refTypeURI != null)) {
			gen.model.calc.repositories.TypeRepository repository = __locator.get().resolve(gen.model.calc.repositories.TypeRepository.class);
			refType = repository.find(refTypeURI).orElse(null);
		}
		return refType;
	}

	
	public Realm setRefType(final gen.model.calc.Type value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"refType\" cannot be null!");
		
		if(value != null && value.getURI() == null) throw new IllegalArgumentException("Reference \"calc.Type\" for property \"refType\" must be persisted before it's assigned");
		this.refType = value;
		
		
		this.type = value.getSuffix();
		this.refTypeURI = value.getURI();
		return this;
	}

	
	private String refTypeURI;

	
	@com.fasterxml.jackson.annotation.JsonProperty("refTypeURI")
	public String getRefTypeURI()  {
		
		if (this.refType != null) this.refTypeURI = this.refType.getURI();
		return this.refTypeURI;
	}

	
	private String type;

	
	@com.fasterxml.jackson.annotation.JsonProperty("type")
	public String getType()  {
		
		if (this.refType != null) this.type = this.refType.getSuffix();
		return type;
	}

	
	private Realm setType(final String value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"type\" cannot be null!");
		this.type = value;
		
		return this;
	}

	
	private String id;

	
	@com.fasterxml.jackson.annotation.JsonProperty("id")
	public String getId()  {
		
		this.id = __calculated_id.apply(this);
		return this.id;
	}

	private static final java.util.function.Function<gen.model.calc.Realm, String> __calculated_id = it ->  (it.getInfo().getCode() + it.getType());
	
	private String name;

	
	@com.fasterxml.jackson.annotation.JsonProperty("name")
	public String getName()  {
		
		this.name = __calculated_name.apply(this);
		return this.name;
	}

	private static final java.util.function.Function<gen.model.calc.Realm, String> __calculated_name = it ->  ( ( (it.getInfo().getName() + " (") + it.getRefType().getDescription()) + ")");
	private transient Realm __originalValue;
	
	static {
		gen.model.calc.repositories.RealmRepository.__setupPersist(
			(aggregates, arg) -> {
				try {
					for (gen.model.calc.Realm agg : aggregates) {
						 
						agg.URI = gen.model.calc.converters.RealmConverter.buildURI(arg, agg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(oldAggregates, newAggregates) -> {
				for (int i = 0; i < newAggregates.size(); i++) {
					gen.model.calc.Realm oldAgg = oldAggregates.get(i);
					gen.model.calc.Realm newAgg = newAggregates.get(i);
					 
				}
			},
			aggregates -> { 
				for (gen.model.calc.Realm agg : aggregates) { 
				}
			},
			agg -> { 
				
		Realm _res = agg.__originalValue;
		agg.__originalValue = (Realm)agg.clone();
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

	static void __serializeJsonObjectMinimal(final Realm self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if(self.infoURI != null) {
				sw.writeAscii(",\"infoURI\":");
				com.dslplatform.json.StringConverter.serializeShort(self.infoURI, sw);
			}
		
			if (!(self.infoID.length() == 0)) {
				sw.writeAscii(",\"infoID\":", 10);
				sw.writeString(self.infoID);
			}
		
			if(self.refTypeURI != null) {
				sw.writeAscii(",\"refTypeURI\":");
				com.dslplatform.json.StringConverter.serializeShort(self.refTypeURI, sw);
			}
		
			if (!(self.type.length() == 0)) {
				sw.writeAscii(",\"type\":", 8);
				sw.writeString(self.type);
			}
		
			if (!(self.id.length() == 0)) {
				sw.writeAscii(",\"id\":", 6);
				sw.writeString(self.id);
			}
		
			if (!(self.name.length() == 0)) {
				sw.writeAscii(",\"name\":", 8);
				sw.writeString(self.name);
			}
	}

	static void __serializeJsonObjectFull(final Realm self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			sw.writeAscii(",\"infoURI\":");
			com.dslplatform.json.StringConverter.serializeShortNullable(self.infoURI, sw);
		
			
			sw.writeAscii(",\"infoID\":", 10);
			sw.writeString(self.infoID);
		
			sw.writeAscii(",\"refTypeURI\":");
			com.dslplatform.json.StringConverter.serializeShortNullable(self.refTypeURI, sw);
		
			
			sw.writeAscii(",\"type\":", 8);
			sw.writeString(self.type);
		
			
			sw.writeAscii(",\"id\":", 6);
			sw.writeString(self.id);
		
			
			sw.writeAscii(",\"name\":", 8);
			sw.writeString(self.name);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<Realm> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<Realm>() {
		@Override
		public Realm deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.calc.Realm(reader);
		}
	};

	private Realm(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		String _infoURI_ = null;
		String _infoID_ = "";
		String _refTypeURI_ = null;
		String _type_ = "";
		String _id_ = "";
		String _name_ = "";
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
					case 553270109:
						_infoURI_ = com.dslplatform.json.StringConverter.deserialize(reader);
							nextToken = reader.getNextToken();
						break;
					case -823552864:
						_infoID_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -1072215980:
						_refTypeURI_ = com.dslplatform.json.StringConverter.deserialize(reader);
							nextToken = reader.getNextToken();
						break;
					case 1361572173:
						_type_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case 926444256:
						_id_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -1925595674:
						_name_ = com.dslplatform.json.StringConverter.deserialize(reader);
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
					case 553270109:
						_infoURI_ = com.dslplatform.json.StringConverter.deserialize(reader);
							nextToken = reader.getNextToken();
						break;
					case -823552864:
						_infoID_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -1072215980:
						_refTypeURI_ = com.dslplatform.json.StringConverter.deserialize(reader);
							nextToken = reader.getNextToken();
						break;
					case 1361572173:
						_type_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case 926444256:
						_id_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -1925595674:
						_name_ = com.dslplatform.json.StringConverter.deserialize(reader);
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
		if(_infoURI_ == null) throw new java.io.IOException("In entity calc.Realm, property info can't be null. infoURI provided as null");
		this.infoURI = _infoURI_;
		this.infoID = _infoID_;
		if(_refTypeURI_ == null) throw new java.io.IOException("In entity calc.Realm, property refType can't be null. refTypeURI provided as null");
		this.refTypeURI = _refTypeURI_;
		this.type = _type_;
		this.id = _id_;
		this.name = _name_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.calc.Realm(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public Realm(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Realm>[] readers) throws java.io.IOException {
		this.__locator = java.util.Optional.ofNullable(reader.locator);
		for (org.revenj.postgres.ObjectConverter.Reader<Realm> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.calc.converters.RealmConverter.buildURI(reader, this);
		this.__originalValue = (Realm)this.clone();
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Realm>[] readers, int __index___infoURI, int __index___infoID, int __index___refTypeURI, int __index___type, int __index___id) {
		
		readers[__index___infoURI] = (item, reader, context) -> { item.infoURI = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); return item; };
		readers[__index___infoID] = (item, reader, context) -> { item.infoID = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
		readers[__index___refTypeURI] = (item, reader, context) -> { item.refTypeURI = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); return item; };
		readers[__index___type] = (item, reader, context) -> { item.type = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Realm>[] readers, int __index__extended_infoURI, int __index__extended_infoID, int __index__extended_refTypeURI, int __index__extended_type, int __index__extended_id) {
		
		readers[__index__extended_infoURI] = (item, reader, context) -> { item.infoURI = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); return item; };
		readers[__index__extended_infoID] = (item, reader, context) -> { item.infoID = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
		readers[__index__extended_refTypeURI] = (item, reader, context) -> { item.refTypeURI = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); return item; };
		readers[__index__extended_type] = (item, reader, context) -> { item.type = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
	}
}
