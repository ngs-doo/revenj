using System;
using System.Linq;
using System.Linq.Expressions;

namespace Revenj.DomainPatterns
{
	public class SpecificationByExpression<TCondition> : ISpecification<TCondition>
		where TCondition : IDataSource
	{
		public SpecificationByExpression(Expression<Func<TCondition, bool>> expression)
		{
			IsSatisfied = expression;
		}

		public Expression<Func<TCondition, bool>> IsSatisfied { get; private set; }
	}

	public static class SpecificationByExpressionHelper
	{
		public static IQueryable<TSource> Find<TSource, TCondition>(
			this IQueryableRepository<TSource> repository,
			Expression<Func<TCondition, bool>> condition)
			where TSource : TCondition
			where TCondition : IDataSource
		{
			return repository.Query(new SpecificationByExpression<TCondition>(condition));
		}
	}
}
