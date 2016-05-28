package org.revenj.serialization.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import java.util.List;
import java.util.function.Function;

@XmlRootElement(name = "double")
class DoubleXML {
	@XmlValue
	public Double value;

	public static final Function<Double, DoubleXML> convert = s -> {
		DoubleXML xml = new DoubleXML();
		xml.value = s;
		return xml;
	};

	@XmlRootElement(name = "ArrayOfdouble")
	static class PrimitiveArray {
		@XmlElement(name = "double")
		public double[] value;

		public static final Function<double[], DoubleXML.PrimitiveArray> convert = s -> {
			PrimitiveArray xml = new PrimitiveArray();
			xml.value = s;
			return xml;
		};
	}

	@XmlRootElement(name = "ArrayOfdouble")
	static class ObjectArray {
		@XmlElement(name = "double")
		public Double[] value;

		public static final Function<Double[], DoubleXML.ObjectArray> convert = s -> {
			ObjectArray xml = new ObjectArray();
			xml.value = s;
			return xml;
		};
	}

	@XmlRootElement(name = "ArrayOfdouble")
	static class ListXML {
		@XmlElement(name = "double")
		public List<Double> value;

		public static final Function<List<Double>, DoubleXML.ListXML> convert = s -> {
			ListXML xml = new ListXML();
			xml.value = s;
			return xml;
		};
	}
}