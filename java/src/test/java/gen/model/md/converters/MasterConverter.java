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

public class MasterConverter implements ObjectConverter<gen.model.md.Master> {

	@SuppressWarnings("unchecked")
	public MasterConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "md".equals(it.typeSchema) && "Master_entity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "md".equals(it.typeSchema) && "-ngs_Master_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		column = columns.stream().filter(it -> "ID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ID' column in md Master_entity. Check if DB is in sync");
		__index___ID = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "ID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ID' column in md Master. Check if DB is in sync");
		__index__extended_ID = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "details".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'details' column in md Master_entity. Check if DB is in sync");
		__index___details = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "details".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'details' column in md Master. Check if DB is in sync");
		__index__extended_details = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		__converter_details = locator.resolve(gen.model.md.converters.DetailConverter.class);
		
			
		gen.model.md.Master.__configureConverter(readers, __index___ID, __converter_details, __index___details);
			
		gen.model.md.Master.__configureConverterExtended(readersExtended, __index__extended_ID, __converter_details, __index__extended_details);
	}

	@Override
	public String getDbName() {
		return "\"md\".\"Master_entity\"";
	}

	@Override
	public gen.model.md.Master from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.md.Master from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.md.Master>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.md.Master instance = new gen.model.md.Master(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.md.Master instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___ID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getID());
		items[__index___details] = org.revenj.postgres.converters.ArrayTuple.create(instance.getDetails(), __converter_details::to);
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.md.Master>[] readers;
	
	public gen.model.md.Master from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.md.Master instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.md.Master from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.md.Master instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_ID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getID());
		items[__index__extended_details] = org.revenj.postgres.converters.ArrayTuple.create(instance.getDetails(), __converter_details::toExtended);
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.md.Master>[] readersExtended;
	
	public gen.model.md.Master fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.md.Master instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.md.Master fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	private final int __index___ID;
	private final int __index__extended_ID;
	
	public static String buildURI(org.revenj.postgres.PostgresBuffer _sw, gen.model.md.Master instance) throws java.io.IOException {
		_sw.initBuffer();
		String _tmp;
		org.revenj.postgres.converters.IntConverter.serializeURI(_sw, instance.getID());
		return _sw.bufferToString();
	}
	private gen.model.md.converters.DetailConverter __converter_details;
	private final int __index___details;
	private final int __index__extended_details;
}
