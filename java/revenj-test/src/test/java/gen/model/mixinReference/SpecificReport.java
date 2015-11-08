package gen.model.mixinReference;


@com.fasterxml.jackson.annotation.JsonTypeName("mixinReference.SpecificReport")
public class SpecificReport   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot, gen.model.mixinReference.Report<gen.model.mixinReference.SpecificReport>, com.dslplatform.json.JsonObject {
	
	
	
	public SpecificReport() {
			
		this.ID = 0;
		this.ID = --__SequenceCounterID__;
		this.URI = java.lang.Integer.toString(this.ID);
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
		if (obj == null || obj instanceof SpecificReport == false)
			return false;
		final SpecificReport other = (SpecificReport) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final SpecificReport other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.ID == other.ID))
			return false;
		if(!(this.authorURI == other.authorURI || this.authorURI != null && this.authorURI.equals(other.authorURI)))
			return false;
		if(!(this.authorID == other.authorID))
			return false;
		return true;
	}

	private SpecificReport(SpecificReport other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.ID = other.ID;
		this.__originalValue = other.__originalValue;
		this.authorURI = other.authorURI;
		this.authorID = other.authorID;
	}

	@Override
	public Object clone() {
		return new SpecificReport(this);
	}

	@Override
	public String toString() {
		return "SpecificReport(" + URI + ')';
	}
	
	
	public SpecificReport(
			final gen.model.mixinReference.Author author) {
			
		this.ID = --__SequenceCounterID__;
		setAuthor(author);
		this.URI = java.lang.Integer.toString(this.ID);
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	private static final long serialVersionUID = -4137289521328466905L;
	
	@com.fasterxml.jackson.annotation.JsonCreator private SpecificReport(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("ID") final int ID,
			@com.fasterxml.jackson.annotation.JsonProperty("authorURI") final String authorURI,
			@com.fasterxml.jackson.annotation.JsonProperty("authorID") final int authorID) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.ID = ID;
		this.authorURI = authorURI;
		this.authorID = authorID;
	}

	
	private int ID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ID")
	public int getID()  {
		
		return ID;
	}

	
	private SpecificReport setID(final int value) {
		
		this.ID = value;
		
		return this;
	}

	
	static {
		gen.model.mixinReference.repositories.SpecificReportRepository.__setupSequenceID((items, connection) -> {
			try (java.sql.PreparedStatement st = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT nextval('\"mixinReference\".\"SpecificReport_ID_seq\"'::regclass)::int FROM generate_series(1, ?)")) {
				st.setInt(1, items.size());
				try (java.sql.ResultSet rs = st.executeQuery()) {
					java.util.Iterator<SpecificReport> iterator = items.iterator();
					while (rs.next()) {
						iterator.next().setID(rs.getInt(1));
					}
				}
			} catch (java.sql.SQLException e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	private static int __SequenceCounterID__;
	private transient SpecificReport __originalValue;
	
	static {
		gen.model.mixinReference.repositories.SpecificReportRepository.__setupPersist(
			(aggregates, arg) -> {
				try {
					for (gen.model.mixinReference.SpecificReport agg : aggregates) {
						 
						agg.URI = gen.model.mixinReference.converters.SpecificReportConverter.buildURI(arg, agg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(oldAggregates, newAggregates) -> {
				for (int i = 0; i < newAggregates.size(); i++) {
					gen.model.mixinReference.SpecificReport oldAgg = oldAggregates.get(i);
					gen.model.mixinReference.SpecificReport newAgg = newAggregates.get(i);
					 
				}
			},
			aggregates -> { 
				for (gen.model.mixinReference.SpecificReport agg : aggregates) { 
				}
			},
			agg -> { 
				
		SpecificReport _res = agg.__originalValue;
		agg.__originalValue = (SpecificReport)agg.clone();
		if (_res != null) {
			return _res;
		}
				return null;
			}
		);
	}
	
	private gen.model.mixinReference.Author author;

	
	@com.fasterxml.jackson.annotation.JsonIgnore
	public gen.model.mixinReference.Author getAuthor()  {
		
		
		if (__locator.isPresent() && (author != null && !author.getURI().equals(authorURI) || author == null && authorURI != null)) {
			gen.model.mixinReference.repositories.AuthorRepository repository = __locator.get().resolve(gen.model.mixinReference.repositories.AuthorRepository.class);
			author = repository.find(authorURI).orElse(null);
		}
		return author;
	}

	
	public SpecificReport setAuthor(final gen.model.mixinReference.Author value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"author\" cannot be null!");
		
		if(value != null && value.getURI() == null) throw new IllegalArgumentException("Reference \"mixinReference.Author\" for property \"author\" must be persisted before it's assigned");
		this.author = value;
		
		
		this.authorID = value.getID();
		this.authorURI = value.getURI();
		return this;
	}

	
	private String authorURI;

	
	@com.fasterxml.jackson.annotation.JsonProperty("authorURI")
	public String getAuthorURI()  {
		
		if (this.author != null) this.authorURI = this.author.getURI();
		return this.authorURI;
	}

	
	private int authorID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("authorID")
	public int getAuthorID()  {
		
		if (this.author != null) this.authorID = this.author.getID();
		return authorID;
	}

	
	private SpecificReport setAuthorID(final int value) {
		
		this.authorID = value;
		
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

	static public void __serializeJsonObjectMinimal(final SpecificReport self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (self.ID != 0) {
				sw.writeAscii(",\"ID\":", 6);
				com.dslplatform.json.NumberConverter.serialize(self.ID, sw);
			}
		
			if(self.authorURI != null) {
				sw.writeAscii(",\"authorURI\":");
				com.dslplatform.json.StringConverter.serializeShort(self.authorURI, sw);
			}
		
			if (self.authorID != 0) {
				sw.writeAscii(",\"authorID\":", 12);
				com.dslplatform.json.NumberConverter.serialize(self.authorID, sw);
			}
	}

	static public void __serializeJsonObjectFull(final SpecificReport self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			sw.writeAscii(",\"ID\":", 6);
			com.dslplatform.json.NumberConverter.serialize(self.ID, sw);
		
			sw.writeAscii(",\"authorURI\":");
			com.dslplatform.json.StringConverter.serializeShortNullable(self.authorURI, sw);
		
			
			sw.writeAscii(",\"authorID\":", 12);
			com.dslplatform.json.NumberConverter.serialize(self.authorID, sw);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<SpecificReport> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<SpecificReport>() {
		@Override
		public SpecificReport deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.mixinReference.SpecificReport(reader);
		}
	};

	private SpecificReport(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		int _ID_ = 0;
		String _authorURI_ = null;
		int _authorID_ = 0;
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
					case 1458105184:
						_ID_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case 624043688:
						_authorURI_ = com.dslplatform.json.StringConverter.deserialize(reader);
							nextToken = reader.getNextToken();
						break;
					case -532237909:
						_authorID_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
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
					case 1458105184:
						_ID_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case 624043688:
						_authorURI_ = com.dslplatform.json.StringConverter.deserialize(reader);
							nextToken = reader.getNextToken();
						break;
					case -532237909:
						_authorID_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
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
		this.ID = _ID_;
		if(_authorURI_ == null) throw new java.io.IOException("In entity mixinReference.SpecificReport, property author can't be null. authorURI provided as null");
		this.authorURI = _authorURI_;
		this.authorID = _authorID_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.mixinReference.SpecificReport(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public SpecificReport(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<SpecificReport>[] readers) throws java.io.IOException {
		for (org.revenj.postgres.ObjectConverter.Reader<SpecificReport> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.mixinReference.converters.SpecificReportConverter.buildURI(reader, this);
		this.__locator = java.util.Optional.ofNullable(reader.locator);
		this.__originalValue = (SpecificReport)this.clone();
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<SpecificReport>[] readers, int __index___ID, int __index___authorURI, int __index___authorID) {
		
		readers[__index___ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
		readers[__index___authorURI] = (item, reader, context) -> { item.authorURI = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); return item; };
		readers[__index___authorID] = (item, reader, context) -> { item.authorID = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<SpecificReport>[] readers, int __index__extended_ID, int __index__extended_authorURI, int __index__extended_authorID) {
		
		readers[__index__extended_ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
		readers[__index__extended_authorURI] = (item, reader, context) -> { item.authorURI = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); return item; };
		readers[__index__extended_authorID] = (item, reader, context) -> { item.authorID = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
	}
}
