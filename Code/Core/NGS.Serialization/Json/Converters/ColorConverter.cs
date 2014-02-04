using System.Drawing;
using System.IO;

namespace NGS.Serialization.Json.Converters
{
	public static class ColorConverter
	{
		public static void Serialize(Color value, StreamWriter sw, char[] buffer)
		{
			sw.Write(value.ToArgb());
		}
		public static void Serialize(Color? value, StreamWriter sw, char[] buffer)
		{
			if (value == null)
				sw.Write("null");
			else
				sw.Write(value.Value.ToArgb());
		}
	}
}
