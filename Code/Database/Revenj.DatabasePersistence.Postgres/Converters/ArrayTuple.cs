using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using Revenj.Common;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public class ArrayTuple : IPostgresTuple
	{
		private readonly IPostgresTuple[] Elements;
		private bool? EscapeRecord;

		public ArrayTuple(IPostgresTuple[] elements)
		{
			this.Elements = elements;
			this.EscapeRecord = null;
		}

		public bool MustEscapeRecord
		{
			get
			{
				if (EscapeRecord == null)
					EscapeRecord = Elements != null
						&& (Elements.Length > 1 || Elements.Length > 0 && Elements[0] != null && Elements[0].MustEscapeRecord);
				return EscapeRecord.Value;
			}
		}

		public bool MustEscapeArray
		{
			get { return Elements != null; }
		}

		public static IPostgresTuple Create<T>(T[] elements, Func<T, IPostgresTuple> converter)
		{
			if (elements != null)
			{
				var tuples = new IPostgresTuple[elements.Length];
				for (int i = 0; i < elements.Length; i++)
					tuples[i] = converter(elements[i]);
				return new ArrayTuple(tuples);
			}
			return null;
		}

		public static IPostgresTuple Create<T>(List<T> elements, Func<T, IPostgresTuple> converter)
		{
			if (elements != null)
			{
				var tuples = new IPostgresTuple[elements.Count];
				for (int i = 0; i < elements.Count; i++)
					tuples[i] = converter(elements[i]);
				return new ArrayTuple(tuples);
			}
			return null;
		}

		public static IPostgresTuple Create<T>(IEnumerable<T> elements, Func<T, IPostgresTuple> converter)
		{
			if (elements != null)
			{
				var tuples = new IPostgresTuple[elements.Count()];
				var i = 0;
				foreach (var el in elements)
					tuples[i++] = converter(el);
				return new ArrayTuple(tuples);
			}
			return null;
		}

		public string BuildTuple(bool quote)
		{
			if (Elements == null)
				return "NULL";
			using (var cms = ChunkedMemoryStream.Create())
			{
				var sw = cms.GetWriter();
				Action<TextWriter, char> mappings = null;
				if (quote)
				{
					mappings = PostgresTuple.EscapeQuote;
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
							e.InsertArray(sw, cms.TmpBuffer, "0", mappings);
							sw.Write('"');
						}
						else e.InsertArray(sw, cms.TmpBuffer, string.Empty, mappings);
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
						e.InsertArray(sw, cms.TmpBuffer, "0", null);
						sw.Write('"');
					}
					else e.InsertArray(sw, cms.TmpBuffer, string.Empty, null);
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

		public void InsertRecord(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
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
						quote = quote ?? PostgresTuple.BuildQuoteEscape(escaping);
						if (mappings != null)
							foreach (var q in quote)
								mappings(sw, q);
						else
							sw.Write(quote);
						e.InsertArray(sw, buf, newEscaping, mappings);
						if (mappings != null)
							foreach (var q in quote)
								mappings(sw, q);
						else
							sw.Write(quote);
					}
					else e.InsertArray(sw, buf, escaping, mappings);
				}
				else
					sw.Write("NULL");
				if (i < Elements.Length - 1)
					sw.Write(',');
			}
			sw.Write('}');
		}

		public void InsertArray(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
		{
			throw new FrameworkException("Should not happen. Insert array called on array tuple. Nested arrays are invalid construct.");
		}
	}
}
