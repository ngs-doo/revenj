using NGS.DomainPatterns;

namespace NGS.Security
{
	public interface IRolePermission : IAggregateRoot
	{
		string Name { get; }
		string RoleID { get; }
		bool IsAllowed { get; }
	}
}
