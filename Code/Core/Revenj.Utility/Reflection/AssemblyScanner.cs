using System;
using System.Collections.Generic;
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
		/// Cache for <see cref="GetAllAssemblies"/>
		/// </summary>
		private static List<Assembly> AllAssemblies = null;

		/// <summary>
		/// Cache for <see cref="GetAllTypes"/>
		/// </summary>
		private static List<Type> AllTypes = null;

		/// <summary>
		/// Gets currently loaded assemblies from current AppDomain, excluding dynamic or Microsoft specific
		/// assemblies.
		/// </summary>
		/// <returns>Currently loaded assemblies.</returns>
		public static IEnumerable<Assembly> GetLoadedAssemblies()
		{
			return
				from asm in AppDomain.CurrentDomain.GetAssemblies()
				where !asm.IsDynamic
					&& !asm.FullName.StartsWith("Microsoft.")
					&& !asm.FullName.StartsWith("mscorelib")
				select asm;
		}

		/// <summary>
		/// Get all types from assemblies.
		/// Types will be cached after first call.
		/// </summary>
		/// <returns>Types in assemblies</returns>
		public static IEnumerable<Type> GetAllTypes()
		{
			if (AllTypes != null)
			{
				return AllTypes;
			}
			else
			{
				try
				{
					AllTypes = new List<Type>();
					foreach (var assembly in GetAllAssemblies())
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
					AllTypes = null;
					var first = (ex.LoaderExceptions ?? new Exception[0]).Take(5).ToList();
					throw new ApplicationException(string.Format("Can't load types:{0}{1}", Environment.NewLine, string.Join(Environment.NewLine, first.Select(it => it.Message))), ex);
				}
			}
		}

		/// <summary>
		/// Gets all referenced assemblies from current AppDomain, excluding dynamic or Microsoft-specific assemblies.
		/// Assemblies will be cached after first call.
		/// </summary>
		/// <returns>All referenced assemblies.</returns>
		public static IEnumerable<Assembly> GetAllAssemblies()
		{
			if (AllAssemblies != null)
			{
				return AllAssemblies;
			}
			else
			{
				var loadedAssemblies = GetLoadedAssemblies();
				foreach (var assembly in loadedAssemblies)
				{
					LoadReferencedAssembiles(assembly);
				}
				AllAssemblies = GetLoadedAssemblies().ToList();
				return AllAssemblies;
			}
		}

		/// <summary>
		/// Loads all assemblies referenced by the specified assembly.
		/// </summary>
		/// <param name="assembly">Assembly whose references to load.</param>
		public static void LoadReferencedAssembiles(Assembly assembly)
		{
			var references = assembly.GetReferencedAssemblies();
			foreach (var reference in references)
			{
				Assembly.Load(reference);
			}
		}
	}
}
