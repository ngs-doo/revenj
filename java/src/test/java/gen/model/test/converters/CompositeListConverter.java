/*
* Created by DSL Platform
* v1.0.0.32432 
*/

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
			
		column = columns.stream().filter(it -> "en".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'en' column in test CompositeList_snowflake. Check if DB is in sync");
		__index___en = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "en".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'en' column in test CompositeList. Check if DB is in sync");
		__index__extended_en = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "tsl".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'tsl' column in test CompositeList_snowflake. Check if DB is in sync");
		__index___tsl = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "tsl".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'tsl' column in test CompositeList. Check if DB is in sync");
		__index__extended_tsl = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "change".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'change' column in test CompositeList_snowflake. Check if DB is in sync");
		__index___change = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "change".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'change' column in test CompositeList. Check if DB is in sync");
		__index__extended_change = (int)column.get().order - 1;
			
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
			
		column = columns.stream().filter(it -> "number".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'number' column in test CompositeList_snowflake. Check if DB is in sync");
		__index___number = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "number".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'number' column in test CompositeList. Check if DB is in sync");
		__index__extended_number = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "entitiesCount".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'entitiesCount' column in test CompositeList_snowflake. Check if DB is in sync");
		__index___entitiesCount = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "entitiesCount".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'entitiesCount' column in test CompositeList. Check if DB is in sync");
		__index__extended_entitiesCount = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "hasEntities".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'hasEntities' column in test CompositeList_snowflake. Check if DB is in sync");
		__index___hasEntities = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "hasEntities".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'hasEntities' column in test CompositeList. Check if DB is in sync");
		__index__extended_hasEntities = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "entityHasMoney".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'entityHasMoney' column in test CompositeList_snowflake. Check if DB is in sync");
		__index___entityHasMoney = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "entityHasMoney".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'entityHasMoney' column in test CompositeList. Check if DB is in sync");
		__index__extended_entityHasMoney = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "indexes".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'indexes' column in test CompositeList_snowflake. Check if DB is in sync");
		__index___indexes = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "indexes".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'indexes' column in test CompositeList. Check if DB is in sync");
		__index__extended_indexes = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		__converter_entities = locator.resolve(gen.model.test.converters.EntityConverter.class);
		__converter_simple = locator.resolve(gen.model.test.converters.SimpleConverter.class);
		
	}

	@Override
	public String getDbName() {
		return "\"test\".\"CompositeList_snowflake\"";
	}

	@Override
	public PostgresTuple to(gen.model.test.CompositeList instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	
	@Override
	public gen.model.test.CompositeList from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.test.CompositeList result = from(reader, context, context == 0 ? 1 : context << 1);
		reader.read();
		return result;
	}

	public gen.model.test.CompositeList from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		reader.read(outerContext);
		int i = 0;
		
		String _URI_ = null;
		java.util.UUID _id_ = null;
		gen.model.test.En[] _enn_ = null;
		gen.model.test.En _en_ = null;
		java.util.List<java.time.OffsetDateTime> _tsl_ = null;
		java.time.LocalDate _change_ = null;
		java.util.List<gen.model.test.Entity> _entities_ = null;
		gen.model.test.Simple _simple_ = null;
		int _number_ = 0;
		int _entitiesCount_ = 0;
		boolean _hasEntities_ = false;
		boolean[] _entityHasMoney_ = null;
		long[] _indexes_ = null;
		for(int x = 0; x < columnCount && i < columnCount; x++) {
			
			if (__index___URI == i) { _URI_ = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); i++; }
			if (__index___id == i) { _id_ = org.revenj.postgres.converters.UuidConverter.parse(reader, false); i++; }
			if (__index___enn == i) { { java.util.List<gen.model.test.En> __list = org.revenj.postgres.converters.EnumConverter.parseCollection(reader, context, gen.model.test.En.A, gen.model.test.converters.EnConverter::convertEnum); if (__list != null) {_enn_ = __list.toArray(new gen.model.test.En[__list.size()]);} else _enn_ = new gen.model.test.En[] { }; }; i++; }
			if (__index___en == i) { _en_ = gen.model.test.converters.EnConverter.fromReader(reader); i++; }
			if (__index___tsl == i) { { java.util.List<java.time.OffsetDateTime> __list = org.revenj.postgres.converters.TimestampConverter.parseOffsetCollection(reader, context, false, true); if(__list != null) {_tsl_ = __list;} else _tsl_ = new java.util.ArrayList<java.time.OffsetDateTime>(4); }; i++; }
			if (__index___change == i) { _change_ = org.revenj.postgres.converters.DateConverter.parse(reader, false); i++; }
			if (__index___entities == i) { { java.util.List<gen.model.test.Entity> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, __converter_entities::from); if (__list != null) {_entities_ = __list;} else _entities_ = new java.util.ArrayList<gen.model.test.Entity>(4); }; i++; }
			if (__index___simple == i) { _simple_ = __converter_simple.from(reader, context); i++; }
			if (__index___number == i) { _number_ = org.revenj.postgres.converters.IntConverter.parse(reader); i++; }
			if (__index___entitiesCount == i) { _entitiesCount_ = org.revenj.postgres.converters.IntConverter.parse(reader); i++; }
			if (__index___hasEntities == i) { _hasEntities_ = org.revenj.postgres.converters.BoolConverter.parse(reader); i++; }
			if (__index___entityHasMoney == i) { { java.util.List<Boolean> __list = org.revenj.postgres.converters.BoolConverter.parseCollection(reader, context, false); if(__list != null) {
				boolean[] __resUnboxed = new boolean[__list.size()];
				for(int _i=0;_i<__list.size();_i++) {
					__resUnboxed[_i] = __list.get(_i);
				}
				_entityHasMoney_ = __resUnboxed;
			} else _entityHasMoney_ = new boolean[] { }; }; i++; }
			if (__index___indexes == i) { { java.util.List<Long> __list = org.revenj.postgres.converters.LongConverter.parseCollection(reader, context, false); if(__list != null) {
				long[] __resUnboxed = new long[__list.size()];
				for(int _i=0;_i<__list.size();_i++) {
					__resUnboxed[_i] = __list.get(_i);
				}
				_indexes_ = __resUnboxed;
			} }; i++; }
		}
		gen.model.test.CompositeList instance = new gen.model.test.CompositeList(_URI_, _id_, _enn_, _en_, _tsl_, _change_, _entities_, _simple_, _number_, _entitiesCount_, _hasEntities_, _entityHasMoney_, _indexes_);
		reader.read(outerContext);
		return instance;
	}
	private final int __index___URI;
	private final int columnCountExtended;
	
	public PostgresTuple toExtended(gen.model.test.CompositeList instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		return RecordTuple.from(items);
	}

	public gen.model.test.CompositeList fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.test.CompositeList result = fromExtended(reader, context, context == 0 ? 1 : context << 1);
		reader.read();
		return result;
	}

	public gen.model.test.CompositeList fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		reader.read(outerContext);
		int i = 0;
		
		String _URI_ = null;
		java.util.UUID _id_ = null;
		gen.model.test.En[] _enn_ = null;
		gen.model.test.En _en_ = null;
		java.util.List<java.time.OffsetDateTime> _tsl_ = null;
		java.time.LocalDate _change_ = null;
		java.util.List<gen.model.test.Entity> _entities_ = null;
		gen.model.test.Simple _simple_ = null;
		int _number_ = 0;
		int _entitiesCount_ = 0;
		boolean _hasEntities_ = false;
		boolean[] _entityHasMoney_ = null;
		long[] _indexes_ = null;
		for(int x = 0; x < columnCountExtended && i < columnCountExtended; x++) {
			
			if (__index__extended_URI == i) { _URI_ = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); i++; }
			if (__index__extended_id == i) { _id_ = org.revenj.postgres.converters.UuidConverter.parse(reader, false); i++; }
			if (__index__extended_enn == i) { { java.util.List<gen.model.test.En> __list = org.revenj.postgres.converters.EnumConverter.parseCollection(reader, context, gen.model.test.En.A, gen.model.test.converters.EnConverter::convertEnum); if (__list != null) {_enn_ = __list.toArray(new gen.model.test.En[__list.size()]);} else _enn_ = new gen.model.test.En[] { }; }; i++; }
			if (__index__extended_en == i) { _en_ = gen.model.test.converters.EnConverter.fromReader(reader); i++; }
			if (__index__extended_tsl == i) { { java.util.List<java.time.OffsetDateTime> __list = org.revenj.postgres.converters.TimestampConverter.parseOffsetCollection(reader, context, false, true); if(__list != null) {_tsl_ = __list;} else _tsl_ = new java.util.ArrayList<java.time.OffsetDateTime>(4); }; i++; }
			if (__index__extended_change == i) { _change_ = org.revenj.postgres.converters.DateConverter.parse(reader, false); i++; }
			if (__index__extended_entities == i) { { java.util.List<gen.model.test.Entity> __list = org.revenj.postgres.converters.ArrayTuple.parse(reader, context, __converter_entities::from); if (__list != null) {_entities_ = __list;} else _entities_ = new java.util.ArrayList<gen.model.test.Entity>(4); }; i++; }
			if (__index__extended_simple == i) { _simple_ = __converter_simple.fromExtended(reader, context); i++; }
			if (__index__extended_number == i) { _number_ = org.revenj.postgres.converters.IntConverter.parse(reader); i++; }
			if (__index__extended_entitiesCount == i) { _entitiesCount_ = org.revenj.postgres.converters.IntConverter.parse(reader); i++; }
			if (__index__extended_hasEntities == i) { _hasEntities_ = org.revenj.postgres.converters.BoolConverter.parse(reader); i++; }
			if (__index__extended_entityHasMoney == i) { { java.util.List<Boolean> __list = org.revenj.postgres.converters.BoolConverter.parseCollection(reader, context, false); if(__list != null) {
				boolean[] __resUnboxed = new boolean[__list.size()];
				for(int _i=0;_i<__list.size();_i++) {
					__resUnboxed[_i] = __list.get(_i);
				}
				_entityHasMoney_ = __resUnboxed;
			} else _entityHasMoney_ = new boolean[] { }; }; i++; }
			if (__index__extended_indexes == i) { { java.util.List<Long> __list = org.revenj.postgres.converters.LongConverter.parseCollection(reader, context, false); if(__list != null) {
				long[] __resUnboxed = new long[__list.size()];
				for(int _i=0;_i<__list.size();_i++) {
					__resUnboxed[_i] = __list.get(_i);
				}
				_indexes_ = __resUnboxed;
			} }; i++; }
		}
		gen.model.test.CompositeList instance = new gen.model.test.CompositeList(_URI_, _id_, _enn_, _en_, _tsl_, _change_, _entities_, _simple_, _number_, _entitiesCount_, _hasEntities_, _entityHasMoney_, _indexes_);
		reader.read(outerContext);
		return instance;
	}
	private final int __index__extended_URI;
	private final int __index___id;
	private final int __index__extended_id;
	private final int __index___enn;
	private final int __index__extended_enn;
	private final int __index___en;
	private final int __index__extended_en;
	private final int __index___tsl;
	private final int __index__extended_tsl;
	private final int __index___change;
	private final int __index__extended_change;
	private gen.model.test.converters.EntityConverter __converter_entities;
	private final int __index___entities;
	private final int __index__extended_entities;
	private gen.model.test.converters.SimpleConverter __converter_simple;
	private final int __index___simple;
	private final int __index__extended_simple;
	private final int __index___number;
	private final int __index__extended_number;
	private final int __index___entitiesCount;
	private final int __index__extended_entitiesCount;
	private final int __index___hasEntities;
	private final int __index__extended_hasEntities;
	private final int __index___entityHasMoney;
	private final int __index__extended_entityHasMoney;
	private final int __index___indexes;
	private final int __index__extended_indexes;
}
