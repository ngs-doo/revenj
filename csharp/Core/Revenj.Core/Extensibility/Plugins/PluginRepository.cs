using System;
using System.Collections.Generic;
using System.Diagnostics.Contracts;
using Revenj.Common;

namespace Revenj.Extensibility
{
	internal class PluginRepository<TTarget> : IPluginRepository<TTarget>
	{
		private readonly Dictionary<string, Type> PluginDictionary = new Dictionary<string, Type>();

		public PluginRepository(IExtensibilityProvider extensibilityProvider)
		{
			Contract.Requires(extensibilityProvider != null);

			var list = extensibilityProvider.FindPlugins<TTarget>();

			foreach (var type in list)
			{
				PluginDictionary[type.Name] = type;
				PluginDictionary[type.FullName] = type;
				try
				{
					PluginDictionary.Add(type.AssemblyQualifiedName, type);
				}
				catch (ArgumentException ex)
				{
					throw new FrameworkException(@"Internal error. 
Adding same type twice: {0}.
Check export metadata on type.".With(type.AssemblyQualifiedName), ex);
				}
			}
		}

		public Type Find(string name)
		{
			Type pluginType;
			PluginDictionary.TryGetValue(name, out pluginType);
			return pluginType;
		}
	}
}
