/*
* Created by DSL Platform
* v1.5.5871.15913 
*/

package gen.model.xc.repositories;



public class SearchByTimestampAndOrderByTimestampRepository   implements java.io.Closeable, org.revenj.patterns.Repository<gen.model.xc.SearchByTimestampAndOrderByTimestamp>, org.revenj.postgres.BulkRepository<gen.model.xc.SearchByTimestampAndOrderByTimestamp>, org.revenj.patterns.PersistableRepository<gen.model.xc.SearchByTimestampAndOrderByTimestamp> {
	
	
	
	public SearchByTimestampAndOrderByTimestampRepository(
			 final java.util.Optional<java.sql.Connection> transactionContext,
			 final javax.sql.DataSource dataSource,
			 final org.revenj.postgres.QueryProvider queryProvider,
			 final gen.model.xc.converters.SearchByTimestampAndOrderByTimestampConverter converter,
			 final org.revenj.patterns.ServiceLocator locator) {
			
		this.transactionContext = transactionContext;
		this.dataSource = dataSource;
		this.queryProvider = queryProvider;
		this.transactionConnection = transactionContext.orElse(null);
		this.converter = converter;
		this.locator = locator;
	}

	private final java.util.Optional<java.sql.Connection> transactionContext;
	private final javax.sql.DataSource dataSource;
	private final org.revenj.postgres.QueryProvider queryProvider;
	private final java.sql.Connection transactionConnection;
	private final gen.model.xc.converters.SearchByTimestampAndOrderByTimestampConverter converter;
	private final org.revenj.patterns.ServiceLocator locator;
	
