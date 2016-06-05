package org.revenj;

import org.junit.Assert;
import org.junit.Test;
import org.revenj.extensibility.Container;
import org.revenj.extensibility.SystemAspect;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ServiceLoader;

public class TestContainer {

	@Singleton
	public static class A1 {
	}
	static class B1 {
	}

	public static class WithInject {
		public final int value;
		public A1 a;
		public WithInject() {
			this.value = 5;
		}
		@Inject
		public WithInject(A1 a) {
			value = 42;
			this.a = a;
		}
		public WithInject(A1 a, B1 d) {
			value = 6;
		}
	}

	public static class Generics<T> {
		public final T t;
		@Inject
		public Generics(T t) {
			this.t = t;
		}
	}

	@Test
	public void willRespectInjectAnnotation() throws Exception {
		Container container = new SimpleContainer(false);
		ServiceLoader<SystemAspect> aspects = ServiceLoader.load(SystemAspect.class);
		for(SystemAspect a : aspects) {
			a.configure(container);
		}
		WithInject wi = container.resolve(WithInject.class);
		Assert.assertEquals(42, wi.value);
		A1 a = container.resolve(A1.class);
		Assert.assertEquals(wi.a, a);
	}

	@Test
	public void canResolveGenerics() throws Exception {
		Container container = new SimpleContainer(false);
		ServiceLoader<SystemAspect> aspects = ServiceLoader.load(SystemAspect.class);
		for(SystemAspect a : aspects) {
			a.configure(container);
		}
		Generics<A1> wi = container.resolve(Generics.class, A1.class);
		Assert.assertTrue(wi.t instanceof A1);
	}
}
