using System;
using System.IO;
using System.Text;

namespace NGS.Utility
{
	/// <summary>
	/// Utilities for working with large objects to avoid LOH.
	/// </summary>
	public static class StreamOperations
	{
		/// <summary>
		/// Convert StringBuilder to Stream.
		/// Depending on the size, appropriate stream will be used.
		/// </summary>
		/// <param name="sb">string builder</param>
		/// <returns>utf8 stream</returns>
		public static Stream ToStream(this StringBuilder sb)
		{
			if (sb.Length < 8192)
				return new MemoryStream(Encoding.UTF8.GetBytes(sb.ToString()));
			var cms = ChunkedMemoryStream.Create();
			var sw = new StreamWriter(cms);
			sw.WriteBuilder(sb);
			sw.Flush();
			cms.Position = 0;
			return cms;
		}
		/// <summary>
		/// Append StringBuilder to stream.
		/// Iterate through StringBuilder to avoid LOH issues.
		/// </summary>
		/// <param name="sw">stream writer</param>
		/// <param name="sb">string builder</param>
		public static void WriteBuilder(this StreamWriter sw, StringBuilder sb)
		{
			var len = sb.Length / 4096;
			var pos = 0;
			for (int i = 0; i < len; i++, pos += 4096)
				sw.Write(sb.ToString(pos, 4096));
			if (sb.Length > pos)
				sw.Write(sb.ToString(pos, sb.Length - pos));
		}
		/// <summary>
		/// Convert stream to base64 encoded stream.
		/// </summary>
		/// <param name="stream">existing stream</param>
		/// <returns>base64 encoded stream</returns>
		public static Stream ToBase64Stream(this Stream stream)
		{
			var cms = stream as ChunkedMemoryStream ?? new ChunkedMemoryStream(stream);
			return cms.ToBase64Stream();
		}
		/// <summary>
		/// Append two string builders.
		/// This actualy doesn't avoid LOH issues.
		/// </summary>
		/// <param name="sb">original string builder</param>
		/// <param name="append">append additional builder</param>
		/// <returns>original string builder</returns>
		[Obsolete("Use Stream instead of StringBuilder")]
		public static StringBuilder AppendBuilder(this StringBuilder sb, StringBuilder append)
		{
			var size = append.Length;
			int pos = 0;
			var buf = new char[4096];
			while (size > 0)
			{
				int len = size > 4096 ? 4096 : size;
				append.CopyTo(pos, buf, 0, len);
				sb.Append(buf, 0, len);
				pos += 4096;
				size -= 4096;
			}
			return sb;
		}
		/// <summary>
		/// Convert string builder to base64 stream.
		/// Depending on the size, appropriate method will be used to avoid LOH issues.
		/// </summary>
		/// <param name="sb">string builder</param>
		/// <returns>base64 encoded stream</returns>
		public static Stream ToBase64Stream(this StringBuilder sb)
		{
			if (sb.Length < 8192)
				return new MemoryStream(Encoding.UTF8.GetBytes(Convert.ToBase64String(Encoding.UTF8.GetBytes(sb.ToString()))));
			using (var cms = ChunkedMemoryStream.Create())
			using (var sw = new StreamWriter(cms))
			{
				var len = sb.Length / 4096;
				var pos = 0;
				for (int i = 0; i < len; i++, pos += 4096)
					sw.Write(sb.ToString(pos, 4096));
				if (sb.Length > pos)
					sw.Write(sb.ToString(pos, sb.Length - pos));
				sw.Flush();
				cms.Position = 0;
				return cms.ToBase64Stream();
			}
		}
	}
}
