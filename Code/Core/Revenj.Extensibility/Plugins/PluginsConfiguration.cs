using System.Collections.Generic;
using System.Reflection;

namespace Revenj.Extensibility
{
	internal class PluginsConfiguration
	{
		public IEnumerable<string> Directories { get; set; }
		public IEnumerable<Assembly> Assemblies { get; set; }
	}
}
