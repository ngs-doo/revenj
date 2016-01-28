/*
* Created by DSL Platform
* v1.5.5871.15913 
*/

package gen.model.stock;



public class Analysis   implements java.lang.Cloneable, java.io.Serializable, org.revenj.patterns.AggregateRoot, com.dslplatform.json.JsonObject {
	
	
	
	public Analysis() {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		this.projectID = 0;
		this.articleID = 0;
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
		if (obj == null || obj instanceof Analysis == false)
			return false;
		final Analysis other = (Analysis) obj;
		return URI.equals(other.URI);
	}

	public boolean deepEquals(final Analysis other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!URI.equals(other.URI))
			return false;
		
		if(!(this.projectID == other.projectID))
			return false;
		if(!(this.articleID == other.articleID))
			return false;
		if(!(this.abc == other.abc || this.abc != null && this.abc.equals(other.abc)))
			return false;
		if(!(this.xyz == other.xyz || this.xyz != null && this.xyz.equals(other.xyz)))
			return false;
		if(!(this.clazz == other.clazz || this.clazz != null && this.clazz.equals(other.clazz)))
			return false;
		return true;
	}

	private Analysis(Analysis other) {
		this.URI = other.URI;
		this.__locator = other.__locator;
		this.projectID = other.projectID;
		this.articleID = other.articleID;
		this.abc = other.abc;
		this.xyz = other.xyz;
		this.clazz = other.clazz;
		this.__originalValue = other.__originalValue;
	}

	@Override
	public Object clone() {
		return new Analysis(this);
	}

	@Override
	public String toString() {
		return "Analysis(" + URI + ')';
	}
	
	@com.fasterxml.jackson.annotation.JsonCreator private Analysis(
			@com.fasterxml.jackson.annotation.JsonProperty("URI") final String URI ,
			@com.fasterxml.jackson.annotation.JacksonInject("__locator") final org.revenj.patterns.ServiceLocator __locator,
			@com.fasterxml.jackson.annotation.JsonProperty("projectID") final int projectID,
			@com.fasterxml.jackson.annotation.JsonProperty("articleID") final int articleID,
			@com.fasterxml.jackson.annotation.JsonProperty("abc") final String abc,
			@com.fasterxml.jackson.annotation.JsonProperty("xyz") final String xyz,
			@com.fasterxml.jackson.annotation.JsonProperty("clazz") final String clazz) {
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.__locator = java.util.Optional.ofNullable(__locator);
		this.projectID = projectID;
		this.articleID = articleID;
		this.abc = abc;
		this.xyz = xyz;
		this.clazz = clazz;
	}

	
	private transient java.util.Optional<org.revenj.patterns.ServiceLocator> __locator = java.util.Optional.empty();
	private static final long serialVersionUID = -4497857989877041664L;
	
	private int projectID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("projectID")
	public int getProjectID()  {
		
		return projectID;
	}

	
	public Analysis setProjectID(final int value) {
		
		this.projectID = value;
		
		return this;
	}

	
	private int articleID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("articleID")
	public int getArticleID()  {
		
		return articleID;
	}

	
	public Analysis setArticleID(final int value) {
		
		this.articleID = value;
		
		return this;
	}

	
	private String abc;

	
	@com.fasterxml.jackson.annotation.JsonProperty("abc")
	public String getAbc()  {
		
		return abc;
	}

	
	public Analysis setAbc(final String value) {
		
		if (value != null) org.revenj.Guards.checkLength(value, 1);
		this.abc = value;
		
		return this;
	}

	
	private String xyz;

	
	@com.fasterxml.jackson.annotation.JsonProperty("xyz")
	public String getXyz()  {
		
		return xyz;
	}

	
	public Analysis setXyz(final String value) {
		
		if (value != null) org.revenj.Guards.checkLength(value, 1);
		this.xyz = value;
		
		return this;
	}

	
	private String clazz;

	
	@com.fasterxml.jackson.annotation.JsonProperty("clazz")
	public String getClazz()  {
		
		return clazz;
	}

	
	public Analysis setClazz(final String value) {
		
		if (value != null) org.revenj.Guards.checkLength(value, 50);
		this.clazz = value;
		
		return this;
	}

	
	static {
		gen.model.stock.repositories.AnalysisRepository.__setupPersist(
			(aggregates, arg) -> {
				try {
					for (gen.model.stock.Analysis agg : aggregates) {
						 
						agg.URI = gen.model.stock.converters.AnalysisConverter.buildURI(arg.getKey(), agg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			(aggregates, arg) -> {
				try {
					java.util.List<gen.model.stock.Analysis> oldAggregates = aggregates.getKey();
					java.util.List<gen.model.stock.Analysis> newAggregates = aggregates.getValue();
					for (int i = 0; i < newAggregates.size(); i++) {
						gen.model.stock.Analysis oldAgg = oldAggregates.get(i);
						gen.model.stock.Analysis newAgg = newAggregates.get(i);
						 
						newAgg.URI = gen.model.stock.converters.AnalysisConverter.buildURI(arg.getKey(), newAgg);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			},
			aggregates -> { 
				for (gen.model.stock.Analysis agg : aggregates) { 
				}
			},
			agg -> { 
				
		Analysis _res = agg.__originalValue;
		agg.__originalValue = (Analysis)agg.clone();
		if (_res != null) {
			return _res;
		}
				return null;
			}
		);
	}
	private transient Analysis __originalValue;
	
	public void serialize(final com.dslplatform.json.JsonWriter sw, final boolean minimal) {
		sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
		if (minimal) {
			__serializeJsonObjectMinimal(this, sw, false);
		} else {
			__serializeJsonObjectFull(this, sw, false);
		}
		sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
	}

	static void __serializeJsonObjectMinimal(final Analysis self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (self.projectID != 0) {
				sw.writeAscii(",\"projectID\":", 13);
				com.dslplatform.json.NumberConverter.serialize(self.projectID, sw);
			}
		
			if (self.articleID != 0) {
				sw.writeAscii(",\"articleID\":", 13);
				com.dslplatform.json.NumberConverter.serialize(self.articleID, sw);
			}
		
			if (self.abc != null) {
				sw.writeAscii(",\"abc\":", 7);
				com.dslplatform.json.StringConverter.serializeShort(self.abc, sw);
			}
		
			if (self.xyz != null) {
				sw.writeAscii(",\"xyz\":", 7);
				com.dslplatform.json.StringConverter.serializeShort(self.xyz, sw);
			}
		
			if (self.clazz != null) {
				sw.writeAscii(",\"clazz\":", 9);
				com.dslplatform.json.StringConverter.serializeShort(self.clazz, sw);
			}
	}

	static void __serializeJsonObjectFull(final Analysis self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
			com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			sw.writeAscii(",\"projectID\":", 13);
			com.dslplatform.json.NumberConverter.serialize(self.projectID, sw);
		
			
			sw.writeAscii(",\"articleID\":", 13);
			com.dslplatform.json.NumberConverter.serialize(self.articleID, sw);
		
			
			if (self.abc != null) {
				sw.writeAscii(",\"abc\":", 7);
				com.dslplatform.json.StringConverter.serializeShort(self.abc, sw);
			} else {
				sw.writeAscii(",\"abc\":null", 11);
			}
		
			
			if (self.xyz != null) {
				sw.writeAscii(",\"xyz\":", 7);
				com.dslplatform.json.StringConverter.serializeShort(self.xyz, sw);
			} else {
				sw.writeAscii(",\"xyz\":null", 11);
			}
		
			
			if (self.clazz != null) {
				sw.writeAscii(",\"clazz\":", 9);
				com.dslplatform.json.StringConverter.serializeShort(self.clazz, sw);
			} else {
				sw.writeAscii(",\"clazz\":null", 13);
			}
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<Analysis> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<Analysis>() {
		@Override
		public Analysis deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.stock.Analysis(reader);
		}
	};

	private Analysis(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		this.__locator = java.util.Optional.ofNullable(reader.context);
		int _projectID_ = 0;
		int _articleID_ = 0;
		String _abc_ = null;
		String _xyz_ = null;
		String _clazz_ = null;
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
					case 504496707:
						_projectID_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case 1386696860:
						_articleID_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case 440920331:
						_abc_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -996022048:
						_xyz_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -1403792415:
						_clazz_ = com.dslplatform.json.StringConverter.deserialize(reader);
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
					case 504496707:
						_projectID_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case 1386696860:
						_articleID_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case 440920331:
						_abc_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -996022048:
						_xyz_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -1403792415:
						_clazz_ = com.dslplatform.json.StringConverter.deserialize(reader);
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
		this.projectID = _projectID_;
		this.articleID = _articleID_;
		this.abc = _abc_;
		this.xyz = _xyz_;
		this.clazz = _clazz_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.stock.Analysis(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
	public Analysis(org.revenj.postgres.PostgresReader reader, int context, org.revenj.postgres.ObjectConverter.Reader<Analysis>[] readers) throws java.io.IOException {
		this.__locator = reader.getLocator();
		for (org.revenj.postgres.ObjectConverter.Reader<Analysis> rdr : readers) {
			rdr.read(this, reader, context);
		}
		URI = gen.model.stock.converters.AnalysisConverter.buildURI(reader, this);
		this.__originalValue = (Analysis)this.clone();
	}

	public static void __configureConverter(org.revenj.postgres.ObjectConverter.Reader<Analysis>[] readers, int __index___projectID, int __index___articleID, int __index___abc, int __index___xyz, int __index___clazz) {
		
		readers[__index___projectID] = (item, reader, context) -> { item.projectID = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
		readers[__index___articleID] = (item, reader, context) -> { item.articleID = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
		readers[__index___abc] = (item, reader, context) -> { item.abc = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); return item; };
		readers[__index___xyz] = (item, reader, context) -> { item.xyz = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); return item; };
		readers[__index___clazz] = (item, reader, context) -> { item.clazz = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); return item; };
	}
	
	public static void __configureConverterExtended(org.revenj.postgres.ObjectConverter.Reader<Analysis>[] readers, int __index__extended_projectID, int __index__extended_articleID, int __index__extended_abc, int __index__extended_xyz, int __index__extended_clazz) {
		
		readers[__index__extended_projectID] = (item, reader, context) -> { item.projectID = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
		readers[__index__extended_articleID] = (item, reader, context) -> { item.articleID = org.revenj.postgres.converters.IntConverter.parse(reader); return item; };
		readers[__index__extended_abc] = (item, reader, context) -> { item.abc = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); return item; };
		readers[__index__extended_xyz] = (item, reader, context) -> { item.xyz = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); return item; };
		readers[__index__extended_clazz] = (item, reader, context) -> { item.clazz = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); return item; };
	}
	
	
	public Analysis(
			final int projectID,
			final int articleID,
			final String abc,
			final String xyz,
			final String clazz) {
			
		URI = java.lang.Integer.toString(System.identityHashCode(this));
		setProjectID(projectID);
		setArticleID(articleID);
		setAbc(abc);
		setXyz(xyz);
		setClazz(clazz);
	}

}
