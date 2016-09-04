using System;
using System.Configuration;
using System.Text;

namespace Revenj.Utility
{
	/// <summary>
	/// Utility for exception reporting.
	/// Check if application debug mode is enabled.
	/// Unroll exceptions in better description.
	/// </summary>
	public static class Exceptions
	{
		/// <summary>
		/// Check if application is running in debug mode.
		/// Set in application config (configuration/appSettings) as &lt;add key="ApplicationMode" value="Debug"/&gt;
		/// </summary>
		public static bool DebugMode { get; set; }

		static Exceptions()
		{
			DebugMode = "Debug".Equals(ConfigurationManager.AppSettings["ApplicationMode"], StringComparison.InvariantCultureIgnoreCase);
		}
		/// <summary>
		/// Get messages for this exception.
		/// Unroll exception stack to single message. 
		/// Aggregate exceptions will be unrolled too.
		/// Only exception message is used.
		/// </summary>
		/// <param name="exception">top exception</param>
		/// <returns>error message</returns>
		public static string GetMessages(this Exception exception)
		{
			var cur = exception;
			var sb = new StringBuilder();
			do
			{
				sb.AppendLine(cur.Message);
				var agex = cur as AggregateException;
				if (agex != null)
					foreach (var ex in agex.InnerExceptions)
						sb.AppendLine(ex.Message);
				cur = cur.InnerException;
			} while (cur != null);
			return sb.ToString();
		}
		/// <summary>
		/// Get detailed messages for this exception.
		/// Unroll exception stack to single message. 
		/// Aggregate exceptions will be unrolled too.
		/// Whole stack trace will be used for each exception.
		/// </summary>
		/// <param name="exception">top exception</param>
		/// <returns>error message</returns>
		public static string GetDetailedExplanation(this Exception exception)
		{
			var cur = exception;
			var sb = new StringBuilder();
			int level = 0;
			while (cur != null && level++ < 20)
			{
				sb.AppendLine(cur.ToString());
				var agex = cur as AggregateException;
				if (agex != null)
					foreach (var ex in agex.InnerExceptions)
						sb.AppendLine(ex.ToString());
				cur = cur.InnerException;
			}
			return sb.ToString();
		}
	}
}
