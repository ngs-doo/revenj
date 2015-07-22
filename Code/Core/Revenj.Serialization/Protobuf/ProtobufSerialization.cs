using System;
using System.IO;
using System.Runtime.Serialization;
using ProtoBuf.Meta;
using Revenj.Utility;

namespace Revenj.Serialization
{
	internal class ProtobufSerialization : ISerialization<MemoryStream>, ISerialization<Stream>
	{
		private readonly RuntimeTypeModel Model;

		public ProtobufSerialization()
		{
			Model = RuntimeTypeModel.Create();
			Model.InferTagFromNameDefault = true;
			Model.Add(typeof(MemoryStream), false).SetSurrogate(typeof(ProtoMemoryStream));
			Model.Add(typeof(Stream), false).SetSurrogate(typeof(ProtoStream));
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

		Stream ISerialization<Stream>.Serialize<T>(T value)
		{
			var cs = ChunkedMemoryStream.Create();
			Model.Serialize(cs, value);
			cs.Position = 0;
			return cs;
		}

		public T Deserialize<T>(Stream data, StreamingContext context)
		{
			return (T)Model.Deserialize(data, null, typeof(T), context);
		}

		public void Serialize(object value, Stream s)
		{
			Model.Serialize(s, value);
		}

		public object Deserialize(Stream s, Type target, StreamingContext context)
		{
			return Model.Deserialize(s, null, target, context);
		}
	}
}
