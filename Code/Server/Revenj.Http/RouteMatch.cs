using System;
using System.Collections.Generic;
using System.Text;
using System.Web;

namespace Revenj.Http
{
	internal class RouteMatch
	{
		internal readonly Dictionary<string, string> BoundVars;
		private readonly Dictionary<string, string> QueryParams;
		private readonly Uri Uri;

		public RouteMatch(Dictionary<string, string> boundVars, Uri uri)
		{
			this.BoundVars = boundVars;
			this.Uri = uri;
		}

		public RouteMatch(Dictionary<string, string> boundVars, Dictionary<string, string> queryParams, Uri uri)
		{
			this.BoundVars = boundVars;
			this.QueryParams = queryParams;
			this.Uri = uri;
		}

		internal UriTemplateMatch CreateTemplateMatch()
		{
			var result = new UriTemplateMatch();
			var bv = result.BoundVariables;
			foreach (var kv in BoundVars)
				bv.Add(kv.Key, kv.Value);
			var rs = result.RelativePathSegments;
			var segments = Uri.Segments;
			for (int i = 2; i < segments.Length; i++)
				rs.Add(segments[i]);
			var qp = result.QueryParameters;
			int pos = 1;
			if (QueryParams != null)
			{
				foreach (var kv in QueryParams)
					qp.Add(kv.Key, kv.Value);
			}
			else
			{
				var query = Uri.Query;
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
					var key = HttpUtility.UrlDecode(sbName.ToString());
					var value = HttpUtility.UrlDecode(sbValue.ToString());
					qp.Add(key, value);
				}
			}
			return result;
		}

	}
}
