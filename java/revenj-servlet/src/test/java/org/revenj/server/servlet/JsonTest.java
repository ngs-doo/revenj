package org.revenj.server.servlet;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Element;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

public class JsonTest {

	@Test
	public void dateFormat() throws IOException {
		JacksonSerialization jackson = new JacksonSerialization(null, Optional.empty());
		String value = jackson.serialize(LocalDate.of(2015, 9, 26));
		Assert.assertEquals("\"2015-09-26\"", value);
	}

	@Test
	public void timestampFormat() throws IOException {
		JacksonSerialization jackson = new JacksonSerialization(null, Optional.empty());
		String value = jackson.serialize(OffsetDateTime.of(LocalDateTime.of(2015, 9, 26, 1, 2, 3), ZoneOffset.ofHours(2)));
		Assert.assertEquals("\"2015-09-26T01:02:03+02:00\"", value);
	}

	@Test
	public void jacksonPoint() throws IOException {
		JacksonSerialization jackson = new JacksonSerialization(null, Optional.empty());
		java.awt.Point value = jackson.deserialize("{}", java.awt.Point.class);
		Assert.assertEquals(0, value.x);
		Assert.assertEquals(0, value.y);
		value = jackson.deserialize("{\"x\":1,\"y\":2}", java.awt.Point.class);
		Assert.assertEquals(1, value.x);
		Assert.assertEquals(2, value.y);
		value = jackson.deserialize("\"3,4\"", java.awt.Point.class);
		Assert.assertEquals(3, value.x);
		Assert.assertEquals(4, value.y);
	}

	@Test
	public void jacksonXml() throws IOException {
		JacksonSerialization jackson = new JacksonSerialization(null, Optional.empty());
		Element value = jackson.deserialize("{\"root\":null}", Element.class);
		Assert.assertEquals("root", value.getNodeName());
		value = jackson.deserialize("\"<root/>\"", Element.class);
		Assert.assertEquals("root", value.getNodeName());
	}
}
