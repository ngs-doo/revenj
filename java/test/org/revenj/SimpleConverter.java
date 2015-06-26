package org.revenj;

import org.revenj.patterns.ServiceLocator;
import org.revenj.postgres.ObjectConverter;
import org.revenj.postgres.PostgresReader;
import org.revenj.postgres.converters.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleConverter implements ObjectConverter<SimpleObject> {

	private final int columnCount;
	private final Reader<SimpleObject>[] readers;

	private final int __index__number;
	private final int __index__text;

	public SimpleConverter(ServiceLocator locator, List<ColumnInfo> allColumns) {
		List<ColumnInfo> columns =
				allColumns.stream().filter(it -> "test".equals(it.typeSchema) && "Simple".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
		readers = new Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
		Optional<ColumnInfo> __numberColumn = columns.stream().filter(it -> "number".equals(it.columnName)).findAny();
		if (!__numberColumn.isPresent()) throw new RuntimeException("Unable to find 'number' column in test Simple. Check if DB is in sync");
		__index__number = (int)__numberColumn.get().order - 1;
		Optional<ColumnInfo> __textColumn = columns.stream().filter(it -> "text".equals(it.columnName)).findAny();
		if (!__textColumn.isPresent()) throw new RuntimeException("Unable to find 'text' column in test Simple. Check if DB is in sync");
		__index__text = (int)__textColumn.get().order - 1;
		SimpleObject.configureConverter(readers);
		ObjectConverter.swap(readers, 0, __index__number);
		ObjectConverter.swap(readers, 1, __index__text);
	}

	@Override
	public SimpleObject from(PostgresReader reader) throws IOException {
		return from(reader, 0);
	}

	public SimpleObject from(PostgresReader reader, int context) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		SimpleObject instance = from(reader, context, context == 0 ? 1 : context << 1);
		reader.read();
		return instance;
	}

	private SimpleObject from(PostgresReader reader, int outerContext, int context) throws IOException {
		reader.read(outerContext);
		SimpleObject instance = new SimpleObject(reader, context, readers);
		if (reader.last() != ')') throw new IOException("Expecting ')'. Found: " + (char)reader.last());
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(SimpleObject instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		items[__index__number] = IntConverter.toTuple(instance.getNumber());
		items[__index__text] = ValueTuple.from(instance.getText());
		return RecordTuple.from(items);
	}
}
