using System;
using System.ComponentModel.Composition;
using System.Configuration;
using Revenj.Extensibility;
using Revenj.Utility;

namespace Revenj.DomainPatterns
{
	[Export(typeof(ISystemAspect))]
	public class SystemStartupAspect : ISystemAspect
	{
		public void Initialize(IObjectFactory factory)
		{
			var state = new Lazy<ISystemState>(() => factory.Resolve<ISystemState>());
			ISystemStartup si;
			foreach (var t in AssemblyScanner.GetAllTypes())
			{
				if (t.IsClass && (t.IsPublic || t.IsNestedPublic)
					&& typeof(ISystemStartup).IsAssignableFrom(t))
				{
					try
					{
						si = (ISystemStartup)Activator.CreateInstance(t);
					}
					catch (Exception ex)
					{
						throw new ConfigurationErrorsException("Error loading " + t.FullName + ". " + ex.Message, ex);
					}
					state.Value.Ready += f => si.Configure(f.Resolve<IServiceProvider>());
				}
			}
		}
	}
}
