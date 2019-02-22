using Revenj.Plugins.AspNetCore.Commands;
using Revenj.DomainPatterns;
using Revenj.Extensibility;
using Revenj.AspNetCore;

namespace Microsoft.AspNetCore.Hosting
{
	public static class RevenjBuilderExtension
	{
		public static IRevenjConfig WithCommands(this IRevenjConfig builder)
		{
			return builder
				.ImportPlugins(typeof(RevenjBuilderExtension).Assembly);
		}
	}
}

namespace Microsoft.AspNetCore.Builder
{
	public static class RevenjBuilderExtension
	{
		public static IApplicationBuilder UseRevenjMiddleware(this IApplicationBuilder builder)
		{
			var objectFactory = builder.ApplicationServices.Resolve<IObjectFactory>();
			objectFactory.RegisterType(typeof(RestMiddleware), InstanceScope.Singleton);
			objectFactory.RegisterType(typeof(CrudMiddleware), InstanceScope.Singleton);
			objectFactory.RegisterType(typeof(DomainMiddleware), InstanceScope.Singleton);
			objectFactory.RegisterType(typeof(CommandsMiddleware), InstanceScope.Singleton);
			objectFactory.RegisterType(typeof(ReportingMiddleware), InstanceScope.Singleton);
			var rest = objectFactory.Resolve<RestMiddleware>();
			builder.MapWhen(
				context => context.Request.Path.StartsWithSegments("/RestApplication.svc"),
				app => app.Run(async context => await rest.Handle(context, 20)));
			var crud = objectFactory.Resolve<CrudMiddleware>();
			builder.MapWhen(
				context => context.Request.Path.StartsWithSegments("/Crud.svc"),
				app => app.Run(async context => await crud.Handle(context, 9)));
			var domain = objectFactory.Resolve<DomainMiddleware>();
			builder.MapWhen(
				context => context.Request.Path.StartsWithSegments("/Domain.svc"),
				app => app.Run(async context => await domain.Handle(context, 11)));
			var commands = objectFactory.Resolve<CommandsMiddleware>();
			builder.MapWhen(
				context => context.Request.Path.StartsWithSegments("/Commands.svc"),
				app => app.Run(async context => await commands.Handle(context, 13)));
			var reporting = objectFactory.Resolve<ReportingMiddleware>();
			builder.MapWhen(
				context => context.Request.Path.StartsWithSegments("/Reporting.svc"),
				app => app.Run(async context => await reporting.Handle(context, 14)));
			return builder;
		}
	}
}
