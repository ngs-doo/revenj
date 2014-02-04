using System;
using Revenj.Processing;

namespace Revenj.Wcf
{
	public class SoapCommandDescription<TFormat> : IServerCommandDescription<TFormat>
	{
		public string RequestID { get; private set; }
		public TFormat Data { get; set; }
		public Type CommandType { get; private set; }

		public static SoapCommandDescription<TFormat> Create(string id, TFormat data, Type commandType)
		{
			return new SoapCommandDescription<TFormat>
			{
				RequestID = id,
				Data = data,
				CommandType = commandType
			};
		}
	}
}
