using System;
using System.IO;
using System.Net;
using System.Text;
using Revenj.Api;
using Revenj.Serialization;

namespace Revenj.Plugins.Rest.Commands
{
	internal struct Either<T>
	{
		public readonly T Result;
		public readonly Stream Error;

		public static readonly Either<T> Empty = new Either<T>(default(T), null);

		private Either(T result, Stream error)
		{
			this.Result = result;
			this.Error = error;
		}

		public bool IsSuccess { get { return Error == null; } }
		public bool IsFailure { get { return Error != null; } }

		public static implicit operator Either<T>(T result)
		{
			return new Either<T>(result, null);
		}

		public static implicit operator Either<T>(string error)
		{
			return BadRequest(error);
		}

		public static implicit operator Either<T>(Stream error)
		{
			return new Either<T>(default(T), error);
		}

		public static Either<T> Succes(T result)
		{
			return new Either<T>(result, null);
		}
		public static Either<T> BadRequest(string message)
		{
			//TODO: throw exception instead!?
			ThreadContext.Response.StatusCode = HttpStatusCode.BadRequest;
			ThreadContext.Response.ContentType = "text/plain; charset=\"utf-8\"";
			return new Either<T>(default(T), new MemoryStream(Encoding.UTF8.GetBytes(message)));
		}
	}

	internal static class Either
	{
		public static Either<T> TryDeserialize<T>(this IWireSerialization serialization, Stream message)
		{
			try
			{
				return serialization.Deserialize<T>(message, ThreadContext.Request.ContentType);
			}
			catch (Exception ex)
			{
				return ex.Message;
			}
		}
	}
}
