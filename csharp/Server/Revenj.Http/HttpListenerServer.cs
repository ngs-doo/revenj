using System;
using System.Configuration;
using System.Diagnostics;
using System.Linq;
using System.Net;
using System.Security;
using System.ServiceModel;
using System.Text;
using System.Threading;
using Revenj.Api;
using Revenj.DomainPatterns;
using Revenj.Utility;

namespace Revenj.Http
{
	internal sealed class HttpListenerServer
	{
		private static readonly TraceSource TraceSource = new TraceSource("Revenj.Server");
		private static readonly string MissingBasicAuth = "Basic realm=\"" + Environment.MachineName + "\"";

		private readonly HttpListener Listener;
		private readonly Routes Routes;
		private readonly HttpAuth Authentication;

		public HttpListenerServer(IServiceProvider locator)
		{
			Listener = new HttpListener();
			Listener.IgnoreWriteExceptions = true;
			foreach (string key in ConfigurationManager.AppSettings.Keys)
			{
				if (key.StartsWith("HttpAddress", StringComparison.InvariantCultureIgnoreCase))
					Listener.Prefixes.Add(ConfigurationManager.AppSettings[key]);
			}
			if (Listener.Prefixes.Count == 0)
				Listener.Prefixes.Add("http://*:8999/");
			Routes = locator.Resolve<Routes>();
			var customAuth = ConfigurationManager.AppSettings["CustomAuth"];
			if (!string.IsNullOrEmpty(customAuth))
			{
				var authType = Type.GetType(customAuth);
				if (!typeof(HttpAuth).IsAssignableFrom(authType))
					throw new ConfigurationErrorsException("Custom auth does not inherit from HttpAuth. Please inherit from " + typeof(HttpAuth).FullName);
				Authentication = locator.Resolve<HttpAuth>(authType);
			}
			else Authentication = locator.Resolve<HttpAuth>();
		}

		public void Run()
		{
			var prefixes = Listener.Prefixes.ToArray();
			try
			{
				Listener.Start();
				TraceSource.TraceEvent(TraceEventType.Start, 1002);
				ThreadPool.SetMinThreads(32 + Environment.ProcessorCount * 3, 32 + Environment.ProcessorCount * 3);
				Console.WriteLine("Server running on:");
				foreach (var url in prefixes)
					Console.WriteLine(url);
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
						TraceSource.TraceEvent(TraceEventType.Error, 5401, "{0}", ex);
					}
				}
			}
			catch (HttpListenerException ex)
			{
				if (ex.Message == "Access is denied")
				{
					Console.WriteLine("Unable to start listener on specified port. Change port or grant access to it.");
					if (prefixes.Length == 1)
						Console.WriteLine("Permission can be allowed with: netsh http add urlacl url=" + prefixes[0] + " user=" + Environment.MachineName + "\\" + Environment.UserName);
					else
						Console.WriteLine("Use 'netsh http add urlacl' to give access permission");
				}
				Console.WriteLine(ex.ToString());
				TraceSource.TraceEvent(TraceEventType.Error, 5402, "{0}", ex);
				throw;
			}
			catch (Exception ex)
			{
				Console.WriteLine(ex.ToString());
				TraceSource.TraceEvent(TraceEventType.Error, 5402, "{0}", ex);
				throw;
			}
			finally
			{
				TraceSource.TraceEvent(TraceEventType.Stop, 1002);
				Listener.Close();
			}
		}

		private readonly ThreadLocal<ChunkedMemoryStream> LocalStream =
			new ThreadLocal<ChunkedMemoryStream>(() => ChunkedMemoryStream.Static());

		private void ProcessMessageThread(object state)
		{
			var context = (HttpListenerContext)state;
			var request = context.Request;
			var response = context.Response;
			try
			{
				RouteHandler route;
				var routeMatch = Routes.Find(request.HttpMethod, request.RawUrl, request.Url.AbsolutePath, out route);
				if (routeMatch != null)
				{
					var match = routeMatch.Value;
					var auth = Authentication.TryAuthorize(context.Request.Headers["Authorization"], context.Request.RawUrl, route);
					if (auth.Principal != null)
					{
						var ctx = new HttpListenerContex(request, response, match, auth.Principal);
						ThreadContext.Request = ctx;
						ThreadContext.Response = ctx;
						Thread.CurrentPrincipal = auth.Principal;
						using (var stream = route.Handle(match.OrderedArgs, ctx, ctx, context.Request.InputStream, LocalStream.Value))
						{
							var cms = stream as ChunkedMemoryStream;
							if (cms != null)
							{
								response.ContentLength64 = cms.Length;
								cms.CopyTo(response.OutputStream);
							}
							else if (stream != null)
							{
								if (stream.CanSeek)
									response.ContentLength64 = stream.Length;
								stream.CopyTo(response.OutputStream);
							}
							else
							{
								response.ContentType = null;
								response.ContentLength64 = 0;
							}
						}
					}
					else if (auth.SendAuthenticate)
					{
						context.Response.AddHeader("WWW-Authenticate", MissingBasicAuth);
						ReturnError(response, (int)auth.ResponseCode, auth.Error);
					}
					else ReturnError(response, (int)auth.ResponseCode, auth.Error);
				}
				else
				{
					var unknownRoute = "Unknown route " + request.RawUrl + " on method " + request.HttpMethod;
					ReturnError(response, 404, unknownRoute);
				}
			}
			catch (SecurityException sex)
			{
				ReturnError(response, (int)HttpStatusCode.Forbidden, sex.Message);
			}
			catch (ActionNotSupportedException anse)
			{
				ReturnError(response, 404, anse.Message);
			}
			catch (Exception ex)
			{
				TraceSource.TraceEvent(TraceEventType.Error, 5403, "{0}", ex);
				ReturnError(response, 500, ex.Message);
			}
			finally
			{
				response.Close();
			}
		}

		private void ReturnError(HttpListenerResponse response, int status, string message)
		{
			// Response is disposed before ReturnError is called when sent request is invalid.
			// This inner try-catch prevents application crash because of that.
			try
			{
				response.StatusCode = status;
				response.ContentType = "text/plain; charset=\"utf-8\"";
				var bytes = Encoding.UTF8.GetBytes(message);
				response.ContentLength64 = bytes.Length;
				response.OutputStream.Write(bytes, 0, bytes.Length);
			}
			catch (Exception ex)
			{
				TraceSource.TraceEvent(TraceEventType.Error, 5404, "{0}", ex);
			}
		}
	}
}
