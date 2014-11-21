using System;
using System.Collections.Generic;
using System.Configuration;
using System.IO;
using System.Runtime.InteropServices;
using System.Security;
using System.Security.Cryptography;
using System.Text;

namespace Revenj.Serialization.Json.Converters
{
	public static class SecretConverter
	{
		private static RSACryptoServiceProvider RsaProvider;

		static SecretConverter()
		{
			var secretKeyFile = ConfigurationManager.AppSettings["EncryptionConfiguration"];
			if (string.IsNullOrEmpty(secretKeyFile))
				throw new ConfigurationErrorsException(@"EncryptionConfiguration file not specified. 
To use secret data type EncryptionConfiguration file must be specified");
			if (!File.Exists(secretKeyFile))
			{
				secretKeyFile = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, secretKeyFile);
				if (!File.Exists(secretKeyFile))
					throw new ConfigurationErrorsException(@"EncryptionConfiguration file not found. 
To use secret data type valid EncryptionConfiguration file must be specified");
			}
			RsaProvider = new RSACryptoServiceProvider();
			try
			{
				RsaProvider.FromXmlString(File.ReadAllText(secretKeyFile));
			}
			catch (Exception ex)
			{
				throw new ConfigurationErrorsException(@"Error initializing EncryptionConfiguration. " + ex.Message, ex);
			}
		}

		public static void Serialize(SecureString value, TextWriter sw)
		{
			if (value == null)
				sw.Write("null");
			else
			{
				var decoded = Marshal.PtrToStringBSTR(Marshal.SecureStringToBSTR(value));
				BinaryConverter.Serialize(RsaProvider.Encrypt(Encoding.UTF8.GetBytes(decoded), false), sw);
			}
		}

		public static SecureString Deserialize(TextReader sr, char[] buffer, int nextToken)
		{
			var ss = new SecureString();
			var bytes = BinaryConverter.Deserialize(sr, buffer, nextToken);
			if (bytes == null)
				return ss;
			var utf8string = Encoding.UTF8.GetString(RsaProvider.Decrypt(bytes, false));
			foreach (var c in utf8string)
				ss.AppendChar(c);
			return ss;
		}

		public static List<SecureString> DeserializeCollection(TextReader sr, char[] buffer, int nextToken)
		{
			return JsonSerialization.DeserializeCollection(sr, nextToken, next => Deserialize(sr, buffer, next));
		}
		public static void DeserializeCollection(TextReader sr, char[] buffer, int nextToken, ICollection<SecureString> res)
		{
			JsonSerialization.DeserializeCollection(sr, nextToken, next => Deserialize(sr, buffer, next), res);
		}
		public static List<SecureString> DeserializeNullableCollection(TextReader sr, char[] buffer, int nextToken)
		{
			return JsonSerialization.DeserializeNullableCollection(sr, nextToken, next => Deserialize(sr, buffer, next));
		}
		public static void DeserializeNullableCollection(TextReader sr, char[] buffer, int nextToken, ICollection<SecureString> res)
		{
			JsonSerialization.DeserializeNullableCollection(sr, nextToken, next => Deserialize(sr, buffer, next), res);
		}
	}
}
