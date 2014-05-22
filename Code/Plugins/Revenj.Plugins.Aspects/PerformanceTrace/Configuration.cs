using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Configuration;
using System.Runtime.Serialization;
using NGS;
using NGS.Extensibility;
using NGS.Logging;

namespace Revenj.Plugins.Aspects.PerformanceTrace
{
	[Export(typeof(ISystemAspect))]
	public class Configuration : ISystemAspect
	{
		private static readonly List<Type> EnabledAspects = new List<Type>();
		private static readonly List<Type> DisabledAspects = new List<Type>();

		public static bool TraceAll { get; set; }

		static Configuration()
		{
			TraceAll = ConfigurationManager.AppSettings["Performance.TraceAll"] == "true";
		}

		public static void Enable(string typeName)
		{
			var type = Type.GetType(typeName);
			if (type == null)
				throw new ConfigurationErrorsException("Can't find type {0} for logging trace.".With(typeName));
			if (!EnabledAspects.Contains(type))
				EnabledAspects.Add(type);
		}

		public static void Disable(string typeName)
		{
			var type = Type.GetType(typeName);
			if (type == null)
				throw new ConfigurationErrorsException("Can't find type {0} for logging trace.".With(typeName));
			if (!DisabledAspects.Contains(type))
				DisabledAspects.Add(type);
		}

		public void Initialize(IObjectFactory factory)
		{
			if (!TraceAll && EnabledAspects.Count == 0)
				return;
			DisabledAspects.Add(typeof(ILogger));
			DisabledAspects.Add(typeof(ICloneable));
			DisabledAspects.Add(typeof(ISerializable));

			factory.RegisterTypes(new[] { typeof(PerformanceInterceptor) });
			var performance = factory.Resolve<PerformanceInterceptor>();
			var registrator = factory.Resolve<IInterceptorRegistrator>();
			if (TraceAll)
				registrator.Intercept(t => !DisabledAspects.Contains(t), performance);
			else
				EnabledAspects.ForEach(it => registrator.Intercept(it, performance));
		}
	}
}
