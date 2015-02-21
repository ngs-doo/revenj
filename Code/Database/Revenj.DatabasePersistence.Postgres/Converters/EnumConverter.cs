using System;
using System.Collections.Generic;
using System.IO;
using System.Text;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public static class EnumConverter
	{
		public static T? ParseNullable<T>(TextReader reader, int context)
			where T : struct
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var sb = new StringBuilder();
			while (cur != -1 && cur != ',' && cur != ')')
			{
				sb.Append((char)cur);
				cur = reader.Read();
			}
			T value;
			if (Enum.TryParse<T>(sb.ToString(), out value))
				return value;
			return null;
		}

		public static T Parse<T>(TextReader reader, int context)
			where T : struct
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return default(T);
			var sb = new StringBuilder();
			while (cur != -1 && cur != ',' && cur != ')')
			{
				sb.Append((char)cur);
				cur = reader.Read();
			}
			T value;
			Enum.TryParse<T>(sb.ToString(), out value);
			return value;
		}

		public static List<T?> ParseNullableCollection<T>(TextReader reader, int context)
			where T : struct
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var espaced = cur != '{';
			if (espaced)
			{
				for (int i = 0; i < context; i++)
					cur = reader.Read();
			}
			var innerContext = context << 1;
			var list = new List<T?>();
			cur = reader.Peek();
			if (cur == '}')
				reader.Read();
			T value;
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				var sb = new StringBuilder();
				if (cur == '"' || cur == '\\')
				{
					for (int i = 0; i < innerContext; i++)
						cur = reader.Read();
					while (cur != -1)
					{
						if (cur == '\\' || cur == '"')
						{
							for (int i = 0; i < innerContext; i++)
								cur = reader.Read();
							if (cur == ',' || cur == '}')
								break;
							for (int i = 0; i < innerContext - 1; i++)
								cur = reader.Read();
						}
						sb.Append((char)cur);
						cur = reader.Read();
					}
					if (Enum.TryParse<T>(sb.ToString(), out value))
						list.Add(value);
					else
						list.Add(null);
				}
				else
				{
					do
					{
						sb.Append((char)cur);
						cur = reader.Read();
					} while (cur != -1 && cur != ',' && cur != '}');
					var val = sb.ToString();
					if (val == "NULL")
						list.Add(null);
					else
					{
						if (Enum.TryParse<T>(sb.ToString(), out value))
							list.Add(value);
						else
							list.Add(null);
					}
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

		public static List<T> ParseCollection<T>(TextReader reader, int context)
			where T : struct
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var espaced = cur != '{';
			if (espaced)
			{
				for (int i = 0; i < context; i++)
					cur = reader.Read();
			}
			var innerContext = context << 1;
			var list = new List<T>();
			cur = reader.Peek();
			if (cur == '}')
				reader.Read();
			T value;
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				var sb = new StringBuilder();
				if (cur == '"' || cur == '\\')
				{
					for (int i = 0; i < innerContext; i++)
						cur = reader.Read();
					while (cur != -1)
					{
						if (cur == '\\' || cur == '"')
						{
							for (int i = 0; i < innerContext; i++)
								cur = reader.Read();
							if (cur == ',' || cur == '}')
								break;
							for (int i = 0; i < innerContext - 1; i++)
								cur = reader.Read();
						}
						sb.Append((char)cur);
						cur = reader.Read();
					}
					Enum.TryParse<T>(sb.ToString(), out value);
					list.Add(value);
				}
				else
				{
					do
					{
						sb.Append((char)cur);
						cur = reader.Read();
					} while (cur != -1 && cur != ',' && cur != '}');
					var val = sb.ToString();
					if (val == "NULL")
						list.Add(default(T));
					else
					{
						Enum.TryParse<T>(sb.ToString(), out value);
						list.Add(value);
					}
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

		public static IPostgresTuple ToTuple<T>(T value)
			where T : struct
		{
			return new EnumTuple(value.ToString());
		}

		public static IPostgresTuple ToTuple<T>(T? value)
			where T : struct
		{
			return value != null ? new EnumTuple(value.ToString()) : default(IPostgresTuple);
		}

		class EnumTuple : IPostgresTuple
		{
			private readonly string Value;
			private readonly bool EscapeArray;

			public EnumTuple(string value)
			{
				this.Value = value;
				EscapeArray = value == "NULL";
			}

			public bool MustEscapeRecord { get { return false; } }
			public bool MustEscapeArray { get { return EscapeArray; } }

			public string BuildTuple(bool quote)
			{
				if (Value == null)
					return "NULL";
				return quote ? "'" + Value + "'" : Value;
			}

			private void Escape(TextWriter sw, string escaping, Action<TextWriter, char> mappings)
			{
				if (mappings != null)
				{
					foreach (var c in Value)
						mappings(sw, c);
				}
				else sw.Write(Value ?? string.Empty);
			}

			public void InsertRecord(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				sw.Write(Value ?? string.Empty);
			}

			public void InsertArray(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				if (Value == null)
					sw.Write("NULL");
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
}
