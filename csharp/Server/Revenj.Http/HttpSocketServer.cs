using System;
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
		private static int KeepAliveTimeout = 3000 * 1000;

		private readonly IServiceProvider Locator;
		private readonly Socket Socket;
		private readonly Routes Routes;
		private readonly HttpAuth Authentication;

		private readonly LinkedList<RequestInfo>[] OpenRequests;

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
			var maxLen = ConfigurationManager.AppSettings["Revenj.ContentLengthLimit"];
			if (!string.IsNullOrEmpty(maxLen)) MessageSizeLimit = int.Parse(maxLen);
			var ka = ConfigurationManager.AppSettings["Revenj.KeepAliveTimeout"];
			if (!string.IsNullOrEmpty(ka)) KeepAliveTimeout = int.Parse(ka) * 1000;
			Routes = new Routes(locator);
			var customAuth = ConfigurationManager.AppSettings["CustomAuth"];
			if (!string.IsNullOrEmpty(customAuth))
			{
				var authType = Type.GetType(customAuth);
				if (!typeof(HttpAuth).IsAssignableFrom(authType))
					throw new ConfigurationErrorsException("Custom auth does not inherit from HttpAuth. Please inherit from " + typeof(HttpAuth).FullName);
				Authentication = locator.Resolve<HttpAuth>(authType);
			}
			else Authentication = locator.Resolve<HttpAuth>();
			OpenRequests = new LinkedList<RequestInfo>[Environment.ProcessorCount * 2];
			for (int i = 0; i < OpenRequests.Length; i++)
			{
				OpenRequests[i] = new LinkedList<RequestInfo>();
				var t = new Thread(ProcessSockets);
				t.Start(i);
			}
		}

		public void Run()
		{
			try
			{
				var backlog = ConfigurationManager.AppSettings["Revenj.Backlog"];
				if (backlog != null)
					Socket.Listen(int.Parse(backlog));
				else
					Socket.Listen(1000);
				TraceSource.TraceEvent(TraceEventType.Start, 1002);
				var threadIndex = 0;
				Console.WriteLine("Http server running");
				while (true)
				{
					try
					{
						var socket = Socket.Accept();
						if (socket.Connected)
						{
							socket.Blocking = true;
							var os = OpenRequests[threadIndex++];
							lock (os)
								os.AddLast(new RequestInfo(socket));
						}
						threadIndex = threadIndex % OpenRequests.Length;
					}
					catch (SocketException ex)
					{
						Console.WriteLine(ex.ToString());
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
			public int LastTicks;
			public bool Alive;

			public RequestInfo(Socket socket)
			{
				this.Socket = socket;
				HasData = true;
				Alive = true;
			}

			public void EndRequest()
			{
				HasData = false;
				Alive = false;
			}
		}

		private void ProcessSockets(object state)
		{
			var index = (int)state;
			var queue = OpenRequests[index];
			var requests = new RequestInfo[1024];
			var total = 0;
			var ctx = new HttpSocketContext("http://127.0.0.1/", MessageSizeLimit);
			ThreadContext.Request = ctx;
			ThreadContext.Response = ctx;
			while (true)
			{
				try
				{
					var curTicks = Environment.TickCount;
					lock (queue)
					{
						if (requests.Length < queue.Count)
							requests = new RequestInfo[queue.Count];
						if (queue.Count > 0)
						{
							var cur = queue.First;
							do
							{
								var next = cur.Next;
								if (!cur.Value.Alive)
									queue.Remove(cur);
								cur = next;
							} while (cur != null);
							queue.CopyTo(requests, 0);
						}
						total = queue.Count;
					}
					if (total == 0)
						Thread.Sleep(1);
					for (int i = 0; i < total; i++)
					{
						var req = requests[i];
						var socket = req.Socket;
						try
						{
							if (!req.HasData)
							{
								req.HasData = socket.Poll(0, SelectMode.SelectRead);
								req.Alive = req.LastTicks > curTicks;
							}
							IPrincipal previousPrincipal = null;
							ctx.Reset();
							while (req.HasData && (req.Alive = ctx.Parse(socket)))
							{
								RouteMatch match;
								var route = Routes.Find(ctx.HttpMethod, ctx.RawUrl, ctx.AbsolutePath, out match);
								if (route == null)
								{
									req.EndRequest();
									var unknownRoute = "Unknown route " + ctx.RawUrl + " on method " + ctx.HttpMethod;
									ctx.ReturnError(socket, 404, unknownRoute, false);
									break;
								}
								var auth = Authentication.TryAuthorize(ctx.GetRequestHeader("authorization"), ctx.RawUrl, route);
								if (auth.Principal != null)
								{
									ctx.ForRouteWithAuth(match, auth.Principal);
									if (previousPrincipal != auth.Principal)
										Thread.CurrentPrincipal = previousPrincipal = auth.Principal;
									using (var stream = route.Handle(match.OrderedArgs, ctx.Stream))
									{
										var keepAlive = ctx.Return(stream, socket);
										if (keepAlive)
										{
											if (ctx.Pipeline) continue;
											else if (socket.Connected)
											{
												req.HasData = socket.Available > 0;
												req.LastTicks = Environment.TickCount + KeepAliveTimeout;
												break;
											}
										}
										req.EndRequest();
										break;
									}
								}
								else if (auth.SendAuthenticate)
								{
									req.EndRequest();
									ctx.AddHeader("WWW-Authenticate", MissingBasicAuth);
									ctx.ReturnError(socket, (int)auth.ResponseCode, auth.Error, true);
									break;
								}
								else
								{
									req.EndRequest();
									ctx.ReturnError(socket, (int)auth.ResponseCode, auth.Error, true);
									break;
								}
							}
						}
						catch (SecurityException sex)
						{
							req.EndRequest();
							try { ctx.ReturnError(socket, (int)HttpStatusCode.Forbidden, sex.Message, true); }
							catch (Exception ex)
							{
								Console.WriteLine(ex.Message);
								TraceSource.TraceEvent(TraceEventType.Error, 5404, "{0}", ex);
							}
						}
						catch (ActionNotSupportedException anse)
						{
							req.EndRequest();
							try { ctx.ReturnError(socket, 404, anse.Message, true); }
							catch (Exception ex)
							{
								Console.WriteLine(ex.Message);
								TraceSource.TraceEvent(TraceEventType.Error, 5404, "{0}", ex);
							}
						}
						catch (Exception ex)
						{
							req.EndRequest();
							Console.WriteLine(ex.ToString());
							TraceSource.TraceEvent(TraceEventType.Error, 5403, "{0}", ex);
							try { ctx.ReturnError(socket, 500, ex.Message, false); }
							catch (Exception ex2)
							{
								Console.WriteLine(ex2.Message);
								TraceSource.TraceEvent(TraceEventType.Error, 5404, "{0}", ex2);
							}
						}
					}
					for (var i = 0; i < total; i++)
					{
						var req = requests[i];
						if (!req.Alive)
						{
							try { req.Socket.Close(); }
							catch { }
						}
					}
				}
				catch (Exception e)
				{
					Console.WriteLine(e.ToString());
					TraceSource.TraceEvent(TraceEventType.Error, 5403, "{0}", e);
				}
			}
			for (var i = 0; i < total; i++)
			{
				try { requests[i].Socket.Close(); }
				catch { }
			}
		}
	}
}