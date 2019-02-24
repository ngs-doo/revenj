using System;
using System.Configuration;
using System.Data;
using System.Diagnostics;

namespace Revenj.Plugins.Aspects.DatabaseTrace
{
	public class QueryInterceptor
	{
		private static readonly int TimerLimit;
		private static readonly TraceSource TraceSource = new TraceSource("Revenj.Aspects");

		static QueryInterceptor()
		{
			if (!int.TryParse(ConfigurationManager.AppSettings["Performance.DatabaseLimit"], out TimerLimit))
				TimerLimit = 100;
		}

		public int LogExecuteNonQuery(IDbCommand command, Func<IDbCommand, int> query)
		{
			var start = Stopwatch.GetTimestamp();
			try
			{
				return query(command);
			}
			finally
			{
				var duration = (Stopwatch.GetTimestamp() - start) / TimeSpan.TicksPerMillisecond;
				if (duration > TimerLimit)
				{
					TraceSource.TraceEvent(
						TraceEventType.Information,
						3141,
						"Execute non query duration: {0} ms, SQL: {1}",
						duration,
						command.CommandText);
				}
			}
		}

		public void LogExecuteDataReader(
			IDbCommand command,
			Action<IDataReader> action,
			Action<IDbCommand, Action<IDataReader>> query)
		{
			var start = Stopwatch.GetTimestamp();
			try
			{
				query(command, action);
			}
			finally
			{
				var duration = (Stopwatch.GetTimestamp() - start) / TimeSpan.TicksPerMillisecond;
				if (duration > TimerLimit)
				{
					TraceSource.TraceEvent(
						TraceEventType.Information,
						3142,
						"Execute data reader duration: {0} ms, SQL: {1}",
						duration,
						command.CommandText);
				}
			}
		}

		public int LogFillTable(
			IDbCommand command,
			DataSet results,
			Func<IDbCommand, DataSet, int> query)
		{
			var start = Stopwatch.GetTimestamp();
			try
			{
				return query(command, results);
			}
			finally
			{
				var duration = (Stopwatch.GetTimestamp() - start) / TimeSpan.TicksPerMillisecond;
				if (duration > TimerLimit)
				{
					TraceSource.TraceEvent(
						TraceEventType.Information,
						3143,
						"Fill table duration: {0} ms, SQL: {1}",
						duration,
						command.CommandText);
				}
			}
		}
	}
}
