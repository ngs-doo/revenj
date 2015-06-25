package org.revenj;

import org.revenj.postgres.ObjectConverter;
import org.revenj.postgres.PostgresReader;
import org.revenj.postgres.converters.IntConverter;
import org.revenj.postgres.converters.StringConverter;

import java.io.IOException;

public class SimpleObject {
	private int number;
	private String text;

	public int getNumber() { return number; }
	public void setNumber(int value) { number = value; }

	public String getText() { return text; }
	public void setText(String value) { text = value; }

	public SimpleObject() { }

	public SimpleObject(PostgresReader reader, int context, int[] readerOrder) throws IOException {
		if (readerOrder == null) {
			number = IntConverter.parse(reader);
			text = StringConverter.parse(reader, context);
		} else {
			for (int i : readerOrder) {
				__READERS[i].read(this, reader, context);
			}
		}
	}

	private static final ObjectConverter.Reader<SimpleObject> __NumberReader = (instance, reader, context) -> {
		instance.number = IntConverter.parse(reader);
	};
	private static final ObjectConverter.Reader<SimpleObject> __StringReader = (instance, reader, context) -> {
		instance.text = StringConverter.parse(reader, context);
	};

	private static final ObjectConverter.Reader<SimpleObject>[] __READERS = new ObjectConverter.Reader[] {
			__NumberReader,
			__StringReader
	};
}
