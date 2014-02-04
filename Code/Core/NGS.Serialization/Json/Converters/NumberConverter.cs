using System.Globalization;
using System.IO;

namespace NGS.Serialization.Json.Converters
{
	public static class NumberConverter
	{
		public static void Serialize(decimal value, StreamWriter sw, char[] buffer)
		{
			sw.Write(value.ToString(CultureInfo.InvariantCulture));
		}
		public static void Serialize(decimal? value, StreamWriter sw, char[] buffer)
		{
			if (value == null)
				sw.Write("null");
			else
				sw.Write(value.Value.ToString(CultureInfo.InvariantCulture));
		}

		public static void Serialize(int value, StreamWriter sw, char[] buffer)
		{
			sw.Write(value);
		}
		public static void Serialize(int? value, StreamWriter sw, char[] buffer)
		{
			if (value == null)
				sw.Write("null");
			else
				sw.Write(value.Value);
		}

		public static void Serialize(long value, StreamWriter sw, char[] buffer)
		{
			sw.Write(value);
		}
		public static void Serialize(long? value, StreamWriter sw, char[] buffer)
		{
			if (value == null)
				sw.Write("null");
			else
				sw.Write(value.Value);
		}

		public static void Serialize(double value, StreamWriter sw, char[] buffer)
		{
			sw.Write(value.ToString(CultureInfo.InvariantCulture));
		}
		public static void Serialize(double? value, StreamWriter sw, char[] buffer)
		{
			if (value == null)
				sw.Write("null");
			else
				sw.Write(value.Value.ToString(CultureInfo.InvariantCulture));
		}

		public static void Serialize(float value, StreamWriter sw, char[] buffer)
		{
			sw.Write(value.ToString(CultureInfo.InvariantCulture));
		}
		public static void Serialize(float? value, StreamWriter sw, char[] buffer)
		{
			if (value == null)
				sw.Write("null");
			else
				sw.Write(value.Value.ToString(CultureInfo.InvariantCulture));
		}
	}
}
