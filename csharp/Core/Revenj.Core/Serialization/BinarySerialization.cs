using System;
using System.Diagnostics.Contracts;
using System.IO;
using System.IO.Compression;
using System.Runtime.Serialization;
using System.Runtime.Serialization.Formatters.Binary;
using Revenj.Utility;

namespace Revenj.Serialization
{
	internal class BinarySerialization : ISerialization<byte[]>
	{
		private readonly Lazy<GenericDeserializationBinder> CustomDeserialization;

		public BinarySerialization(Lazy<GenericDeserializationBinder> customDeserialization)
		{
			Contract.Requires(customDeserialization != null);

			this.CustomDeserialization = customDeserialization;
		}

		public byte[] Serialize<T>(T value)
		{
			var bf = new BinaryFormatter();
			using (var ms = new MemoryStream())
			using (var zip = new GZipStream(ms, CompressionMode.Compress))
			{
				bf.Serialize(zip, value);
				zip.Close();
				return ms.ToArray();
			}
		}

		public T Deserialize<T>(byte[] data, StreamingContext context)
		{
			var stream = Decompress(data);
			try
			{
				var bf = new BinaryFormatter();
				bf.Context = context;
				return (T)bf.Deserialize(stream);
			}
			catch
			{
				var bf = new BinaryFormatter();
				bf.Context = context;
				bf.Binder = CustomDeserialization.Value;
				stream.Position = 0;
				return (T)bf.Deserialize(stream);
			}
		}

		private static ChunkedMemoryStream Decompress(byte[] data)
		{
			int n;
			var array = new byte[8192];

			using (var compressed = new MemoryStream(data))
			using (var zip = new GZipStream(compressed, CompressionMode.Decompress, true))
			{
				var cms = ChunkedMemoryStream.Create();
				while ((n = zip.Read(array, 0, array.Length)) > 0)
					cms.Write(array, 0, n);
				cms.Position = 0;
				return cms;
			}
		}
	}
}
