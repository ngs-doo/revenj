using System.Collections.Generic;
using System.IO;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public static class IntConverter
	{
		public static int? ParseNullable(TextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			return ParseInt(reader, ref cur);
		}

		public static int Parse(TextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return 0;
			return ParseInt(reader, ref cur);
		}

		private static int ParseInt(TextReader reader, ref int cur)
		{
			var neg = cur == '-';
			if (neg)
				cur = reader.Read();
			int res = 0;
			do
			{
				res = res * 10 + (cur - 48);
				cur = reader.Read();
			} while (cur != -1 && cur != ',' && cur != ')' && cur != '}');
			return neg ? -res : res;
		}

		public static List<int?> ParseNullableCollection(TextReader reader, int context)
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
			var list = new List<int?>();
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
					cur = reader.Read();
				}
				else
				{
					list.Add(ParseInt(reader, ref cur));
				}
			}
			if (espaced)
			{
				for (int i = 0; i < context; i++)
					reader.Read();
			}
			reader.Read();
			return list;
		}

		public static List<int> ParseCollection(TextReader reader, int context)
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
			var list = new List<int>();
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
					list.Add(0);
					cur = reader.Read();
				}
				else
				{
					list.Add(ParseInt(reader, ref cur));
				}
			}
			if (espaced)
			{
				for (int i = 0; i < context; i++)
					reader.Read();
			}
			reader.Read();
			return list;
		}

		public static int ParsePositive(char[] source, int start, int end)
		{
			int res = 0;
			for (int i = start; i < end; i++)
				res = res * 10 + (source[i] - 48);
			return res;
		}
	}
}
