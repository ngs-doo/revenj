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
			/*Platform.Container container;
			if (!Enum.TryParse<Platform.Container>(ConfigurationManager.AppSettings["Revenj.Container"], out container))
				container = Platform.Container.Autofac;*/
			Console.WriteLine("Starting server");
			if (httpServer == "Socket" || httpServer == "Revenj")
			{
				var server = Platform.Start<HttpSocketServer>();//container);
				server.Run();
			}
			else
			{
				try
				{
					var server = Platform.Start<HttpListenerServer>();//container);
					server.Run();
				}
				catch (Exception ex)
				{
					var tle = ex.InnerException as TypeLoadException;
					if (tle != null && tle.TypeName == "System.Net.HttpListener")
					{
						throw new TypeLoadException(@"Unable to load HttpListener. 
Newer Mono versions (4.2+) have incompatible Mono.Security.
Either delete Mono.Security.dll from the Revenj folder so it can use Mono default one,
use an older Mono version (pre 4.2) or use Revenj builtin web server.
To run Revenj builtin web server add Revenj.HttpServer=Revenj to command line or add
	<add key=""Revenj.HttpServer"" value=""Socket""/>
to <appSettings>", ex.InnerException);
					}
					throw;
				}
			}
		}
	}
}
