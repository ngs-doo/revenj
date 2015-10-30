package gen.model.binaries.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class WritableDocumentConverter implements ObjectConverter<gen.model.binaries.WritableDocument> {

	@SuppressWarnings("unchecked")
	public WritableDocumentConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "binaries".equals(it.typeSchema) && "WritableDocument".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
			
		column = columns.stream().filter(it -> "ID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ID' column in binaries WritableDocument. Check if DB is in sync");
		__index___id = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "name".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'name' column in binaries WritableDocument. Check if DB is in sync");
		__index___name = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.binaries.WritableDocument.__configureConverter(readers, __index___id, __index___name);
	}

	@Override
	public String getDbName() {
		return "\"binaries\".\"Document\"";
	}

	@Override
	public gen.model.binaries.WritableDocument from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.binaries.WritableDocument from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.binaries.WritableDocument>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.binaries.WritableDocument instance = new gen.model.binaries.WritableDocument(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.binaries.WritableDocument instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___id] = org.revenj.postgres.converters.UuidConverter.toTuple(instance.getId());
		items[__index___name] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getName());
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.binaries.WritableDocument>[] readers;
	
	public gen.model.binaries.WritableDocument from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.binaries.WritableDocument instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.binaries.WritableDocument from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public static String buildURI(org.revenj.postgres.PostgresBuffer _sw, gen.model.binaries.WritableDocument instance) throws java.io.IOException {
		_sw.initBuffer();
		String _tmp;
		org.revenj.postgres.converters.UuidConverter.serializeURI(_sw, instance.getId());
		return _sw.bufferToString();
	}
	private final int __index___id;
	private final int __index___name;
}
