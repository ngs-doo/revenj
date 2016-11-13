using System;
using System.Net;
using System.Threading.Tasks;
using Revenj.DomainPatterns;

namespace Revenj
{
	internal class CrudProxy : ICrudProxy
	{
		private const string URL = "Crud.svc/";
		private readonly HttpClient Http;

		public CrudProxy(HttpClient http)
		{
			this.Http = http;
		}

		public Task<T> Create<T>(T aggregate)
			where T : class, IAggregateRoot
		{
			if (aggregate == null)
				throw new ArgumentNullException("aggregate can't be null");
			aggregate.Validate();
			return
				Http.Call<T, T>(
					URL + typeof(T).FullName,
					"POST",
					aggregate,
					new[] { HttpStatusCode.Created });
		}

		public Task<T> Read<T>(string uri)
			where T : class, IIdentifiable
		{
			if (string.IsNullOrEmpty(uri))
				throw new ArgumentNullException("uri can't be null");
			return
				Http.Get<T>(
					URL + typeof(T).FullName + "/" + Uri.EscapeUriString(uri),
					new[] { HttpStatusCode.OK });
		}

		public Task<T> Update<T>(T aggregate)
			where T : class, IAggregateRoot
		{
			if (aggregate == null)
				throw new ArgumentNullException("aggregate can't be null");
			aggregate.Validate();
			return
				Http.Call<T, T>(
					URL + typeof(T).FullName + "/" + Uri.EscapeUriString(aggregate.URI),
					"PUT",
					aggregate,
					new[] { HttpStatusCode.OK });
		}

		public Task<T> Delete<T>(string uri)
			where T : class, IAggregateRoot
		{
			if (string.IsNullOrEmpty(uri))
				throw new ArgumentNullException("uri can't be null");
			return
				Http.Call<string, T>(
					URL + typeof(T).FullName + "/" + Uri.EscapeUriString(uri),
					"DELETE",
					null,
					new[] { HttpStatusCode.OK });
		}
	}
}
