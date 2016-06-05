package org.revenj.database.postgres.converters;

import org.revenj.database.postgres.PostgresBuffer;
import org.revenj.database.postgres.PostgresReader;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public abstract class UrlConverter {

	public static void serializeURI(PostgresBuffer sw, URI value) throws IOException {
		if (value == null) return;
		sw.addToBuffer(value.toString());
	}

	public static URI parse(PostgresReader reader, int context) throws IOException {
		String value = StringConverter.parse(reader, context, true);
		return value != null ? URI.create(value) : null;
	}

	public static List<URI> parseCollection(PostgresReader reader, int context) throws IOException {
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
		ArrayList<URI> list = new ArrayList<>();
        do {
            cur = reader.read();
            if (cur == '"' || cur == '\\') {
                String url = StringConverter.parseEscapedString(reader, innerContext, '}');
                list.add(URI.create(url));
                cur = reader.last();
            } else {
                reader.initBuffer((char) cur);
                reader.fillUntil(',', '}');
                cur = reader.read();
                if (reader.bufferMatches("NULL")) {
                    list.add(null);
                } else {
                    list.add(URI.create(reader.bufferToString()));
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

	public static PostgresTuple toTuple(URI value) {
        return value != null ? ValueTuple.from(value.toString()) : null;
	}
}
