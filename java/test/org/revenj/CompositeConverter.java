package org.revenj;

import org.revenj.patterns.ServiceLocator;
import org.revenj.postgres.ObjectConverter;
import org.revenj.postgres.PostgresReader;
import org.revenj.postgres.converters.*;

import java.io.IOException;

public class CompositeConverter implements ObjectConverter<CompositeObject> {

	private final int columnCount;
	private final int[] customOrder;
	public final SimpleConverter simpleConverter;

	public CompositeConverter(ServiceLocator locator) {
		customOrder = null;
		columnCount = 2;
		simpleConverter = locator.lookup(SimpleConverter.class).get();
	}

	@Override
	public CompositeObject from(PostgresReader reader) throws IOException {
		return from(reader, 0);
	}

	public CompositeObject from(PostgresReader reader, int context) throws IOException {
		int cur = reader.read();
		if (cur == ',' || cur == ')') return null;
		CompositeObject instance = from(reader, context, context == 0 ? 1 : context << 1);
		reader.read();
		return instance;
	}

	private CompositeObject from(PostgresReader reader, int outerContext, int context) throws IOException {
		reader.read(outerContext);
		CompositeObject instance = new CompositeObject(reader, context, this);
		if (reader.last() != ')') throw new IOException("Expecting ')'. Found: " + (char)reader.last());
		reader.read(outerContext);
		return instance;
	}

	@Override
	public PostgresTuple to(CompositeObject instance) {
		if (instance == null) return null;
		PostgresTuple[] items = new PostgresTuple[columnCount];
		if (customOrder == null) {
			items[0] = UuidConverter.toTuple(instance.getID());
			items[1] = simpleConverter.to(instance.getSimple());
		} else {
			items[customOrder[0]] = UuidConverter.toTuple(instance.getID());
			items[customOrder[1]] = simpleConverter.to(instance.getSimple());
		}
		return RecordTuple.from(items);
	}

	public void fill(
			CompositeObject instance,
			PostgresReader reader,
			int context,
			Reader<CompositeObject>... readers) throws IOException {
		if (customOrder == null) {
			for (Reader<CompositeObject> rdr : readers) {
				rdr.read(instance, reader, context);
			}
		} else {
			for (int i : customOrder) {
				readers[i].read(instance, reader, context);
			}
		}
	}
}
