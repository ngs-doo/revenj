package org.revenj.database.postgres.converters;

import org.postgresql.util.PGobject;
import org.revenj.TreePath;
import org.revenj.database.postgres.PostgresBuffer;
import org.revenj.database.postgres.PostgresReader;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class TreePathConverter {

	public static void setParameter(PostgresBuffer sw, PreparedStatement ps, int index, TreePath value) throws SQLException {
		PGobject pg = new PGobject();
		pg.setType("ltree");
		pg.setValue(value != null ? value.toString() : null);
		ps.setObject(index, pg);
	}

	public static void serializeURI(PostgresBuffer sw, TreePath value) throws IOException {
		if (value == null) return;
		sw.addToBuffer(value.toString());
	}

	public static TreePath parse(PostgresReader reader, boolean allowNulls) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return allowNulls ? null : TreePath.EMPTY;
		}
		reader.initBuffer((char) cur);
		reader.fillUntil(',', ')');
		reader.read();
		return TreePath.create(reader.bufferToString());
	}

	public static List<TreePath> parseCollection(PostgresReader reader, int context, boolean allowNull) throws IOException {
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
		List<TreePath> list = new ArrayList<>();
		TreePath emptyCol = allowNull ? null : TreePath.EMPTY;
		do {
			cur = reader.read();
			reader.initBuffer((char) cur);
			reader.fillUntil(',', '}');
			cur = reader.read();
			if (reader.bufferMatches("NULL")) {
				list.add(emptyCol);
			} else {
				list.add(TreePath.create(reader.bufferToString()));
			}
		} while (cur == ',');
		if (escaped) {
			reader.read(context + 1);
		} else {
			reader.read();
		}
		return list;
	}

	public static PostgresTuple toTuple(TreePath value) {
		return value != null ? ValueTuple.from(value.toString()) : null;
	}
}
