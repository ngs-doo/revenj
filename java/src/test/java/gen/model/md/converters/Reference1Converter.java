/*
* Created by DSL Platform
* v1.0.0.29923 
*/

package gen.model.md.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class Reference1Converter implements ObjectConverter<gen.model.md.Reference1> {

	@SuppressWarnings("unchecked")
	public Reference1Converter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "md".equals(it.typeSchema) && "Reference1_entity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "md".equals(it.typeSchema) && "-ngs_Reference1_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		column = columns.stream().filter(it -> "l".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'l' column in md Reference1_entity. Check if DB is in sync");
		__index___l = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "l".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'l' column in md Reference1. Check if DB is in sync");
		__index__extended_l = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "Detailid".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'Detailid' column in md Reference1_entity. Check if DB is in sync");
		__index___Detailid = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "Detailid".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'Detailid' column in md Reference1. Check if DB is in sync");
		__index__extended_Detailid = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.md.Reference1.__configureConverter(readers, __index___l, __index___Detailid);
			
		gen.model.md.Reference1.__configureConverterExtended(readersExtended, __index__extended_l, __index__extended_Detailid);
	}

	@Override
	public String getDbName() {
		return "\"md\".\"Reference1_entity\"";
	}

	@Override
	public gen.model.md.Reference1 from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.md.Reference1 from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.md.Reference1>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.md.Reference1 instance = new gen.model.md.Reference1(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.md.Reference1 instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___l] = org.revenj.postgres.converters.LongConverter.toTuple(instance.getL());
		items[__index___Detailid] = org.revenj.postgres.converters.UuidConverter.toTuple(instance.getDetailid());
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.md.Reference1>[] readers;
	
	public gen.model.md.Reference1 from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.md.Reference1 instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.md.Reference1 from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.md.Reference1 instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_l] = org.revenj.postgres.converters.LongConverter.toTuple(instance.getL());
		items[__index__extended_Detailid] = org.revenj.postgres.converters.UuidConverter.toTuple(instance.getDetailid());
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.md.Reference1>[] readersExtended;
	
	public gen.model.md.Reference1 fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.md.Reference1 instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.md.Reference1 fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	private final int __index___l;
	private final int __index__extended_l;
	private final int __index___Detailid;
	private final int __index__extended_Detailid;
	
	public static String buildURI(org.revenj.postgres.PostgresBuffer _sw, gen.model.md.Reference1 instance) throws java.io.IOException {
		_sw.initBuffer();
		String _tmp;
		org.revenj.postgres.converters.UuidConverter.serializeURI(_sw, instance.getDetailid());
		return _sw.bufferToString();
	}
}
