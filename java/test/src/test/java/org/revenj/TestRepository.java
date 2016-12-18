package org.revenj;

import gen.model.Seq.Next;
import gen.model.adt.BasicSecurity;
import gen.model.adt.User;
import gen.model.adt.repositories.UserRepository;
import gen.model.binaries.Document;
import gen.model.binaries.WritableDocument;
import gen.model.binaries.converters.DocumentConverter;
import gen.model.calc.converters.InfoConverter;
import gen.model.egzotics.*;
import gen.model.egzotics.repositories.TreeRepository;
import gen.model.issues.TimestampPk;
import gen.model.issues.repositories.TimestampPkRepository;
import gen.model.md.Detail;
import gen.model.md.Master;
import gen.model.md.repositories.MasterRepository;
import gen.model.mixinReference.Author;
import gen.model.mixinReference.repositories.AuthorRepository;
import gen.model.mixintest.Bar;
import gen.model.mixintest.Foo;
import gen.model.mixintest.ImplVoid;
import gen.model.mixintest.repositories.FooRepository;
import gen.model.stock.converters.AnalysisConverter;
import gen.model.test.*;
import gen.model.test.Composite;
import gen.model.test.repositories.*;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.revenj.handlers.ClickedCollectionEventHandler;
import org.revenj.patterns.*;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;

public class TestRepository extends Setup {

