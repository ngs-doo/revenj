namespace Revenj.DatabasePersistence.Postgres.Converters
{
	internal static class NumberConverter
	{
		public static int? TryParsePositiveInt(string number)
		{
			if (number.Length == 0)
				return null;
			int value = 0;
			for (int i = 0; i < number.Length; i++)
				value = (value << 3) + (value << 2) + number[i] - '0';
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
