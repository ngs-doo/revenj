using System;
using System.IO;
#if PORTABLE
using StreamingContext = ProtoBuf.SerializationContext;
#endif
using System.Runtime.Serialization;
using Revenj;
using Revenj.DomainPatterns;
using Revenj.Storage;
using System.Collections.Generic;

namespace Revenj
{
	[DataContract]
	public class S3 : IDisposable
	{
		private IS3Repository InstanceRepository;
		private static readonly IS3Repository StaticRepository;
		private IS3Repository Repository { get { return InstanceRepository ?? StaticRepository; } }
		private static readonly string BucketName;

		static S3()
		{
			StaticRepository = Static.Locator.Resolve<IS3Repository>();
			var config = Static.Locator.Resolve<Configuration>();
			BucketName = config["S3BucketName"];
		}

		public S3(IS3Repository repository = null)
		{
			this.InstanceRepository = repository;
			this.Metadata = new Dictionary<string, string>();
		}
		public S3(Stream stream) : base() { this.Upload(BucketName, stream, null); }
		public S3(Stream stream, long length) : base() { this.Upload(BucketName, stream, length); }
		public S3(byte[] bytes) : base() { this.Upload(bytes); }

		[OnDeserialized]
		private void OnDeserialized(StreamingContext context)
		{
			var locator = context.Context as IServiceProvider;
			if (locator == null)
				return;
			InstanceRepository = locator.Resolve<IS3Repository>();
		}

		[DataMember]
		public string Bucket { get; internal set; }
		[DataMember]
		public string Key { get; internal set; }
		public string URI { get { return Bucket + ":" + Key; } }
		[DataMember]
		public long Length { get; internal set; }
		[DataMember]
		public string Name { get; set; }
		[DataMember]
		public string MimeType { get; set; }
		[DataMember]
		public Dictionary<string, string> Metadata { get; private set; }

		internal byte[] cachedContent;
		public byte[] Content
		{
			get
			{
				if (cachedContent != null)
					cachedContent = this.GetBytes();
				return cachedContent;
			}
		}

		public Stream GetStream()
		{
			return string.IsNullOrEmpty(Key) ? null : Repository.Get(Bucket, Key).Result;
		}

		public byte[] GetBytes()
		{
			if (string.IsNullOrEmpty(Key))
				return null;
			using (var ms = new MemoryStream())
			{
				Repository.Get(Bucket, Key).Result.CopyTo(ms);
				ms.Position = 0;
				return ms.ToArray();
			}
		}

		public string Upload(Stream stream)
		{
			return Upload(Bucket ?? BucketName, stream, null);
		}

		public string Upload(string bucket, Stream stream, long? length)
		{
			if (stream == null)
				throw new ArgumentNullException("Stream can't be null.");
			if (string.IsNullOrEmpty(Key))
			{
				Bucket = bucket;
				Key = Guid.NewGuid().ToString();
			}
			else if (Bucket != bucket)
				throw new ArgumentException("Can't change bucket name");
			cachedContent = null;
			if (length == null)
			{
				var tms = stream as MemoryStream;
				if (tms != null)
					Length = tms.Length;
				else
				{
					try { Length = stream.Length; }
					catch
					{
						using (var ms = new MemoryStream())
						{
							stream.CopyTo(ms);
							Length = ms.Length;
							ms.Position = 0;
							Repository.Upload(Bucket, Key, stream, Length, Metadata).Wait();
							return Key;
						}
					}
				}
			}
			else Length = length.Value;
			Repository.Upload(Bucket, Key, stream, Length, Metadata).Wait();
			return Key;
		}

		public string Upload(byte[] bytes)
		{
			return Upload(Bucket ?? BucketName, bytes);
		}

		public string Upload(string bucket, byte[] bytes)
		{
			if (bytes == null)
				throw new ArgumentNullException("Stream can't be null.");
			if (string.IsNullOrEmpty(Key))
			{
				Bucket = bucket;
				Key = Guid.NewGuid().ToString();
			}
			else if (Bucket != bucket)
				throw new ArgumentException("Can't change bucket name");
			cachedContent = null;
#if PORTABLE
			Length = bytes.Length;
#else
			Length = bytes.LongLength;
#endif
			using (var ms = new MemoryStream(bytes))
				Repository.Upload(Bucket, Key, ms, Length, Metadata).Wait();
			return Key;
		}

		public void Delete()
		{
			if (!string.IsNullOrEmpty(Key))
				throw new ArgumentException("S3 object is empty.");
			cachedContent = null;
			Repository.Delete(Bucket, Key).Wait();
			Length = 0;
			Key = null;
		}

		void IDisposable.Dispose()
		{
			cachedContent = null;
		}
	}
}
