package gen.model.test.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class Detail2Converter implements ObjectConverter<gen.model.test.Detail2> {

	@SuppressWarnings("unchecked")
	public Detail2Converter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "test".equals(it.typeSchema) && "Detail2_entity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "test".equals(it.typeSchema) && "-ngs_Detail2_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
			
		column = columns.stream().filter(it -> "u".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'u' column in test Detail2_entity. Check if DB is in sync");
		__index___u = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "u".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'u' column in test Detail2. Check if DB is in sync");
		__index__extended_u = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "dd".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'dd' column in test Detail2_entity. Check if DB is in sync");
		__index___dd = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "dd".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'dd' column in test Detail2. Check if DB is in sync");
		__index__extended_dd = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "EntityCompositeid".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'EntityCompositeid' column in test Detail2_entity. Check if DB is in sync");
		__index___EntityCompositeid = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "EntityCompositeid".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'EntityCompositeid' column in test Detail2. Check if DB is in sync");
		__index__extended_EntityCompositeid = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "EntityIndex".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'EntityIndex' column in test Detail2_entity. Check if DB is in sync");
		__index___EntityIndex = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "EntityIndex".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'EntityIndex' column in test Detail2. Check if DB is in sync");
		__index__extended_EntityIndex = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "Index".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'Index' column in test Detail2_entity. Check if DB is in sync");
		__index___Index = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "Index".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'Index' column in test Detail2. Check if DB is in sync");
		__index__extended_Index = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.test.Detail2.__configureConverter(readers, __index___u, __index___dd, __index___EntityCompositeid, __index___EntityIndex, __index___Index);
			
		gen.model.test.Detail2.__configureConverterExtended(readersExtended, __index__extended_u, __index__extended_dd, __index__extended_EntityCompositeid, __index__extended_EntityIndex, __index__extended_Index);
	}

	@Override
	public String getDbName() {
		return "\"test\".\"Detail2_entity\"";
	}

	@Override
	public gen.model.test.Detail2 from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.test.Detail2 from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.test.Detail2>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.test.Detail2 instance = new gen.model.test.Detail2(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.test.Detail2 instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___u] = org.revenj.postgres.converters.UrlConverter.toTuple(instance.getU());
		items[__index___dd] = org.revenj.postgres.converters.ArrayTuple.create(instance.getDd(), org.revenj.postgres.converters.DoubleConverter::toTuple);
		items[__index___EntityCompositeid] = org.revenj.postgres.converters.UuidConverter.toTuple(instance.getEntityCompositeid());
		items[__index___EntityIndex] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getEntityIndex());
		items[__index___Index] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getIndex());
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.test.Detail2>[] readers;
	
	public gen.model.test.Detail2 from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.test.Detail2 instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.test.Detail2 from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.test.Detail2 instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_u] = org.revenj.postgres.converters.UrlConverter.toTuple(instance.getU());
		items[__index__extended_dd] = org.revenj.postgres.converters.ArrayTuple.create(instance.getDd(), org.revenj.postgres.converters.DoubleConverter::toTuple);
		items[__index__extended_EntityCompositeid] = org.revenj.postgres.converters.UuidConverter.toTuple(instance.getEntityCompositeid());
		items[__index__extended_EntityIndex] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getEntityIndex());
		items[__index__extended_Index] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getIndex());
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.test.Detail2>[] readersExtended;
	
	public gen.model.test.Detail2 fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.test.Detail2 instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.test.Detail2 fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	private final int __index___u;
	private final int __index__extended_u;
	private final int __index___dd;
	private final int __index__extended_dd;
	private final int __index___EntityCompositeid;
	private final int __index__extended_EntityCompositeid;
	private final int __index___EntityIndex;
	private final int __index__extended_EntityIndex;
	private final int __index___Index;
	private final int __index__extended_Index;
	
	public static String buildURI(org.revenj.postgres.PostgresBuffer _sw, java.util.UUID EntityCompositeid, int EntityIndex, int Index) throws java.io.IOException {
		_sw.initBuffer();
		String _tmp;
		org.revenj.postgres.converters.UuidConverter.serializeURI(_sw, EntityCompositeid);
		_sw.addToBuffer('/');org.revenj.postgres.converters.IntConverter.serializeURI(_sw, EntityIndex);
		_sw.addToBuffer('/');org.revenj.postgres.converters.IntConverter.serializeURI(_sw, Index);
		return _sw.bufferToString();
	}
}
