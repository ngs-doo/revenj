using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using NGS.Utility;

namespace NGS.Extensibility
{
	[Export(typeof(ISystemAspect))]
	public class PluginAspect : ISystemAspect
	{
		public void Initialize(IObjectFactory factory)
		{
			var plugins = new Dictionary<Type, InstanceScope>();
			foreach (var type in AssemblyScanner.GetAllTypes())
			{
				var attr = type.GetCustomAttributes(typeof(PluginAttribute), false) as PluginAttribute[];
				if (attr != null && attr.Length == 1)
					plugins.Add(type, attr[0].Scope);
			}
			foreach (var type in AssemblyScanner.GetAllTypes())
			{
				if (type.IsClass && !type.IsAbstract)
				{
					foreach (var i in type.GetInterfaces())
						if (plugins.ContainsKey(i))
							factory.RegisterType(type, i, plugins[i]);
				}
			}
		}
	}
}
