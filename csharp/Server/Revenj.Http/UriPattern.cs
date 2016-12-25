using System;
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

		private readonly string Template;
		private readonly Regex TemplatePattern;
		private readonly string[] Tokens;
		private readonly Dictionary<string, int> TokenMap;
		private readonly int TokensCount;
		public readonly int Groups;
		private readonly Func<string, int, RouteMatch> ExtractMatcher;
		internal readonly bool IsStatic;

		public UriPattern(string template)
		{
			this.Template = template.TrimEnd('*').ToUpperInvariant();
			Tokens = GetTokens(this.Template);
			TokenMap = new Dictionary<string, int>();
			for (int i = 0; i < Tokens.Length; i++)
				TokenMap[Tokens[i]] = i;
			TokensCount = Tokens.Length;
			IsStatic = !template.EndsWith("*") && TokensCount == 0 && !template.Contains("?") && !template.Contains("{");
			if (IsStatic)
				ExtractMatcher = StaticExtractMatch;
			else
				ExtractMatcher = DynamicExtractMatch;
			var segments = BuildRegex(this.Template);
			var finalPattern = EscapePattern.Replace(segments, PathGroup);
			TemplatePattern = new Regex(finalPattern, RegexOptions.Compiled | RegexOptions.IgnoreCase);
			Groups = TemplatePattern.GetGroupNumbers().Length;
		}

		public RouteMatch? Match(string url, int offset)
		{
			if (!TemplatePattern.IsMatch(url, offset))
				return null;
			return ExtractMatcher(url, offset);
		}

		private static readonly string[] EmptyArgs = new string[0];
		private static readonly KeyValuePair<string, string>[] EmptyPairs = new KeyValuePair<string, string>[0];
		private static readonly Dictionary<string, string> EmptyDict = new Dictionary<string, string>(0);

		public RouteMatch ExtractMatch(string url, int offset)
		{
			return ExtractMatcher(url, offset);
		}

		private RouteMatch StaticExtractMatch(string url, int offset)
		{
			int pos = url.IndexOf('?');
			if (pos == -1)
				return new RouteMatch(EmptyArgs, EmptyPairs, url);
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
				queryParams.Add(key, value);
			}
			return new RouteMatch(EmptyArgs, EmptyPairs, queryParams, url);
		}

		private RouteMatch DynamicExtractMatch(string url, int offset)
		{
			var match = TemplatePattern.Match(url, offset);
			var boundVars = new KeyValuePair<string, string>[TokensCount];
			var orderedArgs = new string[TokensCount];
			var groups = match.Groups;
			for (int i = 1; i < groups.Count; i++)
			{
				boundVars[i - 1] = new KeyValuePair<string, string>(Tokens[i - 1], groups[i].Value);
				orderedArgs[i - 1] = groups[i].Value;
			}
			if (groups.Count == TokensCount + 1)
				return new RouteMatch(orderedArgs, boundVars, url);
			int pos = url.IndexOf('?');
			if (pos == -1)
				return new RouteMatch(orderedArgs, boundVars, EmptyDict, url);
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
				int index;
				if (TokenMap.TryGetValue(upper, out index))
				{
					boundVars[index] = new KeyValuePair<string, string>(upper, value);
					orderedArgs[index] = value;
				}
				queryParams.Add(key, value);
			}
			return new RouteMatch(orderedArgs, boundVars, queryParams, url);
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