using System;
using System.Configuration;
using DSL;
using Microsoft.Owin.Hosting;
using Owin;
using Revenj.DomainPatterns;

namespace Revenj.SignalR2SelfHost
{
	class Program
	{
		static void Main(string[] args)
		{
			var address = ConfigurationManager.AppSettings["HttpAddress"];
			if (args.Length == 1)
				address = args[0];
			if (address == null && args.Length == 0)
			{
				Console.WriteLine("HttpAddress not defined in config and no address passed as command line argument");
				return;
			}
			var locator = Platform.Start<IServiceLocator>();
			NotifyHub.Model = locator.Resolve<IDomainModel>();
			NotifyHub.ChangeNotification = locator.Resolve<IDataChangeNotification>();
			using (WebApp.Start<Startup>(address))
			{
				Console.WriteLine("SignalR started. Listening on " + address);
				Console.WriteLine("Press any key to exit.");
				Console.ReadLine();
			}
		}
	}

	public class Startup
	{
		public void Configuration(IAppBuilder app)
		{
			app.MapSignalR();
		}
	}
}
