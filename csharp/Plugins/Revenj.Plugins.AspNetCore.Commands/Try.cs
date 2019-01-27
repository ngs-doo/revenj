using System;
using System.Net;
using Microsoft.AspNetCore.Http;
using Revenj.Serialization;

namespace Revenj.Plugins.AspNetCore.Commands
{
	internal struct Try<T>
	{
		public readonly T Result;
		public readonly bool IsSuccess;

		public static readonly Try<T> Empty = new Try<T>(default(T), true);
		public static readonly Try<T> Error = new Try<T>(default(T), false);

		private Try(T result, bool isSuccess)
		{
			this.Result = result;
			this.IsSuccess = isSuccess;
		}

		public bool IsFailure { get { return !IsSuccess; } }

		public static implicit operator Try<T>(T result)
		{
			return new Try<T>(result, true);
		}
	}

	internal static class Try
	{
		public static Try<T> Fail<T>(string message, HttpResponse response)
		{
			Utility.WriteError(response, message, HttpStatusCode.BadRequest);
			return Try<T>.Error;
		}
		public static Try<T> TryDeserialize<T>(this IWireSerialization serialization, HttpRequest request, HttpResponse response)
		{
			try
			{
				return serialization.Deserialize<T>(request.Body, request.ContentType);
			}
			catch (Exception ex)
			{
				return Fail<T>(ex.Message, response);
			}
		}
	}
}
