using System.Reflection;
using System.ServiceProcess;

namespace Revenj.WindowsService
{
	partial class HostService : ServiceBase
	{
		private readonly ServiceBase[] Services;
		private readonly MethodInfo OnStartMethod;

		public HostService(ServiceBase[] services)
		{
			this.Services = services;
			InitializeComponent();

			OnStartMethod = typeof(ServiceBase).GetMethod("OnStart", BindingFlags.NonPublic | BindingFlags.Instance);
		}

		protected override void OnStart(string[] args)
		{
			foreach (var s in Services)
				OnStartMethod.Invoke(s, new object[] { args });
		}

		protected override void OnStop()
		{
			foreach (var s in Services)
				s.Stop();
		}
	}
}
