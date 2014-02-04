using System;
using System.Linq.Expressions;
using NGS.DomainPatterns;
using Serialize.Linq.Nodes;

namespace Revenj.Plugins.Rest.Commands
{
	internal class SpecificationFromNode<T> : ISpecification<T>
	{
		public SpecificationFromNode(LambdaExpressionNode node)
		{
			IsSatisfied = node.ToExpression<Func<T, bool>>();
		}

		public Expression<Func<T, bool>> IsSatisfied { get; private set; }
	}
}
