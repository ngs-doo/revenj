using System;
using System.Collections.Generic;
using System.IO;
using System.Xml.Linq;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public static class XmlConverter
	{
		public static XElement Parse(BufferedTextReader reader, int context)
		{
			var stream = StringConverter.ParseStream(reader, context);
			if (stream == null)
				return null;
			try { return XElement.Load(stream); }
			finally { stream.Dispose(); }
		}

		public static List<XElement> ParseCollection(BufferedTextReader reader, int context)
		{
			var list = StringConverter.ParseStreamCollection(reader, context, true);
			if (list == null)
				return null;
			var result = new List<XElement>(list.Count);
			foreach (var stream in list)
			{
				if (stream != null)
				{
					result.Add(XElement.Load(stream));
					stream.Dispose();
				}
				else result.Add(null);
			}
			return result;
		}

		public static IPostgresTuple ToTuple(XElement value)
		{
			return value != null ? new XmlTuple(value) : default(IPostgresTuple);
		}

		class XmlTuple : IPostgresTuple
		{
			private readonly TextReader Reader;

			public XmlTuple(XElement xml)
			{
				var cms = ChunkedMemoryStream.Create();
				xml.Save(cms);
				cms.Position = 0;
				Reader = cms.GetReader();
			}

			public bool MustEscapeRecord { get { return true; } }
			public bool MustEscapeArray { get { return true; } }

			public string BuildTuple(bool quote) { return PostgresTuple.BuildTuple(this, quote); }

			public void InsertRecord(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				string quoteEscape = null;
				string slashEscape = null;
				int c;
				if (mappings != null)
				{
					while ((c = Reader.Read()) != -1)
					{
						if (c == '"')
						{
							quoteEscape = quoteEscape ?? PostgresTuple.BuildQuoteEscape(escaping);
							foreach (var q in quoteEscape)
								mappings(sw, q);
						}
						else if (c == '\\')
						{
							slashEscape = slashEscape ?? PostgresTuple.BuildSlashEscape(escaping.Length);
							foreach (var q in slashEscape)
								mappings(sw, q);
						}
						else mappings(sw, (char)c);
					}
				}
				else
				{
					while ((c = Reader.Read()) != -1)
					{
						if (c == '"')
						{
							quoteEscape = quoteEscape ?? PostgresTuple.BuildQuoteEscape(escaping);
							sw.Write(quoteEscape);
						}
						else if (c == '\\')
						{
							slashEscape = slashEscape ?? PostgresTuple.BuildSlashEscape(escaping.Length);
							sw.Write(slashEscape);
						}
						else sw.Write((char)c);
					}
				}
			}

			public void InsertArray(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				InsertRecord(sw, buf, escaping, mappings);
			}
		}
	}
}
