package org.revenj;

import org.junit.Assert;
import org.junit.Test;

import org.revenj.patterns.ServiceLocator;
import org.revenj.postgres.ObjectConverter;
import org.revenj.postgres.PostgresReader;
import org.revenj.postgres.converters.*;

import java.io.IOException;

public class TestConverter {
	@Test
	public void simpleConversion() throws IOException {
		String input = "(1,abc)";
		PostgresReader reader = new PostgresReader();
		reader.process(input);
		SimpleObject instance = SimpleConverter.INSTANCE.from(reader, null);
		Assert.assertEquals(1, instance.number);
		Assert.assertEquals("abc", instance.text);
	}

	static class SimpleObject {
		public int number;
		public String text;
	}

	enum SimpleConverter implements ObjectConverter<SimpleObject> {
		INSTANCE;

		@Override
		public SimpleObject from(PostgresReader reader, ServiceLocator locator) throws IOException {
			int cur = reader.read();
			if (cur == -1) return null;
			if (cur != '(') throw new IOException("Expecting '('");
			SimpleObject so = new SimpleObject();
			so.number = IntConverter.parse(reader);
			if (reader.last() != ',') throw new IOException("Expecting ','");
			so.text = StringConverter.parse(reader, 0);
			if (reader.last() != ')') throw new IOException("Expecting '('");
			return so;
		}

		@Override
		public PostgresTuple to(SimpleObject instance) {
			if (instance == null) return null;
			PostgresTuple number = IntConverter.toTuple(instance.number);
			PostgresTuple text = ValueTuple.from(instance.text);
			return RecordTuple.from(new PostgresTuple[]{ number, text});
		}
	}
}
