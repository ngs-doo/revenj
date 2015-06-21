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
		/// Cache for <see cref="GetAssemblies"/>
		/// </summary>
		private static readonly List<Assembly> AllAssemblies = new List<Assembly>();

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
				AllAssemblies.AddRange(
					from asm in AppDomain.CurrentDomain.GetAssemblies()
					where !asm.IsDynamic
						&& !asm.FullName.StartsWith("Microsoft.")
						&& !asm.FullName.StartsWith("System.")
						&& !asm.FullName.StartsWith("mscorlib")
					select asm);
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
