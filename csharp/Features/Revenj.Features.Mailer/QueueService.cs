using System;
using System.ComponentModel.Composition;
using System.ServiceProcess;
using Revenj.DomainPatterns;
using Revenj.Extensibility;

namespace Revenj.Features.Mailer
{
	[Export(typeof(ServiceBase))]
	[ExportMetadata(Metadata.ClassType, typeof(QueueService))]
	public partial class QueueService : ServiceBase
	{
		private readonly QueueProcessor Processor;

		public QueueService(IServiceProvider locator)
		{
			Processor = locator.Resolve<QueueProcessor>();
			InitializeComponent();
		}

		protected override void OnStart(string[] args)
		{
			Processor.Start();
		}

		protected override void OnStop()
		{
			Processor.Stop();
		}
	}
}
