using System;
using System.Diagnostics.Contracts;
using System.Runtime.InteropServices;
using System.Security;
using System.Security.Cryptography;
using System.Text;

namespace Revenj.Security
{
	internal class RepositoryAuthentication : IAuthentication<SecureString>, IAuthentication<string>, IAuthentication<byte[]>
	{
		private static readonly SHA1 SHA = SHA1.Create();

		private readonly Func<string, IUser> Lookup;

		public RepositoryAuthentication(Func<string, IUser> lookup)
		{
			Contract.Requires(lookup != null);

			this.Lookup = lookup;
		}

		public bool IsAuthenticated(string user, SecureString password)
		{
			var found = Lookup(user);
			return found != null
				&& found.IsAllowed
				&& AreEqual(found.PasswordHash, password);
		}

		public bool IsAuthenticated(string user, string password)
		{
			var found = Lookup(user);
			return found != null
				&& found.IsAllowed
				&& AreEqual(found.PasswordHash, SHA.ComputeHash(Encoding.UTF8.GetBytes(password)));
		}

		public bool IsAuthenticated(string user, byte[] key)
		{
			var found = Lookup(user);
			return found != null
				&& found.IsAllowed
				&& AreEqual(found.PasswordHash, key);
		}

		private static bool AreEqual(byte[] left, byte[] right)
		{
			if (left.Length != right.Length)
				return false;
			for (int i = 0; i < left.Length; i++)
				if (left[i] != right[i])
					return false;
			return true;
		}

		private static bool AreEqual(byte[] left, SecureString right)
		{
			IntPtr bstr = IntPtr.Zero;
			try
			{
				bstr = Marshal.SecureStringToBSTR(right);
				int len = Marshal.ReadInt32(bstr, -4);
				var bytes = new byte[len];
				for (var i = 0; i < bytes.Length; i++)
					bytes[i] = Marshal.ReadByte(bstr, i);
				return AreEqual(left, SHA.ComputeHash(Encoding.Convert(Encoding.Unicode, Encoding.UTF8, bytes)));
			}
			finally
			{
				if (bstr != IntPtr.Zero) Marshal.ZeroFreeBSTR(bstr);
			}
		}
	}
}
