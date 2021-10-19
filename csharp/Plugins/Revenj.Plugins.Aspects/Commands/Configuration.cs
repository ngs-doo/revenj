using System.ComponentModel.Composition;
using System.Configuration;
#if !NETSTANDARD2_0
using System.IO;
using Revenj.Api;
#endif
using Revenj.Extensibility;
using Revenj.Processing;

namespace Revenj.Plugins.Aspects.Commands
{
	[Export(typeof(ISystemAspect))]
	public class Configuration : ISystemAspect
	{
		public static bool LogCommands { get; set; }
		public static bool TraceCommands { get; set; }

		static Configuration()
		{
			LogCommands = ConfigurationManager.AppSettings["Revenj.LogProcessingCommands"] == "true";
			TraceCommands = ConfigurationManager.AppSettings["Revenj.TraceRestCommands"] == "true";
		}

		public void Initialize(IObjectFactory factory)
		{
			if (!LogCommands)
				return;
			var aspectRegistrator = factory.Resolve<IAspectRegistrator>();

			var processingInterceptor = new ProcessingCommandsIntercepter();
			var tpe = typeof(IProcessingEngine);
			aspectRegistrator.Before(
				tpe,
				tpe.GetMethod("Execute"),
				(e, args) => processingInterceptor.LogCommands((dynamic)(args[0])));
#if !NETSTANDARD2_0
			var restInterceptor = new RestCommandsIntercepter();
			var tcc = typeof(ICommandConverter);
			var tre = typeof(IRestApplication);
			aspectRegistrator.Around(
				tcc,
				tcc.GetMethod("PassThrough"),
				(e, args, baseCall) => restInterceptor.PassThrough(args, baseCall));
			aspectRegistrator.Around<IRestApplication, Stream>(
				r => r.Get(),
				restInterceptor.Get);
			aspectRegistrator.Around<IRestApplication, Stream, Stream>(
				r => r.Post(null),
				(_, s, bc) => restInterceptor.Post(s, bc));
#endif
		}
	}
}
