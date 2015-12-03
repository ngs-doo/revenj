/*
* Created by DSL Platform
* v1.0.0.27897 
*/

package gen.model.md.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class Reference2Converter implements ObjectConverter<gen.model.md.Reference2> {

	@SuppressWarnings("unchecked")
	public Reference2Converter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "md".equals(it.typeSchema) && "Reference2_entity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "md".equals(it.typeSchema) && "-ngs_Reference2_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		column = columns.stream().filter(it -> "x".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'x' column in md Reference2_entity. Check if DB is in sync");
		__index___x = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "x".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'x' column in md Reference2. Check if DB is in sync");
		__index__extended_x = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "Detailid".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'Detailid' column in md Reference2_entity. Check if DB is in sync");
		__index___Detailid = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "Detailid".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'Detailid' column in md Reference2. Check if DB is in sync");
		__index__extended_Detailid = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.md.Reference2.__configureConverter(readers, __index___x, __index___Detailid);
			
		gen.model.md.Reference2.__configureConverterExtended(readersExtended, __index__extended_x, __index__extended_Detailid);
	}

	@Override
	public String getDbName() {
		return "\"md\".\"Reference2_entity\"";
	}

	@Override
	public gen.model.md.Reference2 from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.md.Reference2 from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.md.Reference2>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.md.Reference2 instance = new gen.model.md.Reference2(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.md.Reference2 instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___x] = org.revenj.postgres.converters.DecimalConverter.toTuple(instance.getX());
		items[__index___Detailid] = org.revenj.postgres.converters.UuidConverter.toTuple(instance.getDetailid());
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.md.Reference2>[] readers;
	
	public gen.model.md.Reference2 from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.md.Reference2 instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.md.Reference2 from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.md.Reference2 instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_x] = org.revenj.postgres.converters.DecimalConverter.toTuple(instance.getX());
		items[__index__extended_Detailid] = org.revenj.postgres.converters.UuidConverter.toTuple(instance.getDetailid());
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.md.Reference2>[] readersExtended;
	
	public gen.model.md.Reference2 fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.md.Reference2 instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.md.Reference2 fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	private final int __index___x;
	private final int __index__extended_x;
	private final int __index___Detailid;
	private final int __index__extended_Detailid;
	
	public static String buildURI(org.revenj.postgres.PostgresBuffer _sw, gen.model.md.Reference2 instance) throws java.io.IOException {
		_sw.initBuffer();
		String _tmp;
		org.revenj.postgres.converters.UuidConverter.serializeURI(_sw, instance.getDetailid());
		return _sw.bufferToString();
	}
}
