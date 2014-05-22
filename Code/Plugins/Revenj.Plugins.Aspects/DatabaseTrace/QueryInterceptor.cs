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
			var sw = Stopwatch.StartNew();
			try
			{
				return query(command);
			}
			finally
			{
				sw.Stop();
				if (sw.ElapsedMilliseconds > TimerLimit)
				{
					var msg =
						"Execute non query duration: {0}ms, Sql: {1}".With(
							sw.ElapsedMilliseconds,
							command.CommandText);
					Logger.Trace(() => msg);
				}
			}
		}

		public int LogExecuteNonQuery(StringBuilder builder, Func<StringBuilder, int> query)
		{
			var sw = Stopwatch.StartNew();
			try
			{
				return query(builder);
			}
			finally
			{
				sw.Stop();
				if (sw.ElapsedMilliseconds > TimerLimit)
				{
					Logger.Trace(() =>
						"Execute non query duration: {0}ms, Sql: {1}".With(
							sw.ElapsedMilliseconds,
							builder.ToString(0, Math.Min(10000, builder.Length))));
				}
			}
		}

		public void LogExecuteDataReader(
			IDbCommand command,
			Action<IDataReader> action,
			Action<IDbCommand, Action<IDataReader>> query)
		{
			var sw = Stopwatch.StartNew();
			try
			{
				query(command, action);
			}
			finally
			{
				sw.Stop();
				if (sw.ElapsedMilliseconds > TimerLimit)
				{
					var msg =
						"Execute data reader duration: {0}ms, Sql: {1}".With(
							sw.ElapsedMilliseconds,
							command.CommandText);
					Logger.Trace(() => msg);
				}
			}
		}

		public int LogFillTable(
			IDbCommand command,
			DataTable table,
			Func<IDbCommand, DataTable, int> query)
		{
			var sw = Stopwatch.StartNew();
			try
			{
				return query(command, table);
			}
			finally
			{
				sw.Stop();
				if (sw.ElapsedMilliseconds > TimerLimit)
				{
					var msg =
						"Fill table duration: {0}ms, Sql: {1}".With(
							sw.ElapsedMilliseconds,
							command.CommandText);
					Logger.Trace(() => msg);
				}
			}
		}
	}
}
