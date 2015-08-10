package gen.model.test.converters;



import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class EnConverter implements ObjectConverter<gen.model.test.En> {

	private static final PostgresTuple DEFAULT_TUPLE = org.revenj.postgres.converters.EnumConverter.toTuple(gen.model.test.En.A);
	
	private static final PostgresTuple TUPLE_A = org.revenj.postgres.converters.EnumConverter.toTuple(gen.model.test.En.A);
	private static final PostgresTuple TUPLE_B = org.revenj.postgres.converters.EnumConverter.toTuple(gen.model.test.En.B);

	@Override
	public String getDbName() {
		return "\"test\".\"En\"";
	}

	@Override
	public gen.model.test.En from(PostgresReader reader, int context) throws java.io.IOException {
		return fromReader(reader);
	}

	public static gen.model.test.En fromReader(PostgresReader reader) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return null;
		}
		reader.initBuffer((char)cur);
		reader.fillUntil(',', ')');
		gen.model.test.En result = convertEnum(reader);
		reader.read();
		return result;
	}

	@Override
	public PostgresTuple to(gen.model.test.En instance) {
		return toTuple(instance);
	}
	
	public static PostgresTuple toTuple(gen.model.test.En instance) {
		if (instance == null) return null;
		
		switch (instance) { 
			case A:
				return TUPLE_A;
			case B:
				return TUPLE_B;
		}
		return DEFAULT_TUPLE;
	}

	public static String stringValue(gen.model.test.En item) {
		
		switch (item) { 
			case A:
				return "A";
			case B:
				return "B";
		}
		return "";
	}

	public static gen.model.test.En convertEnum(PostgresReader reader) {
		
		switch (reader.bufferHash()) { 
			case -1005848884:
				return gen.model.test.En.A;
			case -955516027:
				return gen.model.test.En.B;
		}
		return gen.model.test.En.A;
	}
}
