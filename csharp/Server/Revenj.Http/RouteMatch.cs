using System;
using System.Collections.Generic;
using System.Web;

namespace Revenj.Http
{
	internal class RouteMatch
	{
		internal readonly Dictionary<string, string> BoundVars;
		private readonly Dictionary<string, string> QueryParams;
		private readonly string RawUrl;

		public RouteMatch(Dictionary<string, string> boundVars, string rawUrl)
		{
			this.BoundVars = boundVars;
			this.RawUrl = rawUrl;
		}

		public RouteMatch(Dictionary<string, string> boundVars, Dictionary<string, string> queryParams, string rawUrl)
		{
			this.BoundVars = boundVars;
			this.QueryParams = queryParams;
			this.RawUrl = rawUrl;
		}

		internal UriTemplateMatch CreateTemplateMatch()
		{
			var result = new UriTemplateMatch();
			var bv = result.BoundVariables;
			foreach (var kv in BoundVars)
				bv.Add(kv.Key, kv.Value);
			var rs = result.RelativePathSegments;
			int pos = RawUrl.IndexOf('?');
			var maxLen = pos != -1 ? pos : RawUrl.Length;
			var nextSeg = RawUrl.IndexOf('/', 1);
			while (nextSeg != -1)
			{
				var lastSeg = nextSeg;
				nextSeg = RawUrl.IndexOf('/', nextSeg + 1);
				if (nextSeg != -1 && nextSeg < maxLen)
					rs.Add(RawUrl.Substring(lastSeg, nextSeg));
				else
					rs.Add(RawUrl.Substring(lastSeg, maxLen));
			}
			var qp = result.QueryParameters;
			if (QueryParams != null)
			{
				foreach (var kv in QueryParams)
					qp.Add(kv.Key, kv.Value);
				return result;
			}
			if (pos != -1)
			{
				var query = RawUrl;
				pos++;
				while (pos < query.Length)
				{
					int start = pos;
					while (pos < query.Length && query[pos] != '=') pos++;
					var key = HttpUtility.UrlDecode(query.Substring(start, pos - start));
					pos++;
					start = pos;
					while (pos < query.Length && query[pos] != '&') pos++;
					var value = HttpUtility.UrlDecode(query.Substring(start, pos - start));
					pos++;
					qp.Add(key, value);
				}
			}
			return result;
		}

	}
}
