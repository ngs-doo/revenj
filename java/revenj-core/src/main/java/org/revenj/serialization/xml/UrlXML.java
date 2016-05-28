package org.revenj.serialization.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import java.net.URL;
import java.util.List;
import java.util.function.Function;

@XmlRootElement(name = "anyURI")
class UrlXML {
	@XmlValue
	public URL value;

	public static final Function<URL, UrlXML> convert = s -> {
		UrlXML xml = new UrlXML();
		xml.value = s;
		return xml;
	};

	@XmlRootElement(name = "ArrayOfanyURI")
	static class ArrayXML {
		@XmlElement(name = "anyURI")
		public URL[] value;

		public static final Function<URL[], ArrayXML> convert = s -> {
			ArrayXML xml = new ArrayXML();
			xml.value = s;
			return xml;
		};
	}

	@XmlRootElement(name = "ArrayOfanyURI")
	static class ListXML {
		@XmlElement(name = "anyURI")
		public List<URL> value;

		public static final Function<List<URL>, ListXML> convert = s -> {
			ListXML xml = new ListXML();
			xml.value = s;
			return xml;
		};
	}
}