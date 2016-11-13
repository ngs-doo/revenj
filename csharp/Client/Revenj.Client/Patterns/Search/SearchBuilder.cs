using System;
using System.Collections.Generic;
using System.Linq.Expressions;
using System.Threading.Tasks;

namespace Revenj.DomainPatterns
{
	public class SearchBuilder<T> where T : class, ISearchable
	{
		private readonly ISearchableRepository<T> Repository;
		public ISpecification<T> Specification { get; private set; }
		private int? Limit;
		private int? Offset;
		private Dictionary<string, bool> Order = new Dictionary<string, bool>();

		internal SearchBuilder(ISearchableRepository<T> repository)
		{
			this.Repository = repository;
		}

		public SearchBuilder<T> With(ISpecification<T> specification)
		{
			this.Specification = specification;
			return this;
		}
		public SearchBuilder<T> Take(int limit)
		{
			this.Limit = limit;
			return this;
		}
		public SearchBuilder<T> Skip(int offset)
		{
			this.Offset = offset;
			return this;
		}
		internal SearchBuilder<T> OrderBy(string property, bool direction)
		{
			if (string.IsNullOrEmpty(property))
				throw new ArgumentException("property can't be empty");
			Order[property] = direction;
			return this;
		}
		public Task<T[]> Search()
		{
			return Repository.Search(Specification, Limit, Offset, Order);
		}
	}

	public static partial class SearchBuilderHelper
	{
		public static SearchBuilder<T> Builder<T>(this ISearchableRepository<T> repository)
			where T : class, ISearchable
		{
			if (repository == null)
				throw new ArgumentNullException("repository can't be null");
			return new SearchBuilder<T>(repository);
		}

		private static string PropertyName(LambdaExpression lambda)
		{
			MemberExpression memberExpression;
			if (lambda.Body is UnaryExpression)
			{
				var unaryExpression = lambda.Body as UnaryExpression;
				memberExpression = unaryExpression.Operand as MemberExpression;
			}
			else
				memberExpression = lambda.Body as MemberExpression;
			var constantExpression = memberExpression.Expression as ConstantExpression;
			return memberExpression.Member.Name;
		}

		public static SearchBuilder<TSource> Ascending<TSource, TResult>(
			this SearchBuilder<TSource> builder,
			Expression<Func<TSource, TResult>> property)
			where TSource : class, ISearchable
		{
			if (builder == null)
				throw new ArgumentNullException("builder can't be null");
			if (property == null)
				throw new ArgumentNullException("property can't be null");
			return builder.OrderBy(PropertyName(property), true);
		}
		public static SearchBuilder<TSource> Descending<TSource, TResult>(
			this SearchBuilder<TSource> builder,
			Expression<Func<TSource, TResult>> property)
			where TSource : class, ISearchable
		{
			if (builder == null)
				throw new ArgumentNullException("builder can't be null");
			if (property == null)
				throw new ArgumentNullException("property can't be null");
			return builder.OrderBy(PropertyName(property), false);
		}
	}
}
