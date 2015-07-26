using System;
using System.Configuration;
using System.Diagnostics;
using Castle.DynamicProxy;

namespace Revenj.Plugins.Aspects.PerformanceTrace
{
	public class PerformanceInterceptor : IInterceptor
	{
		private static readonly int TimerLimit;
		private static readonly TraceSource TraceSource = new TraceSource("Revenj.Aspects");

		static PerformanceInterceptor()
		{
			if (!int.TryParse(ConfigurationManager.AppSettings["Performance.TimerLimit"], out TimerLimit))
				TimerLimit = 10;
		}

		public void Intercept(IInvocation invocation)
		{
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
					TraceSource.TraceEvent(
						TraceEventType.Information,
						3131,
						"Target: {0}, type: {1}, method: {2}, duration: {3} ms",
						invocation.TargetType.FullName,
						invocation.InvocationTarget.GetType(),
						invocation.Method.Name,
						duration);
				}
			}
		}
	}
}
