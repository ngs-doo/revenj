using System.IO;
using ProtoBuf;

namespace NGS.Serialization
{
	[ProtoContract]
	internal class ProtoMemoryStream
	{
		[ProtoMember(1)]
		public byte[] Data { get; set; }

		public static implicit operator MemoryStream(ProtoMemoryStream value)
		{
			return value != null ? new MemoryStream(value.Data) : null;
		}

		public static implicit operator ProtoMemoryStream(MemoryStream value)
		{
			return value != null ? new ProtoMemoryStream { Data = value.ToArray() } : null;
		}
	}
}
