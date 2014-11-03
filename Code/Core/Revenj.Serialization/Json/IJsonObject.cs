using System;
using System.IO;
using System.Runtime.Serialization;

namespace Revenj.Serialization
{
	public interface IJsonObject
	{
		void Serialize(TextWriter sw, bool minimal, Action<TextWriter, object> serializer);
		object Deserialize(TextReader sr, StreamingContext context, Func<TextReader, Type, object> serializer);
	}
}
