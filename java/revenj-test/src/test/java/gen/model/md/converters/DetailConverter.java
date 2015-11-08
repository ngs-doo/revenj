package gen.model.md.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class DetailConverter implements ObjectConverter<gen.model.md.Detail> {

	@SuppressWarnings("unchecked")
	public DetailConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "md".equals(it.typeSchema) && "Detail_entity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "md".equals(it.typeSchema) && "-ngs_Detail_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		column = columns.stream().filter(it -> "id".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'id' column in md Detail_entity. Check if DB is in sync");
		__index___id = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "id".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'id' column in md Detail. Check if DB is in sync");
		__index__extended_id = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "masterId".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'masterId' column in md Detail_entity. Check if DB is in sync");
		__index___masterId = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "masterId".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'masterId' column in md Detail. Check if DB is in sync");
		__index__extended_masterId = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.md.Detail.__configureConverter(readers, __index___id, __index___masterId);
			
		gen.model.md.Detail.__configureConverterExtended(readersExtended, __index__extended_id, __index__extended_masterId);
	}

	@Override
	public String getDbName() {
		return "\"md\".\"Detail_entity\"";
	}

	@Override
	public gen.model.md.Detail from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.md.Detail from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.md.Detail>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.md.Detail instance = new gen.model.md.Detail(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.md.Detail instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___id] = org.revenj.postgres.converters.UuidConverter.toTuple(instance.getId());
		items[__index___masterId] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getMasterId());
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.md.Detail>[] readers;
	
	public gen.model.md.Detail from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.md.Detail instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.md.Detail from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.md.Detail instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_id] = org.revenj.postgres.converters.UuidConverter.toTuple(instance.getId());
		items[__index__extended_masterId] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getMasterId());
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.md.Detail>[] readersExtended;
	
	public gen.model.md.Detail fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.md.Detail instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.md.Detail fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	
	public static String buildURI(org.revenj.postgres.PostgresBuffer _sw, gen.model.md.Detail instance) throws java.io.IOException {
		_sw.initBuffer();
		String _tmp;
		org.revenj.postgres.converters.UuidConverter.serializeURI(_sw, instance.getId());
		return _sw.bufferToString();
	}
	private final int __index___id;
	private final int __index__extended_id;
	private final int __index___masterId;
	private final int __index__extended_masterId;
}
