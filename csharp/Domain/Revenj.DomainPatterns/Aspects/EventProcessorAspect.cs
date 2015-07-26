using System.ComponentModel.Composition;
using System.Linq;
using Revenj.Extensibility;
using Revenj.Utility;

namespace Revenj.DomainPatterns
{
	[Export(typeof(ISystemAspect))]
	public class EventProcessorAspect : ISystemAspect
	{
		public void Initialize(IObjectFactory factory)
		{
			foreach (var type in AssemblyScanner.GetAllTypes())
			{
				if (type.IsAbstract || !type.IsClass)
					continue;
				var interfaces =
					(from i in type.GetInterfaces()
					 where i.IsGenericType
						 && i.GetGenericTypeDefinition() == typeof(IDomainEventHandler<>)
					 select i).ToList();
				if (interfaces.Count > 0)
				{
					var attr = type.GetCustomAttributes(typeof(ServiceAttribute), false) as ServiceAttribute[];
					if (attr == null || attr.Length == 0)
						factory.RegisterType(type, InstanceScope.Transient, new[] { type }.Union(interfaces).ToArray());
				}
			}
		}
	}
}
