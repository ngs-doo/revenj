using System;
using System.Collections.Generic;
using System.Configuration;
using System.Diagnostics.Contracts;
using System.Linq;

namespace Revenj.Extensibility
{
	public class AspectConfiguration
	{
		private static readonly List<Type> EnabledAspects = new List<Type>();
		private static readonly List<Type> DisabledAspects = new List<Type>();

		private static bool processAllAspects = true;
		public static bool ProcessAll
		{
			get { return processAllAspects; }
			set { processAllAspects = value; }
		}

		private readonly IExtensibilityProvider ExtensibilityProvider;

		public AspectConfiguration(IExtensibilityProvider extensibilityProvider)
		{
			Contract.Requires(extensibilityProvider != null);

			this.ExtensibilityProvider = extensibilityProvider;
		}

		public static void Enable(string typeName)
		{
			var type = Type.GetType(typeName);
			if (type == null)
				throw new ConfigurationErrorsException("Can't find type {0} for aspect configuration.".With(typeName));
			if (!EnabledAspects.Contains(type))
				EnabledAspects.Add(type);
		}

		public static void Disable(string typeName)
		{
			var type = Type.GetType(typeName);
			if (type == null)
				throw new ConfigurationErrorsException("Can't find type {0} for aspect configuration.".With(typeName));
			if (!DisabledAspects.Contains(type))
				DisabledAspects.Add(type);
		}

		private bool Filter(Type type)
		{
			return ProcessAll && !DisabledAspects.Contains(type)
				|| !ProcessAll && EnabledAspects.Contains(type);
		}

		public void Configure()
		{
			DslAspects = ExtensibilityProvider.FindPlugins<IDslAspect>((t, _) => Filter(t)).ToList();
			SystemAspects = ExtensibilityProvider.FindPlugins<ISystemAspect>((t, _) => Filter(t)).ToList();
		}

		public IEnumerable<Type> DslAspects { get; private set; }
		public IEnumerable<Type> SystemAspects { get; private set; }
	}
}
