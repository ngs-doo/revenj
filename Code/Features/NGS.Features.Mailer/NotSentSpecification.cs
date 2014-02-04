using System;
using System.Linq.Expressions;
using NGS.DomainPatterns;

namespace NGS.Features.Mailer
{
	public class NotSentSpecification : ISpecification<IMailMessage>
	{
		public Expression<Func<IMailMessage, bool>> IsSatisfied
		{
			get { return it => it.SentAt == null && it.Attempts <= it.RetriesAllowed; }
		}
	}
}
