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

public class AuthConverter implements ObjectConverter<gen.model.adt.Auth> {

	@SuppressWarnings("unchecked")
	public AuthConverter(List<ObjectConverter.ColumnInfo> allColumns) throws java.io.IOException {
		Optional<ObjectConverter.ColumnInfo> column;
		
			
		final java.util.List<ObjectConverter.ColumnInfo> columns =
				allColumns.stream().filter(it -> "adt".equals(it.typeSchema) && "Auth".equals(it.typeName))
				.collect(Collectors.toList());
		columnCount = columns.size();
			
		readers = new ObjectConverter.Reader[columnCount];
		for (int i = 0; i < readers.length; i++) {
			readers[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		final java.util.List<ObjectConverter.ColumnInfo> columnsExtended =
				allColumns.stream().filter(it -> "adt".equals(it.typeSchema) && "-ngs_Auth_type-".equals(it.typeName))
				.collect(Collectors.toList());
		columnCountExtended = columnsExtended.size();
			
		readersExtended = new ObjectConverter.Reader[columnCountExtended];
		for (int i = 0; i < readersExtended.length; i++) {
			readersExtended[i] = (instance, rdr, ctx) -> { StringConverter.skip(rdr, ctx); return instance; };
		}
			
		column = columns.stream().filter(it -> "adt.BasicSecurity".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'adt.BasicSecurity' column in adt Auth. Check if DB is in sync");
		__obect_index__adt_BasicSecurity = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "adt.BasicSecurity".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'adt.BasicSecurity' column in adt Auth. Check if DB is in sync");
		__obect_index_extended_adt_BasicSecurity = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "adt.Token".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'adt.Token' column in adt Auth. Check if DB is in sync");
		__obect_index__adt_Token = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "adt.Token".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'adt.Token' column in adt Auth. Check if DB is in sync");
		__obect_index_extended_adt_Token = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "adt.Anonymous".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'adt.Anonymous' column in adt Auth. Check if DB is in sync");
		__obect_index__adt_Anonymous = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "adt.Anonymous".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'adt.Anonymous' column in adt Auth. Check if DB is in sync");
		__obect_index_extended_adt_Anonymous = (int)column.get().order - 1;
			
		column = columns.stream().filter(it -> "adt.DigestSecurity".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'adt.DigestSecurity' column in adt Auth. Check if DB is in sync");
		__obect_index__adt_DigestSecurity = (int)column.get().order - 1;
			
		column = columnsExtended.stream().filter(it -> "adt.DigestSecurity".equals(it.columnName)).findAny();
		if (!column.isPresent()) throw new java.io.IOException("Unable to find 'adt.DigestSecurity' column in adt Auth. Check if DB is in sync");
		__obect_index_extended_adt_DigestSecurity = (int)column.get().order - 1;
	}

	public void configure(org.revenj.patterns.ServiceLocator locator) {
		
		
			__converter_adt$BasicSecurity = locator.resolve(gen.model.adt.converters.BasicSecurityConverter.class);
			readers[__obect_index__adt_BasicSecurity] = (item, reader, context) -> { gen.model.adt.BasicSecurity inst = __converter_adt$BasicSecurity.from(reader, context); return item != null ? item : inst; };
			readersExtended[__obect_index_extended_adt_BasicSecurity] = (item, reader, context) -> { gen.model.adt.BasicSecurity inst = __converter_adt$BasicSecurity.fromExtended(reader, context); return item != null ? item : inst; };
			__converter_adt$Token = locator.resolve(gen.model.adt.converters.TokenConverter.class);
			readers[__obect_index__adt_Token] = (item, reader, context) -> { gen.model.adt.Token inst = __converter_adt$Token.from(reader, context); return item != null ? item : inst; };
			readersExtended[__obect_index_extended_adt_Token] = (item, reader, context) -> { gen.model.adt.Token inst = __converter_adt$Token.fromExtended(reader, context); return item != null ? item : inst; };
			__converter_adt$Anonymous = locator.resolve(gen.model.adt.converters.AnonymousConverter.class);
			readers[__obect_index__adt_Anonymous] = (item, reader, context) -> { gen.model.adt.Anonymous inst = __converter_adt$Anonymous.from(reader, context); return item != null ? item : inst; };
			readersExtended[__obect_index_extended_adt_Anonymous] = (item, reader, context) -> { gen.model.adt.Anonymous inst = __converter_adt$Anonymous.fromExtended(reader, context); return item != null ? item : inst; };
			__converter_adt$DigestSecurity = locator.resolve(gen.model.adt.converters.DigestSecurityConverter.class);
			readers[__obect_index__adt_DigestSecurity] = (item, reader, context) -> { gen.model.adt.DigestSecurity inst = __converter_adt$DigestSecurity.from(reader, context); return item != null ? item : inst; };
			readersExtended[__obect_index_extended_adt_DigestSecurity] = (item, reader, context) -> { gen.model.adt.DigestSecurity inst = __converter_adt$DigestSecurity.fromExtended(reader, context); return item != null ? item : inst; };
	}

	@Override
	public String getDbName() {
		return "\"adt\".\"Auth\"";
	}

	@Override
	public gen.model.adt.Auth from(PostgresReader reader) throws java.io.IOException {
		return from(reader, 0);
	}

	private gen.model.adt.Auth from(PostgresReader reader, int outerContext, int context, ObjectConverter.Reader<gen.model.adt.Auth>[] readers) throws java.io.IOException {
		reader.read(outerContext);
		gen.model.adt.Auth instance = null;
		for (ObjectConverter.Reader<gen.model.adt.Auth> rdr : readers) {
			instance = rdr.read(instance, reader, context);
		}
		
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(gen.model.adt.Auth instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		
		if (instance instanceof gen.model.adt.BasicSecurity)items[__obect_index__adt_BasicSecurity] = __converter_adt$BasicSecurity.to((gen.model.adt.BasicSecurity)instance);
		if (instance instanceof gen.model.adt.Token)items[__obect_index__adt_Token] = __converter_adt$Token.to((gen.model.adt.Token)instance);
		if (instance instanceof gen.model.adt.Anonymous)items[__obect_index__adt_Anonymous] = __converter_adt$Anonymous.to((gen.model.adt.Anonymous)instance);
		if (instance instanceof gen.model.adt.DigestSecurity)items[__obect_index__adt_DigestSecurity] = __converter_adt$DigestSecurity.to((gen.model.adt.DigestSecurity)instance);
		return RecordTuple.from(items);
	}

	public PostgresTuple toExtended(gen.model.adt.Auth instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCountExtended];
		
		if (instance instanceof gen.model.adt.BasicSecurity)items[__obect_index_extended_adt_BasicSecurity] = __converter_adt$BasicSecurity.toExtended((gen.model.adt.BasicSecurity)instance);
		if (instance instanceof gen.model.adt.Token)items[__obect_index_extended_adt_Token] = __converter_adt$Token.toExtended((gen.model.adt.Token)instance);
		if (instance instanceof gen.model.adt.Anonymous)items[__obect_index_extended_adt_Anonymous] = __converter_adt$Anonymous.toExtended((gen.model.adt.Anonymous)instance);
		if (instance instanceof gen.model.adt.DigestSecurity)items[__obect_index_extended_adt_DigestSecurity] = __converter_adt$DigestSecurity.toExtended((gen.model.adt.DigestSecurity)instance);
		return RecordTuple.from(items);
	}


	
	private final int columnCount;
	private final ObjectConverter.Reader<gen.model.adt.Auth>[] readers;
	
	public gen.model.adt.Auth from(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.adt.Auth instance = from(reader, context, context == 0 ? 1 : context << 1, readers);
		reader.read();
		return instance;
	}

	public gen.model.adt.Auth from(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readers);
	}
	private final int columnCountExtended;
	private final ObjectConverter.Reader<gen.model.adt.Auth>[] readersExtended;
	
	public gen.model.adt.Auth fromExtended(PostgresReader reader, int context) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		gen.model.adt.Auth instance = from(reader, context, context == 0 ? 1 : context << 1, readersExtended);
		reader.read();
		return instance;
	}

	public gen.model.adt.Auth fromExtended(PostgresReader reader, int outerContext, int context) throws java.io.IOException {
		return from(reader, outerContext, context, readersExtended);
	}
	private final int __obect_index__adt_BasicSecurity;
	private gen.model.adt.converters.BasicSecurityConverter __converter_adt$BasicSecurity;
	private final int __obect_index_extended_adt_BasicSecurity;
	private final int __obect_index__adt_Token;
	private gen.model.adt.converters.TokenConverter __converter_adt$Token;
	private final int __obect_index_extended_adt_Token;
	private final int __obect_index__adt_Anonymous;
	private gen.model.adt.converters.AnonymousConverter __converter_adt$Anonymous;
	private final int __obect_index_extended_adt_Anonymous;
	private final int __obect_index__adt_DigestSecurity;
	private gen.model.adt.converters.DigestSecurityConverter __converter_adt$DigestSecurity;
	private final int __obect_index_extended_adt_DigestSecurity;
}
