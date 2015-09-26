package gen.model.test.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class Detail1Converter implements ObjectConverter<gen.model.test.Detail1> {

	@SuppressWarnings("unchecked")
	public Detail1Converter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "test".equals(it.typeSchema) && "Detail1_entity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "test".equals(it.typeSchema) && "-ngs_Detail1_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
			
		column = columns.stream().filter(it -> "f".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'f' column in test Detail1_entity. Check if DB is in sync");
		__index___f = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "f".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'f' column in test Detail1. Check if DB is in sync");
		__index__extended_f = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "ff".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ff' column in test Detail1_entity. Check if DB is in sync");
		__index___ff = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "ff".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ff' column in test Detail1. Check if DB is in sync");
		__index__extended_ff = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "EntityCompositeid".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'EntityCompositeid' column in test Detail1_entity. Check if DB is in sync");
		__index___EntityCompositeid = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "EntityCompositeid".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'EntityCompositeid' column in test Detail1. Check if DB is in sync");
		__index__extended_EntityCompositeid = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "EntityIndex".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'EntityIndex' column in test Detail1_entity. Check if DB is in sync");
		__index___EntityIndex = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "EntityIndex".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'EntityIndex' column in test Detail1. Check if DB is in sync");
		__index__extended_EntityIndex = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "Index".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'Index' column in test Detail1_entity. Check if DB is in sync");
		__index___Index = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "Index".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'Index' column in test Detail1. Check if DB is in sync");
		__index__extended_Index = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.test.Detail1.__configureConverter(readers, __index___f, __index___ff, __index___EntityCompositeid, __index___EntityIndex, __index___Index);
			
		gen.model.test.Detail1.__configureConverterExtended(readersExtended, __index__extended_f, __index__extended_ff, __index__extended_EntityCompositeid, __index__extended_EntityIndex, __index__extended_Index);
	}

	@Override
	public String getDbName() {
		return "\"test\".\"Detail1_entity\"";
	}

	@Override
	public gen.model.test.Detail1 from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.test.Detail1 from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.test.Detail1>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.test.Detail1 instance = new gen.model.test.Detail1(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.test.Detail1 instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___f] = org.revenj.postgres.converters.FloatConverter.toTuple(instance.getF());
		items[__index___ff] = org.revenj.postgres.converters.FloatConverter.toTuple(instance.getFf());
		items[__index___EntityCompositeid] = org.revenj.postgres.converters.UuidConverter.toTuple(instance.getEntityCompositeid());
		items[__index___EntityIndex] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getEntityIndex());
		items[__index___Index] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getIndex());
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.test.Detail1>[] readers;
	
	public gen.model.test.Detail1 from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.test.Detail1 instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.test.Detail1 from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.test.Detail1 instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_f] = org.revenj.postgres.converters.FloatConverter.toTuple(instance.getF());
		items[__index__extended_ff] = org.revenj.postgres.converters.FloatConverter.toTuple(instance.getFf());
		items[__index__extended_EntityCompositeid] = org.revenj.postgres.converters.UuidConverter.toTuple(instance.getEntityCompositeid());
		items[__index__extended_EntityIndex] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getEntityIndex());
		items[__index__extended_Index] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getIndex());
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.test.Detail1>[] readersExtended;
	
	public gen.model.test.Detail1 fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.test.Detail1 instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.test.Detail1 fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	private final int __index___f;
	private final int __index__extended_f;
	private final int __index___ff;
	private final int __index__extended_ff;
	private final int __index___EntityCompositeid;
	private final int __index__extended_EntityCompositeid;
	private final int __index___EntityIndex;
	private final int __index__extended_EntityIndex;
	private final int __index___Index;
	private final int __index__extended_Index;
	
	public static String buildURI(org.revenj.postgres.PostgresBuffer _sw, gen.model.test.Detail1 instance) throws java.io.IOException {
		_sw.initBuffer();
		String _tmp;
		org.revenj.postgres.converters.UuidConverter.serializeURI(_sw, instance.getEntityCompositeid());
		_sw.addToBuffer('/');org.revenj.postgres.converters.IntConverter.serializeURI(_sw, instance.getEntityIndex());
		_sw.addToBuffer('/');org.revenj.postgres.converters.IntConverter.serializeURI(_sw, instance.getIndex());
		return _sw.bufferToString();
	}
}
