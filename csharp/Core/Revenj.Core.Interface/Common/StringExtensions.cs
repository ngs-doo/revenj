using System.Globalization;
using System.Linq;

namespace Revenj
{
	/// <summary>
	/// Helper methods for working with strings.
	/// Instead of string.Format("....", ...) write "....".With(...)
	/// </summary>
	public static class StringExtensions
	{
		private static readonly CultureInfo Invariant = CultureInfo.InvariantCulture;

		/// <summary>
		/// Single argument helper method.
		/// "print {0} now".With(arg)
		/// </summary>
		/// <param name="value">template string</param>
		/// <param name="arg">formatting argument</param>
		/// <returns>formatted string</returns>
		public static string With(this string value, object arg)
		{
			return string.Format(Invariant, value, arg ?? "<null>");
		}

		/// <summary>
		/// Double argument helper method.
		/// "print {0} {1}".With(arg1, arg2)
		/// </summary>
		/// <param name="value">template string</param>
		/// <param name="arg1">first formatting argument</param>
		/// <param name="arg2">second formatting argument</param>
		/// <returns>formatted string</returns>
		public static string With(this string value, object arg1, object arg2)
		{
			return string.Format(Invariant, value, arg1 ?? "<null>", arg2 ?? "<null>");
		}

		/// <summary>
		/// Triple argument helper method.
		/// "print {0}, {1} and {2}".With(arg1, arg2, arg3)
		/// </summary>
		/// <param name="value">template string</param>
		/// <param name="arg1">first formatting argument</param>
		/// <param name="arg2">second formatting argument</param>
		/// <param name="arg3">third formatting argument</param>
		/// <returns>formatted string</returns>
		public static string With(this string value, object arg1, object arg2, object arg3)
		{
			return string.Format(Invariant, value, arg1 ?? "<null>", arg2 ?? "<null>", arg3 ?? "<null>");
		}

		/// <summary>
		/// Generic helper method. For unlimited number of arguments
		/// "print {0} ... {n}".With(arg1, ... argN)
		/// </summary>
		/// <param name="value">template string</param>
		/// <param name="args">formatting arguments</param>
		/// <returns>formatted string</returns>
		public static string With(this string value, params object[] args)
		{
			var formattedArgs = args.Select(it => it != null ? it : "<null>").ToArray();
			return string.Format(Invariant, value, formattedArgs);
		}
	}
}
