/*
* Created by DSL Platform
* v1.0.0.29923 
*/

package gen.model.adt.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class BasicSecurityConverter implements ObjectConverter<gen.model.adt.BasicSecurity> {

	@SuppressWarnings("unchecked")
	public BasicSecurityConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "adt".equals(it.typeSchema) && "BasicSecurity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "adt".equals(it.typeSchema) && "-ngs_BasicSecurity_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		column = columns.stream().filter(it -> "username".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'username' column in adt BasicSecurity. Check if DB is in sync");
		__index___username = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "username".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'username' column in adt BasicSecurity. Check if DB is in sync");
		__index__extended_username = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "password".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'password' column in adt BasicSecurity. Check if DB is in sync");
		__index___password = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "password".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'password' column in adt BasicSecurity. Check if DB is in sync");
		__index__extended_password = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.adt.BasicSecurity.__configureConverter(readers, __index___username, __index___password);
			
		gen.model.adt.BasicSecurity.__configureConverterExtended(readersExtended, __index__extended_username, __index__extended_password);
	}

	@Override
	public String getDbName() {
		return "\"adt\".\"BasicSecurity\"";
	}

	@Override
	public gen.model.adt.BasicSecurity from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.adt.BasicSecurity from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.adt.BasicSecurity>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.adt.BasicSecurity instance = new gen.model.adt.BasicSecurity(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.adt.BasicSecurity instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___username] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getUsername());
		items[__index___password] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getPassword());
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.adt.BasicSecurity>[] readers;
	
	public gen.model.adt.BasicSecurity from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.adt.BasicSecurity instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.adt.BasicSecurity from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.adt.BasicSecurity instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_username] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getUsername());
		items[__index__extended_password] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getPassword());
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.adt.BasicSecurity>[] readersExtended;
	
	public gen.model.adt.BasicSecurity fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.adt.BasicSecurity instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.adt.BasicSecurity fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	private final int __index___username;
	private final int __index__extended_username;
	private final int __index___password;
	private final int __index__extended_password;
}
