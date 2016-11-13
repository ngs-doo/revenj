using System;
using System.IO;
using System.Linq;
using System.Net;
using System.Runtime.Serialization;
using System.Security;
using System.Threading.Tasks;
using System.Xml;
using Revenj.DomainPatterns;
using NGS.Serialization;

namespace Revenj
{
	internal class HttpClient
	{
		private readonly string Server;
		private string Auhtorization;

		private static int MaxRetryCount = 3;

		private readonly ProtobufSerialization Protobuf;
		private readonly StreamingContext Context;

		public HttpClient(
			IServiceProvider locator,
			ProtobufSerialization protobuf,
			Configuration settings)
		{
			var remoteUrl = settings["RemoteUrl"];
			if (string.IsNullOrEmpty(remoteUrl))
				throw new ArgumentException("RemoteUrl not provided");
			this.Server = remoteUrl;
			var basicAuth = settings["BasicAuth"];
			var auth = settings["Auth"];

			if (!string.IsNullOrEmpty(basicAuth))
				this.Auhtorization = "Basic " + basicAuth;
			else if (!string.IsNullOrEmpty(auth))
				this.Auhtorization = auth;

			this.Protobuf = protobuf;
			Context = new ProtoBuf.SerializationContext { Context = locator };
		}

		public Task<Stream> Get(string command, HttpStatusCode[] expectedStatus, string accept)
		{
			return Call<object>(command, "GET", null, expectedStatus, accept);
		}

		public Task<T> Get<T>(string command, HttpStatusCode[] expectedStatus)
		{
			return Call<object, T>(command, "GET", null, expectedStatus);
		}

		public Task<TResult> Call<TArgument, TResult>(
			string command,
			string method,
			TArgument argument,
			HttpStatusCode[] expectedStatus)
		{
			var task = Call<TArgument>(command, method, argument, expectedStatus, "application/x-protobuf");
			return task.ContinueWith<TResult>(it => Protobuf.Deserialize<TResult>(it.Result, Context));
		}

		public Task<Stream> Call<TArgument>(
			string command,
			string method,
			TArgument argument,
			HttpStatusCode[] expectedStatus,
			string accept)
		{
			var request = (HttpWebRequest)HttpWebRequest.Create(Server + command);
			request.Method = method;
			if (Auhtorization != null)
			{
#if !PORTABLE
				request.PreAuthenticate = true;
#endif
				request.Headers[HttpRequestHeader.Authorization] = Auhtorization;
			}
			request.ContentType = "application/x-protobuf";
			request.Accept = accept;
			return Task.Factory.StartNew(() =>
			{
				if (argument != null)
				{
					using (var ms = Protobuf.Serialize(argument))
					using (var stream = request.GetRequestStream())
					{
						ms.CopyTo(stream);
					}
				}
				return ExecuteRequest(expectedStatus, request, MaxRetryCount);
			});
		}

		private Stream ExecuteRequest(HttpStatusCode[] expectedStatus, HttpWebRequest request, int retryCount)
		{
			HttpWebResponse response;
			try
			{
				response = (HttpWebResponse)request.GetResponse();
				if (expectedStatus != null && !expectedStatus.Contains(response.StatusCode))
				{
					if ((int)response.StatusCode < 300)
						throw new ArgumentException("Invalid response code. Received " + response.StatusCode
							+ ". Expected: " + string.Join(" or ", expectedStatus.Select(it => it.ToString()).ToArray()));
				}
			}
			catch (WebException we)
			{
				response = we.Response as HttpWebResponse;
				if (retryCount > 0 && (response == null || response != null && response.StatusCode == HttpStatusCode.ServiceUnavailable))
					return ExecuteRequest(expectedStatus, request, retryCount - 1);
				if (response == null)
					throw;
				string content;
				var ct = (response.ContentType ?? string.Empty);
				bool isText = ct.StartsWith("plain/text");
				if (ct.StartsWith("application/xml"))
				{
					using (var reader = XmlReader.Create(response.GetResponseStream()))
					{
						reader.MoveToContent();
						content = reader.ReadInnerXml();
					}
				}
				else
					using (var stream = new StreamReader(response.GetResponseStream()))
						content = stream.ReadToEnd();

				switch (response.StatusCode)
				{
					case HttpStatusCode.BadRequest:
						throw new WebException(content);
					case HttpStatusCode.Unauthorized:
					case HttpStatusCode.Forbidden:
						throw new SecurityException(content);
					case HttpStatusCode.NotFound:
						throw new WebException(content);
					case HttpStatusCode.RequestEntityTooLarge:
						throw new ArgumentOutOfRangeException(content);
					default:
						if (isText)
							throw new WebException(content);
						throw new WebException(response.StatusDescription);
				}
			}
			return response.GetResponseStream();
		}
	}
}
