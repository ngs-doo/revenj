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

public class AnalysisGridConverter implements ObjectConverter<gen.model.stock.AnalysisGrid> {

	@SuppressWarnings("unchecked")
	public AnalysisGridConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "stock".equals(it.typeSchema) && "AnalysisGrid".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		column = columns.stream().filter(it -> "URI".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'URI' column in stock AnalysisGrid. Check if DB is in sync");
		__index___URI = (int)column.get().order - 1;
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "stock".equals(it.typeSchema) && "-ngs_AnalysisGrid_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		column = columnsExtended.stream().filter(it -> "URI".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'URI' column in stock AnalysisGrid. Check if DB is in sync");
		__index__extended_URI = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "projectID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'projectID' column in stock AnalysisGrid. Check if DB is in sync");
		__index___projectID = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "projectID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'projectID' column in stock AnalysisGrid. Check if DB is in sync");
		__index__extended_projectID = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "title".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'title' column in stock AnalysisGrid. Check if DB is in sync");
		__index___title = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "title".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'title' column in stock AnalysisGrid. Check if DB is in sync");
		__index__extended_title = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "sku".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'sku' column in stock AnalysisGrid. Check if DB is in sync");
		__index___sku = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "sku".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'sku' column in stock AnalysisGrid. Check if DB is in sync");
		__index__extended_sku = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "xyz".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'xyz' column in stock AnalysisGrid. Check if DB is in sync");
		__index___xyz = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "xyz".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'xyz' column in stock AnalysisGrid. Check if DB is in sync");
		__index__extended_xyz = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "abc".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'abc' column in stock AnalysisGrid. Check if DB is in sync");
		__index___abc = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "abc".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'abc' column in stock AnalysisGrid. Check if DB is in sync");
		__index__extended_abc = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
	}

	@Override
	public String getDbName() {
		return "\"stock\".\"AnalysisGrid\"";
	}

	@Override
	public PostgresTuple to(gen.model.stock.AnalysisGrid instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	
	@Override
	public gen.model.stock.AnalysisGrid from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.stock.AnalysisGrid result = from(reader, context, context == 0 ? 1 : context << 1);
		reader.read();
		return result;
	}

	public gen.model.stock.AnalysisGrid from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		reader.read(outerContext);
		int i = 0;
		
		String _URI_ = null;
		int _projectID_ = 0;
		String _title_ = null;
		String _sku_ = null;
		String _xyz_ = null;
		String _abc_ = null;
		for(int x = 0; x < columnCount && i < columnCount; x++) {
			
			if (__index___URI == i) { _URI_ = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); i++; }
			if (__index___projectID == i) { _projectID_ = org.revenj.postgres.converters.IntConverter.parse(reader); i++; }
			if (__index___title == i) { _title_ = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); i++; }
			if (__index___sku == i) { _sku_ = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); i++; }
			if (__index___xyz == i) { _xyz_ = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); i++; }
			if (__index___abc == i) { _abc_ = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); i++; }
		}
		gen.model.stock.AnalysisGrid instance = new gen.model.stock.AnalysisGrid(_URI_, _projectID_, _title_, _sku_, _xyz_, _abc_);
		reader.read(outerContext);
		return instance;
	}
	private final int __index___URI;
	private final int columnCountExtended;
	
	public PostgresTuple toExtended(gen.model.stock.AnalysisGrid instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		return RecordTuple.from(items);
	}

	public gen.model.stock.AnalysisGrid fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.stock.AnalysisGrid result = fromExtended(reader, context, context == 0 ? 1 : context << 1);
		reader.read();
		return result;
	}

	public gen.model.stock.AnalysisGrid fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		reader.read(outerContext);
		int i = 0;
		
		String _URI_ = null;
		int _projectID_ = 0;
		String _title_ = null;
		String _sku_ = null;
		String _xyz_ = null;
		String _abc_ = null;
		for(int x = 0; x < columnCountExtended && i < columnCountExtended; x++) {
			
			if (__index__extended_URI == i) { _URI_ = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); i++; }
			if (__index__extended_projectID == i) { _projectID_ = org.revenj.postgres.converters.IntConverter.parse(reader); i++; }
			if (__index__extended_title == i) { _title_ = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); i++; }
			if (__index__extended_sku == i) { _sku_ = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); i++; }
			if (__index__extended_xyz == i) { _xyz_ = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); i++; }
			if (__index__extended_abc == i) { _abc_ = org.revenj.postgres.converters.StringConverter.parse(reader, context, true); i++; }
		}
		gen.model.stock.AnalysisGrid instance = new gen.model.stock.AnalysisGrid(_URI_, _projectID_, _title_, _sku_, _xyz_, _abc_);
		reader.read(outerContext);
		return instance;
	}
	private final int __index__extended_URI;
	private final int __index___projectID;
	private final int __index__extended_projectID;
	private final int __index___title;
	private final int __index__extended_title;
	private final int __index___sku;
	private final int __index__extended_sku;
	private final int __index___xyz;
	private final int __index__extended_xyz;
	private final int __index___abc;
	private final int __index__extended_abc;
}
