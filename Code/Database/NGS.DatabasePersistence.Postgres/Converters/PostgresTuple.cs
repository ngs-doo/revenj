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
				var sw = cms.GetWriter();
				if (quote)
				{
					sw.Write('\'');
					InsertRecord(sw, string.Empty, EscapeQuote);
					sw.Write('\'');
				}
				else InsertRecord(sw, string.Empty, null);
				sw.Flush();
				cms.Position = 0;
				return cms.GetReader().ReadToEnd();
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
			switch (c)
			{
				case '\\':
					sw.Write("\\\\");
					break;
				case '\t':
					sw.Write("\\t");
					break;
				case '\n':
					sw.Write("\\n");
					break;
				case '\r':
					sw.Write("\\r");
					break;
				case '\v':
					sw.Write("\\v");
					break;
				case '\b':
					sw.Write("\\b");
					break;
				case '\f':
					sw.Write("\\f");
					break;
				default:
					sw.Write(c);
					break;
			}
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
