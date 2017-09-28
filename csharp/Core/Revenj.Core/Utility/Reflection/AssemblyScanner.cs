using Revenj.Core.Utility;
using System;
using System.Collections.Generic;
using System.Configuration;
using System.IO;
using System.Linq;
using System.Reflection;

namespace Revenj.Utility
{
	/// <summary>
	/// Utility for scanning and loading assemblies in current AppDomain.
	/// Microsoft-specific assemblies will be skipped.
	/// </summary>
	public static class AssemblyScanner
	{
		/// <summary>
		/// Cache for <see cref="GetAssemblies"/>
		/// </summary>
		private static readonly List<Assembly> AllAssemblies = new List<Assembly>();
		private static readonly Dictionary<string, string> AssemblyNames = new Dictionary<string, string>();

		/// <summary>
		/// Cache for <see cref="GetAllTypes"/>
		/// </summary>
		private static readonly List<Type> AllTypes = new List<Type>();

		/// <summary>
		/// Gets all referenced assemblies from current AppDomain, excluding dynamic or Microsoft-specific assemblies.
		/// Assemblies will be cached after first call.
		/// </summary>
		/// <returns>All referenced assemblies.</returns>
		public static IEnumerable<Assembly> GetAssemblies()
		{
			if (AllAssemblies.Count == 0)
			{
				var exclusions = ConfigurationManager.AppSettings["PluginsExclusions"];
				exclusions = (!string.IsNullOrWhiteSpace(exclusions) ? exclusions + "," : "") + 
					"Microsoft,Microsoft.*,Mono,Mono.*,System,System.*,mscorlib,Oracle.DataAccess*,Revenj.DatabasePersistence.Oracle*";

				foreach (var refAsm in AppDomain.CurrentDomain.GetAssemblies().Where(it => !it.IsDynamic))
				{
					AssemblyNames[refAsm.FullName] = Path.GetFileNameWithoutExtension(refAsm.Location);
					foreach (var asm in refAsm.GetReferencedAssemblies())
					{
						AssemblyNames[asm.FullName] = asm.Name;
					}
				}

				var assemblyNames = AssemblyNames.Where(it => !Util.FilenameMatch(it.Value, exclusions)).ToList();
				foreach (var assemblyName in assemblyNames)
				{
					try
					{
						var asm = Assembly.Load(assemblyName.Key);
						if (!asm.IsDynamic)
							AllAssemblies.Add(asm);
					}
					catch (Exception ex)
					{
						System.Diagnostics.Debug.WriteLine(ex.Message);
					}
				}
			}
			return AllAssemblies;
		}

		/// <summary>
		/// Get all types from assemblies.
		/// Types will be cached after first call.
		/// </summary>
		/// <returns>Types in assemblies</returns>
		public static IEnumerable<Type> GetAllTypes()
		{
			if (AllTypes.Count != 0)
				return AllTypes;

			try
			{
				foreach (var assembly in GetAssemblies())
				{
					foreach (var type in assembly.GetTypes().Where(it => it.IsClass || it.IsInterface))
					{
						AllTypes.Add(type);
					}
				}
				return AllTypes;
			}
			catch (ReflectionTypeLoadException ex)
			{
				AllTypes.Clear();
				var first = (ex.LoaderExceptions ?? new Exception[0]).Take(5).ToList();
				throw new ApplicationException(string.Format("Can't load types:{0}{1}", Environment.NewLine, string.Join(Environment.NewLine, first.Select(it => it.Message))), ex);
			}
		}
	}
}
