package gen.model.test.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class LazyLoadConverter implements ObjectConverter<gen.model.test.LazyLoad> {

	@SuppressWarnings("unchecked")
	public LazyLoadConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "test".equals(it.typeSchema) && "LazyLoad_entity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "test".equals(it.typeSchema) && "-ngs_LazyLoad_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
			
		column = columns.stream().filter(it -> "ID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ID' column in test LazyLoad_entity. Check if DB is in sync");
		__index___ID = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "ID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ID' column in test LazyLoad. Check if DB is in sync");
		__index__extended_ID = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "compURI".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'compURI' column in test LazyLoad_entity. Check if DB is in sync");
		__index___compURI = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "compURI".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'compURI' column in test LazyLoad. Check if DB is in sync");
		__index__extended_compURI = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "compID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'compID' column in test LazyLoad_entity. Check if DB is in sync");
		__index___compID = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "compID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'compID' column in test LazyLoad. Check if DB is in sync");
		__index__extended_compID = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "sdURI".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'sdURI' column in test LazyLoad_entity. Check if DB is in sync");
		__index___sdURI = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "sdURI".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'sdURI' column in test LazyLoad. Check if DB is in sync");
		__index__extended_sdURI = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "sdID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'sdID' column in test LazyLoad_entity. Check if DB is in sync");
		__index___sdID = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "sdID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'sdID' column in test LazyLoad. Check if DB is in sync");
		__index__extended_sdID = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.test.LazyLoad.__configureConverter(readers, __index___ID, __index___compURI, __index___compID, __index___sdURI, __index___sdID);
			
		gen.model.test.LazyLoad.__configureConverterExtended(readersExtended, __index__extended_ID, __index__extended_compURI, __index__extended_compID, __index__extended_sdURI, __index__extended_sdID);
	}

	@Override
	public String getDbName() {
		return "\"test\".\"LazyLoad_entity\"";
	}

	@Override
	public gen.model.test.LazyLoad from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.test.LazyLoad from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.test.LazyLoad>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.test.LazyLoad instance = new gen.model.test.LazyLoad(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.test.LazyLoad instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___ID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getID());
		if (instance.getCompURI() != null)items[__index___compURI] = new org.revenj.postgres.converters.ValueTuple(instance.getCompURI());;
		items[__index___compID] = org.revenj.postgres.converters.UuidConverter.toTupleNullable(instance.getCompID());
		if (instance.getSdURI() != null)items[__index___sdURI] = new org.revenj.postgres.converters.ValueTuple(instance.getSdURI());;
		items[__index___sdID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getSdID());
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.test.LazyLoad>[] readers;
	
	public gen.model.test.LazyLoad from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.test.LazyLoad instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.test.LazyLoad from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.test.LazyLoad instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_ID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getID());
		if (instance.getCompURI() != null)items[__index__extended_compURI] = new org.revenj.postgres.converters.ValueTuple(instance.getCompURI());;
		items[__index__extended_compID] = org.revenj.postgres.converters.UuidConverter.toTupleNullable(instance.getCompID());
		if (instance.getSdURI() != null)items[__index__extended_sdURI] = new org.revenj.postgres.converters.ValueTuple(instance.getSdURI());;
		items[__index__extended_sdID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getSdID());
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.test.LazyLoad>[] readersExtended;
	
	public gen.model.test.LazyLoad fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.test.LazyLoad instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.test.LazyLoad fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	private final int __index___ID;
	private final int __index__extended_ID;
	
	public static String buildURI(char[] _buf, int ID) throws java.io.IOException {
		int _len = 0;
		String _tmp;
		_len = org.revenj.postgres.converters.IntConverter.serializeURI(_buf, _len, ID);
		return new String(_buf, 0, _len);
	}
	private final int __index___compURI;
	private final int __index__extended_compURI;
	private final int __index___compID;
	private final int __index__extended_compID;
	private final int __index___sdURI;
	private final int __index__extended_sdURI;
	private final int __index___sdID;
	private final int __index__extended_sdID;
}
