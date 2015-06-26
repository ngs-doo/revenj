package org.revenj;

import org.revenj.patterns.AggregateRoot;
import org.revenj.postgres.ObjectConverter;
import org.revenj.postgres.PostgresReader;
import org.revenj.postgres.converters.IntConverter;
import org.revenj.postgres.converters.StringConverter;

import java.io.IOException;

public class SimpleObject implements AggregateRoot {
	private int number;
	private String text;

	public String getURI() {
		return Integer.toString(number);
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int value) {
		number = value;
	}

	public String getText() {
		return text;
	}

	public void setText(String value) {
		text = value;
	}

	public SimpleObject() {
	}

	public SimpleObject(PostgresReader reader, int context, ObjectConverter.Reader<SimpleObject>[] readers) throws IOException {
		for (ObjectConverter.Reader<SimpleObject> rdr : readers) {
			rdr.read(this, reader, context);
		}
	}

	public static void configureConverter(ObjectConverter.Reader<SimpleObject>[] readers) {
		readers[0] = (instance, reader, context) -> {
			instance.number = IntConverter.parse(reader);
		};
		readers[1] = (instance, reader, context) -> {
			instance.text = StringConverter.parse(reader, context);
		};
	}
}
