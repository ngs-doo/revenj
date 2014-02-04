using System;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;

namespace NGS.Serialization.Json.Converters
{
	public static class BinaryConverter
	{
		private const int BlockSize = 8192;

		public static void Serialize(byte[] value, StreamWriter sw)
		{
			if (value == null)
				sw.Write("null");
			else
			{
				var tmpBuf = new byte[3];
				var base64 = new char[BlockSize * 4];
				var total = value.Length > BlockSize ? value.Length / BlockSize : 0;
				int len;
				var off = 0;
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
			}
		}

		public static void Serialize(Image value, StreamWriter sw)
		{
			if (value == null)
				sw.Write("null");
			else
			{
				using (var ms = new MemoryStream())
				{
					value.Save(ms, ImageFormat.Png);
					Serialize(ms.ToArray(), sw);
				}
			}
		}
	}
}
