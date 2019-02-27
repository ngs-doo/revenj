using Revenj.DomainPatterns;
using tutorial;

namespace AspNetTutorial.Customers.Handlers
{
	public class ViewCustomerHandler : IDomainEventHandler<SearchCustomers>, IDomainEventHandler<LookupCustomer>
	{
		private readonly ICustomerService service;

		public ViewCustomerHandler(ICustomerService service)
		{
			this.service = service;
		}

		public void Handle(SearchCustomers command)
		{
			//nameMatch can't be null due to model definition
			command.result = service.Search(command.nameMatch.ToUpper());
		}

		public void Handle(LookupCustomer command)
		{
			command.result = service.Lookup(command.id);
		}
	}
}
