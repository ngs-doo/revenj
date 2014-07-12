using System.ComponentModel.Composition;
using System.Linq;
using NGS.Extensibility;
using NGS.Utility;

namespace NGS.DomainPatterns
{
	[Export(typeof(ISystemAspect))]
	public class EventProcessorAspect : ISystemAspect
	{
		public void Initialize(IObjectFactory factory)
		{
			foreach (var type in AssemblyScanner.GetAllTypes().Where(it => !it.IsAbstract && it.IsClass))
			{
				var interfaces =
					(from i in type.GetInterfaces()
					 where i.IsGenericType
						 && i.GetGenericTypeDefinition() == typeof(IDomainEventHandler<>)
					 select i).ToList();
				if (interfaces.Count > 0)
				{
					var attr = type.GetCustomAttributes(typeof(ServiceAttribute), false) as ServiceAttribute[];
					if (attr == null || attr.Length == 0)
					{
						factory.RegisterType(type);
						foreach (var i in interfaces)
							factory.RegisterType(type, i, InstanceScope.Transient);
					}
				}
			}
		}
	}
}
