using System.ComponentModel.Composition;
using System.Linq;
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
					factory.RegisterType(type, attr[0].Scope, new[] { type }.Union(type.GetInterfaces()).ToArray());
			}
		}
	}
}
