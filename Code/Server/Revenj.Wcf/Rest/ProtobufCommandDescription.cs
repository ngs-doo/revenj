using System;
using System.IO;
using Revenj.Processing;

namespace Revenj.Wcf
{
	public class ProtobufCommandDescription : IServerCommandDescription<Stream>
	{
		public string RequestID { get; private set; }
		public Stream Data { get; set; }
		public Type CommandType { get; private set; }

		public ProtobufCommandDescription(string[] args, Stream message, Type commandType)
		{
			this.CommandType = commandType;
			Data = message;
		}
	}
}