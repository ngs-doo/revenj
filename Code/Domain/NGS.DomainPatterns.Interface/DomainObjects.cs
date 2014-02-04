using System;

namespace NGS.DomainPatterns
{
	/// <summary>
	/// Entity domain object signature.
	/// </summary>
	public interface IEntity : IEquatable<IEntity>
	{
	}
	/// <summary>
	/// Domain objects with URI property.
	/// URI is string representation of it's unique identity.
	/// </summary>
	public interface IIdentifiable
	{
		string URI { get; }
	}
	/// <summary>
	/// Aggregate root object signature.
	/// Boundary for various entities and values which are persisted as a whole.
	/// </summary>
	public interface IAggregateRoot : IEntity, IIdentifiable
	{
	}
	/// <summary>
	/// Projection on an entity.
	/// Snowflake schema which specialized use for some use case.
	/// </summary>
	/// <typeparam name="TEntity"></typeparam>
	public interface ISnowflake<TEntity> : IEntity, IIdentifiable
		where TEntity : IEntity
	{
		/// <summary>
		/// Update snowflake when entity is changed.
		/// </summary>
		/// <param name="entity">starting entity</param>
		void Update(TEntity entity);
	}
	/// <summary>
	/// Common type for nested values.
	/// </summary>
	/// <typeparam name="TValue">value type</typeparam>
	public interface INestedValue<out TValue>
	{
		/// <summary>
		/// Get actual value
		/// </summary>
		TValue Value { get; }
	}
	//TODO: remove!?
	public interface IAggregateRootRepository<out TRoot> : IQueryableRepository<TRoot>, IRepository<TRoot>
	{
		TRoot[] Create(int count, Action<TRoot[]> initialize);
		TRoot[] Update(string[] uris, Action<TRoot[]> change);
		void Delete(string[] uris);
	}

	public static class AggregateRootRepositoryHelper
	{
		public static TRoot Create<TRoot>(this IAggregateRootRepository<TRoot> repository, Action<TRoot> initialize)
		{
			return repository.Create(1, roots => initialize(roots[0]))[0];
		}

		public static TRoot Update<TRoot>(this IAggregateRootRepository<TRoot> repository, string uri, Action<TRoot> change)
		{
			return repository.Update(new[] { uri }, roots => change(roots[0]))[0];
		}

		public static void Delete<TRoot>(this IAggregateRootRepository<TRoot> repository, string uri)
		{
			repository.Delete(new[] { uri });
		}
	}
}
