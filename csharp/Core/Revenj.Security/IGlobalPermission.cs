namespace Revenj.Security
{
	public interface IGlobalPermission
	{
		string Name { get; }
		bool IsAllowed { get; }
	}
}
