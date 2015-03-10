using System;
using System.Collections.Generic;
using System.ComponentModel.Composition.Hosting;
using System.Linq;
using Revenj.Extensibility.Autofac;
using Revenj.Extensibility.Autofac.Core;
using Revenj.Extensibility.Autofac.Integration.Mef;
using Revenj.Common;

namespace Revenj.Extensibility
{
	public class AutofacMefProvider : PluginsProvider
	{
		public AutofacMefProvider(
			PluginsConfiguration configuration,
			IMixinProvider mixinProvider,
			ILifetimeScope container)
			: base(mixinProvider, new AutofacResolution(container, configuration)) { }

		class AutofacResolution : IResolution
		{
			private readonly ILifetimeScope Container;

			public AutofacResolution(ILifetimeScope container, PluginsConfiguration configuration)
			{
				this.Container = container.BeginLifetimeScope(builder =>
				{
					try
					{
						if (configuration.Directories != null)
							foreach (var directory in configuration.Directories)
								if (directory != null)
									builder.RegisterComposablePartCatalog(new DirectoryCatalog(directory));
						if (configuration.Assemblies != null)
							foreach (var asm in configuration.Assemblies)
								if (asm != null)
									builder.RegisterComposablePartCatalog(new AssemblyCatalog(asm));
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
				});
			}

			public Lazy<T, Dictionary<string, object>>[] Resolve<T>()
			{
				return Container.Resolve<Lazy<T, Dictionary<string, object>>[]>();
			}
		}
	}
}
