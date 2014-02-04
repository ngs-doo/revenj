using System.IO;

namespace NGS.Serialization.Json.Converters
{
	public static class StringConverter
	{
		private static char ToHex(int n)
		{
			if (n <= 9)
				return (char)(n + 48);
			return (char)((n - 10) + 97);
		}

		public static void SerializeNullable(string value, StreamWriter sw)
		{
			if (value == null)
				sw.Write("null");
			else
				Serialize(value, sw);
		}

		public static void Serialize(string value, StreamWriter sw)
		{
			sw.Write('"');
			foreach (var c in value)
			{
				switch (c)
				{
					case '\t':
						sw.Write("\\t");
						break;
					case '\n':
						sw.Write("\\n");
						break;
					case '\r':
						sw.Write("\\r");
						break;
					case '\f':
						sw.Write("\\f");
						break;
					case '\b':
						sw.Write("\\b");
						break;
					case '"':
						sw.Write("\\\"");
						break;
					case '\\':
						sw.Write("\\\\");
						break;
					case '/':
						sw.Write("\\/");
						break;
					default:
						if (c >= ' ' && c < 128)
							sw.Write(c);
						else
						{
							sw.Write('\\');
							sw.Write('u');
							sw.Write(ToHex((c >> 12) & '\x000f'));
							sw.Write(ToHex((c >> 8) & '\x000f'));
							sw.Write(ToHex((c >> 4) & '\x000f'));
							sw.Write(ToHex(c & '\x000f'));
						}
						break;
				}
			}
			sw.Write('"');
		}
	}
}
