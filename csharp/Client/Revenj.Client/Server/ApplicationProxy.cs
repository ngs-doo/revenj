using System;
using System.Net;
using System.Threading.Tasks;

namespace Revenj
{
	internal class ApplicationProxy : IApplicationProxy
	{
		private const string URL = "RestApplication.svc/";
		private readonly HttpClient Http;

		public ApplicationProxy(HttpClient http)
		{
			this.Http = http;
		}

		public Task<T> Get<T>(string command, HttpStatusCode[] expectedStatus)
		{
			if (string.IsNullOrEmpty(command))
				throw new ArgumentNullException("command can't be empty");
			return Http.Get<T>(URL + command, expectedStatus);
		}

		public Task<TResult> Post<TArgument, TResult>(
			string command,
			TArgument argument,
			HttpStatusCode[] expectedStatus)
		{
			if (string.IsNullOrEmpty(command))
				throw new ArgumentNullException("command can't be empty");
			return
				Http.Call<TArgument, TResult>(
					URL + command,
					"POST",
					argument,
					expectedStatus);
		}
	}
}
