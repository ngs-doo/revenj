using System;
using System.IO;

namespace NGS.Serialization.Json.Converters
{
	public static class GuidConverter
	{
		static readonly char[] Lookup = InitLookup();
		private static char[] InitLookup()
		{
			var lookup = new char[257];
			var hexLookup = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
			for (int i = 0; i < hexLookup.Length; i++)
			{
				for (int j = 0; j < hexLookup.Length; j++)
				{
					lookup[i * hexLookup.Length + j] = lookup[i];
					lookup[i * hexLookup.Length + j + 1] = lookup[j];
				}
			}
			return lookup;
		}

		public static void Serialize(Guid value, StreamWriter sw, char[] buffer)
		{
			var bytes = value.ToByteArray();
			var b = bytes[3] * 2;
			buffer[0] = Lookup[b];
			buffer[1] = Lookup[b + 1];
			b = bytes[2] * 2;
			buffer[2] = Lookup[b];
			buffer[3] = Lookup[b + 1];
			b = bytes[1] * 2;
			buffer[4] = Lookup[b];
			buffer[5] = Lookup[b + 1];
			b = bytes[0] * 2;
			buffer[6] = Lookup[b];
			buffer[7] = Lookup[b + 1];
			buffer[8] = '-';
			b = bytes[5] * 2;
			buffer[9] = Lookup[b];
			buffer[10] = Lookup[b + 1];
			b = bytes[4] * 2;
			buffer[11] = Lookup[b];
			buffer[12] = Lookup[b + 1];
			buffer[13] = '-';
			b = bytes[7] * 2;
			buffer[14] = Lookup[b];
			buffer[15] = Lookup[b + 1];
			b = bytes[6] * 2;
			buffer[16] = Lookup[b];
			buffer[17] = Lookup[b + 1];
			buffer[18] = '-';
			b = bytes[8] * 2;
			buffer[19] = Lookup[b];
			buffer[20] = Lookup[b + 1];
			b = bytes[9] * 2;
			buffer[21] = Lookup[b];
			buffer[22] = Lookup[b + 1];
			buffer[23] = '-';
			b = bytes[10] * 2;
			buffer[24] = Lookup[b];
			buffer[25] = Lookup[b + 1];
			b = bytes[11] * 2;
			buffer[26] = Lookup[b];
			buffer[27] = Lookup[b + 1];
			b = bytes[12] * 2;
			buffer[28] = Lookup[b];
			buffer[29] = Lookup[b + 1];
			b = bytes[13] * 2;
			buffer[30] = Lookup[b];
			buffer[31] = Lookup[b + 1];
			b = bytes[14] * 2;
			buffer[32] = Lookup[b];
			buffer[33] = Lookup[b + 1];
			b = bytes[15] * 2;
			buffer[34] = Lookup[b];
			buffer[35] = Lookup[b + 1];
			sw.Write('"');
			sw.Write(buffer, 0, 36);
			sw.Write('"');
		}
		public static void Serialize(Guid? value, StreamWriter sw, char[] buffer)
		{
			if (value == null)
				sw.Write("null");
			else Serialize(value.Value, sw, buffer);
		}
	}
}
