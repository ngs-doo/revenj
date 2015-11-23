package gen.model.calc.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class InfoConverter implements ObjectConverter<gen.model.calc.Info> {

	@SuppressWarnings("unchecked")
	public InfoConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "calc".equals(it.typeSchema) && "Info_entity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "calc".equals(it.typeSchema) && "-ngs_Info_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		column = columns.stream().filter(it -> "code".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'code' column in calc Info_entity. Check if DB is in sync");
		__index___code = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "code".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'code' column in calc Info. Check if DB is in sync");
		__index__extended_code = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "name".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'name' column in calc Info_entity. Check if DB is in sync");
		__index___name = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "name".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'name' column in calc Info. Check if DB is in sync");
		__index__extended_name = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.calc.Info.__configureConverter(readers, __index___code, __index___name);
			
		gen.model.calc.Info.__configureConverterExtended(readersExtended, __index__extended_code, __index__extended_name);
	}

	@Override
	public String getDbName() {
		return "\"calc\".\"Info_entity\"";
	}

	@Override
	public gen.model.calc.Info from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.calc.Info from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.calc.Info>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.calc.Info instance = new gen.model.calc.Info(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.calc.Info instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___code] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getCode());
		items[__index___name] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getName());
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.calc.Info>[] readers;
	
	public gen.model.calc.Info from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.calc.Info instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.calc.Info from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.calc.Info instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_code] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getCode());
		items[__index__extended_name] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getName());
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.calc.Info>[] readersExtended;
	
	public gen.model.calc.Info fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.calc.Info instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.calc.Info fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	
	public static String buildURI(org.revenj.postgres.PostgresBuffer _sw, gen.model.calc.Info instance) throws java.io.IOException {
		_sw.initBuffer();
		String _tmp;
		org.revenj.postgres.converters.StringConverter.serializeURI(_sw, instance.getCode());
		return _sw.bufferToString();
	}
	private final int __index___code;
	private final int __index__extended_code;
	private final int __index___name;
	private final int __index__extended_name;
}
