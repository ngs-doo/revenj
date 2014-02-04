using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using NGS.Common;
using NGS.DatabasePersistence.Postgres.Converters;

namespace NGS.DatabasePersistence.Postgres
{
	//TODO: dead code. remove
	[Obsolete("dead code")]
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

		public static StringBuilder[] ParseRecord(this StringBuilder value)
		{
			if (value == null)
				return null;

			var list = new List<StringBuilder>();
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
						list.Add(cur > startPosition ? new StringBuilder(value.ToString(startPosition, cur - startPosition)) : null);
						startPosition = cur + 1;
					}
					else if (current == '"')
					{
						if (cur > startPosition)
							throw new FrameworkException("Error in record format. {0}".With(value));
						cur++;
						var sb = new StringBuilder(value.Length - cur);
						while (cur < len)
						{
							current = value[cur];
							if (current == '"')
							{
								if (value[cur + 1] != '"')
								{
									list.Add(sb);
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

		public static StringBuilder[] ParseArray(StringBuilder value)
		{
			if (value == null || value.Length == 0)
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

		public static List<StringBuilder> ParseList(StringBuilder value)
		{
			if (value == null || value.Length == 0)
				return null;

			var list = new List<StringBuilder>();
			if (value.Length > 0 && value[0] == '{' && value[value.Length - 1] == '}')
			{
				if (value.Length == 2)
					return list;
				int cur = 1;
				int len = value.Length - 1;
				var sb = new StringBuilder(value.Length / 2);
				while (cur <= len)
				{
					char current = value[cur];
					if (current == ',' || current == '}')
					{
						list.Add(sb.Length == 4 && sb.ToString() == "NULL" ? null : sb);
						sb = new StringBuilder(value.Length - cur);
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
									list.Add(sb);
									sb = new StringBuilder(value.Length - cur);
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

		private static void EscapeRecord(StringBuilder sb, string value)
		{
			var start = sb.Length;
			bool escaped = value.Length == 0;
			foreach (var v in value)
			{
				escaped = escaped || v == ',' || v == '\\' || v == '"' || v == '(' || v == ')' || char.IsWhiteSpace(v);
				if (v == '\\')
					sb.Append('\\');
				else if (v == '"')
					sb.Append('"');
				sb.Append(v);
			}
			if (escaped)
			{
				sb.Insert(start, '"');
				sb.Append('"');
			}
		}

		public static string CreateRecord(string[] values)
		{
			if (values == null)
				return null;
			var sb = new StringBuilder("(");
			foreach (var v in values)
			{
				if (v != null)
					EscapeRecord(sb, v);
				sb.Append(",");
			}
			if (sb.Length > 1)
				sb.Length--;
			sb.Append(")");
			return sb.ToString();
		}

		public static string CreateRecord(this IPostgresTypeConverter converter, object instance)
		{
			return converter.ToTuple(instance).BuildTuple(false);
		}

		public static T[] ParseArray<T>(string value, Func<string, T> converter)
		{
			if (string.IsNullOrEmpty(value))
				return null;
			if (value == "{}")
				return new T[0];
			return ParseArray(value).Select(it => converter(it)).ToArray();
		}

		public static List<T> ParseList<T>(string value, Func<string, T> converter)
		{
			if (string.IsNullOrEmpty(value))
				return null;
			if (value == "{}")
				return new List<T>();
			return ParseList(value).Select(it => converter(it)).ToList();
		}

		public static HashSet<T> ParseSet<T>(string value, Func<string, T> converter)
		{
			if (string.IsNullOrEmpty(value))
				return null;
			if (value == "{}")
				return new HashSet<T>();
			return new HashSet<T>(ParseList(value).Select(it => converter(it)));
		}

		public static T[] ParseArray<T>(StringBuilder value, Func<StringBuilder, T> converter)
		{
			if (value == null || value.Length == 0)
				return null;
			if (value.Length == 2 && value[0] == '{' && value[1] == '}')
				return new T[0];
			return ParseArray(value).Select(it => converter(it)).ToArray();
		}

		public static List<T> ParseList<T>(StringBuilder value, Func<StringBuilder, T> converter)
		{
			if (value == null || value.Length == 0)
				return null;
			if (value.Length == 2 && value[0] == '{' && value[1] == '}')
				return new List<T>();
			return ParseList(value).Select(it => converter(it)).ToList();
		}

		public static HashSet<T> ParseSet<T>(StringBuilder value, Func<StringBuilder, T> converter)
		{
			if (value == null || value.Length == 0)
				return null;
			if (value.Length == 2 && value[0] == '{' && value[1] == '}')
				return new HashSet<T>();
			return new HashSet<T>(ParseList(value).Select(it => converter(it)));
		}

		private static void EscapeArray(StringBuilder sb, string value, bool withQuote)
		{
			var start = sb.Length;
			bool escaped = false;
			foreach (var v in value)
			{
				escaped = escaped || v == ',' || v == '\\' || v == '"' || v == '{' || v == '}' || char.IsWhiteSpace(v);
				if (v == '\\' || v == '"')
					sb.Append('\\');
				else if (withQuote && v == '\'')
					sb.Append('\'');
				sb.Append(v);
			}
			if (escaped || value.Length == 0)
			{
				sb.Insert(start, '"');
				sb.Append('"');
			}
		}

		public static string CreateArray(string[] values)
		{
			if (values == null)
				return string.Empty;
			var sb = new StringBuilder(values.Sum(it => it != null ? it.Length : 0));
			sb.Append('{');
			foreach (var v in values)
			{
				if (v != null)
					EscapeArray(sb, v, false);
				else
					sb.Append("NULL");
				sb.Append(',');
			}
			if (sb.Length > 1)
				sb.Length--;
			sb.Append('}');
			return sb.ToString();
		}

		public static string CreateArray<T>(IEnumerable<T> data, Func<T, string> converter)
		{
			if (data == null)
				return null;
			return CreateArray(data.Select(it => converter(it)).ToArray());
		}

		public static void CreateArray<T>(StringBuilder sb, IEnumerable<T> data, Func<T, string> converter)
		{
			sb.Append("'{");
			var len = sb.Length;
			foreach (var it in data)
			{
				var v = converter(it);
				if (v != null)
					EscapeArray(sb, v, true);
				else
					sb.Append("NULL");
				sb.Append(',');
			}
			if (sb.Length > len)
				sb.Length--;
			sb.Append("}'");
		}

		public static Stream CreateCopy(RecordTuple tuple)
		{
			return tuple.Build(true, PostgresTuple.EscapeBulkCopy);
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
	}
}
