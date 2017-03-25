using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Configuration;
using System.Diagnostics;
using System.Net;
using System.Net.Sockets;
using System.Security;
using System.Security.Principal;
using System.ServiceModel;
using System.Threading;
using Revenj.Api;
using Revenj.DomainPatterns;

namespace Revenj.Http
{
	internal sealed class HttpSocketServer
	{
		private static readonly TraceSource TraceSource = new TraceSource("Revenj.Server");
		private static readonly string MissingBasicAuth = "Basic realm=\"" + Environment.MachineName + "\"";

		private static int MessageSizeLimit = 8 * 1024 * 1024;
		private static int KeepAliveTimeout = 30 * 1000;

		static HttpSocketServer()
		{
			var maxLen = ConfigurationManager.AppSettings["Revenj.ContentLengthLimit"];
			if (!string.IsNullOrEmpty(maxLen)) MessageSizeLimit = int.Parse(maxLen);
			var ka = ConfigurationManager.AppSettings["Revenj.KeepAliveLimit"];
			if (!string.IsNullOrEmpty(ka)) KeepAliveTimeout = int.Parse(ka);
		}

		private readonly IServiceProvider Locator;
		private readonly Socket Socket;
		private readonly HttpAuth Authentication;

		private readonly ThreadLocal<HttpSocketContext> Context;
		private readonly BlockingCollection<RequestInfo> Requests;

