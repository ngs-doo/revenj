using System;

namespace Revenj.Http
{
	[AttributeUsage(AttributeTargets.Class, AllowMultiple = true)]
	public class ControllerAttribute : Attribute
	{
		public readonly string RootUrl;

		public ControllerAttribute(string rootUrl)
		{
			this.RootUrl = rootUrl;
		}
	}
	[AttributeUsage(AttributeTargets.Method, AllowMultiple = true)]
	public class RouteAttribute : Attribute
	{
		public readonly string Path;
		public readonly string Method;
		public readonly bool IsAsync;

		public RouteAttribute(HTTP http, string path) : this(http, path, true) { }
		public RouteAttribute(HTTP http, string path, bool isAsync)
		{
			this.Method = http.ToString();
			this.Path = path;
			this.IsAsync = isAsync;
		}
		public RouteAttribute(string path, string method, bool isAsync = true)
		{
			this.Path = path;
			this.Method = method.ToUpperInvariant();
			this.IsAsync = isAsync;
		}
	}
	public enum HTTP
	{
		GET,
		POST,
		PUT,
		DELETE,
		HEAD,
		OPTIONS,
		SEARCH,
		TRACE,
		PATCH
	}
}
