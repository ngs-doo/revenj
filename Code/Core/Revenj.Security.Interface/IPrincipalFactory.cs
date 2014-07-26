using System.Security.Principal;

namespace Revenj.Security
{
	/// <summary>
	/// Factory for creating principals for users.
	/// </summary>
	public interface IPrincipalFactory
	{
		/// <summary>
		/// Create principal based on provided user information.
		/// Principal contains information about what the user is allowed to do.
		/// </summary>
		/// <param name="user">user identity</param>
		/// <returns>principal for user</returns>
		IPrincipal Create(IIdentity user);
	}
}
