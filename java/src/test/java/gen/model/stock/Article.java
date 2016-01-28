/*
* Created by DSL Platform
* v1.5.5871.15913 
*/

package gen.model.stock;



public class Article   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot, com.dslplatform.json.JsonObject {
	
	
	
	public Article() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.ID = 0L;
		this.ID = --__SequenceCounterID__;
		this.projectID = 0;
		this.sku = "";
		this.title = "";
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
		if (obj == null || obj instanceof Article == false)
			return false;
		final Article other = (Article) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final Article other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.ID == other.ID))
			return false;
		if(!(this.projectID == other.projectID))
			return false;
		if(!(this.sku.equals(other.sku)))
			return false;
		if(!(this.title.equals(other.title)))
			return false;
		return true;
	}

	private Article(Article other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.ID = other.ID;
		this.projectID = other.projectID;
		this.sku = other.sku;
		this.title = other.title;
		this.__originalValue = other.__originalValue;
	}

	@Override
	public Object clone() {
		return new Article(this);
	}

	@Override
	public String toString() {
		return "Article(" + URI + ')';
	}
	
	@com.fasterxml.jackson.annotation.JsonCreator private Article(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("ID") final long ID,
			@com.fasterxml.jackson.annotation.JsonProperty("projectID") final int projectID,
			@com.fasterxml.jackson.annotation.JsonProperty("sku") final String sku,
			@com.fasterxml.jackson.annotation.JsonProperty("title") final String title) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.ID = ID;
		this.projectID = projectID;
		this.sku = sku == null ? "" : sku;
		this.title = title == null ? "" : title;
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	private static final long serialVersionUID = -4645098887917980170L;
	
	private long ID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ID")
	public long getID()  {
		
		return ID;
	}

	
	private Article setID(final long value) {
		
		this.ID = value;
		
		return this;
	}

	
	static {
		gen.model.stock.repositories.ArticleRepository.__setupSequenceID((items, connection) -> {
			try (java.sql.PreparedStatement st = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT nextval('\"stock\".\"Article_ID_seq\"'::regclass)::bigint FROM generate_series(1, ?)")) {
				st.setInt(1, items.size());
				try (java.sql.ResultSet rs = st.executeQuery()) {
					java.util.Iterator<Article> iterator = items.iterator();
					while (rs.next()) {
						iterator.next().setID(rs.getLong(1));
					}
				}
			} catch (java.sql.SQLException e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	private static long __SequenceCounterID__;
	
	private int projectID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("projectID")
	public int getProjectID()  {
		
		return projectID;
	}

	
	public Article setProjectID(final int value) {
		
		this.projectID = value;
		
		return this;
	}

	
	private String sku;

	
	@com.fasterxml.jackson.annotation.JsonProperty("sku")
	public String getSku()  {
		
		return sku;
	}

	
	public Article setSku(final String value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"sku\" cannot be null!");
		org.revenj.Guards.checkLength(value, 10);
		this.sku = value;
		
		return this;
	}

	
	private String title;

	
	@com.fasterxml.jackson.annotation.JsonProperty("title")
	public String getTitle()  {
		
		return title;
	}

	
	public Article setTitle(final String value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"title\" cannot be null!");
		org.revenj.Guards.checkLength(value, 25);
		this.title = value;
		
		return this;
	}

	
	static {
		gen.model.stock.repositories.ArticleRepository.__setupPersist(
			(aggregates, arg) -> {
				try {
					for (gen.model.stock.Article agg : aggregates) {
						 
						agg.URI = gen.model.stock.converters.ArticleConverter.buildURI(arg.getKey(), agg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(aggregates, arg) -> {
				try {
					java.util.List<gen.model.stock.Article> oldAggregates = aggregates.getKey();
					java.util.List<gen.model.stock.Article> newAggregates = aggregates.getValue();
					for (int i = 0; i < newAggregates.size(); i++) {
						gen.model.stock.Article oldAgg = oldAggregates.get(i);
						gen.model.stock.Article newAgg = newAggregates.get(i);
						 
						newAgg.URI = gen.model.stock.converters.ArticleConverter.buildURI(arg.getKey(), newAgg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			aggregates -> { 
				for (gen.model.stock.Article agg : aggregates) { 
				}
			},
			agg -> { 
				
		Article _res = agg.__originalValue;
		agg.__originalValue = (Article)agg.clone();
		if (_res != null) {
			return _res;
		}
				return null;
			}
		);
	}
	private transient Article __originalValue;
	
	public void serialize(final com.dslplatform.json.JsonWriter sw, final boolean minimal) {
		sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
		if (minimal) {
			__serializeJsonObjectMinimal(this, sw, false);
		} else {
			__serializeJsonObjectFull(this, sw, false);
		}
		sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
	}

	static void __serializeJsonObjectMinimal(final Article self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (self.ID != 0L) {
				sw.writeAscii(",\"ID\":", 6);
				com.dslplatform.json.NumberConverter.serialize(self.ID, sw);
			}
		
			if (self.projectID != 0) {
				sw.writeAscii(",\"projectID\":", 13);
				com.dslplatform.json.NumberConverter.serialize(self.projectID, sw);
			}
		
			if (!(self.sku.length() == 0)) {
				sw.writeAscii(",\"sku\":", 7);
				com.dslplatform.json.StringConverter.serializeShort(self.sku, sw);
			}
		
			if (!(self.title.length() == 0)) {
				sw.writeAscii(",\"title\":", 9);
				com.dslplatform.json.StringConverter.serializeShort(self.title, sw);
			}
	}

	static void __serializeJsonObjectFull(final Article self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			sw.writeAscii(",\"ID\":", 6);
			com.dslplatform.json.NumberConverter.serialize(self.ID, sw);
		
			
			sw.writeAscii(",\"projectID\":", 13);
			com.dslplatform.json.NumberConverter.serialize(self.projectID, sw);
		
			
			sw.writeAscii(",\"sku\":", 7);
			com.dslplatform.json.StringConverter.serializeShort(self.sku, sw);
		
			
			sw.writeAscii(",\"title\":", 9);
			com.dslplatform.json.StringConverter.serializeShort(self.title, sw);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<Article> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<Article>() {
		@Override
		public Article deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.stock.Article(reader);
		}
	};

	private Article(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		long _ID_ = 0L;
		int _projectID_ = 0;
		String _sku_ = "";
		String _title_ = "";
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
						_ID_ = com.dslplatform.json.NumberConverter.deserializeLong(reader);
					nextToken = reader.getNextToken();
						break;
					case 504496707:
						_projectID_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case -651451878:
						_sku_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -1738164983:
						_title_ = com.dslplatform.json.StringConverter.deserialize(reader);
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
						_ID_ = com.dslplatform.json.NumberConverter.deserializeLong(reader);
					nextToken = reader.getNextToken();
						break;
					case 504496707:
						_projectID_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case -651451878:
						_sku_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -1738164983:
						_title_ = com.dslplatform.json.StringConverter.deserialize(reader);
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
		this.projectID = _projectID_;
		this.sku = _sku_;
		this.title = _title_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.stock.Article(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public Article(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Article>[] readers) throws java.io.IOException {
		this.__locator = reader.getLocator();
		for (org.revenj.postgres.ObjectConverter.Reader<Article> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.stock.converters.ArticleConverter.buildURI(reader, this);
		this.__originalValue = (Article)this.clone();
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Article>[] readers, int __index___ID, int __index___projectID, int __index___sku, int __index___title) {
		
		readers[__index___ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.LongConverter.parse(reader); return item; };
		readers[__index___projectID] = (item, reader, context) -> { item.projectID = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
		readers[__index___sku] = (item, reader, context) -> { item.sku = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
		readers[__index___title] = (item, reader, context) -> { item.title = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Article>[] readers, int __index__extended_ID, int __index__extended_projectID, int __index__extended_sku, int __index__extended_title) {
		
		readers[__index__extended_ID] = (item, reader, context) -> { item.ID = org.revenj.postgres.converters.LongConverter.parse(reader); return item; };
		readers[__index__extended_projectID] = (item, reader, context) -> { item.projectID = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
		readers[__index__extended_sku] = (item, reader, context) -> { item.sku = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
		readers[__index__extended_title] = (item, reader, context) -> { item.title = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); return item; };
	}
	
	
	public Article(
			final int projectID,
			final String sku,
			final String title) {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.ID = --__SequenceCounterID__;
		setProjectID(projectID);
		setSku(sku);
		setTitle(title);
	}

}
