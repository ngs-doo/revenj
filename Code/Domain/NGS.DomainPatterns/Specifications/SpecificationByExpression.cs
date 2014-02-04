using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Linq.Expressions;
using System.Runtime.Serialization;

namespace NGS.DomainPatterns
{
	public class SpecificationByExpression<TCondition> : ISpecification<TCondition>
		where TCondition : IEntity
	{
		public SpecificationByExpression(Expression<Func<TCondition, bool>> expression)
		{
			IsSatisfied = expression;
		}

		public Expression<Func<TCondition, bool>> IsSatisfied { get; private set; }
	}

	public static class SpecificationByExpressionHelper
	{
		public static IQueryable<TEntity> Find<TEntity, TCondition>(
			this IQueryableRepository<TEntity> repository, 
			Expression<Func<TCondition, bool>> condition)
			where TEntity : TCondition
			where TCondition : IEntity
		{
			return repository.Query(new SpecificationByExpression<TCondition>(condition));
		}
	}
}
