using System.Diagnostics;
using System.Linq;
using Revenj.Processing;

namespace Revenj.Plugins.Aspects.Commands
{
	public class ProcessingCommandsIntercepter
	{
		private static readonly TraceSource TraceSource = new TraceSource("Revenj.Aspects");

		public void LogCommands<T>(IServerCommandDescription<T>[] commandDescriptions)
		{
			TraceSource.TraceEvent(TraceEventType.Information, 3101, "{0}", new LazyFormat<T>(commandDescriptions));
		}

		struct LazyFormat<T>
		{
			private readonly IServerCommandDescription<T>[] Commands;

			public LazyFormat(IServerCommandDescription<T>[] commands)
			{
				this.Commands = commands;
			}

			public override string ToString()
			{
				return string.Join(", ", Commands.Select(it => it.CommandType.Name));
			}
		}
	}
}
