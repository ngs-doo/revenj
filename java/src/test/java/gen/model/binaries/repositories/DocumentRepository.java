package gen.model.binaries.repositories;



public class DocumentRepository   implements java.io.Closeable, org.revenj.patterns.Repository<gen.model.binaries.Document>, org.revenj.patterns.PersistableRepository<gen.model.binaries.Document> {
	
	
	
	public DocumentRepository(
			 final java.sql.Connection connection,
			 final org.revenj.postgres.QueryProvider queryProvider,
			 final org.revenj.postgres.ObjectConverter<gen.model.binaries.Document> converter,
			 final org.revenj.patterns.ServiceLocator locator) {
			
		this.connection = connection;
		this.queryProvider = queryProvider;
		this.converter = converter;
		this.locator = locator;
	}

	private final java.sql.Connection connection;
	private final org.revenj.postgres.QueryProvider queryProvider;
	private final org.revenj.postgres.ObjectConverter<gen.model.binaries.Document> converter;
	private final org.revenj.patterns.ServiceLocator locator;
	
	public DocumentRepository(org.revenj.patterns.ServiceLocator locator) {
		this(locator.resolve(java.sql.Connection.class), locator.resolve(org.revenj.postgres.QueryProvider.class), locator.resolve(gen.model.binaries.converters.DocumentConverter.class), locator);
	}
	
	@Override
	public org.revenj.patterns.Query<gen.model.binaries.Document> query(org.revenj.patterns.Specification<gen.model.binaries.Document> filter) {
		org.revenj.patterns.Query<gen.model.binaries.Document> query = queryProvider.query(connection, locator, gen.model.binaries.Document.class);
		if (filter == null) { }
		else query = query.filter(filter);
		
		return query;
	}

	private java.util.List<gen.model.binaries.Document> readFromDb(java.sql.PreparedStatement statement, java.util.List<gen.model.binaries.Document> result) throws java.sql.SQLException, java.io.IOException {
		try (java.sql.ResultSet rs = statement.executeQuery();
			org.revenj.postgres.PostgresReader reader = org.revenj.postgres.PostgresReader.create(locator)) {
			while (rs.next()) {
				reader.process(rs.getString(1));
				result.add(converter.from(reader));
			}
		}
		
		return result;
	}

