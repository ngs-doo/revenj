package org.revenj;

import gen.model.Boot;
import gen.model.adt.BasicSecurity;
import gen.model.adt.User;
import org.junit.Assert;
import org.junit.Test;
import org.revenj.extensibility.Container;
import org.revenj.json.DslJsonSerialization;
import org.revenj.serialization.WireSerialization;
import org.revenj.server.servlet.Application;
import ru.yandex.qatools.embed.service.PostgresEmbeddedService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class TestDslJson {

	@Test
	public void simpleManualSerialization() throws IOException {
		DslJsonSerialization json = new DslJsonSerialization(null, Optional.empty());
		User user = new User("user", new BasicSecurity().setUsername("email").setPassword("secret"));
		String result = json.serialize(user);
		Assert.assertEquals("{\"URI\":\"user\",\"username\":\"user\",\"authentication\":{\"$type\":\"adt.BasicSecurity\",\"username\":\"email\",\"password\":\"secret\"}}", result);
		User deserialized = json.deserialize(result, User.class);
		Assert.assertTrue(user.deepEquals(deserialized));
	}

	static class TestMe {
	}

	@Test
	public void testFallback() throws Exception {
		PostgresEmbeddedService postgres = null;
		try {
			postgres = Setup.database();
			try (Container container = (Container) Boot.configure("jdbc:postgresql://localhost:5555/revenj")) {
				Application.setup(container);
				WireSerialization serialization = container.resolve(WireSerialization.class);
				ByteArrayOutputStream os = serialization.serialize(new TestMe(), "application/json");
				Assert.assertEquals("{}", os.toString("UTF-8"));
			}
		} finally {
			if (postgres != null) postgres.stop();
		}
	}
}
