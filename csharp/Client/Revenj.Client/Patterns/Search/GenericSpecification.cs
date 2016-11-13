using System;
using System.Collections.Generic;
using System.IO;
using System.Linq.Expressions;
using System.Runtime.Serialization;
using System.Threading.Tasks;
using NGS.Serialization;

namespace Revenj.DomainPatterns
{
	[DataContract]
	public class GenericSpecification<T> : ISpecification<T>
		where T : class, ISearchable
	{
		[DataMember]
		private readonly Dictionary<string, KeyValuePair<int, MemoryStream>> Filters =
			new Dictionary<string, KeyValuePair<int, MemoryStream>>();

		private readonly ISearchableRepository<T> Repository;
		private readonly ProtobufSerialization Protobuf;
		private int? Limit;
		private int? Offset;
		private Dictionary<string, bool> Order = new Dictionary<string, bool>();

		internal GenericSpecification(
			ISearchableRepository<T> repository,
			ProtobufSerialization protobuf)
		{
			this.Repository = repository;
			this.Protobuf = protobuf;
		}

		internal GenericSpecification<T> Match<TArg>(string property, GenericSearchFilter filter, TArg value)
		{
			if (string.IsNullOrEmpty(property))
				throw new ArgumentException("property can't be empty");
			MemoryStream arg = value != null ? Protobuf.Serialize(value) : null;
			Filters.Add(property, new KeyValuePair<int, MemoryStream>((int)filter, arg));
			return this;
		}
		public GenericSpecification<T> Take(int limit)
		{
			this.Limit = limit;
			return this;
		}
		public GenericSpecification<T> Skip(int offset)
		{
			this.Offset = offset;
			return this;
		}
		internal GenericSpecification<T> OrderBy(string property, bool direction)
		{
			if (string.IsNullOrEmpty(property))
				throw new ArgumentException("property can't be empty");
			Order[property] = direction;
			return this;
		}
		public Task<T[]> Search()
		{
			return Repository.Search(this, Limit, Offset, Order);
		}

		public Expression<Func<T, bool>> IsSatisfied { get { return it => false; } }
	}

	public static partial class GenericSpecificationHelper
	{
		public static GenericSpecification<T> Specification<T>(this ISearchableRepository<T> repository)
			where T : class, ISearchable
		{
			if (repository == null)
				throw new ArgumentNullException("repository can't be null");
			return new GenericSpecification<T>(repository, Static.Locator.Resolve<ProtobufSerialization>());
		}

		private static string PropertyName(LambdaExpression lambda)
		{
			MemberExpression memberExpression;
			if (lambda.Body is UnaryExpression)
			{
				var unaryExpression = lambda.Body as UnaryExpression;
				memberExpression = unaryExpression.Operand as MemberExpression;
			}
			else
				memberExpression = lambda.Body as MemberExpression;
			var constantExpression = memberExpression.Expression as ConstantExpression;
			return memberExpression.Member.Name;
		}
		public static GenericSpecification<TSeachable> Ascending<TSeachable, TProperty>(
			this GenericSpecification<TSeachable> specification,
			Expression<Func<TSeachable, TProperty>> expression)
			where TSeachable : class, ISearchable
		{
			return specification.OrderBy(PropertyName(expression), true);
		}
		public static GenericSpecification<TSeachable> Descending<TSeachable, TProperty>(
			this GenericSpecification<TSeachable> specification,
			Expression<Func<TSeachable, TProperty>> expression)
			where TSeachable : class, ISearchable
		{
			return specification.OrderBy(PropertyName(expression), false);
		}

