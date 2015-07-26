using System;
using System.Collections.Generic;
using System.Configuration;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using LitS3;

namespace Revenj.Features.Storage
{
	internal class LitS3Repository : IS3Repository
	{
		private readonly S3Service Service = new S3Service();
		private readonly bool ShouldRetry;

		public LitS3Repository()
		{
			ShouldRetry = ConfigurationManager.AppSettings["LitS3.ShouldRetry"] != "false";
			Service.AccessKeyID = ConfigurationManager.AppSettings["AWSAccessKey"];
			Service.SecretAccessKey = ConfigurationManager.AppSettings["AWSSecretKey"];
		}

		private List<string> S3Buckets;
		private List<string> Buckets
		{
			get
			{
				if (S3Buckets == null)
				{
					try
					{
						S3Buckets = Service.GetAllBuckets().Select(it => it.Name).ToList();
					}
					catch (Exception ex)
					{
						System.Diagnostics.Debug.WriteLine(ex.ToString());
						if (ShouldRetry)
							S3Buckets = Service.GetAllBuckets().Select(it => it.Name).ToList();
						else throw;
					}
				}
				return S3Buckets;
			}
		}

		private void CheckBucket(string name)
		{
			if (string.IsNullOrEmpty(name))
				throw new ArgumentException("Bucket name cannot be empty!");
			if (Buckets.Contains(name))
				return;
			if (ConfigurationManager.AppSettings["S3CreateBucket"] != "true")
				throw new ArgumentException(@"Dynamic bucket creation is not allowed. 
Please create bucket in advance or allow dynamic bucket creation with S3CreateBucket=true appConfig");
			Service.CreateBucket(name);
		}

		public Task<Stream> Get(string bucket, string key)
		{
			CheckBucket(bucket);
			return Task.Factory.StartNew(() => Service.GetObjectStream(bucket, key));
		}

		public Task Upload(string bucket, string key, Stream stream, long length, IDictionary<string, string> metadata)
		{
			CheckBucket(bucket);
			return Task.Factory.StartNew(() => Service.AddObject(stream, length, bucket, key));
		}

		public Task Delete(string bucket, string key)
		{
			CheckBucket(bucket);
			return Task.Factory.StartNew(() => Service.DeleteObject(bucket, key));
		}
	}
}
