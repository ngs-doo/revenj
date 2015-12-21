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

public class UserConverter implements ObjectConverter<gen.model.adt.User> {

	@SuppressWarnings("unchecked")
	public UserConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "adt".equals(it.typeSchema) && "User_entity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "adt".equals(it.typeSchema) && "-ngs_User_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		column = columns.stream().filter(it -> "username".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'username' column in adt User_entity. Check if DB is in sync");
		__index___username = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "username".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'username' column in adt User. Check if DB is in sync");
		__index__extended_username = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "authentication".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'authentication' column in adt User_entity. Check if DB is in sync");
		__index___authentication = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "authentication".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'authentication' column in adt User. Check if DB is in sync");
		__index__extended_authentication = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		__converter_authentication = locator.resolve(gen.model.adt.converters.AuthConverter.class);
		
			
		gen.model.adt.User.__configureConverter(readers, __index___username, __converter_authentication, __index___authentication);
			
		gen.model.adt.User.__configureConverterExtended(readersExtended, __index__extended_username, __converter_authentication, __index__extended_authentication);
	}

	@Override
	public String getDbName() {
		return "\"adt\".\"User_entity\"";
	}

	@Override
	public gen.model.adt.User from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.adt.User from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.adt.User>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.adt.User instance = new gen.model.adt.User(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.adt.User instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___username] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getUsername());
		items[__index___authentication] = __converter_authentication.to(instance.getAuthentication());
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.adt.User>[] readers;
	
	public gen.model.adt.User from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.adt.User instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.adt.User from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.adt.User instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_username] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getUsername());
		items[__index__extended_authentication] = __converter_authentication.toExtended(instance.getAuthentication());
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.adt.User>[] readersExtended;
	
	public gen.model.adt.User fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.adt.User instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.adt.User fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	
	public static String buildURI(org.revenj.postgres.PostgresBuffer _sw, gen.model.adt.User instance) throws java.io.IOException {
		_sw.initBuffer();
		String _tmp;
		org.revenj.postgres.converters.StringConverter.serializeURI(_sw, instance.getUsername());
		return _sw.bufferToString();
	}
	private final int __index___username;
	private final int __index__extended_username;
	private gen.model.adt.converters.AuthConverter __converter_authentication;
	private final int __index___authentication;
	private final int __index__extended_authentication;
}
