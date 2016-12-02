package org.revenj.database.postgres.converters;

import org.revenj.database.postgres.PostgresBuffer;
import org.revenj.database.postgres.PostgresReader;
import org.revenj.serialization.Serialization;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonConverter {

	private final Serialization<String> serialization;

	public JsonConverter(Serialization<String> serialization) {
		this.serialization = serialization;
	}

	private String toJson(Map<String, Object> value) {
		if (value.isEmpty()) return "{}";
		try {
			return serialization.serialize(Map.class, value);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public void serializeURI(PostgresBuffer sw, Map<String, Object> value) {
		if (value == null) return;
		StringConverter.serializeURI(sw, toJson(value));
	}

	public void serializeCompositeURI(PostgresBuffer sw, Map<String, Object> value) {
		if (value == null) return;
		String str = toJson(value);
		StringConverter.serializeCompositeURI(sw, str);
	}

	public Map<String, Object> parse(PostgresReader reader, int context, boolean allowNulls) throws IOException {
		String input = StringConverter.parse(reader, context, true);
		if (input == null) {
			return allowNulls ? null : new HashMap<>(0);
		}
		return (Map<String, Object>) serialization.deserialize(Map.class, input);
	}

	public List<Map<String, Object>> parseCollection(
			PostgresReader reader,
			int context,
			boolean allowNulls) throws IOException {
		List<String> parsed = StringConverter.parseCollection(reader, context, true);
		if (parsed == null) return null;
		List<Map<String, Object>> list = new ArrayList<>(parsed.size());
		for (String s : parsed) {
			if (s == null) {
				if (allowNulls) list.add(null);
				else list.add(new HashMap<>(0));
			} else {
				list.add((Map<String, Object>) serialization.deserialize(Map.class, s));
			}
		}
		return list;
	}

	private static final PostgresTuple EMPTY = ValueTuple.from("{}");

	public PostgresTuple toTuple(Map<String, Object> value) {
		if (value == null) return null;
		else if (value.isEmpty()) return EMPTY;
		try {
			return ValueTuple.from(serialization.serialize(value));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
