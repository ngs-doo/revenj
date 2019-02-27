module tutorial 
{
	value PhoneNumber
	{
		string(2) regionCode;
		string(20) number;
	}

	//data can come from multiple places, eg web or ftp
	enum Source {
		Web;
		Ftp;
	}

	caching {
		//document type is small and rarely changes, so let's keep it in memory and eagerly reload
		eager for DocumentType;
	}

	aggregate DocumentType(code) {
		static Passport 'Passport';
		static DriverLicense 'DriverLicense';
		string code;
		string description;
		bool hasExpiryDate;
	}

	mixin CustomerMixin {
		string name;
		PhoneNumber mobile;
		PhoneNumber? landPhone;
		List<Document> documents;
	}

	command CreateCustomer
	{
		has mixin CustomerMixin;

		string? id;
	}

	entity Document {
		DocumentType *type;
		Date? expiresOn;
		string value;
	}

	aggregate Customer(id) {
		string(20) id;
		string name;
		PhoneNumber mobile;
		PhoneNumber? landPhone;
		List<Document> documents;

		persistence { history; }
	}

	snowflake<Customer> CustomerList {
		id;
		name;
		mobile;

		specification ByName 'it => it.name.ToUpper().Contains(uppercasePattern)' {
			string uppercasePattern;
		}
	}

	event CustomerCreated {
		Customer customer;
		Source source;
	}

	command ChangeCustomer {
		string id;
		has mixin CustomerMixin;
	}

	event CustomerMobileChanged {
		Customer *customer;
		PhoneNumber? oldNumber;
		PhoneNumber? newNumber;
	}

	event CustomerPassportChanged {
		relationship<Customer> customer;
		Document? oldPassport;
		Document? newPassport;
	}

	command SearchCustomers {
		string(30) nameMatch;
		CustomerList[]? result;
	}
	query<SearchCustomers> Search accepts nameMatch returns result;

	command LookupCustomer {
		string(20) id;
		Customer? result;
	}
	query<LookupCustomer> LookupCustomer accepts id returns result;

	role MANAGE_CUSTOMERS;
	role VIEW_CUSTOMERS;
	permissions {
		allow CreateCustomer for MANAGE_CUSTOMERS;
		allow ChangeCustomer for MANAGE_CUSTOMERS;
		allow SearchCustomers for VIEW_CUSTOMERS;
		allow LookupCustomer for VIEW_CUSTOMERS;
	}
}

postgres code <#
INSERT INTO tutorial."DocumentType" 
VALUES ('Passport', 'Passport', true), ('DriverLicense', 'Driver license', true)
ON CONFLICT DO NOTHING;
#>;