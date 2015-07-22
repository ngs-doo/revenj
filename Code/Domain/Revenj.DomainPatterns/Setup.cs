using System;
using System.Collections.Generic;
using System.Reflection;
using Revenj.Extensibility;

namespace Revenj.DomainPatterns
{
	public static class Setup
	{
		public static void ConfigurePatterns(this IObjectFactoryBuilder builder, Func<IObjectFactory, IEnumerable<Assembly>> getDomainModels)
		{
			builder.RegisterFunc<IDomainModel>(c => new DomainModel(getDomainModels(c), c), InstanceScope.Singleton);
			builder.RegisterType<DomainTypeResolver, ITypeResolver>(InstanceScope.Singleton);
			builder.RegisterType(typeof(WeakCache<>), InstanceScope.Context, true, typeof(WeakCache<>), typeof(IDataCache<>));
			builder.RegisterType<DomainEventSource, IDomainEventSource>(InstanceScope.Context);
			builder.RegisterType<DomainEventStore, IDomainEventStore>(InstanceScope.Context);
			builder.RegisterType<GlobalEventStore>(InstanceScope.Context);
			builder.RegisterType(typeof(SingleDomainEventSource<>), InstanceScope.Context, true, typeof(IDomainEventSource<>));
			builder.RegisterType(typeof(RegisterChangeNotifications<>), InstanceScope.Singleton, true, typeof(IObservable<>));
			builder.RegisterType<DataContext, IDataContext>(InstanceScope.Context);
		}
	}
}
