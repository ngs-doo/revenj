using System;
using System.Collections.Generic;
using System.IO;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.Converters
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

		public static byte[] Parse(BufferedTextReader reader, int context)
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

		public static List<byte[]> ParseCollection(BufferedTextReader reader, int context, bool allowNulls)
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

		public static Stream ParseStream(BufferedTextReader reader, int context)
		{
			var cur = reader.Read();
			if (cur == ',' || cur == ')')
				return null;
			var len = context + (context << 1);
			for (int i = 0; i < len; i++)
				reader.Read();
			cur = reader.Read();
			var bytes = new byte[512];
			int ind = 0;
			while (ind < 512 && cur != '\\' && cur != '"')
			{
				bytes[ind++] = (byte)((CharLookup[cur] << 4) + CharLookup[reader.Read()]);
				cur = reader.Read();
			}
			Stream stream;
			if (cur == '\\' || cur == '"')
			{
				stream = new MemoryStream();
				stream.Write(bytes, 0, ind);
			}
			else
			{
				stream = ChunkedMemoryStream.Create();
				stream.Write(bytes, 0, ind);
				while (cur != -1 && cur != '\\' && cur != '"')
				{
					stream.WriteByte((byte)((CharLookup[cur] << 4) + CharLookup[reader.Read()]));
					cur = reader.Read();
				}
			}
			for (int i = 0; i < context; i++)
				reader.Read();
			stream.Position = 0;
			return stream;
		}

		public static List<Stream> ParseStreamCollection(BufferedTextReader reader, int context, bool allowNulls)
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
			var bytes = new byte[512];
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
					cur = reader.Read();
					int ind = 0;
					while (ind < 512 && cur != '\\' && cur != '"')
					{
						bytes[ind++] = (byte)((CharLookup[cur] << 4) + CharLookup[reader.Read()]);
						cur = reader.Read();
					}
					Stream stream;
					if (cur == '\\' || cur == '"')
					{
						stream = new MemoryStream();
						stream.Write(bytes, 0, ind);
					}
					else
					{
						stream = ChunkedMemoryStream.Create();
						stream.Write(bytes, 0, ind);
						while (cur != -1 && cur != '\\' && cur != '"')
						{
							stream.WriteByte((byte)((CharLookup[cur] << 4) + CharLookup[reader.Read()]));
							cur = reader.Read();
						}
					}
					for (int i = 0; i < innerContext; i++)
						cur = reader.Read();
					stream.Position = 0;
					list.Add(stream);
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

		public static IPostgresTuple ToTuple(byte[] value)
		{
			return value != null ? new ByteTuple(value, value.Length) : default(IPostgresTuple);
		}

		public static IPostgresTuple ToTuple(Stream stream)
		{
			if (stream == null)
				return null;
			var cms = stream as ChunkedMemoryStream;
			if (cms != null) return new StreamTuple(cms, false);
			var ms = stream as MemoryStream;
			if (ms != null) return new ByteTuple(ms.GetBuffer(), (int)ms.Length);
			return new StreamTuple(new ChunkedMemoryStream(stream), true);
		}

		public static IPostgresTuple ToTuple(ChunkedMemoryStream stream, bool dispose)
		{
			return new StreamTuple(stream, dispose);
		}

		class ByteTuple : IPostgresTuple
		{
			private readonly byte[] Value;
			private readonly int Length;

			public ByteTuple(byte[] value, int length)
			{
				this.Value = value;
				this.Length = length;
			}

			public bool MustEscapeRecord { get { return true; } }
			public bool MustEscapeArray { get { return true; } }

			private void BuildArray(TextWriter sw)
			{
				for (int i = 0; i < Value.Length && i < Length; i++)
				{
					var b = Value[i];
					sw.Write(CharMap[b >> 4]);
					sw.Write(CharMap[b & 0xf]);
				}
			}

			public string BuildTuple(bool quote)
			{
				using (var cms = ChunkedMemoryStream.Create())
				{
					var sw = cms.GetWriter();
					if (quote)
					{
						sw.Write('\'');
						InsertRecord(sw, cms.TmpBuffer, string.Empty, null);
						sw.Write('\'');
					}
					else InsertRecord(sw, cms.TmpBuffer, string.Empty, null);
					sw.Flush();
					cms.Position = 0;
					return cms.GetReader().ReadToEnd();
				}
			}

			public void InsertRecord(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				var pref = PostgresTuple.BuildSlashEscape(escaping.Length);
				if (mappings != null)
				{
					foreach (var p in pref)
						mappings(sw, p);
				}
				else sw.Write(pref);
				sw.Write('x');
				BuildArray(sw);
			}

			public void InsertArray(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				//TODO this is wrong
				InsertRecord(sw, buf, escaping, mappings);
			}
		}

		class StreamTuple : IPostgresTuple
		{
			private readonly ChunkedMemoryStream Value;
			private readonly bool Dispose;

			public StreamTuple(ChunkedMemoryStream value, bool dispose)
			{
				this.Value = value;
				this.Dispose = dispose;
			}

			public bool MustEscapeRecord { get { return true; } }
			public bool MustEscapeArray { get { return true; } }

			public string BuildTuple(bool quote)
			{
				using (var cms = ChunkedMemoryStream.Create())
				{
					var sw = cms.GetWriter();
					if (quote)
					{
						sw.Write('\'');
						InsertRecord(sw, cms.TmpBuffer, string.Empty, null);
						sw.Write('\'');
					}
					else InsertRecord(sw, cms.TmpBuffer, string.Empty, null);
					sw.Flush();
					cms.Position = 0;
					return cms.GetReader().ReadToEnd();
				}
			}

			public void InsertRecord(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				var pref = PostgresTuple.BuildSlashEscape(escaping.Length);
				if (mappings != null)
				{
					foreach (var p in pref)
						mappings(sw, p);
				}
				else sw.Write(pref);
				sw.Write('x');
				Value.ToPostgresBytea(sw);
				if (Dispose) Value.Dispose();
			}

			public void InsertArray(TextWriter sw, char[] buf, string escaping, Action<TextWriter, char> mappings)
			{
				//TODO this is wrong
				InsertRecord(sw, buf, escaping, mappings);
			}
		}
	}
}