using System;
using System.Collections.Generic;
using System.IO;
using NGS.Utility;

namespace NGS.DatabasePersistence.Postgres.Converters
{
	public static class ByteaConverter
	{
		private static readonly char[] CharMap;
		private static readonly int[] CharLookup;

		static ByteaConverter()
		{
			CharMap = "0123456789abcdef".ToCharArray();
			CharLookup = new int['f' + 1];
			for (int i = 0; i < CharMap.Length; i++)
				CharLookup[CharMap[i]] = i;
		}

		public static byte[] FromDatabase(string value)
		{
			if (value == null)
				return null;
			if (value.Length > 1 && value[0] == '\\' && value[1] == 'x')
			{
				var data = new byte[value.Length / 2 - 1];
				var pos = 2;
				for (int i = 0; i < data.Length; i++)
					data[i] = (byte)((CharLookup[value[pos++]] << 4) + CharLookup[value[pos++]]);
				return data;
			}
			return new byte[0];
		}

		public static byte[] Parse(TextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var len = context + (context << 1);
			if (len == 0)
				len = 1;
			for (int i = 0; i < len; i++)
				reader.Read();
			cur = reader.Read();
			var list = new List<byte>(1024);
			while (cur != -1 && cur != '\\' && cur != '"')
			{
				list.Add((byte)((CharLookup[cur] << 4) + CharLookup[reader.Read()]));
				cur = reader.Read();
			}
			for (int i = 0; i < context; i++)
				reader.Read();
			return list.ToArray();
		}

