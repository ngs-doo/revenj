module cms {
  aggregate Person {
    String firstName;
    String lastName;
    Boolean isAlive;
    Int age;
    Address address;
    List<PhoneNumber> phoneNumbers;
    List<Person> *children;
    Person? *spouse;
  }
  value Address {
    String streetAddress;
    String city;
    String(2)? state;
    String(40) postalCode;
  }
  enum PhoneType {
    home;
    office;
    mobile;
  }
  value PhoneNumber {
    PhoneType type;
    String(20) number;
  }
}