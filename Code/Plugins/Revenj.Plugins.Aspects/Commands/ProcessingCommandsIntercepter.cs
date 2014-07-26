using System.Diagnostics.Contracts;
using System.Linq;
using Revenj.Logging;
using Revenj.Processing;

namespace Revenj.Plugins.Aspects.Commands
{
	public class ProcessingCommandsIntercepter
	{
		private readonly ILogger Logger;

		public ProcessingCommandsIntercepter(ILogFactory logFactory)
		{
			Contract.Requires(logFactory != null);

			this.Logger = logFactory.Create("Processing engine commands trace");
		}

		public void LogCommands<T>(IServerCommandDescription<T>[] commandDescriptions)
		{
			Logger.Debug(() => "Executing command(s): " + string.Join(", ", commandDescriptions.Select(it => it.CommandType.Name)));
		}
	}
}
