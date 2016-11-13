using System;
using System.Linq.Expressions;
using System.Threading.Tasks;
using Serialize.Linq.Nodes;
using Serialize.Linq.Serializers;

namespace Revenj.DomainPatterns
{
	internal class ExpressionSpecification<T> : ISpecification<T>
		where T : class, ISearchable
	{
		private readonly Expression<Func<T, bool>> Filter;
		private static readonly ExpressionConverter Converter = new ExpressionConverter();

		internal ExpressionSpecification(Expression<Func<T, bool>> filter)
		{
			this.Filter = filter;
			Expression = (LambdaExpressionNode)Converter.Convert(filter);
		}

		public Expression<Func<T, bool>> IsSatisfied { get { return Filter; } }

		public LambdaExpressionNode Expression { get; private set; }
	}

	public static partial class ExpressionSpecificationHelper
	{
		public static Task<T[]> Search<T>(
			this ISearchableRepository<T> repository,
			Expression<Func<T, bool>> filter,
			int? limit,
			int? offset)
			where T : class, ISearchable
		{
			if (repository == null)
				throw new ArgumentNullException("repository can't be null");
			return repository.Search(new ExpressionSpecification<T>(filter), limit, offset, null);
		}

		public static Task<long> Count<T>(
			this ISearchableRepository<T> repository,
			Expression<Func<T, bool>> filter)
			where T : class, ISearchable
		{
			if (repository == null)
				throw new ArgumentNullException("repository can't be null");
			return repository.Count(new ExpressionSpecification<T>(filter));
		}

		public static SearchBuilder<T> Builder<T>(
			this ISearchableRepository<T> repository,
			Expression<Func<T, bool>> filter)
			where T : class, ISearchable
		{
			if (repository == null)
				throw new ArgumentNullException("repository can't be null");
			return new SearchBuilder<T>(repository).With(new ExpressionSpecification<T>(filter));
		}
	}
}
