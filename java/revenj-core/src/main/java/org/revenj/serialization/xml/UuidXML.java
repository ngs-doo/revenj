package org.revenj.serialization.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@XmlRootElement(name = "guid")
class UuidXML {
	@XmlValue
	public UUID value;

	public static final Function<UUID, UuidXML> convert = s -> {
		UuidXML xml = new UuidXML();
		xml.value = s;
		return xml;
	};

	@XmlRootElement(name = "ArrayOfguid")
	static class ArrayXML {
		@XmlElement(name = "guid")
		public UUID[] value;

		public static final Function<UUID[], ArrayXML> convert = s -> {
			ArrayXML xml = new ArrayXML();
			xml.value = s;
			return xml;
		};
	}

	@XmlRootElement(name = "ArrayOfguid")
	static class ListXML {
		@XmlElement(name = "guid")
		public List<UUID> value;

		public static final Function<List<UUID>, ListXML> convert = s -> {
			ListXML xml = new ListXML();
			xml.value = s;
			return xml;
		};
	}
}