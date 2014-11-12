using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.IO;
using System.Linq;
using System.Reflection;
using DryIoc;
using DryIoc.MefAttributedModel;
using Revenj.Common;

namespace Revenj.Extensibility
{
	public class DryIocMefProvider : PluginsProvider
	{
		public DryIocMefProvider(
			PluginsConfiguration configuration,
			IMixinProvider mixinProvider,
			Container container)
			: base(mixinProvider, new DryIocResolution(container, configuration)) { }

		class DryIocResolution : IResolution
		{
			private readonly Container Container;

			public DryIocResolution(Container container, PluginsConfiguration configuration)
			{
				this.Container = container.OpenScope();
				var assemblies = new List<Assembly>();
				try
				{
					foreach (var directory in configuration.Directories)
					{
						foreach (var f in Directory.GetFiles(directory, "*Plugin*.dll", SearchOption.AllDirectories))
						{
							assemblies.Add(Assembly.LoadFrom(f));
						}
					}
				}
				catch (System.Reflection.ReflectionTypeLoadException ex)
				{
					var firstFive = string.Join(Environment.NewLine, ex.LoaderExceptions.Take(5).Select(it => it.Message));
					System.Diagnostics.Debug.WriteLine(ex.ToString());
					System.Diagnostics.Debug.WriteLine(firstFive);
					throw new FrameworkException("Error loading plugins. Can't load plugins. {0}".With(firstFive), ex);
				}
				catch (Exception ex)
				{
					System.Diagnostics.Debug.WriteLine(ex.ToString());
					throw new FrameworkException("Error loading plugins.", ex);
				}
				AttributedModel.DefaultSetup(this.Container);
				this.Container.RegisterExports(assemblies.ToArray());
			}

			public Lazy<T, Dictionary<string, object>>[] Resolve<T>()
			{
				var meta = Container.Resolve<Meta<Lazy<T>, ExportMetadataAttribute[]>[]>();
				return (from m in meta
						let md = m.Metadata.ToDictionary(it => it.Name, it => it.Value)
						select new Lazy<T, Dictionary<string, object>>(() => default(T), md)).ToArray();
			}
		}
	}
}
