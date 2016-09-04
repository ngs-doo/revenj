using System;
using System.Collections.Generic;
using System.Diagnostics.Contracts;

namespace Revenj.Extensibility
{
	/// <summary>
	/// Builder for registering services to the container.
	/// Services can be instances, types or factories
	/// </summary>
	[ContractClass(typeof(ObjectFactoryBuilder))]
	public interface IObjectFactoryBuilder
	{
		/// <summary>
		/// Registered instances.
		/// </summary>
		IEnumerable<IFactoryBuilderInstance> Instances { get; }
		/// <summary>
		/// Registered types.
		/// </summary>
		IEnumerable<IFactoryBuilderType> Types { get; }
		/// <summary>
		/// Registered factories.
		/// </summary>
		IEnumerable<IFactoryBuilderFunc> Funcs { get; }
		/// <summary>
		/// Register new instance.
		/// </summary>
		/// <param name="item">instance builder</param>
		void Add(IFactoryBuilderInstance item);
		/// <summary>
		/// Register new type.
		/// </summary>
		/// <param name="item">type builder</param>
		void Add(IFactoryBuilderType item);
		/// <summary>
		/// Register new factory.
		/// </summary>
		/// <param name="item">factory builder</param>
		void Add(IFactoryBuilderFunc item);
	}

	/// <summary>
	/// Instance builder for container registration
	/// </summary>
	[ContractClass(typeof(FactoryBuilderInstance))]
	public interface IFactoryBuilderInstance
	{
		/// <summary>
		/// Provided instance.
		/// </summary>
		object Instance { get; }
		/// <summary>
		/// Registered as service.
		/// </summary>
		Type AsType { get; }
	}

	[ContractClassFor(typeof(IFactoryBuilderInstance))]
	internal sealed class FactoryBuilderInstance : IFactoryBuilderInstance
	{
		internal object instance;
		public object Instance
		{
			get
			{
				Contract.Ensures(Contract.Result<object>() != null);
				return instance;
			}
		}
		public Type AsType { get; set; }
	}

	/// <summary>
	/// Service instance scope.
	/// </summary>
	public enum InstanceScope
	{
		/// <summary>
		/// Same service will be resolved on subsequent request inside this
		/// and nested scopes.
		/// </summary>
		Singleton,
		/// <summary>
		/// New service will be resolved each time.
		/// </summary>
		Transient,
		/// <summary>
		/// Same service will be resolved on subsequent request inside this scope.
		/// </summary>
		Context
	}

	/// <summary>
	/// Type builder for container registration
	/// </summary>
	[ContractClass(typeof(FactoryBuilderType))]
	public interface IFactoryBuilderType
	{
		/// <summary>
		/// Registered type.
		/// </summary>
		Type Type { get; }
		/// <summary>
		/// Specified scope.
		/// </summary>
		InstanceScope Scope { get; }
		/// <summary>
		/// Is registered as generic type.
		/// </summary>
		bool IsGeneric { get; }
		/// <summary>
		/// Registered as service.
		/// </summary>
		Type[] AsType { get; }
	}

	[ContractClassFor(typeof(IFactoryBuilderType))]
	internal class FactoryBuilderType : IFactoryBuilderType
	{
		internal Type type;
		public Type Type
		{
			get
			{
				Contract.Ensures(Contract.Result<Type>() != null);
				return type;
			}
		}
		public InstanceScope Scope { get; set; }
		public bool IsGeneric { get; set; }
		public Type[] AsType { get; set; }
	}

	/// <summary>
	/// Factory builder for container registration
	/// </summary>
	public interface IFactoryBuilderFunc
	{
		/// <summary>
		/// Registered factory.
		/// </summary>
		Func<IObjectFactory, object> Func { get; }
		/// <summary>
		/// Specified scope.
		/// </summary>
		InstanceScope Scope { get; }
		/// <summary>
		/// Registered as service.
		/// </summary>
		Type[] AsType { get; }
	}

