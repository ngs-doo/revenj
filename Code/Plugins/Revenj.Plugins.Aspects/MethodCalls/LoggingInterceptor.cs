using System;
using System.Diagnostics.Contracts;
using System.Linq;
using Castle.DynamicProxy;
using NGS;
using NGS.Logging;

namespace Revenj.Plugins.Aspects.MethodCalls
{
	public class LoggingInterceptor : IInterceptor
	{
		private readonly ILogFactory LogFactory;

		public LoggingInterceptor(ILogFactory logFactory)
		{
			Contract.Requires(logFactory != null);

			this.LogFactory = logFactory;
		}

		public void Intercept(IInvocation invocation)
		{
			var logger = LogFactory.Create(invocation.TargetType.FullName);
			try
			{
				logger.Trace(() => "Entering: {0}. Arguments: {1}".With(
					invocation.Method.Name,
					string.Join(", ", invocation.Arguments.Select(it => it ?? "<null>"))));

				invocation.Proceed();

				logger.Trace(() => "Exiting: {0}. Value: {1}".With(
					invocation.Method.Name,
					invocation.ReturnValue ?? "<null>"));
			}
			catch (Exception ex)
			{
				logger.Error(() => "Error: {0}. Message: {1}".With(
					invocation.Method.Name,
					ex.ToString()));
				throw;
			}
		}
	}
}
