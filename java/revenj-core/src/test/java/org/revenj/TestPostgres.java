package org.revenj;

import org.junit.Assert;
import org.junit.Test;
import org.revenj.postgres.PostgresReader;
import org.revenj.postgres.PostgresWriter;
import org.revenj.postgres.converters.*;

import java.io.IOException;
import java.time.OffsetDateTime;
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

	@Test
	public void binaryIssue() throws IOException {
		PostgresReader reader = new PostgresReader();
		byte[] bytes = Base64.getDecoder().decode("gAB/");
		ByteaConverter.serializeURI(reader, bytes);
		String uri = reader.bufferToString();
		Assert.assertEquals("\\x80007f", uri);
	}

	@Test
	public void zoneRange() throws IOException {
		PostgresReader reader = new PostgresReader();
		reader.process("[NULL,\"2015-09-28 13:35:42.973+02:00\",\"1970-01-01 01:00:00+01:00\",\"0001-01-01 00:00:00Z\",\"2038-02-13 00:45:30.647+01:00\"]");
		List<OffsetDateTime> values = TimestampConverter.parseOffsetCollection(reader, 0, true, true);
		Assert.assertEquals(5, values.size());
		Assert.assertNull(values.get(0));
	}
}
