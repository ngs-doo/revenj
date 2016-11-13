using System;
using System.Collections.Generic;
using System.IO;
using System.Threading.Tasks;
using LitS3;
using Revenj;

namespace Revenj.Storage
{
	internal class LitS3Repository : IS3Repository
	{
		private readonly S3Service Service = new S3Service();

		public LitS3Repository(Configuration settings)
		{
			Service.AccessKeyID = settings["AWSAccessKey"];
			Service.SecretAccessKey = settings["AWSSecretKey"];
		}

		private void CheckBucket(string name)
		{
			if (string.IsNullOrEmpty(name))
				throw new ArgumentException(@"Bucket name cannot be empty. 
Provide S3BucketName to config.");
		}

		public Task<Stream> Get(string bucket, string key)
		{
			CheckBucket(bucket);
			return Task.Factory.StartNew(() => Service.GetObjectStream(bucket, key));
		}

		public Task Upload(string bucket, string key, Stream stream, long length, IDictionary<string, string> metadata)
		{
			CheckBucket(bucket);
			//TODO add metadata to amazon
			return Task.Factory.StartNew(() => Service.AddObject(stream, length, bucket, key));
		}

		public Task Delete(string bucket, string key)
		{
			CheckBucket(bucket);
			return Task.Factory.StartNew(() => Service.DeleteObject(bucket, key));
		}
	}
}
