package org.revenj;

import com.dslplatform.compiler.client.CompileParameter;
import com.dslplatform.compiler.client.Context;
import com.dslplatform.compiler.client.Main;
import com.dslplatform.compiler.client.parameters.*;
import ru.yandex.qatools.embed.service.PostgresEmbeddedService;

import java.io.IOException;
import java.util.List;

public abstract class Setup {
	static class TestContext extends Context {
		public void show(String... values) {
		}

		public void log(String value) {
		}

		public void log(char[] value, int len) {
		}

		public void error(String value) {
		}

		public void error(Exception ex) {
		}

	}

	public static PostgresEmbeddedService database() throws IOException {
		PostgresEmbeddedService postgres = new PostgresEmbeddedService("localhost", 5555, "revenj", "revenj", "revenj", "target/db", true, 5000);
		postgres.start();
		TestContext context = new TestContext();
		context.put(Download.INSTANCE, null);
		context.put(Force.INSTANCE, null);
		context.put(ApplyMigration.INSTANCE, null);
		context.put(Prompt.INSTANCE, null);
		context.put(PostgresConnection.INSTANCE, "localhost:5555/revenj?user=revenj&password=revenj");
		List<CompileParameter> params = Main.initializeParameters(context, ".");
		if (!Main.processContext(context, params)) {
			throw new IOException("Unable to migrate database");
		}
		return postgres;
	}
}
