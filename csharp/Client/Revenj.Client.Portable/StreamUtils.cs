using System.IO;

namespace Revenj
{
	internal static class StreamUtils
	{
		public static void CopyTo(this Stream source, Stream destination)
		{
			var buffer = new byte[8096];
			int offset = 0;
			int len;
			while ((len = source.Read(buffer, offset, buffer.Length)) != -1)
			{
				destination.Write(buffer, 0, len);
				offset += len;
			}
		}
	}
}
