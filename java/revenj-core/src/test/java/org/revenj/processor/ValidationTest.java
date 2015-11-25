package org.revenj.processor;

import org.junit.Test;
import org.revenj.processor.models.TestInvalidEventHandler;
import org.revenj.processor.models.TestValidEventHandler;

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
		assertCompilationSuccessful(compileTestCase(TestValidEventHandler.class));
	}

	@Test
	public void testInvalidHandler() {
		assertCompilationReturned(Diagnostic.Kind.ERROR, 5, compileTestCase(TestInvalidEventHandler.class));
	}
}
