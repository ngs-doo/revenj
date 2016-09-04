using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using Revenj.Common;
using Revenj.DatabasePersistence.Postgres.Converters;

namespace Revenj.DatabasePersistence.Postgres
{
	public static class PostgresRecordConverter
	{
		public static string[] ParseRecord(this string value)
		{
			if (string.IsNullOrEmpty(value))
				return null;

			var list = new List<string>();
			if (value.Length > 0 && value[0] == '(' && value[value.Length - 1] == ')')
			{
				int cur = 1;
				int len = value.Length - 1;
				int startPosition = cur;
				while (cur <= len)
				{
					char current = value[cur];
					if (current == ',' || current == ')')
					{
						list.Add(cur > startPosition ? value.Substring(startPosition, cur - startPosition) : null);
						startPosition = cur + 1;
					}
					else if (current == '"')
					{
						if (cur > startPosition)
							throw new FrameworkException("Error in record format. {0}".With(value));
						cur++;
						var sb = new StringBuilder();
						while (cur < len)
						{
							current = value[cur];
							if (current == '"')
							{
								if (value[cur + 1] != '"')
								{
									//TODO this throws an exception in memory pressure situations
									//rewrite it to use StringBuilder or even better Token (as same as writing)
									list.Add(sb.ToString());
									cur++;
									startPosition = cur + 1;
									break;
								}
								else sb.Append(current);
								cur++;
							}
							else if (current == '\\')
							{
								sb.Append(value[cur + 1]);
								cur++;
							}
							else sb.Append(current);
							cur++;
						}
					}
					cur++;
				}
				if (cur > startPosition)
					throw new FrameworkException("Error in record format. {0}".With(value));
			}
			return list.ToArray();
		}
		public static string[] ParseArray(this string value)
		{
			if (string.IsNullOrEmpty(value))
				return null;

			return ParseList(value).ToArray();
		}

		public static List<string> ParseList(this string value)
		{
			if (string.IsNullOrEmpty(value))
				return null;

			var list = new List<string>();
			if (value.Length > 0 && value[0] == '{' && value[value.Length - 1] == '}')
			{
				if (value.Length == 2)
					return new List<string>();
				int cur = 1;
				int len = value.Length - 1;
				var sb = new StringBuilder();
				while (cur <= len)
				{
					char current = value[cur];
					if (current == ',' || current == '}')
					{
						var sbVal = sb.ToString();
						list.Add(sbVal != "NULL" ? sbVal : null);
						sb = new StringBuilder();
					}
					else if (current == '"')
					{
						if (sb.Length > 0)
							throw new FrameworkException("Error in array format. {0}".With(value));
						cur++;
						while (cur < len)
						{
							current = value[cur];
							if (current == '\\')
							{
								cur++;
								sb.Append(value[cur]);
							}
							else if (current == '"')
							{
								cur++;
								current = value[cur];
								if (current == '"')
									sb.Append(current);
								else
								{
									list.Add(sb.ToString());
									sb = new StringBuilder();
									break;
								}
							}
							else sb.Append(current);
							cur++;
						}
					}
					else sb.Append(current);
					cur++;
				}
				if (sb.Length > 0)
					throw new FrameworkException("Error in array format. {0}".With(value));
			}
			return list;
		}

		public static string CreateRecord(this IPostgresTypeConverter converter, object instance)
		{
			return converter.ToTuple(instance).BuildTuple(false);
		}

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
			var sb = new StringBuilder();
			while (i < len)
			{
				var c = uri[i];
				if (c == '/')
				{
					list.Add(sb.ToString());
					sb.Length = 0;
					i++;
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

		public static void WriteSimpleUriList(TextWriter tw, List<string> uris)
		{
			tw.Write('\'');
			var uri = uris[0];
			var ind = uri.IndexOf('\'');
			if (ind == -1)
				tw.Write(uri);
			else
			{
				for (var i = 0; i < uri.Length; i++)
				{
					var c = uri[i];
					if (c == '\'')
						tw.Write("''");
					else
						tw.Write(c);
				}
			}
			for (int x = 1; x < uris.Count; x++)
			{
				uri = uris[x];
				tw.Write("','");
				ind = uri.IndexOf('\'');
				if (ind == -1)
					tw.Write(uri);
				else
				{
					for (var i = 0; i < uri.Length; i++)
					{
						var c = uri[i];
						if (c == '\'')
							tw.Write("''");
						else
							tw.Write(c);
					}
				}
			}
			tw.Write('\'');
		}

		public static void WriteSimpleUri(TextWriter tw, string uri)
		{
			tw.Write('\'');
			var ind = uri.IndexOf('\'');
			if (ind == -1)
				tw.Write(uri);
			else
			{
				for (var i = 0; i < uri.Length; i++)
				{
					var c = uri[i];
					if (c == '\'')
						tw.Write("''");
					else
						tw.Write(c);
				}
			}
			tw.Write('\'');
		}

		private static readonly char[] EscapeUris = new char[] { '\\', '/', '\'' };

		public static void WriteCompositeUriList(TextWriter tw, List<string> uris)
		{
			tw.Write("('");
			var uri = uris[0];
			var i = 0;
			var ind = uri.IndexOfAny(EscapeUris);
			if (ind == -1)
				tw.Write(uri);
			else
			{
				while (i < uri.Length)
				{
					var c = uri[i];
					if (c == '\\')
						tw.Write(uri[++i]);
					else if (c == '/')
						tw.Write("','");
					else if (c == '\'')
						tw.Write("''");
					else
						tw.Write(c);
					i++;
				}
			}
			for (int x = 1; x < uris.Count; x++)
			{
				tw.Write("'),('");
				uri = uris[x];
				ind = uri.IndexOfAny(EscapeUris);
				if (ind == -1)
					tw.Write(uri);
				else
				{
					i = 0;
					while (i < uri.Length)
					{
						var c = uri[i];
						if (c == '\\')
							tw.Write(uri[++i]);
						else if (c == '/')
							tw.Write("','");
						else if (c == '\'')
							tw.Write("''");
						else
							tw.Write(c);
						i++;
					}
				}
			}
			tw.Write("')");
		}

		public static void WriteCompositeUri(TextWriter tw, string uri)
		{
			tw.Write("('");
			var i = 0;
			var ind = uri.IndexOfAny(EscapeUris);
			if (ind == -1)
				tw.Write(uri);
			else
			{
				while (i < uri.Length)
				{
					var c = uri[i];
					if (c == '\\')
						tw.Write(uri[++i]);
					else if (c == '/')
						tw.Write("','");
					else if (c == '\'')
						tw.Write("''");
					else
						tw.Write(c);
					i++;
				}
			}
			tw.Write("')");
		}
	}
}
