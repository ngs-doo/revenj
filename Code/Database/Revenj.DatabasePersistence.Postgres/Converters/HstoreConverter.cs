using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;

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
			if (value == null)
				return null;
			return
				string.Join(
					", ",
					value.Where(it => it.Value != null).Select(it => "\"{0}\"=>\"{1}\"".With(
						it.Key.Replace("\\", "\\\\").Replace("\"", "\\\""),
						it.Value.Replace("\\", "\\\\").Replace("\"", "\\\""))));
		}

		public static PostgresTuple ToTuple(IDictionary<string, string> value)
		{
			return value != null ? new DictionaryTuple(value) : null;
		}

		public static Dictionary<string, string> Parse(TextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
			{
				return null;
			}
			return ParseDictionary(reader, context, context > 0 ? context << 1 : 1, ref cur);
		}


		private static Dictionary<string, string> ParseDictionary(TextReader reader, int context, int quoteContext, ref int cur)
		{
			var dict = new Dictionary<string, string>();
			for (int i = 0; i < context; i++)
				cur = reader.Read();
			var loop = true;
			var first = true;
			do
			{
				if (first)
				{
					for (int i = 0; i < quoteContext - context; i++)
						cur = reader.Read();
					if (cur == ',' || cur == ')' || cur == '}')
						return dict;
					for (int i = 0; i < context; i++)
						cur = reader.Read();
					first = false;
				}
				else
				{
					for (int i = 0; i < quoteContext; i++)
						cur = reader.Read();
				}
				var name = new StringBuilder();
				do
				{
					if (cur == '\\' || cur == '"')
					{
						for (int i = 0; i < quoteContext; i++)
							cur = reader.Read();
						if (cur == '=')
							break;
						for (int i = 0; i < quoteContext - 1; i++)
							cur = reader.Read();
					}
					name.Append((char)cur);
					cur = reader.Read();
				} while (cur != -1);
				reader.Read();
				cur = reader.Read();
				if (cur == 'N')
				{
					reader.Read();
					reader.Read();
					reader.Read();
					dict[name.ToString()] = null;
					cur = reader.Read();
					loop = cur == ',';
					if (loop)
					{
						while (reader.Read() == ' ') ;
					}
					else
					{
						for (int i = 0; i < context; i++)
							reader.Read();
					}
				}
				else
				{
					var value = new StringBuilder();
					for (int i = 0; i < quoteContext; i++)
						cur = reader.Read();
					do
					{
						if (cur == '\\' || cur == '"')
						{
							for (int i = 0; i < quoteContext; i++)
								cur = reader.Read();
							if (cur == ',' || cur == -1)
							{
								loop = reader.Read() == ' ';
								while (loop && reader.Read() == ' ') ;
								break;
							}
							for (int i = 0; i < context; i++)
								cur = reader.Read();
							if (cur == ',' || cur == ')' || cur == '}')
							{
								loop = false;
								break;
							}
							for (int i = 0; i < context - 1; i++)
								cur = reader.Read();
						}
						value.Append((char)cur);
						cur = reader.Read();
					} while (cur != -1);
					dict[name.ToString()] = value.ToString();
				}
			} while (loop && cur != -1);
			return dict;
		}

		public static List<Dictionary<string, string>> ParseCollection(TextReader reader, int context)
		{
			return ParseCollection(reader, context, true);
		}

		public static List<Dictionary<string, string>> ParseCollection(TextReader reader, int context, bool allowNulls)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var espaced = cur != '{';
			if (espaced)
			{
				for (int i = 0; i < context; i++)
					reader.Read();
			}
			var innerContext = context << 1;
			var list = new List<Dictionary<string, string>>();
			cur = reader.Peek();
			if (cur == '}')
				reader.Read();
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				if (cur == 'N')
				{
					reader.Read();
					reader.Read();
					reader.Read();
					list.Add(allowNulls ? null : new Dictionary<string, string>());
					cur = reader.Read();
				}
				else
				{
					list.Add(ParseDictionary(reader, innerContext, innerContext << 1, ref cur));
				}
			}
			if (espaced)
			{
				for (int i = 0; i < context; i++)
					reader.Read();
			}
			reader.Read();
			return list;
		}

		class DictionaryTuple : PostgresTuple
		{
			private IDictionary<string, string> Value;

			public DictionaryTuple(IDictionary<string, string> value)
			{
				this.Value = value;
			}

			public override bool MustEscapeRecord { get { return true; } }
			public override bool MustEscapeArray { get { return Value != null; } }

			public override void InsertRecord(StreamWriter sw, string escaping, Action<StreamWriter, char> mappings)
			{
				if (Value == null)
					return;
				var esc = BuildQuoteEscape(escaping);
				var quoteEscape = new Lazy<string>(() => BuildQuoteEscape(escaping + "0"));
				var slashEscape = new Lazy<string>(() => BuildSlashEscape(escaping.Length + 1));
				var len = Value.Keys.Count;
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
						mappings(sw, '=');
						mappings(sw, '>');
						if (kv.Value == null)
						{
							foreach (var c in "NULL")
								mappings(sw, c);
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
							sw.Write(',');
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
							{
								sw.Write(quoteEscape.Value);
							}
							else if (c == '\\')
							{
								sw.Write(slashEscape.Value);
							}
							else sw.Write(c);
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
								{
									sw.Write(quoteEscape.Value);
								}
								else if (c == '\\')
								{
									sw.Write(slashEscape.Value);
								}
								else sw.Write(c);
							}
							sw.Write(esc);
						}
						if (len > 0)
							sw.Write(',');
					}
				}
			}

			public override void InsertArray(StreamWriter sw, string escaping, Action<StreamWriter, char> mappings)
			{
				if (Value == null)
				{
					sw.Write("NULL");
					return;
				}
				InsertRecord(sw, escaping, mappings);
			}
		}

	}
}