using System;

namespace Revenj.Utility
{
	public static class StableHashCode
	{
		public static string HashString(this string text)
		{
			int hash = 23;
			if (text != null)
			{
				foreach (var c in text)
					hash = hash * 31 + c;
			}
			return Math.Abs(hash).ToString();
		}
	}
}
