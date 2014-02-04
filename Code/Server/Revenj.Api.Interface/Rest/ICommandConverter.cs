using System.IO;

namespace Revenj.Api
{
	/// <summary>
	/// Service for passing REST-like commands to server commands.
	/// Return stream to external request.
	/// </summary>
	public interface ICommandConverter
	{
		/// <summary>
		/// Pass request to the server command.
		/// Request is casted instead of deserialized.
		/// </summary>
		/// <typeparam name="TCommand">server command type</typeparam>
		/// <typeparam name="TArgument">server command argument</typeparam>
		/// <param name="argument">argument value</param>
		/// <param name="accept">expected result from Accept mime type</param>
		/// <returns>result converted to requested mime type</returns>
		Stream PassThrough<TCommand, TArgument>(TArgument argument, string accept);
		/// <summary>
		/// Call IRestApplication Get/Post with serialized argument
		/// </summary>
		/// <typeparam name="TCommand">server command type</typeparam>
		/// <typeparam name="TArgument">server command argument</typeparam>
		/// <param name="argument">argument value</param>
		/// <returns>result converted to requested mime type</returns>
		Stream ConvertStream<TCommand, TArgument>(TArgument argument);
	}
	/// <summary>
	/// Utility for command converter
	/// </summary>
	public static class CommandConverterHelper
	{
		/// <summary>
		/// Pass request to the server command.
		/// Request is casted instead of deserialized.
		/// Result type is defined with Accept header
		/// If Accept header is not defined, XML will be used
		/// </summary>
		/// <typeparam name="TCommand">server command type</typeparam>
		/// <typeparam name="TArgument">server command argument</typeparam>
		/// <param name="converter">command converter</param>
		/// <param name="argument">argument value</param>
		/// <returns>result converted to requested mime type</returns>
		public static Stream PassThrough<TCommand, TArgument>(this ICommandConverter converter, TArgument argument)
		{
			var accept = (ThreadContext.Request.Accept ?? "application/xml").ToLowerInvariant();
			return converter.PassThrough<TCommand, TArgument>(argument, accept);
		}
	}
}
