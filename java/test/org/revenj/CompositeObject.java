package org.revenj;

import org.revenj.postgres.PostgresReader;
import org.revenj.postgres.converters.UuidConverter;

import java.io.IOException;
import java.util.UUID;

public class CompositeObject {
	private UUID id;
	private SimpleObject simple;

	public UUID getID() { return id; }
	public void setID(UUID value) { id = value; }

	public SimpleObject getSimple() { return simple; }
	public void setSimple(SimpleObject value) { simple = value; }

	public CompositeObject() { }

	public CompositeObject(PostgresReader reader, int context, CompositeConverter converter) throws IOException {
		converter.fill(
				this,
				reader,
				context,
				CompositeObject::__parseIdPostgres,
				(instance, rdr, ctx) -> { instance.simple = converter.simpleConverter.from(rdr, ctx); });
	}

	private static void __parseIdPostgres(CompositeObject instance, PostgresReader reader, int context) throws IOException {
		instance.id = UuidConverter.parse(reader, false);
	}

}
