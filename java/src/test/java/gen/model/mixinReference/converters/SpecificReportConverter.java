package gen.model.mixinReference.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class SpecificReportConverter implements ObjectConverter<gen.model.mixinReference.SpecificReport> {

	@SuppressWarnings("unchecked")
	public SpecificReportConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "mixinReference".equals(it.typeSchema) && "SpecificReport_entity".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "mixinReference".equals(it.typeSchema) && "-ngs_SpecificReport_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> StringConverter.skip(rdr, ctx);
		}
			
		column = columns.stream().filter(it -> "ID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ID' column in mixinReference SpecificReport_entity. Check if DB is in sync");
		__index___ID = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "ID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ID' column in mixinReference SpecificReport. Check if DB is in sync");
		__index__extended_ID = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "authorURI".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'authorURI' column in mixinReference SpecificReport_entity. Check if DB is in sync");
		__index___authorURI = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "authorURI".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'authorURI' column in mixinReference SpecificReport. Check if DB is in sync");
		__index__extended_authorURI = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "authorID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'authorID' column in mixinReference SpecificReport_entity. Check if DB is in sync");
		__index___authorID = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "authorID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'authorID' column in mixinReference SpecificReport. Check if DB is in sync");
		__index__extended_authorID = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			
		gen.model.mixinReference.SpecificReport.__configureConverter(readers, __index___ID, __index___authorURI, __index___authorID);
			
		gen.model.mixinReference.SpecificReport.__configureConverterExtended(readersExtended, __index__extended_ID, __index__extended_authorURI, __index__extended_authorID);
	}

	@Override
	public gen.model.mixinReference.SpecificReport from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.mixinReference.SpecificReport from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.mixinReference.SpecificReport>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.mixinReference.SpecificReport instance = new gen.model.mixinReference.SpecificReport(reader, context, readers);
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.mixinReference.SpecificReport instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		items[__index___ID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getID());
		if (instance.getAuthorURI() != null)items[__index___authorURI] = new org.revenj.postgres.converters.ValueTuple(instance.getAuthorURI());;
		items[__index___authorID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getAuthorID());
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.mixinReference.SpecificReport>[] readers;
	
	public gen.model.mixinReference.SpecificReport from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.mixinReference.SpecificReport instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}
	
	public PostgresTuple toExtended(gen.model.mixinReference.SpecificReport instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		items[__index__extended_ID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getID());
		if (instance.getAuthorURI() != null)items[__index__extended_authorURI] = new org.revenj.postgres.converters.ValueTuple(instance.getAuthorURI());;
		items[__index__extended_authorID] = org.revenj.postgres.converters.IntConverter.toTuple(instance.getAuthorID());
		return RecordTuple.from(items);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.mixinReference.SpecificReport>[] readersExtended;
	
	public gen.model.mixinReference.SpecificReport fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.mixinReference.SpecificReport instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}
	private final int __index___ID;
	private final int __index__extended_ID;
	
	public static String buildURI(char[] _buf, int ID) throws java.io.IOException {
		int _len = 0;
		String _tmp;
		_len = org.revenj.postgres.converters.IntConverter.serializeURI(_buf, _len, ID);
		return new String(_buf, 0, _len);
	}
	private final int __index___authorURI;
	private final int __index__extended_authorURI;
	private final int __index___authorID;
	private final int __index__extended_authorID;
}
