using System.Collections.Generic;
using System.IO;
using System.Threading.Tasks;

namespace Revenj.Storage
{
	public interface IS3Repository
	{
		Task<Stream> Get(string bucket, string key);
		Task Upload(string bucket, string key, Stream stream, long length, IDictionary<string, string> metadata);
		Task Delete(string bucket, string key);
	}

	public static class S3RepositoryHelper
	{
		public static Task Upload(this IS3Repository repository, string bucket, string key, Stream stream)
		{
			return repository.Upload(bucket, key, stream, stream.Length, null);
		}
		public static Task Upload(this IS3Repository repository, string bucket, string key, byte[] bytes)
		{
#if PORTABLE
			return repository.Upload(bucket, key, new MemoryStream(bytes), bytes.Length, null);
#else
			return repository.Upload(bucket, key, new MemoryStream(bytes), bytes.LongLength, null);
#endif
		}
	}
}
