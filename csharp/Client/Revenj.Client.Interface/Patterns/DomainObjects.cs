namespace Revenj.DomainPatterns
{
	public interface ISearchable { }

	public interface IIdentifiable : ISearchable
	{
		string URI { get; }
	}

	public interface IAggregateRoot : IIdentifiable
	{
		void Validate();
	}

	public interface IDomainEvent : IIdentifiable
	{
		void Validate();
	}

	public interface IDomainEvent<TAggregate> : IIdentifiable
		where TAggregate : class, IAggregateRoot
	{
		void Validate();
	}
}
