using System;
using System.Configuration;
using System.Data;
using System.Diagnostics;
using System.Diagnostics.Contracts;
using System.Text;
using NGS;
using NGS.Logging;

namespace Revenj.Plugins.Aspects.DatabaseTrace
{
	public class QueryInterceptor
	{
		private static readonly int TimerLimit;

		static QueryInterceptor()
		{
			if (!int.TryParse(ConfigurationManager.AppSettings["Performance.DatabaseLimit"], out TimerLimit))
				TimerLimit = 100;
		}

		private readonly ILogger Logger;

		public QueryInterceptor(ILogFactory logFactory)
		{
			Contract.Requires(logFactory != null);

			this.Logger = logFactory.Create("Database performance trace");
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
					Logger.Trace(() => "Execute non query duration: {0}ms, Sql: {1}".With(duration, command.CommandText));
			}
		}

		public int LogExecuteNonQuery(StringBuilder builder, Func<StringBuilder, int> query)
		{
			var start = Stopwatch.GetTimestamp();
			try
			{
				return query(builder);
			}
			finally
			{
				var duration = (Stopwatch.GetTimestamp() - start) / TimeSpan.TicksPerMillisecond;
				if (duration > TimerLimit)
				{
					Logger.Trace(() =>
						"Execute non query duration: {0}ms, Sql: {1}".With(
							duration,
							builder.ToString(0, Math.Min(10000, builder.Length))));
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
					Logger.Trace(() => "Execute data reader duration: {0}ms, Sql: {1}".With(duration, command.CommandText));
			}
		}

		public int LogFillTable(
			IDbCommand command,
			DataTable table,
			Func<IDbCommand, DataTable, int> query)
		{
			var start = Stopwatch.GetTimestamp();
			try
			{
				return query(command, table);
			}
			finally
			{
				var duration = (Stopwatch.GetTimestamp() - start) / TimeSpan.TicksPerMillisecond;
				if (duration > TimerLimit)
					Logger.Trace(() => "Fill table duration: {0}ms, Sql: {1}".With(duration, command.CommandText));
			}
		}
	}
}
