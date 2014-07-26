using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics.Contracts;
using System.Linq;
using System.Linq.Expressions;
using DryIoc;

namespace Revenj.Extensibility
{
	public class DryIocObjectFactory : IObjectFactory
	{
		private Container CurrentScope;
		private Stack<Container> MyScopes = new Stack<Container>();

		private readonly List<IObjectFactoryBuilder> FactoryBuilders = new List<IObjectFactoryBuilder>();
		private bool ShouldBuildScope;

		private readonly DryIocObjectFactory ParentFactory;

		private readonly IAspectComposer Aspects;
		private readonly object sync = new object();

		private readonly Action Cleanup;
		private static readonly ConcurrentDictionary<string, DryIocObjectFactory> TaggedScopes = new ConcurrentDictionary<string, DryIocObjectFactory>();

		public DryIocObjectFactory(
			Container lifetimeScope,
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

			if (args == null || args.Length == 0)
			{
				if (CurrentScope.IsRegistered(type))
					return CurrentScope.Resolve(type);
			}
			return BuildWithAspects(type)(args);
		}

		public bool IsRegistered(Type type)
		{
			BuildScopeIfRequired();
			return CurrentScope.IsRegistered(type);
		}

		public void Register(IObjectFactoryBuilder builder)
		{
			lock (sync)
			{
				FactoryBuilders.Add(builder);
				ShouldBuildScope = true;
			}
		}

		private void RegisterNew()
		{
			var cb = CurrentScope;
			foreach (var rb in FactoryBuilders)
			{
				foreach (var item in rb.Types.Where(i => i.IsGeneric == false))
				{
					switch (item.Scope)
					{
						case InstanceScope.Transient:
							if (item.AsType == null)
								cb.Register(item.Type);
							else
								cb.Register(item.AsType, item.Type);
							break;
						case InstanceScope.Singleton:
							if (item.AsType == null)
								cb.Register(item.Type, Reuse.Singleton);
							else
								cb.Register(item.AsType, item.Type, Reuse.Singleton);
							break;
						default:
							if (item.AsType == null)
								cb.Register(item.Type, Reuse.InCurrentScope);
							else
								cb.Register(item.AsType, item.Type, Reuse.InCurrentScope);
							break;
					}
				}
				foreach (var item in rb.Types.Where(i => i.IsGeneric))
				{
					switch (item.Scope)
					{
						case InstanceScope.Transient:
							if (item.AsType == null)
								cb.Register(item.Type);
							else
								cb.Register(item.AsType, item.Type);
							break;
						case InstanceScope.Singleton:
							if (item.AsType == null)
								cb.Register(item.Type, Reuse.Singleton);
							else
								cb.Register(item.AsType, item.Type, Reuse.Singleton);
							break;
						default:
							if (item.AsType == null)
								cb.Register(item.Type, Reuse.InCurrentScope);
							else
								cb.Register(item.AsType, item.Type, Reuse.InCurrentScope);
							break;
					}
				}
				foreach (var item in rb.Instances)
				{
					var type = item.AsType ?? item.Instance.GetType();
					var factory = new DelegateFactory(
						(_, registry) => Expression.Constant(item.Instance),
						Reuse.Transient,
						null);
					cb.Register(factory, type, null);
				}
				foreach (var item in rb.Funcs)
				{
					if (item.AsType == null)
						throw new NotSupportedException("Result type must be defined. Declared Func result is not defined");
					//TODO
					/*
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
					}*/
				}
			}
		}

		private void BuildScopeIfRequired()
		{
			if (!ShouldBuildScope)
				return;
			lock (sync)
			{
				if (!ShouldBuildScope)
					return;
				CurrentScope = CurrentScope.OpenScope();
				RegisterNew();

				FactoryBuilders.Clear();
				ShouldBuildScope = false;

				MyScopes.Push(CurrentScope);
			}
		}

		public IObjectFactory CreateScope(string id)
		{
			BuildScopeIfRequired();
			var innerComposer = Aspects.CreateInnerComposer();
			DryIocObjectFactory tv;
			Action cleanup = string.IsNullOrEmpty(id) ? (Action)null : () => TaggedScopes.TryRemove(id, out tv);
			var factory = new DryIocObjectFactory(this, innerComposer, cleanup);
			factory.RegisterInstance<IObjectFactory>(factory);
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
			lock (sync)
				while (MyScopes.Count > 0)
					MyScopes.Pop().Dispose();
			Aspects.Dispose();
			if (Cleanup != null)
				Cleanup();
		}
	}
}