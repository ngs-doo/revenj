package org.revenj;

import gen.model.Boot;
import gen.model.Seq.Next;
import gen.model.test.Clicked;
import gen.model.test.Composite;
import gen.model.test.FindMany;
import gen.model.test.Simple;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.revenj.extensibility.Container;
import org.revenj.patterns.DataContext;
import org.revenj.patterns.ServiceLocator;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

public class TestReport {

	private Container container;

	@Before
	public void initContainer() throws IOException {
		container = (Container) Boot.configure("jdbc:postgresql://localhost:5432/revenj");
	}

	@After
	public void closeContainer() throws Exception {
		container.close();
	}

	@Test
	public void canPopulateReport() throws IOException {
		ServiceLocator locator = container;
		DataContext context = locator.resolve(DataContext.class);
		Composite co = new Composite();
		UUID id = UUID.randomUUID();
		co.setId(id);
		context.create(co);
        FindMany fm = new FindMany(id, new HashSet<>(Arrays.asList(id)));
        FindMany.Result result = fm.populate(locator);
        Assert.assertEquals(id, result.getFound().getId());
	}
}
