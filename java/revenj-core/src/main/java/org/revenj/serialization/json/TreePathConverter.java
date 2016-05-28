package org.revenj.serialization.json;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import org.revenj.TreePath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public abstract class TreePathConverter {
	static final JsonReader.ReadObject<TreePath> Reader = TreePathConverter::deserialize;
	static final JsonWriter.WriteObject<TreePath> Writer = (writer, value) -> serializeNullable(value, writer);

	public static void serializeNullable(final TreePath value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else {
			serialize(value, sw);
		}
	}

	public static void serialize(final TreePath value, final JsonWriter sw) {
		sw.writeByte(JsonWriter.QUOTE);
		sw.writeAscii(value.toString());
		sw.writeByte(JsonWriter.QUOTE);
	}

	public static TreePath deserialize(final JsonReader reader) throws IOException {
		final char[] tmp = reader.readSimpleQuote();
		final int len = reader.getCurrentIndex() - reader.getTokenStart() - 1;
		if (len == 0) {
			return TreePath.EMPTY;
		}else {
			return TreePath.create(new String(tmp, 0, len));
		}
	}

	public static ArrayList<TreePath> deserializeCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(Reader);
	}

	public static void deserializeCollection(final JsonReader reader, final Collection<TreePath> res) throws IOException {
		reader.deserializeCollection(Reader, res);
	}

	public static ArrayList<TreePath> deserializeNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(Reader);
	}

	public static void deserializeNullableCollection(final JsonReader reader, final Collection<TreePath> res) throws IOException {
		reader.deserializeNullableCollection(Reader, res);
	}
}
