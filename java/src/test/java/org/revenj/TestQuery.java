package org.revenj;

import gen.model.Boot;
import gen.model.Seq.Next;
import gen.model.Seq.repositories.NextRepository;
import gen.model.test.Clicked;
import gen.model.test.Composite;
import gen.model.test.En;
import gen.model.test.Simple;
import gen.model.test.repositories.CompositeRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.revenj.extensibility.Container;
import org.revenj.patterns.DataContext;
import org.revenj.patterns.Query;
import org.revenj.patterns.ServiceLocator;
import org.revenj.patterns.Specification;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

public class TestQuery {

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
	public void simpleQuery() throws IOException {
		ServiceLocator locator = container;
		NextRepository repository = locator.resolve(NextRepository.class);
		repository.insert(new Next());
		List<Next> search = repository.search();
		Query<Next> stream = repository.query();
		List<Next> list = stream.list();
		Assert.assertEquals(search.size(), list.size());
	}

	@Test
	public void queryWithFilter() throws IOException {
		ServiceLocator locator = container;
		NextRepository repository = locator.resolve(NextRepository.class);
		String uri = repository.insert(new Next());
		int id = Integer.parseInt(uri);
		Optional<Next> found = repository.query().filter(next -> next.getID() == id).findAny();
		Assert.assertTrue(found.isPresent());
		Assert.assertEquals(id, found.get().getID());
	}

	@Test
	public void queryWithFilterAndCount() throws IOException {
		ServiceLocator locator = container;
		NextRepository repository = locator.resolve(NextRepository.class);
		String uri = repository.insert(new Next());
		int id = Integer.parseInt(uri);
		long found = repository.query().filter(next -> next.getID() == id).count();
		Assert.assertEquals(1, found);
		found = repository.query().filter(next -> false).count();
		Assert.assertEquals(0, found);
		found = repository.query().filter(next -> true).count();
		Assert.assertNotEquals(0, found);
	}

	@Test
	public void queryWithFilterAndFindAny() throws IOException {
		ServiceLocator locator = container;
		NextRepository repository = locator.resolve(NextRepository.class);
		String uri = repository.insert(new Next());
		int id = Integer.parseInt(uri);
		repository.insert(new Next());
		boolean found = repository.query().anyMatch(next -> next.getID() == id);
		Assert.assertTrue(found);
		found = repository.query().allMatch(next -> next.getID() == id);
		Assert.assertFalse(found);
		found = repository.query().noneMatch(next -> next.getID() == id);
		Assert.assertFalse(found);
		found = repository.query().noneMatch(next -> next.getID() == id + 2);
		Assert.assertTrue(found);
	}

	@Test
	public void collectionContainsQuery() throws IOException {
		ServiceLocator locator = container;
		NextRepository repository = locator.resolve(NextRepository.class);
		String uri = repository.insert(new Next());
		int id = Integer.parseInt(uri);
		List<Integer> numbers = Arrays.asList(-1, 0, id);
		List<Next> found = repository.query().filter(it -> numbers.contains(it.getID())).limit(2).list();
		Assert.assertEquals(1, found.size());
		Assert.assertEquals(id, found.get(0).getID());
	}

	@Test
	public void uuidFunctions() throws IOException {
		ServiceLocator locator = container;
		CompositeRepository repository = locator.resolve(CompositeRepository.class);
		UUID id = UUID.randomUUID();
		String uri = repository.insert(new Composite().setId(id));
		Composite found = repository.query().filter(it -> it.getId().toString().equals(uri)).findAny().get();
		Assert.assertEquals(id, found.getId());
		found = repository.query().filter(it -> UUID.fromString(it.getURI()).equals(id)).findAny().get();
		Assert.assertEquals(id, found.getId());
	}

	@Test
	public void sendDomainObjectAsArgument() throws IOException {
		ServiceLocator locator = container;
		CompositeRepository repository = locator.resolve(CompositeRepository.class);
		UUID id = UUID.randomUUID();
		Random rnd = new Random();
		int next = rnd.nextInt(100000);
		repository.insert(new Composite().setId(id).setSimple(new Simple().setEn(En.A).setNumber(next)));
		Simple simple = new Simple().setEn(En.A).setNumber(next);
		Composite found = repository.query().filter(it -> it.getSimple().getNumber() == simple.getNumber()).sortedBy(it -> it.getId()).findFirst().get();
		Assert.assertEquals(id, found.getId());
	}

	@Test
	public void serchWithCustomSpec() throws IOException {
		ServiceLocator locator = container;
		NextRepository repository = locator.resolve(NextRepository.class);
		String uri = repository.insert(new Next());
		int id = Integer.parseInt(uri);
		List<Next> found = repository.search(it -> it.getID() == id);
		Assert.assertEquals(1, found.size());
		Assert.assertEquals(id, found.get(0).getID());
	}

	@Test
	public void queryWithRegisteredSpecification() throws IOException {
		ServiceLocator locator = container;
		NextRepository repository = locator.resolve(NextRepository.class);
		String uri = repository.insert(new Next());
		int id = Integer.parseInt(uri);
		long size = repository.query(new Next.BetweenIds(id, id)).count();
		Assert.assertEquals(1, size);
	}

