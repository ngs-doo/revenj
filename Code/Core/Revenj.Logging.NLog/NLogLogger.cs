using System;
using NLog;

namespace Revenj.Logging.NLog
{
	public class NLogLogger : ILogger
	{
		private readonly Logger Logger;

		public NLogLogger(string name)
		{
			Logger = LogManager.GetLogger(name);
		}

		public void Log(LogLevel level, Func<string> detail)
		{
			switch (level)
			{
				case LogLevel.Debug:
					Logger.Debug((LogMessageGenerator)(() => detail()));
					break;
				case LogLevel.Trace:
					Logger.Trace((LogMessageGenerator)(() => detail()));
					break;
				case LogLevel.Info:
					Logger.Info((LogMessageGenerator)(() => detail()));
					break;
				case LogLevel.Error:
					Logger.Error((LogMessageGenerator)(() => detail()));
					break;
				case LogLevel.Fatal:
					Logger.Fatal((LogMessageGenerator)(() => detail()));
					break;
			}
		}


		public void Log(LogLevel level, string message)
		{
			switch (level)
			{
				case LogLevel.Debug:
					Logger.Debug(message);
					break;
				case LogLevel.Trace:
					Logger.Trace(message);
					break;
				case LogLevel.Info:
					Logger.Info(message);
					break;
				case LogLevel.Error:
					Logger.Error(message);
					break;
				case LogLevel.Fatal:
					Logger.Fatal(message);
					break;
			}
		}
	}
}
