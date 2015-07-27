package gen.model.test.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class EntityConverter implements ObjectConverter<gen.model.test.Entity> {

	@SuppressWarnings("unchecked")
	public EntityConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "test".equals(it.typeSchema) && "Entity_entity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "test".equals(it.typeSchema) && "-ngs_Entity_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
			
		column = columns.stream().filter(it -> "money".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'money' column in test Entity_entity. Check if DB is in sync");
		__index___money = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "money".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'money' column in test Entity. Check if DB is in sync");
		__index__extended_money = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "id".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'id' column in test Entity_entity. Check if DB is in sync");
		__index___id = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "id".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'id' column in test Entity. Check if DB is in sync");
		__index__extended_id = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "compositeURI".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'compositeURI' column in test Entity_entity. Check if DB is in sync");
		__index___compositeURI = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "compositeURI".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'compositeURI' column in test Entity. Check if DB is in sync");
		__index__extended_compositeURI = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "compositeID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'compositeID' column in test Entity_entity. Check if DB is in sync");
		__index___compositeID = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "compositeID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'compositeID' column in test Entity. Check if DB is in sync");
		__index__extended_compositeID = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "Compositeid".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'Compositeid' column in test Entity_entity. Check if DB is in sync");
		__index___Compositeid = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "Compositeid".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'Compositeid' column in test Entity. Check if DB is in sync");
		__index__extended_Compositeid = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "Index".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'Index' column in test Entity_entity. Check if DB is in sync");
		__index___Index = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "Index".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'Index' column in test Entity. Check if DB is in sync");
		__index__extended_Index = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.test.Entity.__configureConverter(readers, __index___money, __index___id, __index___compositeURI, __index___compositeID, __index___Compositeid, __index___Index);
			
		gen.model.test.Entity.__configureConverterExtended(readersExtended, __index__extended_money, __index__extended_id, __index__extended_compositeURI, __index__extended_compositeID, __index__extended_Compositeid, __index__extended_Index);
	}

	@Override
	public gen.model.test.Entity from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.test.Entity from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.test.Entity>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.test.Entity instance = new gen.model.test.Entity(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.test.Entity instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___money] = org.revenj.postgres.converters.DecimalConverter.toTuple(instance.getMoney());
		items[__index___id] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getId());
		if (instance.getCompositeURI() != null)items[__index___compositeURI] = new org.revenj.postgres.converters.ValueTuple(instance.getCompositeURI());;
		items[__index___compositeID] = org.revenj.postgres.converters.UuidConverter.toTupleNullable(instance.getCompositeID());
		items[__index___Compositeid] = org.revenj.postgres.converters.UuidConverter.toTuple(instance.getCompositeid());
		items[__index___Index] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getIndex());
		return RecordTuple.from(items);
	}

	public PostgresTuple toExtended(gen.model.test.Entity instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index__extended_money] = org.revenj.postgres.converters.DecimalConverter.toTuple(instance.getMoney());
		items[__index__extended_id] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getId());
		if (instance.getCompositeURI() != null)items[__index__extended_compositeURI] = new org.revenj.postgres.converters.ValueTuple(instance.getCompositeURI());;
		items[__index__extended_compositeID] = org.revenj.postgres.converters.UuidConverter.toTupleNullable(instance.getCompositeID());
		items[__index__extended_Compositeid] = org.revenj.postgres.converters.UuidConverter.toTuple(instance.getCompositeid());
		items[__index__extended_Index] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getIndex());
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.test.Entity>[] readers;
	
	public gen.model.test.Entity from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.test.Entity instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.test.Entity>[] readersExtended;
	
	public gen.model.test.Entity fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.test.Entity instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}
	private final int __index___money;
	private final int __index__extended_money;
	private final int __index___id;
	private final int __index__extended_id;
	private final int __index___compositeURI;
	private final int __index__extended_compositeURI;
	private final int __index___compositeID;
	private final int __index__extended_compositeID;
	private final int __index___Compositeid;
	private final int __index__extended_Compositeid;
	private final int __index___Index;
	private final int __index__extended_Index;
	
	public static String buildURI(char[] _buf, java.util.UUID Compositeid, int Index) throws java.io.IOException {
		int _len = 0;
		String _tmp;
		_len = org.revenj.postgres.converters.UuidConverter.serializeURI(_buf, _len, Compositeid);
		_buf[_len++] = '/';_len = org.revenj.postgres.converters.IntConverter.serializeURI(_buf, _len, Index);
		return new String(_buf, 0, _len);
	}
}
