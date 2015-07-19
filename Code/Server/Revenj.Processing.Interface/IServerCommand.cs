using System;
using System.Security.Principal;
using Revenj.Serialization;

namespace Revenj.Processing
{
	/// <summary>
	/// Generic server command.
	/// Server command is added through MEF by exporting IServerCommand.
	/// Server command can replace existing server command by using InsteadOf metadata.
	/// </summary>
	public interface IServerCommand
	{
		/// <summary>
		/// Execute single server command.
		/// Convert provided input data to requested output result.
		/// Provide current context through service provider
		/// Input serialization is used to deserialize provided argument.
		/// Output serialization is used to serialize requested result.
		/// </summary>
		/// <typeparam name="TInput">input format type</typeparam>
		/// <typeparam name="TOutput">output format type</typeparam>
		/// <param name="locator">service locator</param>
		/// <param name="input">input deserializer</param>
		/// <param name="output">output serializer</param>
		/// <param name="principal">security context</param>
		/// <param name="data">command argument</param>
		/// <returns>command response</returns>
		ICommandResult<TOutput> Execute<TInput, TOutput>(
			IServiceProvider locator,
			ISerialization<TInput> input,
			ISerialization<TOutput> output,
			IPrincipal principal,
			TInput data);
	}
	/// <summary>
	/// If server command doesn't change data it can be declared as read-only server command.
	/// This is used as optimization, since transaction scope is created only when at least one command requires transaction.
	/// </summary>
	public interface IReadOnlyServerCommand : IServerCommand { }
}
