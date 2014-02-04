using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Reflection;
using System.Runtime.Serialization;
using System.ServiceModel.Web;
using System.Xml.Linq;
using NGS;
using NGS.DomainPatterns;
using NGS.Serialization;
using Revenj.Api;
using Revenj.Plugins.Server.Commands;
using Serialize.Linq.Nodes;

namespace Revenj.Plugins.Rest.Commands
{
	public static class Utility
	{
		internal static readonly ISerialization<object> PassThrough = new PassThroughSerialization();

		public static Type CheckDomainObject(IDomainModel domainModel, string name)
		{
			var type = domainModel.Find(name);
			if (type == null)
				Utility.ThrowError("Can't find domain object: {0}".With(name), HttpStatusCode.BadRequest);
			return type;
		}

		public static KeyValuePair<Type, Type> CheckCube(IDomainModel domainModel, string name)
		{
			var type = domainModel.Find(name);
			if (type == null)
				Utility.ThrowError("Can't find olap cube: {0}".With(name), HttpStatusCode.BadRequest);
			if (!typeof(IOlapCubeQuery).IsAssignableFrom(type))
				Utility.ThrowError("{0} is not an olap cube.".With(name), HttpStatusCode.BadRequest);
			//TODO ugly hack. fix later
			var prop = type.GetProperty("DataSource");
			var source = prop != null ? (Type)prop.GetValue(null, null) : null;
			return new KeyValuePair<Type, Type>(type, source);
		}

		public static Type CheckAggregateRoot(IDomainModel domainModel, string name)
		{
			var type = CheckDomainObject(domainModel, name);
			if (!typeof(IAggregateRoot).IsAssignableFrom(type))
				Utility.ThrowError("{0} is not an aggregate root".With(name), HttpStatusCode.BadRequest);
			return type;
		}

		public static void CheckIdentifiable(IDomainModel domainModel, string name)
		{
			var type = CheckDomainObject(domainModel, name);
			if (!typeof(IIdentifiable).IsAssignableFrom(type))
				Utility.ThrowError("{0} doesn't have URI".With(name), HttpStatusCode.BadRequest);
		}

		public static Type CheckDomainEvent(IDomainModel domainModel, string name)
		{
			var type = CheckDomainObject(domainModel, name);
			if (!typeof(IDomainEvent).IsAssignableFrom(type))
				Utility.ThrowError("{0} is not a domain event".With(name), HttpStatusCode.BadRequest);
			return type;
		}

