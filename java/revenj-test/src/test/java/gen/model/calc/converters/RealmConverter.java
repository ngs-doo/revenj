package gen.model.calc.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class RealmConverter implements ObjectConverter<gen.model.calc.Realm> {

	@SuppressWarnings("unchecked")
	public RealmConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "calc".equals(it.typeSchema) && "Realm_entity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "calc".equals(it.typeSchema) && "-ngs_Realm_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		column = columns.stream().filter(it -> "infoURI".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'infoURI' column in calc Realm_entity. Check if DB is in sync");
		__index___infoURI = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "infoURI".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'infoURI' column in calc Realm. Check if DB is in sync");
		__index__extended_infoURI = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "infoID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'infoID' column in calc Realm_entity. Check if DB is in sync");
		__index___infoID = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "infoID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'infoID' column in calc Realm. Check if DB is in sync");
		__index__extended_infoID = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "refTypeURI".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'refTypeURI' column in calc Realm_entity. Check if DB is in sync");
		__index___refTypeURI = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "refTypeURI".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'refTypeURI' column in calc Realm. Check if DB is in sync");
		__index__extended_refTypeURI = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "type".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'type' column in calc Realm_entity. Check if DB is in sync");
		__index___type = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "type".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'type' column in calc Realm. Check if DB is in sync");
		__index__extended_type = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "id".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'id' column in calc Realm_entity. Check if DB is in sync");
		__index___id = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "id".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'id' column in calc Realm. Check if DB is in sync");
		__index__extended_id = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.calc.Realm.__configureConverter(readers, __index___infoURI, __index___infoID, __index___refTypeURI, __index___type, __index___id);
			
		gen.model.calc.Realm.__configureConverterExtended(readersExtended, __index__extended_infoURI, __index__extended_infoID, __index__extended_refTypeURI, __index__extended_type, __index__extended_id);
	}

	@Override
	public String getDbName() {
		return "\"calc\".\"Realm_entity\"";
	}

	@Override
	public gen.model.calc.Realm from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.calc.Realm from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.calc.Realm>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.calc.Realm instance = new gen.model.calc.Realm(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.calc.Realm instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		if (instance.getInfoURI() != null)items[__index___infoURI] = new org.revenj.postgres.converters.ValueTuple(instance.getInfoURI());;
		items[__index___infoID] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getInfoID());
		if (instance.getRefTypeURI() != null)items[__index___refTypeURI] = new org.revenj.postgres.converters.ValueTuple(instance.getRefTypeURI());;
		items[__index___type] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getType());
		items[__index___id] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getId());
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.calc.Realm>[] readers;
	
	public gen.model.calc.Realm from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.calc.Realm instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.calc.Realm from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.calc.Realm instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		if (instance.getInfoURI() != null)items[__index__extended_infoURI] = new org.revenj.postgres.converters.ValueTuple(instance.getInfoURI());;
		items[__index__extended_infoID] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getInfoID());
		if (instance.getRefTypeURI() != null)items[__index__extended_refTypeURI] = new org.revenj.postgres.converters.ValueTuple(instance.getRefTypeURI());;
		items[__index__extended_type] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getType());
		items[__index__extended_id] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getId());
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.calc.Realm>[] readersExtended;
	
	public gen.model.calc.Realm fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.calc.Realm instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.calc.Realm fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	
	public static String buildURI(org.revenj.postgres.PostgresBuffer _sw, gen.model.calc.Realm instance) throws java.io.IOException {
		_sw.initBuffer();
		String _tmp;
		org.revenj.postgres.converters.StringConverter.serializeURI(_sw, instance.getId());
		return _sw.bufferToString();
	}
	private final int __index___infoURI;
	private final int __index__extended_infoURI;
	private final int __index___infoID;
	private final int __index__extended_infoID;
	private final int __index___refTypeURI;
	private final int __index__extended_refTypeURI;
	private final int __index___type;
	private final int __index__extended_type;
	private final int __index___id;
	private final int __index__extended_id;
}
