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

public class TokenConverter implements ObjectConverter<gen.model.adt.Token> {

	@SuppressWarnings("unchecked")
	public TokenConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "adt".equals(it.typeSchema) && "Token".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "adt".equals(it.typeSchema) && "-ngs_Token_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		column = columns.stream().filter(it -> "token".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'token' column in adt Token. Check if DB is in sync");
		__index___token = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "token".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'token' column in adt Token. Check if DB is in sync");
		__index__extended_token = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.adt.Token.__configureConverter(readers, __index___token);
			
		gen.model.adt.Token.__configureConverterExtended(readersExtended, __index__extended_token);
	}

	@Override
	public String getDbName() {
		return "\"adt\".\"Token\"";
	}

	@Override
	public gen.model.adt.Token from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.adt.Token from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.adt.Token>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.adt.Token instance = new gen.model.adt.Token(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.adt.Token instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___token] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getToken());
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.adt.Token>[] readers;
	
	public gen.model.adt.Token from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.adt.Token instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.adt.Token from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.adt.Token instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_token] = org.revenj.postgres.converters.StringConverter.toTuple(instance.getToken());
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.adt.Token>[] readersExtended;
	
	public gen.model.adt.Token fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.adt.Token instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.adt.Token fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	private final int __index___token;
	private final int __index__extended_token;
}
