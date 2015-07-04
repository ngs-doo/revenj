package org.revenj;

import gen.model.Boot;
import gen.model.test.Composite;
import gen.model.test.Simple;
import org.junit.Assert;
import org.junit.Test;
import org.revenj.patterns.Container;
import org.revenj.patterns.Generic;
import org.revenj.patterns.ServiceLocator;
import org.revenj.serialization.JsonSerialization;
import org.revenj.server.CommandResultDescription;
import org.revenj.server.ProcessingEngine;
import org.revenj.server.ProcessingResult;
import org.revenj.server.ServerCommandDescription;
import org.revenj.server.commands.CreateCommand;
import org.revenj.server.commands.ReadCommand;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

public class TestProcessingEngine {

	@Test
	public void passThroughEngine() throws IOException, SQLException {
		Container container = Boot.configure("jdbc:postgresql://localhost:5432/revenj");
		ProcessingEngine engine = new ProcessingEngine(container);
		Composite composite = new Composite().setId(UUID.randomUUID()).setSimple(new Simple().setNumber(234).setText("text"));
		ServerCommandDescription cd = new ServerCommandDescription<>(
				null,
				CreateCommand.class,
				new CreateCommand.Argument<>("test.Composite", composite));
		ProcessingResult<Object> result =
				engine.execute(
						Object.class,
						Object.class,
						new ServerCommandDescription[] { cd });
		Assert.assertEquals(200, result.status);
		Assert.assertEquals(1, result.executedCommandResults.length);
		CommandResultDescription description = result.executedCommandResults[0];
		Assert.assertEquals(201, description.result.status);
		String uri = (String) description.result.data;
		Assert.assertEquals(composite.getId().toString(), uri);
		cd = new ServerCommandDescription<>(
				null,
				ReadCommand.class,
				new ReadCommand.Argument("test.Composite", uri));
		result = engine.execute(
						Object.class,
						Object.class,
						new ServerCommandDescription[] { cd });
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
	public void jsonThroughEngine() throws IOException, SQLException {
		Container container = Boot.configure("jdbc:postgresql://localhost:5432/revenj");
		ProcessingEngine engine = new ProcessingEngine(container);
		JsonSerialization json = new JsonSerialization();
		Composite composite = new Composite().setId(UUID.randomUUID()).setSimple(new Simple().setNumber(234).setText("text"));
		ServerCommandDescription cd = new ServerCommandDescription<>(
				null,
				CreateCommand.class,
				json.serializeTo(new CreateCommand.Argument<>("test.Composite", json.serializeTo(composite))));
		ProcessingResult<String> result =
				engine.execute(
						String.class,
						String.class,
						new ServerCommandDescription[] { cd });
		Assert.assertEquals(200, result.status);
		Assert.assertEquals(1, result.executedCommandResults.length);
		CommandResultDescription<String> description = result.executedCommandResults[0];
		Assert.assertEquals(201, description.result.status);
		String uri = json.deserialize(String.class, description.result.data);
		Assert.assertEquals(composite.getId().toString(), uri);
		cd = new ServerCommandDescription<>(
				null,
				ReadCommand.class,
				json.serializeTo(new ReadCommand.Argument("test.Composite", uri)));
		result = engine.execute(
				String.class,
				String.class,
				new ServerCommandDescription[] { cd });
		Assert.assertEquals(200, result.status);
		Assert.assertEquals(1, result.executedCommandResults.length);
		description = result.executedCommandResults[0];
		Assert.assertEquals(200, description.result.status);
		Composite c2 = json.deserialize(Composite.class, description.result.data);
		//Assert.assertEquals(composite, c2);//TODO
		Assert.assertEquals(composite.getId(), c2.getId());
		Assert.assertEquals(composite.getSimple(), c2.getSimple());
	}
}
