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
		public readonly int Groups;

		public UriPattern(string template)
		{
			template = template.TrimEnd('*').ToUpperInvariant();
			Tokens = GetTokens(template);
			var segments = BuildRegex(template);
			var finalPattern = EscapePattern.Replace(segments, PathGroup);
			TemplatePattern = new Regex(finalPattern, RegexOptions.Compiled | RegexOptions.IgnoreCase);
			Groups = TemplatePattern.GetGroupNumbers().Length;
		}

		public UriTemplateMatch Match(string url, Uri uri)
		{
			if (!TemplatePattern.IsMatch(url))
				return null;
			return ExtractMatch(url, uri);
		}

		public UriTemplateMatch ExtractMatch(string url, Uri uri)
		{
			var result = new UriTemplateMatch();
			var boundVars = result.BoundVariables;
			var relativeSegments = result.RelativePathSegments;
			var queryParams = result.QueryParameters;
			var match = TemplatePattern.Match(url);
			var groups = match.Groups;
			for (int i = 1; i < groups.Count; i++)
			{
				boundVars.Add(Tokens[i - 1], groups[i].Value);
			}
			var segments = uri.Segments;
			for (int i = 2; i < segments.Length; i++)
				relativeSegments.Add(segments[i]);
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
				boundVars.Add(key.ToUpperInvariant(), value);
				queryParams.Add(key, value);
			}
			return result;
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