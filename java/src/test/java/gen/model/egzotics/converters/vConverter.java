package gen.model.egzotics.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class vConverter implements ObjectConverter<gen.model.egzotics.v> {

	@SuppressWarnings("unchecked")
	public vConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "egzotics".equals(it.typeSchema) && "v".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "egzotics".equals(it.typeSchema) && "-ngs_v_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		column = columns.stream().filter(it -> "x".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'x' column in egzotics v. Check if DB is in sync");
		__index___x = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "x".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'x' column in egzotics v. Check if DB is in sync");
		__index__extended_x = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.egzotics.v.__configureConverter(readers, __index___x);
			
		gen.model.egzotics.v.__configureConverterExtended(readersExtended, __index__extended_x);
	}

	@Override
	public String getDbName() {
		return "\"egzotics\".\"v\"";
	}

	@Override
	public gen.model.egzotics.v from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.egzotics.v from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.egzotics.v>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.egzotics.v instance = new gen.model.egzotics.v(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.egzotics.v instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___x] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getX());
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.egzotics.v>[] readers;
	
	public gen.model.egzotics.v from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.egzotics.v instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.egzotics.v from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.egzotics.v instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_x] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getX());
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.egzotics.v>[] readersExtended;
	
	public gen.model.egzotics.v fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.egzotics.v instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.egzotics.v fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	private final int __index___x;
	private final int __index__extended_x;
}
