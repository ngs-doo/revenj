using System;
using System.Collections.Generic;
using System.IO;
using System.Runtime.Serialization;
using Revenj.Utility;

namespace Revenj.Serialization
{
	public interface IJsonObject
	{
		void Serialize(TextWriter sw, bool minimal, Action<TextWriter, object> serializer);
		object Deserialize(BufferedTextReader sr, StreamingContext context, Func<TextReader, Type, object> serializer);
	}

	public static class JsonObjectHelper
	{
		public static void Serialize(this IJsonObject instance, ChunkedMemoryStream stream)
		{
			stream.Reset();
			var sw = stream.GetWriter();
			instance.Serialize(sw, false, null);
			sw.Flush();
			stream.Position = 0;
		}
		public static void Serialize(this IJsonObject[] array, ChunkedMemoryStream stream)
		{
			stream.Reset();
			var sw = stream.GetWriter();
			sw.Write('[');
			if (array.Length > 0)
			{
				array[0].Serialize(sw, false, null);
				for (int i = 1; i < array.Length; i++)
				{
					sw.Write(',');
					array[i].Serialize(sw, false, null);
				}
			}
			sw.Write(']');
			sw.Flush();
			stream.Position = 0;
		}
		public static void Serialize(this IJsonObject[] array, ChunkedMemoryStream stream, int len)
		{
			stream.Reset();
			var sw = stream.GetWriter();
			sw.Write('[');
			if (len > 0)
			{
				array[0].Serialize(sw, false, null);
				for (int i = 1; i < len; i++)
				{
					sw.Write(',');
					array[i].Serialize(sw, false, null);
				}
			}
			sw.Write(']');
			sw.Flush();
			stream.Position = 0;
		}
		public static void Serialize<T>(this ArraySegment<T> segment, ChunkedMemoryStream stream)
			where T : IJsonObject
		{
			stream.Reset();
			var sw = stream.GetWriter();
			sw.Write('[');
			if (segment.Count > 0)
			{
				var array = segment.Array;
				var off = segment.Offset;
				array[off].Serialize(sw, false, null);
				for (int i = 1; i < segment.Count; i++)
				{
					sw.Write(',');
					array[off + i].Serialize(sw, false, null);
				}
			}
			sw.Write(']');
			sw.Flush();
			stream.Position = 0;
		}
		public static void Serialize<T>(this List<T> values, ChunkedMemoryStream stream) where T : IJsonObject
		{
			stream.Reset();
			var sw = stream.GetWriter();
			sw.Write('[');
			if (values.Count > 0)
			{
				values[0].Serialize(sw, false, null);
				for (int i = 1; i < values.Count; i++)
				{
					sw.Write(',');
					values[i].Serialize(sw, false, null);
				}
			}
			sw.Write(']');
			sw.Flush();
			stream.Position = 0;
		}
	}
}
