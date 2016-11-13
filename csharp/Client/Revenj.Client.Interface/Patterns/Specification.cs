using System;
using System.Linq.Expressions;

namespace Revenj.DomainPatterns
{
	public interface ISpecification<T>
		where T : class, ISearchable
	{
		Expression<Func<T, bool>> IsSatisfied { get; }
	}
}
