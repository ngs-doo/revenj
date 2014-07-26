namespace Revenj.Security
{
	public interface IUser
	{
		string Name { get; }
		string Password { get; }
		bool IsAllowed { get; }
	}
}
