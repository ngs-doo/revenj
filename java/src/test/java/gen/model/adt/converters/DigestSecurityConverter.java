package gen.model.adt.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class DigestSecurityConverter implements ObjectConverter<gen.model.adt.DigestSecurity> {

	@SuppressWarnings("unchecked")
	public DigestSecurityConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "adt".equals(it.typeSchema) && "DigestSecurity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "adt".equals(it.typeSchema) && "-ngs_DigestSecurity_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		column = columns.stream().filter(it -> "username".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'username' column in adt DigestSecurity. Check if DB is in sync");
		__index___username = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "username".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'username' column in adt DigestSecurity. Check if DB is in sync");
		__index__extended_username = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "passwordHash".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'passwordHash' column in adt DigestSecurity. Check if DB is in sync");
		__index___passwordHash = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "passwordHash".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'passwordHash' column in adt DigestSecurity. Check if DB is in sync");
		__index__extended_passwordHash = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.adt.DigestSecurity.__configureConverter(readers, __index___username, __index___passwordHash);
			
		gen.model.adt.DigestSecurity.__configureConverterExtended(readersExtended, __index__extended_username, __index__extended_passwordHash);
	}

	@Override
	public String getDbName() {
		return "\"adt\".\"DigestSecurity\"";
	}

	@Override
	public gen.model.adt.DigestSecurity from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.adt.DigestSecurity from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.adt.DigestSecurity>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.adt.DigestSecurity instance = new gen.model.adt.DigestSecurity(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.adt.DigestSecurity instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___username] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getUsername());
		items[__index___passwordHash] = org.revenj.postgres.converters.ByteaConverter.toTuple(instance.getPasswordHash());
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.adt.DigestSecurity>[] readers;
	
	public gen.model.adt.DigestSecurity from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.adt.DigestSecurity instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.adt.DigestSecurity from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.adt.DigestSecurity instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_username] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getUsername());
		items[__index__extended_passwordHash] = org.revenj.postgres.converters.ByteaConverter.toTuple(instance.getPasswordHash());
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.adt.DigestSecurity>[] readersExtended;
	
	public gen.model.adt.DigestSecurity fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.adt.DigestSecurity instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.adt.DigestSecurity fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	private final int __index___username;
	private final int __index__extended_username;
	private final int __index___passwordHash;
	private final int __index__extended_passwordHash;
}
