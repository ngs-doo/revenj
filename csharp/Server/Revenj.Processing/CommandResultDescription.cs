using System;
using System.Diagnostics;

namespace Revenj.Processing
{
	public class CommandResultDescription<TFormat> : ICommandResultDescription<TFormat>
	{
		public string RequestID { get; private set; }
		public ICommandResult<TFormat> Result { get; private set; }
		public long Duration { get; private set; }

		public static CommandResultDescription<TFormat> Create(string id, ICommandResult<TFormat> result, long start)
		{
			return new CommandResultDescription<TFormat>
			{
				RequestID = id,
				Result = result,
				Duration = (Stopwatch.GetTimestamp() - start) / TimeSpan.TicksPerMillisecond
			};
		}
	}
}
