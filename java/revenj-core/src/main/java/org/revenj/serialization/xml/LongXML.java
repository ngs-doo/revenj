package org.revenj.serialization.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import java.util.List;
import java.util.function.Function;

@XmlRootElement(name = "long")
class LongXML {
	@XmlValue
	public Long value;

	public static final Function<Long, LongXML> convert = s -> {
		LongXML xml = new LongXML();
		xml.value = s;
		return xml;
	};

	@XmlRootElement(name = "ArrayOflong")
	static class PrimitiveArray {
		@XmlElement(name = "long")
		public long[] value;

		public static final Function<long[], PrimitiveArray> convert = s -> {
			PrimitiveArray xml = new PrimitiveArray();
			xml.value = s;
			return xml;
		};
	}

	@XmlRootElement(name = "ArrayOflong")
	static class ObjectArray {
		@XmlElement(name = "long")
		public Long[] value;

		public static final Function<Long[], ObjectArray> convert = s -> {
			ObjectArray xml = new ObjectArray();
			xml.value = s;
			return xml;
		};
	}

	@XmlRootElement(name = "ArrayOflong")
	static class ListXML {
		@XmlElement(name = "long")
		public List<Long> value;

		public static final Function<List<Long>, ListXML> convert = s -> {
			ListXML xml = new ListXML();
			xml.value = s;
			return xml;
		};
	}
}