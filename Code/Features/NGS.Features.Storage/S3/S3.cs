using System;
using System.Collections.Generic;
using System.IO;
using System.Runtime.Serialization;
using NGS.Features.Storage;

namespace NGS
{
	[Serializable]
	[DataContract]
	public class S3
	{
		internal S3(string bucket, string key, long length, string name, string mimeType, Dictionary<string, string> metadata)
		{
			this.Bucket = bucket;
			this.Key = key;
			this.Length = length;
			this.Name = name;
			this.MimeType = mimeType;
			this.Metadata = metadata ?? new Dictionary<string, string>();
		}

		public S3()
		{
			this.Metadata = new Dictionary<string, string>();
		}
		public S3(Stream stream) : base() { this.Upload(null, stream, null); }
		public S3(Stream stream, long length) : base() { this.Upload(null, stream, length); }
		public S3(byte[] bytes) : base() { this.Upload(bytes); }

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
	}
}
