using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using Revenj.DatabasePersistence.Postgres.Converters;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres
{
	public static class PostgresTypedArray
	{
		public static string ToArray<T>(IEnumerable<T> data, Func<T, string> converter)
		{
			if (data == null)
				return "NULL";
			using (var cms = ChunkedMemoryStream.Create())
			{
				Func<T, IPostgresTuple> toTuple = v => new ValueTuple(converter(v), false, true);
				var writer = cms.GetWriter();
				ToArray(writer, cms.SmallBuffer, data, toTuple);
				writer.Flush();
				cms.Position = 0;
				return cms.GetReader().ReadToEnd();
			}
		}

		public static void ToArray<T>(TextWriter sw, char[] buf, IEnumerable<T> data, Func<T, IPostgresTuple> converter)
		{
			if (data == null)
			{
				sw.Write("NULL");
				return;
			}
			var count = data.Count();
			var tuples = new IPostgresTuple[count];
			int i = 0;
			foreach (var item in data)
				tuples[i++] = converter(item);
			sw.Write('\'');
			var arr = ArrayTuple.From(tuples);
			arr.InsertRecord(sw, buf, string.Empty, PostgresTuple.EscapeQuote);
			sw.Write('\'');
		}

		public static void ToArray<T>(TextWriter sw, char[] buf, T[] data, Func<T, IPostgresTuple> converter)
		{
			if (data == null)
			{
				sw.Write("NULL");
				return;
			}
			var arr = new IPostgresTuple[data.Length];
			for (int i = 0; i < data.Length; i++)
				arr[i] = converter(data[i]);
			sw.Write('\'');
			var tuple = ArrayTuple.From(arr);
			tuple.InsertRecord(sw, buf, string.Empty, PostgresTuple.EscapeQuote);
			sw.Write('\'');
		}

		public static List<T> ParseCollection<T>(BufferedTextReader reader, int context, IServiceProvider locator, Func<BufferedTextReader, int, int, IServiceProvider, T> parseItem)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var escaped = cur != '{';
			if (escaped)
				reader.Read(context);
			cur = reader.Peek();
			if (cur == '}')
			{
				if (escaped)
					reader.Read(context + 2);
				else
					reader.Read(2);
				return new List<T>(0);
			}
			var list = new List<T>();
			var arrayContext = Math.Max(context << 1, 1);
			var recordContext = arrayContext << 1;
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				if (cur == 'N')
				{
					cur = reader.Read(4);
					list.Add(default(T));
				}
				else
				{
					var innerEscaped = cur != '(';
					if (innerEscaped)
						reader.Read(arrayContext);
					list.Add(parseItem(reader, 0, recordContext, locator));
					if (innerEscaped)
						cur = reader.Read(arrayContext + 1);
					else
						cur = reader.Read();
				}
			}
			if (escaped)
				reader.Read(context + 1);
			else
				reader.Read();
			return list;
		}
	}
}
