using System;
using System.Configuration;
using System.Web.Routing;
using DSL;
using Microsoft.AspNet.SignalR;
using Revenj.DomainPatterns;

namespace Revenj.SignalRWeb
{
	public class Global : System.Web.HttpApplication
	{
		protected void Application_Start(object sender, EventArgs e)
		{
			bool cd;
			if (bool.TryParse(ConfigurationManager.AppSettings["SignalR.CrossDomain"], out cd) && cd)
				RouteTable.Routes.MapHubs(new HubConfiguration { EnableCrossDomain = cd });
			else
				RouteTable.Routes.MapHubs();
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
		}

		protected void Application_End(object sender, EventArgs e)
		{
			NotifyHub.Stop();
		}
	}
}