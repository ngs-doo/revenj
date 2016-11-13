using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Threading.Tasks;
using Revenj.DomainPatterns;
#if !PORTABLE
using Serialize.Linq.Nodes;
#endif

namespace Revenj
{
	internal class DomainProxy : IDomainProxy
	{
		private const string URL = "Domain.svc/";
		private readonly HttpClient Http;
		private readonly IApplicationProxy Application;

		public DomainProxy(
			HttpClient http,
			IApplicationProxy application)
		{
			this.Http = http;
			this.Application = application;
		}

		private class GetArgument
		{
			public string Name;
			public string[] Uri;
		}

		public Task<T[]> Find<T>(IEnumerable<string> uris)
			where T : class, IIdentifiable
		{
			if (uris == null)
				throw new ArgumentNullException("uris can't be null");
			var arr = uris.ToArray();
			if (arr.Length == 0)
				return Task.Factory.StartNew(() => new T[0]);
			if (arr.Any(it => it == null))
				throw new ArgumentNullException("Uri can't be null");
			var encodedUris = string.Join(",", arr);
			if (encodedUris.Length > 300 || arr.Any(it => it.Contains(",")))
			{
				var arg = new GetArgument { Name = typeof(T).FullName, Uri = arr };
				return Application.Post<GetArgument, T[]>("GetDomainObject", arg);
			}
			return Http.Get<T[]>(
				URL + "find/" + typeof(T).FullName + "/" + Uri.EscapeUriString(encodedUris),
				new[] { HttpStatusCode.OK });
		}

		public Task<T[]> Search<T>(ISpecification<T> specification, int? limit, int? offset, IDictionary<string, bool> order)
			where T : class, ISearchable
		{
			var domainName = typeof(T).FullName;

			var limitOffsetOrder = string.Empty;
			if (limit != null)
				limitOffsetOrder += "limit=" + limit;
			if (offset != null)
				limitOffsetOrder += (limitOffsetOrder.Length > 0 ? "&" : string.Empty) + "offset=" + offset;
			if (order != null && order.Count > 0)
			{
				limitOffsetOrder +=
					(limitOffsetOrder.Length > 0 ? "&" : string.Empty)
					+ "order="
					+ string.Join(
						",",
						order.Select(it => (it.Value ? string.Empty : "-") + it.Key).ToArray());
			}
			if (limitOffsetOrder.Length > 0)
				limitOffsetOrder = "?" + limitOffsetOrder;

			if (specification == null)
				return Http.Get<T[]>(
					URL + "search/" + domainName + limitOffsetOrder,
					new[] { HttpStatusCode.OK });

			var specName = ProxyHelper.GetSpecificationDomainName(specification);

			var gs = specification as GenericSpecification<T>;
			if (gs != null)
				return
					Http.Call<GenericSpecification<T>, T[]>(
						URL + "search-generic/" + domainName + limitOffsetOrder,
						"PUT",
						gs,
						new[] { HttpStatusCode.OK });
#if !PORTABLE
			var es = specification as ExpressionSpecification<T>;
			if (es != null)
				return
					Http.Call<LambdaExpressionNode, T[]>(
						URL + "search-expression/" + domainName + limitOffsetOrder,
						"PUT",
						es.Expression,
						new[] { HttpStatusCode.OK });
#endif

			return Http.Call<ISpecification<T>, T[]>(
				URL + "search/" + domainName + "/" + specName + limitOffsetOrder,
				"POST",
				specification,
				new[] { HttpStatusCode.OK });
		}

		public Task<long> Count<T>(ISpecification<T> specification)
			where T : class, ISearchable
		{
			var domainName = typeof(T).FullName;
			if (specification == null)
				return Http.Get<long>(
					URL + "count/" + domainName,
					new[] { HttpStatusCode.OK });

			var gs = specification as GenericSpecification<T>;
			if (gs != null)
				return
					Http.Call<GenericSpecification<T>, long>(
						URL + "count-generic/" + domainName,
						"PUT",
						gs,
						new[] { HttpStatusCode.OK });
#if !PORTABLE
			var es = specification as ExpressionSpecification<T>;
			if (es != null)
				return
					Http.Call<LambdaExpressionNode, long>(
						URL + "count-expression/" + domainName,
						"PUT",
						es.Expression,
						new[] { HttpStatusCode.OK });
#endif

			var specName = ProxyHelper.GetSpecificationDomainName(specification);
			return Http.Call<ISpecification<T>, long>(
				URL + "count/" + domainName + "/" + specName,
				"POST",
				specification,
				new[] { HttpStatusCode.OK });
		}

		public Task<string> Submit<T>(T domainEvent)
			where T : class, IDomainEvent
		{
			if (domainEvent == null)
				throw new ArgumentNullException("domainEvent can't be null");
			domainEvent.Validate();
			return
				Http.Call<T, string>(
					URL + "submit/" + typeof(T).FullName,
					"POST",
					domainEvent,
					new[] { HttpStatusCode.Created });
		}

		public Task<TAggregate> Submit<TEvent, TAggregate>(TEvent domainEvent, string uri)
			where TEvent : class, IDomainEvent<TAggregate>
			where TAggregate : class, IAggregateRoot
		{
			if (domainEvent == null)
				throw new ArgumentNullException("domainEvent can't be null");
			if (string.IsNullOrEmpty(uri))
				throw new ArgumentNullException("uri can't be empty");
			domainEvent.Validate();
			return
				Http.Call<TEvent, TAggregate>(
					URL + "submit/" + typeof(TEvent).FullName.Replace('+', '/') + "/" + uri,
					"POST",
					domainEvent,
					new[] { HttpStatusCode.Created });
		}
	}
}
