using Revenj.Processing;
using System;

namespace Revenj.AspNetCore
{
	public class ObjectCommandDescription : IServerCommandDescription<object>
	{
		public string RequestID { get; set; }
		public object Data { get; set; }
		public Type CommandType { get; set; }
	}
}