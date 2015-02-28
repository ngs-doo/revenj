using System.Collections.Generic;
using System.Globalization;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public static class DoubleConverter
	{
		public static double? ParseNullable(BufferedTextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			return ParseDouble(reader, ref cur, ')');
		}

		public static double Parse(BufferedTextReader reader)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return 0;
			return ParseDouble(reader, ref cur, ')');
		}

		private static double ParseDouble(BufferedTextReader reader, ref int cur, char matchEnd)
		{
			reader.InitBuffer((char)cur);
			reader.FillUntil(',', matchEnd);
			cur = reader.Read();
			//TODO: optimize
			return double.Parse(reader.BufferToString(), NumberStyles.Float, CultureInfo.InvariantCulture);
		}

		public static List<double?> ParseNullableCollection(BufferedTextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var espaced = cur != '{';
			if (espaced)
				reader.Read(context);
			var list = new List<double?>();
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
						list.Add(double.NaN);
						cur = reader.Read(2);
					}
				}
				else
				{
					list.Add(ParseDouble(reader, ref cur, '}'));
				}
			}
			if (espaced)
				reader.Read(context + 1);
			else
				reader.Read();
			return list;
		}

		public static List<double> ParseCollection(BufferedTextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var espaced = cur != '{';
			if (espaced)
				reader.Read(context);
			var list = new List<double>();
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
						list.Add(double.NaN);
						cur = reader.Read(2);
					}
				}
				else
				{
					list.Add(ParseDouble(reader, ref cur, '}'));
				}
			}
			if (espaced)
				reader.Read(context + 1);
			else
				reader.Read();
			return list;
		}
	}
}
