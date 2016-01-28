/*
* Created by DSL Platform
* v1.5.5871.15913 
*/

package gen.model.stock;



public final class AnalysisGrid   implements java.io.Serializable, com.dslplatform.json.JsonObject, org.revenj.patterns.Identifiable {
	
	
	@com.fasterxml.jackson.annotation.JsonCreator 
	public AnalysisGrid(
			@com.fasterxml.jackson.annotation.JsonProperty("URI")  final String URI,
			@com.fasterxml.jackson.annotation.JsonProperty("projectID")  final int projectID,
			@com.fasterxml.jackson.annotation.JsonProperty("title")  final String title,
			@com.fasterxml.jackson.annotation.JsonProperty("sku")  final String sku,
			@com.fasterxml.jackson.annotation.JsonProperty("xyz")  final String xyz,
			@com.fasterxml.jackson.annotation.JsonProperty("abc")  final String abc) {
			
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.projectID = projectID;
		this.title = title != null ? title : "";
		org.revenj.Guards.checkLength(title, 25);
		this.sku = sku != null ? sku : "";
		org.revenj.Guards.checkLength(sku, 10);
		this.xyz = xyz;
		org.revenj.Guards.checkLength(xyz, 1);
		this.abc = abc;
		org.revenj.Guards.checkLength(abc, 1);
	}

	
	
	public AnalysisGrid() {
			
		this.URI = java.util.UUID.randomUUID().toString();
		this.projectID = 0;
		this.title = "";
		this.sku = "";
		this.xyz = null;
		this.abc = null;
	}

	
	private final String URI;

	
	@com.fasterxml.jackson.annotation.JsonProperty("URI")
	public String getURI()  {
		
		return this.URI;
	}

	
	@Override
	public int hashCode() {
		return URI.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;
		final AnalysisGrid other = (AnalysisGrid) obj;

		return URI.equals(other.URI);
	}

	@Override
	public String toString() {
		return "AnalysisGrid(" + URI + ')';
	}
	private static final long serialVersionUID = -5336591672687065505L;
	
	private final int projectID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("projectID")
	public int getProjectID()  {
		
		return this.projectID;
	}

	
	private final String title;

	
	@com.fasterxml.jackson.annotation.JsonProperty("title")
	public String getTitle()  {
		
		return this.title;
	}

	
	private final String sku;

	
	@com.fasterxml.jackson.annotation.JsonProperty("sku")
	public String getSku()  {
		
		return this.sku;
	}

	
	private final String xyz;

	
	@com.fasterxml.jackson.annotation.JsonProperty("xyz")
	public String getXyz()  {
		
		return this.xyz;
	}

	
	private final String abc;

	
	@com.fasterxml.jackson.annotation.JsonProperty("abc")
	public String getAbc()  {
		
		return this.abc;
	}

	

public static class filterSearch   implements java.io.Serializable, com.dslplatform.json.JsonObject, org.revenj.patterns.Specification<AnalysisGrid> {
	
	
	
	public filterSearch(
			 final int projectID,
			 final String filter,
			 final String abc,
			 final String xyz,
			 final String clazz) {
			
		setProjectID(projectID);
		setFilter(filter);
		setAbc(abc);
		setXyz(xyz);
		setClazz(clazz);
	}

	
	
	public filterSearch() {
			
		this.projectID = 0;
	}

	private static final long serialVersionUID = -7925476850171536883L;
	
