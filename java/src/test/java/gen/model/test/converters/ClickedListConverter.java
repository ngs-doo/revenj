/*
* Created by DSL Platform
* v1.0.0.12875 
*/

package gen.model.test.converters;



import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class ClickedListConverter implements ObjectConverter<gen.model.test.ClickedList> {

	@SuppressWarnings("unchecked")
	public ClickedListConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "test".equals(it.typeSchema) && "ClickedList".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		column = columns.stream().filter(it -> "date".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'date' column in test ClickedList. Check if DB is in sync");
		__index___date = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "number".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'number' column in test ClickedList. Check if DB is in sync");
		__index___number = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
	}

	@Override
	public String getDbName() {
		return "(SELECT date, number FROM test.\"Clicked\" GROUP BY date, number)";
	}

	@Override
	public PostgresTuple to(gen.model.test.ClickedList instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		return RecordTuple.from(items);
	}

	
	private final int columnCount;
	
	@Override
	public gen.model.test.ClickedList from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.test.ClickedList result = from(reader, context, context == 0 ? 1 : context << 1);
		reader.read();
		return result;
	}

	public gen.model.test.ClickedList from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		reader.read(outerContext);
		int i = 0;
		
		java.time.LocalDate _date_ = null;
		java.math.BigDecimal _number_ = null;
		for(int x = 0; x < columnCount && i < columnCount; x++) {
			
			if (__index___date == i) { _date_ = org.revenj.postgres.converters.DateConverter.parse(reader, true); i++; }
			if (__index___number == i) { _number_ = org.revenj.postgres.converters.DecimalConverter.parse(reader, true); i++; }
		}
		gen.model.test.ClickedList instance = new gen.model.test.ClickedList(_date_, _number_);
		reader.read(outerContext);
		return instance;
	}
	private final int __index___date;
	private final int __index___number;
}
