package org.revenj;

import gen.model.test.Composite;
import gen.model.test.Simple;
import org.junit.Assert;
import org.junit.Test;
import org.postgresql.util.PGobject;
import org.revenj.patterns.ServiceLocator;
import org.revenj.postgres.ObjectConverter;
import org.revenj.postgres.PostgresReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class TestRepository {

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
	public void repositoryTest() throws IOException, SQLException {
		MapServiceLocator locator = new MapServiceLocator(columns);
		CompositeRepository repository = new CompositeRepository(locator);
		Composite co = new Composite();
		UUID id = UUID.randomUUID();
		co.setId(id);
		Simple so = new Simple();
		so.setNumber(5);
		so.setText("test me ' \\ \" now");
		co.setSimple(so);
		String uri = repository.insert(co);
		Assert.assertEquals(id.toString(), uri);
		Optional<Composite> found = repository.find(uri);
		Assert.assertTrue(found.isPresent());
		Composite co2 = found.get();
		Assert.assertEquals(co.getId(), co2.getId());
		Assert.assertEquals(co.getSimple().getNumber(), co2.getSimple().getNumber());
		Assert.assertEquals(co.getSimple().getText(), co2.getSimple().getText());
		co2.getSimple().setNumber(6);
		repository.update(co, co2);
		found = repository.find(uri);
		Assert.assertTrue(found.isPresent());
		Composite co3 = found.get();
		Assert.assertEquals(co2.getId(), co3.getId());
		Assert.assertEquals(co2.getSimple().getNumber(), co3.getSimple().getNumber());
		Assert.assertEquals(co2.getSimple().getText(), co3.getSimple().getText());
		repository.delete(co3);
		found = repository.find(uri);
		Assert.assertFalse(found.isPresent());
	}
}
