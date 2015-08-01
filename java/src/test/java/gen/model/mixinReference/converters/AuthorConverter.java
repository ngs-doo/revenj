package gen.model.mixinReference.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class AuthorConverter implements ObjectConverter<gen.model.mixinReference.Author> {

	@SuppressWarnings("unchecked")
	public AuthorConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "mixinReference".equals(it.typeSchema) && "Author_entity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "mixinReference".equals(it.typeSchema) && "-ngs_Author_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
			
		column = columns.stream().filter(it -> "ID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ID' column in mixinReference Author_entity. Check if DB is in sync");
		__index___ID = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "ID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ID' column in mixinReference Author. Check if DB is in sync");
		__index__extended_ID = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "name".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'name' column in mixinReference Author_entity. Check if DB is in sync");
		__index___name = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "name".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'name' column in mixinReference Author. Check if DB is in sync");
		__index__extended_name = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.mixinReference.Author.__configureConverter(readers, __index___ID, __index___name);
			
		gen.model.mixinReference.Author.__configureConverterExtended(readersExtended, __index__extended_ID, __index__extended_name);
	}

	@Override
	public String getDbName() {
		return "\"mixinReference\".\"Author_entity\"";
	}

	@Override
	public gen.model.mixinReference.Author from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.mixinReference.Author from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.mixinReference.Author>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.mixinReference.Author instance = new gen.model.mixinReference.Author(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.mixinReference.Author instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___ID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getID());
		items[__index___name] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getName());
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.mixinReference.Author>[] readers;
	
	public gen.model.mixinReference.Author from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.mixinReference.Author instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}
	
	public PostgresTuple toExtended(gen.model.mixinReference.Author instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_ID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getID());
		items[__index__extended_name] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getName());
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.mixinReference.Author>[] readersExtended;
	
	public gen.model.mixinReference.Author fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.mixinReference.Author instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}
	private final int __index___ID;
	private final int __index__extended_ID;
	
	public static String buildURI(char[] _buf, int ID) throws java.io.IOException {
		int _len = 0;
		String _tmp;
		_len = org.revenj.postgres.converters.IntConverter.serializeURI(_buf, _len, ID);
		return new String(_buf, 0, _len);
	}
	private final int __index___name;
	private final int __index__extended_name;
}
