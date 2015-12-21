/*
* Created by DSL Platform
* v1.0.0.29923 
*/

package gen.model.mixinReference.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class PersonConverter implements ObjectConverter<gen.model.mixinReference.Person> {

	@SuppressWarnings("unchecked")
	public PersonConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "mixinReference".equals(it.typeSchema) && "Person_entity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "mixinReference".equals(it.typeSchema) && "-ngs_Person_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		column = columns.stream().filter(it -> "birth".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'birth' column in mixinReference Person_entity. Check if DB is in sync");
		__index___birth = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "birth".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'birth' column in mixinReference Person. Check if DB is in sync");
		__index__extended_birth = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "dayOfBirth".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'dayOfBirth' column in mixinReference Person_entity. Check if DB is in sync");
		__index___dayOfBirth = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "dayOfBirth".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'dayOfBirth' column in mixinReference Person. Check if DB is in sync");
		__index__extended_dayOfBirth = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "bornOnEvenDay".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'bornOnEvenDay' column in mixinReference Person_entity. Check if DB is in sync");
		__index___bornOnEvenDay = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "bornOnEvenDay".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'bornOnEvenDay' column in mixinReference Person. Check if DB is in sync");
		__index__extended_bornOnEvenDay = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "AuthorID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'AuthorID' column in mixinReference Person_entity. Check if DB is in sync");
		__index___AuthorID = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "AuthorID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'AuthorID' column in mixinReference Person. Check if DB is in sync");
		__index__extended_AuthorID = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.mixinReference.Person.__configureConverter(readers, __index___birth, __index___dayOfBirth, __index___bornOnEvenDay, __index___AuthorID);
			
		gen.model.mixinReference.Person.__configureConverterExtended(readersExtended, __index__extended_birth, __index__extended_dayOfBirth, __index__extended_bornOnEvenDay, __index__extended_AuthorID);
	}

	@Override
	public String getDbName() {
		return "\"mixinReference\".\"Person_entity\"";
	}

	@Override
	public gen.model.mixinReference.Person from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.mixinReference.Person from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.mixinReference.Person>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.mixinReference.Person instance = new gen.model.mixinReference.Person(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.mixinReference.Person instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___birth] = org.revenj.postgres.converters.DateConverter.toTuple(instance.getBirth());
		items[__index___dayOfBirth] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getDayOfBirth());
		items[__index___bornOnEvenDay] = org.revenj.postgres.converters.BoolConverter.toTuple(instance.getBornOnEvenDay());
		items[__index___AuthorID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getAuthorID());
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.mixinReference.Person>[] readers;
	
	public gen.model.mixinReference.Person from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.mixinReference.Person instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.mixinReference.Person from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	
	public PostgresTuple toExtended(gen.model.mixinReference.Person instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_birth] = org.revenj.postgres.converters.DateConverter.toTuple(instance.getBirth());
		items[__index__extended_dayOfBirth] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getDayOfBirth());
		items[__index__extended_bornOnEvenDay] = org.revenj.postgres.converters.BoolConverter.toTuple(instance.getBornOnEvenDay());
		items[__index__extended_AuthorID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getAuthorID());
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.mixinReference.Person>[] readersExtended;
	
	public gen.model.mixinReference.Person fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.mixinReference.Person instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.mixinReference.Person fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	private final int __index___birth;
	private final int __index__extended_birth;
	private final int __index___dayOfBirth;
	private final int __index__extended_dayOfBirth;
	private final int __index___bornOnEvenDay;
	private final int __index__extended_bornOnEvenDay;
	private final int __index___AuthorID;
	private final int __index__extended_AuthorID;
	
	public static String buildURI(org.revenj.postgres.PostgresBuffer _sw, gen.model.mixinReference.Person instance) throws java.io.IOException {
		_sw.initBuffer();
		String _tmp;
		org.revenj.postgres.converters.IntConverter.serializeURI(_sw, instance.getAuthorID());
		return _sw.bufferToString();
	}
}
