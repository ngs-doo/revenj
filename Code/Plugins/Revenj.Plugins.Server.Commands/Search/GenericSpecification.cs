using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Linq.Expressions;
using System.Reflection;
using System.Runtime.Serialization;
using NGS.DomainPatterns;
using NGS.Serialization;

namespace Revenj.Plugins.Server.Commands
{
	[DataContract]
	public class GenericSpecification<T, TFormat> : ISpecification<T>
		where T : class, IIdentifiable
	{
		[DataMember]
		private readonly Dictionary<string, List<KeyValuePair<int, TFormat>>> Filters;

		private readonly ISerialization<TFormat> Serializer;

		public GenericSpecification(ISerialization<TFormat> serializer, Dictionary<string, List<KeyValuePair<int, TFormat>>> filters)
		{
			this.Serializer = serializer;
			this.Filters = filters ?? new Dictionary<string, List<KeyValuePair<int, TFormat>>>();
			specificationFilter = BuildFilter();
		}

		public GenericSpecification<T, TFormat> Match<TArg>(string property, GenericSearchFilter filter, TArg value)
		{
			if (string.IsNullOrEmpty(property))
				throw new ArgumentException("property can't be empty");
			specificationFilter = null;
			List<KeyValuePair<int, TFormat>> list;
			if (!Filters.TryGetValue(property, out list))
				Filters[property] = list = new List<KeyValuePair<int, TFormat>>();
			list.Add(new KeyValuePair<int, TFormat>((int)filter, Serializer.Serialize(value)));
			return this;
		}

		private Expression<Func<T, bool>> BuildFilter()
		{
			var arg = Expression.Parameter(typeof(T), "it");
			List<Expression> conditions = new List<Expression>();
			foreach (var f in Filters)
				foreach (var kv in f.Value)
					conditions.Add(Filter(arg, f.Key, (GenericSearchFilter)kv.Key, kv.Value));
			if (conditions.Count == 0)
				return it => true;
			if (conditions.Count == 1)
				return Expression.Lambda<Func<T, bool>>(conditions[0], arg);
			var and = Expression.AndAlso(conditions[0], conditions[1]);
			for (int i = 2; i < conditions.Count; i++)
				and = Expression.AndAlso(and, conditions[i]);
			return Expression.Lambda<Func<T, bool>>(and, arg);
		}

		private object Deserialize(Type type, TFormat value)
		{
			if (value == null)
				return null;
			return DeserializationMethod.MakeGenericMethod(type).Invoke(Serializer, new object[] { value, default(StreamingContext) });
		}

		private static MethodInfo DeserializationMethod = typeof(ISerialization<TFormat>).GetMethod("Deserialize");

		private Expression Filter(
			ParameterExpression arg,
			string path,
			GenericSearchFilter filter,
			TFormat value)
		{
			var props = path.Split('.');
			var type = typeof(T);

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
			}
			switch (filter)
			{
				case GenericSearchFilter.Equals:
					return Expression.Equal(expr, Expression.Constant(Deserialize(expr.Type, value)));
				case GenericSearchFilter.NotEquals:
					return Expression.NotEqual(expr, Expression.Constant(Deserialize(expr.Type, value)));
				case GenericSearchFilter.LessThen:
					return Expression.LessThan(expr, Expression.Constant(Deserialize(expr.Type, value)));
				case GenericSearchFilter.LessOrEqualThen:
					return Expression.LessThanOrEqual(expr, Expression.Constant(Deserialize(expr.Type, value)));
				case GenericSearchFilter.GreaterThen:
					return Expression.GreaterThan(expr, Expression.Constant(Deserialize(expr.Type, value)));
				case GenericSearchFilter.GreaterThenOrEqual:
					return Expression.GreaterThanOrEqual(expr, Expression.Constant(Deserialize(expr.Type, value)));
				case GenericSearchFilter.InValue:
					return Expression.Call(
						ContainsMethod.MakeGenericMethod(expr.Type),
						Expression.Constant(Deserialize(expr.Type.MakeArrayType(), value)),
						expr);
				case GenericSearchFilter.NotInValue:
					return Expression.Not(
							Expression.Call(ContainsMethod.MakeGenericMethod(expr.Type),
							Expression.Constant(Deserialize(expr.Type, value)),
							expr));
				case GenericSearchFilter.ValueIn:
					return Expression.Call(
						ContainsMethod.MakeGenericMethod(expr.Type),
						expr,
						Expression.Constant(Deserialize(expr.Type.GetElementType(), value)));
				case GenericSearchFilter.ValueNotIn:
					return Expression.Not(
							Expression.Call(
								ContainsMethod.MakeGenericMethod(expr.Type),
								expr,
								Expression.Constant(Deserialize(expr.Type.GetElementType(), value))));
				case GenericSearchFilter.StartsWithValue:
					return Expression.Call(expr, StringStartsWith, Expression.Constant(Deserialize(expr.Type, value)));
				case GenericSearchFilter.StartsWithCaseInsensitiveValue:
					return Expression.Call(
							expr,
							StringStartsWithCaseInsensitive,
							Expression.Constant(Deserialize(expr.Type, value)),
							Expression.Constant(StringComparison.InvariantCultureIgnoreCase));
				case GenericSearchFilter.NotStartsWithValue:
					return Expression.Not(
							Expression.Call(
								expr,
								StringStartsWith,
								Expression.Constant(Deserialize(expr.Type, value))));
				case GenericSearchFilter.NotStartsWithCaseInsensitiveValue:
					return Expression.Not(
							Expression.Call(
								expr,
								StringStartsWithCaseInsensitive,
								Expression.Constant(Deserialize(expr.Type, value)),
								Expression.Constant(StringComparison.InvariantCultureIgnoreCase)));
				case GenericSearchFilter.ValueStartsWith:
					return Expression.Call(
							Expression.Constant(Deserialize(expr.Type, value)),
							StringStartsWith,
							expr);
				case GenericSearchFilter.ValueStartsWithCaseInsensitive:
					return Expression.Call(
							Expression.Constant(Deserialize(expr.Type, value)),
							StringStartsWithCaseInsensitive,
							expr,
							Expression.Constant(StringComparison.InvariantCultureIgnoreCase));
				case GenericSearchFilter.NotValueStartsWith:
					return Expression.Not(
							Expression.Call(
								Expression.Constant(Deserialize(expr.Type, value)),
								StringStartsWith,
								expr));
				case GenericSearchFilter.NotValueStartsWithCaseInsensitive:
					return Expression.Not(
							Expression.Call(
								Expression.Constant(Deserialize(expr.Type, value)),
								StringStartsWithCaseInsensitive,
								expr,
								Expression.Constant(StringComparison.InvariantCultureIgnoreCase)));
				default:
					throw new ArgumentException("Unknown filter");
			}
		}

		private static MethodInfo ContainsMethod = ((Func<IEnumerable<object>, object, bool>)Enumerable.Contains<object>).Method.GetGenericMethodDefinition();
		private static MethodInfo StringStartsWith = typeof(string).GetMethod("StartsWith", new[] { typeof(string) });
		private static MethodInfo StringStartsWithCaseInsensitive = typeof(string).GetMethod("StartsWith", new[] { typeof(string), typeof(StringComparison) });

		private Expression<Func<T, bool>> specificationFilter;

		public Expression<Func<T, bool>> IsSatisfied
		{
			get
			{
				if (specificationFilter == null)
					specificationFilter = BuildFilter();
				return specificationFilter;
			}
		}
	}
}
