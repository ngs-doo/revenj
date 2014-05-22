using System;
using System.IO;
using System.Runtime.Serialization;

namespace NGS.Serialization
{
	public interface IJsonObject
	{
		void Serialize(StreamWriter sw, Action<StreamWriter, object> serializer);
		object Deserialize(StreamReader sr, StreamingContext context, Func<StreamReader, Type, object> serializer);
	}
}
