using System.Collections.Generic;
using System.IO;

namespace NGS.DatabasePersistence.Postgres.Converters
{
	public static class BoolConverter
	{
		public static bool? ParseNullable(TextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			reader.Read();
			return cur == 't';
		}

		public static bool Parse(TextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return false;
			reader.Read();
			return cur == 't';
		}

		public static List<bool?> ParseNullableCollection(TextReader reader, int context)
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
			var list = new List<bool?>();
			cur = reader.Peek();
			if (cur == '}')
				reader.Read();
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				if (cur == 't')
					list.Add(true);
				else if (cur == 'f')
					list.Add(false);
				else
				{
					reader.Read();
					reader.Read();
					reader.Read();
					list.Add(null);
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

		public static List<bool> ParseCollection(TextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var espaced = cur != '{';
			if (espaced)
			{
				for (int i = 0; i < context; i++)
					cur = reader.Read();
			}
			var list = new List<bool>();
			cur = reader.Peek();
			if (cur == '}')
				reader.Read();
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				if (cur == 't')
					list.Add(true);
				else if (cur == 'f')
					list.Add(false);
				else
				{
					reader.Read();
					reader.Read();
					reader.Read();
					list.Add(false);
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
