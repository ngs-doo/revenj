using System.ComponentModel.Composition;
using System.ServiceProcess;
using NGS.DomainPatterns;
using NGS.Extensibility;

namespace NGS.Features.Mailer
{
	[Export(typeof(ServiceBase))]
	[ExportMetadata(Metadata.ClassType, typeof(QueueService))]
	public partial class QueueService : ServiceBase
	{
		private readonly QueueProcessor Processor;

		public QueueService(IServiceLocator locator)
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
