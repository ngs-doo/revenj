using System;
using System.Collections.Generic;
using System.Configuration;
using System.IO;
using System.Runtime.InteropServices;
using System.Security;
using System.Security.Cryptography;
using System.Text;

namespace NGS.DatabasePersistence.Postgres.Converters
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

		public static SecureString FromDatabase(string value)
		{
			if (value == null)
				return null;
			var data = ByteaConverter.FromDatabase(value);
			var ss = new SecureString();
			var utf8string = Encoding.UTF8.GetString(RsaProvider.Decrypt(data, false));
			foreach (var c in utf8string)
				ss.AppendChar(c);
			return ss;
		}

		public static SecureString Parse(TextReader reader, int context)
		{
			var ss = new SecureString();
			var bytes = ByteaConverter.Parse(reader, context);
			if (bytes == null)
				return ss;
			var utf8string = Encoding.UTF8.GetString(RsaProvider.Decrypt(bytes, false));
			foreach (var c in utf8string)
				ss.AppendChar(c);
			return ss;
		}

		public static List<SecureString> ParseCollection(TextReader reader, int context, bool allowNulls)
		{
			var list = ByteaConverter.ParseCollection(reader, context, allowNulls);
			if (list == null)
				return null;
			var result = new List<SecureString>();
			foreach (var item in list)
			{
				var ss = new SecureString();
				var utf8string = Encoding.UTF8.GetString(RsaProvider.Decrypt(item, false));
				foreach (var c in utf8string)
					ss.AppendChar(c);
				result.Add(ss);
			}
			return result;
		}

		public static string ToDatabase(SecureString value)
		{
			if (value == null)
				return null;
			var decoded = Marshal.PtrToStringBSTR(Marshal.SecureStringToBSTR(value));
			return ByteaConverter.ToDatabase(RsaProvider.Encrypt(Encoding.UTF8.GetBytes(decoded), false));
		}

		public static PostgresTuple ToTuple(SecureString value)
		{
			if (value == null)
				return null;
			var decoded = Marshal.PtrToStringBSTR(Marshal.SecureStringToBSTR(value));
			return ByteaConverter.ToTuple(RsaProvider.Encrypt(Encoding.UTF8.GetBytes(decoded), false));
		}
	}
}