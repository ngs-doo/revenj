using System;
using System.Configuration;
using System.IO;
using System.Net;
using System.Text;
using System.Threading;
using NGS.DomainPatterns;
using NGS.Logging;
using Revenj.Api;

namespace Revenj.Http
{
	public class HttpServer
	{
		private readonly HttpListener Listener;
		private readonly ILogger Logger;
		private readonly Routes Routes;
		private readonly HttpAuth Authentication;

		public HttpServer(ILogFactory logFactory, IServiceLocator locator)
		{
			Logger = logFactory.Create("Http server");
			Listener = new HttpListener();
			Listener.IgnoreWriteExceptions = true;
			foreach (string key in ConfigurationManager.AppSettings.Keys)
			{
				if (key.StartsWith("HttpAddress", StringComparison.InvariantCultureIgnoreCase))
					Listener.Prefixes.Add(ConfigurationManager.AppSettings[key]);
			}
			if (Listener.Prefixes.Count == 0)
			{
				Listener.Prefixes.Add("http://*:80/");
				Listener.Prefixes.Add("https://*:443/");
			}
			Routes = new Routes(locator);
			var customAuth = ConfigurationManager.AppSettings["CustomAuth"];
			if (!string.IsNullOrEmpty(customAuth))
			{
				var authType = Type.GetType(customAuth);
				if (!typeof(HttpAuth).IsAssignableFrom(authType))
					throw new ConfigurationErrorsException("Custom auth does not inherit from HttpAuth. Please inheri from " + typeof(HttpAuth).FullName);
				Authentication = locator.Resolve<HttpAuth>(authType);
			}
			else Authentication = locator.Resolve<HttpAuth>();
		}

		public void Run()
		{
			try
			{
				Listener.Start();
				while (true)
				{
					try
					{
						var context = Listener.GetContext();
						ThreadPool.QueueUserWorkItem(ProcessMessageThread, context);
						context = null;
					}
					catch (HttpListenerException ex)
					{
						Console.WriteLine(ex.ToString());
						Logger.Error(ex.ToString());
					}
				}
			}
			catch (Exception ex)
			{
				Console.WriteLine(ex.ToString());
				Logger.Error(ex.ToString());
				throw;
			}
			finally
			{
				Listener.Close();
			}
		}

		private void ProcessMessageThread(object state)
		{
			var context = (HttpListenerContext)state;
			var request = context.Request;
			var response = context.Response;
			try
			{
				UriTemplateMatch templateMatch;
				var route = Routes.Find(request, out templateMatch);
				if (route != null)
				{
					var auth = Authentication.TryAuthorize(context, route);
					if (auth.Principal != null)
					{
						var ctx = new HttpThreadContex(request, response, templateMatch);
						ThreadContext.Request = ctx;
						ThreadContext.Response = ctx;
						Thread.CurrentPrincipal = auth.Principal;
						using (var stream = route.Handle(templateMatch, context))
						{
							if (stream.CanSeek)
								response.ContentLength64 = stream.Length;
							stream.CopyTo(response.OutputStream);
						}
					}
					else
					{
						response.ContentLength64 = Encoding.UTF8.GetByteCount(auth.Error);
						response.StatusCode = (int)auth.ResponseCode;
						using (var ms = new MemoryStream(Encoding.UTF8.GetBytes(auth.Error)))
							ms.CopyTo(response.OutputStream);
					}
				}
				else
				{
					var unknownRoute = "Unknown route " + request.RawUrl + " on method " + request.HttpMethod;
					response.ContentLength64 = Encoding.UTF8.GetByteCount(unknownRoute);
					using (var ms = new MemoryStream(Encoding.UTF8.GetBytes(unknownRoute)))
						ms.CopyTo(response.OutputStream);
				}
			}
			catch (Exception ex)
			{
				response.StatusCode = 500;
				response.ContentLength64 = Encoding.UTF8.GetByteCount(ex.Message);
				using (var ms = new MemoryStream(Encoding.UTF8.GetBytes(ex.Message)))
					ms.CopyTo(response.OutputStream);
				response.Close();
			}
		}
	}
}
