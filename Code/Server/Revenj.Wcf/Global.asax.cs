using System;
using System.Diagnostics;
using System.Web;
using DSL;
using Revenj.DomainPatterns;

namespace Revenj.Wcf
{
	public class Global : HttpApplication
	{
		private static readonly TraceSource TraceSource = new TraceSource("Revenj.Server");

		protected void Application_Start(object sender, EventArgs e)
		{
			var register = new[] { typeof(RestApplication), typeof(SoapApplication), typeof(CommandConverter) };
			Platform.Start<IServiceLocator>(register);
			TraceSource.TraceEvent(TraceEventType.Start, 1001);
		}

		protected void Application_End(object sender, EventArgs e)
		{
			TraceSource.TraceEvent(TraceEventType.Stop, 1001);
		}
	}
}