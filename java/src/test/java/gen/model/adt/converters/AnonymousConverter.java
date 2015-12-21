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

public class AnonymousConverter implements ObjectConverter<gen.model.adt.Anonymous> {

	@SuppressWarnings("unchecked")
	public AnonymousConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "adt".equals(it.typeSchema) && "Anonymous".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "adt".equals(it.typeSchema) && "-ngs_Anonymous_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.adt.Anonymous.__configureConverter(readers);
			
		gen.model.adt.Anonymous.__configureConverterExtended(readersExtended);
	}

	@Override
	public String getDbName() {
		return "\"adt\".\"Anonymous\"";
	}

	@Override
	public gen.model.adt.Anonymous from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.adt.Anonymous from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.adt.Anonymous>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.adt.Anonymous instance = new gen.model.adt.Anonymous(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.adt.Anonymous instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.adt.Anonymous>[] readers;
	
	public gen.model.adt.Anonymous from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.adt.Anonymous instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.adt.Anonymous from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.adt.Anonymous instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.adt.Anonymous>[] readersExtended;
	
	public gen.model.adt.Anonymous fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.adt.Anonymous instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.adt.Anonymous fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
}
