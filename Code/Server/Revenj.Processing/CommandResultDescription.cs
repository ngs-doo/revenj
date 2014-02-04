namespace Revenj.Processing
{
	public class CommandResultDescription<TFormat> : ICommandResultDescription<TFormat>
	{
		public string RequestID { get; private set; }
		public ICommandResult<TFormat> Result { get; private set; }

		public static CommandResultDescription<TFormat> Create(string id, ICommandResult<TFormat> result)
		{
			return new CommandResultDescription<TFormat>
			{
				RequestID = id,
				Result = result
			};
		}
	}
}
