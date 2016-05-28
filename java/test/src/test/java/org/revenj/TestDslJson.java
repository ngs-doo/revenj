package org.revenj;

import gen.model.adt.BasicSecurity;
import gen.model.adt.User;
import gen.model.issues.SA1Bo;
import gen.model.numbers.DecimalPk;
import gen.model.numbers.EntityWithLongs;
import gen.model.numbers.LongNumbers;
import org.junit.Assert;
import org.junit.Test;
import org.revenj.extensibility.Container;
import org.revenj.serialization.json.DslJsonSerialization;
import org.revenj.patterns.DataContext;
import org.revenj.serialization.WireSerialization;
import org.revenj.server.servlet.Application;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class TestDslJson extends Setup {

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
		try (Container container = Setup.container()) {
			Application.setup(container);
			WireSerialization serialization = container.resolve(WireSerialization.class);
			ByteArrayOutputStream os = serialization.serialize(new TestMe(), "application/json");
			Assert.assertEquals("{}", os.toString("UTF-8"));
		}
	}

	@Test
	public void testJsonObject() throws Exception {
		try (Container container = Setup.container()) {
			Application.setup(container);
			WireSerialization serialization = container.resolve(WireSerialization.class);
			LongNumbers orig = new LongNumbers().setEntity(new EntityWithLongs().setProp(new long[] { 3 }));
			ByteArrayOutputStream os = serialization.serialize(orig, "application/json");
			ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
			LongNumbers deser = serialization.deserialize(is, "application/json", LongNumbers.class);
			Assert.assertTrue(deser.deepEquals(orig));
			Assert.assertArrayEquals(new long[] { 3 }, deser.getEntity().getProp());
		}
	}

	@Test
	public void testJsonObjectDefaults() throws Exception {
		try (Container container = Setup.container()) {
			Application.setup(container);
			WireSerialization serialization = container.resolve(WireSerialization.class);
			ByteArrayInputStream is = new ByteArrayInputStream("{\"sE1Bo\":{\"p1Bo\":true}}".getBytes());
			SA1Bo deser = serialization.deserialize(is, "application/json", SA1Bo.class);
			Assert.assertTrue(deser.getSE1Bo().getP1Bo());
		}
	}

	@Test
	public void testJsonObjectDefaultsForNull() throws Exception {
		try (Container container = Setup.container()) {
			Application.setup(container);
			WireSerialization serialization = container.resolve(WireSerialization.class);
			ByteArrayInputStream is = new ByteArrayInputStream("{}".getBytes());
			SA1Bo deser = serialization.deserialize(is, "application/json", SA1Bo.class);
			Assert.assertNotNull(deser.getSE1Bo());
			Assert.assertNotNull(deser.getSV1Bo());
		}
	}

	@Test
	public void testDecimalURI() throws Exception {
		try (Container container = Setup.container()) {
			Application.setup(container);
			WireSerialization serialization = container.resolve(WireSerialization.class);
			DecimalPk orig = new DecimalPk(BigDecimal.valueOf(3));
			DataContext ctx = container.resolve(DataContext.class);
			ctx.create(orig);
			ctx.delete(orig);
			ByteArrayOutputStream os = serialization.serialize(orig, "application/json");
			Assert.assertTrue(os.toString("UTF-8").contains("{\"URI\":\"3.0000\","));
		}
	}
}
