using System;
using System.Collections.Concurrent;
using System.IO;
using System.Text;
using NGS.Utility;

namespace NGS.DatabasePersistence.Postgres.Converters
{
	public abstract class PostgresTuple
	{
		public abstract bool MustEscapeRecord { get; }
		public abstract bool MustEscapeArray { get; }
		public abstract void InsertRecord(StreamWriter sw, string escaping, Action<StreamWriter, char> mappings);
		public abstract void InsertArray(StreamWriter sw, string escaping, Action<StreamWriter, char> mappings);

		public virtual string BuildTuple(bool quote)
		{
			using (var cms = ChunkedMemoryStream.Create())
			{
				var sw = new StreamWriter(cms);
				if (quote)
				{
					sw.Write('\'');
					InsertRecord(sw, string.Empty, EscapeQuote);
					sw.Write('\'');
				}
				else InsertRecord(sw, string.Empty, null);
				sw.Flush();
				cms.Position = 0;
				using (var sr = new StreamReader(cms))
					return sr.ReadToEnd();
			}
		}

		public static void EscapeQuote(StreamWriter sw, char c)
		{
			if (c == '\'')
				sw.Write('\'');
			sw.Write(c);
		}

		public static void EscapeBulkCopy(StreamWriter sw, char c)
		{
			if (c == '\\')
				sw.Write("\\\\");
			else if (c == '\t')
				sw.Write("\\t");
			else if (c == '\n')
				sw.Write("\\n");
			else if (c == '\r')
				sw.Write("\\r");
			else if (c == '\v')
				sw.Write("\\v");
			else if (c == '\b')
				sw.Write("\\b");
			else if (c == '\f')
				sw.Write("\\f");
			else
				sw.Write(c);
		}

		private static ConcurrentDictionary<string, string> QuoteEscape = new ConcurrentDictionary<string, string>(1, 17);

		public static string BuildQuoteEscape(string escaping)
		{
			string result;
			if (QuoteEscape.TryGetValue(escaping, out result))
				return result;
			var sb = new StringBuilder();
			sb.Append('"');
			for (int j = escaping.Length - 1; j >= 0; j--)
			{
				if (escaping[j] == '1')
				{
					var len = sb.Length;
					for (int i = 0; i < len; i++)
						sb.Insert(i * 2, sb[i * 2]);
				}
				else
					sb.Replace("\\", "\\\\").Replace("\"", "\\\"");
			}
			result = sb.ToString();
			QuoteEscape.TryAdd(escaping, result);
			return result;
		}

		public static string BuildSlashEscape(int len)
		{
			return new string('\\', 1 << len);
		}
	}
}
