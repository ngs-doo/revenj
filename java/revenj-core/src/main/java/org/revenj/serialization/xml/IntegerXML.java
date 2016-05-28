package org.revenj.serialization.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import java.util.List;
import java.util.function.Function;

@XmlRootElement(name = "int")
class IntegerXML {
	@XmlValue
	public Integer value;

	public static final Function<Integer, IntegerXML> convert = s -> {
		IntegerXML xml = new IntegerXML();
		xml.value = s;
		return xml;
	};

	@XmlRootElement(name = "ArrayOfint")
	static class PrimitiveArray {
		@XmlElement(name = "int")
		public int[] value;

		public static final Function<int[], PrimitiveArray> convert = s -> {
			PrimitiveArray xml = new PrimitiveArray();
			xml.value = s;
			return xml;
		};
	}

	@XmlRootElement(name = "ArrayOfint")
	static class ObjectArray {
		@XmlElement(name = "int")
		public Integer[] value;

		public static final Function<Integer[], ObjectArray> convert = s -> {
			ObjectArray xml = new ObjectArray();
			xml.value = s;
			return xml;
		};
	}

	@XmlRootElement(name = "ArrayOfint")
	static class ListXML {
		@XmlElement(name = "int")
		public List<Integer> value;

		public static final Function<List<Integer>, ListXML> convert = s -> {
			ListXML xml = new ListXML();
			xml.value = s;
			return xml;
		};
	}
}