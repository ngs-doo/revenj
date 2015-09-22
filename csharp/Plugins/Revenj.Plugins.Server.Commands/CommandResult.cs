using System;
using System.IO;
using System.Net;
using System.Text;
using Revenj.Processing;
using Revenj.Serialization;
using Revenj.Utility;

namespace Revenj.Plugins.Server.Commands
{
	public class CommandResult<TFormat> : ICommandResult<TFormat>
	{
		public TFormat Data { get; private set; }
		public string Message { get; private set; }
		public HttpStatusCode Status { get; private set; }

		public static CommandResult<TFormat> Return(HttpStatusCode status, TFormat data, string message, params object[] args)
		{
			return new CommandResult<TFormat>
			{
				Data = data,
				Message = args != null && args.Length > 0 ? message.With(args) : message,
				Status = status
			};
		}

		public static CommandResult<TFormat> Success(TFormat data, string message, params object[] args)
		{
			return new CommandResult<TFormat>
			{
				Data = data,
				Message = args != null && args.Length > 0 ? message.With(args) : message,
				Status = HttpStatusCode.OK
			};
		}

		public static CommandResult<TFormat> Forbidden(string name)
		{
			return new CommandResult<TFormat>
			{
				Data = default(TFormat),
				Message = "You don't have permission to access: " + name,
				Status = HttpStatusCode.Forbidden
			};
		}

		public static CommandResult<TFormat> Fail(string message, string details)
		{
			return Exceptions.DebugMode
				? new CommandResult<TFormat>
				{
					Message = message + details ?? string.Empty,
					Status = HttpStatusCode.BadRequest
				}
				: new CommandResult<TFormat>
				{
					Message = message,
					Status = HttpStatusCode.BadRequest
				};
		}

		public class Either<TArgument>
		{
			public readonly TArgument Argument;
			public readonly CommandResult<TFormat> Error;

			public Either(TArgument arg) { this.Argument = arg; }
			public Either(CommandResult<TFormat> error) { this.Error = error; }
		}

		internal static string ConvertToString(TFormat value)
		{
			var sr = value as StreamReader;
			if (sr != null)
				return sr.ReadToEnd();
			var stream = value as Stream;
			if (stream != null)
			{
				var sb = new StringBuilder("{ ", 64);
				int cur = -1;
				int i = 0;
				for (; i < 10; i++)
				{
					cur = stream.ReadByte();
					if (cur != -1)
						sb.Append(cur).Append(", ");
					else
						break;
				}
				if (cur == -1 && i > 0)
					sb.Length -= 2;
				else
					sb.Append("...");
				sb.Append(" }");
				return sb.ToString();
			}
			return value.ToString();
		}

		public static Either<TArgument> Check<TArgument, TInput>(
			ISerialization<TInput> input,
			ISerialization<TFormat> output,
			TInput data,
			Func<ISerialization<TFormat>, TFormat> getExample)
		{
			if (data == null) //TODO: use input serialization!?
				return new Either<TArgument>(Fail("Argument missing.", @"Example argument: 
" + ConvertToString(getExample(output))));

			TArgument argument = default(TArgument);
			try
			{
				argument = input.Deserialize<TInput, TArgument>(data);
			}
			catch (Exception ex)
			{
				return new Either<TArgument>(Fail("Couldn't decode argument data.", @"{0}
Sent data:
{1}
Example argument: 
{2}".With(ex.Message, data, ConvertToString(getExample(output)))));
			}
			if (argument == null)
				return new Either<TArgument>(Fail("Couldn't decode argument data.", @"Sent data:
{0}
Example argument: 
{1}".With(data, ConvertToString(getExample(output)))));

			return new Either<TArgument>(argument);
		}
	}
}
