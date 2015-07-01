using System;
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
}
