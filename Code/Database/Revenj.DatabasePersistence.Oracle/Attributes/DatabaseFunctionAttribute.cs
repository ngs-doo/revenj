using System;
using System.Diagnostics.Contracts;

namespace Revenj.DatabasePersistence.Oracle
{
	public class DatabaseFunctionAttribute : Attribute
	{
		public string Function { get; private set; }
		public Type Call { get; private set; }

		public DatabaseFunctionAttribute(string function, Type call)
		{
			Contract.Requires(function != null);
			Contract.Requires(call != null);

			this.Function = function;
			this.Call = call;
		}
	}
}
