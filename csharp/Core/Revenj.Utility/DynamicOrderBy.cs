using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Linq.Expressions;

namespace Revenj.Utility
{
	/// <summary>
	/// Utility class for conversion from dynamic order to static order.
	/// </summary>
	public static class DynamicOrderBy
	{
		/// <summary>
		/// Order by provided properties using specified direction.
		/// Properties (or paths) are specified as string.
		/// </summary>
		/// <typeparam name="T">collection type</typeparam>
		/// <param name="collection">collection projection</param>
		/// <param name="orderBy">order configuration</param>
		/// <returns>sorted projection</returns>
		public static IQueryable<T> OrderBy<T>(this IQueryable<T> collection, IEnumerable<KeyValuePair<string, bool>> orderBy)
		{
			var first = true;
			if (orderBy != null)
			{
				foreach (var kv in orderBy)
				{
					collection = ApplyOrderBy<T>(collection, kv.Key, kv.Value, first);
					first = false;
				}
			}
			return collection;
		}

		private static IQueryable<T> ApplyOrderBy<T>(IQueryable<T> collection, string path, bool ascending, bool first)
		{
			var props = path.Split('.');
			var type = typeof(T);

			var arg = Expression.Parameter(type, "x");
			Expression expr = arg;
			foreach (var prop in props)
			{
				// use reflection (not ComponentModel) to mirror LINQ
				var pi = type.GetProperty(prop);
				if (pi == null)
				{
					var msg = string.Format(CultureInfo.InvariantCulture, "Unknown property: {0} on type {1}", prop, type.FullName);
					if (prop != path)
						msg += " for path " + path;
					throw new ArgumentException(msg);
				}
				expr = Expression.Property(expr, pi);
				type = pi.PropertyType;
			}
			var delegateType = typeof(Func<,>).MakeGenericType(typeof(T), type);
			var lambda = Expression.Lambda(delegateType, expr, arg);
			string methodName = null;

			if (!first && collection is IOrderedQueryable<T>)
				methodName = ascending ? "ThenBy" : "ThenByDescending";
			else
				methodName = ascending ? "OrderBy" : "OrderByDescending";

			//TODO: apply caching to the generic methodsinfos?
			return (IOrderedQueryable<T>)typeof(Queryable).GetMethods().Single(
				method => method.Name == methodName
						&& method.IsGenericMethodDefinition
						&& method.GetGenericArguments().Length == 2
						&& method.GetParameters().Length == 2)
				.MakeGenericMethod(typeof(T), type)
				.Invoke(null, new object[] { collection, lambda });
		}
	}
}
