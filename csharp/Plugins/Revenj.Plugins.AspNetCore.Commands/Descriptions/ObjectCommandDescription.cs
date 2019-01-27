using Revenj.Processing;
using System;

namespace Revenj.Plugins.AspNetCore.Commands
{
	public class ObjectCommandDescription : IServerCommandDescription<object>
	{
		public string RequestID { get; set; }
		public object Data { get; set; }
		public Type CommandType { get; set; }
	}
}