using Microsoft.AspNetCore.Http;
using System.Linq;
using Revenj.DomainPatterns;
using Revenj.Serialization;
using Revenj.Utility;
using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Text;
using System.Xml.Linq;
using System.Runtime.Serialization;
using System.Reflection;
using Microsoft.Extensions.Primitives;
using Revenj.Plugins.Server.Commands;

namespace Revenj.Plugins.AspNetCore.Commands
{
	internal static class Utility
	{
		internal static readonly ISerialization<object> PassThrough = new PassThroughSerialization();

		public static Try<Type> CheckDomainObject(IDomainModel domainModel, string name, HttpResponse response)
		{
			var type = domainModel.Find(name);
			if (type == null)
				return Try.Fail<Type>("Can't find domain object: " + name, response);
			return type;
		}

		public static Try<Type> CheckDomainObject(IDomainModel domainModel, Try<Type> parentType, string name, HttpResponse response)
		{
			if (parentType.IsFailure) return Try<Type>.Error;
			if (name == null) return Try<Type>.Empty;
			var type = name.Contains("+") ? domainModel.Find(name) : domainModel.Find(parentType.Result.FullName + "+" + name);
			if (type == null)
				return Try.Fail<Type>("Can't find domain object: " + name, response);
			return type;
		}

		public static Try<KeyValuePair<Type, Type>> CheckCube(IDomainModel domainModel, string name, HttpResponse response)
		{
			var type = domainModel.Find(name);
			if (type == null)
				return Try.Fail<KeyValuePair<Type, Type>>("Can't find olap cube: " + name, response);
			var findImpl = type.GetInterfaces().FirstOrDefault(it => it.IsGenericType && it.GetGenericTypeDefinition() == typeof(IOlapCubeQuery<>));
			if (findImpl == null)
				return Try.Fail<KeyValuePair<Type, Type>>(name + " is not an olap cube.", response);
			return new KeyValuePair<Type, Type>(type, findImpl.GetGenericArguments()[0]);
		}

		public static Try<Type> CheckAggregateRoot(IDomainModel domainModel, string name, HttpResponse response)
		{
			var type = CheckDomainObject(domainModel, name, response);
			if (type.IsSuccess && !typeof(IAggregateRoot).IsAssignableFrom(type.Result))
				return Try.Fail<Type>(name + " is not an aggregate root", response);
			return type;
		}

		public static Try<Type> CheckIdentifiable(IDomainModel domainModel, string name, HttpResponse response)
		{
			var type = CheckDomainObject(domainModel, name, response);
			if (type.IsSuccess && !typeof(IIdentifiable).IsAssignableFrom(type.Result))
				return Try.Fail<Type>(name + " doesn't have URI", response);
			return type;
		}

		public static Try<Type> CheckDomainEvent(IDomainModel domainModel, string name, HttpResponse response)
		{
			var type = CheckDomainObject(domainModel, name, response);
			if (type.IsSuccess && !typeof(IDomainEvent).IsAssignableFrom(type.Result))
				return Try.Fail<Type>(name + " is not a domain event", response);
			return type;
		}

		public static Try<Type> CheckEvent(IDomainModel domainModel, string name, HttpResponse response)
		{
			var type = CheckDomainObject(domainModel, name, response);
			if (type.IsSuccess && !typeof(IEvent).IsAssignableFrom(type.Result))
				return Try.Fail<Type>(name + " is not an event (domain event or a command)", response);
			return type;
		}

