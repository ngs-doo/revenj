using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Threading.Tasks;
using Revenj.DomainPatterns;
using System.IO;
#if !PORTABLE
using Serialize.Linq.Nodes;
#endif

namespace Revenj
{
	internal class ReportingProxy : IReportingProxy
	{
		private const string URL = "Reporting.svc/";
		private readonly HttpClient Http;
		private readonly IApplicationProxy Application;

		public ReportingProxy(
			HttpClient http,
			IApplicationProxy application)
		{
			this.Http = http;
			this.Application = application;
		}

		public Task<T> Populate<T>(IReport<T> report)
		{
			return
				Http.Call<IReport<T>, T>(
					URL + "report/" + typeof(T).DeclaringType.FullName,
					"PUT",
					report,
					new[] { HttpStatusCode.OK });
		}

		public Task<Stream> CreateReport<T>(T report, string templater)
		{
			return
				Http.Call<T>(
					URL + "report/" + typeof(T).FullName + "/" + templater,
					"PUT",
					report,
					new[] { HttpStatusCode.Created },
					"application/octet-stream");
		}

		private static string BuildOlapArguments(
			IEnumerable<string> dimensions,
			IEnumerable<string> facts,
			IDictionary<string, bool> order)
		{
			var query = string.Empty;
			if (dimensions != null && dimensions.Any())
				query = "dimensions=" + string.Join(",", dimensions.ToArray());
			if (facts != null && facts.Any())
				query += (query.Length > 0 ? "&" : string.Empty) + "facts=" + string.Join(",", facts.ToArray());
			if (query.Length == 0)
				throw new ArgumentException("At least one dimension or fact is required");
			if (order != null && order.Any())
				query += "&order=" + string.Join(",", order.Select(it => (!it.Value ? "-" : string.Empty) + it.Key).ToArray());
			return query;
		}

		public Task<Stream> OlapCube<TCube, TSpecification>(
			TSpecification specification,
			string templater,
			IEnumerable<string> dimensions,
			IEnumerable<string> facts,
			IDictionary<string, bool> order)
		{
			var specName = typeof(TSpecification).FullName.StartsWith(typeof(TCube).FullName)
				? typeof(TSpecification).Name
				: typeof(TSpecification).FullName.Replace('+', '.');
			var args = BuildOlapArguments(dimensions, facts, order);
			return
				Http.Call<TSpecification>(
					URL + "olap/" + typeof(TCube).FullName + "/" + templater + "?specification=" + specName + (args.Length > 0 ? "&" + args : string.Empty),
					"PUT",
					specification,
					new[] { HttpStatusCode.Created },
					"application/octet-stream");
		}

		public Task<Stream> OlapCube<T>(
			string templater,
			IEnumerable<string> dimensions,
			IEnumerable<string> facts,
			IDictionary<string, bool> order)
		{
			return
				Http.Get(
					URL + "olap/" + typeof(T).FullName + "/" + templater + "?" + BuildOlapArguments(dimensions, facts, order),
					new[] { HttpStatusCode.Created },
					"application/octet-stream");
		}

		class HistoryArg
		{
			public string Name;
			public string[] Uri;
		}

		public Task<IHistory<T>[]> GetHistory<T>(IEnumerable<string> uris)
			where T : class, IAggregateRoot
		{
			if (uris == null)
				throw new ArgumentNullException("uris can't be null");
			var arr = uris.ToArray();
			if (arr.Any(it => it == null))
				throw new ArgumentNullException("Uri can't be null");
			return arr.Length == 1
				? Http.Get<History<T>>(
					URL + "history/" + typeof(T).FullName + "/" + Uri.EscapeUriString(arr[0]),
					new[] { HttpStatusCode.OK }).ContinueWith(r => new IHistory<T>[] { r.Result })
				: Application.Post<HistoryArg, History<T>[]>(
					"GetRootHistory",
					new HistoryArg { Name = typeof(T).FullName, Uri = arr },
					new[] { HttpStatusCode.OK }).ContinueWith(r => (IHistory<T>[])r.Result);
		}


		public Task<Stream> FindTemplater<T>(string file, string uri)
			where T : class, IIdentifiable
		{
			if (string.IsNullOrEmpty(file))
				throw new ArgumentException("file not specified");
			if (uri == null)
				throw new ArgumentNullException("uri not specified");
			return
				Http.Get(
					URL + "templater/" + file + "/" + typeof(T).FullName + "/" + Uri.EscapeUriString(uri),
					new[] { HttpStatusCode.OK },
					"application/octet-stream");
		}

		public Task<Stream> SearchTemplater<T>(string file, ISpecification<T> specification)
			where T : class, ISearchable
		{
			if (string.IsNullOrEmpty(file))
				throw new ArgumentException("file not specified");
			if (specification == null)
				return Http.Get(
					URL + "templater/" + file + "/" + typeof(T).FullName,
					new[] { HttpStatusCode.OK },
					"application/octet-stream");
#if !PORTABLE
			var es = specification as ExpressionSpecification<T>;
			if (es != null)
				return Http.Call<LambdaExpressionNode>(
					URL + "templater-expression/" + file + "/" + typeof(T).FullName,
					"PUT",
					es.Expression,
					new[] { HttpStatusCode.OK },
					"application/octet-stream");
#endif
			return
				Http.Call<ISpecification<T>>(
					URL + "templater/" + file + "/" + specification.GetType().FullName.Replace('+', '/'),
					"POST",
					specification,
					new[] { HttpStatusCode.OK },
					"application/octet-stream");
		}
	}
}
