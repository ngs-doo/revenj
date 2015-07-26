using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public static class FloatConverter
	{
		public static float? ParseNullable(BufferedTextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			return ParseFloat(reader, ref cur, ')');
		}

		public static float Parse(BufferedTextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return 0;
			return ParseFloat(reader, ref  cur, ')');
		}

		private static float ParseFloat(BufferedTextReader reader, ref int cur, char matchEnd)
		{
			reader.InitBuffer((char)cur);
			reader.FillUntil(',', matchEnd);
			cur = reader.Read();
			//TODO: optimize
			return float.Parse(reader.BufferToString(), NumberStyles.Float, CultureInfo.InvariantCulture);
		}

		public static List<float?> ParseNullableCollection(BufferedTextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var espaced = cur != '{';
			if (espaced)
				reader.Read(context);
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
						cur = reader.Read(3);
						list.Add(null);
					}
					else
					{
						list.Add(float.NaN);
						cur = reader.Read(2);
					}
				}
				else
				{
					list.Add(ParseFloat(reader, ref cur, '}'));
				}
			}
			if (espaced)
				reader.Read(context + 1);
			else
				reader.Read();
			return list;
		}

		public static List<float> ParseCollection(BufferedTextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var espaced = cur != '{';
			if (espaced)
				reader.Read(context);
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
						cur = reader.Read(3);
						list.Add(0);
					}
					else
					{
						list.Add(float.NaN);
						cur = reader.Read(2);
					}
				}
				else
				{
					list.Add(ParseFloat(reader, ref cur, '}'));
				}
			}
			if (espaced)
				reader.Read(context + 1);
			else
				reader.Read();
			return list;
		}

		private static readonly CultureInfo Invairant = CultureInfo.InvariantCulture;

		public static int Serialize(float value, char[] buf, int pos)
		{
			var str = value.ToString(Invairant);
			str.CopyTo(0, buf, pos, str.Length);
			return pos + str.Length;
		}

		public static IPostgresTuple ToTuple(float value)
		{
			return new FloatTuple(value);
		}

		class FloatTuple : IPostgresTuple
		{
			private readonly float Value;

			public FloatTuple(float value)
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
				return Value.ToString(Invairant);
			}
		}
	}
}
