using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using NGS.Common;
using NGS.Utility;

namespace NGS.DatabasePersistence.Postgres.Converters
{
	public class ArrayTuple : PostgresTuple
	{
		private readonly PostgresTuple[] Elements;

		public ArrayTuple(PostgresTuple[] elements)
		{
			this.Elements = elements;
		}

		private bool? EscapeRecord;

		public override bool MustEscapeRecord
		{
			get
			{
				if (EscapeRecord == null)
					EscapeRecord = Elements != null
						&& (Elements.Length > 1 || Elements.Length > 0 && Elements[0] != null && Elements[0].MustEscapeRecord);
				return EscapeRecord.Value;
			}
		}

		public override bool MustEscapeArray
		{
			get { return Elements != null; }
		}

		public static ArrayTuple Create<T>(T[] elements, Func<T, PostgresTuple> converter)
		{
			if (elements != null)
			{
				var tuples = new PostgresTuple[elements.Length];
				for (int i = 0; i < elements.Length; i++)
					tuples[i] = converter(elements[i]);
				return new ArrayTuple(tuples);
			}
			return null;
		}

		public static ArrayTuple Create<T>(List<T> elements, Func<T, PostgresTuple> converter)
		{
			if (elements != null)
			{
				var tuples = new PostgresTuple[elements.Count];
				for (int i = 0; i < elements.Count; i++)
					tuples[i] = converter(elements[i]);
				return new ArrayTuple(tuples);
			}
			return null;
		}

		public static ArrayTuple Create<T>(IEnumerable<T> elements, Func<T, PostgresTuple> converter)
		{
			if (elements != null)
			{
				var tuples = new PostgresTuple[elements.Count()];
				var i = 0;
				foreach (var el in elements)
					tuples[i++] = converter(el);
				return new ArrayTuple(tuples);
			}
			return null;
		}

		public override string BuildTuple(bool quote)
		{
			if (Elements == null)
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
				sw.Write('{');
				for (int i = 0; i < Elements.Length; i++)
				{
					var e = Elements[i];
					if (e != null)
					{
						if (e.MustEscapeArray)
						{
							sw.Write('"');
							e.InsertArray(sw, "0", mappings);
							sw.Write('"');
						}
						else e.InsertArray(sw, string.Empty, mappings);
					}
					else sw.Write("NULL");
					if (i < Elements.Length - 1)
						sw.Write(',');
				}
				sw.Write('}');
				if (quote)
					sw.Write('\'');
				sw.Flush();
				cms.Position = 0;
				return cms.GetReader().ReadToEnd();
			}
		}

		private static MemoryStream NullStream = new MemoryStream(new byte[] { (byte)'N', (byte)'U', (byte)'L', (byte)'L' });

		public Stream Build()
		{
			if (Elements == null)
				return NullStream;
			var cms = ChunkedMemoryStream.Create();
			var sw = cms.GetWriter();
			sw.Write('{');
			for (int i = 0; i < Elements.Length; i++)
			{
				var e = Elements[i];
				if (e != null)
				{
					if (e.MustEscapeArray)
					{
						sw.Write('"');
						e.InsertArray(sw, "0", null);
						sw.Write('"');
					}
					else e.InsertArray(sw, string.Empty, null);
				}
				else sw.Write("NULL");
				if (i < Elements.Length - 1)
					sw.Write(',');
			}
			sw.Write('}');
			sw.Flush();
			cms.Position = 0;
			return cms;
		}

		public override void InsertRecord(StreamWriter sw, string escaping, Action<StreamWriter, char> mappings)
		{
			if (Elements == null)
				return;
			sw.Write('{');
			var newEscaping = escaping + "0";
			string quote = null;
			for (int i = 0; i < Elements.Length; i++)
			{
				var e = Elements[i];
				if (e != null)
				{
					if (e.MustEscapeArray)
					{
						quote = quote ?? BuildQuoteEscape(escaping);
						if (mappings != null)
							foreach (var q in quote)
								mappings(sw, q);
						else
							sw.Write(quote);
						e.InsertArray(sw, newEscaping, mappings);
						if (mappings != null)
							foreach (var q in quote)
								mappings(sw, q);
						else
							sw.Write(quote);
					}
					else e.InsertArray(sw, escaping, mappings);
				}
				else
					sw.Write("NULL");
				if (i < Elements.Length - 1)
					sw.Write(',');
			}
			sw.Write('}');
		}

		public override void InsertArray(StreamWriter sw, string escaping, Action<StreamWriter, char> mappings)
		{
			throw new FrameworkException("Should not happen. Insert array called on array tuple. Nested arrays are invalid construct.");
		}
	}
}
