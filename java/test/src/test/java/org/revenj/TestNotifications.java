package org.revenj;

import gen.model.test.Composite;
import org.junit.Assert;
import org.junit.Test;
import org.revenj.extensibility.SystemState;
import org.revenj.patterns.DataChangeNotification;
import org.revenj.patterns.DataContext;
import org.revenj.patterns.Generic;
import org.revenj.patterns.ServiceLocator;
import rx.Observable;

import javax.sql.DataSource;
import java.io.Closeable;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.Callable;

public class TestNotifications extends Setup {

	@Test
	public void willRaiseNotification() throws Exception {
		ServiceLocator locator = container;
		DataContext context = locator.resolve(DataContext.class);
		DataChangeNotification notification = locator.resolve(DataChangeNotification.class);
		boolean[] hasRead = new boolean[1];
		String[] uris = new String[1];
		notification.getNotifications().subscribe(n -> {
			hasRead[0] = true;
			uris[0] = n.uris[0];
		});
		Assert.assertFalse(hasRead[0]);
		Composite co = new Composite();
		context.create(co);
		for (int i = 0; i < 30; i++) {
			Thread.sleep(100);
			if (hasRead[0]) break;
		}
		Assert.assertTrue(hasRead[0]);
		Assert.assertEquals(co.getURI(), uris[0]);
		((Closeable) notification).close();
	}

	@Test
	public void willRaiseMultipleNotifications() throws Exception {
		ServiceLocator locator = container;
		DataContext context = locator.resolve(DataContext.class);
		DataChangeNotification notification = locator.resolve(DataChangeNotification.class);
		int[] hasRead = new int[1];
		String[] uris = new String[1];
		notification.getNotifications().subscribe(n -> {
			hasRead[0] = hasRead[0] + 1;
			uris[0] = n.uris[0];
		});
		Assert.assertEquals(0, hasRead[0]);
		Composite co = new Composite();
		context.create(co);
		for (int i = 0; i < 30; i++) {
			Thread.sleep(100);
			if (hasRead[0] > 0) break;
		}
		Assert.assertEquals(1, hasRead[0]);
		Assert.assertEquals(co.getURI(), uris[0]);
		co = new Composite();
		context.create(co);
		for (int i = 0; i < 30; i++) {
			Thread.sleep(100);
			if (hasRead[0] > 1) break;
		}
		Assert.assertEquals(2, hasRead[0]);
		Assert.assertEquals(co.getURI(), uris[0]);
		((Closeable) notification).close();
	}

	@Test
	public void canTrack() throws Exception {
		ServiceLocator locator = container;
		DataContext context = locator.resolve(DataContext.class);
		DataChangeNotification notification = locator.resolve(DataChangeNotification.class);
		boolean[] hasRead = new boolean[1];
		String[] uris = new String[1];
		Composite co = new Composite();
		notification.track(Composite.class).subscribe(n -> {
			hasRead[0] = true;
			uris[0] = n.uris[0];
			try {
				Assert.assertEquals(co, n.result.call().get(0));
			} catch (Exception e) {
				Assert.fail(e.getMessage());
			}
		});
		Assert.assertFalse(hasRead[0]);
		context.create(co);
		for (int i = 0; i < 30; i++) {
			Thread.sleep(100);
			if (hasRead[0]) break;
		}
		Assert.assertTrue(hasRead[0]);
		Assert.assertEquals(co.getURI(), uris[0]);
		((Closeable) notification).close();
	}

	@Test
	public void observableSignatures() throws Exception {
		ServiceLocator locator = container;
		DataContext context = locator.resolve(DataContext.class);
		Observable<Callable<List<Composite>>> notification0 = new Generic<Observable<Callable<List<Composite>>>>() {
		}.resolve(locator);
		Observable<Composite> notification1 = new Generic<Observable<Composite>>() {
		}.resolve(locator);
		Observable<Callable<Composite>> notification2 = new Generic<Observable<Callable<Composite>>>() {
		}.resolve(locator);
		boolean[] hasRead = new boolean[3];
		String[] uris = new String[3];
		Composite co = new Composite();
		notification0.subscribe(n -> {
			try {
				uris[0] = n.call().get(0).getURI();
				Assert.assertEquals(co, n.call().get(0));
			} catch (Exception e) {
				Assert.fail(e.getMessage());
			}
			hasRead[0] = true;
		});
		notification1.subscribe(n -> {
			uris[1] = n.getURI();
			try {
				Assert.assertEquals(co, n);
			} catch (Exception e) {
				Assert.fail(e.getMessage());
			}
			hasRead[1] = true;
		});
		notification2.subscribe(n -> {
			try {
				uris[2] = n.call().getURI();
				Assert.assertEquals(co, n.call());
			} catch (Exception e) {
				Assert.fail(e.getMessage());
			}
			hasRead[2] = true;
		});
		Assert.assertFalse(hasRead[0]);
		Assert.assertFalse(hasRead[1]);
		Assert.assertFalse(hasRead[2]);
		context.create(co);
		for (int i = 0; i < 30; i++) {
			if (hasRead[0] && hasRead[1] && hasRead[2]) break;
			Thread.sleep(100);
		}
		Assert.assertTrue(hasRead[0]);
		Assert.assertTrue(hasRead[1]);
		Assert.assertTrue(hasRead[2]);
		Assert.assertEquals(co.getURI(), uris[0]);
		Assert.assertEquals(co.getURI(), uris[1]);
		Assert.assertEquals(co.getURI(), uris[2]);
		((AutoCloseable) locator).close();
	}

	@Test
	public void canDetectMigration() throws Exception {
		ServiceLocator locator = container;
		locator.resolve(DataChangeNotification.class);
		SystemState state = locator.resolve(SystemState.class);
		boolean[] changes = new boolean[1];
		state.change().subscribe(it -> changes[0] = true);
		Assert.assertFalse(changes[0]);
		Connection sql = locator.resolve(DataSource.class).getConnection();
		sql.createStatement().execute("SELECT pg_notify('migration', 'new')");
		sql.close();
		for (int i = 0; i < 20; i++) {
			if (changes[0]) break;
			Thread.sleep(100);
		}
		Assert.assertTrue(changes[0]);
		((AutoCloseable) locator).close();
	}
}
