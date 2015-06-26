package org.revenj;

import org.revenj.patterns.ServiceLocator;
import org.revenj.postgres.ObjectConverter;
import org.revenj.postgres.PostgresReader;
import org.revenj.postgres.converters.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompositeConverter implements ObjectConverter<CompositeObject> {

	private final int columnCount;
	public final SimpleConverter simpleConverter;
	private final Reader<CompositeObject>[] readers;

	private final int __index__id;
	private final int __index__simple;

	public CompositeConverter(ServiceLocator locator, List<ColumnInfo> allColumns) {
		List<ColumnInfo> columns =
				allColumns.stream().filter(it -> "test".equals(it.typeSchema) && "Composite".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
		readers = new Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
		Optional<ColumnInfo> __column1 = columns.stream().filter(it -> "id".equals(it.columnName)).findAny();
		if (!__column1.isPresent()) throw new RuntimeException("Unable to find 'id' column in test Composite. Check if DB is in sync");
		__index__id = (int)__column1.get().order - 1;
		Optional<ColumnInfo> __column2 = columns.stream().filter(it -> "simple".equals(it.columnName)).findAny();
		if (!__column2.isPresent()) throw new RuntimeException("Unable to find 'simple' column in test Composite. Check if DB is in sync");
		__index__simple = (int)__column2.get().order - 1;

		simpleConverter = locator.resolve(SimpleConverter.class);
		CompositeObject.configureConverter(readers, simpleConverter);
		ObjectConverter.swap(readers, 0, __index__id);
		ObjectConverter.swap(readers, 1, __index__simple);
	}

	@Override
	public CompositeObject from(PostgresReader reader) throws IOException {
		return from(reader, 0);
	}

	public CompositeObject from(PostgresReader reader, int context) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		CompositeObject instance = from(reader, context, context == 0 ? 1 : context << 1);
		reader.read();
		return instance;
	}

	private CompositeObject from(PostgresReader reader, int outerContext, int context) throws IOException {
		reader.read(outerContext);
		CompositeObject instance = new CompositeObject(reader, context, readers);
		if (reader.last() != ')') throw new IOException("Expecting ')'. Found: " + (char) reader.last());
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(CompositeObject instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		items[__index__id] = UuidConverter.toTuple(instance.getID());
		items[__index__simple] = simpleConverter.to(instance.getSimple());
		return RecordTuple.from(items);
	}
}
