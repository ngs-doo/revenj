package org.revenj.serialization.xml;

import org.revenj.TreePath;
import org.revenj.Utils;
import org.revenj.extensibility.Container;
import org.revenj.extensibility.PluginLoader;
import org.revenj.serialization.Serialization;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMResult;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.List;
import java.util.function.Function;

public class XmlJaxbSerialization implements Serialization<Element> {

	private final Map<Type, Map.Entry<Marshaller, Unmarshaller>> converters = new HashMap<>();
	private final Map<Type, Function> packers = new HashMap<>();
	private final Map<Type, Function> unpackers = new HashMap<>();

	public XmlJaxbSerialization(Container container, Optional<PluginLoader> extensibility) throws IOException {
		this(extensibility.isPresent() ? getPlugins(container, extensibility.get()) : new JaxbConfiguration[0]);
	}

	private static JaxbConfiguration[] getPlugins(Container container, PluginLoader loader) throws IOException {
		try {
			return loader.resolve(container, JaxbConfiguration.class);
		} catch (Exception ex) {
			throw new IOException(ex);
		}
	}

	public XmlJaxbSerialization(JaxbConfiguration[] plugins) {
		register(String.class, StringXML.class, StringXML.convert, s -> s.value);
		registerArray(String.class, StringXML.ArrayXML.class, StringXML.ArrayXML.convert, s -> s.value);
		registerList(String.class, StringXML.ListXML.class, StringXML.ListXML.convert, s -> s.value);
		register(Integer.class, IntegerXML.class, IntegerXML.convert, s -> s.value);
		register(int.class, IntegerXML.class, IntegerXML.convert, s -> s.value);
		registerArray(Integer.class, IntegerXML.ObjectArray.class, IntegerXML.ObjectArray.convert, s -> s.value);
		registerList(Integer.class, IntegerXML.ListXML.class, IntegerXML.ListXML.convert, s -> s.value);
		register(int[].class, IntegerXML.PrimitiveArray.class, IntegerXML.PrimitiveArray.convert, s -> s.value);
		register(Long.class, LongXML.class, LongXML.convert, s -> s.value);
		register(long.class, LongXML.class, LongXML.convert, s -> s.value);
		registerArray(Long.class, LongXML.ObjectArray.class, LongXML.ObjectArray.convert, s -> s.value);
		registerList(Long.class, LongXML.ListXML.class, LongXML.ListXML.convert, s -> s.value);
		register(long[].class, LongXML.PrimitiveArray.class, LongXML.PrimitiveArray.convert, s -> s.value);
		register(Float.class, FloatXML.class, FloatXML.convert, s -> s.value);
		register(float.class, FloatXML.class, FloatXML.convert, s -> s.value);
		registerArray(Float.class, FloatXML.ObjectArray.class, FloatXML.ObjectArray.convert, s -> s.value);
		registerList(Float.class, FloatXML.ListXML.class, FloatXML.ListXML.convert, s -> s.value);
		register(float[].class, FloatXML.PrimitiveArray.class, FloatXML.PrimitiveArray.convert, s -> s.value);
		register(Double.class, DoubleXML.class, DoubleXML.convert, s -> s.value);
		register(double.class, DoubleXML.class, DoubleXML.convert, s -> s.value);
		registerArray(Double.class, DoubleXML.ObjectArray.class, DoubleXML.ObjectArray.convert, s -> s.value);
		registerList(Double.class, DoubleXML.ListXML.class, DoubleXML.ListXML.convert, s -> s.value);
		register(double[].class, DoubleXML.PrimitiveArray.class, DoubleXML.PrimitiveArray.convert, s -> s.value);
		register(Boolean.class, BoolXML.class, BoolXML.convert, s -> s.value);
		register(boolean.class, BoolXML.class, BoolXML.convert, s -> s.value);
		registerArray(Boolean.class, BoolXML.ObjectArray.class, BoolXML.ObjectArray.convert, s -> s.value);
		registerList(Boolean.class, BoolXML.ListXML.class, BoolXML.ListXML.convert, s -> s.value);
		register(boolean[].class, BoolXML.PrimitiveArray.class, BoolXML.PrimitiveArray.convert, s -> s.value);
		register(BigDecimal.class, DecimalXML.class, DecimalXML.convert, s -> s.value);
		registerArray(BigDecimal.class, DecimalXML.ArrayXML.class, DecimalXML.ArrayXML.convert, s -> s.value);
		registerList(BigDecimal.class, DecimalXML.ListXML.class, DecimalXML.ListXML.convert, s -> s.value);
		register(LocalDate.class, DateXML.class, DateXML.convert, s -> LocalDate.parse(s.value));
		registerArray(LocalDate.class, DateXML.ArrayXML.class, DateXML.ArrayXML.convert, DateXML.ArrayXML.parse);
		registerList(LocalDate.class, DateXML.ListXML.class, DateXML.ListXML.convert, DateXML.ListXML.parse);
		register(OffsetDateTime.class, TimestampXML.class, TimestampXML.convert, s -> OffsetDateTime.parse(s.value));
		registerArray(OffsetDateTime.class, TimestampXML.ArrayXML.class, TimestampXML.ArrayXML.convert, TimestampXML.ArrayXML.parse);
		registerList(OffsetDateTime.class, TimestampXML.ListXML.class, TimestampXML.ListXML.convert, TimestampXML.ListXML.parse);
		register(UUID.class, UuidXML.class, UuidXML.convert, s -> s.value);
		registerArray(UUID.class, UuidXML.ArrayXML.class, UuidXML.ArrayXML.convert, s -> s.value);
		registerList(UUID.class, UuidXML.ListXML.class, UuidXML.ListXML.convert, s -> s.value);
		register(TreePath.class, TreePathXML.class, TreePathXML.convert, s -> TreePath.create(s.value));
		registerArray(TreePath.class, TreePathXML.ArrayXML.class, TreePathXML.ArrayXML.convert, TreePathXML.ArrayXML.parse);
		registerList(TreePath.class, TreePathXML.ListXML.class, TreePathXML.ListXML.convert, TreePathXML.ListXML.parse);
		register(Point2D.class, PointDoubleXML.class, PointDoubleXML.convert, s -> new Point2D.Double(s.x, s.y));
		registerArray(Point2D.class, PointDoubleXML.ArrayXML.class, PointDoubleXML.ArrayXML.convert, PointDoubleXML.ArrayXML.parse);
		registerList(Point2D.class, PointDoubleXML.ListXML.class, PointDoubleXML.ListXML.convert, PointDoubleXML.ListXML.parse);
		register(Point.class, PointIntXML.class, PointIntXML.convert, s -> new Point(s.x, s.y));
		registerArray(Point.class, PointIntXML.ArrayXML.class, PointIntXML.ArrayXML.convert, s -> s.value);
		registerList(Point.class, PointIntXML.ListXML.class, PointIntXML.ListXML.convert, s -> s.value);
		register(URL.class, UrlXML.class, UrlXML.convert, s -> s.value);
		registerArray(URL.class, UrlXML.ArrayXML.class, UrlXML.ArrayXML.convert, s -> s.value);
		registerList(URL.class, UrlXML.ListXML.class, UrlXML.ListXML.convert, s -> s.value);
		if (plugins != null) {
			for (JaxbConfiguration it : plugins) {
				it.register(this);
			}
		}
	}

