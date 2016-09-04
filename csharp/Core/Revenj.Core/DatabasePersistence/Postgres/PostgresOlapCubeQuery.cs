using System;
using System.Collections.Generic;
using System.Data;
using System.Diagnostics.Contracts;
using System.IO;
using System.Linq;
using System.Text;
using Revenj.DatabasePersistence.Postgres.QueryGeneration.QueryComposition;
using Revenj.DomainPatterns;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres
{
	public abstract class PostgresOlapCubeQuery<TSource> : IOlapCubeQuery<TSource>
		where TSource : IDataSource
	{
		private readonly IServiceProvider Locator;
		private readonly IDatabaseQuery DatabaseQuery;

		protected abstract string Source { get; }
		protected readonly Dictionary<string, Func<string, string>> CubeDimensions = new Dictionary<string, Func<string, string>>();
		protected readonly Dictionary<string, Func<string, string>> CubeFacts = new Dictionary<string, Func<string, string>>();
		protected readonly Dictionary<string, Func<BufferedTextReader, int, object>> CubeConverters = new Dictionary<string, Func<BufferedTextReader, int, object>>();
		protected readonly Dictionary<string, Type> CubeTypes = new Dictionary<string, Type>();

		protected PostgresOlapCubeQuery(IServiceProvider locator)
		{
			Contract.Requires(locator != null);

			this.Locator = locator;
			this.DatabaseQuery = Locator.Resolve<IDatabaseQuery>();
		}

		public IEnumerable<string> Dimensions { get { return CubeDimensions.Keys; } }
		public IEnumerable<string> Facts { get { return CubeFacts.Keys; } }

		private void ValidateInput(List<string> usedDimensions, List<string> usedFacts, IEnumerable<string> customOrder)
		{
			if (usedDimensions.Count == 0 && usedFacts.Count == 0)
				throw new ArgumentException("Cube must have at least one dimension or fact.");

			foreach (var d in usedDimensions)
				if (!CubeDimensions.ContainsKey(d))
					throw new ArgumentException("Unknown dimension: {0}. Use Dimensions property for available dimensions".With(d));

			foreach (var f in usedFacts)
				if (!CubeFacts.ContainsKey(f))
					throw new ArgumentException("Unknown fact: {0}. Use Facts property for available facts".With(f));

			foreach (var o in customOrder)
				if (!usedDimensions.Contains(o) && !usedFacts.Contains(o))
					throw new ArgumentException("Invalid order: {0}. Order can be only field from used dimensions and facts.".With(o));
		}

		public DataTable Analyze(
			IEnumerable<string> dimensions,
			IEnumerable<string> facts,
			IEnumerable<KeyValuePair<string, bool>> order,
			ISpecification<TSource> filter,
			int? limit,
			int? offset)
		{
			var usedDimensions = new List<string>();
			var usedFacts = new List<string>();
			if (dimensions != null)
				usedDimensions.AddRange(dimensions);
			if (facts != null)
				usedFacts.AddRange(facts);
			var sql = PrepareSql(usedDimensions, usedFacts, order, filter, limit, offset);
			var table = new DataTable { CaseSensitive = true };
			var converters = PrepareConverters(usedDimensions, usedFacts, table);
			using (var cms = ChunkedMemoryStream.Create())
			{
				DatabaseQuery.Execute(sql, dr =>
				{
					var obj = dr.GetValue(0);
					var tr = obj as TextReader;
					var btr = tr != null ? cms.UseBufferedReader(tr) : cms.UseBufferedReader(obj.ToString());
					btr.Read();
					var args = new object[converters.Length];
					for (int i = 0; i < converters.Length; i++)
						args[i] = converters[i](btr, 1);
					table.Rows.Add(args);
					if (tr != null) tr.Dispose();
				});
			}
			return table;
		}

		public Func<BufferedTextReader, int, object>[] PrepareConverters(
			List<string> usedDimensions,
			List<string> usedFacts,
			DataTable table)
		{
			var converters = new Func<BufferedTextReader, int, object>[usedDimensions.Count + usedFacts.Count];
			foreach (var d in usedDimensions)
			{
				converters[table.Columns.Count] = CubeConverters[d];
				table.Columns.Add(d, CubeTypes[d]);
			}
			foreach (var f in usedFacts)
			{
				converters[table.Columns.Count] = CubeConverters[f];
				table.Columns.Add(f, CubeTypes[f]);
			}
			return converters;
		}

		internal string PrepareSql(
			List<string> usedDimensions,
			List<string> usedFacts,
			IEnumerable<KeyValuePair<string, bool>> order,
			ISpecification<TSource> filter,
			int? limit,
			int? offset)
		{
			var customOrder = new List<KeyValuePair<string, bool>>();

			if (order != null)
				foreach (var o in order)
					if (o.Key != null)
						customOrder.Add(new KeyValuePair<string, bool>(o.Key, o.Value));

			ValidateInput(usedDimensions, usedFacts, customOrder.Select(it => it.Key));

			var sb = new StringBuilder();
			var alias = filter != null ? filter.IsSatisfied.Parameters.First().Name : "it";
			sb.Append("SELECT ROW(");
			foreach (var d in usedDimensions)
			{
				sb.Append(CubeDimensions[d](alias));
				sb.Append(',');
			}
			foreach (var f in usedFacts)
			{
				sb.Append(CubeFacts[f](alias));
				sb.Append(',');
			}
			sb.Length--;
			sb.AppendFormat(") FROM {0} \"{1}\" ", Source, alias);
			if (filter != null)
				MainQueryParts.AddFilter(Locator, DatabaseQuery, filter, sb);
			if (usedDimensions.Count > 0)
			{
				sb.Append(" GROUP BY ");
				foreach (var d in usedDimensions)
				{
					sb.Append(CubeDimensions[d](alias));
					sb.Append(',');
				}
				sb.Length--;
			}
			if (customOrder.Count > 0)
			{
				sb.Append(" ORDER BY ");
				foreach (var kv in customOrder)
				{
					if (CubeDimensions.ContainsKey(kv.Key))
						sb.Append(CubeDimensions[kv.Key](alias));
					else if (CubeFacts.ContainsKey(kv.Key))
						sb.Append(CubeFacts[kv.Key](alias));
					else
						sb.AppendFormat("\"{0}\"", kv.Key);
					if (kv.Value == false)
						sb.Append(" DESC");
					sb.Append(", ");
				}
				sb.Length -= 2;
			}
			if (limit != null)
				sb.Append(" LIMIT ").Append(limit.Value);
			if (offset != null)
				sb.Append(" OFFSET ").Append(offset.Value);
			return sb.ToString();
		}
	}
}
