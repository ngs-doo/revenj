using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Configuration;
using NGS;
using NGS.Extensibility;

namespace Revenj.Plugins.Aspects.MethodCalls
{
	[Export(typeof(ISystemAspect))]
	public class Configuration : ISystemAspect
	{
		private static readonly List<Type> EnabledAspects = new List<Type>();
		private static readonly List<Type> DisabledAspects = new List<Type>();

		public static bool TraceAll { get; set; }

		public static void Enable(string typeName)
		{
			var type = Type.GetType(typeName);
			if (type == null)
				throw new ConfigurationErrorsException("Can't find type {0} for method calls tracing.".With(typeName));
			if (!EnabledAspects.Contains(type))
				EnabledAspects.Add(type);
		}

		public static void Disable(string typeName)
		{
			var type = Type.GetType(typeName);
			if (type == null)
				throw new ConfigurationErrorsException("Can't find type {0} for method calls tracing.".With(typeName));
			if (!DisabledAspects.Contains(type))
				DisabledAspects.Add(type);
		}

		public void Initialize(IObjectFactory factory)
		{
			if (!TraceAll && EnabledAspects.Count == 0)
				return;

			factory.RegisterTypes(new[] { typeof(LoggingInterceptor) });
			var logging = factory.Resolve<LoggingInterceptor>();
			var registrator = factory.Resolve<IInterceptorRegistrator>();
			if (TraceAll)
				registrator.Intercept(t => !DisabledAspects.Contains(t), logging);
			else
				EnabledAspects.ForEach(it => registrator.Intercept(it, logging));
		}
	}
}
