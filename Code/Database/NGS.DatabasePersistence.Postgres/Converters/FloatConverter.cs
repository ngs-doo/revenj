using System.Collections.Generic;
using System.Globalization;
using System.IO;

namespace NGS.DatabasePersistence.Postgres.Converters
{
	public static class FloatConverter
	{
		public static float? ParseNullable(TextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			return ParseFloat(reader, ref cur);
		}

		public static float Parse(TextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return 0;
			return ParseFloat(reader, ref  cur);
		}

		private static float ParseFloat(TextReader reader, ref int cur)
		{
			var buf = new char[16];
			var ind = 0;
			do
			{
				buf[ind++] = (char)cur;
				cur = reader.Read();
			} while (cur != -1 && ind < 16 && cur != ',' && cur != ')' && cur != '}');
			return float.Parse(new string(buf, 0, ind), NumberStyles.Float, CultureInfo.InvariantCulture);
		}

		public static List<float?> ParseNullableCollection(TextReader reader, int context)
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
			var list = new List<float?>();
			cur = reader.Peek();
			if (cur == '}')
				reader.Read();
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				if (cur == 'N')
				{
					cur = reader.Read();
					if (cur == 'U')
					{
						reader.Read();
						reader.Read();
						list.Add(null);
					}
					else
					{
						list.Add(float.NaN);
						reader.Read();
					}
					cur = reader.Read();
				}
				else
				{
					list.Add(ParseFloat(reader, ref cur));
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

		public static List<float> ParseCollection(TextReader reader, int context)
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
			var list = new List<float>();
			cur = reader.Peek();
			if (cur == '}')
				reader.Read();
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				if (cur == 'N')
				{
					cur = reader.Read();
					if (cur == 'U')
					{
						reader.Read();
						reader.Read();
						list.Add(0);
					}
					else
					{
						list.Add(float.NaN);
						reader.Read();
					}
					cur = reader.Read();
				}
				else
				{
					list.Add(ParseFloat(reader, ref cur));
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
	}
}
