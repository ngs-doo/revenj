package gen.model.test.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class CompositeListConverter implements ObjectConverter<gen.model.test.CompositeList> {

	@SuppressWarnings("unchecked")
	public CompositeListConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "test".equals(it.typeSchema) && "CompositeList_snowflake".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		column = columns.stream().filter(it -> "URI".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'URI' column in test CompositeList_snowflake. Check if DB is in sync");
		__index___URI = (int)column.get().order - 1;
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "test".equals(it.typeSchema) && "-ngs_CompositeList_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		column = columnsExtended.stream().filter(it -> "URI".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'URI' column in test CompositeList. Check if DB is in sync");
		__index__extended_URI = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "id".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'id' column in test CompositeList_snowflake. Check if DB is in sync");
		__index___id = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "id".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'id' column in test CompositeList. Check if DB is in sync");
		__index__extended_id = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "enn".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'enn' column in test CompositeList_snowflake. Check if DB is in sync");
		__index___enn = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "enn".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'enn' column in test CompositeList. Check if DB is in sync");
		__index__extended_enn = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "entities".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'entities' column in test CompositeList_snowflake. Check if DB is in sync");
		__index___entities = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "entities".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'entities' column in test CompositeList. Check if DB is in sync");
		__index__extended_entities = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "simple".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'simple' column in test CompositeList_snowflake. Check if DB is in sync");
		__index___simple = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "simple".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'simple' column in test CompositeList. Check if DB is in sync");
		__index__extended_simple = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		__converter_entities = locator.resolve(gen.model.test.converters.EntityConverter.class);
		__converter_simple = locator.resolve(gen.model.test.converters.SimpleConverter.class);
		
	}

	@Override
	public gen.model.test.CompositeList from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	@Override
	public PostgresTuple to(gen.model.test.CompositeList instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		return RecordTuple.from(items);
	}

	public PostgresTuple toExtended(gen.model.test.CompositeList instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	
	public gen.model.test.CompositeList from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		context = context == 0 ? 1 : context << 1;
		reader.read(context);
		int i = 0;
		
		String _URI_ = null;
		java.util.UUID _id_ = new java.util.UUID(0L, 0L);
		gen.model.test.En[] _enn_ = null;
		java.util.List<gen.model.test.Entity> _entities_ = null;
		gen.model.test.Simple _simple_ = null;
		for(int x = 0; x < columnCount && i < columnCount; x++) {
			
			if (__index___URI == i) { _URI_ = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); i++; }
			if (__index___id == i) { _id_ = org.revenj.postgres.converters.UuidConverter.parse(reader, false); i++; }
			if (__index___enn == i) { { java.util.List<gen.model.test.En> __list = org.revenj.postgres.converters.EnumConverter.parseCollection(reader, context, gen.model.test.En.A, gen.model.test.converters.EnConverter::convertEnum); if (__list != null) _enn_ = __list.toArray(new gen.model.test.En[__list.size()]); else _enn_ = new gen.model.test.En[] { }; }; i++; }
			if (__index___entities == i) { { java.util.List<gen.model.test.Entity> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, (rdr, ctx) -> __converter_entities.from(rdr, ctx)); if (__list != null) _entities_ = __list; else _entities_ = new java.util.ArrayList<gen.model.test.Entity>(4); }; i++; }
			if (__index___simple == i) { _simple_ = __converter_simple.from(reader, context); i++; }
		}
		gen.model.test.CompositeList instance = new gen.model.test.CompositeList(_URI_, _id_, _enn_, _entities_, _simple_);
		reader.read(context + 1);
		return instance;
	}
	private final int __index___URI;
	private final int columnCountExtended;
	
	public gen.model.test.CompositeList fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		context = context == 0 ? 1 : context << 1;
		reader.read(context);
		int i = 0;
		
		String _URI_ = null;
		java.util.UUID _id_ = new java.util.UUID(0L, 0L);
		gen.model.test.En[] _enn_ = null;
		java.util.List<gen.model.test.Entity> _entities_ = null;
		gen.model.test.Simple _simple_ = null;
		for(int x = 0; x < columnCountExtended && i < columnCountExtended; x++) {
			
			if (__index__extended_URI == i) { _URI_ = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); i++; }
			if (__index__extended_id == i) { _id_ = org.revenj.postgres.converters.UuidConverter.parse(reader, false); i++; }
			if (__index__extended_enn == i) { { java.util.List<gen.model.test.En> __list = org.revenj.postgres.converters.EnumConverter.parseCollection(reader, context, gen.model.test.En.A, gen.model.test.converters.EnConverter::convertEnum); if (__list != null) _enn_ = __list.toArray(new gen.model.test.En[__list.size()]); else _enn_ = new gen.model.test.En[] { }; }; i++; }
			if (__index__extended_entities == i) { { java.util.List<gen.model.test.Entity> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, (rdr, ctx) -> __converter_entities.fromExtended(rdr, ctx)); if (__list != null) _entities_ = __list; else _entities_ = new java.util.ArrayList<gen.model.test.Entity>(4); }; i++; }
			if (__index__extended_simple == i) { _simple_ = __converter_simple.fromExtended(reader, context); i++; }
		}
		gen.model.test.CompositeList instance = new gen.model.test.CompositeList(_URI_, _id_, _enn_, _entities_, _simple_);
		reader.read(context + 1);
		return instance;
	}
	private final int __index__extended_URI;
	private final int __index___id;
	private final int __index__extended_id;
	private final int __index___enn;
	private final int __index__extended_enn;
	private gen.model.test.converters.EntityConverter __converter_entities;
	private final int __index___entities;
	private final int __index__extended_entities;
	private gen.model.test.converters.SimpleConverter __converter_simple;
	private final int __index___simple;
	private final int __index__extended_simple;
}
