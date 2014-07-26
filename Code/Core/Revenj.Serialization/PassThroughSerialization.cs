using System.Runtime.Serialization;

namespace Revenj.Serialization
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
