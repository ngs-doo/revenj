using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Runtime.Serialization;
using System.Xml.Linq;
using Revenj.Api;
using Revenj.DomainPatterns;
using Revenj.Plugins.Server.Commands;
using Revenj.Serialization;
using Revenj.Utility;
using Serialize.Linq.Nodes;

namespace Revenj.Plugins.Rest.Commands
{
	internal static class Utility
	{
		internal static readonly ISerialization<object> PassThrough = new PassThroughSerialization();

		public static Either<Type> CheckDomainObject(IDomainModel domainModel, string name)
		{
			var type = domainModel.Find(name);
			if (type == null)
				return "Can't find domain object: " + name;
			return type;
		}

		public static Either<Type> CheckDomainObject(IDomainModel domainModel, Either<Type> parentType, string name)
		{
			if (parentType.IsFailure) return parentType;
			if (name == null) return Either<Type>.Empty;
			var type = name.Contains("+") ? domainModel.Find(name) : domainModel.Find(parentType.Result.FullName + "+" + name);
			if (type == null)
				return "Can't find domain object: " + name;
			return type;
		}

		public static Either<KeyValuePair<Type, Type>> CheckCube(IDomainModel domainModel, string name)
		{
			var type = domainModel.Find(name);
			if (type == null)
				return "Can't find olap cube: " + name;
			var findImpl = type.GetInterfaces().FirstOrDefault(it => it.IsGenericType && it.GetGenericTypeDefinition() == typeof(IOlapCubeQuery<>));
			if (findImpl == null)
				return name + " is not an olap cube.";
			return new KeyValuePair<Type, Type>(type, findImpl.GetGenericArguments()[0]);
		}

		public static Either<Type> CheckAggregateRoot(IDomainModel domainModel, string name)
		{
			var type = CheckDomainObject(domainModel, name);
			if (type.IsSuccess && !typeof(IAggregateRoot).IsAssignableFrom(type.Result))
				return name + " is not an aggregate root";
			return type;
		}

		public static Either<Type> CheckIdentifiable(IDomainModel domainModel, string name)
		{
			var type = CheckDomainObject(domainModel, name);
			if (type.IsSuccess && !typeof(IIdentifiable).IsAssignableFrom(type.Result))
				return name + " doesn't have URI";
			return type;
		}

		public static Either<Type> CheckDomainEvent(IDomainModel domainModel, string name)
		{
			var type = CheckDomainObject(domainModel, name);
			if (type.IsSuccess && !typeof(IDomainEvent).IsAssignableFrom(type.Result))
				return name + " is not a domain event";
			return type;
		}

		public static Either<XElement> ParseXml(Stream data)
		{
			if (data == null)
				return Either<XElement>.Succes(null);
			try
			{
				return XElement.Load(data);
			}
			catch (Exception ex)
			{
				return @"Sent data is not a valid Xml. 
Set Content-type header to correct format or fix sent data.
Error: " + ex.Message;
			}
		}

		private static string EmptyInstanceString(this IWireSerialization serializer, Type target)
		{
			try
			{
				return SerializeToString(serializer, Revenj.Utility.TemporaryResources.CreateRandomObject(target));
			}
			catch (Exception ex)
			{
				return Exceptions.DebugMode
					? "Error creating instance example: " + ex.Message
					: "Error creating instance example ;(";
			}
		}

		private static string SerializeToString(this IWireSerialization serializer, object instance)
		{
			using (var cms = ChunkedMemoryStream.Create())
			{
				var ct = serializer.Serialize(instance, ThreadContext.Request.Accept, cms);
				ThreadContext.Response.ContentType = ct;
				return cms.GetReader().ReadToEnd();
			}
		}

