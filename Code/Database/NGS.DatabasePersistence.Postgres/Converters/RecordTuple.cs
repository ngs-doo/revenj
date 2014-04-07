using System;
using System.Collections.Generic;
using System.IO;
using NGS.Utility;

namespace NGS.DatabasePersistence.Postgres.Converters
{
	public class RecordTuple : PostgresTuple
	{
		private readonly PostgresTuple[] Properties;

		public RecordTuple(PostgresTuple[] properties)
		{
			this.Properties = properties;
		}

		public override bool MustEscapeRecord { get { return Properties != null; } }
		public override bool MustEscapeArray { get { return Properties != null; } }

		public RecordTuple Except(IEnumerable<int> indexes)
		{
			if (indexes != null)
				foreach (var item in indexes)
					Properties[item] = null;
			return this;
		}

		public override string BuildTuple(bool quote)
		{
			if (Properties == null)
				return "NULL";
			using (var cms = ChunkedMemoryStream.Create())
			{
				var sw = cms.GetWriter();
				Action<StreamWriter, char> mappings = null;
				if (quote)
				{
					mappings = EscapeQuote;
					sw.Write('\'');
				}
				sw.Write('(');
				for (int i = 0; i < Properties.Length; i++)
				{
					var p = Properties[i];
					if (p != null)
					{
						if (p.MustEscapeRecord)
						{
							sw.Write('"');
							p.InsertRecord(sw, "1", mappings);
							sw.Write('"');
						}
						else p.InsertRecord(sw, string.Empty, mappings);
					}
					if (i < Properties.Length - 1)
						sw.Write(',');
				}
				sw.Write(')');
				if (quote)
					sw.Write('\'');
				sw.Flush();
				cms.Position = 0;
				return cms.GetReader().ReadToEnd();
			}
		}

		public Stream Build()
		{
			return Build(false, null);
		}

		public Stream Build(bool bulk, Action<StreamWriter, char> mappings)
		{
			if (Properties == null)
				return new MemoryStream(new byte[] { (byte)'N', (byte)'U', (byte)'L', (byte)'L' });
			var cms = ChunkedMemoryStream.Create();
			var sw = cms.GetWriter();
			if (bulk)
			{
				for (int i = 0; i < Properties.Length; i++)
				{
					var p = Properties[i];
					if (p != null)
						p.InsertRecord(sw, string.Empty, mappings);
					else
						sw.Write("\\N");
					if (i < Properties.Length - 1)
						sw.Write('\t');
				}
			}
			else
			{
				sw.Write('(');
				for (int i = 0; i < Properties.Length; i++)
				{
					var p = Properties[i];
					if (p != null)
					{
						if (p.MustEscapeRecord)
						{
							sw.Write('"');
							//TODO string.Empty !?
							p.InsertRecord(sw, "1", null);
							sw.Write('"');
						}
						else p.InsertRecord(sw, string.Empty, null);
					}
					if (i < Properties.Length - 1)
						sw.Write(',');
				}
				sw.Write(')');
			}
			sw.Flush();
			cms.Position = 0;
			return cms;
		}

		public override void InsertRecord(StreamWriter sw, string escaping, Action<StreamWriter, char> mappings)
		{
			if (Properties == null)
				return;
			sw.Write('(');
			var newEscaping = escaping + '1';
			string quote = null;
			for (int i = 0; i < Properties.Length; i++)
			{
				var p = Properties[i];
				if (p != null)
				{
					if (p.MustEscapeRecord)
					{
						quote = quote ?? BuildQuoteEscape(escaping);
						if (mappings != null)
							foreach (var q in quote)
								mappings(sw, q);
						else
							sw.Write(quote);
						p.InsertRecord(sw, newEscaping, mappings);
						if (mappings != null)
							foreach (var q in quote)
								mappings(sw, q);
						else
							sw.Write(quote);
					}
					else p.InsertRecord(sw, escaping, mappings);
				}
				if (i < Properties.Length - 1)
					sw.Write(',');
			}
			sw.Write(')');
		}

		public override void InsertArray(StreamWriter sw, string escaping, Action<StreamWriter, char> mappings)
		{
			if (Properties == null)
			{
				sw.Write("NULL");
				return;
			}
			sw.Write('(');
			var newEscaping = escaping + '1';
			string quote = null;
			for (int i = 0; i < Properties.Length; i++)
			{
				var p = Properties[i];
				if (p != null)
				{
					if (p.MustEscapeRecord)
					{
						quote = quote ?? BuildQuoteEscape(escaping);
						if (mappings != null)
							foreach (var q in quote)
								mappings(sw, q);
						else
							sw.Write(quote);
						p.InsertRecord(sw, newEscaping, mappings);
						if (mappings != null)
							foreach (var q in quote)
								mappings(sw, q);
						else
							sw.Write(quote);
					}
					else p.InsertRecord(sw, escaping, mappings);
				}
				if (i < Properties.Length - 1)
					sw.Write(',');
			}
			sw.Write(')');
		}
	}
}
