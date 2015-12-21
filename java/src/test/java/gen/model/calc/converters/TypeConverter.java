/*
* Created by DSL Platform
* v1.0.0.29923 
*/

package gen.model.calc.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class TypeConverter implements ObjectConverter<gen.model.calc.Type> {

	@SuppressWarnings("unchecked")
	public TypeConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "calc".equals(it.typeSchema) && "Type_entity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "calc".equals(it.typeSchema) && "-ngs_Type_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		column = columns.stream().filter(it -> "suffix".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'suffix' column in calc Type_entity. Check if DB is in sync");
		__index___suffix = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "suffix".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'suffix' column in calc Type. Check if DB is in sync");
		__index__extended_suffix = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "description".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'description' column in calc Type_entity. Check if DB is in sync");
		__index___description = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "description".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'description' column in calc Type. Check if DB is in sync");
		__index__extended_description = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "xml".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'xml' column in calc Type_entity. Check if DB is in sync");
		__index___xml = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "xml".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'xml' column in calc Type. Check if DB is in sync");
		__index__extended_xml = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.calc.Type.__configureConverter(readers, __index___suffix, __index___description, __index___xml);
			
		gen.model.calc.Type.__configureConverterExtended(readersExtended, __index__extended_suffix, __index__extended_description, __index__extended_xml);
	}

	@Override
	public String getDbName() {
		return "\"calc\".\"Type_entity\"";
	}

	@Override
	public gen.model.calc.Type from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.calc.Type from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.calc.Type>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.calc.Type instance = new gen.model.calc.Type(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.calc.Type instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___suffix] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getSuffix());
		items[__index___description] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getDescription());
		items[__index___xml] = org.revenj.postgres.converters.XmlConverter.toTuple(instance.getXml());
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.calc.Type>[] readers;
	
	public gen.model.calc.Type from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.calc.Type instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.calc.Type from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.calc.Type instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_suffix] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getSuffix());
		items[__index__extended_description] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getDescription());
		items[__index__extended_xml] = org.revenj.postgres.converters.XmlConverter.toTuple(instance.getXml());
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.calc.Type>[] readersExtended;
	
	public gen.model.calc.Type fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.calc.Type instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.calc.Type fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	
	public static String buildURI(org.revenj.postgres.PostgresBuffer _sw, gen.model.calc.Type instance) throws java.io.IOException {
		_sw.initBuffer();
		String _tmp;
		org.revenj.postgres.converters.StringConverter.serializeURI(_sw, instance.getSuffix());
		return _sw.bufferToString();
	}
	private final int __index___suffix;
	private final int __index__extended_suffix;
	private final int __index___description;
	private final int __index__extended_description;
	private final int __index___xml;
	private final int __index__extended_xml;
}
