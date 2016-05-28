package org.revenj.serialization.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

@XmlRootElement(name = "decimal")
class DecimalXML {
	@XmlValue
	public BigDecimal value;

	public static final Function<BigDecimal, DecimalXML> convert = s -> {
		DecimalXML xml = new DecimalXML();
		xml.value = s;
		return xml;
	};

	@XmlRootElement(name = "ArrayOfdecimal")
	static class ArrayXML {
		@XmlElement(name = "decimal")
		public BigDecimal[] value;

		public static final Function<BigDecimal[], DecimalXML.ArrayXML> convert = s -> {
			ArrayXML xml = new ArrayXML();
			xml.value = s;
			return xml;
		};
	}

	@XmlRootElement(name = "ArrayOfdecimal")
	static class ListXML {
		@XmlElement(name = "decimal")
		public List<BigDecimal> value;

		public static final Function<List<BigDecimal>, ListXML> convert = s -> {
			ListXML xml = new ListXML();
			xml.value = s;
			return xml;
		};
	}
}