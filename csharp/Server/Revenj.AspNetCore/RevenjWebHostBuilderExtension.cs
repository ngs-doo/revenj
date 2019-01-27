using System;
using System.Collections.Generic;
using System.Configuration;
using Revenj.Extensibility;
using System.IO;
using System.Reflection;
using Revenj.AspNetCore;

namespace Microsoft.AspNetCore.Hosting
{
	public interface IRevenjConfig
	{
		IRevenjConfig WithAOP();
		IRevenjConfig ImportPlugins(string path);
		IRevenjConfig ImportPlugins(Assembly assembly);
		IRevenjConfig UsingContainer(Setup.IContainerBuilder container);
		IRevenjConfig With(ISystemAspect aspect);
		IWebHostBuilder Configure(string connectionString);
	}

	public static class RevenjWebHostBuilderExtension
	{
		public static IRevenjConfig UseRevenj(this IWebHostBuilder builder)
		{
			return new RevenjConfig(builder);
		}

		public static IWebHostBuilder UseRevenjPostgres(
			this IWebHostBuilder builder, 
			string connectionString,
			bool withAspects = false,
			bool externalConfiguration = false,
			Setup.IContainerBuilder customContainer = null,
			IEnumerable<ISystemAspect> customAspects = null)
		{
			var config = new RevenjConfig(builder);
			if (withAspects) config.WithAOP();
			if (externalConfiguration)
			{
				foreach (var key in ConfigurationManager.AppSettings.AllKeys)
				{
					if (!key.StartsWith("PluginsPath", StringComparison.OrdinalIgnoreCase)) continue;
					var path = ConfigurationManager.AppSettings[key];
					var pathRelative = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, path);
					var chosenPath = Directory.Exists(pathRelative) ? pathRelative : path;
					config.ImportPlugins(chosenPath);
				}
			}
			if (customContainer != null) config.UsingContainer(customContainer);
			if (customAspects != null)
			{
				foreach (var ca in customAspects)
					config.With(ca);
			}
			return config.Configure(connectionString);
		}
	}
}
