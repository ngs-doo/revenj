package org.revenj.processor;

import org.junit.Test;
import org.revenj.processor.models.*;

import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
import java.util.Collection;
import java.util.Collections;

public class ValidationTest extends AbstractAnnotationProcessorTest {

	protected Collection<Processor> getProcessors() {
		return Collections.<Processor>singletonList(new RevenjProcessor());
	}

	@Test
	public void testValidHandler() {
		assertCompilationSuccessful(compileTestCase(ValidEventHandler.class));
	}

	@Test
	public void testInvalidHandler() {
		assertCompilationReturned(Diagnostic.Kind.ERROR, 5, compileTestCase(InvalidEventHandler.class));
	}

	@Test
	public void testGenericsClassWarning() {
		assertCompilationReturned(Diagnostic.Kind.WARNING, 6, compileTestCase(GenericClass.class));
	}

	@Test
	public void testNonPublicClass() {
		assertCompilationReturned(Diagnostic.Kind.WARNING, 6, compileTestCase(NonPublicClass.class));
	}

	@Test
	public void testNonPublicArgument() {
		assertCompilationReturned(Diagnostic.Kind.WARNING, 8, compileTestCase(NonPublicArgument.class));
	}

	@Test
	public void testGenericArgWarning() {
		assertCompilationSuccessful(compileTestCase(GenericArgument.class));
	}

	@Test
	public void testGenericNonPublicArgument() {
		assertCompilationReturned(Diagnostic.Kind.WARNING, 8, compileTestCase(GenericNonPublicArgument.class));
	}
}