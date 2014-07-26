using System.ComponentModel.Composition;
using System.Linq;
using Revenj.Extensibility;
using Revenj.Utility;

namespace Revenj.Processing
{
	[Export(typeof(ISystemAspect))]
	public class ServerServiceAspect : ISystemAspect
	{
		public void Initialize(IObjectFactory factory)
		{
			foreach (var type in AssemblyScanner.GetAllTypes())
			{
				if (!type.IsAbstract
					&& (type.IsPublic || type.IsNestedPublic)
					&& type.GetInterfaces().Any(it => it.IsGenericType && it.GetGenericTypeDefinition() == typeof(IServerService<,>)))
				{
					var attr = type.GetCustomAttributes(typeof(ServiceAttribute), false) as ServiceAttribute[];
					if (attr == null || attr.Length == 0)
						factory.RegisterType(type);
				}
			}
		}
	}
}