		public static List<byte[]> ParseCollection(TextReader reader, int context, bool allowNulls)
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
			var skipInner = innerContext + (innerContext << 1);
			var list = new List<byte[]>();
			cur = reader.Peek();
			if (cur == '}')
				reader.Read();
			var emptyColl = allowNulls ? null : new byte[0];
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				if (cur == 'N')
				{
					reader.Read();
					reader.Read();
					reader.Read();
					list.Add(emptyColl);
					cur = reader.Read();
				}
				else
				{
					for (int i = 0; i < skipInner; i++)
						reader.Read();
					var item = new List<byte>(1024);
					cur = reader.Read();
					while (cur != -1 && cur != '"' && cur != '\\')
					{
						item.Add((byte)((CharLookup[cur] << 4) + CharLookup[reader.Read()]));
						cur = reader.Read();
					}
					for (int i = 0; i < innerContext; i++)
						cur = reader.Read();
					list.Add(item.ToArray());
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

		public static Stream ParseStream(TextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var len = context + (context << 1);
			for (int i = 0; i < len; i++)
				reader.Read();
			cur = reader.Read();
			var cms = ChunkedMemoryStream.Create();
			while (cur != -1 && cur != '\\' && cur != '"')
			{
				cms.WriteByte((byte)((CharLookup[cur] << 4) + CharLookup[reader.Read()]));
				cur = reader.Read();
			}
			for (int i = 0; i < context; i++)
				reader.Read();
			cms.Position = 0;
			return cms;
		}

		public static List<Stream> ParseStreamCollection(TextReader reader, int context, bool allowNulls)
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
			var skipInner = innerContext + (innerContext << 1);
			var list = new List<Stream>();
			cur = reader.Peek();
			if (cur == '}')
				reader.Read();
			var emptyCol = allowNulls ? null : new MemoryStream();
			while (cur != -1 && cur != '}')
			{
				cur = reader.Read();
				if (cur == 'N')
				{
					reader.Read();
					reader.Read();
					reader.Read();
					list.Add(emptyCol);
					cur = reader.Read();
				}
				else
				{
					for (int i = 0; i < skipInner; i++)
						reader.Read();
					var cms = ChunkedMemoryStream.Create();
					cur = reader.Read();
					while (cur != -1 && cur != '"' && cur != '\\')
					{
						cms.WriteByte((byte)((CharLookup[cur] << 4) + CharLookup[reader.Read()]));
						cur = reader.Read();
					}
					for (int i = 0; i < innerContext; i++)
						cur = reader.Read();
					cms.Position = 0;
					list.Add(cms);
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

		public static string ToDatabase(byte[] value)
		{
			if (value == null)
				return null;
			var buf = new char[2 + value.Length * 2];
			buf[0] = '\\';
			buf[1] = 'x';
			var cnt = 2;
			for (int i = 0; i < value.Length; i++)
			{
				var b = value[i];
				buf[cnt++] = CharMap[b >> 4];
				buf[cnt++] = CharMap[b & 0xf];
			}
			return new string(buf);
		}

		public static PostgresTuple ToTuple(byte[] value)
		{
			return value != null ? new ByteTuple(value) : null;
		}

		class ByteTuple : PostgresTuple
		{
			private readonly byte[] Value;

			public ByteTuple(byte[] value)
			{
				this.Value = value;
			}

			public override bool MustEscapeRecord { get { return true; } }
			public override bool MustEscapeArray { get { return true; } }

			private void BuildArray(StreamWriter sw)
			{
				for (int i = 0; i < Value.Length; i++)
				{
					var b = Value[i];
					sw.Write(CharMap[b >> 4]);
					sw.Write(CharMap[b & 0xf]);
				}
			}

			public override string BuildTuple(bool quote)
			{
				if (Value == null)
					return "NULL";
				using (var cms = ChunkedMemoryStream.Create())
				{
					var sw = cms.GetWriter();
					if (quote)
					{
						sw.Write('\'');
						InsertRecord(sw, string.Empty, null);
						sw.Write('\'');
					}
					else InsertRecord(sw, string.Empty, null);
					sw.Flush();
					cms.Position = 0;
					return cms.GetReader().ReadToEnd();
				}
			}

			public override void InsertRecord(StreamWriter sw, string escaping, Action<StreamWriter, char> mappings)
			{
				if (Value != null)
				{
					var pref = BuildSlashEscape(escaping.Length);
					if (mappings != null)
					{
						foreach (var p in pref)
							mappings(sw, p);
					}
					else sw.Write(pref);
					sw.Write('x');
					BuildArray(sw);
				}
			}

			public override void InsertArray(StreamWriter sw, string escaping, Action<StreamWriter, char> mappings)
			{
				//TODO this is wrong
				InsertRecord(sw, escaping, mappings);
			}
		}

		class StreamTuple : PostgresTuple
		{
			private readonly Stream Value;

			public StreamTuple(Stream value)
			{
				this.Value = value;
			}

			public override bool MustEscapeRecord { get { return true; } }
			public override bool MustEscapeArray { get { return true; } }

			private void BuildArray(StreamWriter sw)
			{
				int cur;
				while ((cur = Value.ReadByte()) != -1)
				{
					sw.Write(CharMap[cur >> 4]);
					sw.Write(CharMap[cur & 0xf]);
				}
			}

			public override string BuildTuple(bool quote)
			{
				if (Value == null)
					return "NULL";
				using (var cms = ChunkedMemoryStream.Create())
				{
					var sw = cms.GetWriter();
					if (quote)
					{
						sw.Write('\'');
						InsertRecord(sw, string.Empty, null);
						sw.Write('\'');
					}
					else InsertRecord(sw, string.Empty, null);
					sw.Flush();
					cms.Position = 0;
					return cms.GetReader().ReadToEnd();
				}
			}

			public override void InsertRecord(StreamWriter sw, string escaping, Action<StreamWriter, char> mappings)
			{
				if (Value != null)
				{
					var pref = BuildSlashEscape(escaping.Length);
					if (mappings != null)
					{
						foreach (var p in pref)
							mappings(sw, p);
					}
					else sw.Write(pref);
					sw.Write('x');
					BuildArray(sw);
				}
			}

			public override void InsertArray(StreamWriter sw, string escaping, Action<StreamWriter, char> mappings)
			{
				//TODO this is wrong
				InsertRecord(sw, escaping, mappings);
			}
		}
	}
}