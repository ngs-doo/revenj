using System;
using System.Collections.Generic;
using System.IO;

namespace NGS.DatabasePersistence.Postgres.Converters
{
	public static class GuidConverter
	{
		public static Guid? ParseNullable(TextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			return ParseGuid(reader, cur);
		}

		public static Guid Parse(TextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return Guid.Empty;
			return ParseGuid(reader, cur);
		}

		private static Guid ParseGuid(TextReader reader, int cur)
		{
			var buf = new char[37];
			buf[0] = (char)cur;
			int read = reader.Read(buf, 1, 36);
			if (read != 36) for (int i = read + 1; i < 37; i++) buf[i] = (char)reader.Read();
			//TODO char[] to byte[] conversion
			return new Guid(new string(buf, 0, 36));
		}

		private static Guid ParseCollectionGuid(TextReader reader, int cur)
		{
			var buf = new char[36];
			buf[0] = (char)cur;
			int read = reader.Read(buf, 1, 35);
			if (read != 35) for (int i = read + 1; i < 36; i++) buf[i] = (char)reader.Read();
			return new Guid(new string(buf, 0, 36));
		}

		public static List<Guid?> ParseNullableCollection(TextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var espaced = cur != '{';
			if (espaced)
			{
				for (int i = 0; i < context; i++)
					reader.Read();
			}
			var list = new List<Guid?>();
			cur = reader.Peek();
			if (cur == '}')
				reader.Read();
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				if (cur == 'N')
				{
					reader.Read();
					reader.Read();
					reader.Read();
					list.Add(null);
				}
				else
				{
					list.Add(ParseCollectionGuid(reader, cur));
				}
				cur = reader.Read();
			}
			if (espaced)
			{
				for (int i = 0; i < context; i++)
					reader.Read();
			}
			reader.Read();
			return list;
		}

		public static List<Guid> ParseCollection(TextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var espaced = cur != '{';
			if (espaced)
			{
				for (int i = 0; i < context; i++)
					reader.Read();
			}
			var list = new List<Guid>();
			cur = reader.Peek();
			if (cur == '}')
				reader.Read();
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				if (cur == 'N')
				{
					reader.Read();
					reader.Read();
					reader.Read();
					list.Add(Guid.Empty);
				}
				else
				{
					list.Add(ParseCollectionGuid(reader, cur));
				}
				cur = reader.Read();
			}
			if (espaced)
			{
				for (int i = 0; i < context; i++)
					reader.Read();
			}
			reader.Read();
			return list;
		}
	}
}
