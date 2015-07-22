using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics.Contracts;
using System.Linq;
using System.Reflection;
using Castle.DynamicProxy;
using Revenj.Extensibility.Autofac.Core;
using Revenj.Utility;

namespace Revenj.Extensibility
{
	internal class AspectRepository : IAspectRegistrator, IAspectComposer, IInterceptorRegistrator, IDisposable
	{
		private HashSet<Type> RegisteredCreationTypes;
		private HashSet<Type> AllCreationTypes;
		private bool HasCreationTypes;

		private HashSet<Type> RegisteredMethodTypes;
		private HashSet<Type> AllMethodTypes;
		private bool HasMethodTypes;

		private List<KeyValuePair<Func<Type, bool>, IInterceptor>> RegisteredRules;
		private List<KeyValuePair<Func<Type, bool>, IInterceptor>> AllRulesTypes;
		private bool HasRulesTypes;

		private Dictionary<Type, List<Action>> BeforeCreationAspectDictionary;
		private Dictionary<Type, List<Action<object>>> AfterCreationAspectDictionary;

		private Dictionary<KeyValuePair<Type, MethodInfo>, List<Action<object, object[]>>> BeforeMethodAspectDictionary;
		private Dictionary<KeyValuePair<Type, MethodInfo>, List<Func<object, object[], Func<object[], object>, object>>> AroundMethodAspectDictionary;
		private Dictionary<KeyValuePair<Type, MethodInfo>, List<Func<object, object[], object, object>>> AfterMethodAspectDictionary;

		private Dictionary<Type, List<IInterceptor>> InterceptorsDictionary;
		private ConcurrentDictionary<IComponentRegistration, Type[]> ComponentServices;

		private readonly IDynamicProxyProvider DynamicProxyProvider;
		private readonly AspectRepository ParentRepository;

		private event EventHandler RegistrationChanged = (s, ea) => { };
		private bool RequireInitialization;

		public AspectRepository(IDynamicProxyProvider dynamicProxyProvider)
		{
			Contract.Requires(dynamicProxyProvider != null);

			this.DynamicProxyProvider = dynamicProxyProvider;
			RegisteredCreationTypes = new HashSet<Type>();
			AllCreationTypes = new HashSet<Type>();
			RegisteredMethodTypes = new HashSet<Type>();
			AllMethodTypes = new HashSet<Type>();
			RegisteredRules = new List<KeyValuePair<Func<Type, bool>, IInterceptor>>();
			AllRulesTypes = new List<KeyValuePair<Func<Type, bool>, IInterceptor>>();
			BeforeCreationAspectDictionary = new Dictionary<Type, List<Action>>();
			AfterCreationAspectDictionary = new Dictionary<Type, List<Action<object>>>();
			BeforeMethodAspectDictionary = new Dictionary<KeyValuePair<Type, MethodInfo>, List<Action<object, object[]>>>();
			AroundMethodAspectDictionary = new Dictionary<KeyValuePair<Type, MethodInfo>, List<Func<object, object[], Func<object[], object>, object>>>();
			AfterMethodAspectDictionary = new Dictionary<KeyValuePair<Type, MethodInfo>, List<Func<object, object[], object, object>>>();
			InterceptorsDictionary = new Dictionary<Type, List<IInterceptor>>();
			ComponentServices = new ConcurrentDictionary<IComponentRegistration, Type[]>(1, 127);
		}

		private AspectRepository(AspectRepository parentRepository)
		{
			Contract.Requires(parentRepository != null);

			this.ParentRepository = parentRepository;
			this.DynamicProxyProvider = parentRepository.DynamicProxyProvider;
			RequireInitialization = true;
		}

		private void Initialize()
		{
			lock (this)
			{
				if (!RequireInitialization)
					return;
				RegisteredCreationTypes = new HashSet<Type>();
				AllCreationTypes = new HashSet<Type>();
				RegisteredMethodTypes = new HashSet<Type>();
				AllMethodTypes = new HashSet<Type>();
				RegisteredRules = new List<KeyValuePair<Func<Type, bool>, IInterceptor>>();
				AllRulesTypes = new List<KeyValuePair<Func<Type, bool>, IInterceptor>>();
				BeforeCreationAspectDictionary = new Dictionary<Type, List<Action>>();
				AfterCreationAspectDictionary = new Dictionary<Type, List<Action<object>>>();
				BeforeMethodAspectDictionary = new Dictionary<KeyValuePair<Type, MethodInfo>, List<Action<object, object[]>>>();
				AroundMethodAspectDictionary = new Dictionary<KeyValuePair<Type, MethodInfo>, List<Func<object, object[], Func<object[], object>, object>>>();
				AfterMethodAspectDictionary = new Dictionary<KeyValuePair<Type, MethodInfo>, List<Func<object, object[], object, object>>>();
				InterceptorsDictionary = new Dictionary<Type, List<IInterceptor>>();
				ComponentServices = new ConcurrentDictionary<IComponentRegistration, Type[]>(1, 127);
				ParentRepository.RegistrationChanged += RefreshCache;
				RefreshCache(null, null);
				RequireInitialization = false;
			}
		}

