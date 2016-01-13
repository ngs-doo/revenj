package org.revenj.server;

import gen.model.Boot;
import gen.model.test.Composite;
import gen.model.test.FindMany;
import gen.model.test.Simple;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.revenj.Revenj;
import org.revenj.extensibility.Container;
import org.revenj.extensibility.SystemAspect;
import org.revenj.serialization.Serialization;
import org.revenj.serialization.WireSerialization;
import org.revenj.server.commands.crud.Create;
import org.revenj.server.commands.crud.Read;
import org.revenj.server.commands.reporting.AnalyzeOlapCube;
import org.revenj.server.commands.reporting.PopulateReport;
import org.revenj.server.servlet.Application;

import java.io.File;
import java.io.FileReader;
import java.util.*;

public class TestProcessingEngine {

	private Container container;

	@Before
	public void initContainer() throws Exception {
		Properties properties = new Properties();
		properties.setProperty("revenj.namespace", "gen.model");
		File revProps = new File("test.properties");
		if (revProps.exists() && revProps.isFile()) {
			properties.load(new FileReader(revProps));
		}
		PGSimpleDataSource dataSource = new PGSimpleDataSource();
		dataSource.setUrl("jdbc:postgresql://localhost/revenj");
		dataSource.setUser(properties.getProperty("user"));
		dataSource.setPassword(properties.getProperty("password"));
		container =
				Revenj.setup(
						dataSource,
						properties,
						Optional.<ClassLoader>empty(),
						Collections.singletonList((SystemAspect) new Boot()).iterator());
		Application.setup(container);
	}

	@After
	public void closeContainer() throws Exception {
		container.close();
	}

	@Test
	public void passThroughEngine() throws Exception {
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
						new ServerCommandDescription[]{cd},
						null);
		Assert.assertEquals(200, result.status);
		Assert.assertEquals(1, result.executedCommandResults.length);
		CommandResultDescription description = result.executedCommandResults[0];
		Assert.assertEquals(201, description.result.status);
		Composite saved = (Composite) description.result.data;
		Assert.assertEquals(composite.getId().toString(), saved.getURI());
		cd = new ServerCommandDescription<>(
				null,
				Read.class,
				new Read.Argument("test.Composite", saved.getURI()));
		result =
				engine.execute(
						Object.class,
						Object.class,
						new ServerCommandDescription[]{cd},
						null);
		Assert.assertEquals(200, result.status);
		Assert.assertEquals(1, result.executedCommandResults.length);
		description = result.executedCommandResults[0];
		Assert.assertEquals(200, description.result.status);
		Composite c2 = (Composite) description.result.data;
		Assert.assertEquals(composite, c2);
		Assert.assertEquals(composite.getId(), c2.getId());
		Assert.assertEquals(composite.getSimple(), c2.getSimple());
	}

	@Test
	public void jsonThroughEngine() throws Exception {
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
						new ServerCommandDescription[]{cd},
						null);
		Assert.assertEquals(200, result.status);
		Assert.assertEquals(1, result.executedCommandResults.length);
		CommandResultDescription<String> description = result.executedCommandResults[0];
		Assert.assertEquals(201, description.result.status);
		Composite saved = json.deserialize(description.result.data, Composite.class);
		Assert.assertEquals(composite.getId().toString(), saved.getURI());
		cd = new ServerCommandDescription<>(
				null,
				Read.class,
				json.serialize(new Read.Argument("test.Composite", saved.getURI())));
		result =
				engine.execute(
						String.class,
						String.class,
						new ServerCommandDescription[]{cd},
						null);
		Assert.assertEquals(200, result.status);
		Assert.assertEquals(1, result.executedCommandResults.length);
		description = result.executedCommandResults[0];
		Assert.assertEquals(200, description.result.status);
		Composite c2 = json.deserialize(description.result.data, Composite.class);
		//Assert.assertEquals(composite, c2);//TODO
		Assert.assertEquals(composite.getId(), c2.getId());
		Assert.assertEquals(composite.getSimple(), c2.getSimple());
	}

	@Test
	public void olapCommand() throws Exception {
		ProcessingEngine engine = container.resolve(ProcessingEngine.class);
		Composite composite = new Composite().setId(UUID.randomUUID()).setSimple(new Simple().setNumber(1234).setText("text"));
		ServerCommandDescription cd1 = new ServerCommandDescription<>(
				null,
				Create.class,
				new Create.Argument<>("test.Composite", composite));
		ServerCommandDescription cd2 = new ServerCommandDescription<>(
				null,
				AnalyzeOlapCube.class,
				new AnalyzeOlapCube.Argument<>("test.CompositeCube", null, null, new String[]{"number"}, new String[]{"max"}, null, 5, null));
		ProcessingResult<Object> result =
				engine.execute(
						Object.class,
						Object.class,
						new ServerCommandDescription[]{cd1, cd2},
						null);
		Assert.assertEquals(200, result.status);
		Assert.assertEquals(2, result.executedCommandResults.length);
		CommandResultDescription description = result.executedCommandResults[0];
		Assert.assertEquals(201, description.result.status);
		description = result.executedCommandResults[1];
		Assert.assertEquals(200, description.result.status);
		List<Map<String, Object>> cube = (List<Map<String, Object>>) description.result.data;
		Assert.assertTrue(cube.size() > 0);
		Assert.assertEquals(2, cube.get(0).size());
		Assert.assertTrue(cube.get(0).containsKey("number"));
		Assert.assertTrue(cube.get(0).containsKey("max"));
	}

	@Test
	public void reportCommand() throws Exception {
		ProcessingEngine engine = container.resolve(ProcessingEngine.class);
		Composite composite = new Composite().setId(UUID.randomUUID()).setSimple(new Simple().setNumber(1234).setText("text"));
		ServerCommandDescription cd1 = new ServerCommandDescription<>(
				null,
				Create.class,
				new Create.Argument<>("test.Composite", composite));
		ServerCommandDescription cd2 = new ServerCommandDescription<>(
				null,
				PopulateReport.class,
				new PopulateReport.Argument<>(new FindMany().setId(composite.getId()), "test.FindMany"));
		ProcessingResult<Object> result =
				engine.execute(
						Object.class,
						Object.class,
						new ServerCommandDescription[]{cd1, cd2},
						null);
		Assert.assertEquals(200, result.status);
		Assert.assertEquals(2, result.executedCommandResults.length);
		CommandResultDescription description = result.executedCommandResults[0];
		Assert.assertEquals(201, description.result.status);
		description = result.executedCommandResults[1];
		Assert.assertEquals(200, description.result.status);
		FindMany.Result report = (FindMany.Result) description.result.data;
		Assert.assertEquals(composite.getId(), report.getFound().getId());
	}
}
