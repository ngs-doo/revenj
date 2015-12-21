/*
* Created by DSL Platform
* v1.0.0.29923 
*/

package gen.model.binaries.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class ReadOnlyDocumentConverter implements ObjectConverter<gen.model.binaries.ReadOnlyDocument> {

	@SuppressWarnings("unchecked")
	public ReadOnlyDocumentConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "binaries".equals(it.typeSchema) && "ReadOnlyDocument".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		column = columns.stream().filter(it -> "ID".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'ID' column in binaries ReadOnlyDocument. Check if DB is in sync");
		__index___ID = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "name".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'name' column in binaries ReadOnlyDocument. Check if DB is in sync");
		__index___Name = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
	}

	@Override
	public String getDbName() {
		return "(SELECT \"ID\", name from binaries.\"Document\")";
	}

	@Override
	public PostgresTuple to(gen.model.binaries.ReadOnlyDocument instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	
	@Override
	public gen.model.binaries.ReadOnlyDocument from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.binaries.ReadOnlyDocument result = from(reader, context, context == 0 ? 1 : context << 1);
		reader.read();
		return result;
	}

	public gen.model.binaries.ReadOnlyDocument from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		reader.read(outerContext);
		int i = 0;
		
		java.util.UUID _ID_ = null;
		String _Name_ = null;
		for(int x = 0; x < columnCount && i < columnCount; x++) {
			
			if (__index___ID == i) { _ID_ = org.revenj.postgres.converters.UuidConverter.parse(reader, false); i++; }
			if (__index___Name == i) { _Name_ = org.revenj.postgres.converters.StringConverter.parse(reader, context, false); i++; }
		}
		gen.model.binaries.ReadOnlyDocument instance = new gen.model.binaries.ReadOnlyDocument(_ID_, _Name_);
		reader.read(outerContext);
		return instance;
	}
	private final int __index___ID;
	private final int __index___Name;
}
