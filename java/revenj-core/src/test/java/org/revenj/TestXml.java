package org.revenj;

import org.junit.Assert;
import org.junit.Test;
import org.revenj.serialization.xml.JaxbConfiguration;
import org.revenj.serialization.xml.XmlJaxbSerialization;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.awt.*;
import java.awt.geom.Point2D;
import java.math.BigDecimal;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class TestXml {

	private static String toString(Element element) {
		DOMImplementationLS lsImpl = (DOMImplementationLS) element.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
		LSSerializer serializer = lsImpl.createLSSerializer();
		serializer.getDomConfig().setParameter("xml-declaration", false); //by default its true, so set it to false to get String without xml-declaration
		return serializer.writeToString(element);
	}

	private final XmlJaxbSerialization xml = new XmlJaxbSerialization(new JaxbConfiguration[0]);

	@Test
	public void stringConversion() throws Exception {
		Element elem = xml.serialize("abc");
		Assert.assertEquals("<string>abc</string>", toString(elem));
		String value = xml.deserialize(elem, String.class);
		Assert.assertEquals("abc", value);
	}

	@Test
	public void intConversion() throws Exception {
		Element elem = xml.serialize(123);
		Assert.assertEquals("<int>123</int>", toString(elem));
		int value = xml.deserialize(elem, int.class);
		Assert.assertEquals(123, value);
	}

	@Test
	public void decimalNullWithTypeConversion() throws Exception {
		Element elem = xml.serialize(BigDecimal.class, null);
		Assert.assertEquals("<decimal/>", toString(elem));
		BigDecimal value = xml.deserialize(elem, BigDecimal.class);
		Assert.assertNull(value);
	}

	@Test
	public void decimalNullConversion() throws Exception {
		Element elem = xml.serialize(null);
		Assert.assertEquals("<object nil=\"true\"/>", toString(elem));
		BigDecimal value = xml.deserialize(elem, BigDecimal.class);
		Assert.assertNull(value);
	}

	@Test
	public void emptyStringConversion() throws Exception {
		Element elem = xml.serialize("");
		Assert.assertEquals("<string></string>", toString(elem));
		String value = xml.deserialize(elem, String.class);
		Assert.assertEquals("", value);
	}

	@Test
	public void stringListConversion() throws Exception {
		Element elem = xml.serialize(Utils.makeGenericType(List.class, String.class), Arrays.asList("abc", "def"));
		Assert.assertEquals("<ArrayOfstring><string>abc</string><string>def</string></ArrayOfstring>", toString(elem));
		List<String> value = xml.deserialize(elem, List.class, String.class);
		Assert.assertEquals(2, value.size());
		Assert.assertEquals("abc", value.get(0));
		Assert.assertEquals("def", value.get(1));
	}

	@Test
	public void stringArrayListConversion() throws Exception {
		Element elem = xml.serialize(Arrays.asList("abc", "def"));
		Assert.assertEquals("<ArrayOfstring><string>abc</string><string>def</string></ArrayOfstring>", toString(elem));
		List<String> value = xml.deserialize(elem, List.class, String.class);
		Assert.assertEquals(2, value.size());
		Assert.assertEquals("abc", value.get(0));
		Assert.assertEquals("def", value.get(1));
	}

	@Test
	public void stringCollectionConversion() throws Exception {
		Element elem = xml.serialize(String[].class, new String[]{"abc", "def"});
		Assert.assertEquals("<ArrayOfstring><string>abc</string><string>def</string></ArrayOfstring>", toString(elem));
		String[] value = xml.deserialize(elem, String[].class);
		Assert.assertEquals(2, value.length);
		Assert.assertEquals("abc", value[0]);
		Assert.assertEquals("def", value[1]);
	}

	@XmlRootElement(name = "POJO")
	static class POJO {
		private long num;
		private int[] ints;
		private List<String> strings;

		public long getNum() {
			return num;
		}

		public POJO setNum(long value) {
			num = value;
			return this;
		}

		@XmlElementWrapper(name = "ints")
		@XmlElement(nillable = true, name = "int", namespace = "http://schemas.microsoft.com/2003/10/Serialization/Arrays")
		public int[] getInts() {
			return ints;
		}

		public POJO setInts(int[] value) {
			ints = value;
			return this;
		}

		@XmlElementWrapper(name = "strings")
		@XmlElement(nillable = false, name = "string", namespace = "http://schemas.microsoft.com/2003/10/Serialization/Arrays")
		public List<String> getStrings() {
			return strings;
		}

		public POJO setStrings(List<String> value) {
			strings = value;
			return this;
		}
	}

	@Test
	public void pojoConversion() throws Exception {
		xml.register(POJO.class);
		POJO pojo = new POJO().setInts(new int[]{1, 2, 3}).setStrings(Arrays.asList("abc", "def")).setNum(5);
		Element elem = xml.serialize(POJO.class, pojo);
		Assert.assertEquals("<POJO xmlns:ns2=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\">" +
				"<ints><ns2:int>1</ns2:int><ns2:int>2</ns2:int><ns2:int>3</ns2:int></ints>" +
				"<num>5</num>" +
				"<strings><ns2:string>abc</ns2:string><ns2:string>def</ns2:string></strings>" +
				"</POJO>", toString(elem));
		POJO value = xml.deserialize(elem, POJO.class);
		Assert.assertArrayEquals(pojo.getInts(), value.getInts());
		Assert.assertEquals(pojo.getStrings(), value.getStrings());
	}

	@Test
	public void dateConversion() throws Exception {
		LocalDate today = LocalDate.now();
		Element elem = xml.serialize(today);
		Assert.assertEquals("<dateTime>" + today.toString() + "</dateTime>", toString(elem));
		LocalDate value = xml.deserialize(elem, LocalDate.class);
		Assert.assertEquals(today, value);
	}

	@Test
	public void timestampArrayConversion() throws Exception {
		OffsetDateTime now = OffsetDateTime.now();
		OffsetDateTime soon = OffsetDateTime.now().plusHours(2);
		Element elem = xml.serialize(new OffsetDateTime[]{now, soon});
		Assert.assertEquals("<ArrayOfdateTime><dateTime>" + now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
				+ "</dateTime><dateTime>" + soon.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
				+ "</dateTime></ArrayOfdateTime>", toString(elem));
		OffsetDateTime[] value = xml.deserialize(elem, OffsetDateTime[].class);
		Assert.assertArrayEquals(new OffsetDateTime[]{now, soon}, value);
	}

	@Test
	public void uuidListConversion() throws Exception {
		UUID one = new UUID(0, 1);
		UUID two = new UUID(1, 2);
		List<UUID> list = Arrays.asList(one, two);
		Element elem = xml.serialize(Utils.makeGenericType(List.class, UUID.class), list);
		Assert.assertEquals("<ArrayOfguid><guid>" + one.toString() + "</guid><guid>" + two.toString() + "</guid></ArrayOfguid>", toString(elem));
		List<UUID> value = xml.deserialize(elem, List.class, UUID.class);
		Assert.assertEquals(list, value);
	}

	@Test
	public void dateArrayConversion() throws Exception {
		LocalDate today = LocalDate.now();
		LocalDate soon = LocalDate.now().plusDays(2);
		Element elem = xml.serialize(new LocalDate[]{today, soon});
		Assert.assertEquals("<ArrayOfdateTime><dateTime>" + today.toString() + "</dateTime><dateTime>" + soon.toString() + "</dateTime></ArrayOfdateTime>", toString(elem));
		LocalDate[] value = xml.deserialize(elem, LocalDate[].class);
		Assert.assertArrayEquals(new LocalDate[]{today, soon}, value);
	}

	@Test
	public void pointConversion() throws Exception {
		Point point = new Point(2, 3);
		Element elem = xml.serialize(point);
		Assert.assertEquals("<Point><x>" + point.x + "</x><y>" + point.y + "</y></Point>", toString(elem));
		Point value = xml.deserialize(elem, Point.class);
		Assert.assertEquals(point, value);
	}

	@Test
	public void pointListConversion() throws Exception {
		Point2D point1 = new Point2D.Double(2, 3);
		Point2D point2 = new Point2D.Double(3, 4);
		List<Point2D> list = Arrays.asList(point1, point2);
		Element elem = xml.serialize(Utils.makeGenericType(List.class, Point2D.class), list);
		Assert.assertEquals("<ArrayOfPoint><Point><x>" + point1.getX() + "</x><y>" + point1.getY() + "</y></Point><Point><x>" + point2.getX() + "</x><y>" + point2.getY() + "</y></Point></ArrayOfPoint>", toString(elem));
		List<Point2D> value = xml.deserialize(elem, List.class, Point2D.class);
		Assert.assertEquals(list, value);
	}

	@Test
	public void treePathConversion() throws Exception {
		TreePath path = TreePath.create("1.2.3");
		Element elem = xml.serialize(path);
		Assert.assertEquals("<TreePath>" + path.toString() + "</TreePath>", toString(elem));
		TreePath value = xml.deserialize(elem, TreePath.class);
		Assert.assertEquals(path, value);
	}

	@Test
	public void treePathArrayConversion() throws Exception {
		TreePath path1 = TreePath.create("1.2.3");
		TreePath path2 = TreePath.create("aa.bbb.ccc");
		Element elem = xml.serialize(new TreePath[]{path1, path2});
		Assert.assertEquals("<ArrayOfTreePath><TreePath>" + path1.toString() + "</TreePath><TreePath>" + path2.toString() + "</TreePath></ArrayOfTreePath>", toString(elem));
		TreePath[] value = xml.deserialize(elem, TreePath[].class);
		Assert.assertArrayEquals(new TreePath[]{path1, path2}, value);
	}

	@Test
	public void urlConversion() throws Exception {
		URL url = new URL("http://dsl-platform.com");
		Element elem = xml.serialize(url);
		Assert.assertEquals("<anyURI>http://dsl-platform.com</anyURI>", toString(elem));
		URL value = xml.deserialize(elem, URL.class);
		Assert.assertEquals(url, value);
	}
}
