using System;
using System.IO;

namespace NGS.Serialization
{
	public interface IJsonObject
	{
		void Serialize(StreamWriter sw, Action<StreamWriter, object> serializer);
	}
}
