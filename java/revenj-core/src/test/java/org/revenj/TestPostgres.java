package org.revenj;

import org.junit.Assert;
import org.junit.Test;
import org.revenj.database.postgres.PostgresReader;
import org.revenj.database.postgres.PostgresWriter;
import org.revenj.database.postgres.converters.*;

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
		reader.process("{NULL,\"2015-09-28 13:35:42.973+02:00\",\"1970-01-01 01:00:00+01:00\",\"0001-01-01 00:00:00Z\",\"2038-02-13 00:45:30.647+01:00\"}");
		List<OffsetDateTime> values = TimestampConverter.parseOffsetCollection(reader, 0, true, true);
		Assert.assertEquals(5, values.size());
		Assert.assertNull(values.get(0));
	}

	@Test
	public void timestampWithTimeOffset() throws IOException {
		PostgresReader reader = new PostgresReader();
		reader.process("{\"0001-01-01 00:00:00+01:22\"}");
		List<OffsetDateTime> values = TimestampConverter.parseOffsetCollection(reader, 0, true, false);
		Assert.assertEquals(1, values.size());
		Assert.assertEquals(4920, values.get(0).getOffset().getTotalSeconds());
		Assert.assertEquals(0, values.get(0).getMinute());
		reader.process("{\"0001-01-01 00:00:00+01:22\"}");
		values = TimestampConverter.parseOffsetCollection(reader, 0, true, true);
		Assert.assertEquals(1, values.size());
		Assert.assertEquals(0, values.get(0).getOffset().getTotalSeconds());
		Assert.assertEquals(38, values.get(0).getMinute());
	}

	@Test
	public void specialTimestampUri() throws IOException {
		PostgresWriter writer = new PostgresWriter();
		OffsetDateTime zero = OffsetDateTime.parse("0001-01-01T00:00:00+01:22");
		TimestampConverter.serializeURI(writer, zero);
		String value = writer.bufferToString();
		Assert.assertEquals("0001-01-01 00:00:00+01:22", value);
	}

	@Test
	public void timestampUriNormalization() throws IOException {
		PostgresWriter writer = new PostgresWriter();
		OffsetDateTime zero = OffsetDateTime.parse("2001-01-01T00:00:00+01:22");
		TimestampConverter.serializeURI(writer, zero);
		String value = writer.bufferToString();
		Assert.assertEquals("2000-12-31 23:38:00+01", value);
	}

	@Test
	public void checkUriParsing() throws IOException {
		String[] arr = new String[2];
		PostgresReader.parseCompositeURI("1234/", arr);
		Assert.assertEquals("1234", arr[0]);
		Assert.assertEquals("", arr[1]);
		PostgresReader.parseCompositeURI("123/3456", arr);
		Assert.assertEquals("123", arr[0]);
		Assert.assertEquals("3456", arr[1]);
		PostgresReader.parseCompositeURI("\\\\123\\/34/56", arr);
		Assert.assertEquals("\\123/34", arr[0]);
		Assert.assertEquals("56", arr[1]);
		try {
			PostgresReader.parseCompositeURI("123/34/56", arr);
			Assert.fail("Exception expected");
		}catch (IOException ex) {
			Assert.assertTrue(ex.getMessage().contains("Number of expected parts: 2"));
		}
	}

	@Test
	public void compositeUriCheck() throws IOException {
		StringBuilder sb = new StringBuilder();
		PostgresWriter.writeCompositeUri(sb, "ab\\\\cd/de''fg");
		Assert.assertEquals("('ab\\cd','de''''fg')", sb.toString());
	}

	@Test
	public void longIssue() throws IOException {
		PostgresReader reader = new PostgresReader();
		List<Long> longs = Arrays.asList(0L, 1L, -1L, Long.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE - 1, Long.MIN_VALUE + 1);
		PostgresTuple tuple = ArrayTuple.create(longs, LongConverter::toTuple);
		String value = tuple.buildTuple(false);
		reader.process(value);
		List<Long> result = LongConverter.parseCollection(reader, 0, false);
		Assert.assertEquals(longs, result);
	}

	@Test
	public void invalidUrl() throws IOException {
		try {
			Properties p = new Properties();
			p.setProperty("revenj.jdbcUrl", "jdbc:logging:postgres://localhost/db");
			Revenj.dataSource(p);
			Assert.fail("Expecting IOException");
		} catch (IOException ex) {
			Assert.assertTrue(ex.getMessage().contains("Invalid revenj.jdbcUrl provided. Expecting: 'jdbc:postgresql"));
		}
	}
}