		public HttpSocketServer(IServiceProvider locator)
		{
			this.Locator = locator;
			var endpoints = new List<IPEndPoint>();
			var networkType = AddressFamily.InterNetworkV6;
			foreach (string key in ConfigurationManager.AppSettings.Keys)
			{
				if (key.StartsWith("HttpAddress", StringComparison.InvariantCultureIgnoreCase))
				{
					var addr = new Uri(ConfigurationManager.AppSettings[key]);
					IPAddress ip;
					if (!IPAddress.TryParse(addr.Host, out ip))
					{
						var ips = Dns.GetHostAddresses(addr.Host);
						foreach (var i in ips)
							if (i.AddressFamily == networkType)
								endpoints.Add(new IPEndPoint(i, addr.Port));
						if (endpoints.Count == 0 && ips.Length > 0)
						{
							if (ips[0].AddressFamily == AddressFamily.InterNetwork)
							{
								networkType = AddressFamily.InterNetwork;
								foreach (var i in ips)
									if (i.AddressFamily == networkType)
										endpoints.Add(new IPEndPoint(i, addr.Port));
							}
						}
					}
					else endpoints.Add(new IPEndPoint(ip, addr.Port));
				}
			}
			if (endpoints.Count == 0)
			{
				Console.WriteLine("Http address not found in config. Starting IPv6 on all interfaces");
				endpoints.Add(new IPEndPoint(Socket.OSSupportsIPv6 ? IPAddress.IPv6Any : IPAddress.Any, 8999));
			}
			else if (endpoints.FindAll(it => it.AddressFamily == AddressFamily.InterNetwork).Count == endpoints.Count)
			{
				networkType = AddressFamily.InterNetwork;
			}
			else if (endpoints.FindAll(it => it.AddressFamily == AddressFamily.InterNetworkV6).Count != endpoints.Count)
			{
				throw new ConfigurationErrorsException(@"Unable to setup configuration for both IPv4 and IPv6. Use either only IPv4 or IPv6. 
Please check settings: " + string.Join(", ", endpoints));
			}
			Socket = new Socket(networkType, SocketType.Stream, ProtocolType.Tcp);
			//Socket.SetSocketOption(SocketOptionLevel.IPv6, (SocketOptionName)27, 0);
			foreach (var ep in endpoints)
			{
				Socket.Bind(ep);
				Console.WriteLine("Bound to: " + ep);
			}
			var customAuth = ConfigurationManager.AppSettings["CustomAuth"];
			if (!string.IsNullOrEmpty(customAuth))
			{
				var authType = Type.GetType(customAuth);
				if (!typeof(HttpAuth).IsAssignableFrom(authType))
					throw new ConfigurationErrorsException("Custom auth does not inherit from HttpAuth. Please inherit from " + typeof(HttpAuth).FullName);
				Authentication = locator.Resolve<HttpAuth>(authType);
			}
			else Authentication = locator.Resolve<HttpAuth>();
			var routes = locator.Resolve<Routes>();
			Context = new ThreadLocal<HttpSocketContext>(() => new HttpSocketContext("http://127.0.0.1/", MessageSizeLimit, routes));
			var ca = ConfigurationManager.AppSettings["Revenj.HttpCapacity"];
			Requests = !string.IsNullOrEmpty(ca)
				? new BlockingCollection<RequestInfo>((int)Math.Log(int.Parse(ca), 2))
				: new BlockingCollection<RequestInfo>();
		}

		public void Run()
		{
			try
			{
				var backlog = ConfigurationManager.AppSettings["Revenj.Backlog"];
				if (backlog != null)
					Socket.Listen(int.Parse(backlog));
				else
					Socket.Listen(10000);
				TraceSource.TraceEvent(TraceEventType.Start, 1002);
				var minth = ConfigurationManager.AppSettings["Revenj.MinThreads"];
				var maxth = ConfigurationManager.AppSettings["Revenj.MaxThreads"];
				var minThreads = !string.IsNullOrEmpty(minth) ? int.Parse(minth) : 128 + Environment.ProcessorCount * 3;
				var maxThreads = !string.IsNullOrEmpty(maxth) ? int.Parse(maxth) : 128 + Environment.ProcessorCount * 3;
				ThreadPool.SetMinThreads(minThreads, maxThreads);
				var socketLoops = ConfigurationManager.AppSettings["Revenj.SocketLoops"];
				var loops = !string.IsNullOrEmpty(socketLoops) ? int.Parse(socketLoops) : Environment.ProcessorCount;
				for (int i = 0; i < loops; i++)
				{
					var thread = new Thread(SocketLoop);
					thread.Name = "Socket loop: " + (i + 1);
					thread.Start();
				}
				var ctx = Context.Value;
				while (true)
				{
					try
					{
						var socket = Socket.Accept();
						if (socket.Connected)
						{
							socket.Blocking = true;
							socket.ReceiveTimeout = 1;
							if (!Requests.TryAdd(new RequestInfo(socket)))
							{
								ctx.ReturnError(socket, 503);
								Thread.Yield();
							}
						}
					}
					catch (SocketException ex)
					{
						TraceSource.TraceEvent(TraceEventType.Error, 5401, "{0}", ex);
					}
				}
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
				Socket.Shutdown(SocketShutdown.Both);
			}
		}

		class RequestInfo
		{
			public readonly Socket Socket;
			public bool HasData;
			public int TimeoutAt;

			public RequestInfo(Socket socket)
			{
				this.Socket = socket;
				HasData = socket.Available > 0;
				if (!HasData)
					TimeoutAt = Environment.TickCount + KeepAliveTimeout;
			}
			public RequestInfo Processed()
			{
				this.HasData = Socket.Available > 0;
				TimeoutAt = Environment.TickCount + KeepAliveTimeout;
				return this;
			}
			public RequestInfo Skipped()
			{
				this.HasData = Socket.Poll(0, SelectMode.SelectRead);
				return this;
			}
		}

		private void SocketLoop(object state)
		{
			var ctx = Context.Value;
			var principal = Thread.CurrentPrincipal;
			ThreadContext.Request = ctx;
			ThreadContext.Response = ctx;
			var resetEvent = new ManualResetEvent(false);
			while (true)
			{
				var request = Requests.Take();
				try
				{
					var socket = request.Socket;
					if (request.HasData)
					{
						ctx.Reset();
						ProcessAllMessages(request, ctx, principal, resetEvent, true);
					}
					else if (socket.Available > 0 || request.TimeoutAt < Environment.TickCount)
					{
						ctx.Reset();
						ProcessAllMessages(request, ctx, principal, resetEvent, true);
					}
					else
					{
						if (!Requests.TryAdd(request.Skipped()))
							request.Socket.Close();
						Thread.Yield();
					}
				}
				catch (Exception ex)
				{
					LogError(request.Socket, ctx, ex);
				}
			}
		}

		struct ThreadArgs
		{
			public readonly RequestInfo Request;
			public readonly HttpSocketContext Context;
			public readonly ManualResetEvent ResetEvent;
			public readonly HttpAuth.AuthorizeOrError Auth;
			public readonly RouteHandler Route;
			public readonly RouteMatch Match;
			public ThreadArgs(RequestInfo request, HttpSocketContext context, ManualResetEvent resetEvent, HttpAuth.AuthorizeOrError auth, RouteHandler route, RouteMatch match)
			{
				this.Request = request; ;
				this.Context = context;
				this.ResetEvent = resetEvent;
				this.Auth = auth;
				this.Route = route;
				this.Match = match;
			}
		}

		private void ProcessAllMessages(RequestInfo request, HttpSocketContext ctx, IPrincipal principal, ManualResetEvent resetEvent, bool canReschedule)
		{
			RouteMatch? routeMatch;
			RouteHandler route;
			var socket = request.Socket;
			while (ctx.Parse(socket, out routeMatch, out route))
			{
				var match = routeMatch.Value;
				var auth = Authentication.TryAuthorize(ctx.GetRequestHeader("authorization"), ctx.RawUrl, route);
				if (auth.Principal != null)
				{
					if (canReschedule && route.IsAsync)
					{
						resetEvent.Reset();
						ThreadPool.QueueUserWorkItem(ProcessInThread, new ThreadArgs(request, ctx, resetEvent, auth, route, match));
						Thread.Yield();
						resetEvent.WaitOne();
						return;
					}
					else
					{
						ctx.ForRouteWithAuth(match, auth.Principal);
						if (principal != auth.Principal)
							Thread.CurrentPrincipal = principal = auth.Principal;
						using (var stream = route.Handle(match.OrderedArgs, ctx, ctx, ctx.InputStream, ctx.OutputStream))
						{
							var keepAlive = ctx.Return(stream, socket, !canReschedule);
							if (keepAlive)
							{
								if (ctx.Pipeline) continue;
								else if (socket.Connected && Requests.TryAdd(request.Processed())) return;
							}
							socket.Close();
							return;
						}
					}
				}
				else
				{
					CheckAuth(socket, auth, ctx);
					return;
				}
			}
		}

		private void LogError(Socket socket, HttpSocketContext ctx, Exception ex)
		{
			var sex = ex as SecurityException;
			var ans = ex as ActionNotSupportedException;
			if (sex != null)
			{
				try { ctx.ReturnError(socket, (int)HttpStatusCode.Forbidden, sex.Message, true); }
				catch (Exception e)
				{
					TraceSource.TraceEvent(TraceEventType.Error, 5404, "{0}", e);
				}
			}
			else if (ans != null)
			{
				try { ctx.ReturnError(socket, 404, ans.Message, true); }
				catch (Exception e)
				{
					TraceSource.TraceEvent(TraceEventType.Error, 5404, "{0}", e);
				}
			}
			else
			{
				TraceSource.TraceEvent(TraceEventType.Error, 5403, "{0}", ex);
				try { ctx.ReturnError(socket, 500, ex.Message, false); }
				catch (Exception ex2)
				{
					TraceSource.TraceEvent(TraceEventType.Error, 5404, "{0}", ex2);
				}
			}
		}

		private void CheckAuth(Socket socket, HttpAuth.AuthorizeOrError auth, HttpSocketContext ctx)
		{
			if (auth.SendAuthenticate)
			{
				ctx.AddHeader("WWW-Authenticate", MissingBasicAuth);
				ctx.ReturnError(socket, (int)auth.ResponseCode, auth.Error, true);
			}
			else
			{
				ctx.ReturnError(socket, (int)auth.ResponseCode, auth.Error, true);
			}
		}

		private void ProcessInThread(object state)
		{
			var arg = (ThreadArgs)state;
			var request = arg.Request;
			var socket = request.Socket;
			var ctx = Context.Value;
			var semaphore = arg.ResetEvent;
			try
			{
				ctx.CopyFrom(arg.Context);
				arg.ResetEvent.Set();
				var principal = arg.Auth.Principal;
				ThreadContext.Request = ctx;
				ThreadContext.Response = ctx;
				Thread.CurrentPrincipal = principal;
				ctx.ForRouteWithAuth(arg.Match, principal);
				using (var stream = arg.Route.Handle(arg.Match.OrderedArgs, ctx, ctx, ctx.InputStream, ctx.OutputStream))
				{
					var keepAlive = ctx.Return(stream, socket, true);
					if (!keepAlive)
					{
						socket.Close();
						return;
					}
					else if (!ctx.Pipeline)
					{
						if (!Requests.TryAdd(request.Processed()))
							socket.Close();
						return;
					}
				}
				ProcessAllMessages(request, ctx, principal, semaphore, false);
			}
			catch (Exception ex)
			{
				LogError(socket, ctx, ex);
			}
		}
	}
}