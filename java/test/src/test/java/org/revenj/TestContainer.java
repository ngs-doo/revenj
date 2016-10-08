package org.revenj;

import org.junit.Assert;
import org.junit.Test;
import org.revenj.extensibility.Container;

import javax.inject.Inject;
import javax.inject.Singleton;

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

	public static class GenArg {
		public Generics<A1> g;
		@Inject
		public GenArg(Generics<A1> g) { this.g = g; }
	}

	@Test
	public void willRespectInjectAnnotation() throws Exception {
		Container container = Revenj.container(false, TestContainer.class.getClassLoader());
		WithInject wi = container.resolve(WithInject.class);
		Assert.assertEquals(42, wi.value);
		A1 a = container.resolve(A1.class);
		Assert.assertEquals(wi.a, a);
	}

	@Test
	public void canResolveGenerics() throws Exception {
		Container container = Revenj.container(false, TestContainer.class.getClassLoader());
		Generics<A1> wi = container.resolve(Generics.class, A1.class);
		Assert.assertTrue(wi.t instanceof A1);
	}

	@Test
	public void canResolveGenericArg() throws Exception {
		Container container = Revenj.container(false, TestContainer.class.getClassLoader());
		GenArg wi = container.resolve(GenArg.class);
		Assert.assertTrue(wi.g.t instanceof A1);
	}
}
