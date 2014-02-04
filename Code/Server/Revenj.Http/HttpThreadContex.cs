using System;
using System.Collections;
using System.Collections.Generic;
using System.Net;
using Revenj.Api;

namespace Revenj.Http
{
	internal class HttpThreadContex : IRequestContext, IResponseContext, IDictionary<string, string>
	{
		private readonly HttpListenerRequest Request;
		private readonly HttpListenerResponse Response;
		private UriTemplateMatch TemplateMatch;

		public HttpThreadContex(
			HttpListenerRequest request,
			HttpListenerResponse response,
			UriTemplateMatch templateMatch)
		{
			this.Request = request;
			this.Response = response;
			this.TemplateMatch = templateMatch;
			var at = request.AcceptTypes;
			if (at != null && at.Length != 0)
				AcceptType = at[0];
		}

		private readonly string AcceptType;
		public string Accept { get { return AcceptType; } }

		long IRequestContext.ContentLength { get { return Request.ContentLength64; } }
		long IResponseContext.ContentLength
		{
			get { return Response.ContentLength64; }
			set { Response.ContentLength64 = value; }
		}
		string IRequestContext.ContentType { get { return Request.ContentType; } }
		string IResponseContext.ContentType
		{
			get { return Response.ContentType; }
			set { Response.ContentType = value; }
		}
		public Uri RequestUri { get { return Request.Url; } }
		public UriTemplateMatch UriTemplateMatch
		{
			get { return TemplateMatch; }
			set { TemplateMatch = value; }
		}
		public DateTime? IfModifiedSince
		{
			get { return null; }
		}
		public DateTime? IfUnmodifiedSince
		{
			get { return null; }
		}
		public string GetHeader(string name)
		{
			return Request.Headers.Get(name);
		}
		public IDictionary<string, string> Headers { get { return this; } }
		public HttpStatusCode StatusCode
		{
			get { return (HttpStatusCode)Response.StatusCode; }
			set { Response.StatusCode = (int)value; }
		}

		void IDictionary<string, string>.Add(string key, string value)
		{
			Response.Headers.Add(key, value);
		}

		bool IDictionary<string, string>.ContainsKey(string key)
		{
			return Response.Headers[key] != null;
		}

		ICollection<string> IDictionary<string, string>.Keys
		{
			get { return Response.Headers.AllKeys; }
		}

		bool IDictionary<string, string>.Remove(string key)
		{
			Response.Headers.Remove(key);
			return true;
		}

		bool IDictionary<string, string>.TryGetValue(string key, out string value)
		{
			value = Response.Headers[key];
			return value != null;
		}

		ICollection<string> IDictionary<string, string>.Values
		{
			get
			{
				var h = Response.Headers;
				var list = new List<string>(h.Count);
				foreach (string k in h.Keys)
					list.Add(h[k]);
				return list;
			}
		}

		string IDictionary<string, string>.this[string key]
		{
			get { return Response.Headers[key]; }
			set { Response.Headers.Set(key, value); }
		}

		void ICollection<KeyValuePair<string, string>>.Add(KeyValuePair<string, string> item)
		{
			Response.Headers.Add(item.Key, item.Value);
		}

		void ICollection<KeyValuePair<string, string>>.Clear()
		{
			Response.Headers.Clear();
		}

		bool ICollection<KeyValuePair<string, string>>.Contains(KeyValuePair<string, string> item)
		{
			return Response.Headers[item.Key] == item.Value;
		}

		void ICollection<KeyValuePair<string, string>>.CopyTo(KeyValuePair<string, string>[] array, int arrayIndex)
		{
			var h = Response.Headers;
			for (int i = 0; i < h.Count; i++)
				array[i + arrayIndex] = new KeyValuePair<string, string>(h.GetKey(i), h.Get(i));
		}

		int ICollection<KeyValuePair<string, string>>.Count { get { return Response.Headers.Count; } }

		bool ICollection<KeyValuePair<string, string>>.IsReadOnly { get { return false; } }

		bool ICollection<KeyValuePair<string, string>>.Remove(KeyValuePair<string, string> item)
		{
			if (Response.Headers[item.Key] == item.Value)
			{
				Response.Headers.Remove(item.Key);
				return true;
			}
			return false;
		}

		IEnumerator<KeyValuePair<string, string>> IEnumerable<KeyValuePair<string, string>>.GetEnumerator()
		{
			var h = Response.Headers;
			foreach (string k in h.Keys)
				yield return new KeyValuePair<string, string>(k, h[k]);
		}

		IEnumerator IEnumerable.GetEnumerator() { return Response.Headers.GetEnumerator(); }
	}
}
