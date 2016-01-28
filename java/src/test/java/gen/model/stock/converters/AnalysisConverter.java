/*
* Created by DSL Platform
* v1.5.5871.15913 
*/

package gen.model.stock.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class AnalysisConverter implements ObjectConverter<gen.model.stock.Analysis> {

	@SuppressWarnings("unchecked")
	public AnalysisConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "stock".equals(it.typeSchema) && "Analysis_entity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "stock".equals(it.typeSchema) && "-ngs_Analysis_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		column = columns.stream().filter(it -> "projectID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'projectID' column in stock Analysis_entity. Check if DB is in sync");
		__index___projectID = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "projectID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'projectID' column in stock Analysis. Check if DB is in sync");
		__index__extended_projectID = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "articleID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'articleID' column in stock Analysis_entity. Check if DB is in sync");
		__index___articleID = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "articleID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'articleID' column in stock Analysis. Check if DB is in sync");
		__index__extended_articleID = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "abc".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'abc' column in stock Analysis_entity. Check if DB is in sync");
		__index___abc = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "abc".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'abc' column in stock Analysis. Check if DB is in sync");
		__index__extended_abc = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "xyz".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'xyz' column in stock Analysis_entity. Check if DB is in sync");
		__index___xyz = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "xyz".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'xyz' column in stock Analysis. Check if DB is in sync");
		__index__extended_xyz = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "clazz".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'clazz' column in stock Analysis_entity. Check if DB is in sync");
		__index___clazz = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "clazz".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'clazz' column in stock Analysis. Check if DB is in sync");
		__index__extended_clazz = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.stock.Analysis.__configureConverter(readers, __index___projectID, __index___articleID, __index___abc, __index___xyz, __index___clazz);
			
		gen.model.stock.Analysis.__configureConverterExtended(readersExtended, __index__extended_projectID, __index__extended_articleID, __index__extended_abc, __index__extended_xyz, __index__extended_clazz);
	}

	@Override
	public String getDbName() {
		return "\"stock\".\"Analysis_entity\"";
	}

	@Override
	public gen.model.stock.Analysis from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.stock.Analysis from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.stock.Analysis>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.stock.Analysis instance = new gen.model.stock.Analysis(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.stock.Analysis instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___projectID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getProjectID());
		items[__index___articleID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getArticleID());
		items[__index___abc] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getAbc());
		items[__index___xyz] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getXyz());
		items[__index___clazz] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getClazz());
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.stock.Analysis>[] readers;
	
	public gen.model.stock.Analysis from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.stock.Analysis instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.stock.Analysis from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.stock.Analysis instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_projectID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getProjectID());
		items[__index__extended_articleID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getArticleID());
		items[__index__extended_abc] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getAbc());
		items[__index__extended_xyz] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getXyz());
		items[__index__extended_clazz] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getClazz());
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.stock.Analysis>[] readersExtended;
	
	public gen.model.stock.Analysis fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.stock.Analysis instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.stock.Analysis fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	
	public static String buildURI(org.revenj.postgres.PostgresBuffer _sw, gen.model.stock.Analysis instance) throws java.io.IOException {
		_sw.initBuffer();
		String _tmp;
		org.revenj.postgres.converters.IntConverter.serializeURI(_sw, instance.getProjectID());
		_sw.addToBuffer('/');org.revenj.postgres.converters.IntConverter.serializeURI(_sw, instance.getArticleID());
		return _sw.bufferToString();
	}
	private final int __index___projectID;
	private final int __index__extended_projectID;
	private final int __index___articleID;
	private final int __index__extended_articleID;
	private final int __index___abc;
	private final int __index__extended_abc;
	private final int __index___xyz;
	private final int __index__extended_xyz;
	private final int __index___clazz;
	private final int __index__extended_clazz;
}
