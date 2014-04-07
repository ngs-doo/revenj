using System;
using System.Collections.Generic;
using System.IO;
using System.Xml.Linq;
using NGS.Utility;

namespace NGS.DatabasePersistence.Postgres.Converters
{
	public static class XmlConverter
	{
		public static XElement Parse(TextReader reader, int context)
		{
			var stream = StringConverter.ParseStream(reader, context);
			if (stream == null)
				return null;
			try { return XElement.Load(stream); }
			finally { stream.Dispose(); }
		}

		public static List<XElement> ParseCollection(TextReader reader, int context)
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

		public static PostgresTuple ToTuple(XElement value)
		{
			return value != null ? new XmlTuple(value) : null;
		}

		class XmlTuple : PostgresTuple
		{
			private readonly StreamReader Reader;

			public XmlTuple(XElement xml)
			{
				var cms = ChunkedMemoryStream.Create();
				xml.Save(cms);
				cms.Position = 0;
				Reader = cms.GetReader();
			}

			public override bool MustEscapeRecord { get { return true; } }
			public override bool MustEscapeArray { get { return true; } }

			public override void InsertRecord(StreamWriter sw, string escaping, Action<StreamWriter, char> mappings)
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
							quoteEscape = quoteEscape ?? BuildQuoteEscape(escaping);
							foreach (var q in quoteEscape)
								mappings(sw, q);
						}
						else if (c == '\\')
						{
							slashEscape = slashEscape ?? BuildSlashEscape(escaping.Length);
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
							quoteEscape = quoteEscape ?? BuildQuoteEscape(escaping);
							sw.Write(quoteEscape);
						}
						else if (c == '\\')
						{
							slashEscape = slashEscape ?? BuildSlashEscape(escaping.Length);
							sw.Write(slashEscape);
						}
						sw.Write((char)c);
					}
				}
			}

			public override void InsertArray(StreamWriter sw, string escaping, Action<StreamWriter, char> mappings)
			{
				InsertRecord(sw, escaping, mappings);
			}
		}
	}
}
