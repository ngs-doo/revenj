using System;
using System.Linq;
using System.ServiceProcess;
using DSL;
using NGS.Extensibility;

namespace Revenj.WindowsService
{
	static class Program
	{
		static void Main(string[] args)
		{
			var factory = Platform.Start<IObjectFactory>();
			var extensibility = factory.Resolve<IExtensibilityProvider>();
			var plugins = extensibility.FindPlugins<ServiceBase>();
			factory.RegisterTypes(plugins);
			var services =
				(from p in plugins
				 select factory.Resolve<ServiceBase>(p))
				.ToArray();
			if (services.Length == 0)
				throw new ApplicationException("No services found");
			if (args.Length == 1 && (args[0] == "-console" || args[0] == "/console"))
			{
				foreach (var s in services)
					Console.WriteLine(s.GetType());
				Console.WriteLine("Press any key to exit");
				Console.ReadKey();
				foreach (var s in services)
					s.Stop();
			}
			else ServiceBase.Run(new HostService(services));
		}
	}
}