	@Test
	public void repositoryTest() throws IOException {
		ServiceLocator locator = container;
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
		Assert.assertEquals(co.getURI(), co2.getURI());
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

	@EventHandler
	public static class ClickedEventHandler implements DomainEventHandler<Clicked> {
		public static int COUNT;

		public void handle(Clicked event) {
			COUNT++;
		}
	}

	@EventHandler
	public static class ClickedLazyEventHandler implements DomainEventHandler<Callable<Clicked>> {
		public static int COUNT;

		public void handle(Callable<Clicked> event) {
			COUNT++;
		}
	}

	@EventHandler
	public static class ClickedLazyCollectionEventHandler implements DomainEventHandler<Callable<Clicked[]>> {
		public static int COUNT;
		private final Connection connection;

		public ClickedLazyCollectionEventHandler(Connection connection) {
			this.connection = connection;
		}

		public void handle(Callable<Clicked[]> events) {
			COUNT++;
		}
	}

	@Test
	public void eventTest() throws IOException {
		ServiceLocator locator = container;
		DomainEventStore<Clicked> store = locator.resolve(ClickedRepository.class);
		Clicked cl = new Clicked().setBigint(Long.MAX_VALUE).setDate(LocalDate.now()).setNumber(BigDecimal.valueOf(11.22));
		ClickedEventHandler.COUNT = ClickedCollectionEventHandler.COUNT = ClickedLazyEventHandler.COUNT = ClickedLazyCollectionEventHandler.COUNT = 0;
		String uri = store.submit(cl);
		Assert.assertEquals(1, ClickedEventHandler.COUNT);
		Assert.assertEquals(1, ClickedCollectionEventHandler.COUNT);
		Assert.assertEquals(1, ClickedLazyEventHandler.COUNT);
		Assert.assertEquals(1, ClickedLazyCollectionEventHandler.COUNT);
		Optional<Clicked> cl2o = store.find(uri);
		Assert.assertTrue(cl2o.isPresent());
		Clicked cl2 = cl2o.get();
		Assert.assertEquals(uri, cl2.getURI());
		Assert.assertEquals(cl.getBigint(), cl2.getBigint());
		Assert.assertEquals(cl.getDate(), cl2.getDate());
		Assert.assertEquals(cl.getNumber(), cl2.getNumber());
	}

	@Test
	public void sequenceTest() throws IOException {
		ServiceLocator locator = container;
		PersistableRepository<Next> repository = new Generic<PersistableRepository<Next>>() {
		}.resolve(locator);
		Next next = new Next();
		int id = next.getID();
		String uri = repository.insert(next);
		Assert.assertNotEquals(id, next.getID());
		Optional<Next> found = repository.find(uri);
		Assert.assertTrue(found.isPresent());
		Assert.assertEquals(next.getID(), found.get().getID());
	}

	@Test
	public void simpleRootSearch() throws IOException {
		ServiceLocator locator = container;
		PersistableRepository<Next> repository = new Generic<PersistableRepository<Next>>() {
		}.resolve(locator);
		Next next = new Next();
		repository.insert(next);
		List<Next> found = repository.search(1);
		Assert.assertEquals(1, found.size());
	}

	@Test
	public void simpleEventSearch() throws IOException {
		ServiceLocator locator = container;
		DomainEventStore<Clicked> store = locator.resolve(ClickedRepository.class);
		Clicked cl1 = new Clicked().setBigint(Long.MIN_VALUE).setDate(LocalDate.now()).setNumber(BigDecimal.valueOf(11.22));
		Clicked cl2 = new Clicked().setBigint(Long.MAX_VALUE).setDate(LocalDate.now()).setNumber(BigDecimal.valueOf(11.22));
		String[] uris = store.submit(Arrays.asList(cl1, cl2));
		Assert.assertEquals(2, uris.length);
		List<Clicked> found = store.search(2);
		Assert.assertEquals(2, found.size());
	}

	@Test
	public void specificationRootSearch() throws IOException {
		ServiceLocator locator = container;
		PersistableRepository<Next> repository = new Generic<PersistableRepository<Next>>() {
		}.resolve(locator);
		String uri = repository.insert(new Next());
		int id = Integer.parseInt(uri);
		List<Next> found = repository.search(new Next.BetweenIds(id, id));
		Assert.assertEquals(1, found.size());
	}

	@Test
	public void specificationEventSearch() throws IOException {
		ServiceLocator locator = container;
		DomainEventStore<Clicked> store = locator.resolve(ClickedRepository.class);
		Random rnd = new Random();
		Long rndLong = rnd.nextLong();
		BigDecimal rndDecimal = BigDecimal.valueOf(rnd.nextDouble());
		Clicked cl = new Clicked().setBigint(rndLong).setDate(LocalDate.now()).setNumber(rndDecimal).setEn(En.B);
		String[] uris = store.submit(Collections.singletonList(cl));
		Assert.assertEquals(1, uris.length);
		List<Clicked> found = store.search(new Clicked.BetweenNumbers(rndDecimal, Collections.singleton(rndDecimal), En.B));
		Assert.assertEquals(1, found.size());
	}

	@Test
	public void specificationEventExists() throws IOException {
		ServiceLocator locator = container;
		DomainEventStore<Clicked> store = locator.resolve(ClickedRepository.class);
		Random rnd = new Random();
		Long rndLong = rnd.nextLong();
		BigDecimal rndDecimal = BigDecimal.valueOf(rnd.nextDouble());
		Clicked cl = new Clicked().setBigint(rndLong).setDate(LocalDate.now()).setNumber(rndDecimal).setEn(En.B);
		String[] uris = store.submit(Collections.singletonList(cl));
		Assert.assertEquals(1, uris.length);
		boolean found = store.exists(new Clicked.BetweenNumbers(rndDecimal, Collections.singleton(rndDecimal), En.B));
		Assert.assertTrue(found);
	}

	@Test
	public void specificationSnowflakeSearch() throws IOException {
		ServiceLocator locator = container;
		CompositeRepository repository = locator.resolve(CompositeRepository.class);
		CompositeListRepository listRepository = locator.resolve(CompositeListRepository.class);
		Random rnd = new Random();
		Simple simple = new Simple().setNumber(rnd.nextInt(100000) + 2000);
		repository.insert(new Composite().setSimple(simple));
		List<CompositeList> found = listRepository.search(new CompositeList.ForSimple(Arrays.asList(simple)));
		Assert.assertEquals(1, found.size());
	}

	@Test
	public void specificationSnowflakeCount() throws IOException {
		ServiceLocator locator = container;
		CompositeRepository repository = locator.resolve(CompositeRepository.class);
		CompositeListRepository listRepository = locator.resolve(CompositeListRepository.class);
		Random rnd = new Random();
		Simple simple = new Simple().setNumber(rnd.nextInt(100000) + 2000);
		repository.insert(new Composite().setSimple(simple));
		long total = listRepository.count(new CompositeList.ForSimple(Arrays.asList(simple)));
		Assert.assertEquals(1L, total);
	}

	@Test
	public void specificationSnowflakeFind() throws IOException {
		ServiceLocator locator = container;
		CompositeRepository repository = locator.resolve(CompositeRepository.class);
		CompositeListRepository listRepository = locator.resolve(CompositeListRepository.class);
		String uri = repository.insert(new Composite().setSimple(new Simple().setNumber(1234)));
		CompositeList list = listRepository.find(uri).get();
		Assert.assertEquals(1234, list.getSimple().getNumber());
	}

	@Test
	public void lazyLoadTest() throws IOException {
		ServiceLocator locator = container;
		CompositeRepository compRepository = locator.resolve(CompositeRepository.class);
		String uri = compRepository.insert(new Composite().setSimple(new Simple().setNumber(5432)));
		Composite c = compRepository.find(uri).get();
		Assert.assertEquals(5432, c.getSimple().getNumber());
		LazyLoadRepository llRepository = locator.resolve(LazyLoadRepository.class);
		uri = llRepository.insert(new LazyLoad().setComp(c));
		LazyLoad ll = llRepository.find(uri).get();
		Assert.assertEquals(c.getURI(), ll.getCompURI());
		Assert.assertEquals(5432, ll.getComp().getSimple().getNumber());
	}

	@Test
	public void detailTest() throws IOException {
		ServiceLocator locator = container;
		SingleDetailRepository sdRepository = locator.resolve(SingleDetailRepository.class);
		String uri = sdRepository.insert(new SingleDetail());
		SingleDetail sd = sdRepository.find(uri).get();
		Assert.assertEquals(0, sd.getDetails().length);
		Assert.assertEquals(0, sd.getDetailsURI().length);
		LazyLoadRepository llRepository = locator.resolve(LazyLoadRepository.class);
		uri = llRepository.insert(new LazyLoad().setSd(sd));
		SingleDetail sd2 = sdRepository.find(sd.getURI()).get();
		Assert.assertEquals(1, sd2.getDetails().length);
		Assert.assertEquals(1, sd2.getDetailsURI().length);
		Assert.assertEquals(uri, sd2.getDetailsURI()[0]);
		Assert.assertEquals(uri, sd2.getDetails()[0].getURI());
	}

	@Test
	public void collectionReference() throws IOException {
		ServiceLocator locator = container;
		PersistableRepository<Composite> repository = locator.resolve(CompositeRepository.class);
		Composite co = new Composite();
		UUID id = UUID.randomUUID();
		co.setId(id);
		co.getEntities().add(new Entity());
		co.getEntities().add(new Entity());
		co.getEntities().add(new Entity());
		String uri = repository.insert(co);
		Assert.assertEquals(co.getURI(), uri);
		Optional<Composite> found = repository.find(uri);
		Assert.assertTrue(found.isPresent());
		Composite co2 = found.get();
		Assert.assertEquals(co.getURI(), co2.getURI());
		Assert.assertEquals(3, co2.getEntities().size());
	}

	@Test
	public void nestedCollectionReference() throws IOException {
		ServiceLocator locator = container;
		PersistableRepository<Composite> repository = locator.resolve(CompositeRepository.class);
		Composite co = new Composite();
		UUID id = UUID.randomUUID();
		co.setId(id);
		Entity e1 = new Entity();
		e1.getDetail1().add(new Detail1());
		e1.getDetail1().add(new Detail1());
		co.getEntities().add(e1);
		Entity e2 = new Entity();
		e2.getDetail1().add(new Detail1());
		e2.getDetail1().add(new Detail1());
		e2.getDetail1().add(new Detail1());
		co.getEntities().add(e2);
		String uri = repository.insert(co);
		Assert.assertEquals(co.getURI(), uri);
		Optional<Composite> found = repository.find(uri);
		Assert.assertTrue(found.isPresent());
		Composite co2 = found.get();
		Assert.assertEquals(co.getURI(), co2.getURI());
		Assert.assertEquals(2, co2.getEntities().size());
		Assert.assertEquals(2, co2.getEntities().get(0).getDetail1().size());
		Assert.assertEquals(3, co2.getEntities().get(1).getDetail1().size());
	}

	@Test
	public void persistBinaries() throws Exception {
		ServiceLocator locator = container;
		PersistableRepository<Document> repository = locator.resolve(PersistableRepository.class, Document.class);
		byte[] bytes = new byte[256];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) i;
		}
		String uri = repository.insert(new Document().setContent(bytes).setName("file.bin"));
		Optional<Document> found = repository.find(uri);
		Assert.assertTrue(found.isPresent());
		Assert.assertArrayEquals(bytes, found.get().getContent());
	}

	@Test
	public void listIntPks() throws Exception {
		ServiceLocator locator = container;
		PersistableRepository<pks> repository = locator.resolve(PersistableRepository.class, pks.class);
		Random rnd = new Random();
		pks pks = new pks()
				.setId(Arrays.asList(0, rnd.nextInt(), Integer.MIN_VALUE, Integer.MAX_VALUE, -105102223, -1000000000, -1000000000, 1000000000, 100000000, Integer.MIN_VALUE + 1))
				.setL(new Point2D.Double(4, 55.5))
				.setPp(new Point[]{new Point(3, 4), new Point(55, 66)});
		String uri = repository.insert(pks);
		Optional<pks> found = repository.find(uri);
		repository.delete(found.get());
		Assert.assertTrue(found.isPresent());
		Assert.assertEquals(uri, found.get().getURI());
		Assert.assertTrue(pks.deepEquals(found.get()));
		Assert.assertTrue(uri.contains("-1000000000"));
	}

	@Test
	public void canWorkWithXml() throws Exception {
		ServiceLocator locator = container;
		PersistableRepository<pks> repository = locator.resolve(PersistableRepository.class, pks.class);
		Random rnd = new Random();
		pks pks = new pks().setId(Arrays.asList(rnd.nextInt(), rnd.nextInt(), rnd.nextInt()));
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbf.newDocumentBuilder();
		org.w3c.dom.Document doc = builder.newDocument();
		Element el = doc.createElement("test");
		el.setTextContent("me now");
		pks.setXml(el);
		String uri = repository.insert(pks);
		Optional<pks> found = repository.find(uri);
		Assert.assertTrue(found.isPresent());
		Assert.assertEquals(pks, found.get());
		Assert.assertEquals("test", found.get().getXml().getTagName());
		Assert.assertEquals("me now", found.get().getXml().getTextContent());
	}

	@Test
	public void complexCompositePk() throws Exception {
		ServiceLocator locator = container;
		PersistableRepository<PksV> repository = locator.resolve(PersistableRepository.class, PksV.class);
		Random rnd = new Random();
		Map map = new HashMap<>();
		map.put("abc", "123");
		PksV pks = new PksV()
				.setE(E.B).setV(new v().setX(rnd.nextInt()))
				.setVv(new v[]{new v(rnd.nextInt(), new Map[] { map }), new v().setX(55)})
				.setP(new Point(1, 2))
				.setLl(Arrays.asList(new Point2D.Double(6.6, 8.8), new Point2D.Double(-5.5, -4.3)));
		pks.getEe().add(E.C);
		pks.getEe().add(E.B);
		String uri = repository.insert(pks);
		Optional<PksV> found = repository.find(uri);
		repository.delete(found.get());
		Assert.assertTrue(found.isPresent());
		Assert.assertEquals(uri, found.get().getURI());
		Assert.assertTrue(pks.deepEquals(found.get()));
	}

	@Test
	public void entityReferenceWithoutAssignment() throws IOException {
		ServiceLocator locator = container;
		PersistableRepository<Author> repository = locator.resolve(AuthorRepository.class);
		String uri = repository.insert(new Author());
		Optional<Author> found = repository.find(uri);
		Assert.assertTrue(found.isPresent());
		Author au2 = found.get();
		Assert.assertEquals(uri, au2.getURI());
	}

	@Test
	public void testMasterDetailEntityReferenceUpdate() throws IOException {
		ServiceLocator locator = container;
		PersistableRepository<Master> repository = locator.resolve(MasterRepository.class);
		String uri = repository.insert(new Master(new Detail[]{new Detail(), new Detail()}));
		Optional<Master> found = repository.find(uri);
		Assert.assertTrue(found.isPresent());
		Assert.assertEquals(2, found.get().getDetails().length);
	}

	@Test
	public void canReadWriteInterfaceProperty() throws IOException {
		ServiceLocator locator = container;
		PersistableRepository<User> repository = locator.resolve(UserRepository.class);
		Random rnd = new Random();
		User user = new User().setUsername("user" + Long.toString(rnd.nextLong()))
				.setAuthentication(new BasicSecurity().setUsername("username").setPassword("password"));
		String uri = repository.insert(user);
		Optional<User> found = repository.find(uri);
		Assert.assertTrue(found.isPresent());
		BasicSecurity bs = (BasicSecurity) found.get().getAuthentication();
		Assert.assertEquals("username", bs.getUsername());
		Assert.assertEquals("password", bs.getPassword());
	}

	@Test
	public void datesBefore1884() throws IOException {
		ServiceLocator locator = container;
		PersistableRepository<TimestampPk> repository = locator.resolve(TimestampPkRepository.class);
		OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
		TimestampPk after = new TimestampPk(now, BigDecimal.valueOf(2.22));
		TimestampPk before = new TimestampPk(now.minusYears(now.getYear() - 100), BigDecimal.valueOf(3.333));
		String[] uris = repository.insert(Arrays.asList(before, after));
		Assert.assertTrue(uris[0].endsWith("+01:22"));
		List<TimestampPk> found = repository.find(uris);
		found.sort((a, b) -> a.getTs().compareTo(b.getTs()));
		Assert.assertEquals(2, found.size());
		//TODO: timezone settings
		//Assert.assertTrue(before.deepEquals(found.get(0)));
		//Assert.assertTrue(after.deepEquals(found.get(1)));
	}

	@Test
	public void canReadWriteXml() throws Exception {
		ServiceLocator locator = container;
		PersistableRepository<gen.model.calc.Type> repository = locator.resolve(PersistableRepository.class, gen.model.calc.Type.class);
		Random rnd = new Random();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		org.w3c.dom.Document doc = dBuilder.newDocument();
		org.w3c.dom.Element rootElement = doc.createElement("test");
		doc.appendChild(rootElement);
		rootElement.setAttribute("a", "b");
		gen.model.calc.Type t = new gen.model.calc.Type()
				.setDescription(rnd.nextLong() + "")
				.setSuffix(rnd.nextLong() + "")
				.setXml(rootElement);
		String uri = repository.insert(t);
		Optional<gen.model.calc.Type> found = repository.find(uri);
		Assert.assertTrue(t.deepEquals(found.get()));
		Assert.assertEquals("test", found.get().getXml().getNodeName());
		Assert.assertEquals("b", found.get().getXml().getAttribute("a"));
	}

	@Test
	public void bulkReadingSql() throws Exception {
		ServiceLocator locator = container;
		DataContext context = locator.resolve(DataContext.class);
		Random rnd = new Random();
		String name = "bulk " + rnd.nextInt();
		Document document = new Document().setName(name);
		context.create(document);
		context.create(new Composite());
		UUID id = document.getID();
		RepositoryBulkReader bulkReader = locator.resolve(RepositoryBulkReader.class);
		Callable<Optional<WritableDocument>> wd1 = bulkReader.find(WritableDocument.class, document.getURI());
		Callable<List<WritableDocument>> wd2 = bulkReader.find(WritableDocument.class, new String[]{document.getURI()});
		Callable<List<WritableDocument>> wd3 = bulkReader.search(WritableDocument.class, it -> it.getId() == id);
		Callable<Boolean> wd4 = bulkReader.exists(WritableDocument.class, it -> it.getId() == id);
		Callable<Long> wd5 = bulkReader.count(WritableDocument.class, it -> it.getId() == id);
		Callable<List<Map<String, Object>>> wd6 =
				bulkReader.analyze(
						CompositeCube.class,
						Arrays.asList(CompositeCube.max, CompositeCube.min),
						Collections.EMPTY_LIST,
						it -> true,
						null,
						null);
		bulkReader.execute();
		Assert.assertTrue(wd1.call().isPresent());
		Assert.assertEquals(1, wd2.call().size());
		Assert.assertEquals(1, wd3.call().size());
		Assert.assertTrue(wd4.call());
		Assert.assertEquals(1L, wd5.call().longValue());
		Assert.assertEquals(name, wd1.call().get().getName());
		Assert.assertEquals(name, wd2.call().get(0).getName());
		Assert.assertEquals(name, wd3.call().get(0).getName());
		Assert.assertEquals(1, wd6.call().size());
	}

	@Test
	public void canReadHistory() throws Exception {
		ServiceLocator locator = container;
		PersistableRepository<TimestampPk> repository = locator.resolve(PersistableRepository.class, TimestampPk.class);
		Repository<History<TimestampPk>> history = locator.resolve(Repository.class, Utils.makeGenericType(History.class, TimestampPk.class));
		OffsetDateTime odt = OffsetDateTime.now();
		TimestampPk pk = new TimestampPk().setTs(odt).setD(BigDecimal.ONE);
		String uri = repository.insert(pk);
		Optional<TimestampPk> found = repository.find(uri);
		Optional<History<TimestampPk>> foundHistory = history.find(uri);
		Assert.assertTrue(found.isPresent());
		Assert.assertTrue(foundHistory.isPresent());
		Assert.assertEquals(1, foundHistory.get().getSnapshots().size());
		Assert.assertEquals(uri, foundHistory.get().getURI());
		//Assert.assertEquals(found.get(), foundHistory.get().getSnapshots().get(0).getValue());
		Assert.assertTrue(found.get().getTs().isEqual(foundHistory.get().getSnapshots().get(0).getValue().getTs()));
	}

	@Test
	public void shallowReferenceUuidPkEquality() throws IOException {
		ServiceLocator locator = container;
		DocumentConverter converter = locator.resolve(DocumentConverter.class);
		gen.model.binaries.Document instance = converter.shallowReference("7739d32c-54af-4a71-8412-0f96981d4677", locator);
		Assert.assertEquals("7739d32c-54af-4a71-8412-0f96981d4677", instance.getURI());
		Assert.assertEquals(UUID.fromString("7739d32c-54af-4a71-8412-0f96981d4677"), instance.getID());
	}

	@Test
	public void shallowReferenceStringPkEquality() throws IOException {
		ServiceLocator locator = container;
		InfoConverter converter = locator.resolve(InfoConverter.class);
		gen.model.calc.Info instance = converter.shallowReference(",\\", locator);
		Assert.assertEquals(",\\", instance.getURI());
		Assert.assertEquals(",\\", instance.getCode());
		instance = converter.shallowReference("", locator);
		Assert.assertEquals("", instance.getURI());
		Assert.assertEquals("", instance.getCode());
		instance = converter.shallowReference("abc", locator);
		Assert.assertEquals("abc", instance.getURI());
		Assert.assertEquals("abc", instance.getCode());
		instance = converter.shallowReference("\"", locator);
		Assert.assertEquals("\"", instance.getURI());
		Assert.assertEquals("\"", instance.getCode());
	}

	@Test
	public void shallowReferenceCompositePkEquality() throws IOException {
		ServiceLocator locator = container;
		AnalysisConverter converter = locator.resolve(AnalysisConverter.class);
		gen.model.stock.Analysis instance = converter.shallowReference("123/456", locator);
		Assert.assertEquals("123/456", instance.getURI());
		Assert.assertEquals(123, instance.getProjectID());
		Assert.assertEquals(456L, instance.getArticleID());
	}

	@Test
	public void checkTreePath() throws IOException {
		ServiceLocator locator = container;
		PersistableRepository<Tree> repository = locator.resolve(TreeRepository.class);
		Tree t = new Tree();
		t.setT1(TreePath.create("a.b.c"));
		t.setT2(TreePath.create("aa.bb"));
		t.setTt1(Arrays.asList(TreePath.create("yy"), null, TreePath.create("def")));
		t.setTt2(new TreePath[] { TreePath.create("xxx"), TreePath.create("abc.def")});
		String uri = repository.insert(t);
		Assert.assertEquals("a.b.c", uri);
		Optional<Tree> found = repository.find(uri);
		Assert.assertTrue(found.isPresent());
		repository.delete(t);
		Tree t2 = found.get();
		Assert.assertEquals(t.getURI(), t2.getURI());
		Assert.assertTrue(t.deepEquals(t2));
	}

	@Test
	public void checkSnowflakeReference() throws IOException {
		ServiceLocator locator = container;
		PersistableRepository<Composite> aggregates = locator.resolve(CompositeRepository.class);
		Repository<CompositeList> snowflakes = locator.resolve(CompositeListRepository.class);
		Composite co = new Composite();
		UUID id = UUID.randomUUID();
		co.setId(id);
		String uri = aggregates.insert(co);
		Optional<CompositeList> fs = snowflakes.find(uri);
		Assert.assertTrue(fs.isPresent());
		CompositeList snow = fs.get();
		co.setSelfReference(snow);
		co.getSelfReferences().add(snow);
		aggregates.update(co);
		Optional<Composite> found = aggregates.find(uri);
		Assert.assertTrue(found.isPresent());
		Composite co2 = found.get();
		Assert.assertTrue(co.deepEquals(co2));
		Assert.assertEquals(snow.getURI(), co2.getSelfReference().getURI());
		Assert.assertEquals(1, co2.getSelfReferencesURI().length);
		Assert.assertEquals(1, co2.getSelfReferences().size());
		Assert.assertEquals(snow.getURI(), co2.getSelfReferences().get(0).getURI());
	}

	@Test
	public void checkTreePathOperators() throws IOException {
		ServiceLocator locator = container;
		PersistableRepository<Tree> repository = locator.resolve(TreeRepository.class);
		long now = (new Date()).getTime();
		Tree t1 = new Tree();
		t1.setT1(TreePath.create("a.b" + now + ".c"));
		Tree t2 = new Tree();
		t2.setT1(TreePath.create("a.b.c" + now));
		repository.insert(Arrays.asList(t1, t2));
		List<Tree> found = repository.query().filter(it -> it.getT1().isDescendant(TreePath.create("a.b" + now))).list();
		Assert.assertEquals(1, found.size());
		Assert.assertTrue(t1.deepEquals(found.get(0)));
		found = repository.query().filter(it -> it.getT1().isAncestor(TreePath.create("a.b.c" + now+".d"))).list();
		Assert.assertEquals(1, found.size());
		Assert.assertTrue(t2.deepEquals(found.get(0)));
	}

	@Test
	public void mixinWithEmptyValue() throws IOException {
		ServiceLocator locator = container;
		PersistableRepository<Foo> repository = locator.resolve(FooRepository.class);
		Foo original = new Foo().setBars(Collections.singletonList(new Bar().setMixin(new ImplVoid())));
		String uri = repository.insert(original);
		Optional<Foo> found = repository.find(uri);
		Assert.assertTrue(found.isPresent());
		Assert.assertTrue(found.get().deepEquals(original));
		Assert.assertTrue(found.get().getBars().get(0).getMixin() instanceof ImplVoid);
	}

	@Test
	public void canSaveJsonB() throws IOException {
		ServiceLocator locator = container;
		PersistableRepository<gen.model.security.Document> repository = locator.resolve(gen.model.security.repositories.DocumentRepository.class);
		gen.model.security.Document value = new gen.model.security.Document();
		value.getData().put("abc", 123L);
		String uri = repository.insert(value);
		Optional<gen.model.security.Document> found = repository.find(uri);
		Assert.assertTrue(found.isPresent());
		Assert.assertTrue(found.get().deepEquals(value));
		Assert.assertEquals(123L, found.get().getData().get("abc"));
	}
}