	private java.sql.Connection getConnection() {
		if (transactionConnection != null) return transactionConnection;
		try {
			return dataSource.getConnection();
		} catch (java.sql.SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private void releaseConnection(java.sql.Connection connection) {
		if (this.transactionConnection != null) return;
		try {
			connection.close();
		} catch (java.sql.SQLException ignore) {
		}		
	}

	public SearchByTimestampAndOrderByTimestampRepository(org.revenj.patterns.ServiceLocator locator) {
		this(locator.tryResolve(java.sql.Connection.class), locator.resolve(javax.sql.DataSource.class), locator.resolve(org.revenj.postgres.QueryProvider.class), locator.resolve(gen.model.xc.converters.SearchByTimestampAndOrderByTimestampConverter.class), locator);
	}
	

	public static org.revenj.patterns.Specification<gen.model.xc.SearchByTimestampAndOrderByTimestamp> rewriteSpecificationToLambda(org.revenj.patterns.Specification<gen.model.xc.SearchByTimestampAndOrderByTimestamp> filter) {
		
		return filter;
	}

	private static final boolean hasCustomSecurity = false;

	@Override
	public org.revenj.patterns.Query<gen.model.xc.SearchByTimestampAndOrderByTimestamp> query(org.revenj.patterns.Specification<gen.model.xc.SearchByTimestampAndOrderByTimestamp> filter) {
		org.revenj.patterns.Query<gen.model.xc.SearchByTimestampAndOrderByTimestamp> query = queryProvider.query(transactionConnection, locator, gen.model.xc.SearchByTimestampAndOrderByTimestamp.class);
		if (filter != null) {
			query = query.filter(rewriteSpecificationToLambda(filter));
		}
		
		return query;
	}

	private java.util.List<gen.model.xc.SearchByTimestampAndOrderByTimestamp> readFromDb(java.sql.PreparedStatement statement, java.util.List<gen.model.xc.SearchByTimestampAndOrderByTimestamp> result) throws java.sql.SQLException, java.io.IOException {
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
	public java.util.List<gen.model.xc.SearchByTimestampAndOrderByTimestamp> search(org.revenj.patterns.Specification<gen.model.xc.SearchByTimestampAndOrderByTimestamp> specification, Integer limit, Integer offset) {
		final String selectType = "SELECT it";
		java.util.function.Consumer<java.sql.PreparedStatement> applyFilters = ps -> {};
		java.sql.Connection connection = getConnection();
		try (org.revenj.postgres.PostgresWriter pgWriter = org.revenj.postgres.PostgresWriter.create()) {
			String sql;
			if (specification == null) {
				sql = "SELECT r FROM \"xc\".\"SearchByTimestampAndOrderByTimestamp_entity\" r";
			} 
			else {
				org.revenj.patterns.Query<gen.model.xc.SearchByTimestampAndOrderByTimestamp> query = query(specification);
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
		} finally {
			releaseConnection(connection);
		}
	}

	public java.util.function.BiFunction<java.sql.ResultSet, Integer, java.util.List<gen.model.xc.SearchByTimestampAndOrderByTimestamp>> search(org.revenj.postgres.BulkReaderQuery query, org.revenj.patterns.Specification<gen.model.xc.SearchByTimestampAndOrderByTimestamp> specification, Integer limit, Integer offset) {
		String selectType = "SELECT array_agg(_r) FROM (SELECT _it as _r";
		final org.revenj.postgres.PostgresReader rdr = query.getReader();
		final org.revenj.postgres.PostgresWriter pgWriter = query.getWriter();
		int index = query.getArgumentIndex();
		StringBuilder sb = query.getBuilder();
		if (specification == null) {
			sb.append("SELECT array_agg(_r) FROM (SELECT _r FROM \"xc\".\"SearchByTimestampAndOrderByTimestamp_entity\" _r");
		}
		
		else {
			sb.append("SELECT 0");
			return (rs, ind) -> search(specification, limit, offset);
		}
		if (limit != null && limit >= 0) {
			sb.append(" LIMIT ");
			sb.append(Integer.toString(limit));
		}
		if (offset != null && offset >= 0) {
			sb.append(" OFFSET ");
			sb.append(Integer.toString(offset));
		}
		sb.append(") _sq");
		return (rs, ind) -> {
			try {
				String res = rs.getString(ind);
				if (res == null || res.length() == 0 || res.length() == 2) {
					return new java.util.ArrayList<>(0);
				}
				rdr.process(res);
				java.util.List<gen.model.xc.SearchByTimestampAndOrderByTimestamp> result = org.revenj.postgres.converters.ArrayTuple.parse(rdr, 0, converter::from); 
				
				return result;
			} catch (java.sql.SQLException | java.io.IOException e) {
				throw new RuntimeException(e);
			}
		};
	}

	@Override
	public long count(org.revenj.patterns.Specification<gen.model.xc.SearchByTimestampAndOrderByTimestamp> specification) {
		final String selectType = "SELECT COUNT(*)";
		java.util.function.Consumer<java.sql.PreparedStatement> applyFilters = ps -> {};
		java.sql.Connection connection = getConnection();
		try (org.revenj.postgres.PostgresWriter pgWriter = org.revenj.postgres.PostgresWriter.create()) {
			String sql;
			if (specification == null) {
				sql = "SELECT COUNT(*) FROM \"xc\".\"SearchByTimestampAndOrderByTimestamp_entity\" r";
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
		} finally { 
			releaseConnection(connection); 
		}
	}

	public java.util.function.BiFunction<java.sql.ResultSet, Integer, Long> count(org.revenj.postgres.BulkReaderQuery query, org.revenj.patterns.Specification<gen.model.xc.SearchByTimestampAndOrderByTimestamp> specification) {
		String selectType = "SELECT count(*)";
		final org.revenj.postgres.PostgresReader rdr = query.getReader();
		final org.revenj.postgres.PostgresWriter pgWriter = query.getWriter();
		int index = query.getArgumentIndex();
		StringBuilder sb = query.getBuilder();
		if (specification == null) {
			sb.append("SELECT count(*) FROM \"xc\".\"SearchByTimestampAndOrderByTimestamp_entity\" r");
		}
		
		else {
			sb.append("SELECT 0");
			return (rs, ind) -> {
				try {
					return query(specification).count();
				} catch (java.io.IOException e) {
					throw new RuntimeException(e);
				}
			};
		}
		return (rs, ind) -> {
			try {
				return rs.getLong(ind);
			} catch (java.sql.SQLException e) {
				throw new RuntimeException(e);
			}
		};
	}

	@Override
	public boolean exists(org.revenj.patterns.Specification<gen.model.xc.SearchByTimestampAndOrderByTimestamp> specification) {
		final String selectType = "SELECT exists(SELECT *";
		java.util.function.Consumer<java.sql.PreparedStatement> applyFilters = ps -> {};
		java.sql.Connection connection = getConnection();
		try (org.revenj.postgres.PostgresWriter pgWriter = org.revenj.postgres.PostgresWriter.create()) {
			String sql = null;
			if (specification == null) {
				sql = "SELECT exists(SELECT * FROM \"xc\".\"SearchByTimestampAndOrderByTimestamp_entity\" r";
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
		} finally { 
			releaseConnection(connection); 
		}
	}

	public java.util.function.BiFunction<java.sql.ResultSet, Integer, Boolean> exists(org.revenj.postgres.BulkReaderQuery query, org.revenj.patterns.Specification<gen.model.xc.SearchByTimestampAndOrderByTimestamp> specification) {
		String selectType = "exists(SELECT *";
		final org.revenj.postgres.PostgresReader rdr = query.getReader();
		final org.revenj.postgres.PostgresWriter pgWriter = query.getWriter();
		int index = query.getArgumentIndex();
		StringBuilder sb = query.getBuilder();
		if (specification == null) {
			sb.append("exists(SELECT * FROM \"xc\".\"SearchByTimestampAndOrderByTimestamp_entity\" r");
		}
		
		else {
			sb.append("SELECT 0");
			return (rs, ind) -> {
				try {
					return query(specification).any();
				} catch (java.io.IOException e) {
					throw new RuntimeException(e);
				}
			};
		}
		return (rs, ind) -> {
			try {
				return rs.getBoolean(ind);
			} catch (java.sql.SQLException e) {
				throw new RuntimeException(e);
			}
		};
	}

	@Override
	public void close() throws java.io.IOException { 
	}

	
	@Override
	public java.util.List<gen.model.xc.SearchByTimestampAndOrderByTimestamp> find(String[] uris) {
		final int[] ids = new int[uris.length];
		for (int i = 0; i < uris.length; i++) {
			try {
				ids[i] = Integer.parseInt(uris[i]);
			} catch (java.lang.Exception e) {
				throw new java.lang.IllegalArgumentException("Invalid URI value found: " + uris[i], e);
			}
		}
		java.sql.Connection connection = getConnection();
		try {
			return find(ids, connection);
		} finally {
			releaseConnection(connection);
		}
	}

	@Override
	public java.util.Optional<gen.model.xc.SearchByTimestampAndOrderByTimestamp> find(String uri) {
		final int id;
		try {
			id = Integer.parseInt(uri);
		} catch (java.lang.Exception e) {
			throw new java.lang.IllegalArgumentException("Invalid URI value found: " + uri, e);
		}
		java.sql.Connection connection = getConnection();
		try {
			return find(id, connection);
		} finally {
			releaseConnection(connection);
		}
	}

	public java.util.List<gen.model.xc.SearchByTimestampAndOrderByTimestamp> find(int[] ids, java.sql.Connection connection) {
		try (java.sql.PreparedStatement statement = connection.prepareStatement("SELECT _r FROM \"xc\".\"SearchByTimestampAndOrderByTimestamp_entity\" _r WHERE _r.\"ID\" = ANY(?)");
			org.revenj.postgres.PostgresReader reader = org.revenj.postgres.PostgresReader.create(locator)) {
			org.postgresql.util.PGobject arr = new org.postgresql.util.PGobject();
			arr.setType("int4[]");
			arr.setValue(org.revenj.postgres.converters.ArrayTuple.create(ids, org.revenj.postgres.converters.IntConverter::toTuple).buildTuple(false));
			statement.setObject(1, arr);
			java.util.List<gen.model.xc.SearchByTimestampAndOrderByTimestamp> result = new java.util.ArrayList<>(ids.length);
			try (java.sql.ResultSet rs = statement.executeQuery()) {
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

	public java.util.Optional<gen.model.xc.SearchByTimestampAndOrderByTimestamp> find(int id, java.sql.Connection connection) {
		try (java.sql.PreparedStatement statement = connection.prepareStatement("SELECT _r FROM \"xc\".\"SearchByTimestampAndOrderByTimestamp_entity\" _r WHERE _r.\"ID\" = ?");
			org.revenj.postgres.PostgresReader reader = org.revenj.postgres.PostgresReader.create(locator)) {
			statement.setInt(1, id);
			gen.model.xc.SearchByTimestampAndOrderByTimestamp instance;
			try (java.sql.ResultSet rs = statement.executeQuery()) {
				if (rs.next()) {
					reader.process(rs.getString(1));
					instance = converter.from(reader);
				} else {
					return java.util.Optional.empty();
				}
			}
			if (!hasCustomSecurity) return java.util.Optional.of(instance);
			java.util.List<gen.model.xc.SearchByTimestampAndOrderByTimestamp> result = new java.util.ArrayList<>(1);
			result.add(instance);
			
			if (result.size() == 1) {
				java.util.Optional.of(instance);
			}
			return java.util.Optional.empty();
		} catch (java.sql.SQLException | java.io.IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public java.util.function.BiFunction<java.sql.ResultSet, Integer, java.util.Optional<gen.model.xc.SearchByTimestampAndOrderByTimestamp>> find(org.revenj.postgres.BulkReaderQuery query, String uri) {
		final org.revenj.postgres.PostgresReader rdr = query.getReader();
		StringBuilder sb = query.getBuilder();
		int index = query.getArgumentIndex();
		if (uri == null) {
			sb.append("SELECT 0");
			return (rs, ind) -> java.util.Optional.empty();
		}
		final int id;
		try {
			id = Integer.parseInt(uri);
		} catch (java.lang.Exception e) {
			throw new java.lang.IllegalArgumentException("Invalid URI value found: " + uri, e);
		}
		sb.append("SELECT _r FROM \"xc\".\"SearchByTimestampAndOrderByTimestamp_entity\" _r WHERE _r.\"ID\" = ?");
		query.addArgument(ps -> {
			try {
				ps.setInt(index, id);
			} catch (java.sql.SQLException e) {
				throw new RuntimeException(e);
			}
		});
		return (rs, ind) -> {
			try {
				String res = rs.getString(ind);
				if (res == null) {
					return java.util.Optional.empty();
				}
				rdr.process(res);
				gen.model.xc.SearchByTimestampAndOrderByTimestamp instance = converter.from(rdr);
				if (!hasCustomSecurity) return java.util.Optional.of(instance);
				java.util.List<gen.model.xc.SearchByTimestampAndOrderByTimestamp> result = new java.util.ArrayList<>(1);
				result.add(instance);
				
				if (result.size() == 1) {
					java.util.Optional.of(instance);
				}
			} catch (java.sql.SQLException | java.io.IOException e) {
				throw new RuntimeException(e);
			}
			return java.util.Optional.empty();
		};
	}

	@Override
	public java.util.function.BiFunction<java.sql.ResultSet, Integer, java.util.List<gen.model.xc.SearchByTimestampAndOrderByTimestamp>> find(org.revenj.postgres.BulkReaderQuery query, String[] uris) {
		final org.revenj.postgres.PostgresReader rdr = query.getReader();
		final org.revenj.postgres.PostgresWriter writer = query.getWriter();
		StringBuilder sb = query.getBuilder();
		int index = query.getArgumentIndex();
		if (uris == null || uris.length == 0) {
			sb.append("SELECT 0");
			return (rs, ind) -> new java.util.ArrayList<>(0);
		}
		sb.append("SELECT array_agg(_r) FROM \"xc\".\"SearchByTimestampAndOrderByTimestamp_entity\" _r WHERE _r.\"ID\" = ANY(?)");
		final int[] ids = new int[uris.length];
		for (int i = 0; i < uris.length; i++) {
			try {
				ids[i] = Integer.parseInt(uris[i]);
			} catch (java.lang.Exception e) {
				throw new java.lang.IllegalArgumentException("Invalid URI value found: " + uris[i], e);
			}
		}
		query.addArgument(ps -> {
			try {
				org.postgresql.util.PGobject arr = new org.postgresql.util.PGobject();
				arr.setType("int4[]");
				writer.reset();
				org.revenj.postgres.converters.PostgresTuple tuple = org.revenj.postgres.converters.ArrayTuple.create(ids, org.revenj.postgres.converters.IntConverter::toTuple);
				tuple.buildTuple(writer, false);
				arr.setValue(writer.toString());
				ps.setObject(index, arr);
			} catch (java.sql.SQLException e) {
				throw new RuntimeException(e);
			}
		});
		return (rs, ind) -> {
			try {
				String res = rs.getString(ind);
				if (res == null || res.length() == 0 || res.length() == 2) {
					return new java.util.ArrayList<>(0);
				}
				rdr.process(res);
				java.util.List<gen.model.xc.SearchByTimestampAndOrderByTimestamp> result = org.revenj.postgres.converters.ArrayTuple.parse(rdr, 0, converter::from); 
				
				return result;
			} catch (java.sql.SQLException | java.io.IOException e) {
				throw new RuntimeException(e);
			}
		};
	}

	
	public static void __setupPersist(
			java.util.function.BiConsumer<java.util.Collection<gen.model.xc.SearchByTimestampAndOrderByTimestamp>, java.util.Map.Entry<org.revenj.postgres.PostgresWriter, org.revenj.patterns.ServiceLocator>> insert, 
			java.util.function.BiConsumer<java.util.Map.Entry<java.util.List<gen.model.xc.SearchByTimestampAndOrderByTimestamp>, java.util.List<gen.model.xc.SearchByTimestampAndOrderByTimestamp>>, java.util.Map.Entry<org.revenj.postgres.PostgresWriter, org.revenj.patterns.ServiceLocator>> update,
			java.util.function.Consumer<java.util.Collection<gen.model.xc.SearchByTimestampAndOrderByTimestamp>> delete,
			java.util.function.Function<gen.model.xc.SearchByTimestampAndOrderByTimestamp, gen.model.xc.SearchByTimestampAndOrderByTimestamp> track) {
		insertLoop = insert;
		updateLoop = update;
		deleteLoop = delete;
		trackChanges = track;
	}

	private static java.util.function.BiConsumer<java.util.Collection<gen.model.xc.SearchByTimestampAndOrderByTimestamp>, java.util.Map.Entry<org.revenj.postgres.PostgresWriter, org.revenj.patterns.ServiceLocator>> insertLoop;
	private static java.util.function.BiConsumer<java.util.Map.Entry<java.util.List<gen.model.xc.SearchByTimestampAndOrderByTimestamp>, java.util.List<gen.model.xc.SearchByTimestampAndOrderByTimestamp>>, java.util.Map.Entry<org.revenj.postgres.PostgresWriter, org.revenj.patterns.ServiceLocator>> updateLoop;
	private static java.util.function.Consumer<java.util.Collection<gen.model.xc.SearchByTimestampAndOrderByTimestamp>> deleteLoop;
	private static java.util.function.Function<gen.model.xc.SearchByTimestampAndOrderByTimestamp, gen.model.xc.SearchByTimestampAndOrderByTimestamp> trackChanges;

	private static final String[] EMPTY_URI = new String[0];

	@Override
	public String[] persist(
			java.util.Collection<gen.model.xc.SearchByTimestampAndOrderByTimestamp> insert,
			java.util.Collection<java.util.Map.Entry<gen.model.xc.SearchByTimestampAndOrderByTimestamp, gen.model.xc.SearchByTimestampAndOrderByTimestamp>> update,
			java.util.Collection<gen.model.xc.SearchByTimestampAndOrderByTimestamp> delete) throws java.io.IOException {
		java.sql.Connection connection = getConnection();
		try (java.sql.PreparedStatement statement = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT \"xc\".\"persist_SearchByTimestampAndOrderByTimestamp\"(?, ?, ?, ?)");
			org.revenj.postgres.PostgresWriter sw = org.revenj.postgres.PostgresWriter.create()) {
			String[] result;
			if (insert != null && !insert.isEmpty()) {
				assignSequenceID.accept(insert, connection);
				insertLoop.accept(insert, new java.util.AbstractMap.SimpleEntry<>(sw, locator));
				sw.reset();
				org.revenj.postgres.converters.PostgresTuple tuple = org.revenj.postgres.converters.ArrayTuple.create(insert, converter::to);
				org.postgresql.util.PGobject pgo = new org.postgresql.util.PGobject();
				pgo.setType("\"xc\".\"SearchByTimestampAndOrderByTimestamp_entity\"[]");
				sw.reset();
				tuple.buildTuple(sw, false);
				pgo.setValue(sw.toString());
				statement.setObject(1, pgo);
				result = new String[insert.size()];
				int i = 0;
				for (gen.model.xc.SearchByTimestampAndOrderByTimestamp it : insert) {
					result[i++] = it.getURI();
					trackChanges.apply(it);
				}
			} else {
				statement.setArray(1, null);
				result = EMPTY_URI;
			}
			if (update != null && !update.isEmpty()) {
				java.util.List<gen.model.xc.SearchByTimestampAndOrderByTimestamp> oldUpdate = new java.util.ArrayList<>(update.size());
				java.util.List<gen.model.xc.SearchByTimestampAndOrderByTimestamp> newUpdate = new java.util.ArrayList<>(update.size());
				java.util.Map<String, Integer> missing = new java.util.HashMap<>();
				int cnt = 0;
				for (java.util.Map.Entry<gen.model.xc.SearchByTimestampAndOrderByTimestamp, gen.model.xc.SearchByTimestampAndOrderByTimestamp> it : update) {
					gen.model.xc.SearchByTimestampAndOrderByTimestamp oldValue = trackChanges.apply(it.getValue());
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
					java.util.List<gen.model.xc.SearchByTimestampAndOrderByTimestamp> found = find(missing.keySet().toArray(new String[missing.size()]));
					for (gen.model.xc.SearchByTimestampAndOrderByTimestamp it : found) {
						oldUpdate.set(missing.get(it.getURI()), it);
					}
				}
				updateLoop.accept(new java.util.AbstractMap.SimpleEntry<>(oldUpdate, newUpdate), new java.util.AbstractMap.SimpleEntry<>(sw, locator));
				org.revenj.postgres.converters.PostgresTuple tupleOld = org.revenj.postgres.converters.ArrayTuple.create(oldUpdate, converter::to);
				org.revenj.postgres.converters.PostgresTuple tupleNew = org.revenj.postgres.converters.ArrayTuple.create(newUpdate, converter::to);
				org.postgresql.util.PGobject pgOld = new org.postgresql.util.PGobject();
				org.postgresql.util.PGobject pgNew = new org.postgresql.util.PGobject();
				pgOld.setType("\"xc\".\"SearchByTimestampAndOrderByTimestamp_entity\"[]");
				pgNew.setType("\"xc\".\"SearchByTimestampAndOrderByTimestamp_entity\"[]");
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
				pgo.setType("\"xc\".\"SearchByTimestampAndOrderByTimestamp_entity\"[]");
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
		} finally { 
			releaseConnection(connection); 
		}
	}

	
	@Override
	public String insert(gen.model.xc.SearchByTimestampAndOrderByTimestamp item) throws java.io.IOException {
		java.sql.Connection connection = getConnection();
		try (java.sql.PreparedStatement statement = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT \"xc\".\"insert_SearchByTimestampAndOrderByTimestamp\"(ARRAY[?])");
			org.revenj.postgres.PostgresWriter sw = org.revenj.postgres.PostgresWriter.create()) {
			java.util.List<gen.model.xc.SearchByTimestampAndOrderByTimestamp> insert = java.util.Collections.singletonList(item);
				assignSequenceID.accept(insert, connection);
			if (insertLoop != null) insertLoop.accept(insert, new java.util.AbstractMap.SimpleEntry<>(sw, locator));
			sw.reset();
			org.revenj.postgres.converters.PostgresTuple tuple = converter.to(item);
			org.postgresql.util.PGobject pgo = new org.postgresql.util.PGobject();
			pgo.setType("\"xc\".\"SearchByTimestampAndOrderByTimestamp_entity\"");
			sw.reset();
			tuple.buildTuple(sw, false);
			pgo.setValue(sw.toString());
			statement.setObject(1, pgo);
			statement.execute();
			trackChanges.apply(item);
			return item.getURI();
		} catch (java.sql.SQLException e) {
			throw new java.io.IOException(e);
		} finally { 
			releaseConnection(connection); 
		}
	}

	@Override
	public void update(gen.model.xc.SearchByTimestampAndOrderByTimestamp oldItem, gen.model.xc.SearchByTimestampAndOrderByTimestamp newItem) throws java.io.IOException {
		java.sql.Connection connection = getConnection();
		try (java.sql.PreparedStatement statement = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT \"xc\".\"update_SearchByTimestampAndOrderByTimestamp\"(ARRAY[?], ARRAY[?])");
			 org.revenj.postgres.PostgresWriter sw = org.revenj.postgres.PostgresWriter.create()) {
			if (oldItem == null) oldItem = trackChanges.apply(newItem);
			else trackChanges.apply(newItem);
			if (oldItem == null) oldItem = find(newItem.getURI()).get();
			java.util.List<gen.model.xc.SearchByTimestampAndOrderByTimestamp> oldUpdate = java.util.Collections.singletonList(oldItem);
			java.util.List<gen.model.xc.SearchByTimestampAndOrderByTimestamp> newUpdate = java.util.Collections.singletonList(newItem);
			if (updateLoop != null) updateLoop.accept(new java.util.AbstractMap.SimpleEntry<>(oldUpdate, newUpdate), new java.util.AbstractMap.SimpleEntry<>(sw, locator));
			org.revenj.postgres.converters.PostgresTuple tupleOld = converter.to(oldItem);
			org.revenj.postgres.converters.PostgresTuple tupleNew = converter.to(newItem);
			org.postgresql.util.PGobject pgOld = new org.postgresql.util.PGobject();
			org.postgresql.util.PGobject pgNew = new org.postgresql.util.PGobject();
			pgOld.setType("\"xc\".\"SearchByTimestampAndOrderByTimestamp_entity\"");
			pgNew.setType("\"xc\".\"SearchByTimestampAndOrderByTimestamp_entity\"");
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
		} finally { 
			releaseConnection(connection); 
		}
	}

	
	public static void __setupSequenceID(java.util.function.BiConsumer<java.util.Collection<gen.model.xc.SearchByTimestampAndOrderByTimestamp>, java.sql.Connection> sequence) {
		assignSequenceID = sequence;
	}

	private static java.util.function.BiConsumer<java.util.Collection<gen.model.xc.SearchByTimestampAndOrderByTimestamp>, java.sql.Connection> assignSequenceID;
}
