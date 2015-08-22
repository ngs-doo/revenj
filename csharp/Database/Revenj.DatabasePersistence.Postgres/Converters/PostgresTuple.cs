using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public interface IPostgresTuple
	{
		bool MustEscapeRecord { get; }
		bool MustEscapeArray { get; }
		//TODO: change API to improve performance
		void InsertRecord(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings);
		void InsertArray(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings);
		string BuildTuple(bool quote);
	}

	public static class PostgresTuple
	{
		public static string BuildTuple(this IPostgresTuple tuple, bool quote)
		{
			using (var cms = ChunkedMemoryStream.Create())
			{
				var sw = cms.GetWriter();
				if (quote)
				{
					sw.Write('\'');
					tuple.InsertRecord(sw, cms.SmallBuffer, string.Empty, EscapeQuote);
					sw.Write('\'');
				}
				else tuple.InsertRecord(sw, cms.SmallBuffer, string.Empty, null);
				sw.Flush();
				cms.Position = 0;
				return cms.GetReader().ReadToEnd();
			}
		}

		public static void EscapeQuote(TextWriter sw, char c)
		{
			if (c == '\'')
				sw.Write('\'');
			sw.Write(c);
		}

		public static void EscapeBulkCopy(TextWriter sw, char c)
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

		private static Dictionary<string, string> QuoteEscape = new Dictionary<string, string>();

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
			var newQuotes = new Dictionary<string, string>(QuoteEscape);
			newQuotes[escaping] = result;
			QuoteEscape = newQuotes;
			return result;
		}

		private static readonly string[] Slashes = InitSlashes();

		private static string[] InitSlashes()
		{
			var arr = new string[20];
			for (int i = 0; i < arr.Length; i++)
				arr[i] = new string('\\', 1 << i);
			return arr;
		}

		public static string BuildSlashEscape(int len)
		{
			if (len < Slashes.Length)
				return Slashes[len];
			return new string('\\', 1 << len);
		}
	}
}
