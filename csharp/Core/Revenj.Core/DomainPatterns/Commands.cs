using System;
using System.Runtime.Serialization;

namespace Revenj.DomainPatterns
{
	[Serializable]
	[DataContract]
	public class CommandLog<TCommand> : ICommandLog<TCommand>
		where TCommand : ICommand
	{
		[DataMember]
		public string URI { get; private set; }
		[DataMember]
		public DateTime At { get; private set; }
		[DataMember]
		public TCommand Value { get; private set; }

		public static CommandLog<TCommand> Create(string uri, DateTime at, TCommand value)
		{
			return
				new CommandLog<TCommand>
				{
					URI = uri,
					At = at,
					Value = value
				};
		}
	}
}
