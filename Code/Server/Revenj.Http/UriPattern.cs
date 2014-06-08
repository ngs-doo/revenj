using System;
using System.Collections.Generic;
using System.Text;
using System.Text.RegularExpressions;

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

		public UriPattern(string template)
		{
			template = template.TrimEnd('*').ToUpperInvariant();
			Tokens = GetTokens(template);
			var finalPattern = BuildRegex(template);
			TemplatePattern = new Regex(finalPattern, RegexOptions.Compiled | RegexOptions.IgnoreCase);
		}

		public UriTemplateMatch Match(string url, Uri uri)
		{
			if (!TemplatePattern.IsMatch(url))
				return null;
			return ExtractMatches(url, uri);
		}

		public UriTemplateMatch ExtractMatches(string url, Uri uri)
		{
			var result = new UriTemplateMatch();
			foreach (Match match in TemplatePattern.Matches(url))
			{
				int tokenIndex = 0;
				foreach (Group group in match.Groups)
				{
					if (tokenIndex > 0 && group.Success)
						result.BoundVariables.Add(Tokens[tokenIndex - 1], group.Value);
					tokenIndex++;
				}
			}
			for (int i = 2; i < uri.Segments.Length; i++)
				result.RelativePathSegments.Add(uri.Segments[i]);
			int pos = 1;
			var query = uri.Query;
			var sbName = new StringBuilder();
			var sbValue = new StringBuilder();
			while (pos < query.Length)
			{
				sbName.Length = 0;
				sbValue.Length = 0;
				while (pos < query.Length && query[pos] != '=') sbName.Append(query[pos++]);
				pos++;
				while (pos < query.Length && query[pos] != '&') sbValue.Append(query[pos++]);
				pos++;
				var key = Uri.UnescapeDataString(sbName.ToString());
				var value = Uri.UnescapeDataString(sbValue.ToString());
				result.BoundVariables.Add(key.ToUpperInvariant(), value);
				result.QueryParameters.Add(key, value);
			}
			return result;
		}

		private string BuildRegex(string template)
		{
			var iof = template.IndexOf('?');
			if (iof != -1)
			{
				var beforeQuery = RegexMetaPattern.Replace(template.Substring(0, iof), RegexMetaCharactersReplacements);
				return EscapePattern.Replace(beforeQuery, PathGroup);
			}
			var segments = RegexMetaPattern.Replace(template, RegexMetaCharactersReplacements) + ".*";
			return EscapePattern.Replace(segments, PathGroup);
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