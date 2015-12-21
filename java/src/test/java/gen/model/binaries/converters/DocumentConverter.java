/*
* Created by DSL Platform
* v1.0.0.29923 
*/

package gen.model.binaries.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class DocumentConverter implements ObjectConverter<gen.model.binaries.Document> {

	@SuppressWarnings("unchecked")
	public DocumentConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "binaries".equals(it.typeSchema) && "Document_entity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "binaries".equals(it.typeSchema) && "-ngs_Document_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		column = columns.stream().filter(it -> "ID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ID' column in binaries Document_entity. Check if DB is in sync");
		__index___ID = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "ID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ID' column in binaries Document. Check if DB is in sync");
		__index__extended_ID = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "name".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'name' column in binaries Document_entity. Check if DB is in sync");
		__index___name = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "name".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'name' column in binaries Document. Check if DB is in sync");
		__index__extended_name = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "content".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'content' column in binaries Document_entity. Check if DB is in sync");
		__index___content = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "content".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'content' column in binaries Document. Check if DB is in sync");
		__index__extended_content = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "bools".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'bools' column in binaries Document_entity. Check if DB is in sync");
		__index___bools = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "bools".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'bools' column in binaries Document. Check if DB is in sync");
		__index__extended_bools = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.binaries.Document.__configureConverter(readers, __index___ID, __index___name, __index___content, __index___bools);
			
		gen.model.binaries.Document.__configureConverterExtended(readersExtended, __index__extended_ID, __index__extended_name, __index__extended_content, __index__extended_bools);
	}

	@Override
	public String getDbName() {
		return "\"binaries\".\"Document_entity\"";
	}

	@Override
	public gen.model.binaries.Document from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.binaries.Document from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.binaries.Document>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.binaries.Document instance = new gen.model.binaries.Document(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.binaries.Document instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___ID] = org.revenj.postgres.converters.UuidConverter.toTuple(instance.getID());
		items[__index___name] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getName());
		items[__index___content] = org.revenj.postgres.converters.ByteaConverter.toTuple(instance.getContent());
		items[__index___bools] = org.revenj.postgres.converters.ArrayTuple.create(instance.getBools(), org.revenj.postgres.converters.BoolConverter::toTuple);
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.binaries.Document>[] readers;
	
	public gen.model.binaries.Document from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.binaries.Document instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.binaries.Document from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.binaries.Document instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_ID] = org.revenj.postgres.converters.UuidConverter.toTuple(instance.getID());
		items[__index__extended_name] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getName());
		items[__index__extended_content] = org.revenj.postgres.converters.ByteaConverter.toTuple(instance.getContent());
		items[__index__extended_bools] = org.revenj.postgres.converters.ArrayTuple.create(instance.getBools(), org.revenj.postgres.converters.BoolConverter::toTuple);
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.binaries.Document>[] readersExtended;
	
	public gen.model.binaries.Document fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.binaries.Document instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.binaries.Document fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	private final int __index___ID;
	private final int __index__extended_ID;
	
	public static String buildURI(org.revenj.postgres.PostgresBuffer _sw, gen.model.binaries.Document instance) throws java.io.IOException {
		_sw.initBuffer();
		String _tmp;
		org.revenj.postgres.converters.UuidConverter.serializeURI(_sw, instance.getID());
		return _sw.bufferToString();
	}
	private final int __index___name;
	private final int __index__extended_name;
	private final int __index___content;
	private final int __index__extended_content;
	private final int __index___bools;
	private final int __index__extended_bools;
}
