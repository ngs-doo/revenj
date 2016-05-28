package org.revenj.serialization.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import java.util.List;
import java.util.function.Function;

@XmlRootElement(name = "float")
class FloatXML {
	@XmlValue
	public Float value;

	public static final Function<Float, FloatXML> convert = s -> {
		FloatXML xml = new FloatXML();
		xml.value = s;
		return xml;
	};

	@XmlRootElement(name = "ArrayOffloat")
	static class PrimitiveArray {
		@XmlElement(name = "float")
		public float[] value;

		public static final Function<float[], FloatXML.PrimitiveArray> convert = s -> {
			PrimitiveArray xml = new PrimitiveArray();
			xml.value = s;
			return xml;
		};
	}

	@XmlRootElement(name = "ArrayOffloat")
	static class ObjectArray {
		@XmlElement(name = "float")
		public Float[] value;

		public static final Function<Float[], FloatXML.ObjectArray> convert = s -> {
			ObjectArray xml = new ObjectArray();
			xml.value = s;
			return xml;
		};
	}

	@XmlRootElement(name = "ArrayOffloat")
	static class ListXML {
		@XmlElement(name = "float")
		public List<Float> value;

		public static final Function<List<Float>, FloatXML.ListXML> convert = s -> {
			ListXML xml = new ListXML();
			xml.value = s;
			return xml;
		};
	}
}