package org.revenj;

import gen.model.Boot;
import gen.model.Seq.Next;
import gen.model.adt.BasicSecurity;
import gen.model.adt.User;
import gen.model.adt.repositories.UserRepository;
import gen.model.binaries.Document;
import gen.model.egzotics.E;
import gen.model.egzotics.PksV;
import gen.model.egzotics.pks;
import gen.model.egzotics.v;
import gen.model.md.Detail;
import gen.model.md.Master;
import gen.model.md.repositories.MasterRepository;
import gen.model.mixinReference.Author;
import gen.model.mixinReference.repositories.AuthorRepository;
import gen.model.test.*;
import gen.model.test.repositories.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.revenj.extensibility.Container;
import org.revenj.patterns.*;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Callable;

public class TestRepository {

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
	public static class ClickedCollectionEventHandler implements DomainEventHandler<Clicked[]> {
		public static int COUNT;

		public void handle(Clicked[] events) {
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
		pks pks = new pks().setId(Arrays.asList(0, Integer.MIN_VALUE, Integer.MAX_VALUE, -105102223, -1000000000, -1000000000, 1000000000, 100000000, Integer.MIN_VALUE + 1));
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
		PksV pks = new PksV().setE(E.B).setV(new v().setX(rnd.nextInt())).setVv(new v[]{new v(rnd.nextInt()), new v().setX(55)});
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
}
