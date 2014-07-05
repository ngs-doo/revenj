using System;
using System.Configuration;
using System.Diagnostics;
using System.Diagnostics.Contracts;
using System.Globalization;
using System.IO;
using NGS.Logging;
using Revenj.Api;

namespace Revenj.Plugins.Aspects.Commands
{
	public class RestCommandsIntercepter
	{
		private static readonly int TimerLimit;

		static RestCommandsIntercepter()
		{
			if (!int.TryParse(ConfigurationManager.AppSettings["Performance.CommandLimit"], out TimerLimit))
				TimerLimit = 100;
		}

		private readonly ILogger Logger;

		public RestCommandsIntercepter(ILogFactory logFactory)
		{
			Contract.Requires(logFactory != null);

			this.Logger = logFactory.Create("REST commands trace");
		}

		public Stream PassThrough(object[] args, Func<object[], object> baseCall)
		{
			var start = Stopwatch.GetTimestamp();
			var ta = args[0] != null ? args[0].GetType() : typeof(object);
			Logger.Debug(() => "Executing command with argument: " + ta.FullName + ". Request: " + ThreadContext.Request.ToString());
			Stream stream = null;
			try
			{
				stream = (Stream)baseCall(args);
			}
			finally
			{
				var elapsed = (decimal)(Stopwatch.GetTimestamp() - start) / TimeSpan.TicksPerMillisecond;
				if (elapsed > TimerLimit)
				{
					Logger.Debug(() => "Returning stream for pass through: " + ta.FullName + ". Duration: " + elapsed + "ms. Size: " + (stream != null && stream.CanSeek ? stream.Length.ToString() + "B" : "Unknown"));
					Logger.Debug(() => "Memory usage: " + (GC.GetTotalMemory(false) / 1024 / 1024m).ToString("N3", CultureInfo.InvariantCulture) + "MB");
				}
			}
			return stream;
		}

		public Stream Get(Func<Stream> baseCall)
		{
			var start = Stopwatch.GetTimestamp();
			var req = ThreadContext.Request;
			Logger.Debug(() => "Executing get command. Request: " + req.ToString());
			Stream stream = null;
			try
			{
				stream = baseCall();
			}
			finally
			{
				var elapsed = (decimal)(Stopwatch.GetTimestamp() - start) / TimeSpan.TicksPerMillisecond;
				if (elapsed > TimerLimit)
				{
					Logger.Debug(() => "Returning stream for get REST req: " + req.RequestUri.AbsoluteUri + ". Duration: " + elapsed + "ms. Size: " + (stream != null && stream.CanSeek ? stream.Length.ToString() + "B" : "Unknown"));
					Logger.Debug(() => "Memory usage: " + (GC.GetTotalMemory(false) / 1024 / 1024m).ToString("N3", CultureInfo.InvariantCulture) + "MB");
				}
			}
			return stream;
		}

		public Stream Post(Stream arg, Func<Stream, Stream> baseCall)
		{
			var start = Stopwatch.GetTimestamp();
			var req = ThreadContext.Request;
			Logger.Debug(() => "Executing post command. Request: " + req.ToString());
			Stream stream = null;
			try
			{
				stream = baseCall(arg);
			}
			finally
			{
				var elapsed = (decimal)(Stopwatch.GetTimestamp() - start) / TimeSpan.TicksPerMillisecond;
				if (elapsed > TimerLimit)
				{
					Logger.Debug(() => "Returning stream for post REST req: " + req.RequestUri.AbsoluteUri + ". Duration: " + elapsed + "ms. Size: " + (stream != null && stream.CanSeek ? stream.Length.ToString() + "B" : "Unknown"));
					Logger.Debug(() => "Memory usage: " + (GC.GetTotalMemory(false) / 1024 / 1024m).ToString("N3", CultureInfo.InvariantCulture) + "MB");
				}
			}
			return stream;
		}
	}
}
