using System;
using System.Collections.Generic;
using System.Linq;
using Revenj.DatabasePersistence;
using Revenj.DomainPatterns;
using Revenj.Extensibility;

namespace Revenj.Core
{
	/// <summary>
	/// Unit of work pattern.
	/// IDataContext with a transaction.
	/// </summary>
	public interface IUnitOfWork : IDataContext, IDisposable
	{
		/// <summary>
		/// Confirm database transaction. 
		/// After commit, unit of work needs to be disposed
		/// </summary>
		void Commit();
		/// <summary>
		/// Rollback database transaction.
		/// After rollback, unit of work needs to be disposed
		/// </summary>
		void Rollback();
	}

	internal class UnitOfWork : IUnitOfWork
	{
		private readonly IObjectFactory Scope;
		private readonly IDatabaseQuery DatabaseQuery;
		private readonly IDatabaseQueryManager Manager;
		private readonly IDataContext Context;
		private bool Finished;

		public UnitOfWork(IObjectFactory factory)
		{
			Scope = factory.CreateInnerFactory();
			Manager = factory.Resolve<IDatabaseQueryManager>();
			DatabaseQuery = Manager.BeginTransaction();
			Scope.RegisterInstance(DatabaseQuery);
			Context = Scope.Resolve<IDataContext>();
		}

		public void Commit()
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			Finished = true;
			Manager.Commit(DatabaseQuery);
		}

		public void Rollback()
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			Finished = true;
			Manager.Rollback(DatabaseQuery);
		}

		public void Dispose()
		{
			if (!Finished)
				Rollback();
			Scope.Dispose();
		}

		public T[] Find<T>(IEnumerable<string> uris) where T : IIdentifiable
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.Find<T>(uris);
		}

		public IQueryable<T> Query<T>() where T : IDataSource
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.Query<T>();
		}

		public void Create<T>(IEnumerable<T> aggregates) where T : IAggregateRoot
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			Context.Create(aggregates);
		}

		public void Update<T>(IEnumerable<KeyValuePair<T, T>> pairs) where T : IAggregateRoot
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			Context.Update(pairs);
		}

		public void Delete<T>(IEnumerable<T> aggregates) where T : IAggregateRoot
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			Context.Delete(aggregates);
		}

		public void Submit<T>(IEnumerable<T> events) where T : IDomainEvent
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			Context.Submit(events);
		}

		public T Populate<T>(IReport<T> report)
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.Populate(report);
		}

		public IObservable<NotifyInfo> Track<T>() where T : IIdentifiable
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.Track<T>();
		}

		public IHistory<T>[] History<T>(IEnumerable<string> uris) where T : IObjectHistory
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.History<T>(uris);
		}

		public OlapCubeQueryBuilder CubeBuilder<T>() where T : IOlapCubeQuery
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.CubeBuilder<T>();
		}

		public TResult[] InvalidItems<TValidation, TResult>(ISpecification<TResult> specification)
			where TValidation : IValidation<TResult>
			where TResult : IIdentifiable
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.InvalidItems<TValidation, TResult>(specification);
		}
	}

	/// <summary>
	/// Helper class for IServiceLocator
	/// </summary>
	public static class LocatorHelper
	{
		/// <summary>
		/// Create new unit of work from current locator
		/// </summary>
		/// <param name="locator">service locator</param>
		/// <returns>unit of work</returns>
		public static IUnitOfWork DoWork(IServiceLocator locator)
		{
			return new UnitOfWork(locator.Resolve<IObjectFactory>());
		}
	}
}
