using System;
using System.Collections.Generic;
using System.ComponentModel.Composition.Primitives;
using System.Diagnostics.Contracts;
using System.IO;
using System.Linq;
using System.Reflection;
using Revenj.Common;
using Revenj.Extensibility.Autofac.Core;
using Revenj.Utility;

namespace Revenj.Extensibility
{
	internal abstract class PluginsProvider : IExtensibilityProvider
	{
		private readonly IMixinProvider MixinProvider;
		private readonly IResolution Container;

		protected interface IResolution
		{
			Lazy<T, Dictionary<string, object>>[] Resolve<T>();
		}

		protected PluginsProvider(
			IMixinProvider mixinProvider,
			IResolution container)
		{
			Contract.Requires(mixinProvider != null);
			Contract.Requires(container != null);

			this.MixinProvider = mixinProvider;
			this.Container = container;
		}

		internal static List<Assembly> FindPlugins(PluginsConfiguration configuration)
		{
			var assemblies = new List<Assembly>();
			if (configuration.Directories.Count == 0 && configuration.Assemblies.Count == 0)
			{
				var rootPath = AppDomain.CurrentDomain.BaseDirectory ?? typeof(AutofacMefProvider).Assembly.Location;
				foreach (var f in Directory.GetFiles(rootPath, "*.Plugins.*.dll", SearchOption.TopDirectoryOnly))
				{
					try { assemblies.Add(Assembly.LoadFrom(f)); }
					catch (Exception aex)
					{
						System.Diagnostics.Debug.WriteLine(aex.ToString());
						throw new FrameworkException("Error loading plugin: " + f, aex);
					}
				}
			}
			foreach (var directory in configuration.Directories)
			{
				if (directory != null)
				{
					foreach (var f in Directory.GetFiles(directory, "*.dll", SearchOption.TopDirectoryOnly))
					{
						var name = Path.GetFileNameWithoutExtension(f);
						//TODO: temporary hack, will clean up later
						if (name != "Oracle.DataAccess" && name != "Revenj.DatabasePersistence.Oracle")
						{
							try { assemblies.Add(Assembly.LoadFrom(f)); }
							catch (Exception aex)
							{
								System.Diagnostics.Debug.WriteLine(aex.ToString());
								throw new FrameworkException("Error loading plugin: " + f, aex);
							}
						}
					}
				}
			}
			foreach (var asm in configuration.Assemblies)
				if (asm != null)
					assemblies.Add(asm);
			return assemblies;
		}

		public IEnumerable<Type> FindPlugins<TService>(Func<Type, Type, bool> filter)
		{
			var implementations = Container.Resolve<TService>();
			var dict = new Dictionary<Type, ConceptImplementation<TService>>();
			try
			{
				foreach (var impl in implementations)
				{
					object md;
					if (impl.Metadata.TryGetValue(Metadata.Implements, out md))
					{
						var multiple = md as Type[] ?? new[] { md as Type };
						foreach (var type in multiple)
						{
							var ci = new ConceptImplementation<TService>(impl, type);
							if (filter(ci.Type, type))
								dict[ci.Type] = ci;
						}
					}
					else
					{
						var ci = new ConceptImplementation<TService>(impl, null);
						if (filter(ci.Type, null))
							dict[ci.Type] = ci;
					}
				}
			}
			catch (DependencyResolutionException ex)
			{
				if (ex.InnerException != null)
					throw new FrameworkException("Error loading plugins for " + typeof(TService).FullName + ". " + ex.InnerException.Message, ex.InnerException);
				throw new FrameworkException("Error loading plugins for " + typeof(TService).FullName, ex);
			}

			return Sort(dict).Select(ci => ci.Type).ToList();
		}

		private static IEnumerable<ConceptImplementation<T>> Sort<T>(Dictionary<Type, ConceptImplementation<T>> dict)
		{
			var dependencies = new List<KeyValuePair<Type, Type>>();
			foreach (var impl in dict)
			{
				if (impl.Value.AfterType != null && dict.ContainsKey(impl.Value.AfterType))
					dependencies.Add(new KeyValuePair<Type, Type>(impl.Key, impl.Value.AfterType));
				if (impl.Value.BeforeType != null && dict.ContainsKey(impl.Value.BeforeType))
					dependencies.Add(new KeyValuePair<Type, Type>(impl.Value.BeforeType, impl.Key));
			}

			var sorted = Sorting.TopologicalSort(dict.Keys, dependencies).ToList();

			return sorted.Select(it => dict[it]);
		}

