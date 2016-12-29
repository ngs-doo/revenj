package org.revenj.database.postgres.converters;

import org.postgresql.geometric.PGpoint;
import org.postgresql.util.PGobject;
import org.revenj.database.postgres.PostgresBuffer;
import org.revenj.database.postgres.PostgresReader;
import org.revenj.database.postgres.PostgresWriter;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class PointConverter {

	public static void setParameter(PostgresBuffer sw, PreparedStatement ps, int index, Point2D value) throws SQLException {
		if (value == null) {
			PGobject pg = new PGobject();
			pg.setType("point");
			ps.setObject(index, pg);
		} else {
			PGpoint pg = new PGpoint(value.getX(), value.getY());
			ps.setObject(index, pg);
		}
	}

	public static void serializeURI(PostgresBuffer sw, Point2D value) {
		sw.addToBuffer('(');
		sw.addToBuffer(Double.toString(value.getX()));
		sw.addToBuffer(',');
		sw.addToBuffer(Double.toString(value.getY()));
		sw.addToBuffer(')');
	}

	public static Point parsePoint(PostgresReader reader, int context, boolean allowNulls) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return allowNulls ? null : new Point();
		}
		Point res = parsePoint(reader, context);
		reader.read();
		return res;
	}

	public static Point2D parseLocation(
			PostgresReader reader,
			int context,
			boolean allowNulls) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return allowNulls ? null : new Point2D.Double();
		}
		Point2D res = parseLocation(reader, context);
		reader.read();
		return res;
	}

	private static Point parsePoint(PostgresReader reader, int context) throws IOException {
		reader.read(context);
		int x = IntConverter.parse(reader);
		int y = IntConverter.parse(reader);
		reader.read(context);
		return new Point(x, y);
	}

	private static Point2D parseLocation(PostgresReader reader, int context) throws IOException {
		reader.read(context);
		double x = DoubleConverter.parse(reader);
		double y = DoubleConverter.parse(reader);
		reader.read(context);
		return new Point2D.Double(x, y);
	}

	public static List<Point> parsePointCollection(
			PostgresReader reader,
			int context,
			boolean allowNulls) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return null;
		}
		boolean escaped = cur != '{';
		if (escaped) {
			reader.read(context);
		}
		int innerContext = context == 0 ? 1 : context << 1;
		cur = reader.peek();
		if (cur == '}') {
			if (escaped) {
				reader.read(context + 2);
			} else {
				reader.read(2);
			}
			return new ArrayList<>(0);
		}
		List<Point> list = new ArrayList<>();
		do {
			cur = reader.read();
			if (cur == 'N') {
				cur = reader.read(4);
				if (allowNulls) {
					list.add(null);
				} else {
					list.add(new Point());
				}
			} else {
				list.add(parsePoint(reader, innerContext));
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

	public static List<Point2D> parseLocationCollection(
			PostgresReader reader,
			int context,
			boolean allowNulls) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return null;
		}
		boolean escaped = cur != '{';
		if (escaped) {
			reader.read(context);
		}
		int innerContext = context == 0 ? 1 : context << 1;
		cur = reader.peek();
		if (cur == '}') {
			if (escaped) {
				reader.read(context + 2);
			} else {
				reader.read(2);
			}
			return new ArrayList<>(0);
		}
		List<Point2D> list = new ArrayList<>();
		do {
			cur = reader.read();
			if (cur == 'N') {
				cur = reader.read(4);
				if (allowNulls) {
					list.add(null);
				} else {
					list.add(new Point2D.Double());
				}
			} else {
				list.add(parseLocation(reader, innerContext));
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

	public static PostgresTuple toTuple(Point2D value) {
		if (value == null) return null;
		return new PointTuple(value);
	}

	static class PointTuple extends PostgresTuple {
		private final Point2D value;

		public PointTuple(Point2D value) {
			this.value = value;
		}

		public boolean mustEscapeRecord() {
			return true;
		}

		public boolean mustEscapeArray() {
			return true;
		}

		public void insertRecord(PostgresWriter sw, String escaping, Mapping mappings) {
			sw.write('(');
			sw.write(Double.toString(value.getX()));
			sw.write(',');
			sw.write(Double.toString(value.getY()));
			sw.write(')');
		}

		public void insertArray(PostgresWriter sw, String escaping, Mapping mappings) {
			insertRecord(sw, escaping, mappings);
		}

		public String buildTuple(boolean quote) {
			if (quote) {
				return "'(" + value.getX() + "," + value.getY() + ")'";
			}
			return "(" + value.getX() + "," + value.getY() + ")";
		}
	}
}
