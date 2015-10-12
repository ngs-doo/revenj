using System;
using System.Collections.Generic;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public static class S3Converter
	{
		public class S3
		{
			public string Bucket;
			public string Key;
			public long Length;
			public string Name;
			public string MimeType;
			public Dictionary<string, string> Metadata;
		}

		public static S3 Parse(BufferedTextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var s3 = ParseS3(reader, context, context == 0 ? 1 : context << 1, null);
			reader.Read();
			return s3;
		}

		private static S3 ParseS3(BufferedTextReader reader, int context, int innerContext, IServiceProvider locator)
		{
			for (int i = 0; i < context; i++)
				reader.Read();
			var bucket = StringConverter.Parse(reader, innerContext);
			var key = StringConverter.Parse(reader, innerContext);
			var length = LongConverter.Parse(reader);
			var name = StringConverter.Parse(reader, innerContext);
			var mimeType = StringConverter.Parse(reader, innerContext);
			var metadata = HstoreConverter.Parse(reader, innerContext);
			for (int i = 0; i < context; i++)
				reader.Read();
			return new S3 { Bucket = bucket, Key = key, Length = length, Name = name, MimeType = mimeType, Metadata = metadata };
		}

		public static List<S3> ParseCollection(BufferedTextReader reader, int context)
		{
			return PostgresTypedArray.ParseCollection(reader, context, null, ParseS3);
		}
	}
}
