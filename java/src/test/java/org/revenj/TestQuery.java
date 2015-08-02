package org.revenj;

import gen.model.Boot;
import gen.model.Seq.Next;
import gen.model.Seq.repositories.NextRepository;
import gen.model.test.*;
import gen.model.test.repositories.*;
import org.jinq.orm.stream.JinqStream;
import org.junit.Assert;
import org.junit.Test;
import org.revenj.patterns.*;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestQuery {

    @Test
    public void simpleQuery() throws IOException {
        ServiceLocator locator = Boot.configure("jdbc:postgresql://localhost:5432/revenj");
        NextRepository repository = locator.resolve(NextRepository.class);
        repository.insert(new Next());
        List<Next> search = repository.search();
        Query<Next> stream = repository.query();
        List<Next> list = stream.list();
        Assert.assertEquals(search.size(), list.size());
    }

    @Test
    public void queryWithFilter() throws IOException {
        ServiceLocator locator = Boot.configure("jdbc:postgresql://localhost:5432/revenj");
        NextRepository repository = locator.resolve(NextRepository.class);
        String uri = repository.insert(new Next());
        int id = Integer.parseInt(uri);
        Optional<Next> found = repository.query().filter(next -> next.getID() == id).findAny();
        Assert.assertTrue(found.isPresent());
        Assert.assertEquals(id, found.get().getID());
    }

    @Test
    public void queryWithFilterAndCount() throws IOException {
        ServiceLocator locator = Boot.configure("jdbc:postgresql://localhost:5432/revenj");
        NextRepository repository = locator.resolve(NextRepository.class);
        String uri = repository.insert(new Next());
        int id = Integer.parseInt(uri);
        long found = repository.query().filter(next -> next.getID() == id).count();
        Assert.assertEquals(1, found);
        //TODO: doesn't work correctly
        //found = repository.query().filter(next -> false).count();
        //Assert.assertEquals(0, found);
    }

    @Test
    public void queryWithFilterAndFindAny() throws IOException {
        ServiceLocator locator = Boot.configure("jdbc:postgresql://localhost:5432/revenj");
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
        ServiceLocator locator = Boot.configure("jdbc:postgresql://localhost:5432/revenj");
        NextRepository repository = locator.resolve(NextRepository.class);
        String uri = repository.insert(new Next());
        int id = Integer.parseInt(uri);
        List<Integer> numbers = Arrays.asList(-1, 0, id);
        List<Next> found = repository.query().filter(it -> numbers.contains(it.getID())).limit(2).list();
        Assert.assertEquals(1, found.size());
        Assert.assertEquals(id, found.get(0).getID());
    }

    @Test
    public void uuidToString() throws IOException {
        ServiceLocator locator = Boot.configure("jdbc:postgresql://localhost:5432/revenj");
        CompositeRepository repository = locator.resolve(CompositeRepository.class);
        UUID id = UUID.randomUUID();
        String uri = repository.insert(new Composite().setId(id));
        Composite found = repository.query().filter(it -> it.getId().toString().equals(uri)).findAny().get();
        Assert.assertEquals(id, found.getId());
    }

	@Test
	public void sendDomainObjectAsArgument() throws IOException {
		ServiceLocator locator = Boot.configure("jdbc:postgresql://localhost:5432/revenj");
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
		ServiceLocator locator = Boot.configure("jdbc:postgresql://localhost:5432/revenj");
		NextRepository repository = locator.resolve(NextRepository.class);
		String uri = repository.insert(new Next());
		int id = Integer.parseInt(uri);
		List<Next> found = repository.search(it -> it.getID() == id);
		Assert.assertEquals(1, found.size());
		Assert.assertEquals(id, found.get(0).getID());
	}

	@Test
	public void queryWithRegisteredSpecification() throws IOException {
		ServiceLocator locator = Boot.configure("jdbc:postgresql://localhost:5432/revenj");
		NextRepository repository = locator.resolve(NextRepository.class);
		String uri = repository.insert(new Next());
		int id = Integer.parseInt(uri);
		long size = repository.query(new Next.BetweenIds(id, id)).count();
		Assert.assertEquals(1, size);
	}
}
