/*
* Created by DSL Platform
* v1.0.0.32432 
*/

package gen.model.test.repositories;



public class ClickedRepository   implements java.io.Closeable, org.revenj.patterns.DomainEventStore<gen.model.test.Clicked> {
	
	
	
	public ClickedRepository(
			 final java.util.Optional<java.sql.Connection> transactionContext,
			 final javax.sql.DataSource dataSource,
			 final org.revenj.postgres.QueryProvider queryProvider,
			 final org.revenj.postgres.ObjectConverter<gen.model.test.Clicked> converter,
			 final org.revenj.patterns.ServiceLocator locator,
			 final org.revenj.patterns.DomainEventHandler<gen.model.test.Clicked>[] singleHandlers,
			 final org.revenj.patterns.DomainEventHandler<gen.model.test.Clicked[]>[] collectionHandlers,
			 final org.revenj.patterns.DomainEventHandler<java.util.concurrent.Callable<gen.model.test.Clicked>>[] lazyHandlers,
			 final org.revenj.patterns.DomainEventHandler<java.util.concurrent.Callable<gen.model.test.Clicked[]>>[] collectionLazyHandlers) {
			
		this.transactionContext = transactionContext;
		this.dataSource = dataSource;
		this.queryProvider = queryProvider;
		this.transactionConnection = transactionContext.orElse(null);
		this.converter = converter;
		this.locator = locator;
		this.singleHandlers = singleHandlers;
		this.collectionHandlers = collectionHandlers;
		this.lazyHandlers = lazyHandlers;
		this.collectionLazyHandlers = collectionLazyHandlers;
	}

	private final java.util.Optional<java.sql.Connection> transactionContext;
	private final javax.sql.DataSource dataSource;
	private final org.revenj.postgres.QueryProvider queryProvider;
	private final java.sql.Connection transactionConnection;
	private final org.revenj.postgres.ObjectConverter<gen.model.test.Clicked> converter;
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

	public ClickedRepository(org.revenj.patterns.ServiceLocator locator) {
		this(locator.tryResolve(java.sql.Connection.class), locator.resolve(javax.sql.DataSource.class), locator.resolve(org.revenj.postgres.QueryProvider.class), locator.resolve(gen.model.test.converters.ClickedConverter.class), locator);
	}
	

	public static org.revenj.patterns.Specification<gen.model.test.Clicked> rewriteSpecificationToLambda(org.revenj.patterns.Specification<gen.model.test.Clicked> filter) {
		
		if (filter instanceof gen.model.test.Clicked.BetweenNumbers) {
			gen.model.test.Clicked.BetweenNumbers _spec_ = (gen.model.test.Clicked.BetweenNumbers)filter;
			java.math.BigDecimal _spec_min_ = _spec_.getMin();
			java.util.Set<java.math.BigDecimal> _spec_inSet_ = _spec_.getInSet();
			gen.model.test.En _spec_en_ = _spec_.getEn();
			return it -> ( ( it.getNumber().compareTo(_spec_min_) >= 0 && (_spec_inSet_.contains(it.getNumber()))) &&  it.getEn().equals(_spec_en_));
		}
		return filter;
	}

	@Override
	public org.revenj.patterns.Query<gen.model.test.Clicked> query(org.revenj.patterns.Specification<gen.model.test.Clicked> filter) {
		org.revenj.patterns.Query<gen.model.test.Clicked> query = queryProvider.query(transactionConnection, locator, gen.model.test.Clicked.class);
		if (filter != null) {
			query = query.filter(rewriteSpecificationToLambda(filter));
		}
		
		return query;
	}