		private void RefreshCache(object sender, EventArgs ea)
		{
			RecalculateCreationTypes();
			RecalculateMethodTypes();
			RecalculateRulesTypes();
		}

		private void RecalculateMethodTypes()
		{
			var list = new List<Type>();
			GetAllMethodTypes(list);
			AllMethodTypes = new HashSet<Type>(list);
			HasMethodTypes = AllMethodTypes.Count > 0;
		}

		private void RecalculateCreationTypes()
		{
			var list = new List<Type>();
			FillAllCreationTypes(list);
			AllCreationTypes = new HashSet<Type>(list);
			HasCreationTypes = AllCreationTypes.Count > 0;
		}

		private void RecalculateRulesTypes()
		{
			var list = new List<KeyValuePair<Func<Type, bool>, IInterceptor>>();
			GetAllRules(list);
			AllRulesTypes = list;
			HasRulesTypes = AllRulesTypes.Count > 0;
		}

		private void FillAllCreationTypes(List<Type> list)
		{
			lock (RegisteredCreationTypes)
				list.AddRange(RegisteredCreationTypes);
			if (ParentRepository != null)
				ParentRepository.FillAllCreationTypes(list);
		}

		private void GetAllMethodTypes(List<Type> list)
		{
			lock (RegisteredMethodTypes)
				list.AddRange(RegisteredMethodTypes);
			if (ParentRepository != null)
				ParentRepository.GetAllMethodTypes(list);
		}

		private void GetAllRules(List<KeyValuePair<Func<Type, bool>, IInterceptor>> list)
		{
			lock (RegisteredRules)
				list.AddRange(RegisteredRules);
			if (ParentRepository != null)
				ParentRepository.GetAllRules(list);
		}

		private void AddToDictionary<Taction>(
			KeyValuePair<Type, MethodInfo> kv,
			Dictionary<KeyValuePair<Type, MethodInfo>, List<Taction>> dict,
			Taction action)
		{
			if (!RegisteredMethodTypes.Contains(kv.Key))
				RegisteredMethodTypes.Add(kv.Key);

			List<Taction> list;
			lock (dict)
			{
				if (!dict.TryGetValue(kv, out list))
				{
					list = new List<Taction>();
					dict.Add(kv, list);
				}
				list.Add(action);
			}

			RecalculateMethodTypes();
			RegistrationChanged(this, EventArgs.Empty);
		}

		private void AddToDictionary<TAction>(
			Type type,
			Dictionary<Type, List<TAction>> dict,
			TAction action)
		{
			lock (RegisteredCreationTypes)
				RegisteredCreationTypes.Add(type);

			List<TAction> list;
			lock (dict)
			{
				if (!dict.TryGetValue(type, out list))
				{
					list = new List<TAction>();
					dict.Add(type, list);
				}
				list.Add(action);
			}

			RecalculateCreationTypes();
			RegistrationChanged(this, EventArgs.Empty);
		}

		public void Intercept(Type type, IInterceptor interceptor)
		{
			if (RequireInitialization)
				Initialize();
			lock (RegisteredMethodTypes)
				RegisteredMethodTypes.Add(type);

			List<IInterceptor> list;
			lock (InterceptorsDictionary)
			{
				if (!InterceptorsDictionary.TryGetValue(type, out list))
				{
					list = new List<IInterceptor>();
					InterceptorsDictionary.Add(type, list);
				}
				list.Add(interceptor);
			}

			RecalculateMethodTypes();
			RegistrationChanged(this, EventArgs.Empty);
		}

