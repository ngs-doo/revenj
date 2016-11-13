using System;
using System.IO;
using System.Linq.Expressions;
using System.Threading.Tasks;
using Revenj;

namespace Revenj.DomainPatterns
{
	internal class ClientTemplaterService : ITemplaterService
	{
		private readonly IReportingProxy Proxy;

		public ClientTemplaterService(IReportingProxy proxy)
		{
			this.Proxy = proxy;
		}

		public Task<Stream> Populate<T>(string file, T aggregate)
			where T : class, IIdentifiable
		{
			if (aggregate == null)
				throw new ArgumentNullException("aggregate can't be null");
			return Proxy.FindTemplater<T>(file, aggregate.URI);
		}

		public Task<Stream> Populate<T>(string file, ISpecification<T> specification)
			where T : class, ISearchable
		{
			return Proxy.SearchTemplater<T>(file, specification);
		}
	}
#if !PORTABLE
	public static partial class TemplaterServiceHelper
	{
		public static Task<Stream> Populate<T>(this ITemplaterService templater, string file, Expression<Func<T, bool>> filter)
			where T : class, ISearchable
		{
			if (templater == null)
				throw new ArgumentNullException("templater can't be null");
			if (filter == null)
				throw new ArgumentNullException("filter can't be null");
			return templater.Populate<T>(file, new ExpressionSpecification<T>(filter));
		}
	}
#endif
}
