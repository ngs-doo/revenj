package org.revenj.database.postgres.converters;

import org.revenj.database.postgres.PostgresBuffer;
import org.revenj.database.postgres.PostgresReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.ArrayList;
import java.util.List;

public abstract class XmlConverter {

	private static DocumentBuilder documentBuilder;

	static {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			documentBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public static String xmlToString(Element value) {
		Document document = value.getOwnerDocument();
		DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation();
		LSSerializer serializer = domImplLS.createLSSerializer();
		LSOutput lsOutput = domImplLS.createLSOutput();
		lsOutput.setEncoding("UTF-8");
		StringWriter writer = new StringWriter();
		lsOutput.setCharacterStream(writer);
		serializer.write(value, lsOutput);
		return writer.toString();
	}

	public static void serializeURI(PostgresBuffer sw, Element value) {
		if (value == null) return;
		sw.addToBuffer(xmlToString(value));
	}

	public static void setParameter(PostgresBuffer sw, PreparedStatement ps, int index, Element value) throws SQLException {
		if (value != null) {
			SQLXML xml = ps.getConnection().createSQLXML();
			xml.setString(xmlToString(value));
			ps.setSQLXML(index, xml);
		} else {
			ps.setSQLXML(index, null);
		}
	}

	public static Element parse(PostgresReader reader, int context) throws IOException {
		String value = StringConverter.parse(reader, context, true);
		if (value == null) return null;
		return stringToXml(value);
	}

	public static Element stringToXml(String value) throws IOException {
		if (value.length() == 0) return null;
		try {
			InputSource source = new InputSource(new StringReader(value));
			return documentBuilder.parse(source).getDocumentElement();
		} catch (SAXException ex) {
			throw new IOException(ex);
		}
	}

	public static List<Element> parseCollection(PostgresReader reader, int context) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return null;
		}
		boolean escaped = cur != '{';
		if (escaped) {
			reader.read(context);
		}
		cur = reader.peek();
		if (cur == '}') {
			if (escaped) {
				reader.read(context + 2);
			} else {
				reader.read(2);
			}
			return new ArrayList<>(0);
		}
		int innerContext = context << 1;
		List<Element> list = new ArrayList<>();
		do {
			cur = reader.read();
			if (cur == '"' || cur == '\\') {
				list.add(stringToXml(StringConverter.parseEscapedString(reader, innerContext, '}')));
				cur = reader.last();
			} else {
				reader.initBuffer((char) cur);
				reader.fillUntil(',', '}');
				cur = reader.read();
				if (reader.bufferMatches("NULL")) {
					list.add(null);
				} else {
					list.add(stringToXml(reader.bufferToString()));
				}
			}
		} while (cur == ',');
		if (escaped) {
			reader.read(context + 1);
		} else {
			reader.read();
		}
		return list;
	}

	public static PostgresTuple toTuple(Element value) {
		if (value == null) return null;
		return ValueTuple.from(xmlToString(value));
	}
}
