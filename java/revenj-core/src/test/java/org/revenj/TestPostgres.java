package org.revenj;

import org.junit.Assert;
import org.junit.Test;
import org.revenj.postgres.PostgresReader;
import org.revenj.postgres.PostgresWriter;
import org.revenj.postgres.converters.ArrayTuple;
import org.revenj.postgres.converters.FloatConverter;
import org.revenj.postgres.converters.PostgresTuple;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TestPostgres {

	@Test
	public void floatIssue() throws IOException {
		PostgresWriter writer = new PostgresWriter();
		PostgresReader reader = new PostgresReader();
		List<Float> floats = Arrays.asList(0f, -0.000012345f, -0.00001f, Float.NaN, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
		PostgresTuple tuple = ArrayTuple.create(floats, FloatConverter::toTuple);
		String value = tuple.buildTuple(false);
		reader.process(value);
		List<Float> result = FloatConverter.parseCollection(reader, 0, false);
		Assert.assertEquals(floats, result);
	}
}
