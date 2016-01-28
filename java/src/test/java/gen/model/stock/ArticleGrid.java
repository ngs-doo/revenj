/*
* Created by DSL Platform
* v1.5.5871.15913 
*/

package gen.model.stock;



public final class ArticleGrid   implements java.io.Serializable, com.dslplatform.json.JsonObject, org.revenj.patterns.Identifiable {
	
	
	@com.fasterxml.jackson.annotation.JsonCreator 
	public ArticleGrid(
			@com.fasterxml.jackson.annotation.JsonProperty("URI")  final String URI,
			@com.fasterxml.jackson.annotation.JsonProperty("ID")  final long ID,
			@com.fasterxml.jackson.annotation.JsonProperty("projectID")  final int projectID,
			@com.fasterxml.jackson.annotation.JsonProperty("sku")  final String sku,
			@com.fasterxml.jackson.annotation.JsonProperty("title")  final String title) {
			
		this.URI = URI != null ? URI : new java.util.UUID(0L, 0L).toString();
		this.ID = ID;
		this.projectID = projectID;
		this.sku = sku != null ? sku : "";
		org.revenj.Guards.checkLength(sku, 10);
		this.title = title != null ? title : "";
		org.revenj.Guards.checkLength(title, 25);
	}

	
	
	public ArticleGrid() {
			
		this.URI = java.util.UUID.randomUUID().toString();
		this.ID = 0L;
		this.projectID = 0;
		this.sku = "";
		this.title = "";
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
		final ArticleGrid other = (ArticleGrid) obj;

		return URI.equals(other.URI);
	}

	@Override
	public String toString() {
		return "ArticleGrid(" + URI + ')';
	}
	private static final long serialVersionUID = 4065092723417845769L;
	
	private final long ID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ID")
	public long getID()  {
		
		return this.ID;
	}

	
	private final int projectID;

	
	@com.fasterxml.jackson.annotation.JsonProperty("projectID")
	public int getProjectID()  {
		
		return this.projectID;
	}

	
	private final String sku;

	
	@com.fasterxml.jackson.annotation.JsonProperty("sku")
	public String getSku()  {
		
		return this.sku;
	}

	
	private final String title;

	
	@com.fasterxml.jackson.annotation.JsonProperty("title")
	public String getTitle()  {
		
		return this.title;
	}

	

public static class filterSearch   implements java.io.Serializable, com.dslplatform.json.JsonObject, org.revenj.patterns.Specification<ArticleGrid> {
	
	
	
	public filterSearch(
			 final int projectID,
			 final String filter) {
			
		setProjectID(projectID);
		setFilter(filter);
	}

	
	
	public filterSearch() {
			
		this.projectID = 0;
	}

	private static final long serialVersionUID = -1427010402086690219L;
	
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
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<filterSearch> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<filterSearch>() {
		@Override
		public filterSearch deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.stock.ArticleGrid.filterSearch(reader);
		}
	};

	private filterSearch(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		int _projectID_ = 0;
		String _filter_ = null;
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
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.stock.ArticleGrid.filterSearch(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
	
		public boolean test(gen.model.stock.ArticleGrid it) {
			return ( (it.getProjectID() == this.getProjectID()) &&  ( ( ( this.getFilter() == null ||  this.getFilter().equals("")) || it.getSku().toLowerCase().contains(this.getFilter().toLowerCase())) || it.getTitle().toLowerCase().contains(this.getFilter().toLowerCase())));
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

	static void __serializeJsonObjectMinimal(final ArticleGrid self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
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

	static void __serializeJsonObjectFull(final ArticleGrid self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
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

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<ArticleGrid> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<ArticleGrid>() {
		@Override
		public ArticleGrid deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.stock.ArticleGrid(reader);
		}
	};

	private ArticleGrid(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		String _URI_ = "";
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
				return new gen.model.stock.ArticleGrid(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
}
