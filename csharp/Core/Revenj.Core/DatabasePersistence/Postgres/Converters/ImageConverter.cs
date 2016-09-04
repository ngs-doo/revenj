using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	public static class ImageConverter
	{
		private static readonly HashSet<Guid> Codecs = new HashSet<Guid>();

		static ImageConverter()
		{
			foreach (var enc in ImageCodecInfo.GetImageEncoders())
				Codecs.Add(enc.FormatID);
		}

		private static void SaveImage(Image image, Stream stream)
		{
			if (Codecs.Contains(image.RawFormat.Guid))
				image.Save(stream, image.RawFormat);
			else
				image.Save(stream, ImageFormat.Png);
		}

		public static Image FromDatabase(string value)
		{
			if (value == null)
				return null;
			var bytes = ByteaConverter.FromDatabase(value);
			var ms = new MemoryStream(bytes);
			return Image.FromStream(ms);
		}

		public static Image Parse(BufferedTextReader reader, int context)
		{
			var stream = ByteaConverter.ParseStream(reader, context);
			if (stream == null)
				return null;
			return Image.FromStream(stream);
		}

		public static List<Image> ParseCollection(BufferedTextReader reader, int context)
		{
			var list = ByteaConverter.ParseStreamCollection(reader, context, true);
			if (list == null)
				return null;
			var result = new List<Image>(list.Count);
			foreach (var stream in list)
				result.Add(stream != null ? Image.FromStream(stream) : null);
			return result;
		}

		public static int Serialize(Image value, char[] buf, int pos)
		{
			using (var ms = new MemoryStream())
			{
				SaveImage(value, ms);
				ms.Position = 0;
				return ByteaConverter.Serialize(ms.ToArray(), buf, pos);
			}
		}

		public static IPostgresTuple ToTuple(Image value)
		{
			if (value == null)
				return null;
			var cms = ChunkedMemoryStream.Create();
			SaveImage(value, cms);
			cms.Position = 0;
			return ByteaConverter.ToTuple(cms, true);
		}
	}
}