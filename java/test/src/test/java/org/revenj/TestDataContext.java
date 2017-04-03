package org.revenj;

import gen.model.Seq.Next;
import gen.model.binaries.Document;
import gen.model.binaries.ReadOnlyDocument;
import gen.model.binaries.WritableDocument;
import gen.model.calc.Info;
import gen.model.calc.Realm;
import gen.model.calc.Type;
import gen.model.events.Alert2;
import gen.model.events.AlertEventLog;
import gen.model.events.Event;
import gen.model.events.Root;
import gen.model.issues.TimestampPk;
import gen.model.mixinReference.Author;
import gen.model.mixinReference.SpecificReport;
import gen.model.sql.GuardCheck;
import gen.model.test.Clicked;
import gen.model.test.Composite;
import gen.model.test.FindMany;
import gen.model.test.Simple;
import org.junit.Assert;
import org.junit.Test;
import org.revenj.patterns.*;

import javax.sql.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;

public class TestDataContext extends Setup {

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
		String uri;
		try (UnitOfWork uow = locator.resolve(UnitOfWork.class)) {
			Next next = new Next();
			uow.create(next);
			uri = next.getURI();
			Optional<Next> find = uow.find(Next.class, uri);
			Assert.assertEquals(next, find.get());
		}
		DataContext ctx = locator.resolve(DataContext.class);
		Optional<Next> find = ctx.find(Next.class, uri);
		Assert.assertFalse(find.isPresent());
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
	public void dataContextWithConnection() throws Exception {
		ServiceLocator locator = container;
		javax.sql.DataSource ds = container.resolve(DataSource.class);
		Connection conn = ds.getConnection();
		conn.setAutoCommit(false);
		Function<Connection, DataContext> ctxFactory = locator.resolve(Function.class, Connection.class, DataContext.class);
		DataContext ctx = ctxFactory.apply(conn);
		Next next = new Next();
		ctx.create(next);
		String uri = next.getURI();
		Optional<Next> find1 = ctx.find(Next.class, uri);
		Assert.assertEquals(next, find1.get());
		conn.rollback();
		Optional<Next> find2 = ctx.find(Next.class, uri);
		Assert.assertFalse(find2.isPresent());
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

	@Test
	public void readOnlySqlConcept() throws Exception {
		ServiceLocator locator = container;
		DataContext context = locator.resolve(DataContext.class);
		Document document = new Document().setName("test me");
		context.create(document);
		UUID id = document.getID();
		Optional<ReadOnlyDocument> found =
				context.query(ReadOnlyDocument.class)
						.filter(it -> it.getID().equals(id))
						.findAny();
		Assert.assertTrue(found.isPresent());
		Assert.assertEquals("test me", found.get().getName());
	}

	@Test
	public void writableSqlConcept() throws Exception {
		ServiceLocator locator = container;
		DataContext context = locator.resolve(DataContext.class);
		Document document = new Document().setName("test me now");
		context.create(document);
		UUID id = document.getID();
		Optional<WritableDocument> found =
				context.query(WritableDocument.class)
						.filter(it -> it.getId().equals(id))
						.findAny();
		Assert.assertTrue(found.isPresent());
		WritableDocument wd = found.get();
		Assert.assertEquals("test me now", wd.getName());
		wd.setName("test me later");
		context.update(wd);
		Optional<Document> changed = context.find(Document.class, document.getURI());
		Assert.assertTrue(changed.isPresent());
		Assert.assertEquals("test me later", changed.get().getName());
	}

	@Test
	public void persistableCalculatedPrimaryKey() throws Exception {
		ServiceLocator locator = container;
		DataContext context = locator.resolve(DataContext.class);
		Random rnd = new Random();
		Type t = new Type().setSuffix("ab" + rnd.nextInt(100000)).setDescription("desc");
		context.create(t);
		Info i = new Info().setCode("xx" + rnd.nextInt(1000000)).setName("abcdef" + rnd.nextInt(1000000));
		context.create(i);
		Realm r = new Realm().setInfo(i).setRefType(t);
		context.create(r);
		Optional<Realm> found = context.find(Realm.class, r.getURI());
		Assert.assertTrue(found.isPresent());
		Assert.assertTrue(r.deepEquals(found.get()));
	}

	@Test
	public void canPopulateReport() throws IOException {
		ServiceLocator locator = container;
		DataContext context = locator.resolve(DataContext.class);
		Composite co = new Composite();
		UUID id = UUID.randomUUID();
		co.setId(id);
		context.create(co);
		FindMany.Result result = context.populate(new FindMany(id, new HashSet<>(Collections.singletonList(id))));
		Assert.assertEquals(id, result.getFound().getId());
	}

	@Test
	public void canLoadHistory() throws Exception {
		ServiceLocator locator = container;
		DataContext db = locator.resolve(DataContext.class);
		OffsetDateTime odt = OffsetDateTime.now();
		TimestampPk pk = new TimestampPk().setTs(odt).setD(BigDecimal.ONE);
		db.create(pk);
		String uri = pk.getURI();
		Optional<TimestampPk> found = db.find(TimestampPk.class, uri);
		Optional<History<TimestampPk>> foundHistory = db.history(TimestampPk.class, uri);
		Assert.assertTrue(found.isPresent());
		Assert.assertTrue(foundHistory.isPresent());
		Assert.assertEquals(1, foundHistory.get().getSnapshots().size());
		Assert.assertEquals(uri, foundHistory.get().getURI());
		//Assert.assertEquals(found.get(), foundHistory.get().getSnapshots().get(0).getValue());
		Assert.assertTrue(found.get().getTs().isEqual(foundHistory.get().getSnapshots().get(0).getValue().getTs()));
	}

	@Test
	public void sqlWithCast() throws Exception {
		ServiceLocator locator = container;
		DataContext db = locator.resolve(DataContext.class);
		List<GuardCheck> found = db.search(GuardCheck.class);
		Assert.assertEquals(1, found.size());
		Assert.assertEquals(0, BigDecimal.ONE.compareTo(found.get(0).getA()));
	}

	@Test
	public void rootWithEventReference() throws Exception {
		ServiceLocator locator = container;
		DataContext db = locator.resolve(DataContext.class);
		Root root = new Root();
		Event event = new Event();
		root.setEvent(event);
		db.submit(event);
		Assert.assertEquals(root.getEventURI(), event.getURI());
		db.create(root);
		Optional<Root> found = db.find(Root.class, root.getURI());
		Assert.assertTrue(found.isPresent());
		Assert.assertEquals(found.get().getEventURI(), event.getURI());
	}

	@Test
	public void eventWithReferences() throws Exception {
		ServiceLocator locator = container;
		DataContext db = locator.resolve(DataContext.class);
		Root root = new Root();
		Event event1 = new Event();
		Event event2 = new Event();
		event2.setEvent(event1);
		event2.setRoot(root);
		db.create(root);
		db.submit(event1);
		Assert.assertEquals(event2.getEventURI(), event1.getURI());
		Assert.assertEquals(event2.getRootURI(), root.getURI());
		db.submit(event2);
		Optional<Event> found = db.find(Event.class, event2.getURI());
		Assert.assertTrue(found.isPresent());
		Assert.assertEquals(found.get().getEventURI(), event1.getURI());
		Assert.assertEquals(found.get().getRootURI(), root.getURI());
	}

	@Test
	public void eventWithMixin() throws Exception {
		ServiceLocator locator = container;
		DataContext db = locator.resolve(DataContext.class);
		AlertEventLog log = new AlertEventLog();
		log.setAlert(new Alert2().setPartnerID(2).setProp2("abc"));
		db.submit(log);
		Optional<AlertEventLog> found = db.find(AlertEventLog.class, log.getURI());
		Assert.assertTrue(found.isPresent());
		Assert.assertEquals(2, found.get().getAlert().getPartnerID());
		Alert2 alert = (Alert2) found.get().getAlert();
		Assert.assertEquals("abc", alert.getProp2());
	}

	@Test
	public void canQueueEvent() throws Exception {
		ServiceLocator locator = container;
		DataContext db = locator.resolve(DataContext.class);
		long count = db.count(AlertEventLog.class);
		AlertEventLog log = new AlertEventLog();
		log.setAlert(new Alert2().setPartnerID(2).setProp2("abc"));
		db.queue(log);
		long newCount = 0;
		for (int i = 0; i < 10; i++) {
			newCount = db.count(AlertEventLog.class);
			if (count != newCount) break;
			Thread.sleep(100);
		}
		Assert.assertEquals(count + 1, newCount);
	}
}