		public void Intercept(Func<Type, bool> rule, IInterceptor interceptor)
		{
			if (RequireInitialization)
				Initialize();
			lock (RegisteredRules)
				RegisteredRules.Add(new KeyValuePair<Func<Type, bool>, IInterceptor>(rule, interceptor));

			RecalculateRulesTypes();
			RegistrationChanged(this, EventArgs.Empty);
		}

		public void Before(Type type, Action before)
		{
			if (RequireInitialization)
				Initialize();
			AddToDictionary(type, BeforeCreationAspectDictionary, before);
		}

		public void After(Type type, Action<object> after)
		{
			if (RequireInitialization)
				Initialize();
			AddToDictionary(type, AfterCreationAspectDictionary, after);
		}

		public void Before(Type type, MethodInfo method, Action<object, object[]> before)
		{
			if (RequireInitialization)
				Initialize();
			AddToDictionary(new KeyValuePair<Type, MethodInfo>(type, method), BeforeMethodAspectDictionary, before);
		}

		public void Around(Type type, MethodInfo method, Func<object, object[], Func<object[], object>, object> around)
		{
			if (RequireInitialization)
				Initialize();
			AddToDictionary(new KeyValuePair<Type, MethodInfo>(type, method), AroundMethodAspectDictionary, around);
		}

		public void After(Type type, MethodInfo method, Func<object, object[], object, object> after)
		{
			if (RequireInitialization)
				Initialize();
			AddToDictionary(new KeyValuePair<Type, MethodInfo>(type, method), AfterMethodAspectDictionary, after);
		}

		public void Preparing(PreparingEventArgs pea)
		{
			if (HasCreationTypes)
			{
				var types = new List<Type>();
				foreach (var t in pea.Component.Services)
				{
					var ts = t as TypedService;
					if (ts != null)
						types.Add(ts.ServiceType);
				}
				if (types.Count > 0)
				{
					var set = new HashSet<Type>(AllCreationTypes);
					ProcessBeforeConstruction(set, types);
				}
			}
		}

		public void Activating(ActivatingEventArgs<object> aea)
		{
			var typeService = aea.Service as TypedService;
			var target = typeService != null ? typeService.ServiceType : aea.Instance.GetType();
			Type[] services;
			if (!ComponentServices.TryGetValue(aea.Component, out services))
			{
				services =
					(from s in aea.Component.Services
					 let ts = s as TypedService
					 where ts != null
					 select ts.ServiceType).ToArray();
				ComponentServices.TryAdd(aea.Component, services);
			}
			if (aea.Component.Sharing == InstanceSharing.Shared
				&& services.Any(it => !it.IsInterface))
				return;
			Compose(target, services, aea);
		}

		private void Compose(Type target, Type[] services, ActivatingEventArgs<object> aea)
		{
			if (HasCreationTypes)
			{
				var allCreationTypes = new HashSet<Type>(AllCreationTypes);
				ProcessAfterConstruction(aea.Instance, allCreationTypes, services);
			}
			if (!target.IsInterface || !HasRulesTypes && !HasMethodTypes)
				return;
			var intercepted = SatisfiesRule(services);
			if (intercepted.Length > 0)
				aea.Instance = CreateInterfaceProxy(target, aea.Instance, services.ToArray(), intercepted);
		}

		public IAspectComposer CreateInnerComposer()
		{
			if (RequireInitialization)
				Initialize();
			return new AspectRepository(this);
		}

		private void ProcessBeforeConstruction(HashSet<Type> allCreationTypes, IEnumerable<Type> services)
		{
			var types = new HashSet<Type>();
			foreach (var s in services)
				types.UnionWith(TypeUtility.GetTypeHierarchy(s));
			types.IntersectWith(allCreationTypes);
			if (types.Count > 0)
			{
				var allActions = new List<Action>();
				List<Action> actions;
				lock (BeforeCreationAspectDictionary)
				{
					foreach (var it in types)
						if (BeforeCreationAspectDictionary.TryGetValue(it, out actions))
							allActions.AddRange(actions);
				}
				foreach (var act in allActions)
					act();
			}
		}

		private void ProcessAfterConstruction(object instance, HashSet<Type> creationType, Type[] types)
		{
			creationType.IntersectWith(types);
			if (creationType.Count > 0)
			{
				var allActions = new List<Action<object>>();
				List<Action<object>> actions;
				lock (AfterCreationAspectDictionary)
				{
					foreach (var it in creationType)
						if (AfterCreationAspectDictionary.TryGetValue(it, out actions))
							allActions.AddRange(actions);
				}
				foreach (var act in allActions)
					act(instance);
			}
		}

