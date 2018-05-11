using System;
using System.Configuration;
using DSL;

namespace Revenj.Http
{
	static class Program
	{
		static void Main(string[] args)
		{
			var httpServer = ConfigurationManager.AppSettings["Revenj.HttpServer"];
			try
			{
				foreach (var arg in args)
				{
					var i = arg.IndexOf('=');
					if (i != -1)
					{
						var name = arg.Substring(0, i);
						var value = arg.Substring(i + 1);
						//TODO: Mono doesn't support changing app settings. Make exception for specifying web server
						if (name == "Revenj.HttpServer") httpServer = value;
						else ConfigurationManager.AppSettings[name] = value;
					}
				}
			}
			catch (NotSupportedException ex)
			{
				throw new NotSupportedException(@"Mono has read only appSettings. 
They can't be changed in runtime via command line arguments.
Specify arguments in config file using <add key=... value=... />", ex);
			}
			Console.WriteLine("Starting server");
			if (httpServer == "Socket" || httpServer == "Revenj")
			{
				var server = Platform.Start<HttpSocketServer>();
				server.Run();
			}
			else
			{
				var server = Platform.Start<HttpListenerServer>();
				server.Run();
			}
		}
	}
}
