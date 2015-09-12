package org.revenj;

import gen.model.Boot;
import gen.model.test.Composite;
import org.junit.Assert;
import org.junit.Test;
import org.revenj.patterns.DataChangeNotification;
import org.revenj.patterns.DataContext;
import org.revenj.patterns.Generic;
import org.revenj.patterns.ServiceLocator;
import rx.Observable;

import java.io.Closeable;

public class TestNotifications {

	@Test
	public void willRaiseNotification() throws Exception {
		ServiceLocator locator = Boot.configure("jdbc:postgresql://localhost:5432/revenj");
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
	public void canTrack() throws Exception {
		ServiceLocator locator = Boot.configure("jdbc:postgresql://localhost:5432/revenj");
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
		ServiceLocator locator = Boot.configure("jdbc:postgresql://localhost:5432/revenj");
		DataContext context = locator.resolve(DataContext.class);
		Observable<Composite> notification = new Generic<Observable<Composite>>() {
		}.resolve(locator);
		boolean[] hasRead = new boolean[1];
		String[] uris = new String[1];
		Composite co = new Composite();
		notification.subscribe(n -> {
			hasRead[0] = true;
			uris[0] = n.getURI();
			try {
				Assert.assertEquals(co, n);
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
		((AutoCloseable) locator).close();
	}
}
