using System;
using System.IO;
using System.Linq.Expressions;
using System.Reflection;
using System.Runtime.Serialization;
using Revenj.Api;
using Revenj.Serialization;
using Revenj.Utility;

namespace Revenj.Http
{
	public class RouteHandler
	{
		internal readonly string Service;
		internal readonly string Template;
		internal readonly UriPattern Pattern;
		internal readonly bool IsAsync;
		private readonly bool WithStream;
		private readonly int TotalParams;
		internal readonly string Url;
		private readonly Func<string[], IRequestContext, IResponseContext, Stream, ChunkedMemoryStream, Stream> Invocation;

		public RouteHandler(
			string service,
			string template,
			object instance,
			bool isAsync,
			MethodInfo method,
			IServiceProvider locator,
			IWireSerialization serialization)
		{
			this.Service = "/" + service.ToLowerInvariant();
			this.Template = template;
			this.Pattern = new UriPattern(template == "*" ? "/*" : template);
			this.IsAsync = isAsync;
			this.Url = Service + template;
			var methodParams = method.GetParameters();
			TotalParams = methodParams.Length;
			WithStream = TotalParams != 0 && methodParams[TotalParams - 1].ParameterType == typeof(Stream);
			var lamParams = new ParameterExpression[5];
			lamParams[0] = Expression.Parameter(typeof(string[]), "strArr");
			lamParams[1] = Expression.Parameter(typeof(IRequestContext), "request");
			lamParams[2] = Expression.Parameter(typeof(IResponseContext), "response");
			lamParams[3] = Expression.Parameter(typeof(Stream), "input");
			lamParams[4] = Expression.Parameter(typeof(ChunkedMemoryStream), "output");
			var expArgs = new Expression[TotalParams];
			var argInd = 0;
			for (int i = 0; i < TotalParams; i++)
			{
				var mp = methodParams[i];
				if (mp.ParameterType == typeof(IRequestContext))
					expArgs[i] = lamParams[1];
				else if (mp.ParameterType == typeof(IResponseContext))
					expArgs[i] = lamParams[2];
				else if (i < TotalParams - 1 || !WithStream)
					expArgs[i] = Expression.ArrayIndex(lamParams[0], Expression.Constant(argInd++));
				else
					expArgs[i] = lamParams[3];
			}
			var mce = Expression.Call(Expression.Constant(instance, instance.GetType()), method, expArgs);
			if (typeof(IHtmlView).IsAssignableFrom(method.ReturnType))
			{
				mce = Expression.Call(null, RenderFunc.Method, mce, lamParams[2], lamParams[4]);
			}
			else if (!typeof(Stream).IsAssignableFrom(method.ReturnType) && method.ReturnType != typeof(void))
			{
				var ws = Expression.Constant(serialization);
				mce = Expression.Call(null, SerializeFunc.Method, ws, lamParams[1], lamParams[2], mce, lamParams[4]);
			}
			var lambda = Expression.Lambda<Func<string[], IRequestContext, IResponseContext, Stream, ChunkedMemoryStream, Stream>>(mce, lamParams);
			Invocation = lambda.Compile();
		}

		private static Func<IWireSerialization, Stream, Type, string, StreamingContext, object> DeserializeFunc = Deserialize;
		private static Func<IWireSerialization, IRequestContext, IResponseContext, object, ChunkedMemoryStream, Stream> SerializeFunc = Serialize;
		private static Func<IHtmlView, IResponseContext, ChunkedMemoryStream, Stream> RenderFunc = Render;

		public static object Deserialize(
			IWireSerialization serialization,
			Stream input,
			Type target,
			string contentType,
			StreamingContext context)
		{
			return serialization.Deserialize(input, target, contentType, context);
		}

		public static Stream Serialize(
			IWireSerialization serialization,
			IRequestContext request,
			IResponseContext response,
			object result,
			ChunkedMemoryStream outputStream)
		{
			outputStream.Reset();
			response.ContentType = serialization.Serialize(result, request.Accept, outputStream);
			response.ContentLength = outputStream.Position;
			outputStream.Position = 0;
			return outputStream;
		}

		public static Stream Render(IHtmlView html, IResponseContext response, ChunkedMemoryStream stream)
		{
			stream.Reset();
			response.ContentType = "text/html; charset=UTF-8";
			var sw = stream.GetWriter();
			html.Render(sw);
			sw.Flush();
			response.ContentLength = stream.Position;
			stream.Position = 0;
			return stream;
		}

		internal Stream Handle(
			string[] args,
			IRequestContext request,
			IResponseContext response,
			Stream inputStream,
			ChunkedMemoryStream outputStream)
		{
			return Invocation(args, request, response, inputStream, outputStream);
		}
	}
}
