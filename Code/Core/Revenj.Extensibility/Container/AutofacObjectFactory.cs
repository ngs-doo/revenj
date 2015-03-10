using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics.Contracts;
using System.Linq;
using Revenj.Extensibility.Autofac;
using Revenj.Extensibility.Autofac.Core;

namespace Revenj.Extensibility
{
	public class AutofacObjectFactory : IObjectFactory
	{
		private ILifetimeScope CurrentScope;
		private Stack<ILifetimeScope> MyScopes = new Stack<ILifetimeScope>();

		private readonly List<IObjectFactoryBuilder> FactoryBuilders = new List<IObjectFactoryBuilder>();
		private readonly List<Action<ContainerBuilder>> AutofacBuilders = new List<Action<ContainerBuilder>>(2);
		private bool ShouldBuildScope;

		private readonly ConcurrentDictionary<Type, Func<object>> SimpleCache = new ConcurrentDictionary<Type, Func<object>>(1, 11);
		private readonly ConcurrentDictionary<Type, Func<object[], object>> CacheWithArguments = new ConcurrentDictionary<Type, Func<object[], object>>(1, 3);
		private readonly ConcurrentDictionary<Type, bool> RegistrationCache = new ConcurrentDictionary<Type, bool>(1, 11);
		private readonly AutofacObjectFactory ParentFactory;

		private readonly IAspectComposer Aspects;
		private readonly object sync = new object();

		private readonly Action Cleanup;
		private static readonly ConcurrentDictionary<string, AutofacObjectFactory> TaggedScopes = new ConcurrentDictionary<string, AutofacObjectFactory>();

		public AutofacObjectFactory(
			ILifetimeScope lifetimeScope,
			IAspectComposer aspects)
		{
			Contract.Requires(lifetimeScope != null);
			Contract.Requires(aspects != null);

			CurrentScope = lifetimeScope;
			this.Aspects = aspects;
		}

		private AutofacObjectFactory(
			AutofacObjectFactory parentFactory,
			IAspectComposer aspects,
			Action cleanup)
			: this(parentFactory.CurrentScope, aspects)
		{
			this.ParentFactory = parentFactory;
			this.Cleanup = cleanup;
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
			BuildScopeIfRequired();

			if (args == null)
			{
				Func<object> factory;
				if (!SimpleCache.TryGetValue(type, out factory))
				{
					var serv = new TypedService(type);
					IComponentRegistration reg;
					var inContainer = CurrentScope.ComponentRegistry.TryGetRegistration(serv, out reg);
					RegistrationCache.TryAdd(type, inContainer);
					if (inContainer)
						factory = CurrentScope.ResolveLookup(serv, reg, new Parameter[0]).Factory;
					else
						factory = () => BuildWithAspects(type)(null);
					SimpleCache.TryAdd(type, factory);
				}
				return factory();
			}
			else
			{
				Func<object[], object> factory;
				if (!CacheWithArguments.TryGetValue(type, out factory))
				{
					var serv = new TypedService(type);
					IComponentRegistration reg;
					var inContainer = CurrentScope.ComponentRegistry.TryGetRegistration(serv, out reg);
					RegistrationCache.TryAdd(type, inContainer);
					if (inContainer)
						factory = a => CurrentScope.ResolveLookup(serv, reg, a.Select(it => new TypedParameter(it.GetType(), it))).Factory();
					else
						factory = BuildWithAspects(type);
					CacheWithArguments.TryAdd(type, factory);
				}
				return factory(args);
			}
		}

		public bool IsRegistered(Type type)
		{
			BuildScopeIfRequired();
			var exists = ExistsInCache(type);
			if (exists != null)
				return exists.Value;
			var result = CurrentScope.IsRegistered(type);
			RegistrationCache.TryAdd(type, result);
			return result;
		}

		private bool? ExistsInCache(Type type)
		{
			bool result;
			if (RegistrationCache.TryGetValue(type, out result))
				return result;
			if (ParentFactory != null)
				return ParentFactory.ExistsInCache(type);
			return null;
		}

		public void Register(IObjectFactoryBuilder builder)
		{
			lock (sync)
			{
				FactoryBuilders.Add(builder);
				ShouldBuildScope = true;
			}
		}

