package org.revenj.serialization.xml;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Map;

//TODO: implementation more aligned with XML. This is just to avoid compilation issues
public class MapAdapter extends XmlAdapter<String, Map> {
	private final DslJson<Object> json = new DslJson<>();

	@Override
	public Map unmarshal(String value) throws Exception {
		byte[] bytes = value.getBytes("UTF-8");
		return json.deserialize(Map.class, bytes, bytes.length);
	}

	@Override
	public String marshal(Map value) throws Exception {
		JsonWriter writer = json.newWriter();
		json.serialize(writer, Map.class, value);
		return writer.toString();
	}
}
