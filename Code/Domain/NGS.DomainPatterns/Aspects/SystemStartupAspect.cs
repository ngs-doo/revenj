using System;
using System.ComponentModel.Composition;
using System.Configuration;
using System.Linq;
using NGS.Extensibility;
using NGS.Utility;

namespace NGS.DomainPatterns
{
	[Export(typeof(ISystemAspect))]
	public class SystemStartupAspect : ISystemAspect
	{
		public void Initialize(IObjectFactory factory)
		{
			var types =
				(from type in AssemblyScanner.GetAllTypes()
				 where type.IsClass
				 where type.IsPublic || type.IsNestedPublic
				 where typeof(ISystemStartup).IsAssignableFrom(type)
				 select type)
				.ToList();
			var state = new Lazy<ISystemState>(() => factory.Resolve<ISystemState>());
			foreach (var t in types)
			{
				ISystemStartup si;
				try
				{
					si = (ISystemStartup)Activator.CreateInstance(t);
				}
				catch (Exception ex)
				{
					throw new ConfigurationErrorsException("Error loading " + t.FullName + ". " + ex.Message, ex);
				}
				state.Value.Ready += f => si.Configure(f.Resolve<IServiceLocator>());
			}
		}
	}
}