	private java.util.List<gen.model.test.Clicked> readFromDb(java.sql.PreparedStatement statement, java.util.List<gen.model.test.Clicked> result) throws java.sql.SQLException, java.io.IOException {
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
	public java.util.List<gen.model.test.Clicked> search(org.revenj.patterns.Specification<gen.model.test.Clicked> specification, Integer limit, Integer offset) {
		final String selectType = "SELECT it";
		java.util.function.Consumer<java.sql.PreparedStatement> applyFilters = ps -> {};
		java.sql.Connection connection = getConnection();
		try (org.revenj.postgres.PostgresWriter pgWriter = org.revenj.postgres.PostgresWriter.create()) {
			String sql;
			if (specification == null) {
				sql = "SELECT r FROM \"test\".\"Clicked_event\" r";
			} 
			else if (specification instanceof gen.model.test.Clicked.BetweenNumbers) {
				gen.model.test.Clicked.BetweenNumbers spec = (gen.model.test.Clicked.BetweenNumbers)specification;
				sql = selectType + " FROM \"test\".\"Clicked.BetweenNumbers\"(?, ?, ?) it";
				
				applyFilters = applyFilters.andThen(ps -> {
					try {
						ps.setBigDecimal(1, spec.getMin());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				applyFilters = applyFilters.andThen(ps -> {
					try {
						
					Object[] __arr = new Object[spec.getInSet().size()];
					int __ind = 0;
					for (Object __it : spec.getInSet()) __arr[__ind++] = __it;
					ps.setArray(2, connection.createArrayOf("numeric", __arr));
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				applyFilters = applyFilters.andThen(ps -> {
					try {
						if (spec.getEn() == null) ps.setNull(3, java.sql.Types.OTHER); 
				else {
					org.postgresql.util.PGobject __pgo = new org.postgresql.util.PGobject();
					__pgo.setType("\"test\".\"En\"");
					__pgo.setValue(gen.model.test.converters.EnConverter.stringValue(spec.getEn()));
					ps.setObject(3, __pgo);
				}
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
			}
			else {
				org.revenj.patterns.Query<gen.model.test.Clicked> query = query(specification);
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

	@Override
	public long count(org.revenj.patterns.Specification<gen.model.test.Clicked> specification) {
		final String selectType = "SELECT COUNT(*)";
		java.util.function.Consumer<java.sql.PreparedStatement> applyFilters = ps -> {};
		java.sql.Connection connection = getConnection();
		try (org.revenj.postgres.PostgresWriter pgWriter = org.revenj.postgres.PostgresWriter.create()) {
			String sql;
			if (specification == null) {
				sql = "SELECT COUNT(*) FROM \"test\".\"Clicked_event\" r";
			} 
			else if (specification instanceof gen.model.test.Clicked.BetweenNumbers) {
				gen.model.test.Clicked.BetweenNumbers spec = (gen.model.test.Clicked.BetweenNumbers)specification;
				sql = selectType + " FROM \"test\".\"Clicked.BetweenNumbers\"(?, ?, ?) it";
				
				applyFilters = applyFilters.andThen(ps -> {
					try {
						ps.setBigDecimal(1, spec.getMin());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				applyFilters = applyFilters.andThen(ps -> {
					try {
						
					Object[] __arr = new Object[spec.getInSet().size()];
					int __ind = 0;
					for (Object __it : spec.getInSet()) __arr[__ind++] = __it;
					ps.setArray(2, connection.createArrayOf("numeric", __arr));
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				applyFilters = applyFilters.andThen(ps -> {
					try {
						if (spec.getEn() == null) ps.setNull(3, java.sql.Types.OTHER); 
				else {
					org.postgresql.util.PGobject __pgo = new org.postgresql.util.PGobject();
					__pgo.setType("\"test\".\"En\"");
					__pgo.setValue(gen.model.test.converters.EnConverter.stringValue(spec.getEn()));
					ps.setObject(3, __pgo);
				}
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
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

	@Override
	public boolean exists(org.revenj.patterns.Specification<gen.model.test.Clicked> specification) {
		final String selectType = "SELECT exists(SELECT *";
		java.util.function.Consumer<java.sql.PreparedStatement> applyFilters = ps -> {};
		java.sql.Connection connection = getConnection();
		try (org.revenj.postgres.PostgresWriter pgWriter = org.revenj.postgres.PostgresWriter.create()) {
			String sql = null;
			if (specification == null) {
				sql = "SELECT exists(SELECT * FROM \"test\".\"Clicked_event\" r";
			} 
			else if (specification instanceof gen.model.test.Clicked.BetweenNumbers) {
				gen.model.test.Clicked.BetweenNumbers spec = (gen.model.test.Clicked.BetweenNumbers)specification;
				sql = selectType + " FROM \"test\".\"Clicked.BetweenNumbers\"(?, ?, ?) it";
				
				applyFilters = applyFilters.andThen(ps -> {
					try {
						ps.setBigDecimal(1, spec.getMin());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				applyFilters = applyFilters.andThen(ps -> {
					try {
						
					Object[] __arr = new Object[spec.getInSet().size()];
					int __ind = 0;
					for (Object __it : spec.getInSet()) __arr[__ind++] = __it;
					ps.setArray(2, connection.createArrayOf("numeric", __arr));
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				applyFilters = applyFilters.andThen(ps -> {
					try {
						if (spec.getEn() == null) ps.setNull(3, java.sql.Types.OTHER); 
				else {
					org.postgresql.util.PGobject __pgo = new org.postgresql.util.PGobject();
					__pgo.setType("\"test\".\"En\"");
					__pgo.setValue(gen.model.test.converters.EnConverter.stringValue(spec.getEn()));
					ps.setObject(3, __pgo);
				}
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
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

	@Override
	public void close() throws java.io.IOException { 
	}

	private final org.revenj.patterns.DomainEventHandler<gen.model.test.Clicked>[] singleHandlers;
	private final org.revenj.patterns.DomainEventHandler<gen.model.test.Clicked[]>[] collectionHandlers;
	private final org.revenj.patterns.DomainEventHandler<java.util.concurrent.Callable<gen.model.test.Clicked>>[] lazyHandlers;
	private final org.revenj.patterns.DomainEventHandler<java.util.concurrent.Callable<gen.model.test.Clicked[]>>[] collectionLazyHandlers;
	
	public ClickedRepository(
			final java.util.Optional<java.sql.Connection> transactionContext,
			final javax.sql.DataSource dataSource,
			final org.revenj.postgres.QueryProvider queryProvider,
			final org.revenj.postgres.ObjectConverter<gen.model.test.Clicked> converter,
			final org.revenj.patterns.ServiceLocator locator) {
		this(transactionContext,
				dataSource,
				queryProvider,
				converter,
				locator,
				transactionContext.isPresent() ? new org.revenj.patterns.Generic<org.revenj.patterns.DomainEventHandler<gen.model.test.Clicked>[]>() {}.resolve(locator) : null,
				transactionContext.isPresent() ? new org.revenj.patterns.Generic<org.revenj.patterns.DomainEventHandler<gen.model.test.Clicked[]>[]>() {}.resolve(locator) : null,
				transactionContext.isPresent() ? new org.revenj.patterns.Generic<org.revenj.patterns.DomainEventHandler<java.util.concurrent.Callable<gen.model.test.Clicked>>[]>() {}.resolve(locator) : null,
				transactionContext.isPresent() ? new org.revenj.patterns.Generic<org.revenj.patterns.DomainEventHandler<java.util.concurrent.Callable<gen.model.test.Clicked[]>>[]>() {}.resolve(locator) : null
		);
	}

	@Override
	public java.util.List<gen.model.test.Clicked> find(String[] uris) {
		java.sql.Connection connection = getConnection();
		try (java.sql.PreparedStatement statement = connection.prepareStatement("SELECT r FROM \"test\".\"Clicked_event\" r WHERE r._event_id = ANY(?)")) {
			Object[] ids = new Object[uris.length];
			for(int i = 0; i < uris.length; i++) {
				ids[i] = Long.parseLong(uris[i]);
			}
			statement.setArray(1, connection.createArrayOf("int8", ids));
			return readFromDb(statement, new java.util.ArrayList<>(uris.length));			
		} catch (java.sql.SQLException | java.io.IOException e) {
			throw new RuntimeException(e);
		} finally {
			releaseConnection(connection);
		}
	}

	private org.revenj.extensibility.Container executeBefore(java.sql.Connection connection, gen.model.test.Clicked[] events) {
		final org.revenj.extensibility.Container context;
		final org.revenj.patterns.DomainEventHandler<gen.model.test.Clicked>[] sh;
		final org.revenj.patterns.DomainEventHandler<gen.model.test.Clicked[]>[] ch;
		if (transactionContext.isPresent()) {
			context = null;
			sh = singleHandlers;
			ch = collectionHandlers;
		} else {
			context = locator.resolve(org.revenj.extensibility.Container.class);
			context.registerInstance(java.sql.Connection.class, connection, false);
			sh = new org.revenj.patterns.Generic<org.revenj.patterns.DomainEventHandler<gen.model.test.Clicked>[]>(){}.resolve(context);
			ch = new org.revenj.patterns.Generic<org.revenj.patterns.DomainEventHandler<gen.model.test.Clicked[]>[]>(){}.resolve(context);
		}
		if (sh != null) {
			for (org.revenj.patterns.DomainEventHandler<gen.model.test.Clicked> s : sh) {
				for (gen.model.test.Clicked c : events) {
					s.handle(c);
				}
			}
		}
		if (ch != null) {
			for (org.revenj.patterns.DomainEventHandler<gen.model.test.Clicked[]> s : ch) {
				s.handle(events);
			}
		}
		return context;
	}

	private void executeAfter(org.revenj.patterns.ServiceLocator context, gen.model.test.Clicked[] events) {
		final org.revenj.patterns.DomainEventHandler<java.util.concurrent.Callable<gen.model.test.Clicked>>[] sh;
		final org.revenj.patterns.DomainEventHandler<java.util.concurrent.Callable<gen.model.test.Clicked[]>>[] ch;
		if (context == null) {
			sh = lazyHandlers;
			ch = collectionLazyHandlers;
		} else {
			sh = new org.revenj.patterns.Generic<org.revenj.patterns.DomainEventHandler<java.util.concurrent.Callable<gen.model.test.Clicked>>[]>(){}.resolve(context);
			ch = new org.revenj.patterns.Generic<org.revenj.patterns.DomainEventHandler<java.util.concurrent.Callable<gen.model.test.Clicked[]>>[]>(){}.resolve(context);
		}
		if (sh != null) {
			for (org.revenj.patterns.DomainEventHandler<java.util.concurrent.Callable<gen.model.test.Clicked>> s : sh) {
				for (gen.model.test.Clicked c : events) {
					s.handle(() -> c);
				}
			}
		}
		if (ch != null) {
			for (org.revenj.patterns.DomainEventHandler<java.util.concurrent.Callable<gen.model.test.Clicked[]>> s : ch) {
				s.handle(() -> events);
			}
		}
	}

	@Override
	public String[] submit(java.util.Collection<gen.model.test.Clicked> domainEvents) {
		java.sql.Connection connection = getConnection();
		gen.model.test.Clicked[] events = domainEvents.toArray(new gen.model.test.Clicked[domainEvents.size()]);
		org.revenj.extensibility.Container context = executeBefore(connection, events);
		try (java.sql.PreparedStatement statement = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT \"URI\" FROM \"test\".\"submit_Clicked\"(?)");
			org.revenj.postgres.PostgresWriter sw = org.revenj.postgres.PostgresWriter.create()) {
			if (prepareEvents != null) prepareEvents.accept(domainEvents);
			String[] result = new String[events.length];
			org.revenj.postgres.converters.PostgresTuple tuple = org.revenj.postgres.converters.ArrayTuple.create(events, converter::to);
			org.postgresql.util.PGobject pgo = new org.postgresql.util.PGobject();
			pgo.setType("\"test\".\"Clicked_event\"[]");
			tuple.buildTuple(sw, false);
			pgo.setValue(sw.toString());
			statement.setObject(1, pgo);
			try (java.sql.ResultSet rs = statement.executeQuery()) {
				for (int i = 0; i < result.length; i++) {
					rs.next();
					result[i] = rs.getString(1);
				}
			}
			if (assignUris != null) assignUris.accept(domainEvents, result);
			executeAfter(context, events);
			return result;
		} catch (java.sql.SQLException e) {
			throw new RuntimeException(e);
		} finally {
			if (context != null) {
				try { context.close(); }
				catch (Exception e) { e.printStackTrace(); }
			}
			releaseConnection(connection);
		}
	}

	public static void __configure(
			java.util.function.Consumer<java.util.Collection<gen.model.test.Clicked>> prepare,
			java.util.function.BiConsumer<java.util.Collection<gen.model.test.Clicked>, String[]> assign) {
		prepareEvents = prepare;
		assignUris = assign;
	}

	private static java.util.function.Consumer<java.util.Collection<gen.model.test.Clicked>> prepareEvents;
	private static java.util.function.BiConsumer<java.util.Collection<gen.model.test.Clicked>, String[]> assignUris;

	@Override
	public void mark(String[] uris) {
		java.sql.Connection connection = getConnection();
		try (java.sql.PreparedStatement statement = connection.prepareStatement("/*NO LOAD BALANCE*/SELECT \"test\".\"mark_Clicked\"(?)")) {
			Object[] ids = new Object[uris.length];
			for(int i = 0; i < uris.length; i++) {
				ids[i] = Long.parseLong(uris[i]);
			}
			statement.setArray(1, connection.createArrayOf("int8", ids));
			statement.executeUpdate();
		} catch (java.sql.SQLException e) {
			throw new RuntimeException(e);
		} finally {
			releaseConnection(connection);
		}
	}

}
