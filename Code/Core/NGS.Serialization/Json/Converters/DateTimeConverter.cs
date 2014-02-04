using System;
using System.IO;

namespace NGS.Serialization.Json.Converters
{
	public static class DateTimeConverter
	{
		public static void SerializeDate(DateTime value, StreamWriter sw, char[] buffer)
		{
			buffer[0] = '"';
			var n = value.Year;
			var i = n % 10;
			buffer[4] = (char)('0' + i);
			n = n / 10;
			i = n % 10;
			buffer[3] = (char)('0' + i);
			n = n / 10;
			i = n % 10;
			buffer[2] = (char)('0' + i);
			n = n / 10;
			i = n % 10;
			buffer[1] = (char)('0' + i);
			buffer[5] = '-';
			n = value.Month;
			buffer[7] = (char)('0' + n % 10);
			buffer[6] = (char)('0' + n / 10);
			buffer[8] = '-';
			n = value.Day;
			buffer[10] = (char)('0' + n % 10);
			buffer[9] = (char)('0' + n / 10);
			buffer[11] = '"';
			sw.Write(buffer, 0, 12);
		}
		public static void SerializeDate(DateTime? value, StreamWriter sw, char[] buffer)
		{
			if (value == null)
				sw.Write("null");
			else
				SerializeDate(value.Value, sw, buffer);
		}
		public static void Serialize(DateTime value, StreamWriter sw, char[] buffer)
		{
			sw.Write('"');
			sw.Write(value.ToString("yyyy-MM-dd HH:mm:ss.FFFFFFFK"));
			sw.Write('"');
		}
		public static void Serialize(DateTime? value, StreamWriter sw, char[] buffer)
		{
			if (value == null)
				sw.Write("null");
			else Serialize(value.Value, sw, buffer);
		}
	}
}
