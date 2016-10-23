package org.revenj;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.Main;
import com.dslplatform.compiler.client.parameters.*;
import gen.model.Boot;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.revenj.extensibility.Container;
import ru.yandex.qatools.embed.service.PostgresEmbeddedService;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public abstract class Setup {
	private static class TestContext extends Context {

		StringBuilder error = new StringBuilder();

		public void show(String... values) {
		}

		public void log(String value) {
		}

		public void log(char[] value, int len) {
		}

		public void warning(String value) {
			error.append(value);
		}

		public void warning(Exception ex) {
			error.append(ex.getMessage());
		}

		public void error(String value) {
			error.append(value);
		}

		public void error(Exception ex) {
			error.append(ex.getMessage());
		}
	}

	private static PostgresEmbeddedService postgres;

	@BeforeClass
	public static void setupDatabase() throws IOException {
		postgres = database();
	}

	@AfterClass
	public static void teardownDatabase() {
		if (postgres != null) {
			postgres.stop();
			postgres = null;
		}
	}

	protected Container container;

	@Before
	public void initContainer() throws IOException {
		container = Setup.container();
	}

	@After
	public void closeContainer() throws Exception {
		container.close();
	}

	public static PostgresEmbeddedService database() throws IOException {
		PostgresEmbeddedService postgres = new PostgresEmbeddedService("localhost", 5555, "revenj", "revenj", "revenj", "target/db", true, 5000);
		postgres.start();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
		TestContext context = new TestContext();
		context.put(Download.INSTANCE, null);
		context.put(Force.INSTANCE, null);
		context.put(ApplyMigration.INSTANCE, null);
		context.put(DisablePrompt.INSTANCE, null);
		context.put(PostgresConnection.INSTANCE, "localhost:5555/revenj?user=revenj&password=revenj");
		context.put(DslPath.INSTANCE, "src/test/resources");
		List<CompileParameter> params = Main.initializeParameters(context, ".");
		if (!Main.processContext(context, params)) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				throw new IOException(e);
			}
			context.error.setLength(0);
			if (!Main.processContext(context, params)) {
				throw new IOException("Unable to migrate database: " + context.error.toString());
			}
		}
		return postgres;
	}

	public static Container container() throws IOException {
		return (Container) Boot.configure("jdbc:postgresql://localhost:5555/revenj?user=revenj&password=revenj");
	}

	public static Container container(Properties properties) throws IOException {
		return (Container) Boot.configure("jdbc:postgresql://localhost:5555/revenj?user=revenj&password=revenj", properties);
	}

}
