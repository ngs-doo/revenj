using System.Security;
using System.Security.Principal;
using Revenj.Extensibility;

namespace Revenj.Security
{
	public static class Setup
	{
		public static void ConfigureSecurity(this IObjectFactoryBuilder builder, bool withAuth)
		{
			if (withAuth)
			{
				builder.RegisterType(typeof(RepositoryAuthentication), InstanceScope.Singleton, false,
					typeof(IAuthentication<SecureString>),
					typeof(IAuthentication<string>),
					typeof(IAuthentication<byte[]>));
				builder.RegisterType<RepositoryPrincipalFactory, IPrincipalFactory>();
			}
			builder.RegisterType<PermissionManager, IPermissionManager>(InstanceScope.Singleton);
			builder.RegisterFunc<IPrincipal>(_ => System.Threading.Thread.CurrentPrincipal, InstanceScope.Context);
		}
	}
}
