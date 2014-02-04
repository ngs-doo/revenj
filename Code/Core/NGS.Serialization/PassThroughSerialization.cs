using System.Runtime.Serialization;

namespace NGS.Serialization
{
	public class PassThroughSerialization : ISerialization<object>
	{
		public object Serialize<T>(T value) { return value; }

		public T Deserialize<T>(object data, StreamingContext context)
		{
			return (T)data;
		}
	}
}
