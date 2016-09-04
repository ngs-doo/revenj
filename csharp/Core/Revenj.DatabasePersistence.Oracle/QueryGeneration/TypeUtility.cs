using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using Remotion.Linq;
using Remotion.Linq.Clauses.ResultOperators;
using Revenj.DatabasePersistence.Oracle.QueryGeneration;
using Revenj.DatabasePersistence.Oracle.QueryGeneration.QueryComposition;
using Revenj.DomainPatterns;

namespace Revenj.DatabasePersistence.Oracle
{
	public static class TypeUtility
	{
		public static string AddParameter(this QueryParts query, Type type, object value, bool canUseParams)
		{
			if (canUseParams)
			{
				var paramConv = query.ConverterFactory.GetParameterFactory(type);
				if (paramConv != null)
				{
					var param = paramConv(value);
					if (param != null)
					{
						query.Parameters.Add(param);
						return param.ParameterName;
					}
				}
			}
			if (value == null)
				return "NULL";
			var strConv = query.ConverterFactory.GetStringFactory(type);
			if (strConv == null && type.IsNullable()) query.ConverterFactory.GetStringFactory(type.GetGenericArguments()[0]);
			if (strConv != null)
			{
				var param = strConv(value);
				if (param != null)
					return param;
			}
			throw new NotImplementedException("Unsupported parameter type:" + type.FullName + " .Please provide info about missing feature.");
		}

		public static string AddParameter(this QueryParts query, Type element, object[] value, bool canUseParams)
		{
			if (canUseParams)
			{
				var paramConv = query.ConverterFactory.GetVarrayParameterFactory(element);
				if (paramConv != null)
				{
					var param = paramConv(value);
					if (param != null)
					{
						query.Parameters.Add(param);
						return param.ParameterName;
					}
				}
			}
			if (value == null)
				return "NULL";
			var strConv = query.ConverterFactory.GetVarrayStringFactory(element);
			if (strConv == null && element.IsNullable()) query.ConverterFactory.GetStringFactory(element.GetGenericArguments()[0]);
			if (strConv != null)
			{
				var param = strConv(value);
				if (param != null)
					return param;
			}
			throw new NotImplementedException("Unsupported parameter type:" + element.FullName + ". Please provide info about missing feature");
		}

		public static bool CanUseMain(this QueryModel queryModel)
		{
			return queryModel.IsIdentityQuery()
				&& (queryModel.ResultOperators.Count == 0
					|| queryModel.ResultOperators.Count == 1 && queryModel.ResultOperators[0] is DefaultIfEmptyResultOperator);
		}

		public static bool AsValue(this Type type)
		{
			return typeof(IEntity).IsAssignableFrom(type) || typeof(IIdentifiable).IsAssignableFrom(type);
		}

		public static bool IsNullable(this Type type)
		{
			return type.IsGenericType && type.GetGenericTypeDefinition() == typeof(Nullable<>);
		}

		public static bool IsGrouping(this Type type)
		{
			return type.IsGenericType && type.GetGenericTypeDefinition() == typeof(IGrouping<,>);
		}

		public static bool IsQueryable(this Type type)
		{
			return type.IsGenericType && type.GetGenericTypeDefinition() == typeof(Queryable<>);
		}

		class Grouping<TK, TE> : IGrouping<TK, TE>
		{
			public TK Key { get; private set; }
			private readonly List<TE> Values;

			public Grouping(TK tk, IEnumerable te)
			{
				this.Key = tk;
				this.Values = te.Cast<TE>().ToList();
			}

			IEnumerator IEnumerable.GetEnumerator() { return GetEnumerator(); }
			public IEnumerator<TE> GetEnumerator()
			{
				foreach (var item in Values)
					yield return item;
			}
		}

		public static Type CreateGrouping(this Type type)
		{
			return typeof(Grouping<,>).MakeGenericType(type.GetGenericArguments());
		}
	}
}
