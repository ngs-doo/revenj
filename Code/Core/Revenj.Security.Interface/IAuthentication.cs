namespace Revenj.Security
{
	/// <summary>
	/// Authentication service.
	/// For checking if user is authenticated within the system.
	/// </summary>
	public interface IAuthentication<TKey>
	{
		/// <summary>
		/// Check if user with provided password is authenticated within the system.
		/// </summary>
		/// <param name="user">username</param>
		/// <param name="key">key</param>
		/// <returns>is user allowed into the system</returns>
		bool IsAuthenticated(string user, TKey key);
	}
}
