using System;
using System.Collections.Generic;
using System.ComponentModel.Composition.Hosting;
using System.IO;
using System.Linq;
using System.Text;
using Revenj.Common;
using Revenj.Extensibility.Autofac;
using Revenj.Extensibility.Autofac.Integration.Mef;

namespace Revenj.Extensibility
{
	internal class AutofacMefProvider : PluginsProvider
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
						var assemblies = FindPlugins(configuration);
						foreach (var asm in assemblies)
							builder.RegisterComposablePartCatalog(new AssemblyCatalog(asm));
					}
					catch (System.Reflection.ReflectionTypeLoadException ex)
					{
						var sb = new StringBuilder();
						foreach (var le in ex.LoaderExceptions.Take(5))
						{
							sb.AppendLine(le.Message);
							var fle = le as FileLoadException;
							if (fle != null && fle.FusionLog != null)
								sb.AppendLine(fle.FusionLog);
						}
						System.Diagnostics.Debug.WriteLine(ex.ToString());
						var firstFive = sb.ToString();
						System.Diagnostics.Debug.WriteLine(firstFive);
						throw new FrameworkException("Error loading plugins. Can't load plugins. " + firstFive, ex);
					}
					catch (FrameworkException) { throw; }
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
