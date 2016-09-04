using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics.Contracts;
using System.Linq;
using DryIoc;

namespace Revenj.Extensibility
{
	internal class DryIocObjectFactory : IObjectFactory
	{
		private IContainer CurrentScope;
		private readonly List<IObjectFactoryBuilder> FactoryBuilders = new List<IObjectFactoryBuilder>();
		private bool ShouldUpdateScope;

		private readonly DryIocObjectFactory ParentFactory;
		private readonly ConcurrentDictionary<Type, Func<object>> ServiceCache = new ConcurrentDictionary<Type, Func<object>>(1, 11);

		private readonly IAspectComposer Aspects;
		private readonly object sync = new object();

		private readonly Action Cleanup;
		private static readonly ConcurrentDictionary<string, DryIocObjectFactory> TaggedScopes = new ConcurrentDictionary<string, DryIocObjectFactory>();

		public DryIocObjectFactory(
			IContainer lifetimeScope,
			IAspectComposer aspects)
		{
			Contract.Requires(lifetimeScope != null);
			Contract.Requires(aspects != null);

			CurrentScope = lifetimeScope;
			this.Aspects = aspects;
		}

		private DryIocObjectFactory(
			DryIocObjectFactory parentFactory,
			IAspectComposer aspects,
			Action cleanup)
			: this(parentFactory.CurrentScope.OpenScopeWithoutContext(), aspects)
		{
			this.ParentFactory = parentFactory;
			this.Cleanup = cleanup;
		}

		private Func<object> BuildFactoryForService(Type type)
		{
			if (type.IsClass)
			{
				foreach (var ctor in type.GetConstructors().Where(it => it.IsPublic))
				{
					var ctorParams = ctor.GetParameters();
					if (ctorParams.Length == 0)
						return () => Activator.CreateInstance(type);
					if (ctorParams.Length == 1)
					{
						if (ctorParams[0].ParameterType == typeof(IServiceProvider)
							|| ctorParams[0].ParameterType == typeof(IObjectFactory))
							return () => Activator.CreateInstance(type, this);
					}
					var argFactories = new Func<object>[ctorParams.Length];
					for (int i = 0; i < ctorParams.Length; i++)
					{
						var arg = GetFactory(ctorParams[i].ParameterType);
						if (arg == null)
							return null;
						argFactories[i] = arg;
					}
					return () =>
					{
						var args = new object[argFactories.Length];
						for (int i = 0; i < argFactories.Length; i++)
							args[i] = argFactories[i]();
						return Activator.CreateInstance(type, args);
					};
				}
			}
			return null;
		}

		public object GetService(Type type)
		{
			var factory = GetFactory(type);
			return factory != null ? factory() : null;
		}

		private Func<object> GetFactory(Type type)
		{
			UpdateScopeIfRequired();

			Func<object> factory;
			if (!ServiceCache.TryGetValue(type, out factory))
			{
				if (CurrentScope.IsRegistered(type))
					factory = () => CurrentScope.Resolve(type, IfUnresolved.ReturnDefault);
				else
					factory = BuildFactoryForService(type);
				ServiceCache.TryAdd(type, factory);
			}
			return factory;
		}

