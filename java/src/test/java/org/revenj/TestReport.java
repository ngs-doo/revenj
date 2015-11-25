package org.revenj;

import gen.model.Boot;
import gen.model.test.Composite;
import gen.model.test.CompositeCube;
import gen.model.test.CompositeList;
import gen.model.test.FindMany;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.revenj.extensibility.Container;
import org.revenj.patterns.DataContext;
import org.revenj.patterns.OlapCubeQuery;
import org.revenj.patterns.ServiceLocator;

import java.io.IOException;
import java.util.*;

public class TestReport {

	private Container container;

	@Before
	public void initContainer() throws IOException {
		container = (Container) Boot.configure("jdbc:postgresql://localhost/revenj");
	}

	@After
	public void closeContainer() throws Exception {
		container.close();
	}

	@Test
	public void canPopulateReport() throws IOException {
		ServiceLocator locator = container;
		DataContext context = locator.resolve(DataContext.class);
		Composite co = new Composite();
		UUID id = UUID.randomUUID();
		co.setId(id);
		context.create(co);
		FindMany fm = new FindMany(id, new HashSet<>(Arrays.asList(id)));
		FindMany.Result result = fm.populate(locator);
		Assert.assertEquals(id, result.getFound().getId());
	}

	@Test
	public void canQueryCube() throws IOException {
		ServiceLocator locator = container;
		CompositeCube cube = new CompositeCube(locator);
		List<Map<String, Object>> results =
				cube.builder()
						.use(CompositeCube.number)
						.use(CompositeCube.min)
						.use(CompositeCube.max)
						.limit(10)
						.analyze();
		Assert.assertNotEquals(0, results.size());
		Map<String, Object> first = results.get(0);
		Assert.assertEquals(3, first.size());
		Assert.assertTrue(first.containsKey("number"));
		Assert.assertTrue(first.containsKey("min"));
		Assert.assertTrue(first.containsKey("max"));
		final int number = (int) first.get("number");
		Assert.assertTrue(number > 0);
		Assert.assertTrue(results.size() < 11);
	}

	@Test
	public void canFilterCubeWithLambda() throws IOException {
		ServiceLocator locator = container;
		DataContext context = locator.resolve(DataContext.class);
		Composite co = new Composite();
		co.getSimple().setNumber(100);
		context.create(co);
		CompositeCube cube = new CompositeCube(locator);
		List<Map<String, Object>> results =
				cube.builder()
						.use(CompositeCube.min)
						.use(CompositeCube.max)
						.analyze(it -> it.getNumber() > 10 && it.getNumber() < 100);
		Assert.assertEquals(1, results.size());
	}

	@Test
	public void canFilterCubeWithSpecification() throws IOException {
		ServiceLocator locator = container;
		DataContext context = locator.resolve(DataContext.class);
		Composite co = new Composite();
		co.getSimple().setNumber(100);
		context.create(co);
		CompositeCube cube = new CompositeCube(locator);
		List<Map<String, Object>> results =
				cube.builder()
						.use(CompositeCube.min)
						.use(CompositeCube.max)
						.analyze(new CompositeList.ForSimple());
		Assert.assertEquals(1, results.size());
	}
}
