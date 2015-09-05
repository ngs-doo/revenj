using System;
using System.Collections.Generic;
using System.Data;
using System.IO;
using Revenj.DomainPatterns;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres
{
	public interface IBulkRepository<T> where T : IIdentifiable
	{
		Func<IDataReader, int, T[]> Find(ChunkedMemoryStream stream, IEnumerable<string> uris);
		Func<IDataReader, int, T[]> Search(ChunkedMemoryStream stream, ISpecification<T> filter, int? limit, int? offset);
		Func<IDataReader, int, long> Count(ChunkedMemoryStream stream, ISpecification<T> filter);
		Func<IDataReader, int, bool> Exists(ChunkedMemoryStream stream, ISpecification<T> filter);
	}

	public interface IBulkReader
	{
		void Reset();
		Lazy<T[]> Find<T>(IBulkRepository<T> repository, IEnumerable<string> uri) where T : IIdentifiable;
		Lazy<T[]> Search<T>(IBulkRepository<T> repository, ISpecification<T> filter, int? limit, int? offset) where T : IIdentifiable;
		Lazy<long> Count<T>(IBulkRepository<T> repository, ISpecification<T> filter) where T : IIdentifiable;
		Lazy<bool> Exists<T>(IBulkRepository<T> repository, ISpecification<T> filter) where T : IIdentifiable;
		void Execute();
	}

	public static class BulkReaderHelper
	{
		public static IBulkReader BulkRead(this IPostgresDatabaseQuery query, ChunkedMemoryStream stream)
		{
			return new BulkReader(query, stream);
		}

		public static Lazy<T> Find<T>(this IBulkReader reader, IBulkRepository<T> repository, string uri) where T : IIdentifiable
		{
			var findMany = reader.Find(repository, new[] { uri });
			return new Lazy<T>(() =>
			{
				var result = findMany.Value;
				if (result.Length != 0)
					return result[0];
				return default(T);
			});
		}

		class BulkReader : IBulkReader
		{
			private readonly IPostgresDatabaseQuery Query;
			private readonly ChunkedMemoryStream Stream;
			private readonly TextWriter Writer;
			private int Index;
			private readonly List<Func<IDataReader, int, object>> Actions = new List<Func<IDataReader, int, object>>();
			private object[] Results;

			internal BulkReader(IPostgresDatabaseQuery query, ChunkedMemoryStream stream)
			{
				this.Query = query;
				this.Stream = stream;
				this.Writer = stream.GetWriter();
				Reset();
			}

			public void Reset()
			{
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

			public Lazy<T[]> Find<T>(IBulkRepository<T> repository, IEnumerable<string> uri) where T : IIdentifiable
			{
				return Add(repository.Find(Stream, uri));
			}

			public Lazy<T[]> Search<T>(IBulkRepository<T> repository, ISpecification<T> filter, int? limit, int? offset) where T : IIdentifiable
			{
				return Add(repository.Search(Stream, filter, limit, offset));
			}

			public Lazy<long> Count<T>(IBulkRepository<T> repository, ISpecification<T> filter) where T : IIdentifiable
			{
				return Add(repository.Count(Stream, filter));
			}

			public Lazy<bool> Exists<T>(IBulkRepository<T> repository, ISpecification<T> filter) where T : IIdentifiable
			{
				return Add(repository.Exists(Stream, filter));
			}
		}
	}
}
