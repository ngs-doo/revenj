using System.Collections.Generic;
using System.Reflection;

namespace Revenj.Extensibility
{
	internal class PluginsConfiguration
	{
		public List<string> Directories { get; set; }
		public List<Assembly> Assemblies { get; set; }
	}
}
