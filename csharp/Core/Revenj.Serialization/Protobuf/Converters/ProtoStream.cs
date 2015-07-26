using System.IO;
using ProtoBuf;

namespace Revenj.Serialization
{
	[ProtoContract]
	internal class ProtoStream
	{
		[ProtoMember(1)]
		public byte[] Data { get; set; }

		public static implicit operator Stream(ProtoStream value)
		{
			return value != null ? new MemoryStream(value.Data) : null;
		}

		public static implicit operator ProtoStream(Stream value)
		{
			if (value == null)
				return null;

			using (var ms = new MemoryStream())
			{
				value.CopyTo(ms);
				return new ProtoStream { Data = ms.ToArray() };
			}
		}
	}
}
