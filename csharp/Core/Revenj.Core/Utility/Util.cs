using System;
using System.IO;
using System.Linq;
using System.Text.RegularExpressions;

namespace Revenj.Core.Utility
{
	public static class Util
	{
		public static bool FilenameMatch(string filePath, string commaSeparatedWildcards)
		{
			if (string.IsNullOrWhiteSpace(filePath))
				throw new ArgumentNullException(nameof(filePath));

			var filename = Path.GetFileName(filePath).ToLower();
			return commaSeparatedWildcards != null && commaSeparatedWildcards.ToLower().Split(',')
						.Any(wildcard => Regex.IsMatch(filename, WildcardToRegExPattern(wildcard)));
		}

		public static string WildcardToRegExPattern(string value)
		{
			return "^" + Regex.Escape(value).Replace("\\?", ".").Replace("\\*", ".*") + "$";
		}
	}
}