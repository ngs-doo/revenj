package gen.model.egzotics.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class PksVConverter implements ObjectConverter<gen.model.egzotics.PksV> {

	@SuppressWarnings("unchecked")
	public PksVConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "egzotics".equals(it.typeSchema) && "PksV_entity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "egzotics".equals(it.typeSchema) && "-ngs_PksV_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
			
		column = columns.stream().filter(it -> "v".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'v' column in egzotics PksV_entity. Check if DB is in sync");
		__index___v = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "v".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'v' column in egzotics PksV. Check if DB is in sync");
		__index__extended_v = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "vv".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'vv' column in egzotics PksV_entity. Check if DB is in sync");
		__index___vv = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "vv".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'vv' column in egzotics PksV. Check if DB is in sync");
		__index__extended_vv = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "e".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'e' column in egzotics PksV_entity. Check if DB is in sync");
		__index___e = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "e".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'e' column in egzotics PksV. Check if DB is in sync");
		__index__extended_e = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "ee".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ee' column in egzotics PksV_entity. Check if DB is in sync");
		__index___ee = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "ee".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ee' column in egzotics PksV. Check if DB is in sync");
		__index__extended_ee = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		__converter_v = locator.resolve(gen.model.egzotics.converters.vConverter.class);
		__converter_vv = locator.resolve(gen.model.egzotics.converters.vConverter.class);
		
			
		gen.model.egzotics.PksV.__configureConverter(readers, __converter_v, __index___v, __converter_vv, __index___vv, __index___e, __index___ee);
			
		gen.model.egzotics.PksV.__configureConverterExtended(readersExtended, __converter_v, __index__extended_v, __converter_vv, __index__extended_vv, __index__extended_e, __index__extended_ee);
	}

	@Override
	public String getDbName() {
		return "\"egzotics\".\"PksV_entity\"";
	}

	@Override
	public gen.model.egzotics.PksV from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.egzotics.PksV from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.egzotics.PksV>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.egzotics.PksV instance = new gen.model.egzotics.PksV(reader, context, readers, this);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.egzotics.PksV instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___v] = __converter_v.to(instance.getV());
		items[__index___vv] = org.revenj.postgres.converters.ArrayTuple.create(instance.getVv(), __converter_vv::to);
		items[__index___e] = gen.model.egzotics.converters.EConverter.toTuple(instance.getE());
		items[__index___ee] = org.revenj.postgres.converters.ArrayTuple.create(instance.getEe(), gen.model.egzotics.converters.EConverter::toTuple);
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.egzotics.PksV>[] readers;
	
	public gen.model.egzotics.PksV from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.egzotics.PksV instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.egzotics.PksV from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.egzotics.PksV instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_v] = __converter_v.toExtended(instance.getV());
		items[__index__extended_vv] = org.revenj.postgres.converters.ArrayTuple.create(instance.getVv(), __converter_vv::toExtended);
		items[__index__extended_e] = gen.model.egzotics.converters.EConverter.toTuple(instance.getE());
		items[__index__extended_ee] = org.revenj.postgres.converters.ArrayTuple.create(instance.getEe(), gen.model.egzotics.converters.EConverter::toTuple);
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.egzotics.PksV>[] readersExtended;
	
	public gen.model.egzotics.PksV fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.egzotics.PksV instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.egzotics.PksV fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	
	public String buildURI(org.revenj.postgres.PostgresBuffer _sw, gen.model.egzotics.PksV instance) throws java.io.IOException {
		_sw.initBuffer();
		String _tmp;
		
		_tmp = org.revenj.postgres.converters.ArrayTuple.create(instance.getVv(), __converter_vv::to).buildTuple(false);
		org.revenj.postgres.converters.StringConverter.serializeCompositeURI(_sw, _tmp);
		_sw.addToBuffer('/');
		_tmp = gen.model.egzotics.converters.EConverter.stringValue(instance.getE());
		org.revenj.postgres.converters.StringConverter.serializeURI(_sw, _tmp);
		_sw.addToBuffer('/');
		_tmp = org.revenj.postgres.converters.ArrayTuple.create(instance.getEe(), gen.model.egzotics.converters.EConverter::toTuple).buildTuple(false);
		org.revenj.postgres.converters.StringConverter.serializeCompositeURI(_sw, _tmp);
		return _sw.bufferToString();
	}
	private gen.model.egzotics.converters.vConverter __converter_v;
	private final int __index___v;
	private final int __index__extended_v;
	private gen.model.egzotics.converters.vConverter __converter_vv;
	private final int __index___vv;
	private final int __index__extended_vv;
	private final int __index___e;
	private final int __index__extended_e;
	private final int __index___ee;
	private final int __index__extended_ee;
}
