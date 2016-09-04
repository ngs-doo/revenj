using System;
using Newtonsoft.Json;

namespace Revenj.Serialization
{
	internal class TreePathConverter : JsonConverter
	{
		public override bool CanConvert(Type objectType)
		{
			return objectType == typeof(TreePath) || objectType == typeof(TreePath?);
		}

		public override object ReadJson(JsonReader reader, Type objectType, object existingValue, JsonSerializer serializer)
		{
			return new TreePath(reader.Value as string);
		}

		public override void WriteJson(JsonWriter writer, object value, JsonSerializer serializer)
		{
			var tr = (TreePath)value;
			writer.WriteValue(tr.ToString());
		}
	}
}
