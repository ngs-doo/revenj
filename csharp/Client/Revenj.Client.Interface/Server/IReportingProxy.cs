using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using Revenj.DomainPatterns;

namespace Revenj
{
	public interface IReportingProxy
	{
		Task<TResult> Populate<TResult>(IReport<TResult> report);
		Task<Stream> CreateReport<TReport>(TReport report, string templater);
		Task<Stream> OlapCube<TCube, TSpecification>(
			TSpecification specification,
			string templater,
			IEnumerable<string> dimensions,
			IEnumerable<string> facts,
			IDictionary<string, bool> order);
		Task<Stream> OlapCube<TCube>(
			string templater,
			IEnumerable<string> dimensions,
			IEnumerable<string> facts,
			IDictionary<string, bool> order);
		Task<IHistory<TAggregate>[]> GetHistory<TAggregate>(IEnumerable<string> uris)
			where TAggregate : class, IAggregateRoot;
		Task<Stream> FindTemplater<T>(string file, string uri)
			where T : class, IIdentifiable;
		Task<Stream> SearchTemplater<T>(string file, ISpecification<T> specification)
			where T : class, ISearchable;
	}

	public static class ReportingProxyHelper
	{
		public static Task<IHistory<TAggregate>> GetHistory<TAggregate>(
			this IReportingProxy proxy,
			string uri)
			where TAggregate : class, IAggregateRoot
		{
			if (proxy == null)
				throw new ArgumentNullException("proxy can't be null");
			if (string.IsNullOrEmpty(uri))
				throw new ArgumentNullException("uri can't be null");
			return
				proxy.GetHistory<TAggregate>(new[] { uri })
				.ContinueWith(t => t.Result.FirstOrDefault());
		}
	}
}
