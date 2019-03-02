using System.Collections.Generic;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public static class ShortConverter
	{
		public static short? ParseNullable(BufferedTextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			return ParseShort(reader, ref cur, ')');
		}

		public static short Parse(BufferedTextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return 0;
			return ParseShort(reader, ref cur, ')');
		}

		private static short ParseShort(BufferedTextReader reader, ref int cur, char endChar)
		{
			return (short)IntConverter.ParseInt(reader, ref cur, endChar);
		}

		public static List<short?> ParseNullableCollection(BufferedTextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var espaced = cur != '{';
			if (espaced)
				reader.Read(context);
			var list = new List<short?>();
			cur = reader.Peek();
			if (cur == '}')
				reader.Read();
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				if (cur == 'N')
				{
					cur = reader.Read(4);
					list.Add(null);
				}
				else
				{
					list.Add(ParseShort(reader, ref cur, '}'));
				}
			}
			if (espaced)
				reader.Read(context + 1);
			else
				reader.Read();
			return list;
		}

		public static List<short> ParseCollection(BufferedTextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var espaced = cur != '{';
			if (espaced)
				reader.Read(context);
			var list = new List<short>();
			cur = reader.Peek();
			if (cur == '}')
				reader.Read();
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				if (cur == 'N')
				{
					cur = reader.Read(4);
					list.Add(0);
				}
				else
				{
					list.Add(ParseShort(reader, ref cur, '}'));
				}
			}
			if (espaced)
				reader.Read(context + 1);
			else
				reader.Read();
			return list;
		}

		public static int Serialize(short value, char[] buf, int start)
		{
			return IntConverter.Serialize(value, buf, start);
		}

		public static IPostgresTuple ToTuple(short value)
		{
			return IntConverter.ToTuple(value);
		}
	}
}
