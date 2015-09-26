package org.revenj;

import org.junit.Assert;
import org.junit.Test;
import org.revenj.postgres.PostgresReader;
import org.revenj.postgres.PostgresWriter;
import org.revenj.postgres.converters.*;

import java.io.IOException;
import java.util.*;

public class TestPostgres {

	@Test
	public void floatIssue() throws IOException {
		PostgresReader reader = new PostgresReader();
		List<Float> floats = Arrays.asList(0f, -0.000012345f, -0.00001f, Float.NaN, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
		PostgresTuple tuple = ArrayTuple.create(floats, FloatConverter::toTuple);
		String value = tuple.buildTuple(false);
		reader.process(value);
		List<Float> result = FloatConverter.parseCollection(reader, 0, false);
		Assert.assertEquals(floats, result);
	}

	@Test
	public void uuidIssue() throws IOException {
		PostgresReader reader = new PostgresReader();
		List<UUID> uuids = Arrays.asList(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
		PostgresTuple tuple = ArrayTuple.create(uuids, UuidConverter::toTuple);
		String value = tuple.buildTuple(false);
		reader.process(value);
		List<UUID> result = UuidConverter.parseCollection(reader, 0, false);
		Assert.assertEquals(uuids, result);
	}

	@Test
	public void mapIssue() throws IOException {
		PostgresReader reader = new PostgresReader();
		List<Map<String, String>> maps = new ArrayList<>();
		maps.add(null);
		maps.add(new HashMap<>());
		Map<String, String> ab = new HashMap<>();
		ab.put("a", "b");
		maps.add(ab);
		Map<String, String> cplx = new HashMap<>();
		cplx.put("a ' \\ x", "\" b \\ '");
		maps.add(cplx);
		PostgresTuple tuple = ArrayTuple.create(maps, HstoreConverter::toTuple);
		String value = tuple.buildTuple(false);
		reader.process(value);
		List<Map<String, String>> result = HstoreConverter.parseCollection(reader, 0, true);
		Assert.assertEquals(maps, result);
	}
}
