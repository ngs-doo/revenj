using System.IO;

namespace NGS.Serialization.Json.Converters
{
	public static class BoolConverter
	{
		public static void Serialize(bool value, StreamWriter sw)
		{
			if (value)
				sw.Write("true");
			else
				sw.Write("false");
		}
		public static void Serialize(bool? value, StreamWriter sw)
		{
			if (value == null)
				sw.Write("null");
			else if (value == true)
				sw.Write("true");
			else
				sw.Write("false");
		}
	}
}
