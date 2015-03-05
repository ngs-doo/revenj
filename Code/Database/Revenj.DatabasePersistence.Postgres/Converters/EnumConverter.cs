using System;
using System.Collections.Generic;
using System.IO;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public static class EnumConverter
	{
		public static List<T?> ParseNullableCollection<T>(BufferedTextReader reader, int context, Func<BufferedTextReader, T> factory)
			where T : struct
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var espaced = cur != '{';
			if (espaced)
				reader.Read(context);
			cur = reader.Peek();
			if (cur == '}')
			{
				if (espaced)
					reader.Read(context);
				else
					reader.Read(2);
				return new List<T?>(0);
			}
			var innerContext = context == 0 ? 1 : context << 1;
			var list = new List<T?>();
			do
			{
				cur = reader.Read();
				if (cur == '"' || cur == '\\')
				{
					cur = reader.Read(innerContext);
					reader.InitBuffer((char)cur);
					reader.FillUntil('\\', '"');
					list.Add(factory(reader));
					cur = reader.Read(innerContext + 1);
				}
				else
				{
					reader.InitBuffer((char)cur);
					reader.FillUntil(',', '}');
					cur = reader.Read();
					if (reader.BufferMatches("NULL"))
						list.Add(null);
					else
						list.Add(factory(reader));
				}
			} while (cur == ',');
			if (espaced)
				reader.Read(context + 1);
			else
				reader.Read();
			return list;
		}

		public static List<T> ParseCollection<T>(BufferedTextReader reader, int context, Func<BufferedTextReader, T> factory)
			where T : struct
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var espaced = cur != '{';
			if (espaced)
				reader.Read(context);
			cur = reader.Peek();
			if (cur == '}')
			{
				if (espaced)
					reader.Read(context);
				else
					reader.Read(2);
				return new List<T>(0);
			}
			var innerContext = context == 0 ? 1 : context << 1;
			var list = new List<T>();
			do
			{
				cur = reader.Read();
				if (cur == '"' || cur == '\\')
				{
					cur = reader.Read(innerContext);
					reader.InitBuffer((char)cur);
					reader.FillUntil('\\', '"');
					list.Add(factory(reader));
					cur = reader.Read(innerContext + 1);
				}
				else
				{
					reader.InitBuffer((char)cur);
					reader.FillUntil(',', '}');
					cur = reader.Read();
					if (reader.BufferMatches("NULL"))
						list.Add(default(T));
					else
						list.Add(factory(reader));
				}
			} while (cur == ',');
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
					foreach (var c in Value)
						mappings(sw, c);
				else
					sw.Write(Value);
			}
		}
	}
}
