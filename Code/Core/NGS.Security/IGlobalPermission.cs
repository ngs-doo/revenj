using NGS.DomainPatterns;

namespace NGS.Security
{
	public interface IGlobalPermission : IAggregateRoot
	{
		string Name { get; }
		bool IsAllowed { get; }
	}
}
