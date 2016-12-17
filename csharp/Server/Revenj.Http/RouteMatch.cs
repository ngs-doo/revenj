using System;
using System.Collections.Generic;
using System.Web;

namespace Revenj.Http
{
	internal struct RouteMatch
	{
		internal readonly string[] OrderedArgs;
		internal readonly KeyValuePair<string, string>[] BoundVars;
		private readonly Dictionary<string, string> QueryParams;
		internal readonly string RawUrl;

		public RouteMatch(string[] orderedArgs, KeyValuePair<string, string>[] boundVars, string rawUrl)
		{
			this.OrderedArgs = orderedArgs;
			this.BoundVars = boundVars;
			this.QueryParams = null;
			this.RawUrl = rawUrl;
		}

		public RouteMatch(
			string[] orderedArgs,
			KeyValuePair<string, string>[] boundVars,
			Dictionary<string, string> queryParams,
			string rawUrl)
		{
			this.OrderedArgs = orderedArgs;
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
			var nextSeg = RawUrl.IndexOf('/', 1) + 1;
			while (nextSeg != 0)
			{
				var lastSeg = nextSeg;
				nextSeg = RawUrl.IndexOf('/', nextSeg) + 1;
				if (nextSeg != 0)
					rs.Add(RawUrl.Substring(lastSeg, nextSeg - lastSeg - 1));
				else
					rs.Add(RawUrl.Substring(lastSeg, maxLen - lastSeg));
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
