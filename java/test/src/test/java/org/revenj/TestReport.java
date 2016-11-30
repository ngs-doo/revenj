package org.revenj;

import gen.model.test.Composite;
import gen.model.test.CompositeCube;
import gen.model.test.CompositeList;
import gen.model.test.FindMany;
import org.junit.Assert;
import org.junit.Test;
import org.revenj.patterns.DataContext;
import org.revenj.patterns.ServiceLocator;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class TestReport extends Setup {

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
		Assert.assertEquals(Integer.class, first.get("number").getClass());
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

	@Test
	public void useExoticFieldsInCube() throws IOException {
		ServiceLocator locator = container;
		DataContext context = locator.resolve(DataContext.class);
		Composite co = new Composite();
		co.getSimple().setNumber(200);
		context.create(co);
		CompositeCube cube = new CompositeCube(locator);
		List<Map<String, Object>> results =
				cube.builder()
						.use(CompositeCube.avgInd)
						.use(CompositeCube.count)
						.use(CompositeCube.enn)
						.use(CompositeCube.hasEntities)
						.use(CompositeCube.hasSum)
						.use(CompositeCube.indexes)
						.use(CompositeCube.max)
						.use(CompositeCube.min)
						.use(CompositeCube.number)
						.use(CompositeCube.simple)
						.analyze(new CompositeList.ForSimple(Arrays.asList(co.getSimple())));
		Assert.assertEquals(1, results.size());
	}

	@Test
	public void canStreamCube() throws IOException, SQLException {
		ServiceLocator locator = container;
		DataSource ds = locator.resolve(DataSource.class);
		Connection conn = ds.getConnection();
		DataContext context = locator.resolve(DataContext.class);
		Composite co = new Composite();
		co.getSimple().setNumber(5432);
		context.create(co);
		CompositeCube cube = new CompositeCube(locator);
		ResultSet rs = cube.stream(
				conn,
				Collections.singletonList(CompositeCube.number),
				Arrays.asList(CompositeCube.count, CompositeCube.max),
				null,
				it -> it.getNumber() == 5432,
				null,
				null);
		Assert.assertTrue(rs.next());
		Assert.assertEquals(5432, rs.getInt(1));
		Assert.assertFalse(rs.next());
		rs.close();
		conn.close();
	}
}
