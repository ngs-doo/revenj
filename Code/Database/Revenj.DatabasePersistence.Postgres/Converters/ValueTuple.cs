using System;
using System.IO;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public class ValueTuple : PostgresTuple
	{
		private readonly string Value;
		private readonly bool HasMarkers;

		private readonly bool EscapeRecord;
		private readonly bool EscapeArray;

		public ValueTuple(string value)
		{
			this.Value = value;
			if (value != null)
			{
				EscapeRecord = value.Length == 0;
				EscapeArray = value.Length == 0 || value == "NULL";
				for (int i = 0; i < Value.Length; i++)
				{
					var v = Value[i];
					EscapeRecord = EscapeRecord || v == ',' || v == '\\' || v == '"' || v == '(' || v == ')' || char.IsWhiteSpace(v);
					EscapeArray = EscapeArray || v == ',' || v == '\\' || v == '"' || v == '{' || v == '}' || char.IsWhiteSpace(v);
					HasMarkers = HasMarkers || v == '\\' || v == '"';
				}
			}
			else
			{
				EscapeArray = true;
			}
		}

		public ValueTuple(string value, bool record, bool array)
		{
			this.Value = value;
			this.EscapeRecord = record;
			this.EscapeArray = array;
			this.HasMarkers = record || array;
		}

		public override bool MustEscapeRecord { get { return EscapeRecord; } }
		public override bool MustEscapeArray { get { return EscapeArray; } }

		public override string BuildTuple(bool quote)
		{
			if (Value == null)
				return "NULL";
			return quote ? "'" + Value.Replace("'", "''") + "'" : Value;
		}

		private void Escape(TextWriter sw, string escaping, Action<TextWriter, char> mappings)
		{
			string quoteEscape = null;
			string slashEscape = null;
			if (mappings != null)
			{
				foreach (var c in Value)
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
						quoteEscape = quoteEscape ?? BuildQuoteEscape(escaping);
						sw.Write(quoteEscape);
					}
					else if (c == '\\')
					{
						slashEscape = slashEscape ?? BuildSlashEscape(escaping.Length);
						sw.Write(slashEscape);
					}
					else sw.Write(c);
				}
			}
		}

		public override void InsertRecord(TextWriter sw, string escaping, Action<TextWriter, char> mappings)
		{
			if (HasMarkers)
				Escape(sw, escaping, mappings);
			else
			{
				if (mappings != null)
					foreach (var c in Value ?? string.Empty)
						mappings(sw, c);
				else
					sw.Write(Value ?? string.Empty);
			}
		}

		public override void InsertArray(TextWriter sw, string escaping, Action<TextWriter, char> mappings)
		{
			if (Value == null)
				sw.Write("NULL");
			else if (HasMarkers)
				Escape(sw, escaping, mappings);
			else
			{
				if (mappings != null)
					foreach (var c in Value ?? string.Empty)
						mappings(sw, c);
				else
					sw.Write(Value);
			}
		}
	}
}