		public static XElement ParseXml(Stream data)
		{
			if (data == null)
				return null;
			try
			{
				return XElement.Load(data);
			}
			catch (Exception ex)
			{
				throw new WebFaultException<string>(@"Sent data is not a valid Xml. 
Set Content-type header to correct format or fix sent data.
Error: {0}".With(ex.Message), HttpStatusCode.BadRequest);
			}
		}

		private static string EmptyInstanceString(this IWireSerialization serializer, Type target)
		{
			return SerializeToString(serializer, NGS.Utility.TemporaryResources.CreateRandomObject(target));
		}

		private static string SerializeToString(this IWireSerialization serializer, object instance)
		{
			using (var ms = new MemoryStream())
			{
				var ct = serializer.Serialize(instance, ThreadContext.Request.Accept, ms);
				ThreadContext.Response.ContentType = ct;
				var sr = new StreamReader(ms);
				return sr.ReadToEnd();
			}
		}

		public static object ParseObject(
			IWireSerialization serializer,
			Type type,
			Stream data,
			bool canCreate,
			IServiceLocator locator)
		{
			if (data == null)
			{
				if (canCreate == false)
					ThrowError(@"{0} must be provided. Example: 
{1}".With(type.FullName, serializer.EmptyInstanceString(type)), HttpStatusCode.BadRequest);
				try
				{
					return Activator.CreateInstance(type);
				}
				catch (Exception ex)
				{
					ThrowError(@"Can't create instance of {0}. Data must be provided. Error: {1}. Example: 
{2}".With(type.FullName, ex.Message, serializer.EmptyInstanceString(type)), HttpStatusCode.BadRequest);
				}
			}
			try
			{
				var sc = new StreamingContext(StreamingContextStates.All, locator);
				//TODO: objects deserialized here will have global scope access. Do OnDeserialized again later in scope
				return serializer.Deserialize(data, type, ThreadContext.Request.ContentType, sc);
			}
			catch (TargetInvocationException tie)
			{
				var ex = tie.InnerException ?? tie;
				throw new WebFaultException<string>(@"Can't deserialize {0}. Error: {1}. Example: 
{2}".With(type.FullName, ex.Message, serializer.EmptyInstanceString(type)), HttpStatusCode.BadRequest);
			}
			catch (Exception ex)
			{
				throw new WebFaultException<string>(@"Can't deserialize {0}. Error: {1}. Example: 
{2}".With(type.FullName, ex.Message, serializer.EmptyInstanceString(type)), HttpStatusCode.BadRequest);
			}
		}

		public static object SpecificationFromQuery(Type specType)
		{
			object instance = null;
			try
			{
				var queryParams = ThreadContext.Request.UriTemplateMatch.QueryParameters;
				var ctor = specType.GetConstructors().FirstOrDefault();
				instance = ctor.Invoke(ctor.GetParameters().Select(_ => (object)null).ToArray());
				foreach (var k in queryParams.AllKeys)
				{
					var prop = specType.GetProperty(k);
					if (prop != null)
						prop.SetValue(instance, Convert.ChangeType(queryParams[k], prop.PropertyType), null);
				}
			}
			catch (Exception ex)
			{
				ThrowError("Error creating object from query string. " + ex.Message, HttpStatusCode.BadRequest);
			}
			return instance;
		}

		public static object GenericSpecificationFromQuery(Type domainType)
		{
			try
			{
				var arg = new Dictionary<string, List<KeyValuePair<int, object>>>();
				var specType = typeof(GenericSpecification<,>).MakeGenericType(domainType, typeof(object));
				//TODO better match parameters. allow > != ~ etc...
				var queryParams = ThreadContext.Request.UriTemplateMatch.QueryParameters;
				foreach (var k in queryParams.AllKeys)
				{
					var prop = domainType.GetProperty(k);
					if (prop != null)
					{
						List<KeyValuePair<int, object>> list;
						if (!arg.TryGetValue(k, out list))
							arg[k] = list = new List<KeyValuePair<int, object>>();
						list.Add(new KeyValuePair<int, object>(0, Convert.ChangeType(queryParams[k], prop.PropertyType)));
					}
				}
				return Activator.CreateInstance(specType, PassThrough, arg);
			}
			catch (Exception ex)
			{
				ThrowError("Error creating object from query string. " + ex.Message, HttpStatusCode.BadRequest);
			}
			return null;
		}

		public static MessageFormat GetFormat(string type)
		{
			type = (type ?? string.Empty).ToLowerInvariant().Trim();
			return
				type == "application/json"
				? MessageFormat.Json
				: type == "application/x-protobuf"
					? MessageFormat.ProtoBuf
					: MessageFormat.Xml;
		}

		public static MessageFormat GetIncomingFormat()
		{
			return GetFormat(ThreadContext.Request.ContentType);
		}

		public static void ThrowError(string message, HttpStatusCode code)
		{
			ThreadContext.Response.StatusCode = code;
			//TODO: should return text/plain, but that doesn't work
			switch (ThreadContext.Request.Accept)
			{
				case "application/json":
					ThreadContext.Response.ContentType = "application/json";
					break;
				default:
					ThreadContext.Response.ContentType = "application/xml; charset=\"utf-8\"";
					break;
			}
#if MONO
			throw new System.Exception(message);
#else
			throw new WebFaultException<string>(message, code);
#endif
		}

		public static Dictionary<string, bool> ParseOrder(string order)
		{
			if (string.IsNullOrEmpty(order))
				return null;
			return
				(from o in order.Split(',')
				 let word = o.Trim(new[] { '-', '+' })
				 select new { word, asc = !o.StartsWith("-") })
				.ToDictionary(it => it.word, it => it.asc);
		}

		public static void ParseLimitOffset(
			string limitQuery,
			string offsetQuery,
			out int? limit,
			out int? offset)
		{
			int x;
			if (int.TryParse(limitQuery, out x))
				limit = x;
			else
				limit = null;
			if (int.TryParse(offsetQuery, out x))
				offset = x;
			else
				offset = null;
		}

		public static object ParseGenericSpecification<TFormat>(IWireSerialization serializer, Type domainType, Stream data)
		{
			var genSer = serializer.GetSerializer<TFormat>();
			var specType = typeof(GenericSpecification<,>).MakeGenericType(domainType, typeof(TFormat));
			try
			{
				var arg = Utility.ParseObject(serializer, typeof(Dictionary<string, List<KeyValuePair<int, TFormat>>>), data, true, null);
				return Activator.CreateInstance(specType, genSer, arg);
			}
			catch (Exception ex)
			{
				if (ex.InnerException != null)
					ex = ex.InnerException;
				var specArg = new Dictionary<string, List<KeyValuePair<int, TFormat>>>();
				specArg["URI"] = new List<KeyValuePair<int, TFormat>>(new[] { new KeyValuePair<int, TFormat>(1, genSer.Serialize("1001")) });
				throw new WebFaultException<string>(@"Error deserializing specification. " + ex.Message + @"
Example: 
" + serializer.SerializeToString(specArg), HttpStatusCode.BadRequest);
			}
		}

		public static object ParseExpressionSpecification(IWireSerialization serializer, Type domainType, Stream data)
		{
			try
			{
				var expressionNode = serializer.Deserialize<LambdaExpressionNode>(data, ThreadContext.Request.ContentType);
				return Activator.CreateInstance(typeof(SpecificationFromNode<>).MakeGenericType(domainType), expressionNode);
			}
			catch (Exception ex)
			{
				if (ex.InnerException != null)
					ex = ex.InnerException;
				Utility.ThrowError(@"Error deserializing expression. " + ex.Message, HttpStatusCode.BadRequest);
			}
			return null;
		}
	}
}
