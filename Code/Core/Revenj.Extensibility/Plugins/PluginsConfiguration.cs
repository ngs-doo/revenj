using System.Collections.Generic;
using System.Reflection;

namespace Revenj.Extensibility
{
	public class PluginsConfiguration
	{
		public IEnumerable<string> Directories { get; set; }
		public IEnumerable<Assembly> Assemblies { get; set; }
	}
}
