using System;
using Revenj.Processing;

namespace Revenj.Wcf
{
	public class ObjectCommandDescription : IServerCommandDescription<object>
	{
		public string RequestID { get; set; }
		public object Data { get; set; }
		public Type CommandType { get; set; }
	}
}