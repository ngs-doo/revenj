package gen.model.test.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class SingleDetailConverter implements ObjectConverter<gen.model.test.SingleDetail> {

	@SuppressWarnings("unchecked")
	public SingleDetailConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "test".equals(it.typeSchema) && "SingleDetail_entity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "test".equals(it.typeSchema) && "-ngs_SingleDetail_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
			
		column = columns.stream().filter(it -> "ID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ID' column in test SingleDetail_entity. Check if DB is in sync");
		__index___ID = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "ID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ID' column in test SingleDetail. Check if DB is in sync");
		__index__extended_ID = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "detailsURI".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'detailsURI' column in test SingleDetail_entity. Check if DB is in sync");
		__index___detailsURI = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "detailsURI".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'detailsURI' column in test SingleDetail. Check if DB is in sync");
		__index__extended_detailsURI = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.test.SingleDetail.__configureConverter(readers, __index___ID, __index___detailsURI);
			
		gen.model.test.SingleDetail.__configureConverterExtended(readersExtended, __index__extended_ID, __index__extended_detailsURI);
	}

	@Override
	public String getDbName() {
		return "\"test\".\"SingleDetail_entity\"";
	}

	@Override
	public gen.model.test.SingleDetail from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.test.SingleDetail from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.test.SingleDetail>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.test.SingleDetail instance = new gen.model.test.SingleDetail(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.test.SingleDetail instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___ID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getID());
		if (instance.getDetailsURI() != null) items[__index___detailsURI] = org.revenj.postgres.converters.ArrayTuple.create(instance.getDetailsURI(), org.revenj.postgres.converters.ValueTuple::new);;
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.test.SingleDetail>[] readers;
	
	public gen.model.test.SingleDetail from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.test.SingleDetail instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.test.SingleDetail from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.test.SingleDetail instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_ID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getID());
		if (instance.getDetailsURI() != null) items[__index__extended_detailsURI] = org.revenj.postgres.converters.ArrayTuple.create(instance.getDetailsURI(), org.revenj.postgres.converters.ValueTuple::new);;
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.test.SingleDetail>[] readersExtended;
	
	public gen.model.test.SingleDetail fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.test.SingleDetail instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.test.SingleDetail fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	private final int __index___ID;
	private final int __index__extended_ID;
	
	public static String buildURI(org.revenj.postgres.PostgresBuffer _sw, gen.model.test.SingleDetail instance) throws java.io.IOException {
		_sw.initBuffer();
		String _tmp;
		org.revenj.postgres.converters.IntConverter.serializeURI(_sw, instance.getID());
		return _sw.bufferToString();
	}
	private final int __index___detailsURI;
	private final int __index__extended_detailsURI;
}
