namespace Revenj.Utility
{
	/// <summary>
	/// Utility for calculating levenstein distance.
	/// </summary>
	public static class LevensteinDistance
	{
		/// <summary>
		/// Calculate levenstein distance between two strings.
		/// Levenstein distance is a measure for difference between strings
		/// </summary>
		/// <param name="left">first string</param>
		/// <param name="right">second string</param>
		/// <returns>levenstein distance</returns>
		public static int Calculate(string left, string right)
		{
			var n = left.Length;
			var m = right.Length;
			int cost;

			if (n == 0) return m;
			if (m == 0) return n;

			var v0 = new int[n + 1];
			var v1 = new int[n + 1];
			int[] tmp;

			for (int i = 1; i <= n; i++)
				v0[i] = i;

			for (int j = 1; j <= m; j++)
			{
				v1[0] = j;

				for (int i = 1; i <= n; i++)
				{
					cost = (left[i - 1] == right[j - 1]) ? 0 : 1;

					var m_min = v0[i] + 1;
					var b = v1[i - 1] + 1;
					var c = v0[i - 1] + cost;

					if (b < m_min) m_min = b;
					if (c < m_min) m_min = c;

					v1[i] = m_min;
				}

				tmp = v0;
				v0 = v1;
				v1 = tmp;
			}

			return v0[n];
		}
	}
}
