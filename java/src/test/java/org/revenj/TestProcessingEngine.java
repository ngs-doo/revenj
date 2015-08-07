package org.revenj;

import gen.model.Boot;
import gen.model.test.Composite;
import gen.model.test.Simple;
import org.junit.Assert;
import org.junit.Test;
import org.revenj.patterns.Serialization;
import org.revenj.patterns.ServiceLocator;
import org.revenj.patterns.WireSerialization;
import org.revenj.server.CommandResultDescription;
import org.revenj.server.ProcessingEngine;
import org.revenj.server.ProcessingResult;
import org.revenj.server.ServerCommandDescription;
import org.revenj.server.commands.CRUD.Create;
import org.revenj.server.commands.CRUD.Read;

import java.util.UUID;

public class TestProcessingEngine {

	@Test
	public void passThroughEngine() throws Exception {
		ServiceLocator container = Boot.configure("jdbc:postgresql://localhost:5432/revenj");
		ProcessingEngine engine = container.resolve(ProcessingEngine.class);
		Composite composite = new Composite().setId(UUID.randomUUID()).setSimple(new Simple().setNumber(234).setText("text"));
		ServerCommandDescription cd = new ServerCommandDescription<>(
				null,
				Create.class,
				new Create.Argument<>("test.Composite", composite));
		ProcessingResult<Object> result =
				engine.execute(
						Object.class,
						Object.class,
						new ServerCommandDescription[]{cd});
		Assert.assertEquals(200, result.status);
		Assert.assertEquals(1, result.executedCommandResults.length);
		CommandResultDescription description = result.executedCommandResults[0];
		Assert.assertEquals(201, description.result.status);
		String uri = (String) description.result.data;
		Assert.assertEquals(composite.getId().toString(), uri);
		cd = new ServerCommandDescription<>(
				null,
				Read.class,
				new Read.Argument("test.Composite", uri));
		result =
				engine.execute(
						Object.class,
						Object.class,
						new ServerCommandDescription[]{cd});
		Assert.assertEquals(200, result.status);
		Assert.assertEquals(1, result.executedCommandResults.length);
		description = result.executedCommandResults[0];
		Assert.assertEquals(200, description.result.status);
		Composite c2 = (Composite) description.result.data;
		//Assert.assertEquals(composite, c2);//TODO
		Assert.assertEquals(composite.getId(), c2.getId());
		Assert.assertEquals(composite.getSimple(), c2.getSimple());
	}

	@Test
	public void jsonThroughEngine() throws Exception {
		ServiceLocator container = Boot.configure("jdbc:postgresql://localhost:5432/revenj");
		ProcessingEngine engine = container.resolve(ProcessingEngine.class);
		WireSerialization serialization = container.resolve(WireSerialization.class);
		Serialization<String> json = serialization.find(String.class).get();
		Composite composite = new Composite().setId(UUID.randomUUID()).setSimple(new Simple().setNumber(234).setText("text"));
		ServerCommandDescription cd = new ServerCommandDescription<>(
				null,
				Create.class,
				json.serialize(new Create.Argument<>("test.Composite", json.serialize(composite))));
		ProcessingResult<String> result =
				engine.execute(
						String.class,
						String.class,
						new ServerCommandDescription[]{cd});
		Assert.assertEquals(200, result.status);
		Assert.assertEquals(1, result.executedCommandResults.length);
		CommandResultDescription<String> description = result.executedCommandResults[0];
		Assert.assertEquals(201, description.result.status);
		String uri = json.deserialize(description.result.data, String.class);
		Assert.assertEquals(composite.getId().toString(), uri);
		cd = new ServerCommandDescription<>(
				null,
				Read.class,
				json.serialize(new Read.Argument("test.Composite", uri)));
		result =
				engine.execute(
						String.class,
						String.class,
						new ServerCommandDescription[]{cd});
		Assert.assertEquals(200, result.status);
		Assert.assertEquals(1, result.executedCommandResults.length);
		description = result.executedCommandResults[0];
		Assert.assertEquals(200, description.result.status);
		Composite c2 = json.deserialize(description.result.data, Composite.class);
		//Assert.assertEquals(composite, c2);//TODO
		Assert.assertEquals(composite.getId(), c2.getId());
		Assert.assertEquals(composite.getSimple(), c2.getSimple());
	}
}
