using System;
using System.Diagnostics.Contracts;
using System.Runtime.InteropServices;
using System.Security;

namespace NGS.Security
{
	public class RepositoryAuthentication : IAuthentication
	{
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
				&& found.Password == Marshal.PtrToStringBSTR(Marshal.SecureStringToBSTR(password))
				&& found.IsAllowed;
		}

		public bool IsAuthenticated(string user, string password)
		{
			var found = Lookup(user);
			return found != null
				&& found.Password == password
				&& found.IsAllowed;
		}
	}
}
