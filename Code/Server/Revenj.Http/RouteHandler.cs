using System;
using System.Collections.Generic;
using System.IO;
using System.Linq.Expressions;
using System.Net;
using System.Reflection;

namespace Revenj.Http
{
	public class RouteHandler
	{
		public readonly UriTemplate Template;
		internal readonly string Service;
		private readonly UriPattern Pattern;
		private readonly bool WithStream;
		private readonly int TotalParams;
		private readonly Dictionary<string, int> UppercaseArgumentOrder = new Dictionary<string, int>();
		private readonly Dictionary<string, int> ArgumentOrder = new Dictionary<string, int>();
		private readonly Func<string[], Stream, Stream> Invocation;

		public RouteHandler(string service, string template, object instance, MethodInfo method)
		{
			this.Service = "/" + service.ToLowerInvariant();
			this.Template = new UriTemplate(template == "*" ? "/*" : template);
			this.Pattern = new UriPattern(template == "*" ? "/*" : template);
			var methodParams = method.GetParameters();
			TotalParams = methodParams.Length;
			WithStream = TotalParams != 0 && methodParams[TotalParams - 1].ParameterType == typeof(Stream);
			var lamParams = new ParameterExpression[2];
			lamParams[0] = Expression.Parameter(typeof(string[]), "strArr");
			lamParams[1] = Expression.Parameter(typeof(Stream), "input");
			var expArgs = new Expression[TotalParams];
			for (int i = 0; i < TotalParams; i++)
			{
				UppercaseArgumentOrder[methodParams[i].Name.ToUpperInvariant()] = i;
				ArgumentOrder[methodParams[i].Name] = i;
				if (i < TotalParams - 1 || !WithStream)
					expArgs[i] = Expression.ArrayIndex(lamParams[0], Expression.Constant(i));
				else
					expArgs[i] = lamParams[1];
			}
			var mce = Expression.Call(Expression.Constant(instance, instance.GetType()), method, expArgs);
			var lambda = Expression.Lambda<Func<string[], Stream, Stream>>(mce, lamParams);
			Invocation = lambda.Compile();
		}

		public Stream Handle(UriTemplateMatch match, HttpListenerContext listener)
		{
			var args = new string[TotalParams];
			for (int i = 0; i < match.BoundVariables.Count; i++)
				args[UppercaseArgumentOrder[match.BoundVariables.GetKey(i)]] = match.BoundVariables[i];

			// Fixes bug in Mono's TemplateMatch
#if MONO
			var last = match.BoundVariables.Count - 1;
			if (last >= 0 && args[last].Length > 0 && args[last][0] == '/')
				args[last] = args[last].Substring(1);
#endif

			return Invocation(args, listener.Request.InputStream);
		}

		public Stream Handle(HttpListenerContext listener)
		{
			var args = new string[TotalParams];
			var match = Pattern.Parse(listener.Request.Url.PathAndQuery);
			foreach (var kv in match)
				args[ArgumentOrder[kv.Key]] = kv.Value;
			return Invocation(args, listener.Request.InputStream);
		}
	}
}
