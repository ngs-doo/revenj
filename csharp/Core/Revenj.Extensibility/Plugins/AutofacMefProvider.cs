using System;
using System.Collections.Generic;
using System.ComponentModel.Composition.Hosting;
using System.IO;
using System.Linq;
using System.Reflection;
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
						var assemblies = new List<Assembly>();
						if (configuration.Directories != null)
						{
							foreach (var directory in configuration.Directories)
								if (directory != null)
								{
									var files = Directory.GetFiles(directory, "*.dll", SearchOption.TopDirectoryOnly);
									foreach (var f in files)
									{
										var name = Path.GetFileNameWithoutExtension(f);
										if (name != "Oracle.DataAccess")
										{
											try
											{
												assemblies.Add(Assembly.LoadFrom(f));
											}
											catch (Exception aex)
											{
												System.Diagnostics.Debug.WriteLine(aex.ToString());
												throw new FrameworkException("Error loading plugin: " + f, aex);
											}
										}
									}
								}
						}
						if (configuration.Assemblies != null)
							assemblies.AddRange(configuration.Assemblies);
						foreach (var asm in assemblies)
							if (asm != null && !asm.FullName.StartsWith("Oracle.DataAccess"))
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
