using System;
using System.Diagnostics.Contracts;

namespace Revenj.Extensibility
{
	/// <summary>
	/// Inversion of control container service.
	/// Object factory should be used for scoping so dependencies can be specified for that scope.
	/// </summary>
	[ContractClass(typeof(ObjectFactoryContract))]
	public interface IObjectFactory : IServiceProvider, IDisposable
	{
		/// <summary>
		/// Check if service is registered with the factory
		/// </summary>
		/// <param name="type">service type</param>
		/// <returns>is service registered</returns>
		bool IsRegistered(Type type);
		/// <summary>
		/// Resolve service from the factory. Instance is resolved based on scoping rules.
		/// If service can't be resolved an exception will be thrown.
		/// </summary>
		/// <param name="type">service type</param>
		/// <param name="args">argument for service resolution</param>
		/// <returns>instance for requested service</returns>
		object Resolve(Type type, object[] args);

		/// <summary>
		/// Create nested scope. 
		/// Scope should be used as unit of work, after which services resolved from that scope should be disposed.
		/// If id is provided scope will be traced.
		/// </summary>
		/// <param name="id">specify scope identification</param>
		/// <returns>created nested scope</returns>
		IObjectFactory CreateScope(string id);
		/// <summary>
		/// Named scopes can be used for durable transactions.
		/// </summary>
		/// <param name="id">scope identification</param>
		/// <returns>found scope</returns>
		IObjectFactory FindScope(string id);
		/// <summary>
		/// Register services to the container.
		/// </summary>
		/// <param name="builder">configuration for additional services</param>
		void Register(IObjectFactoryBuilder builder);
	}

	[ContractClassFor(typeof(IObjectFactory))]
	internal sealed class ObjectFactoryContract : IObjectFactory
	{
		public bool IsRegistered(Type type)
		{
			Contract.Requires(type != null);
			return false;
		}
		public object Resolve(Type type, object[] args)
		{
			Contract.Requires(type != null);
			return false;
		}
		public IObjectFactory CreateScope(string tag)
		{
			Contract.Ensures(Contract.Result<IObjectFactory>() != null);
			return null;
		}
		public IObjectFactory FindScope(string tag)
		{
			Contract.Ensures(Contract.Result<IObjectFactory>() != null);
			return null;
		}
		public void Register(IObjectFactoryBuilder builder)
		{
			Contract.Requires(builder != null);
		}
		public void Dispose() { }

		public object GetService(Type serviceType)
		{
			Contract.Requires(serviceType != null);
			return null;
		}
	}

	/// <summary>
	/// Container service helper
	/// </summary>
	public static partial class ObjectFactoryHelper
	{
		/// <summary>
		/// Create unnamed scope. This scope will not be tracked.
		/// </summary>
		/// <param name="factory">container service</param>
		/// <returns>nested service</returns>
		public static IObjectFactory CreateInnerFactory(this IObjectFactory factory)
		{
			Contract.Requires(factory != null);

			return factory.CreateScope(null);
		}
		/// <summary>
		/// Resolve service from current scope.
		/// If service can't be resolved an exception will be thrown.
		/// </summary>
		/// <typeparam name="T">service type</typeparam>
		/// <param name="factory">current scope</param>
		/// <returns>found and resolved service</returns>
		public static T Resolve<T>(this IObjectFactory factory)
		{
			Contract.Requires(factory != null);

			return (T)factory.Resolve(typeof(T), null);
		}
		/// <summary>
		/// Resolve service from current scope.
		/// If service can't be resolved an exception will be thrown.
		/// </summary>
		/// <typeparam name="T">resolved service type</typeparam>
		/// <param name="factory">current scope</param>
		/// <param name="type">registered/actual service type</param>
		/// <returns>found and resolved service</returns>
		public static T Resolve<T>(this IObjectFactory factory, Type type)
		{
			Contract.Requires(factory != null);

			return (T)factory.Resolve(type, null);
		}
	}
}
