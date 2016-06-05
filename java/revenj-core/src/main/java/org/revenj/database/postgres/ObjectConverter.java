package org.revenj.database.postgres;

import org.revenj.database.postgres.converters.PostgresTuple;

import java.io.IOException;

public interface ObjectConverter<T> {
	T from(PostgresReader reader, int context) throws IOException;

	default T from(PostgresReader reader) throws IOException {
		return from(reader, 0);
	}

	PostgresTuple to(T instance);

	String getDbName();

	interface Reader<T> {
		T read(T instance, PostgresReader reader, int context) throws IOException;
	}

	class ColumnInfo {
		public final String typeSchema;
		public final String typeName;
		public final String columnName;
		public final String columnSchema;
		public final String columnType;
		public final short order;
		public final boolean nonNullable;
		public final boolean isMaintained;

		public ColumnInfo(
				String typeSchema,
				String typeName,
				String columnName,
				String columnSchema,
				String columnType,
				short order,
				boolean nonNullable,
				boolean isMaintained) {
			this.typeSchema = typeSchema;
			this.typeName = typeName;
			this.columnName = columnName;
			this.columnSchema = columnSchema;
			this.columnType = columnType;
			this.order = order;
			this.nonNullable = nonNullable;
			this.isMaintained = isMaintained;
		}
	}
}
