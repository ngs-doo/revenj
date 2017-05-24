package org.revenj;

import org.junit.Assert;
import org.junit.Test;
import org.revenj.extensibility.Container;
import org.revenj.extensibility.InstanceScope;
import org.revenj.patterns.Generic;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.Callable;

public class TestContainer {

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

	static class Later {
		private final Callable<A> aCallable;

		public Later(Callable<A> aCallable) {
			this.aCallable = aCallable;
		}

		public A getA() throws Exception {
			return aCallable.call();
		}
	}

	@Test
	public void resolveLater() throws Exception {
		Container container = new SimpleContainer(true);
		Later l = container.resolve(Later.class);
		Assert.assertNotNull(l);
		A a = l.getA();
		Assert.assertNotNull(a);
	}

	static class SelfReference {
		private final Callable<SelfReference> self;

		public SelfReference(Callable<SelfReference> self) {
			this.self = self;
		}

		public SelfReference getSelf() throws Exception {
			return self.call();
		}
	}

	@Test
	public void selfReferenceSingleton() throws Exception {
		Container container = new SimpleContainer(false);
		container.register(SelfReference.class, InstanceScope.SINGLETON);
		SelfReference sr = container.resolve(SelfReference.class);
		SelfReference sr2 = sr.getSelf();
		Assert.assertEquals(sr, sr2);
	}

	static class Single {
	}

	@Test
	public void singletonInContext() throws Exception {
		Container container = new SimpleContainer(false);
		container.register(Single.class, InstanceScope.SINGLETON);
		Container nested = container.createScope();
		Single s1 = nested.resolve(Single.class);
		Single s2 = container.resolve(Single.class);
		Assert.assertEquals(s1, s2);
	}

	@Test
	public void singletonFactory() throws Exception {
		Container container = new SimpleContainer(false);
		container.registerFactory(Single.class, c -> new Single(), InstanceScope.SINGLETON);
		Single s1 = container.resolve(Single.class);
		Single s2 = container.resolve(Single.class);
		Assert.assertEquals(s1, s2);
	}

	@Test
	public void singletonFactoryInScope() throws Exception {
		Container container = new SimpleContainer(false);
		container.registerFactory(Single.class, c -> new Single(), InstanceScope.SINGLETON);
		Container c1 = container.createScope();
		Single s1 = c1.resolve(Single.class);
		Single s2 = c1.resolve(Single.class);
		Assert.assertEquals(s1, s2);
		Container c2 = container.createScope();
		Single s3 = c2.resolve(Single.class);
		Single s4 = c2.resolve(Single.class);
		Assert.assertEquals(s3, s4);
		Assert.assertEquals(s1, s3);
	}

	@Test
	public void contextFactoryInScope() throws Exception {
		Container container = new SimpleContainer(false);
		container.registerFactory(Single.class, c -> new Single(), InstanceScope.CONTEXT);
		Container c1 = container.createScope();
		Single s1 = c1.resolve(Single.class);
		Single s2 = c1.resolve(Single.class);
		Assert.assertEquals(s1, s2);
		Container c2 = container.createScope();
		Single s3 = c2.resolve(Single.class);
		Single s4 = c2.resolve(Single.class);
		Assert.assertEquals(s3, s4);
		Assert.assertNotEquals(s1, s3);
	}

	@Test
	public void circularDependency() throws Exception {
		Container container = new SimpleContainer(false);
		container.register(CircularTop.class, InstanceScope.CONTEXT);
		container.register(CircularDep.class, InstanceScope.CONTEXT);
		try {
			container.resolve((Type)CircularTop.class);
			Assert.fail("Expecting exception");
		} catch (ReflectiveOperationException ex) {
			Assert.assertEquals("Unable to resolve: class org.revenj.TestContainer$CircularTop. Circular dependencies in signature detected", ex.getMessage());
		}
	}

	@Test
	public void singletonContainer() throws Exception {
		Container container = new SimpleContainer(false);
		container.register(UsesContainer.class, InstanceScope.SINGLETON);
		Container scope = container.createScope();
		UsesContainer uc = scope.resolve(UsesContainer.class);
		Assert.assertEquals(container, uc.container);
		Assert.assertNotEquals(scope, uc.container);
	}

	public static class UsesContainer {
		public final Container container;
		public UsesContainer(Container container) {
			this.container = container;
		}
	}

	public static class CircularTop {
		public final CircularDep dep;

		public CircularTop(CircularDep dep) {
			this.dep = dep;
		}
	}

	public static class CircularDep {
		public final CircularTop top;

		public CircularDep(CircularTop top) {
			this.top = top;
		}
	}

	@Test
	public void collectionErrorIsPropagated() throws Exception {
		Container container = new SimpleContainer(false);
		container.register(ErrorInCollection.class);
		try {
			container.resolve((Type)ErrorInCollection[].class);
			Assert.fail("Expecting exception");
		} catch (ReflectiveOperationException ex) {
			Assert.assertEquals("Unable to resolve class org.revenj.TestContainer$ErrorInCollection. Error: naah", ex.getMessage());
		}
	}

	public static class ErrorInCollection {
		public ErrorInCollection() {
			throw new RuntimeException("naah");
		}
	}
}
