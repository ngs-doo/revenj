using System.Configuration;
using System.Diagnostics;
using System.Diagnostics.Contracts;
using Castle.DynamicProxy;
using NGS;
using NGS.Logging;

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
			var sw = new Stopwatch();
			try
			{
				sw.Start();
				invocation.Proceed();
			}
			finally
			{
				sw.Stop();
				if (sw.ElapsedMilliseconds > TimerLimit)
				{
					var msg =
						"Type: {0}, method: {1}, duration: {2}ms.".With(
							invocation.InvocationTarget.GetType(),
							invocation.Method.Name,
							sw.ElapsedMilliseconds);
					logger.Trace(() => msg);
				}
			}
		}
	}
}
