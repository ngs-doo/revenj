namespace Revenj.DomainPatterns
{
	public static class NumberCounter<T>
	{
		private static int CurrentTempInt;
		private static long CurrentTempLong;
		private static int CurrentInt;
		private static long CurrentLong;

		public static int GetNextTempInt()
		{
			return --CurrentTempInt;
		}
		public static long GetNextTempLong()
		{
			return --CurrentTempLong;
		}
		public static int GetNextInt()
		{
			return ++CurrentInt;
		}
		public static long GetNextLong()
		{
			return ++CurrentLong;
		}
	}
}
