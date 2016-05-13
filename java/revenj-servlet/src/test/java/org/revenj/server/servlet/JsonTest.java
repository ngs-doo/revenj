package org.revenj.server.servlet;

import org.junit.Assert;
import org.junit.Test;
import org.revenj.TreePath;
import org.revenj.Utils;
import org.w3c.dom.Element;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

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

	static class WithXml {
		public String URI;
		public List<Element> xmls;
	}

	@Test
	public void xmlJacksonClass() throws IOException {
		final JacksonSerialization json = new JacksonSerialization(null, Optional.empty());
		byte[] bytes = "[null,{\"document\":null},{\"TextElement\":\"some text & \"},{\"ElementWithCData\":\"\"},{\"AtributedElement\":{\"@foo\":\"bar\",\"@qwe\":\"poi\"}},{\"NestedTextElement\":{\"FirstNest\":{\"SecondNest\":\"bird\"}}}]".getBytes();
		List<Element> xmls = (List<Element>) json.deserialize(Utils.makeGenericType(ArrayList.class, Element.class), bytes, bytes.length);
		WithXml instance = new WithXml();
		instance.URI = "abc";
		instance.xmls = xmls;
		String res = json.serialize(instance);
		WithXml deser = (WithXml) json.deserialize(WithXml.class, res.getBytes(), res.length());
		assertEquals(6, deser.xmls.size());
	}

	@Test
	public void treePath() throws IOException {
		final JacksonSerialization json = new JacksonSerialization(null, Optional.empty());
		List<TreePath> paths = Arrays.asList(TreePath.create("abc.def"), null, TreePath.create("a.b"), TreePath.EMPTY);
		String res = json.serialize(paths);
		List<TreePath> deser = (List<TreePath>) json.deserialize(Utils.makeGenericType(ArrayList.class, TreePath.class), res);
		assertEquals(deser, paths);
	}
}
