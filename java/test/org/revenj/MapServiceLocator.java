package org.revenj;

import org.revenj.patterns.ServiceLocator;
import org.revenj.postgres.ObjectConverter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

class MapServiceLocator implements ServiceLocator {

	public final Map<String, Object> container = new HashMap<>();

	public MapServiceLocator(List<ObjectConverter.ColumnInfo> columns) throws IOException, SQLException {
		Properties props = new Properties();
		props.load(new FileReader(new File("test.properties")));
		Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/revenj", props);
		container.put(Connection.class.getName(), connection);
		container.put(SimpleConverter.class.getName(), new SimpleConverter(this, columns));
		container.put(CompositeConverter.class.getName(), new CompositeConverter(this, columns));
	}

	@Override
	public Optional<Object> resolve(String name) {
		return Optional.ofNullable(container.get(name));
	}
}