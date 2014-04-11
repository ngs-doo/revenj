using System;
using System.Diagnostics.Contracts;
using System.Runtime.InteropServices;
using System.Security;
using System.Security.Cryptography;
using System.Text;

namespace NGS.Security
{
	public class RepositoryAuthentication : IAuthentication<SecureString>, IAuthentication<string>, IAuthentication<byte[]>
	{
		private readonly Func<string, IUser> Lookup;
		private readonly SHA1 SHA = SHA1.Create();

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
				&& found.Password == Marshal.PtrToStringBSTR(Marshal.SecureStringToBSTR(password));
		}

		public bool IsAuthenticated(string user, string password)
		{
			var found = Lookup(user);
			return found != null
				&& found.IsAllowed
				&& found.Password == password;
		}

		public bool IsAuthenticated(string user, byte[] key)
		{
			var found = Lookup(user);
			return found != null
				&& found.IsAllowed
				&& AreEqual(SHA.ComputeHash(Encoding.UTF8.GetBytes(found.Password)), key);
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
	}
}
