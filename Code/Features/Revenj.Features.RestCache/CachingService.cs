using System;
using System.Collections.Concurrent;
using System.IO;
using System.Net;
using System.Text;
using NGS.DomainPatterns;
using NGS.Security;
using NGS.Serialization;
using NGS.Utility;
using Revenj.Api;

namespace Revenj.Features.RestCache
{
	internal static class CachingService
	{
		private static readonly ConcurrentDictionary<Type, Func<string, Stream>> SingleLookups = new ConcurrentDictionary<Type, Func<string, Stream>>();
		private static readonly ConcurrentDictionary<Type, Func<string[], Stream>> CollectionLookups = new ConcurrentDictionary<Type, Func<string[], Stream>>();

		internal static Stream ReadFromCache(Type type, string uri, IServiceLocator locator)
		{
			Func<string, Stream> converter;
			if (!SingleLookups.TryGetValue(type, out converter))
			{
				var al = (IAggregateLookup)Activator.CreateInstance(typeof(AggregateLookup<>).MakeGenericType(type), locator);
				converter = al.Find;
				SingleLookups.TryAdd(type, converter);
			}
			return converter(uri);
		}

		internal static Stream ReadFromCache(Type type, string[] uri, IServiceLocator locator)
		{
			Func<string[], Stream> converter;
			if (!CollectionLookups.TryGetValue(type, out converter))
			{
				var al = (IAggregateLookup)Activator.CreateInstance(typeof(AggregateLookup<>).MakeGenericType(type), locator);
				converter = al.Find;
				CollectionLookups.TryAdd(type, converter);
			}
			return converter(uri);
		}

		interface IAggregateLookup
		{
			Stream Find(string uri);
			Stream Find(string[] uris);
		}

		class AggregateLookup<T> : IAggregateLookup
			where T : IAggregateRoot
		{
			private readonly IDataCache<T> Cache;
			private readonly IWireSerialization Serialization;
			private readonly IPermissionManager Permissions;

			public AggregateLookup(IServiceLocator locator)
			{
				Cache = locator.Resolve<IDataCache<T>>();
				Serialization = locator.Resolve<IWireSerialization>();
				Permissions = locator.Resolve<IPermissionManager>();
			}

			private static Stream Explain(string message)
			{
				return new MemoryStream(Encoding.UTF8.GetBytes(message));
			}

			public Stream Find(string uri)
			{
				if (!Permissions.CanAccess(typeof(T)))
					return Explain("You don't have permission to access: " + typeof(T));
				var response = ThreadContext.Response;
				var aggs = Cache.Find(new[] { uri });
				var filtered = Permissions.ApplyFilters(aggs);
				if (filtered.Length == 1)
				{
					response.StatusCode = HttpStatusCode.OK;
					var cms = ChunkedMemoryStream.Create();
					var ct = Serialization.Serialize(filtered[0], ThreadContext.Request.Accept, cms);
					response.ContentType = ct;
					cms.Position = 0;
					return cms;
				}
				response.StatusCode = HttpStatusCode.NotFound;
				return Explain("Can't find " + typeof(T).FullName + " with Uri: " + uri);
			}

			public Stream Find(string[] uri)
			{
				if (!Permissions.CanAccess(typeof(T)))
					return Explain("You don't have permission to access: " + typeof(T));
				var aggs = Cache.Find(uri);
				var filtered = Permissions.ApplyFilters(aggs);
				var response = ThreadContext.Response;
				response.StatusCode = HttpStatusCode.OK;
				var cms = ChunkedMemoryStream.Create();
				var ct = Serialization.Serialize(filtered, ThreadContext.Request.Accept, cms);
				response.ContentType = ct;
				cms.Position = 0;
				return cms;
			}
		}
	}
}
