using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public static class HstoreConverter
	{
		//TODO: to private (this is only a hackish way to parse hstore)
		public static Dictionary<string, string> FromDatabase(string value)
		{
			if (value == null)
				return null;
			var dict = new Dictionary<string, string>();
			if (string.IsNullOrWhiteSpace(value))
				return dict;
			var parts = value.Substring(1, value.Length - 2).Split(new[] { "\", \"", "\",\"" }, StringSplitOptions.None);
			foreach (var p in parts)
			{
				var splt = p.Split(new[] { "\"=>\"" }, StringSplitOptions.None);
				var left = splt[0].Replace("\\\"", "\"").Replace("\\\\", "\\");
				var right = splt[1].Replace("\\\"", "\"").Replace("\\\\", "\\");
				dict[left] = right;
			}
			return dict;
		}

		public static string ToDatabase(IDictionary<string, string> value)
		{
			return string.Join(
					", ",
					value.Where(it => it.Value != null).Select(it => "\"{0}\"=>\"{1}\"".With(
						it.Key.Replace("\\", "\\\\").Replace("\"", "\\\""),
						it.Value.Replace("\\", "\\\\").Replace("\"", "\\\""))));
		}

		public static int SerializeURI(IDictionary<string, string> value, char[] buf, int pos)
		{
			//TODO: optimize
			var str = ToDatabase(value);
			str.CopyTo(0, buf, pos, str.Length);
			return pos + str.Length;
		}

		public static int SerializeCompositeURI(IDictionary<string, string> value, char[] buf, int pos)
		{
			//TODO: optimize
			var str = ToDatabase(value);
			return StringConverter.SerializeCompositeURI(str, buf, pos);
		}

		public static IPostgresTuple ToTuple(IDictionary<string, string> value)
		{
			//TODO: empty dictionary
			return value != null ? new DictionaryTuple(value) : null;
		}

		public static Dictionary<string, string> Parse(BufferedTextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			return ParseDictionary(reader, context, context > 0 ? context << 1 : 1, ref cur, ')');
		}

		private static Dictionary<string, string> ParseDictionary(
			BufferedTextReader reader,
			int context,
			int quoteContext,
			ref int cur,
			char matchEnd)
		{
			cur = reader.Read(quoteContext);
			if (cur == ',' || cur == matchEnd)
				return new Dictionary<string, string>(0);
			var dict = new Dictionary<string, string>();
			for (int i = 0; i < context; i++)
				cur = reader.Read();
			reader.InitBuffer();
			do
			{
				do
				{
					if (cur == '\\' || cur == '"')
					{
						cur = reader.Read(quoteContext);
						if (cur == '=')
							break;
						for (int i = 0; i < quoteContext - 1; i++)
							cur = reader.Read();
					}
					reader.AddToBuffer((char)cur);
					reader.FillUntil('\\', '"');
					cur = reader.Read();
				} while (cur != -1);
				var name = reader.BufferToString();
				cur = reader.Read(2);
				if (cur == 'N')
				{
					dict.Add(name, null);
					cur = reader.Read(4);
					if (cur == '\\' || cur == '"')
					{
						reader.Read(context);
						return dict;
					}
					if (cur == ',' && reader.Peek() != ' ')
						return dict;
					do { cur = reader.Read(); }
					while (cur == ' ');
				}
				else
				{
					cur = reader.Read(quoteContext);
					do
					{
						if (cur == '\\' || cur == '"')
						{
							cur = reader.Read(quoteContext);
							if (cur == ',')
							{
								dict.Add(name, reader.BufferToString());
								do { cur = reader.Read(); }
								while (cur == ' ');
								cur = reader.Read(quoteContext);
								break;
							}
							for (int i = 0; i < context; i++)
								cur = reader.Read();
							if (cur == ',' || cur == -1 || cur == matchEnd)
							{
								dict.Add(name, reader.BufferToString());
								return dict;
							}
							for (int i = 0; i < context - 1; i++)
								cur = reader.Read();
						}
						reader.AddToBuffer((char)cur);
						reader.FillUntil('\\', '"');
						cur = reader.Read();
					} while (cur != -1);
				}
			} while (cur != -1);
			return dict;
		}

		public static List<Dictionary<string, string>> ParseCollection(BufferedTextReader reader, int context)
		{
			return ParseCollection(reader, context, true);
		}

		public static List<Dictionary<string, string>> ParseCollection(BufferedTextReader reader, int context, bool allowNulls)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var escaped = cur != '{';
			if (escaped)
				reader.Read(context);
			int innerContext = context == 0 ? 1 : context << 1;
			var list = new List<Dictionary<string, string>>();
			cur = reader.Peek();
			if (cur == '}')
				reader.Read();
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				if (cur == 'N')
				{
					cur = reader.Read(4);
					list.Add(allowNulls ? null : new Dictionary<string, string>());
				}
				else
				{
					list.Add(ParseDictionary(reader, innerContext, innerContext << 1, ref cur, '}'));
				}
			}
			if (escaped)
				reader.Read(context + 1);
			else
				reader.Read();
			return list;
		}

		class DictionaryTuple : IPostgresTuple
		{
			private IDictionary<string, string> Value;

			public DictionaryTuple(IDictionary<string, string> value)
			{
				this.Value = value;
			}

			public bool MustEscapeRecord { get { return true; } }
			public bool MustEscapeArray { get { return true; } }

			public string BuildTuple(bool quote) { return PostgresTuple.BuildTuple(this, quote); }

			public void InsertRecord(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				var esc = PostgresTuple.BuildQuoteEscape(escaping);
				var quoteEscape = new Lazy<string>(() => PostgresTuple.BuildQuoteEscape(escaping + "0"));
				var slashEscape = new Lazy<string>(() => PostgresTuple.BuildSlashEscape(escaping.Length + 1));
				var len = Value.Count;
				if (mappings != null)
				{
					foreach (var kv in Value)
					{
						len--;
						foreach (var c in esc)
							mappings(sw, c);
						foreach (var c in kv.Key)
						{
							if (c == '"')
							{
								foreach (var e in quoteEscape.Value)
									mappings(sw, e);
							}
							else if (c == '\\')
							{
								foreach (var e in slashEscape.Value)
									mappings(sw, e);
							}
							else mappings(sw, c);
						}
						foreach (var c in esc)
							mappings(sw, c);
						sw.Write("=>");
						if (kv.Value == null)
						{
							sw.Write("NULL");
						}
						else
						{
							foreach (var c in esc)
								mappings(sw, c);
							foreach (var c in kv.Value)
							{
								if (c == '"')
								{
									foreach (var e in quoteEscape.Value)
										mappings(sw, e);
								}
								else if (c == '\\')
								{
									foreach (var e in slashEscape.Value)
										mappings(sw, e);
								}
								else mappings(sw, c);
							}
							foreach (var c in esc)
								mappings(sw, c);
						}
						if (len > 0)
							sw.Write(", ");
					}
				}
				else
				{
					foreach (var kv in Value)
					{
						len--;
						sw.Write(esc);
						foreach (var c in kv.Key)
						{
							if (c == '"')
								sw.Write(quoteEscape.Value);
							else if (c == '\\')
								sw.Write(slashEscape.Value);
							else
								sw.Write(c);
						}
						sw.Write(esc);
						sw.Write("=>");
						if (kv.Value == null)
							sw.Write("NULL");
						else
						{
							sw.Write(esc);
							foreach (var c in kv.Value)
							{
								if (c == '"')
									sw.Write(quoteEscape.Value);
								else if (c == '\\')
									sw.Write(slashEscape.Value);
								else
									sw.Write(c);
							}
							sw.Write(esc);
						}
						if (len > 0)
							sw.Write(", ");
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