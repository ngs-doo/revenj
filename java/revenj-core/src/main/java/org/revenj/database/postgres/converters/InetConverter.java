package org.revenj.database.postgres.converters;

import org.revenj.Utils;
import org.revenj.database.postgres.PostgresBuffer;
import org.revenj.database.postgres.PostgresReader;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public abstract class InetConverter {

	public static void serializeURI(PostgresBuffer sw, InetAddress value) throws IOException {
		if (value == null) return;
		sw.addToBuffer(value.getHostAddress());
	}

	public static InetAddress parse(PostgresReader reader, int context, boolean nullable) throws IOException {
		String value = StringConverter.parse(reader, context, true);
		if (value == null) {
			return nullable ? null : Utils.LOOPBACK;
		}
		return InetAddress.getByName(value);
	}

	public static List<InetAddress> parseCollection(PostgresReader reader, int context, boolean nullable) throws IOException {
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
		ArrayList<InetAddress> list = new ArrayList<>();
		InetAddress defaultValue = nullable ? null : Utils.LOOPBACK;
		do {
			cur = reader.read();
			if (cur == 'N') {
				cur = reader.read(4);
				list.add(defaultValue);
			} else {
				reader.initBuffer((char) cur);
				reader.fillUntil(',', '}');
				list.add(InetAddress.getByName(reader.bufferToString()));
				cur = reader.read();
			}
		} while (cur == ',');
		if (escaped) {
			reader.read(context + 1);
		} else {
			reader.read();
		}
		return list;
	}

	public static PostgresTuple toTupleNullable(InetAddress value) {
		return value == null ? null : ValueTuple.from(value.getHostAddress());
	}

	public static PostgresTuple toTuple(InetAddress value) {
		return ValueTuple.from(value.getHostAddress());
	}
}
