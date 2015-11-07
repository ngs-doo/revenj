package org.revenj;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import org.junit.Assert;
import org.junit.Test;
import org.revenj.json.DslJsonSerialization;
import org.revenj.json.JavaTimeConverter;
import org.revenj.postgres.PostgresReader;
import org.revenj.postgres.converters.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

public class TestJson {

	@Test
	public void dateTimeOffsetConversion() throws IOException {
		OffsetDateTime now = OffsetDateTime.now();
		JsonWriter jw = new JsonWriter();
		JavaTimeConverter.serialize(now, jw);
		JsonReader jr = new JsonReader(jw.toString().getBytes("UTF-8"), null);
		jr.read();
		OffsetDateTime value = JavaTimeConverter.deserializeDateTime(jr);
		Assert.assertEquals(now, value);
	}

	@Test
	public void localDateTimeConversion() throws IOException {
		LocalDateTime now = LocalDateTime.now();
		JsonWriter jw = new JsonWriter();
		JavaTimeConverter.serialize(now, jw);
		JsonReader jr = new JsonReader(jw.toString().getBytes("UTF-8"), null);
		jr.read();
		LocalDateTime value = JavaTimeConverter.deserializeLocalDateTime(jr);
		Assert.assertEquals(now, value);
	}

	@Test
	public void jsonSerialization() throws IOException {
		DslJsonSerialization json = new DslJsonSerialization(null);
		Assert.assertEquals("3", json.serialize(3));
		Assert.assertEquals(3, (int) json.deserialize("3", int.class));
	}
}
