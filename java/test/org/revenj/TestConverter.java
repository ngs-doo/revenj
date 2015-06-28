package org.revenj;

import gen.model._DatabaseCommon.Factorytest.CompositeConverter;
import gen.model._DatabaseCommon.Factorytest.SimpleConverter;
import gen.model.test.Composite;
import gen.model.test.Simple;
import org.junit.Assert;
import org.junit.Test;

import org.postgresql.util.PGobject;
import org.revenj.postgres.ObjectConverter;
import org.revenj.postgres.PostgresReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class TestConverter {

	private static final List<ObjectConverter.ColumnInfo> columns = Arrays.asList(
			new ObjectConverter.ColumnInfo("test", "Simple", "number", "pg_catalog", "int4", (short) 1, false, true),
			new ObjectConverter.ColumnInfo("test", "Simple", "text", "pg_catalog", "varchar", (short) 2, false, true),
			new ObjectConverter.ColumnInfo("test", "-ngs_Simple_type-", "number", "pg_catalog", "int4", (short) 1, false, true),
			new ObjectConverter.ColumnInfo("test", "-ngs_Simple_type-", "text", "pg_catalog", "varchar", (short) 2, false, true),
			new ObjectConverter.ColumnInfo("test", "Composite_entity", "id", "pg_catalog", "uuid", (short) 1, false, true),
			new ObjectConverter.ColumnInfo("test", "Composite_entity", "simple", "test", "Simple", (short) 2, false, true),
			new ObjectConverter.ColumnInfo("test", "-ngs_Composite_type-", "id", "pg_catalog", "uuid", (short) 1, false, true),
			new ObjectConverter.ColumnInfo("test", "-ngs_Composite_type-", "simple", "test", "Simple", (short) 2, false, true)
	);

	@Test
	public void simpleConversion() throws IOException {
		String input = "(1,abc)";
		PostgresReader reader = new PostgresReader();
		reader.process(input);
		SimpleConverter converter = new SimpleConverter(columns);
		converter.configure(null);
		Simple instance = converter.from(reader);
		Assert.assertEquals(1, instance.getNumber());
		Assert.assertEquals("abc", instance.getText());
	}

	@Test
	public void testDbDriverConverterObject() throws IOException, SQLException {
		Properties props = new Properties();
		props.load(new FileReader(new File("test.properties")));
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/revenj", props)) {
			connection.setAutoCommit(false);
			Statement st = connection.createStatement();
			st.execute("CREATE TYPE Simple AS (i int, t text)");
			ResultSet rs = st.executeQuery("SELECT '(1,abc)'::Simple");
			rs.next();
			PGobject obj = (PGobject) rs.getObject(1);
			PostgresReader reader = new PostgresReader();
			reader.process(obj.getValue());
			SimpleConverter converter = new SimpleConverter(columns);
			converter.configure(null);
			Simple instance = converter.from(reader);
			connection.rollback();
			Assert.assertEquals(1, instance.getNumber());
			Assert.assertEquals("abc", instance.getText());
		}
	}

	@Test
	public void testDbDriverConverterArray() throws IOException, SQLException {
		Properties props = new Properties();
		props.load(new FileReader(new File("test.properties")));
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/revenj", props)) {
			connection.setAutoCommit(false);
			Statement st = connection.createStatement();
			st.execute("CREATE TYPE Simple AS (i int, t text)");
			ResultSet rs = st.executeQuery("SELECT ARRAY['(1,abc)', '(22,\"x,y,z\")']::Simple[]");
			rs.next();
			Object[] arr = (Object[]) rs.getArray(1).getArray();
			PostgresReader reader = new PostgresReader();
			Simple[] result = new Simple[arr.length];
			SimpleConverter converter = new SimpleConverter(columns);
			converter.configure(null);
			for (int i = 0; i < arr.length; i++) {
				reader.process(((PGobject) arr[i]).getValue());
				result[i] = converter.from(reader);
			}
			connection.rollback();
			Assert.assertEquals(2, result.length);
			Assert.assertEquals(1, result[0].getNumber());
			Assert.assertEquals("abc", result[0].getText());
			Assert.assertEquals(22, result[1].getNumber());
			Assert.assertEquals("x,y,z", result[1].getText());
		}
	}

	@Test
	public void compositeConversion() throws IOException, SQLException {
		String input = "(6a07867f-1b23-416d-893a-6e493157e268,\"(1,abc)\")";
		PostgresReader reader = new PostgresReader();
		reader.process(input);
		MapServiceLocator locator = new MapServiceLocator(columns);
		CompositeConverter converter = locator.resolve(CompositeConverter.class);
		Composite instance = converter.from(reader);
		Assert.assertEquals(UUID.fromString("6a07867f-1b23-416d-893a-6e493157e268"), instance.getId());
		Assert.assertEquals(1, instance.getSimple().getNumber());
		Assert.assertEquals("abc", instance.getSimple().getText());
	}
}
