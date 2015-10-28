package org.revenj;

import gen.model.Boot;
import gen.model.Seq.Next;
import gen.model.mixinReference.Author;
import gen.model.mixinReference.SpecificReport;
import gen.model.test.Clicked;
import gen.model.test.Composite;
import gen.model.test.Simple;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.revenj.extensibility.Container;
import org.revenj.patterns.DataContext;
import org.revenj.patterns.ServiceLocator;
import org.revenj.patterns.UnitOfWork;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class TestDataContext {

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
	public void canCreateAggregate() throws IOException {
		ServiceLocator locator = container;
		DataContext context = locator.resolve(DataContext.class);
		Composite co = new Composite();
		UUID id = UUID.randomUUID();
		co.setId(id);
		Simple so = new Simple();
		so.setNumber(5);
		so.setText("test me ' \\ \" now");
		co.setSimple(so);
		context.create(co);
		Optional<Composite> found = context.find(Composite.class, id.toString());
		Assert.assertTrue(found.isPresent());
		Composite co2 = found.get();
		Assert.assertEquals(co.getId(), co2.getId());
		Assert.assertEquals(co.getSimple().getNumber(), co2.getSimple().getNumber());
		Assert.assertEquals(co.getSimple().getText(), co2.getSimple().getText());
	}

	@Test
	public void canSubmitEvent() throws IOException {
		ServiceLocator locator = container;
		DataContext context = locator.resolve(DataContext.class);
		Clicked cl = new Clicked().setBigint(Long.MAX_VALUE).setDate(LocalDate.now()).setNumber(BigDecimal.valueOf(11.22));
		context.submit(cl);
	}

	@Test
	public void canQuery() throws IOException {
		ServiceLocator locator = container;
		DataContext context = locator.resolve(DataContext.class);
		long total1 = context.query(Next.class).count();
		context.create(Arrays.asList(new Next(), new Next()));
		long total2 = context.query(Next.class).count();
		Assert.assertEquals(total1 + 2, total2);
	}

	@Test
	public void canSearch() throws IOException {
		ServiceLocator locator = container;
		DataContext context = locator.resolve(DataContext.class);
		long total1 = context.search(Next.class).size();
		context.create(Arrays.asList(new Next(), new Next()));
		long total2 = context.search(Next.class).size();
		Assert.assertEquals(total1 + 2, total2);
	}

	@Test
	public void canCount() throws IOException {
		ServiceLocator locator = container;
		DataContext context = locator.resolve(DataContext.class);
		long total1 = context.count(Next.class);
		context.create(Arrays.asList(new Next(), new Next()));
		long total2 = context.count(Next.class);
		Assert.assertEquals(total1 + 2, total2);
	}

	@Test
	public void unitOfWork() throws IOException {
		ServiceLocator locator = container;
		try (UnitOfWork uow = locator.resolve(UnitOfWork.class)) {
			Next next = new Next();
			uow.create(next);
			Optional<Next> find = uow.find(Next.class, next.getURI());
			Assert.assertEquals(next, find.get());
		}
	}

	@Test
	public void transactionsWithUnitOfWork() throws IOException {
		ServiceLocator locator = container;
		try (UnitOfWork uow1 = locator.resolve(UnitOfWork.class);
			UnitOfWork uow2 = locator.resolve(UnitOfWork.class)) {
			Next next = new Next();
			uow1.create(next);
			Optional<Next> find1 = uow1.find(Next.class, next.getURI());
			Optional<Next> find2 = uow2.find(Next.class, next.getURI());
			Assert.assertEquals(next, find1.get());
			Assert.assertFalse(find2.isPresent());
		}
	}

	@Test
	public void referenceIDUpdate() throws Exception {
		ServiceLocator locator = container;
		SpecificReport report = new SpecificReport();
		Author author = new Author();
		report.setAuthor(author);
		Assert.assertEquals(author.getURI(), report.getAuthorURI());
		Assert.assertEquals(author.getID(), report.getAuthorID());
		DataContext context = locator.resolve(DataContext.class);
		context.create(author);
		Assert.assertEquals(author.getURI(), report.getAuthorURI());
		Assert.assertEquals(author.getID(), report.getAuthorID());
		context.create(report);
	}
}
