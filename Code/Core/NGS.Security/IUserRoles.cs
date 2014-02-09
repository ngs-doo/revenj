using NGS.DomainPatterns;

namespace NGS.Security
{
	public interface IUserRoles : IAggregateRoot
	{
		string Name { get; }
		string ParentName { get; }
	}
}
