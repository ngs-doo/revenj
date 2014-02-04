using NGS.DomainPatterns;

namespace NGS.Security
{
	public interface IUser : IAggregateRoot
	{
		string Name { get; }
		string Password { get; }
		bool IsAllowed { get; }
	}
}
