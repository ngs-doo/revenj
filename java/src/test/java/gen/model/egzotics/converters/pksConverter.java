package gen.model.egzotics.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class pksConverter implements ObjectConverter<gen.model.egzotics.pks> {

	@SuppressWarnings("unchecked")
	public pksConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "egzotics".equals(it.typeSchema) && "pks_entity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "egzotics".equals(it.typeSchema) && "-ngs_pks_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
			
		column = columns.stream().filter(it -> "id".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'id' column in egzotics pks_entity. Check if DB is in sync");
		__index___id = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "id".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'id' column in egzotics pks. Check if DB is in sync");
		__index__extended_id = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "xml".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'xml' column in egzotics pks_entity. Check if DB is in sync");
		__index___xml = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "xml".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'xml' column in egzotics pks. Check if DB is in sync");
		__index__extended_xml = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.egzotics.pks.__configureConverter(readers, __index___id, __index___xml);
			
		gen.model.egzotics.pks.__configureConverterExtended(readersExtended, __index__extended_id, __index__extended_xml);
	}

	@Override
	public String getDbName() {
		return "\"egzotics\".\"pks_entity\"";
	}

	@Override
	public gen.model.egzotics.pks from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.egzotics.pks from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.egzotics.pks>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.egzotics.pks instance = new gen.model.egzotics.pks(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.egzotics.pks instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___id] = org.revenj.postgres.converters.ArrayTuple.create(instance.getId(), org.revenj.postgres.converters.IntConverter::toTuple);
		items[__index___xml] = org.revenj.postgres.converters.XmlConverter.toTuple(instance.getXml());
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.egzotics.pks>[] readers;
	
	public gen.model.egzotics.pks from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.egzotics.pks instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.egzotics.pks from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.egzotics.pks instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_id] = org.revenj.postgres.converters.ArrayTuple.create(instance.getId(), org.revenj.postgres.converters.IntConverter::toTuple);
		items[__index__extended_xml] = org.revenj.postgres.converters.XmlConverter.toTuple(instance.getXml());
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.egzotics.pks>[] readersExtended;
	
	public gen.model.egzotics.pks fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.egzotics.pks instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.egzotics.pks fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	
	public static String buildURI(org.revenj.postgres.PostgresBuffer _sw, gen.model.egzotics.pks instance) throws java.io.IOException {
		_sw.initBuffer();
		String _tmp;
		
			_tmp = org.revenj.postgres.converters.ArrayTuple.create(instance.getId(), org.revenj.postgres.converters.IntConverter::toTuple).buildTuple(false);
			_sw.addToBuffer(_tmp);
		return _sw.bufferToString();
	}
	private final int __index___id;
	private final int __index__extended_id;
	private final int __index___xml;
	private final int __index__extended_xml;
}
