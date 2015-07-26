using System.Globalization;

namespace Revenj.DatabasePersistence.Postgres.Converters
{
	internal static class NumberConverter
	{
		private static readonly CultureInfo Invariant = CultureInfo.InvariantCulture;

		internal struct Pair
		{
			public readonly char First;
			public readonly char Second;
			public readonly byte Offset;
			public Pair(int number)
			{
				First = (char)((number / 10) + '0');
				Second = (char)((number % 10) + '0');
				Offset = number < 10 ? (byte)1 : (byte)0;
			}
		}
		internal static readonly Pair[] Numbers;

		static NumberConverter()
		{
			Numbers = new Pair[100];
			for (int i = 0; i < Numbers.Length; i++)
				Numbers[i] = new Pair(i);
		}

		internal static void Write2(int number, char[] buffer, int start)
		{
			var pair = Numbers[number];
			buffer[start] = pair.First;
			buffer[start + 1] = pair.Second;
		}

		internal static void Write4(int number, char[] buffer, int start)
		{
			var div = number / 100;
			var pair1 = Numbers[div];
			buffer[start] = pair1.First;
			buffer[start + 1] = pair1.Second;
			var rem = number - div * 100;
			var pair2 = Numbers[rem];
			buffer[start + 2] = pair2.First;
			buffer[start + 3] = pair2.Second;
		}

		internal static int Read2(char[] source, int start)
		{
			int first = source[start] - 48;
			return (first << 3) + (first << 1) + source[start + 1] - 48;
		}

		internal static int Read4(char[] source, int start)
		{
			int first = source[start] - 48;
			var second = source[start + 1] - 48;
			var third = source[start + 2] - 48;
			return first * 1000 + second * 100 + (third << 3) + (third << 1) + source[start + 3] - 48;
		}

		public static int? TryParsePositiveInt(string number)
		{
			if (number.Length == 0 || number[0] < '0' || number[0] > '9')
				return null;
			int value = 0;
			for (int i = 0; i < number.Length; i++)
				value = (value << 3) + (value << 1) + number[i] - '0';
			return value;
		}

		public static long ParseLong(string number)
		{
			long value = 0;
			if (number[0] == '-')
			{
				for (int i = 1; i < number.Length; i++)
					value = (value << 3) + (value << 2) - number[i] + '0';
			}
			else
			{
				for (int i = 0; i < number.Length; i++)
					value = (value << 3) + (value << 2) + number[i] - '0';
			}
			return value;
		}
	}
}
