using System;
using System.Collections.Generic;
using System.Configuration;
using System.Linq;
using System.ServiceModel.Web;
using System.Xml.Linq;

namespace Revenj.Http
{
	internal class Routes
	{
		private readonly Dictionary<string, Dictionary<string, List<RouteHandler>>> MethodRoutes = new Dictionary<string, Dictionary<string, List<RouteHandler>>>();
		private Dictionary<ReqId, RouteHandler> Cache = new Dictionary<ReqId, RouteHandler>();

		public Routes(IServiceProvider locator)
		{
			var config = ConfigurationManager.OpenExeConfiguration(ConfigurationUserLevel.None);
			var xml = XElement.Load(config.FilePath, LoadOptions.None);
			var sm = xml.Element("system.serviceModel");
			if (sm == null)
				throw new ConfigurationErrorsException("Services not defined. system.serviceModel missing from configuration");
			var she = sm.Element("serviceHostingEnvironment");
			if (she == null)
				throw new ConfigurationErrorsException("Services not defined. serviceHostingEnvironment missing from configuration");
			var sa = she.Element("serviceActivations");
			if (sa == null)
				throw new ConfigurationErrorsException("Services not defined. serviceActivations missing from configuration");
			var services = sa.Elements("add").ToList();
			if (services.Count == 0)
				throw new ConfigurationErrorsException("Services not defined. serviceActivations elements not defined in configuration");
			foreach (XElement s in services)
				ConfigureService(s, locator);
		}

		private void ConfigureService(XElement service, IServiceProvider locator)
		{
			var attributes = service.Attributes().ToList();
			var ra = attributes.FirstOrDefault(it => "relativeAddress".Equals(it.Name.LocalName, StringComparison.InvariantCultureIgnoreCase));
			var serv = attributes.FirstOrDefault(it => "service".Equals(it.Name.LocalName, StringComparison.InvariantCultureIgnoreCase));
			if (serv == null || string.IsNullOrEmpty(serv.Value))
				throw new ConfigurationErrorsException("Missing service type on serviceActivation element: " + service.ToString());
			if (ra == null || string.IsNullOrEmpty(ra.Value))
				throw new ConfigurationErrorsException("Missing relative address on serviceActivation element: " + service.ToString());
			var type = Type.GetType(serv.Value);
			if (type == null)
				throw new ConfigurationErrorsException("Invalid service defined in " + ra.Value + ". Type " + serv.Value + " not found.");
			var instance = locator.GetService(type);
			foreach (var i in new[] { type }.Union(type.GetInterfaces()))
			{
				foreach (var m in i.GetMethods())
				{
					var inv = (WebInvokeAttribute[])m.GetCustomAttributes(typeof(WebInvokeAttribute), false);
					var get = (WebGetAttribute[])m.GetCustomAttributes(typeof(WebGetAttribute), false);
					foreach (var at in inv)
					{
						var rh = new RouteHandler(ra.Value, at.UriTemplate, instance, m);
						Add(at.Method, rh);
					}
					foreach (var at in get)
					{
						var rh = new RouteHandler(ra.Value, at.UriTemplate, instance, m);
						Add("GET", rh);
					}
				}
			}
		}

		private void Add(string method, RouteHandler handler)
		{
			Dictionary<string, List<RouteHandler>> dict;
			if (!MethodRoutes.TryGetValue(method, out dict))
				MethodRoutes[method] = dict = new Dictionary<string, List<RouteHandler>>();
			List<RouteHandler> list;
			if (!dict.TryGetValue(handler.Service, out list))
				dict[handler.Service] = list = new List<RouteHandler>();
			var first = list.FindIndex(it => it.Pattern.Groups < handler.Pattern.Groups);
			if (first != -1)
				list.Insert(first, handler);
			else
				list.Add(handler);
		}

		struct ReqId : IEquatable<ReqId>
		{
			private readonly int HashCode;
			private readonly string Http;
			private readonly string Path;
			public ReqId(string http, string path)
			{
				this.HashCode = http.GetHashCode() + path.GetHashCode();
				this.Http = http;
				this.Path = path;
			}
			public override int GetHashCode() { return HashCode; }
			public override bool Equals(object obj) { return Equals((ReqId)obj); }
			public bool Equals(ReqId other)
			{
				return this.Http == other.Http && this.Path == other.Path;
			}
		}

		public RouteHandler Find(string httpMethod, string rawUrl, string absolutePath, out RouteMatch routeMatch)
		{
			var reqId = new ReqId(httpMethod, absolutePath);
			RouteHandler handler;
			if (Cache.TryGetValue(reqId, out handler))
			{
				routeMatch = handler.Pattern.ExtractMatch(rawUrl, handler.Service.Length);
				return handler;
			}
			routeMatch = null;
			Dictionary<string, List<RouteHandler>> handlers;
			if (!MethodRoutes.TryGetValue(httpMethod, out handlers))
				return null;
			if (rawUrl.IndexOf('/') == rawUrl.LastIndexOf('/'))
				return null;
			string service;
			int pos = rawUrl.IndexOf('/', 1);
			if (pos == -1)
				service = rawUrl.ToLowerInvariant();
			else
				service = rawUrl.Substring(0, pos).ToLowerInvariant();
			List<RouteHandler> routes;
			if (!handlers.TryGetValue(service, out routes))
				return null;
			foreach (var h in routes)
			{
				var match = h.Pattern.Match(rawUrl, service.Length);
				if (match != null)
				{
					routeMatch = match;
					var newCache = new Dictionary<ReqId, RouteHandler>(Cache);
					newCache[reqId] = h;
					Cache = newCache;
					return h;
				}
			}
			return null;
		}
	}
}
