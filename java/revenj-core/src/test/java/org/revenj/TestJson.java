package org.revenj;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import org.junit.Assert;
import org.junit.Test;
import org.revenj.serialization.json.DslJsonSerialization;
import com.dslplatform.json.JavaTimeConverter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.*;
import java.util.*;

public class TestJson {

	@Test
	public void dateTimeOffsetConversion() throws IOException {
		OffsetDateTime now = OffsetDateTime.now();
		JsonWriter jw = new JsonWriter();
		JavaTimeConverter.serialize(now, jw);
		JsonReader jr = new JsonReader<>(jw.toString().getBytes("UTF-8"), null);
		jr.read();
		OffsetDateTime value = JavaTimeConverter.deserializeDateTime(jr);
		Assert.assertEquals(now, value);
	}

	@Test
	public void dateTimeOffsetUtcConversion() throws IOException {
		OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
		JsonWriter jw = new JsonWriter();
		JavaTimeConverter.serialize(now, jw);
		JsonReader jr = new JsonReader<>(jw.toString().getBytes("UTF-8"), null);
		jr.read();
		OffsetDateTime value = JavaTimeConverter.deserializeDateTime(jr);
		Assert.assertEquals(now, value);
	}

	@Test
	public void localDateTimeConversion() throws IOException {
		LocalDateTime now = LocalDateTime.now();
		JsonWriter jw = new JsonWriter();
		JavaTimeConverter.serialize(now, jw);
		JsonReader jr = new JsonReader<>(jw.toString().getBytes("UTF-8"), null);
		jr.read();
		LocalDateTime value = JavaTimeConverter.deserializeLocalDateTime(jr);
		Assert.assertEquals(now, value);
	}

	@Test
	public void jsonSerialization() throws IOException {
		DslJsonSerialization json = new DslJsonSerialization(null, Optional.empty());
		Assert.assertEquals("3", json.serialize(3));
		Assert.assertEquals(3, (int) json.deserialize("3", int.class));
	}

	@Test
	public void jsonSimpleStreamTest() throws IOException {
		DslJsonSerialization json = new DslJsonSerialization(null, Optional.empty());
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		json.serialize(42, os);
		byte[] result = os.toByteArray();
		Assert.assertEquals(2, result.length);
		Assert.assertEquals('4', result[0]);
		Assert.assertEquals('2', result[1]);
		ByteArrayInputStream is = new ByteArrayInputStream(result);
		int value = json.deserialize(int.class, is, new byte[512]);
		Assert.assertEquals(42, value);
	}

	@Test
	public void json512LimitStreamTest() throws IOException {
		DslJsonSerialization json = new DslJsonSerialization(null, Optional.empty());
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		json.serialize(new String(new char[512]).replace('\0', 'x'), os);
		byte[] result = os.toByteArray();
		Assert.assertEquals(514, result.length);
		Assert.assertEquals('"', result[0]);
		for(int i = 1; i < 513; i++) {
			Assert.assertEquals('x', result[i]);
		}
		Assert.assertEquals('"', result[513]);
		ByteArrayInputStream is = new ByteArrayInputStream(result);
		String value = json.deserialize(String.class, is, new byte[512]);
		Assert.assertEquals(new String(new char[512]).replace('\0', 'x'), value);
	}

	@Test
	public void dateFormat() throws IOException {
		DslJsonSerialization json = new DslJsonSerialization(null, Optional.empty());
		String value = json.serialize(LocalDate.of(2015, 9, 6));
		Assert.assertEquals("\"2015-09-06\"", value);
		LocalDate date = json.deserialize("\"2015-9-6\"", LocalDate.class);
		Assert.assertEquals(LocalDate.of(2015, 9, 6), date);
		date = json.deserialize("\"2015-9-16\"", LocalDate.class);
		Assert.assertEquals(LocalDate.of(2015, 9, 16), date);
		date = json.deserialize("\"2015-10-6\"", LocalDate.class);
		Assert.assertEquals(LocalDate.of(2015, 10, 6), date);
	}

	@Test
	public void canSerializeSqlDate() throws IOException {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("number", 42);
		result.put("min", java.sql.Date.valueOf(LocalDate.of(2015, 11, 25)));
		result.put("max", java.sql.Date.valueOf(LocalDate.of(2015, 11, 26)));
		List<Map<String, Object>> list = new ArrayList<>();
		list.add(result);
		DslJsonSerialization json = new DslJsonSerialization(null, Optional.empty());
		String serialized = json.serialize(list);
		Assert.assertEquals("[{\"number\":42,\"min\":\"2015-11-25\",\"max\":\"2015-11-26\"}]", serialized);
	}

	@Test
	public void dateConversion() throws IOException {
		DslJsonSerialization json = new DslJsonSerialization(null, Optional.empty());
		OffsetDateTime odt = OffsetDateTime.now();
		java.util.Date date = java.util.Date.from(odt.toInstant());
		String serialized = json.serialize(date);
		java.util.Date deserialized = json.deserialize(serialized, java.util.Date.class);
		Assert.assertEquals(deserialized, date);
		Assert.assertEquals(deserialized.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime(), odt);
	}

	@Test
	public void timestampConversion() throws IOException {
		DslJsonSerialization json = new DslJsonSerialization(null, Optional.empty());
		OffsetDateTime odt = OffsetDateTime.now();
		java.sql.Timestamp ts = java.sql.Timestamp.from(odt.toInstant());
		String serialized = json.serialize(ts);
		java.sql.Timestamp deserialized = json.deserialize(serialized, java.sql.Timestamp.class);
		Assert.assertEquals(deserialized, ts);
		Assert.assertEquals(deserialized.toInstant(), odt.toInstant());
	}

	@Test
	public void treePathConversion() throws IOException {
		DslJsonSerialization json = new DslJsonSerialization(null, Optional.empty());
		TreePath tp = TreePath.create("abc.def");
		String serialized = json.serialize(tp);
		TreePath deserialized = json.deserialize(serialized, TreePath.class);
		Assert.assertEquals(deserialized, tp);
	}
}
