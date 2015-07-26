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
		public static readonly IPostgresTuple Empty;
		public static readonly IPostgresTuple Null;

		static ArrayTuple()
		{
			Empty = new EmptyArrayTuple();
			Null = new NullTuple();
		}

		private readonly IPostgresTuple[] Elements;
		private readonly bool EscapeRecord;

		private ArrayTuple(IPostgresTuple[] elements)
		{
			this.Elements = elements;
			this.EscapeRecord = elements.Length > 1 || elements[0] != null && elements[0].MustEscapeRecord;
		}

		public static IPostgresTuple From(IPostgresTuple[] elements)
		{
			if (elements == null)
				return Null;
			if (elements.Length == 0)
				return Empty;
			return new ArrayTuple(elements);
		}

		class EmptyArrayTuple : IPostgresTuple
		{
			public bool MustEscapeRecord { get { return false; } }
			public bool MustEscapeArray { get { return false; } }
			public void InsertRecord(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				sw.Write("{}");
			}
			public void InsertArray(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				throw new FrameworkException("Should not happen. Insert array called on array tuple. Nested arrays are invalid construct.");
			}
			public string BuildTuple(bool quote) { return quote ? "'{}'" : "{}"; }
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

		public bool MustEscapeRecord { get { return EscapeRecord; } }
		public bool MustEscapeArray { get { return true; } }

		public static IPostgresTuple Create<T>(T[] elements, Func<T, IPostgresTuple> converter)
		{
			if (elements == null)
				return null;
			if (elements.Length == 0)
				return Empty;
			var tuples = new IPostgresTuple[elements.Length];
			for (int i = 0; i < elements.Length; i++)
				tuples[i] = converter(elements[i]);
			return new ArrayTuple(tuples);
		}

		public static IPostgresTuple Create<T>(List<T> elements, Func<T, IPostgresTuple> converter)
		{
			if (elements == null)
				return null;
			if (elements.Count == 0)
				return Empty;
			var tuples = new IPostgresTuple[elements.Count];
			for (int i = 0; i < elements.Count; i++)
				tuples[i] = converter(elements[i]);
			return new ArrayTuple(tuples);
		}

		public static IPostgresTuple Create<T>(IEnumerable<T> elements, Func<T, IPostgresTuple> converter)
		{
			if (elements == null)
				return null;
			var count = elements.Count();
			if (count == 0)
				return Empty;
			var tuples = new IPostgresTuple[count];
			var i = 0;
			foreach (var el in elements)
				tuples[i++] = converter(el);
			return new ArrayTuple(tuples);
		}

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
				sw.Write('{');
				var e = Elements[0];
				if (e != null)
				{
					if (e.MustEscapeArray)
					{
						sw.Write('"');
						e.InsertArray(sw, cms.SmallBuffer, "0", mappings);
						sw.Write('"');
					}
					else e.InsertArray(sw, cms.SmallBuffer, string.Empty, mappings);
				}
				else sw.Write("NULL");
				for (int i = 1; i < Elements.Length; i++)
				{
					sw.Write(',');
					e = Elements[i];
					if (e != null)
					{
						if (e.MustEscapeArray)
						{
							sw.Write('"');
							e.InsertArray(sw, cms.SmallBuffer, "0", mappings);
							sw.Write('"');
						}
						else e.InsertArray(sw, cms.SmallBuffer, string.Empty, mappings);
					}
					else sw.Write("NULL");
				}
				sw.Write('}');
				if (quote)
					sw.Write('\'');
				sw.Flush();
				cms.Position = 0;
				return cms.GetReader().ReadToEnd();
			}
		}

		public Stream Build()
		{
			var cms = ChunkedMemoryStream.Create();
			var sw = cms.GetWriter();
			sw.Write('{');
			var e = Elements[0];
			if (e != null)
			{
				if (e.MustEscapeArray)
				{
					sw.Write('"');
					e.InsertArray(sw, cms.SmallBuffer, "0", null);
					sw.Write('"');
				}
				else e.InsertArray(sw, cms.SmallBuffer, string.Empty, null);
			}
			else sw.Write("NULL");
			for (int i = 1; i < Elements.Length; i++)
			{
				sw.Write(',');
				e = Elements[i];
				if (e != null)
				{
					if (e.MustEscapeArray)
					{
						sw.Write('"');
						e.InsertArray(sw, cms.SmallBuffer, "0", null);
						sw.Write('"');
					}
					else e.InsertArray(sw, cms.SmallBuffer, string.Empty, null);
				}
				else sw.Write("NULL");
			}
			sw.Write('}');
			sw.Flush();
			cms.Position = 0;
			return cms;
		}

		public void InsertRecord(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
		{
			sw.Write('{');
			var newEscaping = escaping + "0";
			string quote = null;
			var e = Elements[0];
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
			else sw.Write("NULL");
			for (int i = 1; i < Elements.Length; i++)
			{
				sw.Write(',');
				e = Elements[i];
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
				else sw.Write("NULL");
			}
			sw.Write('}');
		}

		public void InsertArray(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
		{
			throw new FrameworkException("Should not happen. Insert array called on array tuple. Nested arrays are invalid construct.");
		}
	}
}
