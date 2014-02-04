namespace NGS.Logging.NLog
{
	public class LogFactory : ILogFactory
	{
		public ILogger Create(string name)
		{
			return new NLogLogger(name);
		}
	}
}
