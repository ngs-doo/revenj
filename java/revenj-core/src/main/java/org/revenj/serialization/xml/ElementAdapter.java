package org.revenj.serialization.xml;

import org.revenj.database.postgres.converters.XmlConverter;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ElementAdapter extends XmlAdapter<String, Element> {
	@Override
	public Element unmarshal(String v) throws Exception {
		return XmlConverter.stringToXml(v);
	}

	@Override
	public String marshal(Element v) throws Exception {
		return XmlConverter.xmlToString(v);
	}
}
