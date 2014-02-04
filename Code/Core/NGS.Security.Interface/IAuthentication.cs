using System.Security;

namespace NGS.Security
{
	/// <summary>
	/// Authentication service.
	/// For checking if user is authenticated within the system.
	/// </summary>
	public interface IAuthentication
	{
		/// <summary>
		/// Check if user with provided password is authenticated within the system.
		/// </summary>
		/// <param name="user">username</param>
		/// <param name="password">password</param>
		/// <returns>is user allowed into the system</returns>
		bool IsAuthenticated(string user, SecureString password);
		/// <summary>
		/// Check if user with provided password is authenticated within the system.
		/// Less secure but faster authentication version
		/// </summary>
		/// <param name="user">username</param>
		/// <param name="password">password</param>
		/// <returns>is user allowed into the system</returns>
		bool IsAuthenticated(string user, string password);
	}
}
