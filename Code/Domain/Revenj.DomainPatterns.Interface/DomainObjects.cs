using System;

namespace Revenj.DomainPatterns
{
	/// <summary>
	/// Queryable domain object signature
	/// </summary>
	public interface IDataSource { }

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
	public interface IIdentifiable : IDataSource
	{
		/// <summary>
		/// String representation of object identity
		/// </summary>
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
}
