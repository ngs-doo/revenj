using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;

namespace NGS.DatabasePersistence.Postgres.Converters
{
	public static class ImageConverter
	{
		public static Image FromDatabase(string value)
		{
			if (value == null)
				return null;
			var bytes = ByteaConverter.FromDatabase(value);
			var ms = new MemoryStream(bytes);
			return Image.FromStream(ms);
		}

		public static Image Parse(TextReader reader, int context)
		{
			var stream = ByteaConverter.ParseStream(reader, context);
			if (stream == null)
				return null;
			return Image.FromStream(stream);
		}

		public static List<Image> ParseCollection(TextReader reader, int context)
		{
			var list = ByteaConverter.ParseStreamCollection(reader, context, true);
			if (list == null)
				return null;
			var result = new List<Image>(list.Count);
			foreach (var stream in list)
				result.Add(stream != null ? Image.FromStream(stream) : null);
			return result;
		}

		public static string ToDatabase(Image value)
		{
			if (value == null)
				return null;
			using (var ms = new MemoryStream())
			{
				value.Save(ms, ImageFormat.Png);
				ms.Position = 0;
				return ByteaConverter.ToDatabase(ms.ToArray());
			}
		}

		public static PostgresTuple ToTuple(Image value)
		{
			if (value == null)
				return null;
			using (var ms = new MemoryStream())
			{
				value.Save(ms, ImageFormat.Png);
				return ByteaConverter.ToTuple(ms.ToArray());
			}
		}
	}
}