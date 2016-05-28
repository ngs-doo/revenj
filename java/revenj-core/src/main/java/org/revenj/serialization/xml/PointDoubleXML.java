package org.revenj.serialization.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@XmlRootElement(name = "Point")
class PointDoubleXML {
	@XmlElement
	public double x;
	@XmlElement
	public double y;

	static PointDoubleXML create(Point2D point) {
		if (point == null) return null;
		PointDoubleXML value = new PointDoubleXML();
		value.x = point.getX();
		value.y = point.getY();
		return value;
	}

	static Point2D create(PointDoubleXML point) {
		if (point == null) return null;
		return new Point2D.Double(point.x, point.y);
	}

	public static final Function<Point2D, PointDoubleXML> convert = s -> {
		PointDoubleXML xml = new PointDoubleXML();
		xml.x = s.getX();
		xml.y = s.getY();
		return xml;
	};

	@XmlRootElement(name = "ArrayOfPoint")
	static class ArrayXML {
		@XmlElement(name = "Point")
		public PointDoubleXML[] value;

		public static final Function<Point2D[], ArrayXML> convert = s -> {
			ArrayXML xml = new ArrayXML();
			xml.value = new PointDoubleXML[s.length];
			for (int i = 0; i < s.length; i++) {
				xml.value[i] = create(s[i]);
			}
			return xml;
		};

		public static final Function<ArrayXML, Point2D[]> parse = s -> {
			Point2D[] result = new Point2D[s.value.length];
			for (int i = 0; i < s.value.length; i++) {
				result[i] = create(s.value[i]);
			}
			return result;
		};
	}

	@XmlRootElement(name = "ArrayOfPoint")
	static class ListXML {
		@XmlElement(name = "Point")
		public List<PointDoubleXML> value;

		public static final Function<List<Point2D>, ListXML> convert = s -> {
			ListXML xml = new ListXML();
			xml.value = new ArrayList<>(s.size());
			for (Point2D it : s) {
				xml.value.add(create(it));
			}
			return xml;
		};

		public static final Function<ListXML, List<Point2D>> parse = s -> {
			List<Point2D> result = new ArrayList<>(s.value.size());
			for (int i = 0; i < s.value.size(); i++) {
				result.add(create(s.value.get(i)));
			}
			return result;
		};
	}
}