		public static Try<XElement> ParseXml(Stream data, HttpResponse response)
		{
			if (data == null)
				return Try<XElement>.Empty;
			try
			{
				return XElement.Load(data);
			}
			catch (Exception ex)
			{
				return Try.Fail<XElement>(@"Sent data is not a valid Xml. 
Set Content-type header to correct format or fix sent data.
Error: " + ex.Message, response);
			}
		}

		private static string EmptyInstanceString(this IWireSerialization serializer, Type target, HttpRequest request, HttpResponse response)
		{
			try
			{
				return SerializeToString(serializer, TemporaryResources.CreateRandomObject(target), request, response);
			}
			catch (Exception ex)
			{
				return Exceptions.DebugMode
					? "Error creating instance example: " + ex.Message
					: "Error creating instance example ;(";
			}
		}

		private static string SerializeToString(this IWireSerialization serializer, object instance, HttpRequest request, HttpResponse response)
		{
			StringValues headers;
			string accept = null;
			if (request.Headers.TryGetValue("accept", out headers) && headers.Count > 0)
				accept = headers[0];
			using (var cms = ChunkedMemoryStream.Create())
			{
				var ct = serializer.Serialize(instance, accept, cms);
				response.ContentType = ct;
				return cms.GetReader().ReadToEnd();
			}
		}

		public static Try<object> ParseObject(
			IWireSerialization serializer,
			Try<Type> maybeType,
			Stream data,
			bool canCreate,
			IServiceProvider locator,
			HttpRequest request,
			HttpResponse response)
		{
			if (maybeType.IsFailure) return Try<object>.Error;
			var type = maybeType.Result;
			if (type == null) return Try<object>.Empty;
			if (data == null)
			{
				if (canCreate == false)
					return Try.Fail<object>(@"{0} must be provided. Example: 
{1}".With(type.FullName, serializer.EmptyInstanceString(type, request, response)), response);
				try
				{
					return Activator.CreateInstance(type);
				}
				catch (Exception ex)
				{
					return Try.Fail<object>(@"Can't create instance of {0}. Data must be provided. Error: {1}. Example: 
{2}".With(type.FullName, ex.Message, serializer.EmptyInstanceString(type, request, response)), response);
				}
			}
			try
			{
				var sc = new StreamingContext(StreamingContextStates.All, locator);
				//TODO: objects deserialized here will have global scope access. Do OnDeserialized again later in scope
				return serializer.Deserialize(data, type, request.ContentType, sc);
			}
			catch (TargetInvocationException tie)
			{
				var ex = tie.InnerException ?? tie;
				return Try.Fail<object>(@"Can't deserialize {0}. Error: {1}. Example: 
{2}".With(type.FullName, ex.Message, serializer.EmptyInstanceString(type, request, response)), response);
			}
			catch (Exception ex)
			{
				return Try.Fail<object>(@"Can't deserialize {0}. Error: {1}. Example: 
{2}".With(type.FullName, ex.Message, serializer.EmptyInstanceString(type, request, response)), response);
			}
		}

		public static Try<object> ObjectFromQuery(Try<Type> specType, HttpRequest request, HttpResponse response)
		{
			if (specType.IsFailure) return Try<object>.Error;
			var type = specType.Result;
			if (type == null) return Try<object>.Empty;
			try
			{
				var queryParams = request.Query;
				var ctor = type.GetConstructors().FirstOrDefault();
				var instance = ctor.Invoke(ctor.GetParameters().Select(_ => (object)null).ToArray());
				foreach (var kv in queryParams)
				{
					var prop = type.GetProperty(kv.Key);
					if (prop != null)
						prop.SetValue(instance, Convert.ChangeType(kv.Value[0], prop.PropertyType), null);
				}
				return instance;
			}
			catch (Exception ex)
			{
				return Try.Fail<object>("Error creating object from query string. " + ex.Message, response);
			}
		}

		public static Try<object> GenericSpecificationFromQuery(Try<Type> domainType, HttpRequest request, HttpResponse response)
		{
			if (domainType.IsFailure) return Try<object>.Error;
			var type = domainType.Result;
			try
			{
				var arg = new Dictionary<string, List<KeyValuePair<int, object>>>();
				var specType = typeof(GenericSpecification<,>).MakeGenericType(type, typeof(object));
				//TODO better match parameters. allow > != ~ etc...
				var queryParams = request.Query;
				foreach (var kv in queryParams)
				{
					var prop = type.GetProperty(kv.Key);
					if (prop != null)
					{
						List<KeyValuePair<int, object>> list;
						if (!arg.TryGetValue(kv.Key, out list))
							arg[kv.Key] = list = new List<KeyValuePair<int, object>>();
						list.Add(new KeyValuePair<int, object>(0, Convert.ChangeType(kv.Value[0], prop.PropertyType)));
					}
				}
				return Activator.CreateInstance(specType, PassThrough, arg);
			}
			catch (Exception ex)
			{
				return Try.Fail<object>("Error creating object from query string. " + ex.Message, response);
			}
		}

		public static MessageFormat GetIncomingFormat(HttpRequest request)
		{
			var type = (request.ContentType ?? string.Empty).ToLowerInvariant().Trim();
			return
				type == "application/json"
				? MessageFormat.Json
				: type == "application/x-protobuf"
					? MessageFormat.ProtoBuf
					: MessageFormat.Xml;
		}

		public static List<KeyValuePair<string, bool>> ParseOrder(string order)
		{
			if (string.IsNullOrEmpty(order))
				return null;
			return
				(from o in order.Split(',')
				 let word = o.Trim(new[] { '-', '+' })
				 select new KeyValuePair<string, bool>(word, !o.StartsWith("-")))
				 .ToList();
		}		

		public static Try<object> ParseGenericSpecification(this IWireSerialization serialization, Try<Type> target, Stream data, HttpRequest request, HttpResponse response)
		{
			if (target.IsFailure) return Try<object>.Error;
			switch (GetIncomingFormat(request))
			{
				case MessageFormat.Json:
					return ParseGenericSpecification<string>(serialization, target, data, request, response);
				case MessageFormat.ProtoBuf:
					return ParseGenericSpecification<MemoryStream>(serialization, target, data, request, response);
				default:
					return ParseGenericSpecification<XElement>(serialization, target, data, request, response);
			}
		}

		public static Try<object> ParseGenericSpecification<TFormat>(this IWireSerialization serializer, Try<Type> domainType, Stream data, HttpRequest request, HttpResponse response)
		{
			if (domainType.IsFailure) return Try<object>.Error;
			var genSer = serializer.GetSerializer<TFormat>();
			var specType = typeof(GenericSpecification<,>).MakeGenericType(domainType.Result, typeof(TFormat));
			try
			{
				var arg = ParseObject(serializer, typeof(Dictionary<string, List<KeyValuePair<int, TFormat>>>), data, true, null, request, response);
				if (arg.IsFailure) return Try<object>.Error;
				return Activator.CreateInstance(specType, genSer, arg.Result);
			}
			catch (Exception ex)
			{
				if (ex.InnerException != null)
					ex = ex.InnerException;
				var specArg = new Dictionary<string, List<KeyValuePair<int, TFormat>>>();
				specArg["URI"] = new List<KeyValuePair<int, TFormat>>(new[] { new KeyValuePair<int, TFormat>(1, genSer.Serialize("1001")) });
				return Try.Fail<object>(@"Error deserializing specification. " + ex.Message + @"
Example: 
" + serializer.SerializeToString(specArg, request, response), response);
			}
		}
		/*
		public static Try<object> ParseExpressionSpecification(IWireSerialization serializer, Try<Type> domainType, Stream data)
		{
			if (domainType.IsFailure) return Try<object>.Error;
			try
			{
				var expressionNode = serializer.Deserialize<LambdaExpressionNode>(data, ThreadContext.Request.ContentType);
				return Activator.CreateInstance(typeof(SpecificationFromNode<>).MakeGenericType(domainType.Result), expressionNode);
			}
			catch (Exception ex)
			{
				if (ex.InnerException != null)
					ex = ex.InnerException;
				return @"Error deserializing expression. " + ex.Message;
			}
		}
		*/
		public static bool? ReturnInstance(string argument, HttpRequest request)
		{
			var result = argument;
			if (result == null)
			{
				StringValues header;
				if (request.Headers.TryGetValue("x-revenj-result", out header))
					result = header[0];
			}
			return result == "instance" ? true : result == "uri" ? (bool?)false : null;
		}

		public static bool IncludeCount(string argument, HttpRequest request)
		{
			var result = argument;
			if (result == null)
			{
				StringValues header;
				if (request.Headers.TryGetValue("x-revenj-includecount", out header))
					result = header[0];
			}
			return result == "yes";
		}

		public static void WriteError(this HttpResponse response, string message, HttpStatusCode code)
		{
			response.StatusCode = (int)code;
			response.ContentType = "text/plain; charset=UTF-8";
			response.Body.Write(Encoding.UTF8.GetBytes(message));
		}
	}
}