	static Map.Entry<Marshaller, Unmarshaller> create(Class<?> manifest) {
		try {
			JAXBContext ctx = JAXBContext.newInstance(manifest);
			Marshaller marshaller = ctx.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			Unmarshaller unmarshaller = ctx.createUnmarshaller();
			return new HashMap.SimpleEntry<>(marshaller, unmarshaller);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	public <T> void register(Class<T> type) {
		converters.put(type, create(type));
	}

	public <T> void register(Class<T> container, Type type) {
		converters.put(Utils.makeGenericType(container, type), create(container));
	}

	public <T, W> void register(Class<? extends T> type, Class<W> wrapper, Function<T, W> packer, Function<W, T> unpacker) {
		converters.put(type, create(wrapper));
		packers.put(type, packer);
		unpackers.put(type, unpacker);
	}

	public <T, W> void registerArray(Class<? extends T> content, Class<W> wrapper, Function<T[], W> packer, Function<W, T[]> unpacker) {
		Type type = Array.newInstance(content, 0).getClass();
		converters.put(type, create(wrapper));
		packers.put(type, packer);
		unpackers.put(type, unpacker);
	}

	public <T, W> void registerList(Class<? extends T> content, Class<W> wrapper, Function<List<T>, W> packer, Function<W, List<T>> unpacker) {
		Type type = Utils.makeGenericType(List.class, content);
		converters.put(type, create(wrapper));
		packers.put(type, packer);
		unpackers.put(type, unpacker);
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

	private static final byte[] NULL = "<object nil=\"true\"/>".getBytes();
	private static final byte[] EMPTY = "<ArrayOfobject/>".getBytes();

	public void serializeTo(Object value, OutputStream stream) throws IOException {
		if (value == null) {
			stream.write(NULL);
			return;
		}
		Type manifest = findBestManifest(value);
		Map.Entry<Marshaller, Unmarshaller> converter = converters.get(manifest);
		if (converter == null) {
			if (value instanceof Collection) {
				Collection items = (Collection) value;
				if (items.size() == 0) {
					stream.write(EMPTY);
					return;
				}
				ArrayList list = new ArrayList(items.size());
				list.addAll(items);
				manifest = findBestManifest(list);
				converter = converters.get(manifest);
			}
			if (converter == null) {
				throw new IOException("Unable to find XML marshaller for: " + manifest);
			}
		}
		Function wrapper = packers.get(manifest);
		try {
			converter.getKey().marshal(wrapper != null ? wrapper.apply(value) : value, stream);
		} catch (JAXBException e) {
			throw new IOException(e);
		}
	}

	private Type findBestManifest(Object value) {
		Class<?> container = value.getClass();
		if (List.class.isAssignableFrom(container)) {
			List items = (List) value;
			if (items.size() > 0) {
				for (int i = 0; i < items.size(); i++) {
					Object item = items.get(i);
					if (item != null) {
						return Utils.makeGenericType(List.class, item.getClass());
					}
				}
			}
		}
		return container;
	}

	@Override
	public Element serialize(Type manifest, Object value) throws IOException {
		if (manifest == null && value != null) {
			manifest = findBestManifest(value);
		}
		if (manifest == null && value == null) {
			Document doc = documentBuilder.get().newDocument();
			Element object = doc.createElement("object");
			object.setAttribute("nil", "true");
			doc.appendChild(object);
			return object;
		}
		Map.Entry<Marshaller, Unmarshaller> converter = converters.get(manifest);
		if (converter == null) {
			throw new IOException("Unable to find XML marshaller for: " + manifest);
		}
		Function wrapper = packers.get(manifest);
		try {
			DOMResult result = new DOMResult();
			converter.getKey().marshal(wrapper != null ? wrapper.apply(value) : value, result);
			return ((Document) result.getNode()).getDocumentElement();
		} catch (JAXBException e) {
			throw new IOException(e);
		}
	}

	public Object deserialize(Type type, InputStream stream) throws IOException {
		if (stream == null) return null;
		Map.Entry<Marshaller, Unmarshaller> converter = converters.get(type);
		if (converter == null) {
			throw new IOException("Unable to find XML unmarshaller for: " + type);
		}
		try {
			Object result = converter.getValue().unmarshal(stream);
			Function wrapper = unpackers.get(type);
			return wrapper == null ? result : wrapper.apply(result);
		} catch (JAXBException e) {
			throw new IOException(e);
		}
	}

	@Override
	public Object deserialize(Type type, Element data) throws IOException {
		if (data == null) return null;
		if (data.getFirstChild() == null) return null;
		Map.Entry<Marshaller, Unmarshaller> converter = converters.get(type);
		if (converter == null) {
			throw new IOException("Unable to find XML unmarshaller for: " + type);
		}
		try {
			Object result = converter.getValue().unmarshal(data);
			Function wrapper = unpackers.get(type);
			return wrapper == null ? result : wrapper.apply(result);
		} catch (JAXBException e) {
			throw new IOException(e);
		}
	}
}
