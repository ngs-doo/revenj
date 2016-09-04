using System;
using System.Collections.Generic;
using System.Data;
using System.IO;
using System.Linq;
using System.Text;
using Revenj.DomainPatterns;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres
{
	public interface IBulkRepository<T>
	{
		Func<IDataReader, int, T> Find(IBulkReadQuery query, string uri);
		Func<IDataReader, int, T[]> Find(IBulkReadQuery query, IEnumerable<string> uris);
		Func<IDataReader, int, T[]> Search(IBulkReadQuery query, ISpecification<T> filter, int? limit, int? offset);
		Func<IDataReader, int, long> Count(IBulkReadQuery query, ISpecification<T> filter);
		Func<IDataReader, int, bool> Exists(IBulkReadQuery query, ISpecification<T> filter);
	}

	public interface IBulkReadQuery
	{
		ChunkedMemoryStream Stream { get; }
		TextWriter Writer { get; }
		bool UsePrepared { get; }
		int Index { get; }
		string GetNextArgument(Action<TextWriter> writeValue, string type);
	}

	public static class BulkReaderHelper
	{
		public static IRepositoryBulkReader BulkRead(this IServiceProvider locator, ChunkedMemoryStream stream)
		{
			var query = locator.Resolve<IDatabaseQuery>();
			return new BulkReader(locator, query, stream);
		}

		private static readonly string[] ArgumentsCache = new string[32];
		private static int PreparedCount = 100000000;

		static BulkReaderHelper()
		{
			for (int i = 0; i < ArgumentsCache.Length; i++)
				ArgumentsCache[i] = "$" + (i + 1);
		}

		class BulkReader : IRepositoryBulkReader, IBulkReadQuery, IDisposable
		{
			private readonly IServiceProvider Locator;
			private readonly IDatabaseQuery Query;
			public ChunkedMemoryStream Stream { get; private set; }
			public TextWriter Writer { get; private set; }
			public int Index { get; private set; }
			private int Arguments;
			private readonly List<string> Types = new List<string>();
			private readonly List<Action<TextWriter>> WriteValues = new List<Action<TextWriter>>();
			private readonly List<Func<IDataReader, int, object>> ResultActions = new List<Func<IDataReader, int, object>>();
			private object[] Results;
			private object LastRepository;
			private object LastCube;
			private readonly Dictionary<Type, object> Repositories = new Dictionary<Type, object>();
			private readonly Dictionary<Type, object> Cubes = new Dictionary<Type, object>();
			private readonly Dictionary<string, PreparedCommand> PreparedStatements = new Dictionary<string, PreparedCommand>();
			public bool UsePrepared { get; private set; }
			private readonly StringBuilder ActionName = new StringBuilder(512);
			public string GetNextArgument(Action<TextWriter> writeValue, string type)
			{
				WriteValues.Add(writeValue);
				if (type != null)
					Types.Add(type);
				Arguments++;
				if (Arguments < ArgumentsCache.Length)
					return ArgumentsCache[Arguments - 1];
				return "$" + Arguments;
			}

			class PreparedCommand
			{
				public readonly string Name;
				public readonly string Query;
				public readonly string Types;
				public PreparedCommand(string name, string query, string types)
				{
					this.Name = name;
					this.Query = query;
					this.Types = types;
				}
			}
			internal BulkReader(IServiceProvider locator, IDatabaseQuery query, ChunkedMemoryStream stream)
			{
				this.Locator = locator;
				this.Query = query;
				this.Stream = stream;
				this.Writer = stream.GetWriter();
				Stream.Reset();
				Writer.Write("SELECT (");
			}

			public void Reset(bool usePrepared)
			{
				Writer.Flush();
				Stream.Reset();
				WriteValues.Clear();
				Types.Clear();
				Results = null;
				Index = 0;
				Arguments = 0;
				ActionName.Clear();
				ResultActions.Clear();
				this.UsePrepared = usePrepared;
				Writer.Write("SELECT (");
			}

			private Lazy<T> Add<T>(Func<IDataReader, int, T> reader)
			{
				Writer.Write("),(");
				ResultActions.Add((dr, ind) => reader(dr, ind));
				int i = Index;
				Index++;
				return new Lazy<T>(() =>
				{
					if (Results == null)
						Execute();
					return (T)Results[i];
				});
			}

			public void Execute()
			{
				Writer.Flush();
				Stream.Position = 0;
				Stream.SetLength(Stream.Length - 2);
				IDbCommand com;
				if (UsePrepared)
				{
					var name = ActionName.ToString();
					PreparedCommand preparedCommand;
					if (!PreparedStatements.TryGetValue(name, out preparedCommand))
					{
						var preparedName = "LR-" + PreparedCount++;
						var rdr = Stream.GetReader();
						preparedCommand = new PreparedCommand(preparedName, rdr.ReadToEnd(), string.Join(",", Types));
						PreparedStatements[name] = preparedCommand;
					}
					Stream.Reset();
					Writer.Write("EXECUTE \"");
					Writer.Write(preparedCommand.Name);
					Writer.Write('"');
					if (preparedCommand.Types.Length > 0)
					{
						Writer.Write(" (");
						if (WriteValues.Count > 0)
							WriteValues[0](Writer);
						for (int i = 1; i < WriteValues.Count; i++)
						{
							Writer.Write(',');
							WriteValues[i](Writer);
						}
						Writer.Write(")");
					}
					Writer.Flush();
					Stream.Position = 0;
					com = PostgresCommandFactory.PreparedCommand(Stream, preparedCommand.Name, preparedCommand.Query, preparedCommand.Types);
				}
				else com = PostgresCommandFactory.NewCommand(Stream);
				Results = new object[ResultActions.Count];
				Query.Execute(
					com,
					dr =>
					{
						for (int i = 0; i < ResultActions.Count; i++)
							Results[i] = ResultActions[i](dr, i);
					});
			}

			private IBulkRepository<T> GetRepository<T>()
			{
				if (LastRepository is IBulkRepository<T>)
					return (IBulkRepository<T>)LastRepository;
				if (Repositories.TryGetValue(typeof(T), out LastRepository))
					return (IBulkRepository<T>)LastRepository;
				try
				{
					var repository = Locator.Resolve<IBulkRepository<T>>();
					LastRepository = repository;
					Repositories[typeof(T)] = repository;
					return repository;
				}
				catch (Exception ex)
				{
					throw new ArgumentException("Specified type: " + typeof(T).FullName + " doesn't support bulk reading.", ex);
				}
			}

			private PostgresOlapCubeQuery<TFilter> GetCube<TCube, TFilter>()
				where TFilter : IDataSource
			{
				if (LastCube is TCube)
					return (PostgresOlapCubeQuery<TFilter>)LastCube;
				if (Cubes.TryGetValue(typeof(TCube), out LastCube))
					return (PostgresOlapCubeQuery<TFilter>)LastCube;
				try
				{
					var cube = Locator.Resolve<TCube>();
					LastCube = cube;
					Cubes[typeof(TCube)] = cube;
					return cube as PostgresOlapCubeQuery<TFilter>;
				}
				catch (Exception ex)
				{
					throw new ArgumentException("Specified type: " + typeof(TCube).FullName + " doesn't support OLAP cube.", ex);
				}
			}

			public Lazy<T> Find<T>(string uri) where T : IIdentifiable
			{
				if (UsePrepared)
					ActionName.Append(":F1:").Append(typeof(T).FullName);
				return Add(GetRepository<T>().Find(this, uri));
			}

			public Lazy<T[]> Find<T>(IEnumerable<string> uris) where T : IIdentifiable
			{
				if (UsePrepared)
					ActionName.Append(":FM:").Append(typeof(T).FullName);
				return Add(GetRepository<T>().Find(this, uris));
			}

			public Lazy<T[]> Search<T>(ISpecification<T> filter, int? limit, int? offset) where T : IDataSource
			{
				if (UsePrepared)
				{
					ActionName.Append(":S:").Append(filter == null ? typeof(T).FullName : filter.GetType().FullName).Append(":");
					if (limit != null)
						ActionName.Append(limit.Value);
					ActionName.Append(":");
					if (offset != null)
						ActionName.Append(offset.Value);
				}
				return Add(GetRepository<T>().Search(this, filter, limit, offset));
			}

			public Lazy<long> Count<T>(ISpecification<T> filter) where T : IDataSource
			{
				if (UsePrepared)
					ActionName.Append(":C:").Append(filter == null ? typeof(T).FullName : filter.GetType().FullName);
				return Add(GetRepository<T>().Count(this, filter));
			}

			public Lazy<bool> Exists<T>(ISpecification<T> filter) where T : IDataSource
			{
				if (UsePrepared)
					ActionName.Append(":E:").Append(filter == null ? typeof(T).FullName : filter.GetType().FullName);
				return Add(GetRepository<T>().Exists(this, filter));
			}

			public Lazy<DataTable> Analyze<TCube, TFilter>(
				IEnumerable<string> dimensionsAndFacts,
				IEnumerable<KeyValuePair<string, bool>> order,
				ISpecification<TFilter> filter,
				int? limit,
				int? offset)
				where TCube : IOlapCubeQuery<TFilter>
				where TFilter : IDataSource
			{
				if (UsePrepared)
				{
					ActionName.Append(":A");
				}
				Writer.Write("SELECT array_agg(_x) FROM (");
				var cube = GetCube<TCube, TFilter>();
				var dimensions = cube.Dimensions.Intersect(dimensionsAndFacts).ToList();
				var facts = cube.Facts.Intersect(dimensionsAndFacts).ToList();
				Writer.Write(cube.PrepareSql(dimensions, facts, order, filter, limit, offset));
				Writer.Write(") _x),(");
				var table = new DataTable { CaseSensitive = true };
				var converters = cube.PrepareConverters(dimensions, facts, table);
				ResultActions.Add((dr, ind) =>
				{
					var obj = dr.GetValue(ind);
					var tr = obj as TextReader;
					var btr = tr != null ? Stream.UseBufferedReader(tr) : Stream.UseBufferedReader(obj.ToString());
					PostgresTypedArray.ParseCollection(btr, 0, Locator, (rdr, _, __, ___) =>
					{
						rdr.Read(3);
						var args = new object[converters.Length];
						for (int x = 0; x < converters.Length; x++)
							args[x] = converters[x](rdr, 1);
						rdr.Read(3);
						return table.Rows.Add(args);
					});
					if (tr != null) tr.Dispose();
					return table;
				});
				int i = Index;
				Index++;
				return new Lazy<DataTable>(() =>
				{
					if (Results == null)
						Execute();
					return table;
				});
			}

			public void Dispose()
			{
				Stream.Close();
			}
		}
	}
}
