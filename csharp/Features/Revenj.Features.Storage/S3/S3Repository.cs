using System;
using System.Collections.Generic;
using System.Configuration;
using System.IO;

namespace Revenj.Features.Storage
{
	public static class S3Repository
	{
		private static readonly IS3Repository Repository;
		private static string BucketName;

		static S3Repository()
		{
			var customRepository = ConfigurationManager.AppSettings["S3Repository"];
			BucketName = ConfigurationManager.AppSettings["S3BucketName"];
			if (customRepository != null)
			{
				var type = Type.GetType(customRepository);
				if (type == null)
					throw new ConfigurationErrorsException("Custom S3 repository (" + customRepository + @") not found. 
Please check your settings.");
				Repository = Activator.CreateInstance(type) as IS3Repository;
				if (Repository == null)
					throw new ConfigurationErrorsException("Custom S3 repository (" + customRepository + @") does not implement IS3Repository interface. 
Please check your settings.");
			}
			else Repository = new LitS3Repository();
		}

		public static S3 Load(string bucket, string key, long length, string name, string mimeType, Dictionary<string, string> metadata)
		{
			return new S3(bucket, key, length, name, mimeType, metadata);
		}

		public static Stream GetStream(this S3 s3)
		{
			return string.IsNullOrEmpty(s3.Key) ? null : Repository.Get(s3.Bucket, s3.Key).Result;
		}

		public static byte[] GetBytes(this S3 s3)
		{
			if (string.IsNullOrEmpty(s3.Key))
				return null;
			using (var ms = new MemoryStream())
			{
				Repository.Get(s3.Bucket, s3.Key).Result.CopyTo(ms);
				ms.Position = 0;
				return ms.ToArray();
			}
		}

		public static string Upload(this S3 s3, Stream stream)
		{
			return Upload(s3, s3.Bucket, stream, null);
		}

		public static string Upload(this S3 s3, string bucket, Stream stream, long? length)
		{
			bucket = bucket ?? BucketName;
			if (stream == null)
				throw new ArgumentNullException("Stream can't be null.");
			if (string.IsNullOrEmpty(s3.Key))
			{
				s3.Bucket = bucket;
				s3.Key = Guid.NewGuid().ToString();
			}
			else if (s3.Bucket != bucket)
				throw new ArgumentException("Can't change bucket name");
			s3.cachedContent = null;
			if (length == null)
			{
				var tms = stream as MemoryStream;
				if (tms != null)
					s3.Length = tms.Length;
				else
				{
					try { s3.Length = stream.Length; }
					catch
					{
						using (var ms = new MemoryStream())
						{
							stream.CopyTo(ms);
							s3.Length = ms.Length;
							ms.Position = 0;
							Repository.Upload(s3.Bucket, s3.Key, stream, s3.Length, s3.Metadata).Wait();
							return s3.Key;
						}
					}
				}
			}
			else s3.Length = length.Value;
			Repository.Upload(s3.Bucket, s3.Key, stream, s3.Length, s3.Metadata).Wait();
			return s3.Key;
		}

		public static string Upload(this S3 s3, byte[] bytes)
		{
			return Upload(s3, s3.Bucket ?? BucketName, bytes);
		}

		public static string Upload(this S3 s3, string bucket, byte[] bytes)
		{
			if (bytes == null)
				throw new ArgumentNullException("Stream can't be null.");
			if (string.IsNullOrEmpty(s3.Key))
			{
				s3.Bucket = bucket;
				s3.Key = Guid.NewGuid().ToString();
			}
			else if (s3.Bucket != bucket)
				throw new ArgumentException("Can't change bucket name");
			s3.cachedContent = null;
			s3.Length = bytes.LongLength;
			using (var ms = new MemoryStream(bytes))
				Repository.Upload(s3.Bucket, s3.Key, ms, s3.Length, s3.Metadata).Wait();
			return s3.Key;
		}

		public static void Delete(this S3 s3)
		{
			if (!string.IsNullOrEmpty(s3.Key))
				throw new ArgumentException("S3 object is empty.");
			s3.cachedContent = null;
			Repository.Delete(s3.Bucket, s3.Key).Wait();
			s3.Length = 0;
			s3.Key = null;
		}
	}
}
