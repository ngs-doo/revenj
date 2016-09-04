using System;
using System.Collections.Generic;
using System.Configuration;
using System.IO;
using System.Runtime.InteropServices;
using System.Security;
using System.Security.Cryptography;
using System.Text;
using Revenj.Utility;

namespace Revenj.DatabasePersistence.Postgres.Converters
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
			var decrypt = RsaProvider.Decrypt(data, false);
			var chars = Encoding.Unicode.GetChars(decrypt);
			for (int i = 0; i < chars.Length; i++)
				ss.AppendChar(chars[i]);
			return ss;
		}

		public static SecureString Parse(BufferedTextReader reader, int context)
		{
			var ss = new SecureString();
			var bytes = ByteaConverter.Parse(reader, context);
			if (bytes == null)
				return ss;
			var decrypt = RsaProvider.Decrypt(bytes, false);
			var chars = Encoding.Unicode.GetChars(decrypt);
			for (int i = 0; i < chars.Length; i++)
				ss.AppendChar(chars[i]);
			return ss;
		}

		public static SecureString ParseNullable(BufferedTextReader reader, int context)
		{
			var bytes = ByteaConverter.Parse(reader, context);
			if (bytes == null)
				return null;
			var ss = new SecureString();
			var decrypt = RsaProvider.Decrypt(bytes, false);
			var chars = Encoding.Unicode.GetChars(decrypt);
			for (int i = 0; i < chars.Length; i++)
				ss.AppendChar(chars[i]);
			return ss;
		}

		public static List<SecureString> ParseCollection(BufferedTextReader reader, int context, bool allowNulls)
		{
			var list = ByteaConverter.ParseCollection(reader, context, allowNulls);
			if (list == null)
				return null;
			var result = new List<SecureString>();
			foreach (var item in list)
			{
				if (item == null)
				{
					result.Add(null);
					continue;
				}
				var ss = new SecureString();
				var bytes = RsaProvider.Decrypt(item, false);
				var chars = Encoding.Unicode.GetChars(bytes);
				for (int i = 0; i < chars.Length; i++)
					ss.AppendChar(chars[i]);
				result.Add(ss);
			}
			return result;
		}

		public static int Serialize(SecureString value, char[] buf, int pos)
		{
			return ByteaConverter.Serialize(ExtractBytesAndEcrypt(value), buf, pos);
		}

		public static IPostgresTuple ToTuple(SecureString value)
		{
			if (value == null)
				return null;
			return ByteaConverter.ToTuple(ExtractBytesAndEcrypt(value));
		}

		private static byte[] ExtractBytesAndEcrypt(SecureString value)
		{
			IntPtr bstr = IntPtr.Zero;
			try
			{
				bstr = Marshal.SecureStringToBSTR(value);
				int len = Marshal.ReadInt32(bstr, -4);
				var bytes = new byte[len];
				for (var i = 0; i < len; ++i)
					bytes[i] = Marshal.ReadByte(bstr, i);
				return RsaProvider.Encrypt(bytes, false);
			}
			finally
			{
				if (bstr != IntPtr.Zero) Marshal.ZeroFreeBSTR(bstr);
			}
		}
	}
}