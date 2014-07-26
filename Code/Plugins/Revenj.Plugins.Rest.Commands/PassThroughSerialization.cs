using System.Runtime.Serialization;
using Revenj.Serialization;

namespace Revenj.Plugins.Rest.Commands
{
	internal class PassThroughSerialization : ISerialization<object>
	{
		public object Serialize<T>(T value) { return value; }
		public T Deserialize<T>(object data, StreamingContext context) { return (T)data; }
	}
}
