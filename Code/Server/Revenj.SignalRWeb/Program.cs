using System;
using System.Configuration;
using DSL;
using Microsoft.AspNet.SignalR;
using Microsoft.Owin.Hosting;
using NGS.DomainPatterns;
using Owin;
using System.Threading;

namespace Revenj.SignalRWeb
{
	class Program
	{
		static void Main(string[] args)
		{
			string url = ConfigurationManager.AppSettings["Listen"];
			if (string.IsNullOrEmpty(url))
				throw new ConfigurationErrorsException("Missing 'Listen' key in your application config.");

			using (WebApplication.Start<Startup>(url))
			{
				Console.WriteLine("Server running on {0}", url);
				Thread.Sleep(Timeout.Infinite);
			}
		}
	}

	class Startup
	{
		public void Configuration(IAppBuilder app)
		{
			bool cd = false;
			bool.TryParse(ConfigurationManager.AppSettings["SignalR.CrossDomain"], out cd);

			var config = new HubConfiguration { EnableCrossDomain = cd };

			int ct;
			if (int.TryParse(ConfigurationManager.AppSettings["SignalR.ConnectionTimeout"], out ct) && ct > 0)
				GlobalHost.Configuration.ConnectionTimeout = TimeSpan.FromSeconds(ct);
			int dt;
			if (int.TryParse(ConfigurationManager.AppSettings["SignalR.DisconnectTimeout"], out dt) && dt > 0)
				GlobalHost.Configuration.DisconnectTimeout = TimeSpan.FromSeconds(dt);
			int ka;
			if (int.TryParse(ConfigurationManager.AppSettings["SignalR.KeepAlive"], out ka) && ka > 0)
				GlobalHost.Configuration.KeepAlive = TimeSpan.FromSeconds(ka);

			var locator = Platform.Start<IServiceLocator>();
			NotifyHub.Model = locator.Resolve<IDomainModel>();
			NotifyHub.ChangeNotification = locator.Resolve<IDataChangeNotification>();

			app.MapHubs(config);
		}
	}
}