		public object Create(Type type, object[] args, Type[] services)
		{
			if (RequireInitialization)
				Initialize();
			HashSet<Type> allCreationTypes = null;
			if (HasCreationTypes)
			{
				allCreationTypes = new HashSet<Type>(AllCreationTypes);
				var arr = new Type[services.Length + 1];
				arr[0] = type;
				for (int i = 1; i < services.Length + 1; i++)
					arr[i] = services[i - 1];
				ProcessBeforeConstruction(allCreationTypes, arr);
			}
			var serviceTypes = SatisfiesRule(services);
			var instance = serviceTypes.Length == 0
				? Activator.CreateInstance(type, args)
				: CreateClassProxy(type, args, services, serviceTypes);
			if (HasCreationTypes)
				ProcessAfterConstruction(instance, allCreationTypes, services);
			return instance;
		}

		private static readonly Type[] EmptyType = new Type[0];

		private Type[] SatisfiesRule(IEnumerable<Type> services)
		{
			HashSet<Type> intercept;
			if (HasMethodTypes)
			{
				intercept = new HashSet<Type>(AllMethodTypes);
				intercept.IntersectWith(services);
			}
			else intercept = new HashSet<Type>();
			if (HasRulesTypes)
			{
				var rules = new List<KeyValuePair<Func<Type, bool>, IInterceptor>>(AllRulesTypes);
				return
					intercept.Where(it => !rules.Any(r => !r.Key(it)))
					.Union(services.Where(it => rules.Any(r => r.Key(it))))
					.ToArray();
			}
			else if (HasMethodTypes && intercept.Count > 0) return intercept.ToArray();
			return EmptyType;
		}

		private Dictionary<MethodInfo, List<TAction>> CreateInterceptorDictionary<TAction>(
			Type serviceToProxy,
			Func<AspectRepository, Dictionary<KeyValuePair<Type, MethodInfo>, List<TAction>>> getAspectDictionary)
		{
			var current = this;
			var dict = new Dictionary<MethodInfo, List<TAction>>();
			while (current != null)
			{
				var ad = getAspectDictionary(current);
				lock (ad)
				{
					foreach (var kv in ad.Keys)
						if (kv.Key == serviceToProxy)
						{
							List<TAction> actions;
							if (dict.TryGetValue(kv.Value, out actions))
								actions.AddRange(ad[kv]);
							else
								dict[kv.Value] = ad[kv];
						}
				}
				current = current.ParentRepository;
			}
			return dict.Keys.Count > 0 ? dict : null;
		}

		private object CreateInterfaceProxy(Type type, object instance, Type[] services, Type[] targets)
		{
			return
				DynamicProxyProvider.CreateInterfaceProxy(
					type,
					instance,
					services,
					new CastleSelector(),
					targets.SelectMany(it => GetInterceptors(it)).ToArray());
		}

		private object CreateClassProxy(Type type, object[] args, Type[] services, Type[] targets)
		{
			return
				DynamicProxyProvider.CreateClassProxy(
					type,
					services,
					args,
					new CastleSelector(),
					targets.SelectMany(it => GetInterceptors(it)).ToArray());
		}

		private List<IInterceptor> GetInterceptors(Type type)
		{
			var list = new List<IInterceptor>();
			var before = CreateInterceptorDictionary<Action<object, object[]>>(type, a => a.BeforeMethodAspectDictionary);
			var around = CreateInterceptorDictionary<Func<object, object[], Func<object[], object>, object>>(type, a => a.AroundMethodAspectDictionary);
			var after = CreateInterceptorDictionary<Func<object, object[], object, object>>(type, a => a.AfterMethodAspectDictionary);
			if (before != null || around != null || after != null)
				list.Add(new CastleInterceptor(before, around, after));

			var current = this;
			List<IInterceptor> interceptors;
			while (current != null)
			{
				lock (current.InterceptorsDictionary)
				{
					if (current.InterceptorsDictionary.TryGetValue(type, out interceptors))
						list.AddRange(interceptors);
				}
				current = current.ParentRepository;
			}

			foreach (var rr in RegisteredRules)
				if (rr.Key(type))
					list.Add(rr.Value);

			return list;
		}

		public void Dispose()
		{
			if (!RequireInitialization && ParentRepository != null)
				ParentRepository.RegistrationChanged -= RefreshCache;
		}
	}
}
