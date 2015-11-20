package org.revenj;

import gen.model.adt.BasicSecurity;
import gen.model.adt.User;
import org.junit.Assert;
import org.junit.Test;
import org.revenj.json.DslJsonSerialization;

import java.io.IOException;
import java.util.*;

public class TestDslJson {

	@Test
	public void simpleManualSerialization() throws IOException {
		DslJsonSerialization json = new DslJsonSerialization(null, Optional.empty());
		User user =  new User("user", new BasicSecurity().setUsername("email").setPassword("secret"));
		String result = json.serialize(user);
		Assert.assertEquals("{\"URI\":\"user\",\"username\":\"user\",\"authentication\":{\"$type\":\"adt.BasicSecurity\",\"username\":\"email\",\"password\":\"secret\"}}", result);
		User deserialized = json.deserialize(result, User.class);
		Assert.assertTrue(user.deepEquals(deserialized));
	}
}