	internal sealed class FactoryBuilderFunc : IFactoryBuilderFunc
	{
		internal Func<IObjectFactory, object> func;
		public Func<IObjectFactory, object> Func { get { return func; } }
		public InstanceScope Scope { get; set; }
		public Type[] AsType { get; set; }
	}

	[ContractClassFor(typeof(IObjectFactoryBuilder))]
	internal sealed class ObjectFactoryBuilder : IObjectFactoryBuilder
	{
		private List<IFactoryBuilderInstance> instances = new List<IFactoryBuilderInstance>();
		private List<IFactoryBuilderType> types = new List<IFactoryBuilderType>();
		private List<IFactoryBuilderFunc> funcs = new List<IFactoryBuilderFunc>();

		public IEnumerable<IFactoryBuilderInstance> Instances { get { return instances; } }
		public IEnumerable<IFactoryBuilderType> Types { get { return types; } }
		public IEnumerable<IFactoryBuilderFunc> Funcs { get { return funcs; } }
		public void Add(IFactoryBuilderInstance item) { instances.Add(item); }
		public void Add(IFactoryBuilderType item) { types.Add(item); }
		public void Add(IFactoryBuilderFunc item) { funcs.Add(item); }
	}

	/// <summary>
	/// Helper methods for container registration
	/// </summary>
	public static partial class ObjectFactoryHelper
	{
		/// <summary>
		/// Register specified type to the container as self using transient scope.
		/// </summary>
		/// <param name="factory">container scope</param>
		/// <param name="type">service</param>
		public static void RegisterType(this IObjectFactory factory, Type type)
		{
			RegisterType(factory, type, InstanceScope.Transient);
		}
		/// <summary>
		/// Register specified type to the container as custom service with provided scope.
		/// </summary>
		/// <param name="factory">container scope</param>
		/// <param name="type">service type</param>
		/// <param name="asType">register as</param>
		/// <param name="scope">resolution scope</param>
		public static void RegisterType(this IObjectFactory factory, Type type, InstanceScope scope, params Type[] asType)
		{
			Contract.Requires(factory != null);
			Contract.Requires(type != null);

			var ofb = new ObjectFactoryBuilder();
			ofb.Add(new FactoryBuilderType { type = type, Scope = scope, AsType = asType });
			factory.Register(ofb);
		}
		/// <summary>
		/// Register specified type to the container as custom service with provided scope.
		/// </summary>
		/// <param name="builder">container builder</param>
		/// <param name="type">service type</param>
		/// <param name="asType">register as</param>
		/// <param name="scope">resolution scope</param>
		/// <param name="isGeneric">is generic type</param>
		public static void RegisterType(
			this IObjectFactoryBuilder builder,
			Type type,
			InstanceScope scope,
			bool isGeneric,
			params Type[] asType)
		{
			Contract.Requires(builder != null);
			Contract.Requires(type != null);

			builder.Add(new FactoryBuilderType { type = type, Scope = scope, AsType = asType, IsGeneric = isGeneric });
		}
		/// <summary>
		/// Register specified type to the container. Default scope is transient
		/// </summary>
		/// <typeparam name="TService">service type</typeparam>
		/// <param name="builder">container builder</param>
		/// <param name="scope">resolution scope</param>
		public static void RegisterType<TService>(
			this IObjectFactoryBuilder builder,
			InstanceScope scope = InstanceScope.Transient)
		{
			Contract.Requires(builder != null);

			builder.Add(new FactoryBuilderType { type = typeof(TService), Scope = scope, IsGeneric = false });
		}
		/// <summary>
		/// Register specified type to the container as custom service. Default scope is transient
		/// </summary>
		/// <typeparam name="TService">service implementation</typeparam>
		/// <typeparam name="TAs">service type</typeparam>
		/// <param name="builder">container builder</param>
		/// <param name="scope">resolution scope</param>
		public static void RegisterType<TService, TAs>(
			this IObjectFactoryBuilder builder,
			InstanceScope scope = InstanceScope.Transient)
			where TService : TAs
		{
			Contract.Requires(builder != null);

			builder.Add(new FactoryBuilderType { type = typeof(TService), Scope = scope, AsType = new[] { typeof(TAs) }, IsGeneric = false });
		}
		/// <summary>
		/// Register specified type to the container as custom services. Default scope is transient
		/// </summary>
		/// <typeparam name="TService">service implementation</typeparam>
		/// <typeparam name="TAs1">service type</typeparam>
		/// <typeparam name="TAs2">additional service type</typeparam>
		/// <param name="builder">container builder</param>
		/// <param name="scope">resolution scope</param>
		public static void RegisterType<TService, TAs1, TAs2>(
			this IObjectFactoryBuilder builder,
			InstanceScope scope = InstanceScope.Transient)
			where TService : TAs1, TAs2
		{
			Contract.Requires(builder != null);

			builder.Add(new FactoryBuilderType { type = typeof(TService), Scope = scope, AsType = new[] { typeof(TAs1), typeof(TAs2) }, IsGeneric = false });
		}
		/// <summary>
		/// Register generic type to the container as custom service with provided scope.
		/// </summary>
		/// <param name="factory">container scope</param>
		/// <param name="type">service type</param>
		/// <param name="asType">register as</param>
		/// <param name="scope">resolution scope</param>
		public static void RegisterGeneric(this IObjectFactory factory, Type type, InstanceScope scope, params Type[] asType)
		{
			Contract.Requires(factory != null);
			Contract.Requires(type != null);
			Contract.Requires(asType != null);

			var ofb = new ObjectFactoryBuilder();
			ofb.Add(new FactoryBuilderType { type = type, Scope = scope, AsType = asType, IsGeneric = true });
			factory.Register(ofb);
		}
		/// <summary>
		/// Register multiple types to the container as singletons.
		/// </summary>
		/// <param name="factory">current scope</param>
		/// <param name="types">services</param>
		public static void RegisterTypes(this IObjectFactory factory, IEnumerable<Type> types)
		{
			Contract.Requires(factory != null);
			Contract.Requires(types != null);

			RegisterTypes(factory, types, InstanceScope.Singleton);
		}
		/// <summary>
		/// Register multiple types to the container with specified scope.
		/// </summary>
		/// <param name="factory">current scope</param>
		/// <param name="types">services</param>
		/// <param name="scope">resolution scope</param>
		public static void RegisterTypes(this IObjectFactory factory, IEnumerable<Type> types, InstanceScope scope)
		{
			Contract.Requires(factory != null);
			Contract.Requires(types != null);

			var ofb = new ObjectFactoryBuilder();
			foreach (var t in types)
				ofb.Add(new FactoryBuilderType { type = t, Scope = scope });
			factory.Register(ofb);
		}
		/// <summary>
		/// Register instance to the container scope.
		/// Registered instance will be available in nested scopes too.
		/// </summary>
		/// <typeparam name="T">service</typeparam>
		/// <param name="factory">current scope</param>
		/// <param name="instance">provided instance</param>
		public static void RegisterInstance<T>(this IObjectFactory factory, T instance)
		{
			Contract.Requires(factory != null);
			Contract.Requires(instance != null);

			var ofb = new ObjectFactoryBuilder();
			ofb.Add(new FactoryBuilderInstance { instance = instance, AsType = typeof(T) });
			factory.Register(ofb);
		}
		/// <summary>
		/// Register instance to the container builder.
		/// Registered instance will be available in nested scopes too.
		/// </summary>
		/// <typeparam name="T">service</typeparam>
		/// <param name="builder">container builder</param>
		/// <param name="instance">provided instance</param>
		public static void RegisterSingleton<T>(this IObjectFactoryBuilder builder, T instance)
		{
			Contract.Requires(builder != null);
			Contract.Requires(instance != null);

			builder.Add(new FactoryBuilderInstance { instance = instance, AsType = typeof(T) });
		}
		/// <summary>
		/// Register all interfaces and specified service for provided instance to the container.
		/// Services will be available in nested scopes too.
		/// </summary>
		/// <typeparam name="T">service type</typeparam>
		/// <param name="factory">current scope</param>
		/// <param name="instance">provided instance</param>
		public static void RegisterInterfaces<T>(this IObjectFactory factory, T instance)
		{
			Contract.Requires(factory != null);
			Contract.Requires(instance != null);

			var ofb = new ObjectFactoryBuilder();
			var interfaces = instance.GetType().GetInterfaces();
			foreach (var i in interfaces)
				ofb.Add(new FactoryBuilderInstance { instance = instance, AsType = i });
			if (typeof(T).IsInterface)
				ofb.Add(new FactoryBuilderInstance { instance = instance, AsType = typeof(T) });
			factory.Register(ofb);
		}
		/// <summary>
		/// Register factory to the container with transient scope. 
		/// Service will be resolved from the factory using contexed scope.
		/// </summary>
		/// <typeparam name="T">service type</typeparam>
		/// <param name="factory">current scope</param>
		/// <param name="func">factory to service</param>
		public static void RegisterFunc<T>(this IObjectFactory factory, Func<IObjectFactory, T> func)
		{
			RegisterFunc(factory, func, InstanceScope.Transient);
		}
		/// <summary>
		/// Register factory to the container with specified scope.
		/// Service will be resolved from the factory using context scope.
		/// </summary>
		/// <typeparam name="T">service type</typeparam>
		/// <param name="factory">current scope</param>
		/// <param name="func">factory to service</param>
		/// <param name="scope">factory scope</param>
		public static void RegisterFunc<T>(this IObjectFactory factory, Func<IObjectFactory, T> func, InstanceScope scope)
		{
			Contract.Requires(factory != null);
			Contract.Requires(func != null);

			var ofb = new ObjectFactoryBuilder();
			ofb.Add(new FactoryBuilderFunc { func = f => func(f), AsType = new[] { typeof(T) }, Scope = scope });
			factory.Register(ofb);
		}
		/// <summary>
		/// Register factory to the container builder with specified scope. Default scope is Transient.
		/// Service will be resolved from the factory using context scope.
		/// </summary>
		/// <typeparam name="T">service type</typeparam>
		/// <param name="builder">container builder</param>
		/// <param name="func">factory to service</param>
		/// <param name="scope">factory scope</param>
		public static void RegisterFunc<T>(this IObjectFactoryBuilder builder, Func<IObjectFactory, T> func, InstanceScope scope = InstanceScope.Transient)
		{
			Contract.Requires(builder != null);
			Contract.Requires(func != null);

			builder.Add(new FactoryBuilderFunc { func = f => func(f), AsType = new[] { typeof(T) }, Scope = scope });
		}
		/// <summary>
		/// Register factory to the container builder with specified scope. Default scope is Transient.
		/// Service will be resolved from the factory using context scope.
		/// </summary>
		/// <typeparam name="T">implementation type</typeparam>
		/// <typeparam name="TAs1">as service</typeparam>
		/// <typeparam name="TAs2">as alternative service</typeparam>
		/// <param name="builder">container builder</param>
		/// <param name="func">factory to service</param>
		/// <param name="scope">factory scope</param>
		public static void RegisterFunc<T, TAs1, TAs2>(this IObjectFactoryBuilder builder, Func<IObjectFactory, T> func, InstanceScope scope = InstanceScope.Transient)
			where T : TAs1, TAs2
		{
			Contract.Requires(builder != null);
			Contract.Requires(func != null);

			builder.Add(new FactoryBuilderFunc { func = f => func(f), AsType = new[] { typeof(TAs1), typeof(TAs2) }, Scope = scope });
		}
	}
}
