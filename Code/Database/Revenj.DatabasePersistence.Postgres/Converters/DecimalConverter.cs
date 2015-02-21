using System;
using System.Collections.Generic;
using System.IO;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public static class DecimalConverter
	{
		public static decimal? ParseNullable(TextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			return ParseDecimal(reader, ref cur);
		}

		public static decimal Parse(TextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return 0;
			return ParseDecimal(reader, ref cur);
		}

		private static decimal ParseDecimal(TextReader reader, ref int cur)
		{
			var neg = cur == '-';
			if (neg)
				cur = reader.Read();
			decimal res = 0;
			do
			{
				res = res * 10 + (cur - 48);
				cur = reader.Read();
			} while (cur != -1 && cur != ',' && cur != '.' && cur != ')' && cur != '}');
			if (cur == '.')
			{
				cur = reader.Read();
				decimal pow = 0.1m;
				do
				{
					res += pow * (cur - 48);
					cur = reader.Read();
					pow = pow / 10;
				} while (cur != -1 && cur != ',' && cur != ')' && cur != '}');
			}
			return neg ? -res : res;
		}


		public static List<decimal?> ParseNullableCollection(TextReader reader, int context)
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
			var list = new List<decimal?>();
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
					list.Add(ParseDecimal(reader, ref cur));
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

		public static List<decimal> ParseCollection(TextReader reader, int context)
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
			var list = new List<decimal>();
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
					list.Add(ParseDecimal(reader, ref cur));
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

		public static IPostgresTuple ToTuple(decimal value)
		{
			return new DecimalTuple(value);
		}

		class DecimalTuple : IPostgresTuple
		{
			private readonly decimal Value;

			public DecimalTuple(decimal value)
			{
				this.Value = value;
			}

			public bool MustEscapeRecord { get { return false; } }
			public bool MustEscapeArray { get { return false; } }

			public void InsertRecord(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				sw.Write(Value);
			}

			public void InsertArray(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				sw.Write(Value);
			}

			public string BuildTuple(bool quote)
			{
				return Value.ToString();
			}
		}
	}
}
