package org.revenj;

import gen.model._DatabaseCommon.Factorytest.CompositeConverter;
import gen.model.test.Composite;
import org.postgresql.util.PGobject;
import org.revenj.patterns.PersistableRepository;
import org.revenj.patterns.ServiceLocator;
import org.revenj.postgres.PostgresReader;
import org.revenj.postgres.converters.ArrayTuple;
import org.revenj.postgres.converters.PostgresTuple;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class CompositeRepository implements PersistableRepository<Composite> {

	private final Connection connection;
	private final CompositeConverter converter;
	private final ServiceLocator locator;

	public CompositeRepository(ServiceLocator locator) {
		this.locator = locator;
		this.connection = locator.resolve(Connection.class);
		this.converter = locator.resolve(CompositeConverter.class);
	}

	@Override
	public List<String> persist(
			List<Composite> insert,
			List<Map.Entry<Composite, Composite>> update,
			List<Composite> delete) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM test.\"persist_Composite\"(?, ?, ?, ?)")) {
			List<String> result;
			if (insert != null && !insert.isEmpty()) {
				result = new ArrayList<>(insert.size());
				PostgresTuple tuple = ArrayTuple.create(insert, converter);
				PGobject pgo = new PGobject();
				pgo.setType("\"test\".\"Composite_entity\"[]");
				pgo.setValue(tuple.buildTuple(false));
				statement.setObject(1, pgo);
				for(int i = 0; i < insert.size();i++) {
					result.add(insert.get(0).getId().toString());
				}
			} else {
				statement.setArray(1, null);
				result = new ArrayList<>(0);
			}
			if (update != null && !update.isEmpty()) {
				List<Composite> oldUpdate = new ArrayList<>(update.size());
				List<Composite> newUpdate = new ArrayList<>(update.size());
				for(Map.Entry<Composite, Composite> it : update) {
					oldUpdate.add(it.getKey());
					newUpdate.add(it.getValue());
				}
				PostgresTuple tupleOld = ArrayTuple.create(oldUpdate, converter);
				PostgresTuple tupleNew = ArrayTuple.create(newUpdate, converter);
				PGobject pgOld = new PGobject();
				PGobject pgNew = new PGobject();
				pgOld.setType("\"test\".\"Composite_entity\"[]");
				pgNew.setType("\"test\".\"Composite_entity\"[]");
				pgOld.setValue(tupleOld.buildTuple(false));
				pgNew.setValue(tupleNew.buildTuple(false));
				statement.setObject(2, pgOld);
				statement.setObject(3, pgNew);
			} else {
				statement.setArray(2, null);
				statement.setArray(3, null);
			}
			if (delete != null && !delete.isEmpty()) {
				result = new ArrayList<>(delete.size());
				PostgresTuple tuple = ArrayTuple.create(delete, converter);
				PGobject pgo = new PGobject();
				pgo.setType("\"test\".\"Composite_entity\"[]");
				pgo.setValue(tuple.buildTuple(false));
				statement.setObject(4, pgo);
			} else {
				statement.setArray(4, null);
			}
			try(ResultSet rs = statement.executeQuery()) {
				rs.next();
				String message = rs.getString(1);
				if (message != null) throw new SQLException(message);
			}
			return result;
		}
	}

	@Override
	public List<Composite> find(String[] uris) {
		try (PreparedStatement statement = connection.prepareStatement("SELECT r from test.\"Composite_entity\" r WHERE r.id = ANY(?)")) {
			UUID[] uuids = new UUID[uris.length];
			for (int i = 0; i < uuids.length; i++) {
				uuids[i] = UUID.fromString(uris[i]);
			}
			Array ids = connection.createArrayOf("uuid", uuids);
			statement.setArray(1, ids);
			ArrayList<Composite> result = new ArrayList<>(uris.length);
			PostgresReader reader = new PostgresReader(locator::resolve);
			try(ResultSet rs = statement.executeQuery()) {
				while (rs.next()) {
					PGobject pgo = (PGobject) rs.getObject(1);
					reader.process(pgo.getValue());
					result.add(converter.from(reader));
				}
			}
			return result;
		} catch (SQLException | IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
