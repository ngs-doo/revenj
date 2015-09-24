package org.revenj;

import org.junit.Assert;
import org.junit.Test;
import org.revenj.patterns.Generic;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

public class TestReflection {

	@Test
	public void genericTypeNames() throws IOException {
		Type generated = Utils.makeGenericType(Map.class, String.class, Object.class);
		Type original = new Generic<Map<String, Object>>() {	}.type;
		Assert.assertEquals(original.getTypeName(), generated.getTypeName());
		Assert.assertEquals(original, generated);
	}

	@Test
	public void genericTypeReuse() throws IOException {
		Type generated1 = Utils.makeGenericType(Map.class, String.class, Object.class);
		Type generated2 = Utils.makeGenericType(Map.class, String.class, Object.class);
		Assert.assertEquals(generated1.hashCode(), generated2.hashCode());
	}
}
