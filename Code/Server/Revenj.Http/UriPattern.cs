using System.Collections.Generic;
using System.Text.RegularExpressions;
using System.Web;

namespace Revenj.Http
{
	internal class UriPattern
	{
		private static Regex EscapePattern = new Regex(@"\{[^{}]+\}", RegexOptions.Compiled);
		private static Regex RegexMetaPattern = new Regex(@"[\\\[\]\(\)\&\^\$\?\#\+\*\|\>\<]", RegexOptions.Compiled);
		private static string RegexMetaCharactersReplacements = @"\$0";
		private static string PathGroup = "([^/?#]*)";

		private readonly Regex TemplatePattern;
		private readonly string[] Tokens;
		private readonly HashSet<string> TokenSet;
		private readonly int TokensCount;
		public readonly int Groups;

		public UriPattern(string template)
		{
			template = template.TrimEnd('*').ToUpperInvariant();
			Tokens = GetTokens(template);
			TokenSet = new HashSet<string>(Tokens);
			TokensCount = Tokens.Length;
			var segments = BuildRegex(template);
			var finalPattern = EscapePattern.Replace(segments, PathGroup);
			TemplatePattern = new Regex(finalPattern, RegexOptions.Compiled | RegexOptions.IgnoreCase);
			Groups = TemplatePattern.GetGroupNumbers().Length;
		}

		public RouteMatch Match(string url, int offset)
		{
			if (!TemplatePattern.IsMatch(url, offset))
				return null;
			return ExtractMatch(url, offset);
		}

		private static Dictionary<string, string> Empty = new Dictionary<string, string>(0);

		public RouteMatch ExtractMatch(string url, int offset)
		{
			var match = TemplatePattern.Match(url, offset);
			var boundVars = new Dictionary<string, string>(TokensCount);
			var groups = match.Groups;
			for (int i = 1; i < groups.Count; i++)
				boundVars.Add(Tokens[i - 1], groups[i].Value);
			if (groups.Count == TokensCount + 1)
				return new RouteMatch(boundVars, url);
			int pos = url.IndexOf('?');
			if (pos == -1)
				return new RouteMatch(boundVars, Empty, url);
			var queryParams = new Dictionary<string, string>();
			pos++;
			while (pos < url.Length)
			{
				int start = pos;
				while (pos < url.Length && url[pos] != '=') pos++;
				var key = HttpUtility.UrlDecode(url.Substring(start, pos - start));
				pos++;
				start = pos;
				while (pos < url.Length && url[pos] != '&') pos++;
				var value = HttpUtility.UrlDecode(url.Substring(start, pos - start));
				pos++;
				var upper = key.ToUpperInvariant();
				if (TokenSet.Contains(upper))
					boundVars.Add(upper, value);
				queryParams.Add(key, value);
			}
			return new RouteMatch(boundVars, queryParams, url);
		}

		private string BuildRegex(string template)
		{
			var iof = template.IndexOf('?');
			if (iof != -1)
				return RegexMetaPattern.Replace(template.Substring(0, iof), RegexMetaCharactersReplacements);
			return RegexMetaPattern.Replace(template, RegexMetaCharactersReplacements) + ".*";
		}

		private string[] GetTokens(string template)
		{
			var tokens = new List<string>();
			foreach (Match match in EscapePattern.Matches(template))
			{
				var token = match.Value;
				token = token.Substring(1, token.Length - 2).TrimStart('*');
				if (!tokens.Contains(token))
					tokens.Add(token);
			}
			return tokens.ToArray();
		}
	}
}