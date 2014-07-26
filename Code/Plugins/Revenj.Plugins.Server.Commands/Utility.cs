using System;
using System.Globalization;
using Revenj.DomainPatterns;

namespace Revenj.Plugins.Server.Commands
{
	internal static class Utility
	{
		public static Type FindNested(this IDomainModel dom, string type, string name)
		{
			return dom.Find(string.Format(CultureInfo.InvariantCulture, "{0}+{1}", type, name));
		}
	}
}
