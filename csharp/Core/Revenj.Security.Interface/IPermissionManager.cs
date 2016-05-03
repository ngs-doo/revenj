using System;
using System.Diagnostics.Contracts;
using System.Linq;
using System.Linq.Expressions;
using System.Security.Principal;
using System.Threading;

namespace Revenj.Security
{
	/// <summary>
	/// Permission manager service for setting up permission rules
	/// and checking access to data.
	/// </summary>
	public interface IPermissionManager
	{
		/// <summary>
		/// Check if user can access provided identifier.
		/// Identifier can be type name, service name or something similar.
		/// Rules for access can be defined for smaller parts of identifier.
		/// </summary>
		/// <param name="identifier">identifier is sliced by . and matched against the rules</param>
		/// <param name="user">principal to check</param>
		/// <returns>is user allowed to access</returns>
		bool CanAccess(string identifier, IPrincipal user);
		/// <summary>
		/// Filter data based on provided user principal.
		/// This will apply various permissions registered for this user and for data of type T
		/// If interface is provided, permissions must be defined explicitly for that interface.
		/// Filter will be applied on Queryable tree, which can be evaluated at a later time.
		/// </summary>
		/// <typeparam name="T">data type</typeparam>
		/// <param name="user">user principal</param>
		/// <param name="data">data to filter</param>
		/// <returns>filtered data based on permission rules</returns>
		IQueryable<T> ApplyFilters<T>(IPrincipal user, IQueryable<T> data);
		/// <summary>
		/// Filter data based on provided user principal.
		/// This will apply various permissions registered for this user and for data of type T
		/// If interface is provided, permissions must be defined explicitly for that interface.
		/// Filter will be applied immediately.
		/// </summary>
		/// <typeparam name="T">data type</typeparam>
		/// <param name="user">user principal</param>
		/// <param name="data">data to filter</param>
		/// <returns>filtered data based on permission rules</returns>
		T[] ApplyFilters<T>(IPrincipal user, T[] data);
		/// <summary>
		/// Register filter predicate for data.
		/// Apply this filter for specified roles, or when role is missing.
		/// Only exact match will be filtered, so if data can be queried through some 
		/// interface, explicit registration must be done.
		/// </summary>
		/// <typeparam name="T">data type</typeparam>
		/// <param name="filter">predicate for filtering</param>
		/// <param name="role">role for which the principal will be tested</param>
		/// <param name="inverse">should filter apply for this role, or when this role is missing</param>
		/// <returns>disposable for unregistering filter</returns>
		IDisposable RegisterFilter<T>(Expression<Func<T, bool>> filter, string role, bool inverse);
	}

	/// <summary>
	/// Helper for permission manager service
	/// </summary>
	public static class PermissionManagerHelper
	{
		/// <summary>
		/// Check if current principal bound to thread can access some resource.
		/// Resource identity will be provided from type full name.
		/// </summary>
		/// <typeparam name="T">object type</typeparam>
		/// <param name="manager">permission service</param>
		/// <returns>is user allowed to access requested resource</returns>
		public static bool CanAccess<T>(this IPermissionManager manager)
		{
			Contract.Requires(manager != null);

			return manager.CanAccess(typeof(T).FullName, Thread.CurrentPrincipal);
		}
		/// <summary>
		/// Check if current principal bound to thread can access some resource.
		/// Resource identity will be provided from target type argument full name.
		/// </summary>
		/// <param name="manager">permission service</param>
		/// <param name="target">type argument</param>
		/// <returns>is user allowed to access requested resource</returns>
		public static bool CanAccess(this IPermissionManager manager, Type target)
		{
			Contract.Requires(manager != null);

			return manager.CanAccess(target.FullName, Thread.CurrentPrincipal);
		}
		/// <summary>
		/// Filter data based on user principal bound to current Thread.
		/// This will apply various permissions registered for this user and for data of type T
		/// If interface is provided, permissions must be defined explicitly for that interface.
		/// </summary>
		/// <typeparam name="T">object type</typeparam>
		/// <param name="manager">permission service</param>
		/// <param name="data">data to filter</param>
		/// <returns>filtered data based on permission rules</returns>
		public static T[] ApplyFilters<T>(this IPermissionManager manager, T[] data)
		{
			Contract.Requires(manager != null);

			return manager.ApplyFilters(Thread.CurrentPrincipal, data);
		}
		/// <summary>
		/// Specify filter which will be applied when user principal is in specified role.
		/// Users which are not in specified role will not have this filter applied to them.
		/// </summary>
		/// <typeparam name="T">object type</typeparam>
		/// <param name="manager">permission service</param>
		/// <param name="filter">filtering expression</param>
		/// <param name="role">for which role filter applies</param>
		/// <returns>un-register instance. If called registration will be removed</returns>
		public static IDisposable RegisterForRole<T>(this IPermissionManager manager, Expression<Func<T, bool>> filter, string role)
		{
			return manager.RegisterFilter(filter, role, false);
		}
		/// <summary>
		/// Specify filter which will be applied when user principal is not in specified role.
		/// Users which are not in specified role will not have this filter applied to them.
		/// </summary>
		/// <typeparam name="T">object type</typeparam>
		/// <param name="manager">permission service</param>
		/// <param name="filter">filtering expression</param>
		/// <param name="role">for which role filter applies</param>
		/// <returns>un-register instance. If called registration will be removed</returns>
		public static IDisposable RegisterWhenNotInRole<T>(this IPermissionManager manager, Expression<Func<T, bool>> filter, string role)
		{
			return manager.RegisterFilter(filter, role, false);
		}
	}
}
