using System;
using System.Collections.Generic;
using System.Text;

namespace Revenj.DatabasePersistence.Oracle
{
	public static class OracleUriConverter
	{
		public static string BuildURI(string[] parts)
		{
			var sb = new StringBuilder();
			foreach (var p in parts)
			{
				foreach (var c in p)
				{
					if (c == '/' || c == '\\')
						sb.Append('\\');
					sb.Append(c);
				}
				sb.Append('/');
			}
			sb.Length--;
			return sb.ToString();
		}

		public static List<string> ParseURI(string uri)
		{
			var list = new List<string>();
			var len = uri.Length;
			int i = 0;
			var last = 0;
			var sb = new StringBuilder();
			while (i < len)
			{
				var c = uri[i];
				if (c == '/')
				{
					list.Add(sb.ToString());
					sb.Length = 0;
					i++;
					last = i;
					continue;
				}
				if (c == '\\')
					c = uri[++i];
				sb.Append(c);
				i++;
			}
			list.Add(sb.ToString());
			return list;
		}

		public static string BuildSimpleUriList(List<string> uris)
		{
			if (uris.Count == 0)
				throw new ArgumentException("uris list can't be empty");
			var sb = new StringBuilder(uris.Count * 40);
			foreach (var uri in uris)
			{
				sb.Append('\'');
				for (var i = 0; i < uri.Length; i++)
				{
					var c = uri[i];
					if (c == '\'')
						sb.Append('\'');
					sb.Append(c);
				}
				sb.Append("',");
			}
			sb.Length--;
			return sb.ToString();
		}

		public static string BuildSimpleUri(string uri)
		{
			if (uri.Contains("'"))
				return "'" + uri.Replace("'", "''") + "'";
			return "'" + uri + "'";
		}

		public static string BuildCompositeUriList(List<string> uris)
		{
			if (uris.Count == 0)
				throw new ArgumentException("uris list can't be empty");
			var sb = new StringBuilder(uris.Count * 40);
			foreach (var uri in uris)
			{
				sb.Append("('");
				var len = uri.Length;
				int i = 0;
				while (i < len)
				{
					var c = uri[i];
					if (c == '\\')
					{
						i++;
						sb.Append(uri[i]);
					}
					else if (c == '/')
						sb.Append("','");
					else if (c == '\'')
						sb.Append("''");
					else
						sb.Append(c);
					i++;
				}
				sb.Append("'),");
			}
			sb.Length--;
			return sb.ToString();
		}

		public static string BuildCompositeUri(string uri)
		{
			var sb = new StringBuilder(uri.Length + 4);
			sb.Append("'");
			var len = uri.Length;
			int i = 0;
			while (i < len)
			{
				var c = uri[i];
				if (c == '\\')
				{
					i++;
					sb.Append(uri[i]);
				}
				else if (c == '/')
					sb.Append("','");
				else if (c == '\'')
					sb.Append("''");
				else
					sb.Append(c);
				i++;
			}
			sb.Append("'");
			return sb.ToString();
		}
	}
}
