/*
* Created by DSL Platform
* v1.0.0.24260 
*/

package gen.model.issues.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class TimestampPkConverter implements ObjectConverter<gen.model.issues.TimestampPk> {

	@SuppressWarnings("unchecked")
	public TimestampPkConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "issues".equals(it.typeSchema) && "TimestampPk_entity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "issues".equals(it.typeSchema) && "-ngs_TimestampPk_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		column = columns.stream().filter(it -> "ts".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ts' column in issues TimestampPk_entity. Check if DB is in sync");
		__index___ts = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "ts".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ts' column in issues TimestampPk. Check if DB is in sync");
		__index__extended_ts = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "d".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'd' column in issues TimestampPk_entity. Check if DB is in sync");
		__index___d = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "d".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'd' column in issues TimestampPk. Check if DB is in sync");
		__index__extended_d = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.issues.TimestampPk.__configureConverter(readers, __index___ts, __index___d);
			
		gen.model.issues.TimestampPk.__configureConverterExtended(readersExtended, __index__extended_ts, __index__extended_d);
	}

	@Override
	public String getDbName() {
		return "\"issues\".\"TimestampPk_entity\"";
	}

	@Override
	public gen.model.issues.TimestampPk from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.issues.TimestampPk from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.issues.TimestampPk>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.issues.TimestampPk instance = new gen.model.issues.TimestampPk(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.issues.TimestampPk instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___ts] = org.revenj.postgres.converters.TimestampConverter.toTuple(instance.getTs());
		items[__index___d] = org.revenj.postgres.converters.DecimalConverter.toTuple(instance.getD());
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.issues.TimestampPk>[] readers;
	
	public gen.model.issues.TimestampPk from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.issues.TimestampPk instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.issues.TimestampPk from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.issues.TimestampPk instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_ts] = org.revenj.postgres.converters.TimestampConverter.toTuple(instance.getTs());
		items[__index__extended_d] = org.revenj.postgres.converters.DecimalConverter.toTuple(instance.getD());
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.issues.TimestampPk>[] readersExtended;
	
	public gen.model.issues.TimestampPk fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.issues.TimestampPk instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.issues.TimestampPk fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	
	public static String buildURI(org.revenj.postgres.PostgresBuffer _sw, gen.model.issues.TimestampPk instance) throws java.io.IOException {
		_sw.initBuffer();
		String _tmp;
		org.revenj.postgres.converters.TimestampConverter.serializeURI(_sw, instance.getTs());
		return _sw.bufferToString();
	}
	private final int __index___ts;
	private final int __index__extended_ts;
	private final int __index___d;
	private final int __index__extended_d;
}
