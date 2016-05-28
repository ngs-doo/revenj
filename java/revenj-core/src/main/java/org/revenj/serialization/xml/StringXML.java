package org.revenj.serialization.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import java.util.List;
import java.util.function.Function;

@XmlRootElement(name = "string")
class StringXML {
	@XmlValue
	public String value;

	public static final Function<String, StringXML> convert = s -> {
		StringXML xml = new StringXML();
		xml.value = s;
		return xml;
	};

	@XmlRootElement(name = "ArrayOfstring")
	static class ArrayXML {
		@XmlElement(name = "string")
		public String[] value;

		public static final Function<String[], ArrayXML> convert = s -> {
			ArrayXML xml = new ArrayXML();
			xml.value = s;
			return xml;
		};
	}

	@XmlRootElement(name = "ArrayOfstring")
	static class ListXML {
		@XmlElement(name = "string")
		public List<String> value;

		public static final Function<List<String>, ListXML> convert = s -> {
			ListXML xml = new ListXML();
			xml.value = s;
			return xml;
		};
	}
}