package org.revenj.serialization.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import java.util.List;
import java.util.function.Function;

@XmlRootElement(name = "short")
class ShortXML {
	@XmlValue
	public Short value;

	public static final Function<Short, ShortXML> convert = s -> {
		ShortXML xml = new ShortXML();
		xml.value = s;
		return xml;
	};

	@XmlRootElement(name = "ArrayOfshort")
	static class PrimitiveArray {
		@XmlElement(name = "short")
		public short[] value;

		public static final Function<short[], PrimitiveArray> convert = s -> {
			PrimitiveArray xml = new PrimitiveArray();
			xml.value = s;
			return xml;
		};
	}

	@XmlRootElement(name = "ArrayOfshort")
	static class ObjectArray {
		@XmlElement(name = "short")
		public Short[] value;

		public static final Function<Short[], ObjectArray> convert = s -> {
			ObjectArray xml = new ObjectArray();
			xml.value = s;
			return xml;
		};
	}

	@XmlRootElement(name = "ArrayOfshort")
	static class ListXML {
		@XmlElement(name = "short")
		public List<Short> value;

		public static final Function<List<Short>, ListXML> convert = s -> {
			ListXML xml = new ListXML();
			xml.value = s;
			return xml;
		};
	}
}