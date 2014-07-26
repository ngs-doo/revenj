using System.ComponentModel.Composition;
using Revenj.Utility;

namespace Revenj.Extensibility
{
	[Export(typeof(ISystemAspect))]
	public class ServiceAspect : ISystemAspect
	{
		public void Initialize(IObjectFactory factory)
		{
			foreach (var type in AssemblyScanner.GetAllTypes())
			{
				var attr = type.GetCustomAttributes(typeof(ServiceAttribute), false) as ServiceAttribute[];
				if (attr != null && attr.Length == 1)
				{
					var st = attr[0].Scope;
					factory.RegisterType(type, type, st);
					foreach (var i in type.GetInterfaces())
						factory.RegisterType(type, i, st);
				}
			}
		}
	}
}
