using System.Collections;
using System.ComponentModel;
using System.Configuration.Install;

namespace Revenj.WindowsService
{
	[RunInstaller(true)]
	public partial class ProjectInstaller : Installer
	{
		public ProjectInstaller()
		{
			InitializeComponent();
		}

		public override void Install(IDictionary stateSaver)
		{
			SetServiceName();
			base.Install(stateSaver);
		}

		public override void Uninstall(IDictionary savedState)
		{
			SetServiceName();
			base.Uninstall(savedState);
		}

		private void SetServiceName()
		{
			var contextName = Context.Parameters["servicename"];
			if (!string.IsNullOrEmpty(contextName))
			{
				this.revenjServiceInstaller.ServiceName = contextName;
				this.revenjServiceInstaller.DisplayName = contextName;
			}
		}
	}
}
