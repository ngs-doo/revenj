using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using NGS.DatabasePersistence.Postgres.Converters;
using NGS.DomainPatterns;

namespace NGS.DatabasePersistence.Postgres
{
	public static class PostgresTypedArray
	{
		//TODO remove use of StringBuilder
		public static string ToArray<T>(IEnumerable<T> data, Func<T, string> converter)
		{
			if (data == null)
				return "NULL";
			var sb = new StringBuilder("'");
			var value = PostgresRecordConverter.CreateArray(data, it => converter(it));
			sb.Append(value.Replace("'", "''"));
			sb.Append("'");
			return sb.ToString();
		}

		public static void ToArray<T>(StreamWriter sw, IEnumerable<T> data, Func<T, RecordTuple> converter)
		{
			if (data == null)
			{
				sw.Write("NULL");
				return;
			}
			var list = new List<RecordTuple>();
			foreach (var item in data)
				list.Add(converter(item));
			sw.Write('\'');
			var arr = new ArrayTuple(list.ToArray());
			arr.InsertRecord(sw, string.Empty, PostgresTuple.EscapeQuote);
			sw.Write('\'');
		}

		public static void ToArray<T>(StreamWriter sw, T[] data, Func<T, RecordTuple> converter)
		{
			if (data == null)
			{
				sw.Write("NULL");
				return;
			}
			var arr = new RecordTuple[data.Length];
			for (int i = 0; i < data.Length; i++)
				arr[i] = converter(data[i]);
			sw.Write('\'');
			var tuple = new ArrayTuple(arr);
			tuple.InsertRecord(sw, string.Empty, PostgresTuple.EscapeQuote);
			sw.Write('\'');
		}

		public static List<T> ParseCollection<T>(TextReader reader, int context, IServiceLocator locator, Func<TextReader, int, int, IServiceLocator, T> parseItem)
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
			var list = new List<T>();
			cur = reader.Peek();
			if (cur == '}')
				reader.Read();
			var arrayContext = Math.Max(context << 1, 1);
			var recordContext = arrayContext << 1;
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				if (cur == 'N')
				{
					reader.Read();
					reader.Read();
					reader.Read();
					list.Add(default(T));
				}
				else
				{
					var escaped = cur != '(';
					if (escaped)
					{
						for (int i = 0; i < arrayContext; i++)
							reader.Read();
					}
					list.Add(parseItem(reader, 0, recordContext, locator));
					if (escaped)
					{
						for (int i = 0; i < arrayContext; i++)
							reader.Read();
					}
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
