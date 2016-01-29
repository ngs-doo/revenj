/*
* Created by DSL Platform
* v1.5.5871.15913 
*/

package gen.model.xc.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class SearchByTimestampAndOrderByTimestampConverter implements ObjectConverter<gen.model.xc.SearchByTimestampAndOrderByTimestamp> {

	@SuppressWarnings("unchecked")
	public SearchByTimestampAndOrderByTimestampConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "xc".equals(it.typeSchema) && "SearchByTimestampAndOrderByTimestamp_entity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "xc".equals(it.typeSchema) && "-ngs_SearchByTimestampAndOrderByTimestamp_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		column = columns.stream().filter(it -> "ID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ID' column in xc SearchByTimestampAndOrderByTimestamp_entity. Check if DB is in sync");
		__index___ID = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "ID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ID' column in xc SearchByTimestampAndOrderByTimestamp. Check if DB is in sync");
		__index__extended_ID = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "ondate".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ondate' column in xc SearchByTimestampAndOrderByTimestamp_entity. Check if DB is in sync");
		__index___ondate = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "ondate".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ondate' column in xc SearchByTimestampAndOrderByTimestamp. Check if DB is in sync");
		__index__extended_ondate = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "marker".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'marker' column in xc SearchByTimestampAndOrderByTimestamp_entity. Check if DB is in sync");
		__index___marker = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "marker".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'marker' column in xc SearchByTimestampAndOrderByTimestamp. Check if DB is in sync");
		__index__extended_marker = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.xc.SearchByTimestampAndOrderByTimestamp.__configureConverter(readers, __index___ID, __index___ondate, __index___marker);
			
		gen.model.xc.SearchByTimestampAndOrderByTimestamp.__configureConverterExtended(readersExtended, __index__extended_ID, __index__extended_ondate, __index__extended_marker);
	}

	@Override
	public String getDbName() {
		return "\"xc\".\"SearchByTimestampAndOrderByTimestamp_entity\"";
	}

	@Override
	public gen.model.xc.SearchByTimestampAndOrderByTimestamp from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.xc.SearchByTimestampAndOrderByTimestamp from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.xc.SearchByTimestampAndOrderByTimestamp>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.xc.SearchByTimestampAndOrderByTimestamp instance = new gen.model.xc.SearchByTimestampAndOrderByTimestamp(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.xc.SearchByTimestampAndOrderByTimestamp instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___ID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getID());
		items[__index___ondate] = org.revenj.postgres.converters.TimestampConverter.toTuple(instance.getOndate());
		items[__index___marker] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getMarker());
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.xc.SearchByTimestampAndOrderByTimestamp>[] readers;
	
	public gen.model.xc.SearchByTimestampAndOrderByTimestamp from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.xc.SearchByTimestampAndOrderByTimestamp instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.xc.SearchByTimestampAndOrderByTimestamp from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.xc.SearchByTimestampAndOrderByTimestamp instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_ID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getID());
		items[__index__extended_ondate] = org.revenj.postgres.converters.TimestampConverter.toTuple(instance.getOndate());
		items[__index__extended_marker] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getMarker());
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.xc.SearchByTimestampAndOrderByTimestamp>[] readersExtended;
	
	public gen.model.xc.SearchByTimestampAndOrderByTimestamp fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.xc.SearchByTimestampAndOrderByTimestamp instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.xc.SearchByTimestampAndOrderByTimestamp fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	private final int __index___ID;
	private final int __index__extended_ID;
	
	public static String buildURI(org.revenj.postgres.PostgresBuffer _sw, gen.model.xc.SearchByTimestampAndOrderByTimestamp instance) throws java.io.IOException {
		_sw.initBuffer();
		String _tmp;
		org.revenj.postgres.converters.IntConverter.serializeURI(_sw, instance.getID());
		return _sw.bufferToString();
	}
	private final int __index___ondate;
	private final int __index__extended_ondate;
	private final int __index___marker;
	private final int __index__extended_marker;
}
