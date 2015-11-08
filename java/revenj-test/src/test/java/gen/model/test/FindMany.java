package gen.model.test;



public final class FindMany   implements java.io.Serializable, org.revenj.patterns.Report<FindMany.Result>, com.dslplatform.json.JsonObject {
	
	

public static class Result   implements com.dslplatform.json.JsonObject {
	
	
	@com.fasterxml.jackson.annotation.JsonCreator 
	private Result(
			@com.fasterxml.jackson.annotation.JsonProperty("composites")  final java.util.List<gen.model.test.CompositeList> composites,
			@com.fasterxml.jackson.annotation.JsonProperty("found")  final gen.model.test.Composite found) {
			
		this.composites = composites == null ? new java.util.ArrayList<gen.model.test.CompositeList>(4) : composites;
		this.found = found == null ? new gen.model.test.Composite() : found;
	}

	
	private final java.util.List<gen.model.test.CompositeList> composites;

	
	@com.fasterxml.jackson.annotation.JsonProperty("composites")
	public java.util.List<gen.model.test.CompositeList> getComposites()  {
		
		return this.composites;
	}

	
	private final gen.model.test.Composite found;

	
	@com.fasterxml.jackson.annotation.JsonProperty("found")
	public gen.model.test.Composite getFound()  {
		
		return this.found;
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

	static void __serializeJsonObjectMinimal(final Result self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
		if(self.composites.size() != 0) {
			if(hasWrittenProperty) sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
			hasWrittenProperty = true;
			sw.writeAscii("\"composites\":[", 14);
			gen.model.test.CompositeList item = self.composites.get(0);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.test.CompositeList.__serializeJsonObjectMinimal(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			for(int i = 1; i < self.composites.size(); i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
				item = self.composites.get(i);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.test.CompositeList.__serializeJsonObjectMinimal(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		
		if(self.found != null) {
			if(hasWrittenProperty) sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
			hasWrittenProperty = true;
			sw.writeAscii("\"found\":{", 9);
			
					gen.model.test.Composite.__serializeJsonObjectMinimal(self.found, sw, false);
					sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
		}
	}

	static void __serializeJsonObjectFull(final Result self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
		if(self.composites.size() != 0) {
			sw.writeAscii(",\"composites\":[", 15);
			gen.model.test.CompositeList item = self.composites.get(0);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.test.CompositeList.__serializeJsonObjectFull(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			for(int i = 1; i < self.composites.size(); i++) {
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
				item = self.composites.get(i);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_START);
				gen.model.test.CompositeList.__serializeJsonObjectFull(item, sw, false);
				sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
			}
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else sw.writeAscii(",\"composites\":[]", 16);
		
		
		if(self.found != null) {
			sw.writeAscii(",\"found\":{", 10);
			
					gen.model.test.Composite.__serializeJsonObjectFull(self.found, sw, false);
					sw.writeByte(com.dslplatform.json.JsonWriter.OBJECT_END);
		} else {
			sw.writeAscii(",\"found\":null", 13);
		}
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<Result> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<Result>() {
		@Override
		public Result deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.test.FindMany.Result(reader);
		}
	};

	private Result(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		java.util.List<gen.model.test.CompositeList> _composites_ = new java.util.ArrayList<gen.model.test.CompositeList>(4);
		gen.model.test.Composite _found_ = null;
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
					
					case 1131107909:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							reader.deserializeCollection(gen.model.test.CompositeList.JSON_READER, _composites_);
						}
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case 1694464927:
						
					if (nextToken == '{') {
						reader.getNextToken();
						_found_ = gen.model.test.Composite.JSON_READER.deserialize(reader);
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
					
					case 1131107909:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							reader.deserializeCollection(gen.model.test.CompositeList.JSON_READER, _composites_);
						}
						nextToken = reader.getNextToken();
					} else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
						break;
					case 1694464927:
						
					if (nextToken == '{') {
						reader.getNextToken();
						_found_ = gen.model.test.Composite.JSON_READER.deserialize(reader);
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
		
		this.composites = _composites_;
		this.found = _found_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.test.FindMany.Result(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
}

	
	@com.fasterxml.jackson.annotation.JsonCreator 
	public FindMany(
			@com.fasterxml.jackson.annotation.JsonProperty("id")  final java.util.UUID id,
			@com.fasterxml.jackson.annotation.JsonProperty("ids")  final java.util.Set<java.util.UUID> ids) {
			
		if(id != null) setId(id); else this.id = java.util.UUID.randomUUID();
		if(ids != null) setIds(ids); else this.ids = new java.util.LinkedHashSet<java.util.UUID>(4);
	}

	
	
	public FindMany() {
			
		this.id = java.util.UUID.randomUUID();
		this.ids = new java.util.LinkedHashSet<java.util.UUID>(4);
	}

	private static final long serialVersionUID = 8933720349785970089L;
	
	private java.util.UUID id;

	
	@com.fasterxml.jackson.annotation.JsonProperty("id")
	public java.util.UUID getId()  {
		
		return id;
	}

	
	public FindMany setId(final java.util.UUID value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"id\" cannot be null!");
		this.id = value;
		
		return this;
	}

	
	private java.util.Set<java.util.UUID> ids;

	
	@com.fasterxml.jackson.annotation.JsonProperty("ids")
	public java.util.Set<java.util.UUID> getIds()  {
		
		return ids;
	}

	
	public FindMany setIds(final java.util.Set<java.util.UUID> value) {
		
		if(value == null) throw new IllegalArgumentException("Property \"ids\" cannot be null!");
		org.revenj.Guards.checkNulls(value);
		this.ids = value;
		
		return this;
	}

	

	private static java.sql.Connection getConnection(org.revenj.patterns.ServiceLocator locator) {
		try {
			return locator.resolve(javax.sql.DataSource.class).getConnection();
		} catch (java.sql.SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private static void releaseConnection(java.sql.Connection connection) {
		try {
			connection.close();
		} catch (java.sql.SQLException ignore) {
		}		
	}

	public Result populate(org.revenj.patterns.ServiceLocator locator) {
		java.util.Optional<java.sql.Connection> tryConnection = locator.tryResolve(java.sql.Connection.class);
		if (tryConnection.isPresent()) return populate(tryConnection.get(), locator);
		java.sql.Connection connection = getConnection(locator);
		try {
			return populate(connection, locator);
		} finally {
			releaseConnection(connection);
		}
	}

	public Result populate(java.sql.Connection connection, org.revenj.patterns.ServiceLocator locator) {
		try (java.sql.PreparedStatement ps = connection.prepareStatement("SELECT \"test\".\"FindMany\"(?, ?)");
			org.revenj.postgres.PostgresReader reader = org.revenj.postgres.PostgresReader.create(locator)) {
			int index = 1;
			
			ps.setObject(index, this.id);
			index++;
			
			{
				Object[] __arr = new Object[this.ids.size()];
				int __ind = 0;
				for (Object __it : this.ids) __arr[__ind++] = __it;
				ps.setArray(index, connection.createArrayOf("uuid", __arr));
			}
			index++;
			
			java.sql.ResultSet rs = ps.executeQuery();
			rs.next();
			reader.process(rs.getString(1));
			rs.close();
			reader.read(2);
			
			java.util.List<gen.model.test.CompositeList> _list_composites = org.revenj.postgres.converters.ArrayTuple.parse(reader, 1, locator.resolve(gen.model.test.converters.CompositeListConverter.class)::from); 
			java.util.List<gen.model.test.CompositeList> _composites_ = null;
			if (_list_composites != null) _composites_ = _list_composites;;
			gen.model.test.Composite _found_ = locator.resolve(gen.model.test.converters.CompositeConverter.class).from(reader, 1);
			return new Result(_composites_, _found_);
		} catch (Exception e) {
			throw new RuntimeException(e);
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

	static void __serializeJsonObjectMinimal(final FindMany self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			if (!(self.id.getMostSignificantBits() == 0 && self.id.getLeastSignificantBits() == 0)) {
			hasWrittenProperty = true;
				sw.writeAscii("\"id\":", 5);
				com.dslplatform.json.UUIDConverter.serialize(self.id, sw);
			}
		
		if(self.ids.size() != 0) {
			if(hasWrittenProperty) sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);
			hasWrittenProperty = true;
			sw.writeAscii("\"ids\":[", 7);
			java.util.UUID item;
			java.util.Iterator<java.util.UUID> iterator = self.ids.iterator();
			int total = self.ids.size() - 1;
			for(int i = 0; i < total; i++) {
				item = iterator.next();com.dslplatform.json.UUIDConverter.serialize(item, sw);
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
			}
			item = iterator.next();com.dslplatform.json.UUIDConverter.serialize(item, sw);
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
	}

	static void __serializeJsonObjectFull(final FindMany self, com.dslplatform.json.JsonWriter sw, boolean hasWrittenProperty) {
		
		
			
			sw.writeAscii("\"id\":", 5);
			com.dslplatform.json.UUIDConverter.serialize(self.id, sw);
		
		if(self.ids.size() != 0) {
			sw.writeAscii(",\"ids\":[", 8);
			java.util.UUID item;
			java.util.Iterator<java.util.UUID> iterator = self.ids.iterator();
			int total = self.ids.size() - 1;
			for(int i = 0; i < total; i++) {
				item = iterator.next();com.dslplatform.json.UUIDConverter.serialize(item, sw);
				sw.writeByte(com.dslplatform.json.JsonWriter.COMMA);	
			}
			item = iterator.next();com.dslplatform.json.UUIDConverter.serialize(item, sw);
			sw.writeByte(com.dslplatform.json.JsonWriter.ARRAY_END);
		}
		else sw.writeAscii(",\"ids\":[]", 9);
	}

	public static final com.dslplatform.json.JsonReader.ReadJsonObject<FindMany> JSON_READER = new com.dslplatform.json.JsonReader.ReadJsonObject<FindMany>() {
		@Override
		public FindMany deserialize(final com.dslplatform.json.JsonReader reader) throws java.io.IOException {
			return new gen.model.test.FindMany(reader);
		}
	};

	private FindMany(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		
		java.util.UUID _id_ = org.revenj.Utils.MIN_UUID;
		java.util.Set<java.util.UUID> _ids_ = new java.util.LinkedHashSet<java.util.UUID>(4);
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
					
					case 926444256:
						_id_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -2133867159:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							com.dslplatform.json.UUIDConverter.deserializeCollection(reader, _ids_);
						}
						nextToken = reader.getNextToken();
					}
					else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
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
					
					case 926444256:
						_id_ = com.dslplatform.json.UUIDConverter.deserialize(reader);
					nextToken = reader.getNextToken();
						break;
					case -2133867159:
						
					if (nextToken == '[') {
						nextToken = reader.getNextToken();
						if (nextToken != ']') {
							com.dslplatform.json.UUIDConverter.deserializeCollection(reader, _ids_);
						}
						nextToken = reader.getNextToken();
					}
					else throw new java.io.IOException("Expecting '[' at position " + reader.positionInStream() + ". Found " + (char)nextToken);
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
		
		this.id = _id_;
		this.ids = _ids_;
	}

	public static Object deserialize(final com.dslplatform.json.JsonReader<org.revenj.patterns.ServiceLocator> reader) throws java.io.IOException {
		switch (reader.getNextToken()) {
			case 'n':
				if (reader.wasNull())
					return null;
				throw new java.io.IOException("Invalid null value found at: " + reader.positionInStream());
			case '{':
				reader.getNextToken();
				return new gen.model.test.FindMany(reader);
			case '[':
				return reader.deserializeNullableCollection(JSON_READER);
			default:
				throw new java.io.IOException("Invalid char value found at: " + reader.positionInStream() + ". Expecting null, { or [. Found: " + (char)reader.last());
		}
	}
}
