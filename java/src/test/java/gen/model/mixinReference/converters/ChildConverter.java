/*
* Created by DSL Platform
* v1.0.0.29923 
*/

package gen.model.mixinReference.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class ChildConverter implements ObjectConverter<gen.model.mixinReference.Child> {

	@SuppressWarnings("unchecked")
	public ChildConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "mixinReference".equals(it.typeSchema) && "Child_entity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "mixinReference".equals(it.typeSchema) && "-ngs_Child_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		column = columns.stream().filter(it -> "version".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'version' column in mixinReference Child_entity. Check if DB is in sync");
		__index___version = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "version".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'version' column in mixinReference Child. Check if DB is in sync");
		__index__extended_version = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "AuthorID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'AuthorID' column in mixinReference Child_entity. Check if DB is in sync");
		__index___AuthorID = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "AuthorID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'AuthorID' column in mixinReference Child. Check if DB is in sync");
		__index__extended_AuthorID = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "Index".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'Index' column in mixinReference Child_entity. Check if DB is in sync");
		__index___Index = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "Index".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'Index' column in mixinReference Child. Check if DB is in sync");
		__index__extended_Index = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.mixinReference.Child.__configureConverter(readers, __index___version, __index___AuthorID, __index___Index);
			
		gen.model.mixinReference.Child.__configureConverterExtended(readersExtended, __index__extended_version, __index__extended_AuthorID, __index__extended_Index);
	}

	@Override
	public String getDbName() {
		return "\"mixinReference\".\"Child_entity\"";
	}

	@Override
	public gen.model.mixinReference.Child from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.mixinReference.Child from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.mixinReference.Child>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.mixinReference.Child instance = new gen.model.mixinReference.Child(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.mixinReference.Child instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___version] = org.revenj.postgres.converters.LongConverter.toTuple(instance.getVersion());
		items[__index___AuthorID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getAuthorID());
		items[__index___Index] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getIndex());
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.mixinReference.Child>[] readers;
	
	public gen.model.mixinReference.Child from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.mixinReference.Child instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.mixinReference.Child from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.mixinReference.Child instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_version] = org.revenj.postgres.converters.LongConverter.toTuple(instance.getVersion());
		items[__index__extended_AuthorID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getAuthorID());
		items[__index__extended_Index] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getIndex());
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.mixinReference.Child>[] readersExtended;
	
	public gen.model.mixinReference.Child fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.mixinReference.Child instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.mixinReference.Child fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	private final int __index___version;
	private final int __index__extended_version;
	private final int __index___AuthorID;
	private final int __index__extended_AuthorID;
	private final int __index___Index;
	private final int __index__extended_Index;
	
	public static String buildURI(org.revenj.postgres.PostgresBuffer _sw, gen.model.mixinReference.Child instance) throws java.io.IOException {
		_sw.initBuffer();
		String _tmp;
		org.revenj.postgres.converters.IntConverter.serializeURI(_sw, instance.getAuthorID());
		_sw.addToBuffer('/');org.revenj.postgres.converters.IntConverter.serializeURI(_sw, instance.getIndex());
		return _sw.bufferToString();
	}
}
