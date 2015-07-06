package org.revenj;

import org.junit.Assert;
import org.junit.Test;
import org.revenj.extensibility.Container;
import org.revenj.patterns.Generic;
import org.revenj.patterns.ServiceLocator;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class TestContainer {

	@Test
	public void initialSetup() throws IOException, SQLException {
		ServiceLocator locator = Revenj.setup("jdbc:postgresql://localhost:5432/revenj");
		try (Connection connection = locator.resolve(Connection.class)) {
			Assert.assertNotNull(connection);
		}
	}

	static class A {
		public final B b;

		public A(B b) {
			this.b = b;
		}
	}

	static class B {
		public static int counter;

		public B() {
			counter++;
		}
	}

	@Test
		 public void typeTest() throws IOException {
		B.counter = 0;
		Container container = new SimpleContainer(false);
		container.register(A.class);
		Optional<A> aopt = container.tryResolve(A.class);
		Assert.assertFalse(aopt.isPresent());
		container.register(B.class);
		A a = container.resolve(A.class);
		Assert.assertNotNull(a);
		Assert.assertEquals(1, B.counter);
	}

	static class C {
		public final A a;
		public final Optional<D> d;

		public C(A a, Optional<D> d) {
			this.a = a;
			this.d = d;
		}
	}

	static class D {
	}


	@Test
	public void optionalTest() throws IOException {
		B.counter = 0;
		Container container = new SimpleContainer(false);
		container.register(A.class, B.class, C.class);
		C c = container.resolve(C.class);
		Assert.assertNotNull(c);
		Assert.assertEquals(1, B.counter);
		container.register(D.class);
		c = container.resolve(C.class);
		Assert.assertNotNull(c);
		Assert.assertEquals(2, B.counter);
	}

	static class G<T> {
		public final T instance;

		public G(T instance) {
			this.instance = instance;
		}
	}

	@Test
	public void genericsTest() throws IOException {
		B.counter = 0;
		Container container = new SimpleContainer(false);
		container.register(A.class, B.class, G.class);
		G<A> g = new Generic<G<A>>() {
		}.resolve(container);
		Assert.assertNotNull(g);
		Assert.assertEquals(1, B.counter);
		Assert.assertTrue(g.instance instanceof A);
		Assert.assertEquals(A.class, g.instance.getClass());
	}

	static class ComplexGenerics<T1, T2> {
		public final T1 instance1;
		public final T2 instance2;

		public ComplexGenerics(T2 instance2, T1 instance1) {
			this.instance1 = instance1;
			this.instance2 = instance2;
		}
	}

	@Test
	public void complexGenericsTest() throws IOException {
		B.counter = 0;
		Container container = new SimpleContainer(false);
		container.register(A.class, B.class, ComplexGenerics.class);
		ComplexGenerics<A, B> cg = new Generic<ComplexGenerics<A, B>>() {
		}.resolve(container);
		Assert.assertNotNull(cg);
		Assert.assertEquals(2, B.counter);
		Assert.assertTrue(cg.instance1 instanceof A);
		Assert.assertEquals(A.class, cg.instance1.getClass());
		Assert.assertTrue(cg.instance2 instanceof B);
		Assert.assertEquals(B.class, cg.instance2.getClass());
	}

	static class CtorRawGenerics {
		public final ComplexGenerics<A, B> generics;

		public CtorRawGenerics(ComplexGenerics<A, B> generics) {
			this.generics = generics;
		}
	}

	@Test
	public void complexCtorRawGenericsTest() throws IOException {
		B.counter = 0;
		Container container = new SimpleContainer(false);
		container.register(A.class, B.class, ComplexGenerics.class, CtorRawGenerics.class);
		CtorRawGenerics cg = container.resolve(CtorRawGenerics.class);
		Assert.assertNotNull(cg);
		Assert.assertEquals(2, B.counter);
		Assert.assertTrue(cg.generics.instance1 instanceof A);
		Assert.assertEquals(A.class, cg.generics.instance1.getClass());
		Assert.assertTrue(cg.generics.instance2 instanceof B);
		Assert.assertEquals(B.class, cg.generics.instance2.getClass());
	}

	static class CtorGenerics<T> {
		public final ComplexGenerics<T, B> generics;

		public CtorGenerics(ComplexGenerics<T, B> generics) {
			this.generics = generics;
		}
	}

	@Test
	public void complexCtorGenericsTest() throws IOException {
		B.counter = 0;
		Container container = new SimpleContainer(false);
		container.register(A.class, B.class, ComplexGenerics.class, CtorGenerics.class);
		CtorGenerics<A> cg = new Generic<CtorGenerics<A>>() {
		}.resolve(container);
		Assert.assertNotNull(cg);
		Assert.assertEquals(2, B.counter);
		Assert.assertTrue(cg.generics.instance1 instanceof A);
		Assert.assertEquals(A.class, cg.generics.instance1.getClass());
		Assert.assertTrue(cg.generics.instance2 instanceof B);
		Assert.assertEquals(B.class, cg.generics.instance2.getClass());
	}

	@Test
	public void passExceptions() {
		Container container = new SimpleContainer(false);
		container.register(TestContainer.class, c -> {
			throw new RuntimeException("test me now");
		});
		try {
			container.resolve(TestContainer.class);
			Assert.fail("Expecting ReflectiveOperationException");
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains("test me now"));
		}
	}

	@Test
	public void resolveUnknown() throws IOException {
		B.counter = 0;
		Container container = new SimpleContainer(true);
		A a = container.resolve(A.class);
		Assert.assertNotNull(a);
		Assert.assertEquals(1, B.counter);
	}

	@Test
	public void complexCtorGenericsUnknown() throws IOException {
		B.counter = 0;
		Container container = new SimpleContainer(true);
		CtorGenerics<A> cg = new Generic<CtorGenerics<A>>() {
		}.resolve(container);
		Assert.assertNotNull(cg);
		Assert.assertEquals(2, B.counter);
		Assert.assertTrue(cg.generics.instance1 instanceof A);
		Assert.assertEquals(A.class, cg.generics.instance1.getClass());
		Assert.assertTrue(cg.generics.instance2 instanceof B);
		Assert.assertEquals(B.class, cg.generics.instance2.getClass());
	}
}
