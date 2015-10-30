package gen.model.security.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class DocumentConverter implements ObjectConverter<gen.model.security.Document> {

	@SuppressWarnings("unchecked")
	public DocumentConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "security".equals(it.typeSchema) && "Document_entity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "security".equals(it.typeSchema) && "-ngs_Document_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
			
		column = columns.stream().filter(it -> "ID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ID' column in security Document_entity. Check if DB is in sync");
		__index___ID = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "ID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ID' column in security Document. Check if DB is in sync");
		__index__extended_ID = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "data".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'data' column in security Document_entity. Check if DB is in sync");
		__index___data = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "data".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'data' column in security Document. Check if DB is in sync");
		__index__extended_data = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "deactivated".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'deactivated' column in security Document_entity. Check if DB is in sync");
		__index___deactivated = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "deactivated".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'deactivated' column in security Document. Check if DB is in sync");
		__index__extended_deactivated = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.security.Document.__configureConverter(readers, __index___ID, __index___data, __index___deactivated);
			
		gen.model.security.Document.__configureConverterExtended(readersExtended, __index__extended_ID, __index__extended_data, __index__extended_deactivated);
	}

	@Override
	public String getDbName() {
		return "\"security\".\"Document_entity\"";
	}

	@Override
	public gen.model.security.Document from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.security.Document from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.security.Document>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.security.Document instance = new gen.model.security.Document(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.security.Document instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___ID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getID());
		items[__index___data] = org.revenj.postgres.converters.HstoreConverter.toTuple(instance.getData());
		items[__index___deactivated] = org.revenj.postgres.converters.BoolConverter.toTuple(instance.getDeactivated());
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.security.Document>[] readers;
	
	public gen.model.security.Document from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.security.Document instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.security.Document from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.security.Document instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_ID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getID());
		items[__index__extended_data] = org.revenj.postgres.converters.HstoreConverter.toTuple(instance.getData());
		items[__index__extended_deactivated] = org.revenj.postgres.converters.BoolConverter.toTuple(instance.getDeactivated());
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.security.Document>[] readersExtended;
	
	public gen.model.security.Document fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.security.Document instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.security.Document fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	private final int __index___ID;
	private final int __index__extended_ID;
	
	public static String buildURI(org.revenj.postgres.PostgresBuffer _sw, gen.model.security.Document instance) throws java.io.IOException {
		_sw.initBuffer();
		String _tmp;
		org.revenj.postgres.converters.IntConverter.serializeURI(_sw, instance.getID());
		return _sw.bufferToString();
	}
	private final int __index___data;
	private final int __index__extended_data;
	private final int __index___deactivated;
	private final int __index__extended_deactivated;
}