		public static GenericSpecification<TSeachable> Equal<TSeachable, TProperty>(
			this GenericSpecification<TSeachable> specification,
			Expression<Func<TSeachable, TProperty>> expression,
			TProperty value)
			where TSeachable : class, ISearchable
		{
			specification.Match(PropertyName(expression), GenericSearchFilter.Equals, value);
			return specification;
		}
		public static GenericSpecification<TSeachable> NotEqual<TSeachable, TProperty>(
			this GenericSpecification<TSeachable> specification,
			Expression<Func<TSeachable, TProperty>> expression,
			TProperty value)
			where TSeachable : class, ISearchable
		{
			specification.Match(PropertyName(expression), GenericSearchFilter.NotEquals, value);
			return specification;
		}
		public static GenericSpecification<TSeachable> LessThen<TSeachable, TProperty>(
			this GenericSpecification<TSeachable> specification,
			Expression<Func<TSeachable, TProperty>> expression,
			TProperty value)
			where TSeachable : class, ISearchable
		{
			specification.Match(PropertyName(expression), GenericSearchFilter.LessThen, value);
			return specification;
		}
		public static GenericSpecification<TSeachable> LessOrEqualThen<TSeachable, TProperty>(
			this GenericSpecification<TSeachable> specification,
			Expression<Func<TSeachable, TProperty>> expression,
			TProperty value)
			where TSeachable : class, ISearchable
		{
			specification.Match(PropertyName(expression), GenericSearchFilter.LessOrEqualThen, value);
			return specification;
		}
		public static GenericSpecification<TSeachable> MoreThen<TSeachable, TProperty>(
			this GenericSpecification<TSeachable> specification,
			Expression<Func<TSeachable, TProperty>> expression,
			TProperty value)
			where TSeachable : class, ISearchable
		{
			specification.Match(PropertyName(expression), GenericSearchFilter.MoreThen, value);
			return specification;
		}
		public static GenericSpecification<TSeachable> MoreOrEqualThen<TSeachable, TProperty>(
			this GenericSpecification<TSeachable> specification,
			Expression<Func<TSeachable, TProperty>> expression,
			TProperty value)
			where TSeachable : class, ISearchable
		{
			specification.Match(PropertyName(expression), GenericSearchFilter.MoreOrEqualThen, value);
			return specification;
		}
		public static GenericSpecification<TSeachable> Between<TSeachable, TProperty>(
			this GenericSpecification<TSeachable> specification,
			Expression<Func<TSeachable, TProperty>> expression,
			TProperty lower,
			TProperty upper)
			where TSeachable : class, ISearchable
		{
			specification.Match(PropertyName(expression), GenericSearchFilter.MoreOrEqualThen, lower);
			specification.Match(PropertyName(expression), GenericSearchFilter.LessOrEqualThen, upper);
			return specification;
		}
		public static GenericSpecification<TSeachable> StartsWith<TSeachable>(
			this GenericSpecification<TSeachable> specification,
			Expression<Func<TSeachable, string>> expression,
			string value,
			bool caseInsensitive = false)
			where TSeachable : class, ISearchable
		{
			specification.Match(
				PropertyName(expression),
				caseInsensitive ? GenericSearchFilter.StartsWithCaseInsensitiveValue : GenericSearchFilter.StartsWithValue,
				value);
			return specification;
		}
		public static GenericSpecification<TSeachable> NotStartsWith<TSeachable>(
			this GenericSpecification<TSeachable> specification,
			Expression<Func<TSeachable, string>> expression,
			string value,
			bool caseInsensitive = false)
			where TSeachable : class, ISearchable
		{
			specification.Match(
				PropertyName(expression),
				caseInsensitive ? GenericSearchFilter.NotStartsWithCaseInsensitiveValue : GenericSearchFilter.NotStartsWithValue,
				value);
			return specification;
		}
		public static GenericSpecification<TSeachable> StartsWith<TSeachable>(
			this GenericSpecification<TSeachable> specification,
			string value,
			Expression<Func<TSeachable, string>> expression,
			bool caseInsensitive = false)
			where TSeachable : class, ISearchable
		{
			specification.Match(
				PropertyName(expression),
				caseInsensitive ? GenericSearchFilter.ValueStartsWithCaseInsensitive : GenericSearchFilter.ValueStartsWith,
				value);
			return specification;
		}
		public static GenericSpecification<TSeachable> NotStartsWith<TSeachable>(
			this GenericSpecification<TSeachable> specification,
			string value,
			Expression<Func<TSeachable, string>> expression,
			bool caseInsensitive = false)
			where TSeachable : class, ISearchable
		{
			specification.Match(
				PropertyName(expression),
				caseInsensitive ? GenericSearchFilter.ValueNotStartsWithCaseInsensitive : GenericSearchFilter.ValueNotStartsWith,
				value);
			return specification;
		}
		public static GenericSpecification<TSeachable> In<TSeachable, TProperty>(
			this GenericSpecification<TSeachable> specification,
			Expression<Func<TSeachable, TProperty>> expression,
			IEnumerable<TProperty> value)
			where TSeachable : class, ISearchable
		{
			specification.Match(PropertyName(expression), GenericSearchFilter.InValue, value);
			return specification;
		}
		public static GenericSpecification<TSeachable> NotIn<TSeachable, TProperty>(
			this GenericSpecification<TSeachable> specification,
			Expression<Func<TSeachable, TProperty>> expression,
			IEnumerable<TProperty> value)
			where TSeachable : class, ISearchable
		{
			specification.Match(PropertyName(expression), GenericSearchFilter.NotInValue, value);
			return specification;
		}
		public static GenericSpecification<TSeachable> In<TSeachable, TProperty>(
			this GenericSpecification<TSeachable> specification,
			Expression<Func<TSeachable, IEnumerable<TProperty>>> expression,
			TProperty value)
			where TSeachable : class, ISearchable
		{
			specification.Match(PropertyName(expression), GenericSearchFilter.ValueIn, value);
			return specification;
		}
		public static GenericSpecification<TSeachable> NotIn<TSeachable, TProperty>(
			this GenericSpecification<TSeachable> specification,
			Expression<Func<TSeachable, IEnumerable<TProperty>>> expression,
			TProperty value)
			where TSeachable : class, ISearchable
		{
			specification.Match(PropertyName(expression), GenericSearchFilter.ValueNotIn, value);
			return specification;
		}
	}
}
