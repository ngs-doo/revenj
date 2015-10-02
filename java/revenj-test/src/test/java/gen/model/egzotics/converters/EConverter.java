package gen.model.egzotics.converters;



import org.revenj.postgres.*;
import org.revenj.postgres.converters.*;

public class EConverter implements ObjectConverter<gen.model.egzotics.E> {

	private static final PostgresTuple DEFAULT_TUPLE = org.revenj.postgres.converters.EnumConverter.toTuple(gen.model.egzotics.E.A);
	
	private static final PostgresTuple TUPLE_A = org.revenj.postgres.converters.EnumConverter.toTuple(gen.model.egzotics.E.A);
	private static final PostgresTuple TUPLE_B = org.revenj.postgres.converters.EnumConverter.toTuple(gen.model.egzotics.E.B);
	private static final PostgresTuple TUPLE_C = org.revenj.postgres.converters.EnumConverter.toTuple(gen.model.egzotics.E.C);

	@Override
	public String getDbName() {
		return "\"egzotics\".\"E\"";
	}

	@Override
	public gen.model.egzotics.E from(PostgresReader reader, int context) throws java.io.IOException {
		return fromReader(reader);
	}

	public static gen.model.egzotics.E fromReader(PostgresReader reader) throws java.io.IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') {
			return null;
		}
		reader.initBuffer((char)cur);
		reader.fillUntil(',', ')');
		gen.model.egzotics.E result = convertEnum(reader);
		reader.read();
		return result;
	}

	@Override
	public PostgresTuple to(gen.model.egzotics.E instance) {
		return toTuple(instance);
	}
	
	public static PostgresTuple toTuple(gen.model.egzotics.E instance) {
		if (instance == null) return null;
		
		switch (instance) { 
			case A:
				return TUPLE_A;
			case B:
				return TUPLE_B;
			case C:
				return TUPLE_C;
		}
		return DEFAULT_TUPLE;
	}

	public static String stringValue(gen.model.egzotics.E item) {
		
		switch (item) { 
			case A:
				return "A";
			case B:
				return "B";
			case C:
				return "C";
		}
		return "";
	}

	public static gen.model.egzotics.E convertEnum(PostgresReader reader) {
		
		switch (reader.bufferHash()) { 
			case -1005848884:
				return gen.model.egzotics.E.A;
			case -955516027:
				return gen.model.egzotics.E.B;
			case -972293646:
				return gen.model.egzotics.E.C;
		}
		return gen.model.egzotics.E.A;
	}
}
