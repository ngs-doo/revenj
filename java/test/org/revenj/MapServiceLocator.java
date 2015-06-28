package org.revenj;

import gen.model._DatabaseCommon.Factorytest.CompositeConverter;
import gen.model._DatabaseCommon.Factorytest.SimpleConverter;
import org.revenj.patterns.ServiceLocator;
import org.revenj.postgres.ObjectConverter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

class MapServiceLocator implements ServiceLocator {

	public final Map<Type, Object> container = new HashMap<>();

	public MapServiceLocator(List<ObjectConverter.ColumnInfo> columns) throws IOException, SQLException {
		Properties props = new Properties();
		props.load(new FileReader(new File("test.properties")));
		Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/revenj", props);
		container.put(Connection.class, connection);
		SimpleConverter sc = new SimpleConverter(columns);
		container.put(SimpleConverter.class, sc);
		CompositeConverter cc = new CompositeConverter(columns);
		container.put(CompositeConverter.class, cc);
		sc.configure(this);
		cc.configure(this);
	}

	@Override
	public Optional<Object> tryResolve(Type type) {
		return Optional.ofNullable(container.get(type));
	}
}