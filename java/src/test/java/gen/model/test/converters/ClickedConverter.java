package gen.model.test.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class ClickedConverter implements ObjectConverter<gen.model.test.Clicked> {

	@SuppressWarnings("unchecked")
	public ClickedConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "test".equals(it.typeSchema) && "Clicked_event".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
			
		column = columns.stream().filter(it -> "_event_id".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find '_event_id' column in test Clicked_event. Check if DB is in sync");
		__index____event_id = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "QueuedAt".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'QueuedAt' column in test Clicked_event. Check if DB is in sync");
		__index___QueuedAt = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "ProcessedAt".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ProcessedAt' column in test Clicked_event. Check if DB is in sync");
		__index___ProcessedAt = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "date".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'date' column in test Clicked_event. Check if DB is in sync");
		__index___date = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "number".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'number' column in test Clicked_event. Check if DB is in sync");
		__index___number = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "bigint".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'bigint' column in test Clicked_event. Check if DB is in sync");
		__index___bigint = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "bool".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'bool' column in test Clicked_event. Check if DB is in sync");
		__index___bool = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "en".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'en' column in test Clicked_event. Check if DB is in sync");
		__index___en = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.test.Clicked.__configureConverter(readers, __index____event_id, __index___QueuedAt, __index___ProcessedAt, __index___date, __index___number, __index___bigint, __index___bool, __index___en);
	}

	@Override
	public gen.model.test.Clicked from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.test.Clicked from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.test.Clicked>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.test.Clicked instance = new gen.model.test.Clicked(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.test.Clicked instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___QueuedAt] = org.revenj.postgres.converters.TimestampConverter.toTuple(java.time.LocalDateTime.now());
		items[__index___ProcessedAt] = org.revenj.postgres.converters.TimestampConverter.toTuple(instance.getProcessedAt());
		items[__index___date] = org.revenj.postgres.converters.DateConverter.toTuple(instance.getDate());
		items[__index___number] = org.revenj.postgres.converters.DecimalConverter.toTuple(instance.getNumber());
		items[__index___bigint] = org.revenj.postgres.converters.LongConverter.toTuple(instance.getBigint());
		items[__index___bool] = org.revenj.postgres.converters.ArrayTuple.create(instance.getBool(), it -> org.revenj.postgres.converters.BoolConverter.toTuple(it));
		items[__index___en] = gen.model.test.converters.EnConverter.toTuple(instance.getEn());
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.test.Clicked>[] readers;
	
	public gen.model.test.Clicked from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.test.Clicked instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}
	private final int __index____event_id;
	private final int __index___QueuedAt;
	private final int __index___ProcessedAt;
	private final int __index___date;
	private final int __index___number;
	private final int __index___bigint;
	private final int __index___bool;
	private final int __index___en;
}
