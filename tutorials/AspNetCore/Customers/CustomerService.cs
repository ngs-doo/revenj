using Revenj.DomainPatterns;
using System.Linq;
using Revenj.Extensibility;
using System;
using System.Collections.Generic;
using tutorial;

namespace AspNetTutorial.Customers
{
	public interface ICustomerService
	{
		void Create(IEnumerable<CreateCustomer> commands, Source source);
		void Update(ChangeCustomer command);
		CustomerList[] Search(string name);
		Customer Lookup(string id);
	}

	[Service(InstanceScope.Context)]
	public class CustomerService : ICustomerService
	{
		private readonly IDataContext context;

		public CustomerService(IDataContext context)
		{
			this.context = context;
		}

		public void Create(IEnumerable<CreateCustomer> commands, Source source)
		{
			var newCustomers = new List<Customer>();
			var existingIDs = commands.Where(it => !string.IsNullOrEmpty(it.id)).Select(it => it.id);
			//bulk api show their value when we can preload all the relevant dependencies before going into loops 
			//to do the the actual business processing
			var existingCustomers = context.Find<Customer>(existingIDs).ToDictionary(it => it.id, it => it);
			var hasErrors = false;
			foreach (var c in commands)
			{
				var customer = new Customer { id = ValidateID(c, existingCustomers, source) };
				ApplyChanges(customer, c);
				hasErrors = hasErrors || c.GetValidationErrors().Count != 0;
				newCustomers.Add(customer);
			}
			//since current context works directly on the DB, let's optimize it by avoiding work if we don't need to
			if (!hasErrors)
			{
				context.Create(newCustomers);
				var eventLog = newCustomers.Select(c => new CustomerCreated { customer = c, source = source });
				context.Submit(eventLog);
			}
		}

		//out service methods should accept commands as arguments
		public void Update(ChangeCustomer command)
		{
			var customer = context.Find<Customer>(command.id);
			//since we have a custom command processing which recognizes the specific exception 
			//we can turn it into a user friendly error
			if (customer == null) throw new CustomException($"Invalid customer id: {customer.id}");
			//and leave a trail of events for the relevant changes
			var mobileChanged = CheckMobileChanged(customer, command.mobile);
			var passportChanged = CheckPassportChanged(customer, command.documents);
			ApplyChanges(customer, command);
			//since current context works directly on the DB, let's optimize it by avoiding work if we don't need to
			if (command.GetValidationErrors().Count == 0)
			{
				context.Update(customer);
				if (mobileChanged != null)
					context.Submit(mobileChanged);
				if (passportChanged != null)
					context.Submit(passportChanged);
			}
		}

		//a useful pattern for applying same changes across different commands is to use a common signature and make the appropriate call
		private static Customer ApplyChanges<T>(Customer customer, T command)
			where T : ICommand, CustomerMixin
		{
			customer.name = Validate.Name(command, customer.name, command.name, nameof(command.name));
			customer.mobile = Validate.Phone(command, customer.mobile, command.mobile, nameof(command.mobile));
			customer.landPhone = Validate.Phone(command, customer.landPhone, command.landPhone, nameof(command.landPhone));
			customer.documents = ValidateDocuments(command, command.documents, nameof(command.documents));
			return customer;
		}

		private static string ValidateID(CreateCustomer command, Dictionary<string, Customer> existingCustomers, Source source)
		{
			//a use case is that when customers are created through the browser an ID is assigned to them
			if (source == Source.Web)
			{
				if (!string.IsNullOrEmpty(command.id)) command.LogError(nameof(command.id), "ID can't be set");
				//simplistic way to create new id
				command.id = Guid.NewGuid().ToString().Replace("-", "").Substring(0, 15);
			}
			else //while when they are created through other means ID is provided to them
			{
				//often we need to proceed with processing and we can create temporary values to proceed with furter validations
				var id = (command.id ?? "TEMP-ID").ToUpperInvariant();
				if (string.IsNullOrEmpty(command.id)) command.LogError(nameof(command.id), "ID must be provided");
				else if (existingCustomers.ContainsKey(id)) command.LogError(nameof(command.id), "ID already taken");
				command.id = id;
			}
			return command.id;
		}

		private static List<Document> ValidateDocuments(ICommand command, List<Document> newDocuments, string path)
		{
			for (int i = 0; i < newDocuments.Count; i++)
			{
				var d = newDocuments[i];
				var dt = d.type;//due to eager cache loaded from memory
				if (dt.hasExpiryDate && d.expiresOn == null)
					command.LogError($"{path}[{i}].{nameof(Document.expiresOn)}", "Expiry date must be provided");
				if (string.IsNullOrEmpty(d.value))
					command.LogError($"{path}[{i}].{nameof(Document.value)}", "Value can't be empty");
				//often there is all kinds of cases which must be handled, such as is date in the future, 
				//is it too far in the past and various other kinds which are defined for the business
			}
			return newDocuments;
		}

		private static CustomerMobileChanged CheckMobileChanged(Customer customer, PhoneNumber mobile)
		{
			if (customer.mobile == null && mobile != null
				|| customer.mobile != null && !customer.mobile.Equals(mobile))
				return new CustomerMobileChanged { customer = customer, oldNumber = customer.mobile, newNumber = mobile };
			return null;
		}

		private static CustomerPassportChanged CheckPassportChanged(Customer current, List<Document> newDocuments)
		{
			var passportCode = DocumentType.Passport.code;
			var currentPassport = current.documents.FirstOrDefault(it => it.typeURI == passportCode);
			var newPassport = newDocuments.FirstOrDefault(it => it.typeURI == passportCode);
			if (currentPassport == null && newPassport != null
				|| currentPassport != null && !currentPassport.Equals(newPassport))
				return new CustomerPassportChanged { customerID = current.id, oldPassport = currentPassport, newPassport = newPassport };
			return null;
		}

		public CustomerList[] Search(string name)
		{
			return context.Search(new CustomerList.ByName(uppercasePattern: name.ToUpper()));
		}

		public Customer Lookup(string id)
		{
			return context.Find<Customer>(id);
		}
	}
}
