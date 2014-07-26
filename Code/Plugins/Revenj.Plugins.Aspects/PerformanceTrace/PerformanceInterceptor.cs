using System;
using System.Configuration;
using System.Diagnostics;
using System.Diagnostics.Contracts;
using Castle.DynamicProxy;
using Revenj.Logging;

namespace Revenj.Plugins.Aspects.PerformanceTrace
{
	public class PerformanceInterceptor : IInterceptor
	{
		private static readonly int TimerLimit;

		static PerformanceInterceptor()
		{
			if (!int.TryParse(ConfigurationManager.AppSettings["Performance.TimerLimit"], out TimerLimit))
				TimerLimit = 10;
		}

		private readonly ILogFactory LogFactory;

		public PerformanceInterceptor(ILogFactory logFactory)
		{
			Contract.Requires(logFactory != null);

			this.LogFactory = logFactory;
		}

		public void Intercept(IInvocation invocation)
		{
			var logger = LogFactory.Create(invocation.TargetType.FullName);
			var start = Stopwatch.GetTimestamp();
			try
			{
				invocation.Proceed();
			}
			finally
			{
				var duration = (Stopwatch.GetTimestamp() - start) / TimeSpan.TicksPerMillisecond;
				if (duration > TimerLimit)
				{
					logger.Trace(() => "Type: {0}, method: {1}, duration: {2}ms.".With(
							invocation.InvocationTarget.GetType(),
							invocation.Method.Name,
							duration));
				}
			}
		}
	}
}
