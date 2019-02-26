using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Revenj.DatabasePersistence;
using Revenj.DomainPatterns;
using Revenj.Extensibility;

namespace Revenj
{
	/// <summary>
	/// Unit of work pattern.
	/// IDataContext with a transaction.
	/// Don't forget to Commit() before disposing
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

		public T Find<T>(string uri) where T : IIdentifiable
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.Find<T>(uri);
		}

		public Task<T> FindAsync<T>(string uri, CancellationToken cancellationToken) where T : IIdentifiable
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.FindAsync<T>(uri, cancellationToken);
		}

		public T[] Find<T>(IEnumerable<string> uris) where T : IIdentifiable
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.Find<T>(uris);
		}

		public Task<IEnumerable<T>> FindAsync<T>(IEnumerable<string> uris, CancellationToken cancellationToken) where T : IIdentifiable
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.FindAsync<T>(uris, cancellationToken);
		}

		public IQueryable<T> Query<T>() where T : IDataSource
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.Query<T>();
		}

		public T[] Search<T>(ISpecification<T> filter, int? limit, int? offset) where T : IDataSource
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.Search<T>(filter, limit, offset);
		}

		public Task<IEnumerable<T>> SearchAsync<T>(ISpecification<T> filter, int? limit, int? offset, CancellationToken cancellationToken) where T : IDataSource
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.SearchAsync<T>(filter, limit, offset, cancellationToken);
		}

		public long Count<T>(ISpecification<T> filter) where T : IDataSource
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.Count<T>(filter);
		}

		public Task<long> CountAsync<T>(ISpecification<T> filter, CancellationToken cancellationToken) where T : IDataSource
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.CountAsync<T>(filter, cancellationToken);
		}

		public bool Exists<T>(ISpecification<T> filter) where T : IDataSource
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.Exists<T>(filter);
		}

		public Task<bool> ExistsAsync<T>(ISpecification<T> filter, CancellationToken cancellationToken) where T : IDataSource
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.ExistsAsync<T>(filter, cancellationToken);
		}

		public void Create<T>(IEnumerable<T> aggregates) where T : IAggregateRoot
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			Context.Create(aggregates);
		}

		public Task CreateAsync<T>(IEnumerable<T> aggregates, CancellationToken cancellationToken) where T : IAggregateRoot
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.CreateAsync(aggregates, cancellationToken);
		}

		public void Update<T>(IEnumerable<KeyValuePair<T, T>> pairs) where T : IAggregateRoot
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			Context.Update(pairs);
		}

		public Task UpdateAsync<T>(IEnumerable<KeyValuePair<T, T>> pairs, CancellationToken cancellationToken) where T : IAggregateRoot
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.UpdateAsync(pairs, cancellationToken);
		}

		public void Delete<T>(IEnumerable<T> aggregates) where T : IAggregateRoot
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			Context.Delete(aggregates);
		}

		public Task DeleteAsync<T>(IEnumerable<T> aggregates, CancellationToken cancellationToken) where T : IAggregateRoot
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.DeleteAsync(aggregates, cancellationToken);
		}

		public string[] Submit<T>(IEnumerable<T> events) where T : IEvent
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.Submit(events);
		}

		public Task<string[]> SubmitAsync<T>(IEnumerable<T> events, CancellationToken cancellationToken) where T : IEvent
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.SubmitAsync(events, cancellationToken);
		}

		public T Populate<T>(IReport<T> report)
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.Populate(report);
		}

		public Task<T> PopulateAsync<T>(IReport<T> report, CancellationToken cancellationToken)
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.PopulateAsync(report, cancellationToken);
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

		public Task<IEnumerable<IHistory<T>>> HistoryAsync<T>(IEnumerable<string> uris, CancellationToken cancellationToken) where T : IObjectHistory
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.HistoryAsync<T>(uris, cancellationToken);
		}

		public OlapCubeQueryBuilder<TSource> CubeBuilder<TCube, TSource>()
			where TCube : IOlapCubeQuery<TSource>
			where TSource : IDataSource
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.CubeBuilder<TCube, TSource>();
		}

		public TResult[] InvalidItems<TValidation, TResult>(ISpecification<TResult> specification)
			where TValidation : IValidation<TResult>
			where TResult : IIdentifiable
		{
			if (Finished)
				throw new InvalidOperationException("Transaction was already closed");
			return Context.InvalidItems<TValidation, TResult>(specification);
		}

		public void Queue<T>(IEnumerable<T> events) where T : IEvent
		{
			Context.Queue(events);
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
		public static IUnitOfWork DoWork(this IServiceProvider locator)
		{
			return new UnitOfWork(locator.Resolve<IObjectFactory>());
		}
	}
}
