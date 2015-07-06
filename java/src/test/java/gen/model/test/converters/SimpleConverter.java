package gen.model.test.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class SimpleConverter implements ObjectConverter<gen.model.test.Simple> {

	@SuppressWarnings("unchecked")
	public SimpleConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "test".equals(it.typeSchema) && "Simple".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "test".equals(it.typeSchema) && "-ngs_Simple_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
			
		column = columns.stream().filter(it -> "number".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'number' column in test Simple. Check if DB is in sync");
		__index___number = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "number".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'number' column in test Simple. Check if DB is in sync");
		__index__extended_number = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "text".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'text' column in test Simple. Check if DB is in sync");
		__index___text = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "text".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'text' column in test Simple. Check if DB is in sync");
		__index__extended_text = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.test.Simple.__configureConverter(readers, __index___number, __index___text);
			
		gen.model.test.Simple.__configureConverterExtended(readersExtended, __index__extended_number, __index__extended_text);
	}

	@Override
	public gen.model.test.Simple from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.test.Simple from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.test.Simple>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.test.Simple instance = new gen.model.test.Simple(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.test.Simple instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___number] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getNumber());
		items[__index___text] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getText());
		return RecordTuple.from(items);
	}

	public PostgresTuple toExtended(gen.model.test.Simple instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index__extended_number] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getNumber());
		items[__index__extended_text] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getText());
		return RecordTuple.from(items);
	}

	
	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.test.Simple>[] readers;
	
	public gen.model.test.Simple from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.test.Simple instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}
	
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.test.Simple>[] readersExtended;
	
	public gen.model.test.Simple fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.test.Simple instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}
	private final int __index___number;
	private final int __index__extended_number;
	private final int __index___text;
	private final int __index__extended_text;
}
