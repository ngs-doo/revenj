package org.revenj;

import org.revenj.patterns.AggregateRoot;
import org.revenj.postgres.ObjectConverter;
import org.revenj.postgres.PostgresReader;
import org.revenj.postgres.converters.UuidConverter;

import java.io.IOException;
import java.util.UUID;

public class CompositeObject implements AggregateRoot {

	public String getURI() {
		return id.toString();
	}

	private UUID id;
	private SimpleObject simple;

	public UUID getID() {
		return id;
	}

	public void setID(UUID value) {
		id = value;
	}

	public SimpleObject getSimple() {
		return simple;
	}

	public void setSimple(SimpleObject value) {
		simple = value;
	}

	public CompositeObject() {
	}

	public CompositeObject(PostgresReader reader, int context, ObjectConverter.Reader<CompositeObject>[] readers) throws IOException {
		for (ObjectConverter.Reader<CompositeObject> rdr : readers) {
			rdr.read(this, reader, context);
		}
	}

	public static void configureConverter(ObjectConverter.Reader<CompositeObject>[] readers, SimpleConverter simpleConverter) {
		readers[0] = (instance, rdr, ctx) -> {
			instance.id = UuidConverter.parse(rdr, false);
		};
		readers[1] = (instance, rdr, ctx) -> {
			instance.simple = simpleConverter.from(rdr, ctx);
		};
	}
}