		private Func<object[], object> BuildWithAspects(Type type)
		{
			var services = new[] { type }.Union(type.GetInterfaces()).ToArray();
			return args =>
			{
				try
				{
					return Aspects.Create(type, args, services);
				}
				catch (MissingMethodException mme)
				{
					throw new MissingMethodException(@"Can't create instance of an type {0}. 
Check if type should be registered in the container or if correct arguments are passed.".With(type.FullName), mme);
				}
			};
		}

		public object Resolve(Type type, object[] args)
		{
			UpdateScopeIfRequired();

			if (args == null || args.Length == 0)
			{
				if (CurrentScope.IsRegistered(type))
					return CurrentScope.Resolve(type);
			}
			return BuildWithAspects(type)(args);
		}

		public bool IsRegistered(Type type)
		{
			UpdateScopeIfRequired();
			return CurrentScope.IsRegistered(type);
		}

		public void Register(IObjectFactoryBuilder builder)
		{
			lock (sync)
			{
				FactoryBuilders.Add(builder);
				ShouldUpdateScope = true;
			}
		}

		private void RegisterNew()
		{
			var cb = CurrentScope;
			foreach (var rb in FactoryBuilders)
				RegisterToContainer(cb, rb);
		}

		internal static void RegisterToContainer(IContainer cb, IObjectFactoryBuilder rb)
		{
			foreach (var item in rb.Types)
			{
				switch (item.Scope)
				{
					case InstanceScope.Transient:
						if (item.AsType == null || item.AsType.Length == 0)
							cb.Register(item.Type, Reuse.Transient);
						else foreach (var t in item.AsType)
								cb.Register(t, item.Type);
						break;
					case InstanceScope.Singleton:
						if (item.AsType == null || item.AsType.Length == 0)
							cb.Register(item.Type, Reuse.Singleton);
						else foreach (var t in item.AsType)
								cb.Register(t, item.Type, Reuse.Singleton);
						break;
					default:
						if (item.AsType == null || item.AsType.Length == 0)
							cb.Register(item.Type, Reuse.InResolutionScope);
						else foreach (var t in item.AsType)
								cb.Register(t, item.Type, Reuse.InResolutionScope);
						break;
				}
			}
			foreach (var item in rb.Instances)
			{
				var type = item.AsType ?? item.Instance.GetType();
				cb.RegisterInstance(type, item.Instance);
			}
			foreach (var it in rb.Funcs)
			{
				var item = it;
				if (item.AsType == null || item.AsType.Length == 0)
					throw new NotSupportedException("Result type must be defined. Declared Func result is not defined");
				switch (item.Scope)
				{
					case InstanceScope.Transient:
						foreach (var t in item.AsType)
							cb.RegisterDelegate(t, c => item.Func(c.Resolve<IObjectFactory>()), Reuse.Transient);
						break;
					case InstanceScope.Singleton:
						foreach (var t in item.AsType)
							cb.RegisterDelegate(t, c => item.Func(c.Resolve<IObjectFactory>()), Reuse.Singleton);
						break;
					default:
						foreach (var t in item.AsType)
							cb.RegisterDelegate(t, c => item.Func(c.Resolve<IObjectFactory>()), Reuse.InResolutionScope);
						break;
				}
			}
		}

		private void UpdateScopeIfRequired()
		{
			if (!ShouldUpdateScope)
				return;
			lock (sync)
			{
				if (!ShouldUpdateScope)
					return;
				RegisterNew();

				FactoryBuilders.Clear();
				ServiceCache.Clear();
				ShouldUpdateScope = false;
			}
		}

		public IObjectFactory CreateScope(string id)
		{
			UpdateScopeIfRequired();
			var innerComposer = Aspects.CreateInnerComposer();
			DryIocObjectFactory tv;
			Action cleanup = string.IsNullOrEmpty(id) ? (Action)null : () => TaggedScopes.TryRemove(id, out tv);
			var factory = new DryIocObjectFactory(this, innerComposer, cleanup);
			factory.RegisterInstance<IObjectFactory>(factory);
			factory.RegisterInstance<IServiceProvider>(factory);
			factory.RegisterInterfaces(innerComposer);
			if (!string.IsNullOrEmpty(id))
				TaggedScopes.TryAdd(id, factory);
			return factory;
		}

		public IObjectFactory FindScope(string id)
		{
			if (string.IsNullOrEmpty(id))
				return null;
			DryIocObjectFactory factory;
			TaggedScopes.TryGetValue(id, out factory);
			return factory;
		}

		public void Dispose()
		{
			CurrentScope.Dispose();
			Aspects.Dispose();
			if (Cleanup != null)
				Cleanup();
		}
	}
}