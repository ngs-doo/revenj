using System;
using System.Collections.Generic;
using System.IO;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public static class EnumConverter
	{
		public static T? ParseNullable<T>(BufferedTextReader reader, int context)
			where T : struct
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			reader.InitBuffer();
			reader.FillUntil(',', ')');
			reader.Read();
			T value;
			if (Enum.TryParse<T>(reader.BufferToString(), out value))
				return value;
			return null;
		}

		public static T Parse<T>(BufferedTextReader reader, int context)
			where T : struct
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return default(T);
			reader.InitBuffer();
			reader.FillUntil(',', ')');
			reader.Read();
			T value;
			Enum.TryParse<T>(reader.BufferToString(), out value);
			return value;
		}

		public static List<T?> ParseNullableCollection<T>(BufferedTextReader reader, int context)
			where T : struct
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var espaced = cur != '{';
			if (espaced)
				reader.Read(context);
			var innerContext = context << 1;
			var list = new List<T?>();
			cur = reader.Peek();
			if (cur == '}')
				reader.Read();
			T value;
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				reader.InitBuffer();
				if (cur == '"' || cur == '\\')
				{
					cur = reader.Read(innerContext);
					while (cur != -1)
					{
						if (cur == '\\' || cur == '"')
						{
							cur = reader.Read(innerContext);
							if (cur == ',' || cur == '}')
								break;
							cur = reader.Read(innerContext - 1);
						}
						reader.AddToBuffer((char)cur);
						cur = reader.Read();
					}
					if (Enum.TryParse<T>(reader.BufferToString(), out value))
						list.Add(value);
					else
						list.Add(null);
				}
				else
				{
					reader.FillUntil(',', '}');
					cur = reader.Read();
					if (reader.BufferMatches("NULL"))
						list.Add(null);
					else
					{
						if (Enum.TryParse<T>(reader.BufferToString(), out value))
							list.Add(value);
						else
							list.Add(null);
					}
				}
			}
			if (espaced)
				reader.Read(context + 1);
			else
				reader.Read();
			return list;
		}

		public static List<T> ParseCollection<T>(BufferedTextReader reader, int context)
			where T : struct
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var espaced = cur != '{';
			if (espaced)
				reader.Read(context);
			var innerContext = context << 1;
			var list = new List<T>();
			cur = reader.Peek();
			if (cur == '}')
				reader.Read();
			T value;
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				reader.InitBuffer();
				if (cur == '"' || cur == '\\')
				{
					cur = reader.Read(innerContext);
					while (cur != -1)
					{
						if (cur == '\\' || cur == '"')
						{
							cur = reader.Read(innerContext);
							if (cur == ',' || cur == '}')
								break;
							cur = reader.Read(innerContext - 1);
						}
						reader.AddToBuffer((char)cur);
						cur = reader.Read();
					}
					Enum.TryParse<T>(reader.BufferToString(), out value);
					list.Add(value);
				}
				else
				{
					reader.FillUntil(',', '}');
					cur = reader.Read();
					if (reader.BufferMatches("NULL"))
						list.Add(default(T));
					else
					{
						Enum.TryParse<T>(reader.BufferToString(), out value);
						list.Add(value);
					}
				}
			}
			if (espaced)
				reader.Read(context + 1);
			else
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
			return value != null ? new EnumTuple(value.ToString()) : null;
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
				return quote ? "'" + Value + "'" : Value;
			}

			private void Escape(TextWriter sw, string escaping, Action<TextWriter, char> mappings)
			{
				if (mappings != null)
				{
					foreach (var c in Value)
						mappings(sw, c);
				}
				else sw.Write(Value);
			}

			public void InsertRecord(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				sw.Write(Value);
			}

			public void InsertArray(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
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
