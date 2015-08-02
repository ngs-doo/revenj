package org.revenj;

import gen.model.Boot;
import gen.model.Seq.Next;
import gen.model.test.*;
import gen.model.test.repositories.*;
import org.junit.Assert;
import org.junit.Test;
import org.revenj.patterns.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class TestDataContext {

	@Test
	public void canCreateAggregate() throws IOException {
		ServiceLocator locator = Boot.configure("jdbc:postgresql://localhost:5432/revenj");
		DataContext context = locator.resolve(DataContext.class);
		Composite co = new Composite();
		UUID id = UUID.randomUUID();
		co.setId(id);
		Simple so = new Simple();
		so.setNumber(5);
		so.setText("test me ' \\ \" now");
		co.setSimple(so);
		context.create(co);
		Optional<Composite> found = context.find(Composite.class, id.toString());
		Assert.assertTrue(found.isPresent());
		Composite co2 = found.get();
		Assert.assertEquals(co.getId(), co2.getId());
		Assert.assertEquals(co.getSimple().getNumber(), co2.getSimple().getNumber());
		Assert.assertEquals(co.getSimple().getText(), co2.getSimple().getText());
	}

	@Test
	public void canSubmitEvent() throws IOException {
		ServiceLocator locator = Boot.configure("jdbc:postgresql://localhost:5432/revenj");
		DataContext context = locator.resolve(DataContext.class);
		Clicked cl = new Clicked().setBigint(Long.MAX_VALUE).setDate(LocalDate.now()).setNumber(BigDecimal.valueOf(11.22));
		context.submit(cl);
	}

	@Test
	public void canQuery() throws IOException {
		ServiceLocator locator = Boot.configure("jdbc:postgresql://localhost:5432/revenj");
		DataContext context = locator.resolve(DataContext.class);
		context.create(Arrays.asList(new Next(), new Next()));
		long total = context.query(Next.class).count();
		Assert.assertTrue(total > 1);
	}
}
