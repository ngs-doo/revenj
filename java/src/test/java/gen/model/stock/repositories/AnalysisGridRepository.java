/*
* Created by DSL Platform
* v1.5.5871.15913 
*/

package gen.model.stock.repositories;



public class AnalysisGridRepository   implements java.io.Closeable, org.revenj.patterns.Repository<gen.model.stock.AnalysisGrid>, org.revenj.postgres.BulkRepository<gen.model.stock.AnalysisGrid> {
	
	
	
	public AnalysisGridRepository(
			 final java.util.Optional<java.sql.Connection> transactionContext,
			 final javax.sql.DataSource dataSource,
			 final org.revenj.postgres.QueryProvider queryProvider,
			 final gen.model.stock.converters.AnalysisGridConverter converter,
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
	private final gen.model.stock.converters.AnalysisGridConverter converter;
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

	public AnalysisGridRepository(org.revenj.patterns.ServiceLocator locator) {
		this(locator.tryResolve(java.sql.Connection.class), locator.resolve(javax.sql.DataSource.class), locator.resolve(org.revenj.postgres.QueryProvider.class), locator.resolve(gen.model.stock.converters.AnalysisGridConverter.class), locator);
	}
	

	public static org.revenj.patterns.Specification<gen.model.stock.AnalysisGrid> rewriteSpecificationToLambda(org.revenj.patterns.Specification<gen.model.stock.AnalysisGrid> filter) {
		
		if (filter instanceof gen.model.stock.AnalysisGrid.filterSearch) {
			gen.model.stock.AnalysisGrid.filterSearch _spec_ = (gen.model.stock.AnalysisGrid.filterSearch)filter;
			int _spec_projectID_ = _spec_.getProjectID();
			String _spec_filter_ = _spec_.getFilter();
			String _spec_abc_ = _spec_.getAbc();
			String _spec_xyz_ = _spec_.getXyz();
			String _spec_clazz_ = _spec_.getClazz();
			return it -> ( ( ( ( (it.getProjectID() == _spec_projectID_) &&  ( ( ( _spec_filter_ == null ||  _spec_filter_.equals("")) || it.getTitle().toLowerCase().contains(_spec_filter_.toLowerCase())) || it.getSku().toLowerCase().contains(_spec_filter_.toLowerCase()))) &&  ( _spec_abc_ == null ||  it.getAbc().equals(_spec_abc_))) &&  ( _spec_xyz_ == null ||  it.getXyz().equals(_spec_xyz_))) &&  ( ( _spec_clazz_ == null ||  _spec_clazz_.equals("")) || it.getSku().toLowerCase().contains(_spec_clazz_.toLowerCase())));
		}
		return filter;
	}

	private static final boolean hasCustomSecurity = false;

	@Override
	public org.revenj.patterns.Query<gen.model.stock.AnalysisGrid> query(org.revenj.patterns.Specification<gen.model.stock.AnalysisGrid> filter) {
		org.revenj.patterns.Query<gen.model.stock.AnalysisGrid> query = queryProvider.query(transactionConnection, locator, gen.model.stock.AnalysisGrid.class);
		if (filter != null) {
			query = query.filter(rewriteSpecificationToLambda(filter));
		}
		
		return query;
	}

	private java.util.List<gen.model.stock.AnalysisGrid> readFromDb(java.sql.PreparedStatement statement, java.util.List<gen.model.stock.AnalysisGrid> result) throws java.sql.SQLException, java.io.IOException {
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
	public java.util.List<gen.model.stock.AnalysisGrid> search(org.revenj.patterns.Specification<gen.model.stock.AnalysisGrid> specification, Integer limit, Integer offset) {
		final String selectType = "SELECT it";
		java.util.function.Consumer<java.sql.PreparedStatement> applyFilters = ps -> {};
		java.sql.Connection connection = getConnection();
		try (org.revenj.postgres.PostgresWriter pgWriter = org.revenj.postgres.PostgresWriter.create()) {
			String sql;
			if (specification == null) {
				sql = "SELECT r FROM \"stock\".\"AnalysisGrid\" r";
			} 
			else if (specification instanceof gen.model.stock.AnalysisGrid.filterSearch) {
				gen.model.stock.AnalysisGrid.filterSearch spec = (gen.model.stock.AnalysisGrid.filterSearch)specification;
				sql = selectType + " FROM \"stock\".\"AnalysisGrid.filterSearch\"(?, ?, ?, ?, ?) it";
				
				applyFilters = applyFilters.andThen(ps -> {
					try {
						ps.setInt(1, spec.getProjectID());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				applyFilters = applyFilters.andThen(ps -> {
					try {
						ps.setString(2, spec.getFilter());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				applyFilters = applyFilters.andThen(ps -> {
					try {
						ps.setString(3, spec.getAbc());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				applyFilters = applyFilters.andThen(ps -> {
					try {
						ps.setString(4, spec.getXyz());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				applyFilters = applyFilters.andThen(ps -> {
					try {
						ps.setString(5, spec.getClazz());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
			}
			else {
				org.revenj.patterns.Query<gen.model.stock.AnalysisGrid> query = query(specification);
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

	public java.util.function.BiFunction<java.sql.ResultSet, Integer, java.util.List<gen.model.stock.AnalysisGrid>> search(org.revenj.postgres.BulkReaderQuery query, org.revenj.patterns.Specification<gen.model.stock.AnalysisGrid> specification, Integer limit, Integer offset) {
		String selectType = "SELECT array_agg(_r) FROM (SELECT _it as _r";
		final org.revenj.postgres.PostgresReader rdr = query.getReader();
		final org.revenj.postgres.PostgresWriter pgWriter = query.getWriter();
		int index = query.getArgumentIndex();
		StringBuilder sb = query.getBuilder();
		if (specification == null) {
			sb.append("SELECT array_agg(_r) FROM (SELECT _r FROM \"stock\".\"AnalysisGrid\" _r");
		}
		
			else if (specification instanceof gen.model.stock.AnalysisGrid.filterSearch) {
				gen.model.stock.AnalysisGrid.filterSearch spec = (gen.model.stock.AnalysisGrid.filterSearch)specification;
				sb.append(selectType);
				sb.append(" FROM \"stock\".\"AnalysisGrid.filterSearch\"(?, ?, ?, ?, ?) it");
				
				query.addArgument(ps -> {
					try {
						ps.setInt(index + 1, spec.getProjectID());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				query.addArgument(ps -> {
					try {
						ps.setString(index + 2, spec.getFilter());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				query.addArgument(ps -> {
					try {
						ps.setString(index + 3, spec.getAbc());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				query.addArgument(ps -> {
					try {
						ps.setString(index + 4, spec.getXyz());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				query.addArgument(ps -> {
					try {
						ps.setString(index + 5, spec.getClazz());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
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
				java.util.List<gen.model.stock.AnalysisGrid> result = org.revenj.postgres.converters.ArrayTuple.parse(rdr, 0, converter::from); 
				
				return result;
			} catch (java.sql.SQLException | java.io.IOException e) {
				throw new RuntimeException(e);
			}
		};
	}

	@Override
	public long count(org.revenj.patterns.Specification<gen.model.stock.AnalysisGrid> specification) {
		final String selectType = "SELECT COUNT(*)";
		java.util.function.Consumer<java.sql.PreparedStatement> applyFilters = ps -> {};
		java.sql.Connection connection = getConnection();
		try (org.revenj.postgres.PostgresWriter pgWriter = org.revenj.postgres.PostgresWriter.create()) {
			String sql;
			if (specification == null) {
				sql = "SELECT COUNT(*) FROM \"stock\".\"AnalysisGrid\" r";
			} 
			else if (specification instanceof gen.model.stock.AnalysisGrid.filterSearch) {
				gen.model.stock.AnalysisGrid.filterSearch spec = (gen.model.stock.AnalysisGrid.filterSearch)specification;
				sql = selectType + " FROM \"stock\".\"AnalysisGrid.filterSearch\"(?, ?, ?, ?, ?) it";
				
				applyFilters = applyFilters.andThen(ps -> {
					try {
						ps.setInt(1, spec.getProjectID());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				applyFilters = applyFilters.andThen(ps -> {
					try {
						ps.setString(2, spec.getFilter());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				applyFilters = applyFilters.andThen(ps -> {
					try {
						ps.setString(3, spec.getAbc());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				applyFilters = applyFilters.andThen(ps -> {
					try {
						ps.setString(4, spec.getXyz());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				applyFilters = applyFilters.andThen(ps -> {
					try {
						ps.setString(5, spec.getClazz());
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

	public java.util.function.BiFunction<java.sql.ResultSet, Integer, Long> count(org.revenj.postgres.BulkReaderQuery query, org.revenj.patterns.Specification<gen.model.stock.AnalysisGrid> specification) {
		String selectType = "SELECT count(*)";
		final org.revenj.postgres.PostgresReader rdr = query.getReader();
		final org.revenj.postgres.PostgresWriter pgWriter = query.getWriter();
		int index = query.getArgumentIndex();
		StringBuilder sb = query.getBuilder();
		if (specification == null) {
			sb.append("SELECT count(*) FROM \"stock\".\"AnalysisGrid\" r");
		}
		
			else if (specification instanceof gen.model.stock.AnalysisGrid.filterSearch) {
				gen.model.stock.AnalysisGrid.filterSearch spec = (gen.model.stock.AnalysisGrid.filterSearch)specification;
				sb.append(selectType);
				sb.append(" FROM \"stock\".\"AnalysisGrid.filterSearch\"(?, ?, ?, ?, ?) it");
				
				query.addArgument(ps -> {
					try {
						ps.setInt(index + 1, spec.getProjectID());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				query.addArgument(ps -> {
					try {
						ps.setString(index + 2, spec.getFilter());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				query.addArgument(ps -> {
					try {
						ps.setString(index + 3, spec.getAbc());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				query.addArgument(ps -> {
					try {
						ps.setString(index + 4, spec.getXyz());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				query.addArgument(ps -> {
					try {
						ps.setString(index + 5, spec.getClazz());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
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
	public boolean exists(org.revenj.patterns.Specification<gen.model.stock.AnalysisGrid> specification) {
		final String selectType = "SELECT exists(SELECT *";
		java.util.function.Consumer<java.sql.PreparedStatement> applyFilters = ps -> {};
		java.sql.Connection connection = getConnection();
		try (org.revenj.postgres.PostgresWriter pgWriter = org.revenj.postgres.PostgresWriter.create()) {
			String sql = null;
			if (specification == null) {
				sql = "SELECT exists(SELECT * FROM \"stock\".\"AnalysisGrid\" r";
			} 
			else if (specification instanceof gen.model.stock.AnalysisGrid.filterSearch) {
				gen.model.stock.AnalysisGrid.filterSearch spec = (gen.model.stock.AnalysisGrid.filterSearch)specification;
				sql = selectType + " FROM \"stock\".\"AnalysisGrid.filterSearch\"(?, ?, ?, ?, ?) it";
				
				applyFilters = applyFilters.andThen(ps -> {
					try {
						ps.setInt(1, spec.getProjectID());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				applyFilters = applyFilters.andThen(ps -> {
					try {
						ps.setString(2, spec.getFilter());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				applyFilters = applyFilters.andThen(ps -> {
					try {
						ps.setString(3, spec.getAbc());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				applyFilters = applyFilters.andThen(ps -> {
					try {
						ps.setString(4, spec.getXyz());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				applyFilters = applyFilters.andThen(ps -> {
					try {
						ps.setString(5, spec.getClazz());
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

	public java.util.function.BiFunction<java.sql.ResultSet, Integer, Boolean> exists(org.revenj.postgres.BulkReaderQuery query, org.revenj.patterns.Specification<gen.model.stock.AnalysisGrid> specification) {
		String selectType = "exists(SELECT *";
		final org.revenj.postgres.PostgresReader rdr = query.getReader();
		final org.revenj.postgres.PostgresWriter pgWriter = query.getWriter();
		int index = query.getArgumentIndex();
		StringBuilder sb = query.getBuilder();
		if (specification == null) {
			sb.append("exists(SELECT * FROM \"stock\".\"AnalysisGrid\" r");
		}
		
			else if (specification instanceof gen.model.stock.AnalysisGrid.filterSearch) {
				gen.model.stock.AnalysisGrid.filterSearch spec = (gen.model.stock.AnalysisGrid.filterSearch)specification;
				sb.append(selectType);
				sb.append(" FROM \"stock\".\"AnalysisGrid.filterSearch\"(?, ?, ?, ?, ?) it");
				
				query.addArgument(ps -> {
					try {
						ps.setInt(index + 1, spec.getProjectID());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				query.addArgument(ps -> {
					try {
						ps.setString(index + 2, spec.getFilter());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				query.addArgument(ps -> {
					try {
						ps.setString(index + 3, spec.getAbc());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				query.addArgument(ps -> {
					try {
						ps.setString(index + 4, spec.getXyz());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
				query.addArgument(ps -> {
					try {
						ps.setString(index + 5, spec.getClazz());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
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
	public java.util.List<gen.model.stock.AnalysisGrid> find(String[] uris) {
		java.sql.Connection connection = getConnection();
		try (java.sql.PreparedStatement statement = connection.prepareStatement("SELECT r FROM \"stock\".\"AnalysisGrid\" r WHERE r.\"URI\" = ANY(?)")) {
			statement.setArray(1, connection.createArrayOf("text", uris));
			return readFromDb(statement, new java.util.ArrayList<>(uris.length));
		} catch (java.sql.SQLException | java.io.IOException e) {
			throw new RuntimeException(e);
		} finally {
			releaseConnection(connection);
		}
	}

	@Override
	public java.util.function.BiFunction<java.sql.ResultSet, Integer, java.util.Optional<gen.model.stock.AnalysisGrid>> find(org.revenj.postgres.BulkReaderQuery query, String uri) {
		final org.revenj.postgres.PostgresReader rdr = query.getReader();
		StringBuilder sb = query.getBuilder();
		int index = query.getArgumentIndex();
		if (uri == null) {
			sb.append("SELECT 0");
			return (rs, ind) -> java.util.Optional.empty();
		}
		sb.append("SELECT _r FROM \"stock\".\"AnalysisGrid\" _r WHERE _r.\"URI\" = ?");
		query.addArgument(ps -> {
			try {
				ps.setString(index, uri);
			} catch (java.sql.SQLException e) {
				throw new RuntimeException(e);
			}
		});
		return (rs, ind) -> 
		{
			try {
				String res = rs.getString(ind);
				if (res == null) {
					return java.util.Optional.empty();
				}
				rdr.process(res);
				gen.model.stock.AnalysisGrid instance = converter.from(rdr);
				if (!hasCustomSecurity) return java.util.Optional.of(instance);
				java.util.List<gen.model.stock.AnalysisGrid> result = new java.util.ArrayList<>(1);
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
	public java.util.function.BiFunction<java.sql.ResultSet, Integer, java.util.List<gen.model.stock.AnalysisGrid>> find(org.revenj.postgres.BulkReaderQuery query, String[] uris) {
		final org.revenj.postgres.PostgresReader rdr = query.getReader();
		final org.revenj.postgres.PostgresWriter writer = query.getWriter();
		StringBuilder sb = query.getBuilder();
		int index = query.getArgumentIndex();
		if (uris == null || uris.length == 0) {
			sb.append("SELECT 0");
			return (rs, ind) -> new java.util.ArrayList<>(0);
		}
		sb.append("SELECT array_agg(_r) FROM \"stock\".\"AnalysisGrid\" _r WHERE _r.\"URI\" = ANY(?)");
		query.addArgument(ps -> {
			try {
				org.postgresql.util.PGobject arr = new org.postgresql.util.PGobject();
				arr.setType("text[]");
				writer.reset();
				org.revenj.postgres.converters.PostgresTuple tuple = org.revenj.postgres.converters.ArrayTuple.create(uris, org.revenj.postgres.converters.StringConverter::toTuple);
				tuple.buildTuple(writer, false);
				arr.setValue(writer.toString());
				ps.setObject(index, arr);
			} catch (java.sql.SQLException e) {
				throw new RuntimeException(e);
			}
		});
		return (rs, ind) -> 
		{
			try {
				String res = rs.getString(ind);
				if (res == null || res.length() == 0 || res.length() == 2) {
					return new java.util.ArrayList<>(0);
				}
				rdr.process(res);
				java.util.List<gen.model.stock.AnalysisGrid> result = org.revenj.postgres.converters.ArrayTuple.parse(rdr, 0, converter::from); 
				
				return result;
			} catch (java.sql.SQLException | java.io.IOException e) {
				throw new RuntimeException(e);
			}
		};
	}

}
