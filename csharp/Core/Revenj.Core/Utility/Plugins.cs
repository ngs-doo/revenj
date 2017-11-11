using System.Configuration;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text.RegularExpressions;

namespace Revenj.Utility
{
	internal static class Plugins
	{
		private static Regex[] FileExclusions;
		private static Regex[] AssemblyExclusions;

		static Plugins()
		{
			var fileExclusions = ConfigurationManager.AppSettings["Plugins.FileExclusions"];
			fileExclusions = (!string.IsNullOrWhiteSpace(fileExclusions) ? fileExclusions + "," : "")
				+ "Microsoft.*;Mono.*;Oracle.DataAccess*;Revenj.DatabasePersistence.Oracle*";
			FileExclusions = fileExclusions.Split(new[] { ':', ';' }).Select(it => new Regex("^" + Regex.Escape(it).Replace("\\?", ".").Replace("\\*", ".*"))).ToArray();
			var assemblyExclusions = ConfigurationManager.AppSettings["Plugins.AssemblyExclusions"];
			assemblyExclusions = (!string.IsNullOrWhiteSpace(assemblyExclusions) ? assemblyExclusions + "," : "") +
				"Microsoft,;Microsoft.*;Mono,;Mono.*;System,;System.*;mscorlib,;Oracle.DataAccess*;Revenj.DatabasePersistence.Oracle*";
			AssemblyExclusions = assemblyExclusions.Split(new[] { ':', ';' }).Select(it => new Regex("^" + Regex.Escape(it).Replace("\\?", ".").Replace("\\*", ".*"))).ToArray();
		}

		public static bool ExcludeFile(string filePath)
		{
			if (string.IsNullOrWhiteSpace(filePath)) return true;

			var filename = Path.GetFileNameWithoutExtension(filePath);
			return FileExclusions.Any(match => match.IsMatch(filename));
		}

		public static bool ExcludeAssembly(Assembly assembly)
		{
			if (assembly == null || assembly.IsDynamic) return true;

			return AssemblyExclusions.Any(match => match.IsMatch(assembly.FullName));
		}

		public static bool ExcludeAssembly(AssemblyName assembly)
		{
			return AssemblyExclusions.Any(match => match.IsMatch(assembly.FullName));
		}
	}
}