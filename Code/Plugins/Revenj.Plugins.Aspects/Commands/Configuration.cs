using System.ComponentModel.Composition;
using System.Configuration;
using System.IO;
using NGS.Extensibility;
using Revenj.Api;
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
			factory.RegisterTypes(new[] { typeof(ProcessingCommandsIntercepter), typeof(RestCommandsIntercepter) });
			var processingInterceptor = factory.Resolve<ProcessingCommandsIntercepter>();
			var restInterceptor = factory.Resolve<RestCommandsIntercepter>();
			var aspectRegistrator = factory.Resolve<IAspectRegistrator>();

			var tpe = typeof(IProcessingEngine);
			var tcc = typeof(ICommandConverter);
			var tre = typeof(IRestApplication);
			aspectRegistrator.Before(
				tpe,
				tpe.GetMethod("Execute"),
				(e, args) => processingInterceptor.LogCommands((dynamic)(args[0])));
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
		}
	}
}
