// Copyright (c) rubicon IT GmbH, www.rubicon.eu
//
// See the NOTICE file distributed with this work for additional information
// regarding copyright ownership.  rubicon licenses this file to you under 
// the Apache License, Version 2.0 (the "License"); you may not use this 
// file except in compliance with the License.  You may obtain a copy of the 
// License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the 
// License for the specific language governing permissions and limitations
// under the License.
// 
using System;
using System.Collections.Concurrent;
using System.Linq;
using System.Linq.Expressions;
using Remotion.Linq.Parsing.Structure;

namespace Remotion.Linq
{
	/// <summary>
	/// Represents a default implementation of <see cref="QueryProviderBase"/> that is automatically used by <see cref="QueryableBase{T}"/>
	/// unless a custom <see cref="IQueryProvider"/> is specified. The <see cref="DefaultQueryProvider"/> executes queries by parsing them into
	/// an instance of type <see cref="QueryModel"/>, which is then passed to an implementation of <see cref="IQueryExecutor"/> to obtain the
	/// result set.
	/// </summary>
	public sealed class DefaultQueryProvider : QueryProviderBase
	{
		private readonly Type _queryableType;

		/// <summary>
		/// Initializes a new instance of <see cref="DefaultQueryProvider"/> using a custom <see cref="IQueryParser"/>.
		/// </summary>
		/// <param name="queryableType">
		///   A type implementing <see cref="IQueryable{T}"/>. This type is used to construct the chain of query operators. Must be a generic type
		///   definition.
		/// </param>
		/// <param name="queryParser">The <see cref="IQueryParser"/> used to parse queries. Specify an instance of 
		///   <see cref="Parsing.Structure.QueryParser"/> for default behavior. See also <see cref="QueryParser.CreateDefault"/>.</param>
		/// <param name="executor">The <see cref="IQueryExecutor"/> used to execute queries against a specific query backend.</param>
		public DefaultQueryProvider(Type queryableType, IQueryParser queryParser, IQueryExecutor executor)
			: base(queryParser, executor)
		{
			CheckQueryableType(queryableType);

			_queryableType = queryableType;
		}

		private void CheckQueryableType(Type queryableType)
		{
			if (!queryableType.IsGenericTypeDefinition)
			{
				var message = string.Format(
					"Expected the generic type definition of an implementation of IQueryable<T>, but was '{0}'.",
					queryableType);
				throw new ArgumentException(message, "queryableType");
			}
			var genericArgumentCount = queryableType.GetGenericArguments().Length;
			if (genericArgumentCount != 1)
			{
				var message = string.Format(
					"Expected the generic type definition of an implementation of IQueryable<T> with exactly one type argument, but found {0} arguments on '{1}.",
					genericArgumentCount,
					queryableType);
				throw new ArgumentException(message, "queryableType");
			}
		}

		/// <summary>
		/// Gets the type of queryable created by this provider. This is the generic type definition of an implementation of <see cref="IQueryable{T}"/>
		/// (usually a subclass of <see cref="QueryableBase{T}"/>) with exactly one type argument.
		/// </summary>
		public Type QueryableType
		{
			get { return _queryableType; }
		}

		struct Pair : IEquatable<Pair>
		{
			private readonly Type Queryable;
			private readonly Type Target;

			public Pair(Type qt, Type t)
			{
				this.Queryable = qt;
				this.Target = t;
			}

			public override int GetHashCode() { return Target.GetHashCode(); }
			public override bool Equals(object obj) { return Equals((Pair)obj); }
			public bool Equals(Pair other)
			{
				return other.Queryable == Queryable && other.Target == Target;
			}
		}

		private static readonly ConcurrentDictionary<Pair, object> QueryFactoryCache = new ConcurrentDictionary<Pair, object>(1, 117);

		/// <summary>
		/// Creates a new <see cref="IQueryable"/> (of type <see cref="QueryableType"/> with <typeparamref name="T"/> as its generic argument) that
		/// represents the query defined by <paramref name="expression"/> and is able to enumerate its results.
		/// </summary>
		/// <typeparam name="T">The type of the data items returned by the query.</typeparam>
		/// <param name="expression">An expression representing the query for which a <see cref="IQueryable{T}"/> should be created.</param>
		/// <returns>An <see cref="IQueryable{T}"/> that represents the query defined by <paramref name="expression"/>.</returns>
		public override IQueryable<T> CreateQuery<T>(Expression expression)
		{
			var pair = new Pair(_queryableType, typeof(T));
			object result;
			if (QueryFactoryCache.TryGetValue(pair, out result))
				return (result as Func<DefaultQueryProvider, Expression, IQueryable<T>>)(this, expression);

			var target = QueryableType.MakeGenericType(typeof(T));
			var param1 = Expression.Parameter(typeof(DefaultQueryProvider));
			var param2 = Expression.Parameter(typeof(Expression));
			var ctor = target.GetConstructor(new Type[] { typeof(DefaultQueryProvider), typeof(Expression) });
			var newExp = Expression.New(ctor, param1, param2);
			var lambda = Expression.Lambda<Func<DefaultQueryProvider, Expression, IQueryable<T>>>(newExp, param1, param2);
			var func = lambda.Compile();
			QueryFactoryCache.TryAdd(pair, func);
			return func(this, expression);
		}
	}
}
