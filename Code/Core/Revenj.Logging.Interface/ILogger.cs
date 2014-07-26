using System;
using System.Diagnostics.Contracts;

namespace Revenj.Logging
{
	/// <summary>
	/// Service for logging various events and state of the system.
	/// </summary>
	[ContractClass(typeof(LoggerContract))]
	public interface ILogger
	{
		/// <summary>
		/// Log some information using provided log level and message.
		/// Message will be constructed only if log is created.
		/// </summary>
		/// <param name="level">log severity</param>
		/// <param name="detail">log message</param>
		void Log(LogLevel level, Func<string> detail);
		/// <summary>
		/// Log some information using provided log level and message.
		/// Use this log if message is already constructed.
		/// </summary>
		/// <param name="level">log severity</param>
		/// <param name="detail">log message</param>
		void Log(LogLevel level, string message);
	}

	[ContractClassFor(typeof(ILogger))]
	internal sealed class LoggerContract : ILogger
	{
		public void Log(LogLevel level, Func<string> detail)
		{
			Contract.Requires(detail != null);
		}
		public void Log(LogLevel level, string message) { }
	}

	/// <summary>
	/// Helper for logging.
	/// Instead of providing log level, use functions for specified level instead
	/// </summary>
	public static class LoggerHelper
	{
		/// <summary>
		/// Log debug information. Since debug logging is often disabled,
		/// Func&lt;string&gt; is the preferred way of logging debug information.
		/// </summary>
		/// <param name="logger">logging service</param>
		/// <param name="detail">log message</param>
		public static void Debug(this ILogger logger, Func<string> detail)
		{
			Contract.Requires(logger != null);
			Contract.Requires(detail != null);

			logger.Log(LogLevel.Debug, detail);
		}
		/// <summary>
		/// Log debug information. Since debug logging is often disabled,
		/// Func&lt;string&gt; is the preferred way of logging debug information.
		/// Use this only when message is already evaluated.
		/// </summary>
		/// <param name="logger">logging service</param>
		/// <param name="message">log message</param>
		public static void Debug(this ILogger logger, string message)
		{
			Contract.Requires(logger != null);

			logger.Log(LogLevel.Debug, message);
		}
		/// <summary>
		/// Log trace information. Since trace logging is often disabled,
		/// Func&lt;string&gt; is the preferred way of logging trace information.
		/// </summary>
		/// <param name="logger">logging service</param>
		/// <param name="detail">log message</param>
		public static void Trace(this ILogger logger, Func<string> detail)
		{
			Contract.Requires(logger != null);
			Contract.Requires(detail != null);

			logger.Log(LogLevel.Trace, detail);
		}
		/// <summary>
		/// Log trace information. Since trace logging is often disabled,
		/// Func&lt;string&gt; is the preferred way of logging trace information.
		/// Use this only when message is already evaluated.
		/// </summary>
		/// <param name="logger">logging service</param>
		/// <param name="message">log message</param>
		public static void Trace(this ILogger logger, string message)
		{
			Contract.Requires(logger != null);

			logger.Log(LogLevel.Trace, message);
		}
		/// <summary>
		/// Log various information. To optimize in case of disabled logging,
		/// Func&lt;string&gt; is the preferred way of logging.
		/// </summary>
		/// <param name="logger">logging service</param>
		/// <param name="detail">log message</param>
		public static void Info(this ILogger logger, Func<string> detail)
		{
			Contract.Requires(logger != null);

			logger.Log(LogLevel.Info, detail);
		}
		/// <summary>
		/// Log various information. To optimize in case of disabled logging,
		/// Func&lt;string&gt; is the preferred way of logging.
		/// Use this when message is already evaluated.
		/// </summary>
		/// <param name="logger">logging service</param>
		/// <param name="message">log message</param>
		public static void Info(this ILogger logger, string message)
		{
			Contract.Requires(logger != null);

			logger.Log(LogLevel.Info, message);
		}
		/// <summary>
		/// Log errors. Since error logging should usually be enabled,
		/// use this function only when message is heavy for construction.
		/// </summary>
		/// <param name="logger">logging service</param>
		/// <param name="detail">log message</param>
		public static void Error(this ILogger logger, Func<string> detail)
		{
			Contract.Requires(logger != null);

			logger.Log(LogLevel.Error, detail);
		}
		/// <summary>
		/// Log errors. Since error logging should usually be enabled,
		/// this is the preferred way to log errors.
		/// </summary>
		/// <param name="logger">logging service</param>
		/// <param name="detail">log message</param>
		public static void Error(this ILogger logger, string message)
		{
			Contract.Requires(logger != null);

			logger.Log(LogLevel.Error, message);
		}
		/// <summary>
		/// Log unrecoverable errors on which some human action should be taken. 
		/// Fatal errors logging should always be enabled and 
		/// call this only when message construction matches the API signature.
		/// </summary>
		/// <param name="logger">logging service</param>
		/// <param name="detail">log message</param>
		public static void Fatal(this ILogger logger, Func<string> detail)
		{
			Contract.Requires(logger != null);

			logger.Log(LogLevel.Fatal, detail);
		}
		/// <summary>
		/// Log unrecoverable errors on which some human action should be taken. 
		/// Fatal errors logging should always be enabled and 
		/// this is the preferred way to log fatal errors.
		/// </summary>
		/// <param name="logger">logging service</param>
		/// <param name="detail">log message</param>
		public static void Fatal(this ILogger logger, string message)
		{
			Contract.Requires(logger != null);

			logger.Log(LogLevel.Fatal, message);
		}
	}
}