		private void RegisterNew(ContainerBuilder cb)
		{
			foreach (var rb in FactoryBuilders)
			{
				foreach (var item in rb.Types.Where(i => i.IsGeneric == false))
				{
					RegistrationCache.TryAdd(item.AsType ?? item.Type, true);
					switch (item.Scope)
					{
						case InstanceScope.Transient:
							if (item.AsType == null)
								cb.RegisterType(item.Type);
							else
								cb.RegisterType(item.Type).As(item.AsType);
							break;
						case InstanceScope.Singleton:
							if (item.AsType == null)
								cb.RegisterType(item.Type).SingleInstance();
							else
								cb.RegisterType(item.Type).As(item.AsType).SingleInstance();
							break;
						default:
							if (item.AsType == null)
								cb.RegisterType(item.Type).InstancePerLifetimeScope();
							else
								cb.RegisterType(item.Type).As(item.AsType).InstancePerLifetimeScope();
							break;
					}
				}
				foreach (var item in rb.Types.Where(i => i.IsGeneric))
				{
					RegistrationCache.TryAdd(item.AsType ?? item.Type, true);
					switch (item.Scope)
					{
						case InstanceScope.Transient:
							if (item.AsType == null)
								cb.RegisterGeneric(item.Type);
							else
								cb.RegisterGeneric(item.Type).As(item.AsType);
							break;
						case InstanceScope.Singleton:
							if (item.AsType == null)
								cb.RegisterGeneric(item.Type).SingleInstance();
							else
								cb.RegisterGeneric(item.Type).As(item.AsType).SingleInstance();
							break;
						default:
							if (item.AsType == null)
								cb.RegisterGeneric(item.Type).InstancePerLifetimeScope();
							else
								cb.RegisterGeneric(item.Type).As(item.AsType).InstancePerLifetimeScope();
							break;
					}
				}
				foreach (var item in rb.Instances)
				{
					var type = item.AsType ?? item.Instance.GetType();
					RegistrationCache.TryAdd(type, true);
					cb.RegisterInstance(item.Instance).As(type);
				}
				foreach (var item in rb.Funcs)
				{
					if (item.AsType == null)
						throw new NotSupportedException("Result type must be defined. Declared Func result is not defined");
					RegistrationCache.TryAdd(item.AsType, true);
					switch (item.Scope)
					{
						case InstanceScope.Transient:
							cb.Register(c => item.Func(c.Resolve<IObjectFactory>())).As(item.AsType);
							break;
						case InstanceScope.Singleton:
							cb.Register(c => item.Func(c.Resolve<IObjectFactory>())).As(item.AsType).SingleInstance();
							break;
						default:
							cb.Register(c => item.Func(c.Resolve<IObjectFactory>())).As(item.AsType).InstancePerLifetimeScope();
							break;
					}
				}
			}
			foreach (var builder in AutofacBuilders)
				builder(cb);
		}

		private void BuildScopeIfRequired()
		{
			if (!ShouldBuildScope)
				return;
			lock (sync)
			{
				if (!ShouldBuildScope)
					return;
				RegistrationCache.Clear();
				CurrentScope = CurrentScope.BeginLifetimeScope(RegisterNew);

				FactoryBuilders.Clear();
				AutofacBuilders.Clear();
				SimpleCache.Clear();
				CacheWithArguments.Clear();
				ShouldBuildScope = false;

				MyScopes.Push(CurrentScope);
			}
		}

		public IObjectFactory CreateScope(string id)
		{
			BuildScopeIfRequired();
			var innerComposer = Aspects.CreateInnerComposer();
			AutofacObjectFactory tv;
			Action cleanup = string.IsNullOrEmpty(id) ? (Action)null : () => TaggedScopes.TryRemove(id, out tv);
			var factory = new AutofacObjectFactory(this, innerComposer, cleanup);
			factory.AutofacBuilders.Add(cb => cb.RegisterInstance(factory).As<IObjectFactory>().ExternallyOwned());
			factory.RegisterInterfaces(innerComposer);
			if (!string.IsNullOrEmpty(id))
				TaggedScopes.TryAdd(id, factory);
			return factory;
		}

		public IObjectFactory FindScope(string id)
		{
			if (string.IsNullOrEmpty(id))
				return null;
			AutofacObjectFactory factory;
			TaggedScopes.TryGetValue(id, out factory);
			return factory;
		}

		public void Dispose()
		{
			lock (sync)
				while (MyScopes.Count > 0)
					MyScopes.Pop().Dispose();
			Aspects.Dispose();
			if (Cleanup != null)
				Cleanup();
		}
	}
}