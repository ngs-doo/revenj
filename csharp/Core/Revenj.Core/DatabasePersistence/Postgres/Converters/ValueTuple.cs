using System;
using System.IO;
using System.Linq;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public class ValueTuple : IPostgresTuple
	{
		private static char[] Whitespace = new[] { (char)9, (char)10, (char)11, (char)12, (char)13, (char)32, (char)160, (char)5760, (char)8192, (char)8193, (char)8194, (char)8195, (char)8196, (char)8197, (char)8198, (char)8199, (char)8200, (char)8201, (char)8202, (char)8232, (char)8233, (char)8239, (char)8287, (char)12288 };
		private static char[] RecordEscapes = new[] { ',', '(', ')' }.UnionAll(Whitespace).ToArray();
		private static char[] ArrayEscapes = new[] { ',', '{', '}' }.UnionAll(Whitespace).ToArray();
		private static char[] EscapeMarkers = new[] { '\\', '"' };

		public static readonly IPostgresTuple Empty;

		static ValueTuple()
		{
			Empty = new EmptyValueTuple();
		}

		private readonly string Value;
		private readonly bool HasMarkers;

		private readonly bool EscapeRecord;
		private readonly bool EscapeArray;

		public ValueTuple(string value)
		{
			this.Value = value;
			if (value != null)
			{
				HasMarkers = value.IndexOfAny(EscapeMarkers) != -1;
				//TODO: postgres can actually cope with whitespace... think about changing this
				EscapeRecord = value.Length == 0 || HasMarkers || value.IndexOfAny(RecordEscapes) != -1;
				EscapeArray = value.Length == 0 || value == "NULL" || HasMarkers || value.IndexOfAny(ArrayEscapes) != -1;
			}
			else
			{
				EscapeRecord = false;
				EscapeArray = true;
				HasMarkers = false;
			}
		}

		public static IPostgresTuple From(string value)
		{
			if (value == null)
				return null;
			if (value.Length == 0)
				return Empty;
			return new ValueTuple(value);
		}

		public ValueTuple(string value, bool record, bool array)
		{
			this.Value = value;
			this.EscapeRecord = record;
			this.EscapeArray = array;
			this.HasMarkers = record || array;
		}

		public bool MustEscapeRecord { get { return EscapeRecord; } }
		public bool MustEscapeArray { get { return EscapeArray; } }

		public string BuildTuple(bool quote)
		{
			if (Value == null)
				return "NULL";
			return quote ? "'" + Value.Replace("'", "''") + "'" : Value;
		}

		private void Escape(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
		{
			string quoteEscape = null;
			string slashEscape = null;
			if (mappings != null)
			{
				foreach (var c in Value)
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
					else mappings(sw, c);
				}
			}
			else
			{
				for (int i = 0; i < Value.Length; i++)
				{
					var c = Value[i];
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
					else sw.Write(c);
				}
			}
		}

		public void InsertRecord(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
		{
			if (HasMarkers)
				Escape(sw, buf, escaping, mappings);
			else
			{
				if (mappings != null)
					foreach (var c in Value ?? string.Empty)
						mappings(sw, c);
				else
					sw.Write(Value ?? string.Empty);
			}
		}

		public void InsertArray(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
		{
			if (Value == null)
				sw.Write("NULL");
			else if (HasMarkers)
				Escape(sw, buf, escaping, mappings);
			else
			{
				if (mappings != null)
					foreach (var c in Value)
						mappings(sw, c);
				else
					sw.Write(Value);
			}
		}

		class EmptyValueTuple : IPostgresTuple
		{
			public bool MustEscapeRecord { get { return true; } }
			public bool MustEscapeArray { get { return true; } }
			public void InsertRecord(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings) { }
			public void InsertArray(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings) { }
			public string BuildTuple(bool quote) { return quote ? "'\"\"'" : "\"\""; }
		}
	}
}
