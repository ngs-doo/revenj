using Microsoft.AspNetCore.Http;
using Revenj.Processing;
using System;
using System.IO;

namespace Revenj.Plugins.AspNetCore.Commands
{
	public class ProtobufCommandDescription : IServerCommandDescription<Stream>
	{
		public string RequestID { get; private set; }
		public Stream Data { get; set; }
		public Type CommandType { get; private set; }

		public ProtobufCommandDescription(IQueryCollection args, Stream message, Type commandType)
		{
			this.CommandType = commandType;
			Data = message;
		}
	}
}