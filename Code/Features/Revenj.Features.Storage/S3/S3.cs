using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.Serialization;
using Revenj.Features.Storage;

namespace Revenj
{
	[Serializable]
	[DataContract]
	public class S3 : IEquatable<S3>
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
		public S3(Stream stream) : this() { this.Upload(null, stream, null); }
		public S3(Stream stream, long length) : this() { this.Upload(null, stream, length); }
		public S3(byte[] bytes) : this() { this.Upload(bytes); }

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

		public override int GetHashCode()
		{
			return (Bucket ?? string.Empty).GetHashCode()
				+ (Key ?? string.Empty).GetHashCode();
		}

		public override bool Equals(object obj)
		{
			return Equals(obj as S3);
		}

		public bool Equals(S3 other)
		{
			return other != null
				&& other.Bucket == this.Bucket
				&& other.Key == this.Key
				&& other.Length == this.Length
				&& other.Name == this.Name
				&& other.MimeType == this.MimeType
				&& other.Metadata.Count == this.Metadata.Count
				&& other.Metadata.SequenceEqual(this.Metadata); //TODO: sort and compare
		}
	}
}
