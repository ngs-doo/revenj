using System;
using System.Collections.Generic;
using System.Drawing;
using System.IO;
using System.Runtime.Serialization;
using NGS.Utility;

namespace NGS.Serialization.Json.Converters
{
	public static class BinaryConverter
	{
		private const int BlockSize = 1024;

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
				var total = value.Length > BlockSize ? value.Length / BlockSize : 0;
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
				len = Convert.ToBase64CharArray(value, total * BlockSize + off, value.Length != BlockSize ? value.Length % BlockSize - off : BlockSize, base64, 0);
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
					value.Save(cms, value.RawFormat);
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

		public static byte[] Deserialize(StreamReader sr, char[] buffer, int nextToken)
		{
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			var res = new List<byte>();
			nextToken = sr.Read();
			while (nextToken != '"' && nextToken != -1)
			{
				buffer[0] = (char)nextToken;
				buffer[1] = (char)sr.Read();
				buffer[2] = (char)sr.Read();
				buffer[3] = (char)sr.Read();
				res.AddRange(Convert.FromBase64CharArray(buffer, 0, 4));
				nextToken = sr.Read();
			}
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found end of stream.");
			return res.ToArray();
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
			var res = ChunkedMemoryStream.Create();
			nextToken = sr.Read();
			while (nextToken != '"' && nextToken != -1)
			{
				buffer[0] = (char)nextToken;
				buffer[1] = (char)sr.Read();
				buffer[2] = (char)sr.Read();
				buffer[3] = (char)sr.Read();
				var bytes = Convert.FromBase64CharArray(buffer, 0, 4);
				res.Write(bytes, 0, bytes.Length);
				nextToken = sr.Read();
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
