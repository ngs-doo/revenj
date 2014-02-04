using System;
using System.ComponentModel;
using System.Linq.Expressions;
using System.Reactive.Linq;

namespace NGS.Utility
{
	/// <summary>
	/// Utilities for working with reflection
	/// </summary>
	public static class StrongReflection
	{
		/// <summary>
		/// Get property name from lambda expression.
		/// </summary>
		/// <param name="lambda">lambda expression</param>
		/// <returns>property name</returns>
		public static string PropertyName(LambdaExpression lambda)
		{
			MemberExpression memberExpression;
			if (lambda.Body is UnaryExpression)
			{
				var unaryExpression = lambda.Body as UnaryExpression;
				memberExpression = unaryExpression.Operand as MemberExpression;
			}
			else
				memberExpression = lambda.Body as MemberExpression;
			if (memberExpression == null)
				throw new ArgumentException("Invalid property name");
			return memberExpression.Member.Name;
		}
		/// <summary>
		/// Get property name from expression.
		/// </summary>
		/// <typeparam name="T">property type</typeparam>
		/// <param name="property">expression</param>
		/// <returns>property name</returns>
		public static string PropertyName<T>(Expression<Func<T>> property)
		{
			return PropertyName(property as LambdaExpression);
		}
		/// <summary>
		/// Get property name from expression.
		/// </summary>
		/// <typeparam name="TSource">object type</typeparam>
		/// <typeparam name="TResult">property type</typeparam>
		/// <param name="property">expression</param>
		/// <returns>property name</returns>
		public static string PropertyName<TSource, TResult>(Expression<Func<TSource, TResult>> property)
		{
			return PropertyName(property as LambdaExpression);
		}
		/// <summary>
		/// Raise PropertyChangedEventHandler for specified property.
		/// </summary>
		/// <typeparam name="T">property type</typeparam>
		/// <param name="handler">property changed event handler</param>
		/// <param name="property">expression</param>
		public static void Notify<T>(this PropertyChangedEventHandler handler, Expression<Func<T>> property)
		{
			if (handler == null)
				return;

			var lambda = property as LambdaExpression;
			MemberExpression memberExpression;
			if (lambda.Body is UnaryExpression)
			{
				var unaryExpression = lambda.Body as UnaryExpression;
				memberExpression = unaryExpression.Operand as MemberExpression;
			}
			else
				memberExpression = lambda.Body as MemberExpression;
			var constantExpression = memberExpression.Expression as ConstantExpression;
			handler(constantExpression.Value, new PropertyChangedEventArgs(memberExpression.Member.Name));
		}
		/// <summary>
		/// Observe PropertyChanged events.
		/// </summary>
		/// <typeparam name="TSource">notify property changed object type</typeparam>
		/// <typeparam name="TResult">property type</typeparam>
		/// <param name="notifySource">notify property changed object</param>
		/// <param name="property">expression</param>
		/// <returns>observable events</returns>
		public static IObservable<TResult> Observe<TSource, TResult>(this TSource notifySource, Expression<Func<TSource, TResult>> property)
			where TSource : INotifyPropertyChanged
		{
			var propertyName = StrongReflection.PropertyName(property);
			var method = property.Compile();
			return
				Observable.FromEventPattern<PropertyChangedEventArgs>(notifySource, "PropertyChanged")
					.Where(it => it.EventArgs.PropertyName == propertyName)
					.Select(it => method(notifySource));
		}
	}
}