		public Dictionary<Type, List<Type>> FindImplementations<TInterface>(Func<Type, Type, bool> filter)
		{
			var implementations = Container.Resolve<TInterface>();

			var implDict = new Dictionary<Type, List<ConceptImplementation<TInterface>>>();

			foreach (var impl in implementations)
			{
				object md;
				if (impl.Metadata.TryGetValue(Metadata.Implements, out md))
				{
					var multiple = md as Type[] ?? new[] { md as Type };
					foreach (var type in multiple)
					{
						var ci = new ConceptImplementation<TInterface>(impl, type);
						List<ConceptImplementation<TInterface>> conceptImplList;
						if (!filter(ci.Type, ci.ImplementsType))
							continue;

						if (!implDict.TryGetValue(ci.ImplementsType, out conceptImplList))
							implDict.Add(ci.ImplementsType, conceptImplList = new List<ConceptImplementation<TInterface>>());
						conceptImplList.Add(ci);
					}
				}
			}

			var dict = new Dictionary<Type, List<Type>>();
			foreach (var type in implDict.Keys)
			{
				var list = implDict[type];
				var conceptImpls = list.Count > 1 ? Sort(list.ToDictionary(it => it.Type)).ToList() : list;
				dict.Add(type, new List<Type>(UseInsteadOfImplementations(conceptImpls).Select(ci => ci.Type)));
			}
			return dict;
		}


		private List<ConceptImplementation<TInterface>> UseInsteadOfImplementations<TInterface>(
			List<ConceptImplementation<TInterface>> list)
		{
			var insteadOfConcepts = list.Where(ci => ci.InsteadOfType != null);
			var newList = list.Where(it => !insteadOfConcepts.Contains(it)).ToList();
			foreach (var impl in newList.ToArray())
			{
				var found = insteadOfConcepts.Where(it => it.InsteadOfType == impl.Type).ToList();
				if (!found.Any())
					continue;
				var ind = newList.IndexOf(impl);
				newList.RemoveAt(ind);
				newList.InsertRange(ind, found);
			}
			return newList;
		}

		public Dictionary<Type, Func<Type, object[], TImplementation>> FindExtensions<TImplementation>()
		{
			var implementations = Container.Resolve<TImplementation>();

			var list = new List<ConceptImplementation<TImplementation>>();
			foreach (var impl in implementations)
				list.Add(new ConceptImplementation<TImplementation>(impl, null));

			var dict = new Dictionary<Type, Func<Type, object[], TImplementation>>(list.Count);
			foreach (var item in list)
			{
				if (item.ExtendsType != null)
					continue;
				var extensions = list.FindAll(it => it.ExtendsType != null && it.ExtendsType.IsAssignableFrom(item.Type));
				dict.Add(
					item.Type,
					(type, args) => (TImplementation)MixinProvider.Create(
						type,
						args,
						from ext in extensions
						//TODO consider factory.Resolve
						select Activator.CreateInstance(ext.Type)));
			}
			return dict;
		}

		private class ConceptImplementation<TImplementation>
		{
			public readonly Type Type;
			public readonly Type ImplementsType;
			public readonly Type AfterType;
			public readonly Type BeforeType;
			public readonly Type ExtendsType;
			public readonly Type InsteadOfType;

			public ConceptImplementation(Lazy<TImplementation, Dictionary<string, object>> impl, Type ImplementsType)
			{
				Contract.Requires(impl != null);
				this.ImplementsType = ImplementsType;

				object md;
				if (impl.Metadata.TryGetValue(Metadata.ClassType, out md))
					Type = (Type)md;
				else
				{
					try
					{
						Type = impl.Value.GetType();
					}
					catch (ComposablePartException ex)
					{
						throw new FrameworkException(@"Plugin {0} doesn't have an empty constructor.
It must be decorated with ClassType Metadata.".With(ex.Element.DisplayName), ex);
					}
				}

				if (impl.Metadata.TryGetValue(Metadata.After, out md))
					AfterType = (Type)md;

				if (impl.Metadata.TryGetValue(Metadata.Before, out md))
					BeforeType = (Type)md;

				if (impl.Metadata.TryGetValue(Metadata.Extends, out md))
					ExtendsType = (Type)md;

				if (impl.Metadata.TryGetValue(Metadata.InsteadOf, out md))
					InsteadOfType = (Type)md;
			}
		}
	}
}