	@Override
	public java.util.List<gen.model.binaries.Document> search(org.revenj.patterns.Specification<gen.model.binaries.Document> specification, Integer limit, Integer offset) {
		final String selectType = "SELECT it";
		java.util.function.Consumer<java.sql.PreparedStatement> applyFilters = ps -> {};
		try (org.revenj.postgres.PostgresWriter pgWriter = org.revenj.postgres.PostgresWriter.create()) {
			String sql;
			if (specification == null) {
				sql = "SELECT r FROM \"binaries\".\"Document_entity\" r";
			} 
			else {
				org.revenj.patterns.Query<gen.model.binaries.Document> query = query(specification);
				if (offset != null) {
					query = query.skip(offset);
				}
				if (limit != null) {
					query = query.limit(limit);
				}
				try {
					return query.list();
				} catch (java.io.IOException e) {
					throw new RuntimeException(e);
				}
			}
			if (limit != null) {
				sql += " LIMIT " + Integer.toString(limit);
			}
			if (offset != null) {
				sql += " OFFSET " + Integer.toString(offset);
			}
			try (java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {
				applyFilters.accept(statement);
				return readFromDb(statement, new java.util.ArrayList<>());
			} catch (java.sql.SQLException | java.io.IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public long count(org.revenj.patterns.Specification<gen.model.binaries.Document> specification) {
		final String selectType = "SELECT COUNT(*)";
		java.util.function.Consumer<java.sql.PreparedStatement> applyFilters = ps -> {};
		try (org.revenj.postgres.PostgresWriter pgWriter = org.revenj.postgres.PostgresWriter.create()) {
			String sql;
			if (specification == null) {
				sql = "SELECT COUNT(*) FROM \"binaries\".\"Document_entity\" r";
			} 
			else {
				try {
					return query(specification).count();
				} catch (java.io.IOException e) {
					throw new RuntimeException(e);
				}
			}
			try (java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {
				applyFilters.accept(statement);
				try (java.sql.ResultSet rs = statement.executeQuery()) {
					rs.next();
					return rs.getLong(1);
				}
			} catch (java.sql.SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public boolean exists(org.revenj.patterns.Specification<gen.model.binaries.Document> specification) {
		final String selectType = "SELECT exists(SELECT *";
		java.util.function.Consumer<java.sql.PreparedStatement> applyFilters = ps -> {};
		try (org.revenj.postgres.PostgresWriter pgWriter = org.revenj.postgres.PostgresWriter.create()) {
			String sql = null;
			if (specification == null) {
				sql = "SELECT exists(SELECT * FROM \"binaries\".\"Document_entity\" r";
			} 
			else {
				try {
					return query(specification).any();
				} catch (java.io.IOException e) {
					throw new RuntimeException(e);
				}
			}
			try (java.sql.PreparedStatement statement = connection.prepareStatement(sql + ")")) {
				applyFilters.accept(statement);
				try (java.sql.ResultSet rs = statement.executeQuery()) {
					rs.next();
					return rs.getBoolean(1);
				}
			} catch (java.sql.SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void close() throws java.io.IOException { 
	}

	
	@Override
	public java.util.List<gen.model.binaries.Document> find(String[] uris) {
		try (java.sql.Statement statement = connection.createStatement();
			org.revenj.postgres.PostgresReader reader = org.revenj.postgres.PostgresReader.create(locator)) {
			java.util.List<gen.model.binaries.Document> result = new java.util.ArrayList<>(uris.length);
			StringBuilder sb = new StringBuilder("SELECT r FROM \"binaries\".\"Document_entity\" r WHERE r.\"ID\" IN (");
			org.revenj.postgres.PostgresWriter.writeSimpleUriList(sb, uris);
			sb.append(")");
			try (java.sql.ResultSet rs = statement.executeQuery(sb.toString())) {
				while (rs.next()) {
					reader.process(rs.getString(1));
					result.add(converter.from(reader));
				}
			}
			
			return result;
		} catch (java.sql.SQLException | java.io.IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void __setupPersist(
			java.util.function.BiConsumer<java.util.Collection<gen.model.binaries.Document>, org.revenj.postgres.PostgresWriter> insert, 
			java.util.function.BiConsumer<java.util.List<gen.model.binaries.Document>, java.util.List<gen.model.binaries.Document>> update,
			java.util.function.Consumer<java.util.Collection<gen.model.binaries.Document>> delete,
			java.util.function.Function<gen.model.binaries.Document, gen.model.binaries.Document> track) {
		insertLoop = insert;
		updateLoop = update;
		deleteLoop = delete;
		trackChanges = track;
	}

	private static java.util.function.BiConsumer<java.util.Collection<gen.model.binaries.Document>, org.revenj.postgres.PostgresWriter> insertLoop;
	private static java.util.function.BiConsumer<java.util.List<gen.model.binaries.Document>, java.util.List<gen.model.binaries.Document>> updateLoop;
	private static java.util.function.Consumer<java.util.Collection<gen.model.binaries.Document>> deleteLoop;
	private static java.util.function.Function<gen.model.binaries.Document, gen.model.binaries.Document> trackChanges;

	private static final String[] EMPTY_URI = new String[0];

	@Override
	public String[] persist(
			java.util.Collection<gen.model.binaries.Document> insert,
			java.util.Collection<java.util.Map.Entry<gen.model.binaries.Document, gen.model.binaries.Document>> update,
			java.util.Collection<gen.model.binaries.Document> delete) throws java.io.IOException {
		try (java.sql.PreparedStatement statement = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT \"binaries\".\"persist_Document\"(?, ?, ?, ?)");
			org.revenj.postgres.PostgresWriter sw = org.revenj.postgres.PostgresWriter.create()) {
			String[] result;
			if (insert != null && !insert.isEmpty()) {
				insertLoop.accept(insert, sw);
				sw.reset();
				org.revenj.postgres.converters.PostgresTuple tuple = org.revenj.postgres.converters.ArrayTuple.create(insert, converter::to);
				org.postgresql.util.PGobject pgo = new org.postgresql.util.PGobject();
				pgo.setType("\"binaries\".\"Document_entity\"[]");
				sw.reset();
				tuple.buildTuple(sw, false);
				pgo.setValue(sw.toString());
				statement.setObject(1, pgo);
				result = new String[insert.size()];
				int i = 0;
				for (gen.model.binaries.Document it : insert) {
					result[i++] = it.getURI();
					trackChanges.apply(it);
				}
			} else {
				statement.setArray(1, null);
				result = EMPTY_URI;
			}
			if (update != null && !update.isEmpty()) {
				java.util.List<gen.model.binaries.Document> oldUpdate = new java.util.ArrayList<>(update.size());
				java.util.List<gen.model.binaries.Document> newUpdate = new java.util.ArrayList<>(update.size());
				java.util.Map<String, Integer> missing = new java.util.HashMap<>();
				int cnt = 0;
				for (java.util.Map.Entry<gen.model.binaries.Document, gen.model.binaries.Document> it : update) {
					gen.model.binaries.Document oldValue = trackChanges.apply(it.getValue());
					if (it.getKey() != null) {
						oldValue = it.getKey();
					}
					oldUpdate.add(oldValue);
					if (oldValue == null) {
						missing.put(it.getValue().getURI(), cnt);
					}
					newUpdate.add(it.getValue());
					cnt++;
				}
				if (!missing.isEmpty()) {
					java.util.List<gen.model.binaries.Document> found = find(missing.keySet().toArray(new String[missing.size()]));
					for (gen.model.binaries.Document it : found) {
						oldUpdate.set(missing.get(it.getURI()), it);
					}
				}
				updateLoop.accept(oldUpdate, newUpdate);
				org.revenj.postgres.converters.PostgresTuple tupleOld = org.revenj.postgres.converters.ArrayTuple.create(oldUpdate, converter::to);
				org.revenj.postgres.converters.PostgresTuple tupleNew = org.revenj.postgres.converters.ArrayTuple.create(newUpdate, converter::to);
				org.postgresql.util.PGobject pgOld = new org.postgresql.util.PGobject();
				org.postgresql.util.PGobject pgNew = new org.postgresql.util.PGobject();
				pgOld.setType("\"binaries\".\"Document_entity\"[]");
				pgNew.setType("\"binaries\".\"Document_entity\"[]");
				tupleOld.buildTuple(sw, false);
				pgOld.setValue(sw.toString());
				sw.reset();
				tupleNew.buildTuple(sw, false);
				pgNew.setValue(sw.toString());
				sw.reset();
				statement.setObject(2, pgOld);
				statement.setObject(3, pgNew);
			} else {
				statement.setArray(2, null);
				statement.setArray(3, null);
			}
			if (delete != null && !delete.isEmpty()) {
				deleteLoop.accept(delete);
				org.revenj.postgres.converters.PostgresTuple tuple = org.revenj.postgres.converters.ArrayTuple.create(delete, converter::to);
				org.postgresql.util.PGobject pgo = new org.postgresql.util.PGobject();
				pgo.setType("\"binaries\".\"Document_entity\"[]");
				tuple.buildTuple(sw, false);
				pgo.setValue(sw.toString());
				statement.setObject(4, pgo);
			} else {
				statement.setArray(4, null);
			}
			try (java.sql.ResultSet rs = statement.executeQuery()) {
				rs.next();
				String message = rs.getString(1);
				if (message != null) throw new java.io.IOException(message);
			}
			return result;
		} catch (java.sql.SQLException e) {
			throw new java.io.IOException(e);
		}
	}

	
	@Override
	public String insert(gen.model.binaries.Document item) throws java.io.IOException {
		try (java.sql.PreparedStatement statement = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT \"binaries\".\"insert_Document\"(ARRAY[?])");
			org.revenj.postgres.PostgresWriter sw = org.revenj.postgres.PostgresWriter.create()) {
			java.util.List<gen.model.binaries.Document> insert = java.util.Collections.singletonList(item);
			if (insertLoop != null) insertLoop.accept(insert, sw);
			sw.reset();
			org.revenj.postgres.converters.PostgresTuple tuple = converter.to(item);
			org.postgresql.util.PGobject pgo = new org.postgresql.util.PGobject();
			pgo.setType("\"binaries\".\"Document_entity\"");
			sw.reset();
			tuple.buildTuple(sw, false);
			pgo.setValue(sw.toString());
			statement.setObject(1, pgo);
			statement.execute();
			trackChanges.apply(item);
			return item.getURI();
		} catch (java.sql.SQLException e) {
			throw new java.io.IOException(e);
		}
	}

	@Override
	public void update(gen.model.binaries.Document oldItem, gen.model.binaries.Document newItem) throws java.io.IOException {
		try (java.sql.PreparedStatement statement = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT \"binaries\".\"update_Document\"(ARRAY[?], ARRAY[?])");
			 org.revenj.postgres.PostgresWriter sw = org.revenj.postgres.PostgresWriter.create()) {
			if (oldItem == null) oldItem = trackChanges.apply(newItem);
			else trackChanges.apply(newItem);
			if (oldItem == null) oldItem = find(newItem.getURI()).get();
			java.util.List<gen.model.binaries.Document> oldUpdate = java.util.Collections.singletonList(oldItem);
			java.util.List<gen.model.binaries.Document> newUpdate = java.util.Collections.singletonList(newItem);
			if (updateLoop != null) updateLoop.accept(oldUpdate, newUpdate);
			org.revenj.postgres.converters.PostgresTuple tupleOld = converter.to(oldItem);
			org.revenj.postgres.converters.PostgresTuple tupleNew = converter.to(newItem);
			org.postgresql.util.PGobject pgOld = new org.postgresql.util.PGobject();
			org.postgresql.util.PGobject pgNew = new org.postgresql.util.PGobject();
			pgOld.setType("\"binaries\".\"Document_entity\"");
			pgNew.setType("\"binaries\".\"Document_entity\"");
			tupleOld.buildTuple(sw, false);
			pgOld.setValue(sw.toString());
			sw.reset();
			tupleNew.buildTuple(sw, false);
			pgNew.setValue(sw.toString());
			statement.setObject(1, pgOld);
			statement.setObject(2, pgNew);
			try (java.sql.ResultSet rs = statement.executeQuery()) {
				rs.next();
				String message = rs.getString(1);
				if (message != null) throw new java.io.IOException(message);
			}
		} catch (java.sql.SQLException e) {
			throw new java.io.IOException(e);
		}
	}

}
