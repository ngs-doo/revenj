using System;
using System.Collections.Concurrent;
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
		private const int BlockSizeDiv3 = 32766;

		private static readonly HashSet<Guid> Codecs = new HashSet<Guid>();
		private static readonly ConcurrentBag<char[]> Buffers = new ConcurrentBag<char[]>();

		static BinaryConverter()
		{
			foreach (var enc in ImageCodecInfo.GetImageEncoders())
				Codecs.Add(enc.FormatID);
			for (int i = 0; i < Environment.ProcessorCount / 2 + 1; i++)
				Buffers.Add(new char[65536]);//TODO: use 65536/2 instead!?
		}

		public static void Serialize(byte[] value, TextWriter sw)
		{
			if (value == null)
			{
				sw.Write("null");
			}
			else if (value.Length == 0)
			{
				sw.Write("\"\"");
			}
			else
			{
				char[] base64;
				var took = Buffers.TryTake(out base64);
				if (!took) base64 = new char[65536];
				var total = value.Length / BlockSizeDiv3;
				var remaining = value.Length % BlockSizeDiv3;
				int len;
				sw.Write('"');
				for (int i = 0; i < total; i++)
				{
					len = Convert.ToBase64CharArray(value, i * BlockSizeDiv3, BlockSizeDiv3, base64, 0);
					sw.Write(base64, 0, len);
				}
				len = Convert.ToBase64CharArray(value, total * BlockSizeDiv3, remaining, base64, 0);
				sw.Write(base64, 0, len);
				sw.Write('"');
				Buffers.Add(base64);
			}
		}

		public static void Serialize(Image value, TextWriter sw)
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

		public static void Serialize(Stream value, TextWriter sw)
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

		public static byte[] Deserialize(BufferedTextReader sr, int nextToken)
		{
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			nextToken = sr.Read();
			if (nextToken == '"') return EmptyBytes;
			var base64 = sr.LargeTempBuffer;
			var res = new List<byte[]>();
			int total = 0;
			int i = 1;
			base64[0] = (char)nextToken;
			int len;
			while ((len = sr.ReadUntil(base64, i, '"')) > 0)
			{
				i += len;
				if (i == base64.Length)
				{
					var bytes = Convert.FromBase64CharArray(base64, 0, base64.Length);
					res.Add(bytes);
					i = 0;
					total += bytes.Length;
				}
			}
			nextToken = sr.Read();
			if (i > 0)
			{
				var bytes = Convert.FromBase64CharArray(base64, 0, i);
				res.Add(bytes);
				total += bytes.Length;
			}
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found end of stream.");
			var result = new byte[total];
			var cur = 0;
			for (i = 0; i < res.Count; i++)
			{
				var item = res[i];
				Array.Copy(item, 0, result, cur, item.Length);
				cur += item.Length;
			}
			return result;
		}

		public static List<byte[]> DeserializeCollection(BufferedTextReader sr, int nextToken)
		{
			return JsonSerialization.DeserializeCollection(sr, nextToken, next => Deserialize(sr, next));
		}

		public static void DeserializeCollection(BufferedTextReader sr, int nextToken, ICollection<byte[]> res)
		{
			JsonSerialization.DeserializeCollection(sr, nextToken, next => Deserialize(sr, next), res);
		}

		public static List<byte[]> DeserializeNullableCollection(BufferedTextReader sr, int nextToken)
		{
			return JsonSerialization.DeserializeNullableCollection(sr, nextToken, next => Deserialize(sr, next));
		}

		public static void DeserializeNullableCollection(BufferedTextReader sr, int nextToken, ICollection<byte[]> res)
		{
			JsonSerialization.DeserializeNullableCollection(sr, nextToken, next => Deserialize(sr, next), res);
		}

		public static Stream DeserializeStream(BufferedTextReader sr, int nextToken)
		{
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found " + (char)nextToken);
			nextToken = sr.Read();
			if (nextToken == '"') return new MemoryStream();
			//TODO: lazy init stream (more lightweight!?)
			var res = new ChunkedMemoryStream();
			var base64 = sr.LargeTempBuffer;
			int i = 1;
			base64[0] = (char)nextToken;
			int len;
			while ((len = sr.ReadUntil(base64, i, '"')) > 0)
			{
				i += len;
				if (i == base64.Length)
				{
					var bytes = Convert.FromBase64CharArray(base64, 0, base64.Length);
					res.Write(bytes, 0, bytes.Length);
					i = 0;
				}
			}
			nextToken = sr.Read();
			if (i > 0)
			{
				var bytes = Convert.FromBase64CharArray(base64, 0, i);
				res.Write(bytes, 0, bytes.Length);
			}
			if (nextToken != '"') throw new SerializationException("Expecting '\"' at position " + JsonSerialization.PositionInStream(sr) + ". Found end of stream.");
			return res;
		}

		public static List<Stream> DeserializeStreamCollection(BufferedTextReader sr, int nextToken)
		{
			return JsonSerialization.DeserializeCollection(sr, nextToken, next => DeserializeStream(sr, next));
		}

		public static void DeserializeStreamCollection(BufferedTextReader sr, int nextToken, ICollection<Stream> res)
		{
			JsonSerialization.DeserializeCollection(sr, nextToken, next => DeserializeStream(sr, next), res);
		}

		public static List<Stream> DeserializeStreamNullableCollection(BufferedTextReader sr, int nextToken)
		{
			return JsonSerialization.DeserializeNullableCollection(sr, nextToken, next => DeserializeStream(sr, next));
		}

		public static void DeserializeStreamNullableCollection(BufferedTextReader sr, int nextToken, ICollection<Stream> res)
		{
			JsonSerialization.DeserializeNullableCollection(sr, nextToken, next => DeserializeStream(sr, next), res);
		}

		public static Image DeserializeImage(BufferedTextReader sr, int nextToken)
		{
			return Image.FromStream(DeserializeStream(sr, nextToken));
		}

		public static List<Image> DeserializeImageCollection(BufferedTextReader sr, int nextToken)
		{
			return JsonSerialization.DeserializeCollection(sr, nextToken, next => DeserializeImage(sr, next));
		}

		public static void DeserializeImageCollection(BufferedTextReader sr, int nextToken, ICollection<Image> res)
		{
			JsonSerialization.DeserializeCollection(sr, nextToken, next => DeserializeImage(sr, next), res);
		}

		public static List<Image> DeserializeImageNullableCollection(BufferedTextReader sr, int nextToken)
		{
			return JsonSerialization.DeserializeNullableCollection(sr, nextToken, next => DeserializeImage(sr, next));
		}

		public static void DeserializeImageNullableCollection(BufferedTextReader sr, int nextToken, ICollection<Image> res)
		{
			JsonSerialization.DeserializeNullableCollection(sr, nextToken, next => DeserializeImage(sr, next), res);
		}
	}
}
