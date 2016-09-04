namespace Revenj.Security
{
	public interface IRolePermission
	{
		string Name { get; }
		string RoleID { get; }
		bool IsAllowed { get; }
	}
}
