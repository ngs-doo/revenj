package org.revenj.serialization.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.awt.*;
import java.util.List;
import java.util.function.Function;

@XmlRootElement(name = "Point")
class PointIntXML {
	@XmlElement
	public int x;
	@XmlElement
	public int y;

	public static final Function<Point, PointIntXML> convert = s -> {
		PointIntXML xml = new PointIntXML();
		xml.x = s.x;
		xml.y = s.y;
		return xml;
	};

	@XmlRootElement(name = "ArrayOfPoint")
	static class ArrayXML {
		@XmlElement(name = "Point")
		public Point[] value;

		public static final Function<Point[], ArrayXML> convert = s -> {
			ArrayXML xml = new ArrayXML();
			xml.value = s;
			return xml;
		};
	}

	@XmlRootElement(name = "ArrayOfPoint")
	static class ListXML {
		@XmlElement(name = "Point")
		public List<Point> value;

		public static final Function<List<Point>, ListXML> convert = s -> {
			ListXML xml = new ListXML();
			xml.value = s;
			return xml;
		};
	}
}