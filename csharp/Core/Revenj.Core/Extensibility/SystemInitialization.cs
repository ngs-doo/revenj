using System.Diagnostics.Contracts;

namespace Revenj.Extensibility
{
	internal class SystemInitialization
	{
		private readonly IAspectRegistrator AspectRegistrator;
		private readonly IObjectFactory ObjectFactory;

		public SystemInitialization(
			IAspectRegistrator aspectRegistrator,
			IObjectFactory objectFactory)
		{
			Contract.Requires(aspectRegistrator != null);
			Contract.Requires(objectFactory != null);

			this.AspectRegistrator = aspectRegistrator;
			this.ObjectFactory = objectFactory;
		}

		public void Initialize(bool dslAspects)
		{
			using (var inner = ObjectFactory.CreateInnerFactory())
			{
				inner.RegisterType(typeof(AspectConfiguration));
				var aspectConfiguration = inner.Resolve<AspectConfiguration>();
				aspectConfiguration.Configure();

				if (dslAspects)
				{
					inner.RegisterTypes(aspectConfiguration.DslAspects);
					foreach (var type in aspectConfiguration.DslAspects)
					{
						var asp = inner.Resolve<IDslAspect>(type);
						asp.Register(AspectRegistrator);
					}
				}
				inner.RegisterTypes(aspectConfiguration.SystemAspects);
				foreach (var type in aspectConfiguration.SystemAspects)
				{
					var asp = inner.Resolve<ISystemAspect>(type);
					asp.Initialize(ObjectFactory);
				}
			}
		}
	}
}
