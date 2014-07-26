using System;
using System.Collections.Generic;
using System.Diagnostics.Contracts;
using System.Linq;

namespace Revenj.DomainPatterns
{
	/// <summary>
	/// Validation service. Validation checks which items don't pass
	/// validation and provides an explanation why.
	/// </summary>
	/// <typeparam name="TEntity">domain object type</typeparam>
	public interface IValidation<TEntity> where TEntity : IIdentifiable
	{
		/// <summary>
		/// Apply filter on provided items and filter items which fail 
		/// defined specifications.
		/// </summary>
		/// <param name="items">items to check</param>
		/// <returns>invalid items</returns>
		IQueryable<TEntity> FindInvalidItems(IQueryable<TEntity> items);
		/// <summary>
		/// Check if provided objects can be persisted.
		/// Sometimes invalid objects are allowed to be persisted, but they are still considered invalid.
		/// </summary>
		/// <param name="items">items to check</param>
		/// <returns>all items can be persisted</returns>
		bool CanPersist(IEnumerable<TEntity> items);
		/// <summary>
		/// Reason why does provided object fail validation.
		/// </summary>
		/// <param name="item">invalid object</param>
		/// <returns>why is object invalid</returns>
		string GetErrorDescription(TEntity item);
	}

	/// <summary>
	/// Validation result signature.
	/// </summary>
	public interface IValidationResult : IIdentifiable
	{
		/// <summary>
		/// Explanation why does current identity object fails validation.
		/// </summary>
		string ErrorDescription { get; }
	}
	/// <summary>
	/// Utility for validation service
	/// </summary>
	public static class ValidationHelper
	{
		/// <summary>
		/// Aggregate errors for invalid items in a single message.
		/// </summary>
		/// <typeparam name="TEntity">domain object type</typeparam>
		/// <param name="validation">validation service</param>
		/// <param name="items">invalid items</param>
		/// <returns>aggregated result message</returns>
		public static string GenerateDescription<TEntity>(this IValidation<TEntity> validation, IEnumerable<TEntity> items)
			where TEntity : IIdentifiable
		{
			Contract.Requires(validation != null);
			Contract.Requires(items != null);
			Contract.Requires(items.All(it => it != null));
			Contract.Ensures(Contract.Result<string>() != null);

			return string.Join(
				Environment.NewLine,
				from i in items
				select validation.GetErrorDescription(i));
		}
	}
}
