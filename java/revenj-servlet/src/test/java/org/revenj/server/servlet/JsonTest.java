package org.revenj.server.servlet;

import junit.framework.Assert;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class JsonTest {

	@Test
	public void dateFormat() throws IOException {
		JacksonSerialization jackson = new JacksonSerialization(null);
		String value = jackson.serialize(LocalDate.of(2015, 9, 26));
		Assert.assertEquals("\"2015-09-26\"", value);
	}

	@Test
	public void timestampFormat() throws IOException {
		JacksonSerialization jackson = new JacksonSerialization(null);
		String value = jackson.serialize(OffsetDateTime.of(LocalDateTime.of(2015, 9, 26, 1, 2, 3), ZoneOffset.ofHours(2)));
		Assert.assertEquals("\"2015-09-26T01:02:03+02:00\"", value);
	}
}
