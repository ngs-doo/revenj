using System;
using System.Collections.Generic;
using System.Data;
using System.IO;
using Revenj.DomainPatterns;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres
{
	public interface IBulkRepository<T>
	{
		Func<IDataReader, int, T> Find(ChunkedMemoryStream stream, string uri);
		Func<IDataReader, int, T[]> Find(ChunkedMemoryStream stream, IEnumerable<string> uris);
		Func<IDataReader, int, T[]> Search(ChunkedMemoryStream stream, ISpecification<T> filter, int? limit, int? offset);
		Func<IDataReader, int, long> Count(ChunkedMemoryStream stream, ISpecification<T> filter);
		Func<IDataReader, int, bool> Exists(ChunkedMemoryStream stream, ISpecification<T> filter);
	}

	public static class BulkReaderHelper
	{
		public static IRepositoryBulkReader BulkRead(this IServiceProvider locator, ChunkedMemoryStream stream)
		{
			var query = locator.Resolve<IDatabaseQuery>();
			return new BulkReader(locator, query, stream);
		}

		class BulkReader : IRepositoryBulkReader
		{
			private readonly IServiceProvider Locator;
			private readonly IDatabaseQuery Query;
			private readonly ChunkedMemoryStream Stream;
			private readonly TextWriter Writer;
			private int Index;
			private readonly List<Func<IDataReader, int, object>> Actions = new List<Func<IDataReader, int, object>>();
			private object[] Results;
			private object LastRepository;
			private readonly Dictionary<Type, object> Repositories = new Dictionary<Type, object>();

			internal BulkReader(IServiceProvider locator, IDatabaseQuery query, ChunkedMemoryStream stream)
			{
				this.Locator = locator;
				this.Query = query;
				this.Stream = stream;
				this.Writer = stream.GetWriter();
				Stream.Reset();
				Writer.Write("SELECT (");
			}

			public void Reset()
			{
				Writer.Flush();
				Stream.Reset();
				Writer.Write("SELECT (");
				Results = null;
				Index = 0;
				Actions.Clear();
			}

			private Lazy<T> Add<T>(Func<IDataReader, int, T> reader)
			{
				Writer.Write("),(");
				int i = Index;
				Actions.Add((dr, ind) => reader(dr, ind));
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
				var com = PostgresCommandFactory.NewCommand(Stream);
				Results = new object[Actions.Count];
				Query.Execute(
					com,
					dr =>
					{
						for (int i = 0; i < Actions.Count; i++)
							Results[i] = Actions[i](dr, i);
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

			public Lazy<T> Find<T>(string uri) where T : IIdentifiable
			{
				return Add(GetRepository<T>().Find(Stream, uri));
			}

			public Lazy<T[]> Find<T>(IEnumerable<string> uris) where T : IIdentifiable
			{
				return Add(GetRepository<T>().Find(Stream, uris));
			}

			public Lazy<T[]> Search<T>(ISpecification<T> filter, int? limit, int? offset) where T : IDataSource
			{
				return Add(GetRepository<T>().Search(Stream, filter, limit, offset));
			}

			public Lazy<long> Count<T>(ISpecification<T> filter) where T : IDataSource
			{
				return Add(GetRepository<T>().Count(Stream, filter));
			}

			public Lazy<bool> Exists<T>(ISpecification<T> filter) where T : IDataSource
			{
				return Add(GetRepository<T>().Exists(Stream, filter));
			}
		}
	}
}
