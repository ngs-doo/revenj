using System;
using System.Diagnostics;
using System.Linq;
using Castle.DynamicProxy;

namespace Revenj.Plugins.Aspects.MethodCalls
{
	public class LoggingInterceptor : IInterceptor
	{
		private static readonly TraceSource TraceSource = new TraceSource("Revenj.Aspects");

		public void Intercept(IInvocation invocation)
		{
			try
			{
				TraceSource.TraceEvent(
					TraceEventType.Information,
					3112,
					"Type: {0}, method: {1}, arguments: {2}",
					invocation.TargetType.FullName,
					invocation.Method.Name,
					new LazyArguments(invocation.Arguments));

				invocation.Proceed();

				TraceSource.TraceEvent(
					TraceEventType.Information,
					3113,
					"Type: {0}, method: {1}, value: {2}",
					invocation.TargetType.FullName,
					invocation.Method.Name,
					invocation.ReturnValue ?? "<null>");
			}
			catch (Exception ex)
			{
				TraceSource.TraceEvent(
					TraceEventType.Error,
					3114,
					"Error: {0}. Message: {1}",
					invocation.Method.Name,
					ex);
				throw;
			}
		}

		struct LazyArguments
		{
			private readonly object[] Arguments;

			public LazyArguments(object[] arguments)
			{
				this.Arguments = arguments;
			}

			public override string ToString()
			{
				return string.Join(", ", Arguments.Select(it => it ?? "<null>"));
			}
		}
	}
}
