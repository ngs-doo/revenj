using Revenj.DomainPatterns;
using tutorial;

namespace AspNetTutorial.Customers.Handlers
{
	public class ModifyCustomerHandler : IDomainEventHandler<CreateCustomer>, IDomainEventHandler<ChangeCustomer>
	{
		private readonly ICustomerService service;

		public ModifyCustomerHandler(ICustomerService service)
		{
			this.service = service;
		}

		public void Handle(CreateCustomer command)
		{
			service.Create(new[] { command }, Source.Web);
		}

		public void Handle(ChangeCustomer command)
		{
			service.Update(command);
		}
	}
}
