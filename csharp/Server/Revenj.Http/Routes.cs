using System;
using System.Collections.Generic;
using System.Configuration;
using System.Linq;
using System.ServiceModel.Web;
using System.Xml.Linq;
using Revenj.Serialization;
using Revenj.Utility;

namespace Revenj.Http
{
	internal class Routes
	{
		private readonly Dictionary<string, Dictionary<string, List<RouteHandler>>> MethodRoutes = new Dictionary<string, Dictionary<string, List<RouteHandler>>>();
		private Dictionary<int, RouteHandler> Cache = new Dictionary<int, RouteHandler>();

		public Routes(IServiceProvider locator, IWireSerialization serialization)
		{
			var totalControllers = 0;
			foreach (var t in AssemblyScanner.GetAllTypes())
			{
				if (t.IsClass && !t.IsAbstract && (t.IsPublic || t.IsNestedPublic))
				{
					var attr = t.GetCustomAttributes(typeof(ControllerAttribute), false) as ControllerAttribute[];
					if (attr != null && attr.Length > 0)
					{
						totalControllers++;
						foreach (var a in attr)
							ConfigureService(locator, serialization, a.RootUrl, t);
					}
				}
			}
			var config = ConfigurationManager.OpenExeConfiguration(ConfigurationUserLevel.None);
			var xml = XElement.Load(config.FilePath, LoadOptions.None);
			var sm = xml.Element("system.serviceModel");
			if (sm == null)
			{
				if (totalControllers > 0) return;
				throw new ConfigurationErrorsException("Services not defined. system.serviceModel missing from configuration");
			}
			var she = sm.Element("serviceHostingEnvironment");
			if (she == null)
			{
				if (totalControllers > 0) return;
				throw new ConfigurationErrorsException("Services not defined. serviceHostingEnvironment missing from configuration");
			}
			var sa = she.Element("serviceActivations");
			if (sa == null)
			{
				if (totalControllers > 0) return;
				throw new ConfigurationErrorsException("Services not defined. serviceActivations missing from configuration");
			}
			var services = sa.Elements("add").ToList();
			if (services.Count == 0 && totalControllers == 0)
				throw new ConfigurationErrorsException("Services not defined and controllers not found on path. serviceActivations elements not defined in configuration");
			foreach (XElement s in services)
			{
				var attributes = s.Attributes().ToList();
				var ra = attributes.FirstOrDefault(it => "relativeAddress".Equals(it.Name.LocalName, StringComparison.InvariantCultureIgnoreCase));
				var serv = attributes.FirstOrDefault(it => "service".Equals(it.Name.LocalName, StringComparison.InvariantCultureIgnoreCase));
				if (serv == null || string.IsNullOrEmpty(serv.Value))
					throw new ConfigurationErrorsException("Missing service type on serviceActivation element: " + s.ToString());
				if (ra == null || string.IsNullOrEmpty(ra.Value))
					throw new ConfigurationErrorsException("Missing relative address on serviceActivation element: " + s.ToString());
				var type = Type.GetType(serv.Value);
				if (type == null)
					throw new ConfigurationErrorsException("Invalid service defined in " + ra.Value + ". Type " + serv.Value + " not found.");
				ConfigureService(locator, serialization, ra.Value, type);
			}
		}

		private void ConfigureService(IServiceProvider locator, IWireSerialization serialization, string name, Type type)
		{
			var instance = locator.GetService(type);
			foreach (var i in new[] { type }.Union(type.GetInterfaces()))
			{
				foreach (var m in i.GetMethods())
				{
					var inv = (WebInvokeAttribute[])m.GetCustomAttributes(typeof(WebInvokeAttribute), false);
					var get = (WebGetAttribute[])m.GetCustomAttributes(typeof(WebGetAttribute), false);
					var route = (RouteAttribute[])m.GetCustomAttributes(typeof(RouteAttribute), false);
					foreach (var at in inv)
					{
						var rh = new RouteHandler(name, at.UriTemplate, instance, true, m, locator, serialization);
						Add(at.Method, rh);
					}
					foreach (var at in get)
					{
						var rh = new RouteHandler(name, at.UriTemplate, instance, true, m, locator, serialization);
						Add("GET", rh);
					}
					foreach (var at in route)
					{
						var rh = new RouteHandler(name, at.Path, instance, at.IsAsync, m, locator, serialization);
						Add(at.Method, rh);
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

		internal RouteMatch? Find(string httpMethod, char[] buffer, int len, out RouteHandler handler)
		{
			var reqHash = StringCache.CalcHash(httpMethod, buffer, len);
			int askSign = -1;
			for (int i = 0; i < len; i++)
			{
				if (buffer[i] == '?')
				{
					askSign = i;
					break;
				}
			}
			if (askSign == -1 && Cache.TryGetValue(reqHash, out handler))
			{
				if (handler.Pattern.IsStatic)
					return handler.Pattern.ExtractMatch(handler.Url, 0);
			}
			var rawUrl = new string(buffer, 0, len);
			return FindRoute(httpMethod, rawUrl, reqHash, out handler);
		}

		public RouteMatch? Find(string httpMethod, string rawUrl, string absolutePath, out RouteHandler handler)
		{
			var reqHash = StringCache.CalcHash(httpMethod, absolutePath);
			if (Cache.TryGetValue(reqHash, out handler))
			{
				return handler.Pattern.ExtractMatch(rawUrl, handler.Service.Length);
			}
			return FindRoute(httpMethod, rawUrl, reqHash, out handler);
		}

		private RouteMatch? FindRoute(string httpMethod, string rawUrl, int reqHash, out RouteHandler handler)
		{
			handler = null;
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
					var newCache = new Dictionary<int, RouteHandler>(Cache);
					newCache[reqHash] = h;
					Cache = newCache;
					handler = h;
					return match;
				}
			}
			return null;
		}
	}
}