		public static Either<object> ParseObject(
			IWireSerialization serializer,
			Either<Type> maybeType,
			Stream data,
			bool canCreate,
			IServiceProvider locator)
		{
			if (maybeType.IsFailure) return maybeType.Error;
			var type = maybeType.Result;
			if (type == null) return Either<object>.Empty;
			if (data == null)
			{
				if (canCreate == false)
					return @"{0} must be provided. Example: 
{1}".With(type.FullName, serializer.EmptyInstanceString(type));
				try
				{
					return Activator.CreateInstance(type);
				}
				catch (Exception ex)
				{
					return @"Can't create instance of {0}. Data must be provided. Error: {1}. Example: 
{2}".With(type.FullName, ex.Message, serializer.EmptyInstanceString(type));
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
				return @"Can't deserialize {0}. Error: {1}. Example: 
{2}".With(type.FullName, ex.Message, serializer.EmptyInstanceString(type));
			}
			catch (Exception ex)
			{
				return @"Can't deserialize {0}. Error: {1}. Example: 
{2}".With(type.FullName, ex.Message, serializer.EmptyInstanceString(type));
			}
		}

		public static Either<object> ObjectFromQuery(Either<Type> specType)
		{
			if (specType.IsFailure) return specType.Error;
			var type = specType.Result;
			if (type == null) return Either<object>.Empty;
			try
			{
				var queryParams = ThreadContext.Request.UriTemplateMatch.QueryParameters;
				var ctor = type.GetConstructors().FirstOrDefault();
				var instance = ctor.Invoke(ctor.GetParameters().Select(_ => (object)null).ToArray());
				foreach (var k in queryParams.AllKeys)
				{
					var prop = type.GetProperty(k);
					if (prop != null)
						prop.SetValue(instance, Convert.ChangeType(queryParams[k], prop.PropertyType), null);
				}
				return instance;
			}
			catch (Exception ex)
			{
				return "Error creating object from query string. " + ex.Message;
			}
		}

		public static Either<object> GenericSpecificationFromQuery(Either<Type> domainType)
		{
			if (domainType.IsFailure) return domainType.Error;
			var type = domainType.Result;
			try
			{
				var arg = new Dictionary<string, List<KeyValuePair<int, object>>>();
				var specType = typeof(GenericSpecification<,>).MakeGenericType(type, typeof(object));
				//TODO better match parameters. allow > != ~ etc...
				var queryParams = ThreadContext.Request.UriTemplateMatch.QueryParameters;
				foreach (var k in queryParams.AllKeys)
				{
					var prop = type.GetProperty(k);
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
				return "Error creating object from query string. " + ex.Message;
			}
		}

		public static MessageFormat GetIncomingFormat()
		{
			var type = (ThreadContext.Request.ContentType ?? string.Empty).ToLowerInvariant().Trim();
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

		public static Either<object> ParseGenericSpecification(this IWireSerialization serialization, Either<Type> target, Stream data)
		{
			if (target.IsFailure) return target.Error;
			switch (GetIncomingFormat())
			{
				case MessageFormat.Json:
					return ParseGenericSpecification<string>(serialization, target, data);
				case MessageFormat.ProtoBuf:
					return ParseGenericSpecification<MemoryStream>(serialization, target, data);
				default:
					return ParseGenericSpecification<XElement>(serialization, target, data);
			}
		}

		public static Either<object> ParseGenericSpecification<TFormat>(this IWireSerialization serializer, Either<Type> domainType, Stream data)
		{
			if (domainType.IsFailure) return domainType.Error;
			var genSer = serializer.GetSerializer<TFormat>();
			var specType = typeof(GenericSpecification<,>).MakeGenericType(domainType.Result, typeof(TFormat));
			try
			{
				var arg = ParseObject(serializer, typeof(Dictionary<string, List<KeyValuePair<int, TFormat>>>), data, true, null);
				if (arg.IsFailure) return arg.Error;
				return Activator.CreateInstance(specType, genSer, arg.Result);
			}
			catch (Exception ex)
			{
				if (ex.InnerException != null)
					ex = ex.InnerException;
				var specArg = new Dictionary<string, List<KeyValuePair<int, TFormat>>>();
				specArg["URI"] = new List<KeyValuePair<int, TFormat>>(new[] { new KeyValuePair<int, TFormat>(1, genSer.Serialize("1001")) });
				return @"Error deserializing specification. " + ex.Message + @"
Example: 
" + serializer.SerializeToString(specArg);
			}
		}

		public static Either<object> ParseExpressionSpecification(IWireSerialization serializer, Either<Type> domainType, Stream data)
		{
			if (domainType.IsFailure) return domainType.Error;
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

		public static bool? ReturnInstance(string argument, IRequestContext request)
		{
			var result = argument ?? request.GetHeaderLowercase("x-revenj-result");
			return result == "instance" ? true : result == "uri" ? (bool?)false : null;
		}

		public static bool IncludeCount(string argument, IRequestContext request)
		{
			var result = argument ?? request.GetHeaderLowercase("x-revenj-includecount");
			return result == "yes";
		}
	}
}
