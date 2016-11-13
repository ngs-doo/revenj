using System;
using System.Net;
using System.Threading.Tasks;

namespace Revenj
{
	public interface IApplicationProxy
	{
		Task<T> Get<T>(
			string command,
			HttpStatusCode[] expectedStatus);
		Task<TResult> Post<TArgument, TResult>(
			string command,
			TArgument argument,
			HttpStatusCode[] expectedStatus);
	}

	public static class ApplicationProxyHelper
	{
		public static Task<T> Get<T>(this IApplicationProxy proxy, string command)
		{
			if (proxy == null)
				throw new ArgumentNullException("proxy can't be null");
			return proxy.Get<T>(command, new[] { HttpStatusCode.OK });
		}

		public static Task<TResult> Post<TArgument, TResult>(
			this IApplicationProxy proxy,
			string command,
			TArgument argument)
		{
			if (proxy == null)
				throw new ArgumentNullException("proxy can't be null");
			return proxy.Post<TArgument, TResult>(command, argument, new[] { HttpStatusCode.OK });
		}
	}
}
