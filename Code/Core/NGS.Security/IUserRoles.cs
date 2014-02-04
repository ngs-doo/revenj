using NGS.DomainPatterns;

namespace NGS.Security
{
	public interface IUserRoles : IIdentifiable
	{
		string Name { get; }
		string ParentName { get; }
	}
}
