package org.revenj.server.servlet;

import com.dslplatform.json.XmlConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.revenj.TreePath;
import org.revenj.serialization.Serialization;
import org.revenj.patterns.ServiceLocator;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Optional;

final class JacksonSerialization implements Serialization<String> {

	private final ObjectMapper mapper;

	public JacksonSerialization(ServiceLocator locator, Optional<ObjectMapper> jackson) {
		mapper = jackson.orElse(new ObjectMapper())
				.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true)
				.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.setInjectableValues(new InjectableValues.Std().addValue("__locator", locator))
				.registerModule(new Jdk8Module())
				.registerModule(new JavaTimeModule())
				.registerModule(withCustomSerializers())
				.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
	}

	private static final ThreadLocal<DocumentBuilder> documentBuilder = new ThreadLocal<DocumentBuilder>() {
		@Override
		public DocumentBuilder initialValue() {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			try {
				return dbFactory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				throw new RuntimeException(e);
			}
		}
	};

	private static SimpleModule withCustomSerializers() {
		SimpleModule module = new SimpleModule();
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		} catch (TransformerConfigurationException e) {
			throw new RuntimeException(e);
		}

		module.addSerializer(Element.class, new JsonSerializer<Element>() {
			@Override
			public void serialize(Element element, JsonGenerator gen, SerializerProvider unused) throws IOException {
				StringWriter writer = new StringWriter();
				try {
					transformer.transform(new DOMSource(element.getOwnerDocument()), new StreamResult(writer));
				} catch (TransformerException e) {
					throw new IOException(e);
				}
				gen.writeString(writer.toString());
			}
		});
		module.addSerializer(java.awt.Point.class, new JsonSerializer<java.awt.Point>() {
			@Override
			public void serialize(final java.awt.Point value, final JsonGenerator jg, final SerializerProvider unused) throws IOException {
				jg.writeStartObject();
				jg.writeNumberField("X", value.x);
				jg.writeNumberField("Y", value.y);
				jg.writeEndObject();
			}
		});
		module.addSerializer(java.awt.geom.Point2D.class, new JsonSerializer<java.awt.geom.Point2D>() {
			@Override
			public void serialize(final java.awt.geom.Point2D value, final JsonGenerator jg, final SerializerProvider unused) throws IOException {
				jg.writeStartObject();
				jg.writeNumberField("X", value.getX());
				jg.writeNumberField("Y", value.getY());
				jg.writeEndObject();
			}
		});
		module.addSerializer(java.awt.geom.Rectangle2D.class, new JsonSerializer<java.awt.geom.Rectangle2D>() {
			@Override
			public void serialize(final java.awt.geom.Rectangle2D rect, final JsonGenerator jg, final SerializerProvider unused) throws IOException {
				jg.writeStartObject();
				jg.writeNumberField("X", rect.getX());
				jg.writeNumberField("Y", rect.getY());
				jg.writeNumberField("Width", rect.getWidth());
				jg.writeNumberField("Height", rect.getHeight());
				jg.writeEndObject();
			}
		});
		module.addSerializer(java.awt.image.BufferedImage.class, new JsonSerializer<java.awt.image.BufferedImage>() {
			@Override
			public void serialize(final java.awt.image.BufferedImage image, final JsonGenerator jg, final SerializerProvider _unused) throws IOException {
				final ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(image, "png", baos);
				jg.writeBinary(baos.toByteArray());
			}
		});
		module.addSerializer(TreePath.class, new JsonSerializer<TreePath>() {
			@Override
			public void serialize(final TreePath path, final JsonGenerator jg, final SerializerProvider _unused) throws IOException {
				jg.writeString(path.toString());
			}
		});
		module.addDeserializer(Element.class, new JsonDeserializer<Element>() {
			@Override
			public Element deserialize(JsonParser parser, DeserializationContext unused) throws IOException {
				if (parser.getCurrentToken() == JsonToken.VALUE_STRING) {
					try {
						byte[] content = parser.getValueAsString().getBytes("UTF-8");
						return documentBuilder.get().parse(new ByteArrayInputStream(content)).getDocumentElement();
					} catch (SAXException ex) {
						throw new IOException(ex);
					}
				}
				@SuppressWarnings("unchecked")
				final HashMap<String, Object> map = parser.readValueAs(HashMap.class);
				return map == null ? null : XmlConverter.mapToXml(map);
			}
		});
		module.addDeserializer(java.awt.Point.class, new JsonDeserializer<java.awt.Point>() {
			@Override
			public java.awt.Point deserialize(final JsonParser parser, final DeserializationContext unused) throws IOException {
				if (parser.getCurrentToken() == JsonToken.VALUE_STRING) {
					String[] parts = parser.getValueAsString().split(",");
					if (parts.length == 2) {
						return new java.awt.Point(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
					}
					throw new IOException("Unable to parse \"number,number\" format for point");
				}
				final JsonNode tree = parser.getCodec().readTree(parser);
				JsonNode x = tree.get("X");
				if (x == null) x = tree.get("x");
				JsonNode y = tree.get("Y");
				if (y == null) y = tree.get("y");
				return new java.awt.Point(x != null ? x.asInt() : 0, y != null ? y.asInt() : 0);
			}
		});
		module.addDeserializer(java.awt.geom.Point2D.class, new JsonDeserializer<java.awt.geom.Point2D>() {
			@Override
			public java.awt.geom.Point2D deserialize(final JsonParser parser, final DeserializationContext unused) throws IOException {
				if (parser.getCurrentToken() == JsonToken.VALUE_STRING) {
					String[] parts = parser.getValueAsString().split(",");
					if (parts.length == 2) {
						return new java.awt.geom.Point2D.Double(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
					}
					throw new IOException("Unable to parse \"number,number\" format for point");
				}
				final JsonNode tree = parser.getCodec().readTree(parser);
				JsonNode x = tree.get("X");
				if (x == null) x = tree.get("x");
				JsonNode y = tree.get("Y");
				if (y == null) y = tree.get("y");
				return new java.awt.geom.Point2D.Double(x != null ? x.asDouble() : 0, y != null ? y.asDouble() : 0);
			}
		});
		module.addDeserializer(java.awt.geom.Rectangle2D.class, new JsonDeserializer<java.awt.geom.Rectangle2D>() {
			@Override
			public java.awt.geom.Rectangle2D deserialize(final JsonParser parser, final DeserializationContext _unused) throws IOException {
				if (parser.getCurrentToken() == JsonToken.VALUE_STRING) {
					String[] parts = parser.getValueAsString().split(",");
					if (parts.length == 4) {
						return new java.awt.geom.Rectangle2D.Double(
								Double.parseDouble(parts[0]),
								Double.parseDouble(parts[1]),
								Double.parseDouble(parts[2]),
								Double.parseDouble(parts[3]));
					}
					throw new IOException("Unable to parse \"number,number,number,number\" format for rectangle");
				}
				final JsonNode tree = parser.getCodec().readTree(parser);
				JsonNode x = tree.get("X");
				if (x == null) x = tree.get("x");
				JsonNode y = tree.get("Y");
				if (y == null) y = tree.get("y");
				JsonNode width = tree.get("Width");
				if (width == null) width = tree.get("width");
				JsonNode height = tree.get("Height");
				if (height == null) height = tree.get("height");
				return new java.awt.geom.Rectangle2D.Double(
						x != null ? x.asDouble() : 0,
						y != null ? y.asDouble() : 0,
						width != null ? width.asDouble() : 0,
						height != null ? height.asDouble() : 0);
			}
		});
		module.addDeserializer(java.awt.image.BufferedImage.class, new JsonDeserializer<java.awt.image.BufferedImage>() {
			@Override
			public java.awt.image.BufferedImage deserialize(final JsonParser parser, final DeserializationContext _unused) throws IOException {
				final InputStream is = new ByteArrayInputStream(parser.getBinaryValue());
				return ImageIO.read(is);
			}
		});
		module.addDeserializer(TreePath.class, new JsonDeserializer<TreePath>() {
			@Override
			public TreePath deserialize(final JsonParser parser, final DeserializationContext _unused) throws IOException {
				return TreePath.create(parser.getValueAsString());
			}
		});
		return module;
	}

	byte[] serializeAsBytes(Object value) throws IOException {
		return mapper.writeValueAsBytes(value);
	}

	void serializeTo(Object value, OutputStream stream) throws IOException {
		mapper.writeValue(stream, value);
	}

	@Override
	public String serialize(Type type, Object value) throws IOException {
		if (value == null) return "null";
		JavaType javaType = mapper.getTypeFactory().constructType(type == null ? value.getClass() : type);
		return mapper.writerFor(javaType).writeValueAsString(value);
	}

	public void serialize(Object value, OutputStream stream) throws IOException {
		mapper.writeValue(stream, value);
	}

	Object deserialize(Type type, byte[] content, int length) throws IOException {
		JavaType javaType = mapper.getTypeFactory().constructType(type);
		return mapper.readValue(content, 0, length, javaType);
	}

	public Object deserialize(Type type, InputStream stream) throws IOException {
		JavaType javaType = mapper.getTypeFactory().constructType(type);
		return mapper.readValue(stream, javaType);
	}

	@Override
	public Object deserialize(Type type, String input) throws IOException {
		JavaType javaType = mapper.getTypeFactory().constructType(type);
		return mapper.readValue(input, javaType);
	}
}