	private int projectID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("projectID")
	public int getProjectID()  {
		
		return projectID;
	}

	
	public filterSearch setProjectID(final int value) {
		
		this.projectID = value;
		
		return this;
	}

	
	private String filter;

	
	@com.fasterxml.jackson.annotation.JsonProperty("filter")
	public String getFilter()  {
		
		return filter;
	}

	
	public filterSearch setFilter(final String value) {
		
		this.filter = value;
		
		return this;
	}

	
	private String abc;

	
	@com.fasterxml.jackson.annotation.JsonProperty("abc")
	public String getAbc()  {
		
		return abc;
	}

	
	public filterSearch setAbc(final String value) {
		
		if (value != null) org.revenj.Guards.checkLength(value, 1);
		this.abc = value;
		
		return this;
	}

	
	private String xyz;

	
	@com.fasterxml.jackson.annotation.JsonProperty("xyz")
	public String getXyz()  {
		
		return xyz;
	}

	
	public filterSearch setXyz(final String value) {
		
		if (value != null) org.revenj.Guards.checkLength(value, 1);
		this.xyz = value;
		
		return this;
	}

	
	private String clazz;

	
	@com.fasterxml.jackson.annotation.JsonProperty("clazz")
	public String getClazz()  {
		
		return clazz;
	}

	
	public filterSearch setClazz(final String value) {
		
		if (value != null) org.revenj.Guards.checkLength(value, 50);
		this.clazz = value;
		
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

	static void __serializeJsonObjectMinimal(final filterSearch self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			if (self.projectID != 0) {
			hasWrittenProperty = true;
				sw.writeAscii("\"projectID\":", 12);
				com.dslplatform.json.NumberConverter.serialize(self.projectID, sw);
			}
		
			if (self.filter != null) {
			if(hasWrittenProperty) sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
			hasWrittenProperty = true;
				sw.writeAscii("\"filter\":", 9);
				sw.writeString(self.filter);
			}
		
			if (self.abc != null) {
			if(hasWrittenProperty) sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
			hasWrittenProperty = true;
				sw.writeAscii("\"abc\":", 6);
				com.dslplatform.json.StringConverter.serializeShort(self.abc, sw);
			}
		
			if (self.xyz != null) {
			if(hasWrittenProperty) sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
			hasWrittenProperty = true;
				sw.writeAscii("\"xyz\":", 6);
				com.dslplatform.json.StringConverter.serializeShort(self.xyz, sw);
			}
		
			if (self.clazz != null) {
			if(hasWrittenProperty) sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
			hasWrittenProperty = true;
				sw.writeAscii("\"clazz\":", 8);
				com.dslplatform.json.StringConverter.serializeShort(self.clazz, sw);
			}
	}

	static void __serializeJsonObjectFull(final filterSearch self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			
			sw.writeAscii("\"projectID\":", 12);
			com.dslplatform.json.NumberConverter.serialize(self.projectID, sw);
		
			
			if (self.filter != null) {
				sw.writeAscii(",\"filter\":", 10);
				sw.writeString(self.filter);
			} else {
				sw.writeAscii(",\"filter\":null", 14);
			}
		
			
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

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<filterSearch> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<filterSearch>() {
		@Override
		public filterSearch deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.stock.AnalysisGrid.filterSearch(reader);
		}
	};

	private filterSearch(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		int _projectID_ = 0;
		String _filter_ = null;
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
					
					case 504496707:
						_projectID_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case -941528969:
						_filter_ = com.dslplatform.json.StringConverter.deserialize(reader);
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
					
					case 504496707:
						_projectID_ = com.dslplatform.json.NumberConverter.deserializeInt(reader);
					nextToken = reader.getNextToken();
						break;
					case -941528969:
						_filter_ = com.dslplatform.json.StringConverter.deserialize(reader);
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
		
		this.projectID = _projectID_;
		this.filter = _filter_;
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
				return new gen.model.stock.AnalysisGrid.filterSearch(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
		public boolean test(gen.model.stock.AnalysisGrid it) {
			return ( ( ( ( (it.getProjectID() == this.getProjectID()) &&  ( ( ( this.getFilter() == null ||  this.getFilter().equals("")) || it.getTitle().toLowerCase().contains(this.getFilter().toLowerCase())) || it.getSku().toLowerCase().contains(this.getFilter().toLowerCase()))) &&  ( this.getAbc() == null ||  it.getAbc().equals(this.getAbc()))) &&  ( this.getXyz() == null ||  it.getXyz().equals(this.getXyz()))) &&  ( ( this.getClazz() == null ||  this.getClazz().equals("")) || it.getSku().toLowerCase().contains(this.getClazz().toLowerCase())));
		}
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

	static void __serializeJsonObjectMinimal(final AnalysisGrid self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
		com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			if (self.projectID != 0) {
				sw.writeAscii(",\"projectID\":", 13);
				com.dslplatform.json.NumberConverter.serialize(self.projectID, sw);
			}
		
			if (!(self.title.length() == 0)) {
				sw.writeAscii(",\"title\":", 9);
				com.dslplatform.json.StringConverter.serializeShort(self.title, sw);
			}
		
			if (!(self.sku.length() == 0)) {
				sw.writeAscii(",\"sku\":", 7);
				com.dslplatform.json.StringConverter.serializeShort(self.sku, sw);
			}
		
			if (self.xyz != null) {
				sw.writeAscii(",\"xyz\":", 7);
				com.dslplatform.json.StringConverter.serializeShort(self.xyz, sw);
			}
		
			if (self.abc != null) {
				sw.writeAscii(",\"abc\":", 7);
				com.dslplatform.json.StringConverter.serializeShort(self.abc, sw);
			}
	}

	static void __serializeJsonObjectFull(final AnalysisGrid self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		sw.writeAscii("\"URI\":");
		com.dslplatform.json.StringConverter.serializeShort(self.URI, sw);
		
			
			sw.writeAscii(",\"projectID\":", 13);
			com.dslplatform.json.NumberConverter.serialize(self.projectID, sw);
		
			
			sw.writeAscii(",\"title\":", 9);
			com.dslplatform.json.StringConverter.serializeShort(self.title, sw);
		
			
			sw.writeAscii(",\"sku\":", 7);
			com.dslplatform.json.StringConverter.serializeShort(self.sku, sw);
		
			
			if (self.xyz != null) {
				sw.writeAscii(",\"xyz\":", 7);
				com.dslplatform.json.StringConverter.serializeShort(self.xyz, sw);
			} else {
				sw.writeAscii(",\"xyz\":null", 11);
			}
		
			
			if (self.abc != null) {
				sw.writeAscii(",\"abc\":", 7);
				com.dslplatform.json.StringConverter.serializeShort(self.abc, sw);
			} else {
				sw.writeAscii(",\"abc\":null", 11);
			}
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<AnalysisGrid> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<AnalysisGrid>() {
		@Override
		public AnalysisGrid deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.stock.AnalysisGrid(reader);
		}
	};

	private AnalysisGrid(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
		int _projectID_ = 0;
		String _title_ = "";
		String _sku_ = "";
		String _xyz_ = null;
		String _abc_ = null;
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
					case -1738164983:
						_title_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -651451878:
						_sku_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -996022048:
						_xyz_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case 440920331:
						_abc_ = com.dslplatform.json.StringConverter.deserialize(reader);
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
					case -1738164983:
						_title_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -651451878:
						_sku_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -996022048:
						_xyz_ = com.dslplatform.json.StringConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case 440920331:
						_abc_ = com.dslplatform.json.StringConverter.deserialize(reader);
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
		this.title = _title_;
		this.sku = _sku_;
		this.xyz = _xyz_;
		this.abc = _abc_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.stock.AnalysisGrid(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
}
