#if NETSTANDARD2_0
using System;

namespace Revenj
{
    internal class ConfigurationErrorsException : Exception
    {
		public ConfigurationErrorsException(string message)
			: base(message) { }
		public ConfigurationErrorsException(string message, Exception inner)
			: base(message, inner) { }
	}
}
#endif