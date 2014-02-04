using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Configuration;
using System.Net;
using System.ServiceModel.Configuration;
using System.ServiceModel.Web;
using NGS.DomainPatterns;

namespace Revenj.Http
{
	internal class Routes
	{
		private readonly Dictionary<string, List<RouteHandler>> MethodRoutes = new Dictionary<string, List<RouteHandler>>();
		private readonly ConcurrentDictionary<string, KeyValuePair<RouteHandler, Uri>> Cache = new ConcurrentDictionary<string, KeyValuePair<RouteHandler, Uri>>();

		public Routes(IServiceLocator locator)
		{
			var config = ConfigurationManager.OpenExeConfiguration(ConfigurationUserLevel.None);
			var sm = config.GetSectionGroup("system.serviceModel");
			if (sm == null)
				throw new ConfigurationErrorsException("Services not defined. system.serviceModel missing from configuration");
			var she = sm.Sections["serviceHostingEnvironment"];
			if (she == null)
				throw new ConfigurationErrorsException("Services not defined. serviceHostingEnvironment missing from configuration");
			var sa = she.ElementInformation.Properties["serviceActivations"];
			if (sa == null)
				throw new ConfigurationErrorsException("Services not defined. serviceActivations missing from configuration");
			var services = sa.Value as ServiceActivationElementCollection;
			if (services == null || services.Count == 0)
				throw new ConfigurationErrorsException("Services not defined. serviceActivations elements not defined in configuration");
			foreach (ServiceActivationElement s in services)
				ConfigureService(s, locator);
		}

		private void ConfigureService(ServiceActivationElement service, IServiceLocator locator)
		{
			var type = Type.GetType(service.Service);
			if (type == null)
				throw new ConfigurationErrorsException("Invalid service defined in " + service.RelativeAddress + ". Type " + service.Service + " not found.");
			var instance = locator.Resolve(type, null);
			foreach (var i in type.GetInterfaces())
			{
				foreach (var m in i.GetMethods())
				{
					var inv = (WebInvokeAttribute[])m.GetCustomAttributes(typeof(WebInvokeAttribute), false);
					var get = (WebGetAttribute[])m.GetCustomAttributes(typeof(WebGetAttribute), false);
					foreach (var at in inv)
					{
						var rh = new RouteHandler(service.RelativeAddress, at.UriTemplate, instance, m);
						Add(at.Method, rh);
					}
					foreach (var at in get)
					{
						var rh = new RouteHandler(service.RelativeAddress, at.UriTemplate, instance, m);
						Add("GET", rh);
					}
				}
			}
		}

		private void Add(string method, RouteHandler handler)
		{
			List<RouteHandler> list;
			if (!MethodRoutes.TryGetValue(method, out list))
				MethodRoutes[method] = list = new List<RouteHandler>();
			list.Add(handler);
		}

		public RouteHandler Find(HttpListenerRequest request, out UriTemplateMatch templateMatch)
		{
			var reqId = request.HttpMethod + ":" + request.Url.LocalPath;
			KeyValuePair<RouteHandler, Uri> rh;
			if (Cache.TryGetValue(reqId, out rh))
			{
				templateMatch = rh.Key.Template.Match(rh.Value, request.Url);
				return rh.Key;
			}
			templateMatch = null;
			List<RouteHandler> handlers;
			if (!MethodRoutes.TryGetValue(request.HttpMethod, out handlers))
				return null;
			var reqUrl = request.Url;
			var url = reqUrl.ToString();
			var baseAddr = new Uri(url.Substring(0, url.Length - request.RawUrl.Length));
			foreach (var h in handlers)
			{
				var match = h.Template.Match(baseAddr, reqUrl);
				if (match != null)
				{
					templateMatch = match;
					Cache.TryAdd(reqId, new KeyValuePair<RouteHandler, Uri>(h, baseAddr));
					return h;
				}
			}
			return null;
		}
	}
}
