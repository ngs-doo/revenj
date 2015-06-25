package org.revenj;

import org.revenj.patterns.ServiceLocator;
import org.revenj.postgres.ObjectConverter;
import org.revenj.postgres.PostgresReader;
import org.revenj.postgres.converters.IntConverter;
import org.revenj.postgres.converters.PostgresTuple;
import org.revenj.postgres.converters.RecordTuple;
import org.revenj.postgres.converters.ValueTuple;

import java.io.IOException;

public class SimpleConverter implements ObjectConverter<SimpleObject> {

	private final int columnCount;
	private final int[] customOrder;

	public SimpleConverter(ServiceLocator locator) {
		customOrder = null;
		columnCount = 2;
	}

	@Override
	public SimpleObject from(PostgresReader reader) throws IOException {
		return from(reader, 0);
	}

	public SimpleObject from(PostgresReader reader, int context) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		SimpleObject instance = from(reader, context, context == 0 ? 1 : context << 1);
		reader.read();
		return instance;
	}

	private SimpleObject from(PostgresReader reader, int outerContext, int context) throws IOException {
		reader.read(outerContext);
		SimpleObject instance = new SimpleObject(reader, context, customOrder);
		if (reader.last() != ')') throw new IOException("Expecting ')'. Found: " + (char)reader.last());
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(SimpleObject instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		if (customOrder == null) {
			items[0] = IntConverter.toTuple(instance.getNumber());
			items[1] = ValueTuple.from(instance.getText());
		} else {
			items[customOrder[0]] = IntConverter.toTuple(instance.getNumber());
			items[customOrder[1]] = ValueTuple.from(instance.getText());
		}
		return RecordTuple.from(items);
	}
}
