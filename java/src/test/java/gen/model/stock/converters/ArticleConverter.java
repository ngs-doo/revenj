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

public class ArticleConverter implements ObjectConverter<gen.model.stock.Article> {

	@SuppressWarnings("unchecked")
	public ArticleConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "stock".equals(it.typeSchema) && "Article_entity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "stock".equals(it.typeSchema) && "-ngs_Article_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		column = columns.stream().filter(it -> "ID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ID' column in stock Article_entity. Check if DB is in sync");
		__index___ID = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "ID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ID' column in stock Article. Check if DB is in sync");
		__index__extended_ID = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "projectID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'projectID' column in stock Article_entity. Check if DB is in sync");
		__index___projectID = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "projectID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'projectID' column in stock Article. Check if DB is in sync");
		__index__extended_projectID = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "sku".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'sku' column in stock Article_entity. Check if DB is in sync");
		__index___sku = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "sku".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'sku' column in stock Article. Check if DB is in sync");
		__index__extended_sku = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "title".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'title' column in stock Article_entity. Check if DB is in sync");
		__index___title = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "title".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'title' column in stock Article. Check if DB is in sync");
		__index__extended_title = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.stock.Article.__configureConverter(readers, __index___ID, __index___projectID, __index___sku, __index___title);
			
		gen.model.stock.Article.__configureConverterExtended(readersExtended, __index__extended_ID, __index__extended_projectID, __index__extended_sku, __index__extended_title);
	}

	@Override
	public String getDbName() {
		return "\"stock\".\"Article_entity\"";
	}

	@Override
	public gen.model.stock.Article from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.stock.Article from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.stock.Article>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.stock.Article instance = new gen.model.stock.Article(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.stock.Article instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___ID] = org.revenj.postgres.converters.LongConverter.toTuple(instance.getID());
		items[__index___projectID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getProjectID());
		items[__index___sku] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getSku());
		items[__index___title] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getTitle());
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.stock.Article>[] readers;
	
	public gen.model.stock.Article from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.stock.Article instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.stock.Article from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.stock.Article instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_ID] = org.revenj.postgres.converters.LongConverter.toTuple(instance.getID());
		items[__index__extended_projectID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getProjectID());
		items[__index__extended_sku] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getSku());
		items[__index__extended_title] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getTitle());
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.stock.Article>[] readersExtended;
	
	public gen.model.stock.Article fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.stock.Article instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.stock.Article fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	private final int __index___ID;
	private final int __index__extended_ID;
	
	public static String buildURI(org.revenj.postgres.PostgresBuffer _sw, gen.model.stock.Article instance) throws java.io.IOException {
		_sw.initBuffer();
		String _tmp;
		org.revenj.postgres.converters.LongConverter.serializeURI(_sw, instance.getID());
		return _sw.bufferToString();
	}
	private final int __index___projectID;
	private final int __index__extended_projectID;
	private final int __index___sku;
	private final int __index__extended_sku;
	private final int __index___title;
	private final int __index__extended_title;
}
