package org.revenj;

import gen.model.Boot;
import gen.model.test.Composite;
import gen.model.test.Simple;
import gen.model.test.repositories.CompositeRepository;
import org.junit.Assert;
import org.junit.Test;
import org.revenj.patterns.Container;
import org.revenj.patterns.GenericType;
import org.revenj.patterns.PersistableRepository;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class TestRepository {

	@Test
	public void repositoryTest() throws IOException, SQLException {
		Container locator = Boot.start("jdbc:postgresql://localhost:5432/revenj");
		PersistableRepository<Composite> repository = locator.resolve(CompositeRepository.class);
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
