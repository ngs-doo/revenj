using System.IO;
using System.Runtime.Serialization;
using ProtoBuf.Meta;

namespace NGS.Serialization
{
	internal class ProtobufSerialization
	{
		private readonly RuntimeTypeModel Model;

		public ProtobufSerialization()
		{
			Model = RuntimeTypeModel.Create();
			Model.InferTagFromNameDefault = true;
			Model.Add(typeof(MemoryStream), false).SetSurrogate(typeof(ProtoMemoryStream));
		}

		public MemoryStream Serialize<T>(T value)
		{
			var ms = new MemoryStream();
			Model.Serialize(ms, value);
			ms.Position = 0;
			return ms;
		}

		public T Deserialize<T>(MemoryStream data, StreamingContext context)
		{
			data.Position = 0;
			return (T)Model.Deserialize(data, null, typeof(T), context);
		}

		public T Deserialize<T>(Stream data, StreamingContext context)
		{
			return (T)Model.Deserialize(data, null, typeof(T), context);
		}
	}
}
