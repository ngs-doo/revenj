package gen.model.issues.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class DateListConverter implements ObjectConverter<gen.model.issues.DateList> {

	@SuppressWarnings("unchecked")
	public DateListConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "issues".equals(it.typeSchema) && "DateList_entity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "issues".equals(it.typeSchema) && "-ngs_DateList_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
			
		column = columns.stream().filter(it -> "ID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ID' column in issues DateList_entity. Check if DB is in sync");
		__index___ID = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "ID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ID' column in issues DateList. Check if DB is in sync");
		__index__extended_ID = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "list".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'list' column in issues DateList_entity. Check if DB is in sync");
		__index___list = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "list".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'list' column in issues DateList. Check if DB is in sync");
		__index__extended_list = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.issues.DateList.__configureConverter(readers, __index___ID, __index___list);
			
		gen.model.issues.DateList.__configureConverterExtended(readersExtended, __index__extended_ID, __index__extended_list);
	}

	@Override
	public String getDbName() {
		return "\"issues\".\"DateList_entity\"";
	}

	@Override
	public gen.model.issues.DateList from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.issues.DateList from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.issues.DateList>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.issues.DateList instance = new gen.model.issues.DateList(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.issues.DateList instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___ID] = org.revenj.postgres.converters.LongConverter.toTuple(instance.getID());
		items[__index___list] = org.revenj.postgres.converters.ArrayTuple.create(instance.getList(), org.revenj.postgres.converters.TimestampConverter::toTuple);
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.issues.DateList>[] readers;
	
	public gen.model.issues.DateList from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.issues.DateList instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.issues.DateList from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.issues.DateList instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_ID] = org.revenj.postgres.converters.LongConverter.toTuple(instance.getID());
		items[__index__extended_list] = org.revenj.postgres.converters.ArrayTuple.create(instance.getList(), org.revenj.postgres.converters.TimestampConverter::toTuple);
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.issues.DateList>[] readersExtended;
	
	public gen.model.issues.DateList fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.issues.DateList instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.issues.DateList fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	private final int __index___ID;
	private final int __index__extended_ID;
	
	public static String buildURI(org.revenj.postgres.PostgresBuffer _sw, gen.model.issues.DateList instance) throws java.io.IOException {
		_sw.initBuffer();
		String _tmp;
		org.revenj.postgres.converters.LongConverter.serializeURI(_sw, instance.getID());
		return _sw.bufferToString();
	}
	private final int __index___list;
	private final int __index__extended_list;
}
