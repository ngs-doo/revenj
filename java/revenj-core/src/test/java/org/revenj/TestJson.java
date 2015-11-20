package org.revenj;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import org.junit.Assert;
import org.junit.Test;
import org.revenj.json.DslJsonSerialization;
import org.revenj.json.JavaTimeConverter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
		int value = (int)json.deserialize(int.class, is);
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
		String value = (String)json.deserialize(String.class, is);
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
}
