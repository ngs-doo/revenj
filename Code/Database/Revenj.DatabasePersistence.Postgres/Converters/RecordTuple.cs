using System;
using System.IO;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public class RecordTuple : IPostgresTuple
	{
		public static readonly IPostgresTuple Empty;
		public static readonly IPostgresTuple Null;

		static RecordTuple()
		{
			Empty = new EmptyRecordTuple();
			Null = new NullTuple();
		}

		private readonly IPostgresTuple[] Properties;

		internal RecordTuple(IPostgresTuple[] properties)
		{
			this.Properties = properties;
		}

		public static IPostgresTuple From(IPostgresTuple[] properties)
		{
			if (properties == null)
				return Null;
			if (properties.Length == 0)
				return Empty;
			return new RecordTuple(properties);
		}

		class EmptyRecordTuple : IPostgresTuple
		{
			public bool MustEscapeRecord { get { return true; } }
			public bool MustEscapeArray { get { return false; } }
			public void InsertRecord(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				sw.Write("()");
			}
			public void InsertArray(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				sw.Write("()");
			}
			public string BuildTuple(bool quote) { return quote ? "'()'" : "()"; }
		}

		class NullTuple : IPostgresTuple
		{
			public bool MustEscapeRecord { get { return false; } }
			public bool MustEscapeArray { get { return false; } }
			public void InsertRecord(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings) { }
			public void InsertArray(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				sw.Write("NULL");
			}
			public string BuildTuple(bool quote) { return "NULL"; }
		}

		public bool MustEscapeRecord { get { return true; } }
		public bool MustEscapeArray { get { return true; } }

		public string BuildTuple(bool quote)
		{
			using (var cms = ChunkedMemoryStream.Create())
			{
				var sw = cms.GetWriter();
				Action<TextWriter, char> mappings = null;
				if (quote)
				{
					mappings = PostgresTuple.EscapeQuote;
					sw.Write('\'');
				}
				sw.Write('(');
				var p = Properties[0];
				if (p != null)
				{
					if (p.MustEscapeRecord)
					{
						sw.Write('"');
						p.InsertRecord(sw, cms.SmallBuffer, "1", mappings);
						sw.Write('"');
					}
					else p.InsertRecord(sw, cms.SmallBuffer, string.Empty, mappings);
				}
				for (int i = 1; i < Properties.Length; i++)
				{
					sw.Write(',');
					p = Properties[i];
					if (p != null)
					{
						if (p.MustEscapeRecord)
						{
							sw.Write('"');
							p.InsertRecord(sw, cms.SmallBuffer, "1", mappings);
							sw.Write('"');
						}
						else p.InsertRecord(sw, cms.SmallBuffer, string.Empty, mappings);
					}
				}
				sw.Write(')');
				if (quote)
					sw.Write('\'');
				sw.Flush();
				cms.Position = 0;
				return cms.GetReader().ReadToEnd();
			}
		}

		private static readonly byte[] NULL = new byte[] { (byte)'N', (byte)'U', (byte)'L', (byte)'L' };

		public void InsertRecord(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
		{
			sw.Write('(');
			var newEscaping = escaping + '1';
			string quote = null;
			var p = Properties[0];
			if (p != null)
			{
				if (p.MustEscapeRecord)
				{
					quote = PostgresTuple.BuildQuoteEscape(escaping);
					if (mappings != null)
						foreach (var q in quote)
							mappings(sw, q);
					else
						sw.Write(quote);
					p.InsertRecord(sw, buf, newEscaping, mappings);
					if (mappings != null)
						foreach (var q in quote)
							mappings(sw, q);
					else
						sw.Write(quote);
				}
				else p.InsertRecord(sw, buf, escaping, mappings);
			}
			for (int i = 1; i < Properties.Length; i++)
			{
				sw.Write(',');
				p = Properties[i];
				if (p != null)
				{
					if (p.MustEscapeRecord)
					{
						//TODO: build quote only once and reuse it, instead of looping all the time
						quote = quote ?? PostgresTuple.BuildQuoteEscape(escaping);
						if (mappings != null)
							foreach (var q in quote)
								mappings(sw, q);
						else
							sw.Write(quote);
						p.InsertRecord(sw, buf, newEscaping, mappings);
						if (mappings != null)
							foreach (var q in quote)
								mappings(sw, q);
						else
							sw.Write(quote);
					}
					else p.InsertRecord(sw, buf, escaping, mappings);
				}
			}
			sw.Write(')');
		}

		public void InsertArray(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
		{
			InsertRecord(sw, buf, escaping, mappings);
		}
	}
}
