package org.revenj.database.postgres.converters;

import org.junit.Assert;
import org.junit.Test;
import org.revenj.storage.S3;
import org.revenj.database.postgres.PostgresReader;

import java.io.IOException;
import java.util.*;

public class TestPostgres {

	@Test
	public void simpleS3() throws IOException {
		PostgresReader reader = new PostgresReader();
		S3 s3 = new S3("bucket", "key", 42, "name", "mime-type", null);
		PostgresTuple tuple = S3Converter.toTuple(s3);
		String value = tuple.buildTuple(false);
		reader.process(value);
		S3 result = S3Converter.parse(reader, 0);
		Assert.assertEquals(s3, result);
	}

	@Test
	public void s3collection() throws IOException {
		PostgresReader reader = new PostgresReader();
		S3 s3a = new S3("bucket1", "key1", 142, "na me", "mime'type", Collections.singletonMap("a\\b", "c\"d"));
		S3 s3b = new S3("bucket2", "key2", 242, "na,me", "mime\\type", null);
		PostgresTuple tuple = ArrayTuple.create(Arrays.asList(s3a, s3b), S3Converter::toTuple);
		String value = tuple.buildTuple(false);
		reader.process(value);
		List<S3> result = S3Converter.parseCollection(reader, 0);
		Assert.assertEquals(2, result.size());
		Assert.assertEquals(s3a, result.get(0));
		Assert.assertEquals("c\"d", result.get(0).getMetadata().get("a\\b"));
		Assert.assertEquals(s3b, result.get(1));
		Assert.assertEquals(0, result.get(1).getMetadata().size());
		Assert.assertEquals("mime\\type", result.get(1).getMimeType());
	}
}
