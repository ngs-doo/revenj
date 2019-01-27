using Revenj.Extensibility;
using Revenj.Plugins.AspNetCore.Commands;

namespace Microsoft.AspNetCore.Hosting
{
	public static class RevenjWebHostCommandExtension
	{
		private class CommandSetup : ISystemAspect
		{
			public void Initialize(IObjectFactory factory)
			{
				factory.RegisterType(typeof(RestApplication), InstanceScope.Singleton);
				factory.RegisterType(typeof(CommandConverter), InstanceScope.Singleton);
			}
		}

		public static IRevenjConfig WithCommands(this IRevenjConfig builder)
		{
			return builder
				.With(new CommandSetup())
				.ImportPlugins(typeof(RevenjWebHostCommandExtension).Assembly);
		}
	}
}
