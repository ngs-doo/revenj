using System;
using System.Configuration;
using System.Diagnostics;
using System.IO;
using Revenj.Api;

namespace Revenj.Plugins.Aspects.Commands
{
	public class RestCommandsIntercepter
	{
		private static readonly int TimerLimit;
		private static readonly TraceSource TraceSource = new TraceSource("Revenj.Aspects");

		static RestCommandsIntercepter()
		{
			if (!int.TryParse(ConfigurationManager.AppSettings["Performance.CommandLimit"], out TimerLimit))
				TimerLimit = 100;
		}

		private void LogMemoryUsage()
		{
			TraceSource.TraceEvent(
				TraceEventType.Information,
				3121,
				"GC memory usage: {0:N3} MB",
				GC.GetTotalMemory(false) / 1024 / 1024m);
			TraceSource.TraceEvent(
				TraceEventType.Information,
				3122,
				"Process memory usage: {0:N3} MB",
				Process.GetCurrentProcess().WorkingSet64 / 1024 / 1024m);
		}

		public Stream PassThrough(object[] args, Func<object[], object> baseCall)
		{
			var start = Stopwatch.GetTimestamp();
			var ta = args[0] != null ? args[0].GetType() : typeof(object);
			TraceSource.TraceEvent(
				TraceEventType.Information,
				3123,
				"Executing command with argument: {0}. Request: {1}",
				ta.FullName,
				ThreadContext.Request);
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
					TraceSource.TraceEvent(
						TraceEventType.Information,
						3124,
						"Returning stream for pass through: {0}. Duration: {1} ms. Size: {2}",
						ta.FullName,
						elapsed,
						stream != null && stream.CanSeek ? stream.Length.ToString() + " bytes" : "Unknown");
					LogMemoryUsage();
				}
			}
			return stream;
		}

		public Stream Get(Func<Stream> baseCall)
		{
			var start = Stopwatch.GetTimestamp();
			var req = ThreadContext.Request;
			TraceSource.TraceEvent(TraceEventType.Information, 3125, "Executing get command. Request: {0}", req);
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
					TraceSource.TraceEvent(
						TraceEventType.Information,
						3126,
						"Returning stream for get REST req: {0}. Duration: {1} ms. Size: {2}",
						req.RequestUri.AbsoluteUri,
						elapsed,
						stream != null && stream.CanSeek ? stream.Length.ToString() + " bytes" : "Unknown");
					LogMemoryUsage();
				}
			}
			return stream;
		}

		public Stream Post(Stream arg, Func<Stream, Stream> baseCall)
		{
			var start = Stopwatch.GetTimestamp();
			var req = ThreadContext.Request;
			TraceSource.TraceEvent(TraceEventType.Information, 3127, "Executing post command. Request: {0}", req);
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
					TraceSource.TraceEvent(
						TraceEventType.Information,
						3126,
						"Returning stream for post REST req: {0}. Duration: {1} ms. Size: {2}",
						req.RequestUri.AbsoluteUri,
						elapsed,
						stream != null && stream.CanSeek ? stream.Length.ToString() + " bytes" : "Unknown");
					LogMemoryUsage();
				}
			}
			return stream;
		}
	}
}
