using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;

namespace NGS.Utility
{
	/// <summary>
	/// Utility for scanning assemblies in current AppDomain.
	/// Microsoft specific assemblies will be skiped.
	/// </summary>
	public static class AssemblyScanner
	{
		private static List<Type> AllTypes = new List<Type>();
		/// <summary>
		/// Get assemblies from current AppDomain.
		/// Don't return dynamic or Microsoft specific assemblies.
		/// </summary>
		/// <returns>found assemblies</returns>
		public static IEnumerable<Assembly> GetAssemblies()
		{
			return
				from asm in AppDomain.CurrentDomain.GetAssemblies()
				where !asm.IsDynamic
					&& !asm.FullName.StartsWith("Microsoft.")
					&& !asm.FullName.StartsWith("mscorelib.")
				select asm;
		}
		/// <summary>
		/// Get all types from assemblies.
		/// Types will be cached after first call.
		/// </summary>
		/// <returns>Types in assemblies</returns>
		public static IEnumerable<Type> GetAllTypes()
		{
			if (AllTypes.Count > 0)
				return AllTypes.ToArray();
			try
			{
				foreach (var asm in GetAssemblies())
					foreach (var type in asm.GetTypes().Where(it => it.IsClass || it.IsInterface))
						AllTypes.Add(type);

				return AllTypes.ToArray();
			}
			catch (ReflectionTypeLoadException ex)
			{
				var first = (ex.LoaderExceptions ?? new Exception[0]).Take(5).ToList();
				throw new ApplicationException(string.Format(@"Can't load types: 
{0}
", string.Join(Environment.NewLine, first.Select(it => it.Message))), ex);
			}
		}
	}
}
