namespace Revenj.Security
{
	public interface IUser
	{
		string Name { get; }
		byte[] PasswordHash { get; }
		bool IsAllowed { get; }
	}
}
