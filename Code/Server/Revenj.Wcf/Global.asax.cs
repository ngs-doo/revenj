using System;
using System.Web;
using DSL;
using NGS.Logging;

namespace Revenj.Wcf
{
	public class Global : HttpApplication
	{
		protected void Application_Start(object sender, EventArgs e)
		{
			var register = new[] { typeof(RestApplication), typeof(SoapApplication), typeof(CommandConverter) };
			var logger = Platform.Start<ILogFactory>(register);
			logger.Create("Revenj.Wcf").Info("Started at " + DateTime.Now);
		}
	}
}