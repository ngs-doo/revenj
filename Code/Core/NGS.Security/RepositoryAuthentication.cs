using System.Diagnostics.Contracts;
using System.Runtime.InteropServices;
using System.Security;
using NGS.DomainPatterns;

namespace NGS.Security
{
	public class RepositoryAuthentication : IAuthentication
	{
		private readonly IDataCache<IUser> Repository;

		public RepositoryAuthentication(IDataCache<IUser> repository)
		{
			Contract.Requires(repository != null);

			this.Repository = repository;
		}

		public bool IsAuthenticated(string user, SecureString password)
		{
			var found = Repository.Find(user);
			return found != null
				&& found.Password == Marshal.PtrToStringBSTR(Marshal.SecureStringToBSTR(password))
				&& found.IsAllowed;
		}

		public bool IsAuthenticated(string user, string password)
		{
			var found = Repository.Find(user);
			return found != null
				&& found.Password == password
				&& found.IsAllowed;
		}
	}
}
