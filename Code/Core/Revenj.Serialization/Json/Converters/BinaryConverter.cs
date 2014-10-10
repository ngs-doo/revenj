using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Runtime.Serialization;
using Revenj.Utility;

namespace Revenj.Serialization.Json.Converters
{
	public static class BinaryConverter
	{
		private const int BlockSize = 1024;
		private const int BlockAnd = 1023;
		private const int BlockShift = 10;

		private static readonly HashSet<Guid> Codecs = new HashSet<Guid>();

		static BinaryConverter()
		{
			foreach (var enc in ImageCodecInfo.GetImageEncoders())
				Codecs.Add(enc.FormatID);
		}
		public static void Serialize(byte[] value, StreamWriter sw)
		{
			if (value == null)
			{
				sw.Write("null");
			}
			else if (value.Length < 85000)
			{
				sw.Write('"');
				sw.Write(Convert.ToBase64String(value));
				sw.Write('"');
			}
			else
			{
				var tmpBuf = new byte[3];
				var base64 = new char[BlockSize * 4];
				var total = value.Length >> BlockShift;
				var remaining = value.Length & BlockAnd;
				int len;
				var off = 0;
				sw.Write('"');
				for (int i = 0; i < total; i++)
				{
					len = Convert.ToBase64CharArray(value, off + i * BlockSize, BlockSize - 2, base64, 0);
					sw.Write(base64, 0, len);
					for (int j = 0; j < 2 - off; j++)
						tmpBuf[j] = value[(i + 1) * BlockSize - 2 + j + off];
					for (int j = 0; j < 1 + off; j++)
						tmpBuf[2 - off + j] = value[(i + 1) * BlockSize + j];
					len = Convert.ToBase64CharArray(tmpBuf, 0, 3, base64, 0);
					sw.Write(base64, 0, len);
					off = (off + 1) & 3;
				}
				len = Convert.ToBase64CharArray(value, total * BlockSize + off, remaining != 0 ? remaining - off : BlockSize, base64, 0);
				sw.Write(base64, 0, len);
				sw.Write('"');
			}
		}

		public static void Serialize(Image value, StreamWriter sw)
		{
			if (value == null)
			{
				sw.Write("null");
			}
			else
			{
				sw.Write('"');
				using (var cms = ChunkedMemoryStream.Create())
				{
					if (Codecs.Contains(value.RawFormat.Guid))
						value.Save(cms, value.RawFormat);
					else
						value.Save(cms, ImageFormat.Png);
					cms.Position = 0;
					cms.ToBase64Writer(sw);
				}
				sw.Write('"');
			}
		}

		public static void Serialize(Stream value, StreamWriter sw)
		{
			if (value == null)
				sw.Write("null");
			else
			{
				sw.Write('"');
				var cms = value as ChunkedMemoryStream;
				if (cms != null) cms.ToBase64Writer(sw);
				else
				{
					using (cms = new ChunkedMemoryStream(value))
						cms.ToBase64Writer(sw);
				}
				sw.Write('"');
			}
		}

		private static readonly byte[] EmptyBytes = new byte[0];

		public static byte[] Deserialize(StreamReader sr, char[] buffer, int nextToken)
		{
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			nextToken = sr.Read();
			if (nextToken == '"') return EmptyBytes;
			var res = new List<byte[]>();
			var buf = new char[4096];
			int total = 0;
			int i = 0;
			while (nextToken != '"' && nextToken != -1)
			{
				buf[i + 0] = (char)nextToken;
				buf[i + 1] = (char)sr.Read();
				buf[i + 2] = (char)sr.Read();
				buf[i + 3] = (char)sr.Read();
				nextToken = sr.Read();
				i += 4;
				if (i == buf.Length)
				{
					var bytes = Convert.FromBase64CharArray(buf, 0, buf.Length);
					res.Add(bytes);
					i = 0;
					total += bytes.Length;
				}
			}
			if (i > 0)
			{
				var bytes = Convert.FromBase64CharArray(buf, 0, i);
				res.Add(bytes);
				total += bytes.Length;
			}
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found end of stream.");
			var result = new byte[total];
			var cur = 0;
			for (i = 0; i < res.Count; i++)
			{
				var item = res[i];
				for (int j = 0; j < item.Length; j++)
					result[cur++] = item[j];
			}
			return result;
		}

		public static List<byte[]> DeserializeCollection(StreamReader sr, char[] buffer, int nextToken)
		{
			return JsonSerialization.DeserializeCollection(sr, nextToken, next => Deserialize(sr, buffer, next));
		}

		public static List<byte[]> DeserializeNullableCollection(StreamReader sr, char[] buffer, int nextToken)
		{
			return JsonSerialization.DeserializeNullableCollection(sr, nextToken, next => Deserialize(sr, buffer, next));
		}

		public static Stream DeserializeStream(StreamReader sr, char[] buffer, int nextToken)
		{
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			nextToken = sr.Read();
			if (nextToken == '"') return new MemoryStream();
			var res = ChunkedMemoryStream.Create();
			var buf = new char[4096];
			int i = 0;
			while (nextToken != '"' && nextToken != -1)
			{
				buf[i + 0] = (char)nextToken;
				buf[i + 1] = (char)sr.Read();
				buf[i + 2] = (char)sr.Read();
				buf[i + 3] = (char)sr.Read();
				nextToken = sr.Read();
				i += 4;
				if (i == buf.Length)
				{
					var bytes = Convert.FromBase64CharArray(buf, 0, buf.Length);
					res.Write(bytes, 0, bytes.Length);
					i = 0;
				}
			}
			if (i > 0)
			{
				var bytes = Convert.FromBase64CharArray(buf, 0, i);
				res.Write(bytes, 0, bytes.Length);
			}
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found end of stream.");
			return res;
		}

		public static List<Stream> DeserializeStreamCollection(StreamReader sr, char[] buffer, int nextToken)
		{
			return JsonSerialization.DeserializeCollection(sr, nextToken, next => DeserializeStream(sr, buffer, next));
		}

		public static List<Stream> DeserializeStreamNullableCollection(StreamReader sr, char[] buffer, int nextToken)
		{
			return JsonSerialization.DeserializeNullableCollection(sr, nextToken, next => DeserializeStream(sr, buffer, next));
		}
	}
}
