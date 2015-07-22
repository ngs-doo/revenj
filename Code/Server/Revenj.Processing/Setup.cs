using Revenj.Extensibility;

namespace Revenj.Processing
{
	public static class Setup
	{
		public static void ConfigureProcessing(this IObjectFactoryBuilder builder)
		{
			builder.RegisterType<ProcessingEngine, IProcessingEngine>(InstanceScope.Singleton);
			builder.RegisterType<ScopePool, IScopePool>(InstanceScope.Singleton);
		}
	}
}