	@Test
	public void toStringAndValueOf() throws IOException {
		ServiceLocator locator = container;
		NextRepository repository = locator.resolve(NextRepository.class);
		String uri = repository.insert(new Next());
		int id = Integer.parseInt(uri);
		List<Next> found = repository.search(it -> String.valueOf(it.getID()).equals(uri) && id == Integer.valueOf(it.getURI()));
		Assert.assertEquals(1, found.size());
		Assert.assertEquals(id, found.get(0).getID());
	}

	@Test
	public void modOperator() throws IOException {
		ServiceLocator locator = container;
		NextRepository repository = locator.resolve(NextRepository.class);
		long total = repository.query().count();
		long totalEven = repository.query(it -> it.getID() % 2 == 0).count();
		long totalOdd = repository.query(it -> it.getID() % 2 != 0).count();
		Assert.assertEquals(total, totalEven, totalOdd);
		String uri = repository.insert(new Next());
		int id = Integer.parseInt(uri);
		long newTotal = repository.query().count();
		long newTotalEven = repository.query(it -> it.getID() % 2 == 0).count();
		long newTotalOdd = repository.query(it -> it.getID() % 2 != 0).count();
		Assert.assertEquals(newTotal, newTotalEven, newTotalOdd);
		Assert.assertEquals(total + 1, newTotal);
		if (id % 2 == 0) {
			Assert.assertEquals(totalEven + 1, newTotalEven);
		} else {
			Assert.assertEquals(totalOdd + 1, newTotalOdd);
		}
	}

	@Test
	public void queryWithNotFilter() throws IOException {
		ServiceLocator locator = container;
		NextRepository repository = locator.resolve(NextRepository.class);
		String uri = repository.insert(new Next());
		int id = Integer.parseInt(uri);
		Specification<Next> filter = next -> next.getID() == id + 1;
		Optional<Next> found = repository.query().filter(filter).findAny();
		Assert.assertFalse(found.isPresent());
		//found = repository.query().filter(filter.negate()).findAny();
		//Assert.assertTrue(found.isPresent());
	}

	@Test
	public void localDateFunctions() throws IOException {
		ServiceLocator locator = container;
		DataContext db = locator.resolve(DataContext.class);
		LocalDate ld = LocalDate.now();
		Clicked cl = new Clicked().setDate(ld);
		db.submit(cl);
		Query<Clicked> query = db.query(Clicked.class);
		String uri = cl.getURI();
		boolean found1 = query.anyMatch(it -> it.getURI().equals(uri) && ld.compareTo(it.getDate()) == 0);
		boolean found2 = query.anyMatch(it -> it.getURI().equals(uri) && ld.equals(it.getDate()) && ld.isEqual(it.getDate()));
		boolean notFound = query.anyMatch(it -> it.getURI().equals(uri) && ld.compareTo(it.getDate()) != 0);
		Assert.assertTrue(found1);
		Assert.assertTrue(found2);
		Assert.assertFalse(notFound);
	}

	@Test
	public void offsetDateTimeFunctions() throws IOException {
		ServiceLocator locator = container;
		DataContext db = locator.resolve(DataContext.class);
		Clicked cl = new Clicked();
		db.submit(cl);
		Query<Clicked> query = db.query(Clicked.class);
		OffsetDateTime dt = cl.getQueuedAt();
		boolean found = query.anyMatch(it -> it.getURI().equals(cl.getURI()) && it.getProcessedAt().compareTo(dt) == 0);
		boolean notFound = query.anyMatch(it -> it.getURI().equals(cl.getURI()) && it.getProcessedAt().compareTo(dt) != 0);
		Assert.assertTrue(found);
		Assert.assertFalse(notFound);
	}

	//@Test
	public void numberFunctions() throws IOException {
		ServiceLocator locator = container;
		DataContext db = locator.resolve(DataContext.class);
		LocalDate ld = LocalDate.now();
		Clicked cl = new Clicked().setDate(ld);
		db.submit(cl);
		Query<Clicked> query = db.query(Clicked.class);
		String uri = cl.getURI();
		//TODO: produces incorrect queries
		boolean found1 = query.anyMatch(it -> Long.valueOf(3).equals(it.getBigint()) && it.getEn() == En.A || it.getURI().toUpperCase().equals(uri));
		boolean found2 = query.anyMatch(it -> 3L >= it.getBigint() && it.getDate() == LocalDate.now() || Integer.valueOf(it.getURI()).equals(Integer.valueOf(uri)));
		boolean found3 = query.anyMatch(it -> Long.valueOf(3).equals(it.getBigint()) && it.getEn() == En.B || it.getURI().toUpperCase().equals(uri));
		En b = En.B;
		boolean found4 = query.anyMatch(it -> it.getEn() == b || it.getURI().toUpperCase().equals(uri));
		Assert.assertTrue(found1);
		Assert.assertTrue(found2);
		Assert.assertTrue(found3);
		Assert.assertTrue(found4);
	}
}
