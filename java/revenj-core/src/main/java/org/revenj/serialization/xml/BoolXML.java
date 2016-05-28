package org.revenj.serialization.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import java.util.List;
import java.util.function.Function;

@XmlRootElement(name = "bool")
class BoolXML {
	@XmlValue
	public Boolean value;

	public static final Function<Boolean, BoolXML> convert = s -> {
		BoolXML xml = new BoolXML();
		xml.value = s;
		return xml;
	};

	@XmlRootElement(name = "ArrayOfbool")
	static class PrimitiveArray {
		@XmlElement(name = "bool")
		public boolean[] value;

		public static final Function<boolean[], PrimitiveArray> convert = s -> {
			PrimitiveArray xml = new PrimitiveArray();
			xml.value = s;
			return xml;
		};
	}

	@XmlRootElement(name = "ArrayOfbool")
	static class ObjectArray {
		@XmlElement(name = "bool")
		public Boolean[] value;

		public static final Function<Boolean[], ObjectArray> convert = s -> {
			ObjectArray xml = new ObjectArray();
			xml.value = s;
			return xml;
		};
	}

	@XmlRootElement(name = "ArrayOfbool")
	static class ListXML {
		@XmlElement(name = "bool")
		public List<Boolean> value;

		public static final Function<List<Boolean>, ListXML> convert = s -> {
			ListXML xml = new ListXML();
			xml.value = s;
			return xml;
		};
	}
}