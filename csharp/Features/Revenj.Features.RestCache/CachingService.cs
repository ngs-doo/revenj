using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Text;
using Revenj.Api;
using Revenj.DomainPatterns;
using Revenj.Security;
using Revenj.Serialization;
using Revenj.Utility;

namespace Revenj.Features.RestCache
{
	internal static class CachingService
	{
		private static readonly ConcurrentDictionary<Type, Func<string, Stream>> SingleLookups = new ConcurrentDictionary<Type, Func<string, Stream>>();
		private static readonly ConcurrentDictionary<Type, Func<string[], bool, Stream>> CollectionLookups = new ConcurrentDictionary<Type, Func<string[], bool, Stream>>();

		internal static Stream ReadFromCache(Type type, string uri, IServiceProvider locator)
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

		internal static Stream ReadFromCache(Type type, string[] uri, bool matchOrder, IServiceProvider locator)
		{
			Func<string[], bool, Stream> converter;
			if (!CollectionLookups.TryGetValue(type, out converter))
			{
				var al = (IAggregateLookup)Activator.CreateInstance(typeof(AggregateLookup<>).MakeGenericType(type), locator);
				converter = al.Find;
				CollectionLookups.TryAdd(type, converter);
			}
			return converter(uri, matchOrder);
		}

		interface IAggregateLookup
		{
			Stream Find(string uri);
			Stream Find(string[] uris, bool matchOrder);
		}

		class AggregateLookup<T> : IAggregateLookup
			where T : IAggregateRoot
		{
			private readonly IDataCache<T> Cache;
			private readonly IWireSerialization Serialization;
			private readonly IPermissionManager Permissions;

			class UriComparer : IComparer<T>
			{
				private readonly Dictionary<string, int> Uri;

				public UriComparer(string[] uri)
				{
					Uri = new Dictionary<string, int>();
					for (int i = 0; i < uri.Length; i++)
						Uri[uri[i]] = i;
				}

				public int Compare(T x, T y)
				{
					return Uri[x.URI] - Uri[y.URI];
				}
			}

			public AggregateLookup(IServiceProvider locator)
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

			public Stream Find(string[] uri, bool matchOrder)
			{
				if (!Permissions.CanAccess(typeof(T)))
					return Explain("You don't have permission to access: " + typeof(T));
				var aggs = Cache.Find(uri);
				var filtered = Permissions.ApplyFilters(aggs);
				if (matchOrder && filtered.Length > 1)
					Array.Sort(filtered, new UriComparer(uri));
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
