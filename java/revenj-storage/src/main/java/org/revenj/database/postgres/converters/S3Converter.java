package org.revenj.database.postgres.converters;

import org.postgresql.util.PGobject;
import org.revenj.storage.S3;
import org.revenj.database.postgres.PostgresBuffer;
import org.revenj.database.postgres.PostgresReader;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public abstract class S3Converter {

	public static void setParameter(PostgresBuffer sw, PreparedStatement ps, int index, S3 value) throws SQLException {
		if (value == null) {
			ps.setObject(index, null);
		} else {
			PGobject pg = new PGobject();
			pg.setType("s3");
			pg.setValue(toString(value));
			ps.setObject(index, pg);
		}
	}

	public static void serializeURI(PostgresBuffer sw, S3 value) throws IOException {
		if (value == null) return;
		sw.addToBuffer(toString(value));
	}

	private static String toString(S3 value) {
		//TODO: this does not match PG behavior fully, but it's good enough (for now)
		String map = HstoreConverter.toTuple(value.getMetadata()).buildTuple(false);
		return String.format("(%s,%s,%d,%s,%s,\"%s\")",
				value.getBucket() == null ? "" : value.getBucket(),
				value.getKey() == null ? "" : value.getKey(),
				value.getLength(),
				value.getName() == null ? "" : "\"" + value.getName().replace("\\", "\\\\").replace("\"", "\\\"") + "\"",
				value.getMimeType() == null ? "" : "\"" + value.getMimeType().replace("\\", "\\\\").replace("\"", "\\\"") + "\"",
				map.replace("\\", "\\\\").replace("\"", "\\\""));
	}

	public static S3 parse(PostgresReader reader, int context) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return null;
		}
		S3 s3 = parseS3(reader, context, context == 0 ? 1 : context << 1);
		reader.read();
		return s3;
	}

	private static S3 parseS3(PostgresReader reader, int context, int innerContext) throws IOException {
		for (int i = 0; i < context; i++) {
			reader.read();
		}
		String bucket = StringConverter.parse(reader, innerContext, false);
		String key = StringConverter.parse(reader, innerContext, false);
		long length = LongConverter.parse(reader);
		String name = StringConverter.parse(reader, innerContext, true);
		String mimeType = StringConverter.parse(reader, innerContext, true);
		Map<String, String> metadata = HstoreConverter.parse(reader, innerContext, false);
		for (int i = 0; i < context; i++) {
			reader.read();
		}
		return new S3(bucket, key, length, name, mimeType, metadata);
	}

	public static List<S3> parseCollection(PostgresReader reader, int context) throws IOException {
		return ArrayTuple.parse(reader, context, S3Converter::parseS3);
	}

	public static PostgresTuple toTuple(S3 value) {
		if (value == null) return null;
		return ValueTuple.from(toString(value));
	}
}
