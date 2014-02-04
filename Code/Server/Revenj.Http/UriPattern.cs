using System.Collections.Generic;
using System.Text.RegularExpressions;

namespace Revenj.Http
{
	internal class UriPattern
	{
		private static Regex EscapePattern = new Regex(@"\{[^{}]+\}", RegexOptions.Compiled);
		private static Regex RegexMetaPattern = new Regex(@"[\\\[\]\(\)\&\^\$\?\#\+\*\|\>\<]", RegexOptions.Compiled);
		private static string RegexMetaCharactersReplacements = @"\$0";
		private static string TokenReplacement = "([^/?#]*)?";

		private Regex TemplatePattern;
		private string[] Tokens;

		public UriPattern(string template)
		{
			Tokens = GetTokens(template);
			var finalPattern = BuildRegex(template);
			TemplatePattern = new Regex(finalPattern, RegexOptions.Compiled);
		}

		public Dictionary<string, string> Parse(string instance)
		{
			var dict = new Dictionary<string, string>();

			foreach (Match match in TemplatePattern.Matches(instance))
			{
				int tokenIndex = 0;
				foreach (Group group in match.Groups)
				{
					if (tokenIndex > 0 && group.Success)
						dict.Add(Tokens[tokenIndex - 1], group.Value);

					tokenIndex++;
				}
			}

			return dict;
		}

		private string BuildRegex(string template)
		{
			template = RegexMetaPattern.Replace(template, RegexMetaCharactersReplacements) + ".*";
			return EscapePattern.Replace(template, TokenReplacement);
		}

		private string[] GetTokens(string template)
		{
			var tokens = new List<string>();

			foreach (Match match in EscapePattern.Matches(template))
			{
				var token = match.Value;
				token = token.Substring(1, token.Length - 2);

				if (!tokens.Contains(token))
					tokens.Add(token);
			}

			return tokens.ToArray();
		}
	}
}