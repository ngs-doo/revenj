module demo {

  role PACKAGE_VIEW;
  role PACKAGE_CREATE;
  role PACKAGE_MANAGE;
  role PACKAGE_CHANGE_STATUS;

  struct Address {
    String street;
    String zipCode;
    String city;
    String region;
    String country;
  }

  enum PackageStatus {
    Pending { description 'Pending'; }
    InDeliver { description 'In Delivery'; }
    Delivered { description 'Delivered'; }
    ReturnedToSender { description 'Returned to Sender'; }
  }

  mixin PackageMixin {
    UUID          ID;
    Money         price;
    Decimal       weight;
    Text?         description;
    Address       deliverToAddress;
    Address?      returnAddress;
    Timestamp?    statusChangedOn;
    PackageStatus status;
  }

  struct PackageVM {
    has mixin PackageMixin;
  }

  command LookupPackage {
    UUID      id;
    PackageVM package { server managed; }
  }

  struct SearchPackagesFilter {
    Money?               minPrice;
    Money?               maxPrice;
    Decimal?             minWeight;
    Decimal?             maxWeight;
    List<PackageStatus>? statuses;
  }

  command SearchPackages {
    SearchPackagesFilter filter;
    List<PackageVM>      packages { server managed; }
  }

  command CreatePackage {
    has mixin PackageMixin;
    UUID          ID { server managed; }
    Timestamp?    statusChangedOn { server managed; }
    PackageStatus status { server managed; }
  }

  command EditPackage {
    has mixin PackageMixin;
    Timestamp?    statusChangedOn { server managed; }
    PackageStatus status { server managed; }
  }

  command MarkPackageInDelivery {
    UUID packageID;
  }

  command MarkPackageDelivered {
    UUID packageID;
  }

  command MarkPackageReturned {
    UUID packageID;
  }

  permissions {
    allow LookupPackage for PACKAGE_VIEW;
    allow SearchPackages for PACKAGE_VIEW;
    allow CreatePackage for PACKAGE_CREATE;
    allow EditPackage for PACKAGE_MANAGE;
    allow MarkPackageInDelivery for PACKAGE_CHANGE_STATUS;
    allow MarkPackageDelivered for PACKAGE_CHANGE_STATUS;
    allow MarkPackageReturned for PACKAGE_CHANGE_STATUS;
  }

  // ---- UI concepts ----
  item view Address {
    street 'Street and Number';
    zipCode 'Postal/Zip Code';
    city 'City';
    region 'Region/State';
    country 'Country';
  }

  group view PackageBasicInformation 'Basic Information' for PackageMixin {
    price 'Delivery price' {
      validation Typescript 'isPositive';
    }
    weight 'Weight (kg)' {
      validation Typescript 'isPositive';
      validation Typescript 'lessThan(500)';
    }
    description 'Description and Delivery Notes';
  }

  presenter CreatePackage 'Enter Package' {
    item view {
      use group view PackageBasicInformation;
      group 'Deliver To' {
        use deliverToAddress on item view Address;
      }
      group 'Return Address' {
        use returnAddress on item view Address;
      }
    }

    actions {
      save changes;
    }
  }

  presenter EditPackage 'Manage Package' {
    item view {
      use group view PackageBasicInformation;
      group 'Deliver To' {
        use deliverToAddress on item view Address;
      }
      // We only allow
      group 'Return Address' {
        use returnAddress on item view Address;
        properties { visibility 'this.context.visibility.hasReturnAddress'; }
      }
    }

    actions {
      change data;
      view switching;
    }
  }

  presenter SearchPackages 'Search Packages' {
    filter from filter {
      group 'Price' {
        minPrice 'From ($)';
        maxPrice 'To ($)';
      }
      group 'Weight' {
        minWeight 'From (kg)';
        maxWeight 'To (kg)';
      }
      statuses 'In Statuses';
    }

    templater 'Export' 'SearchPackages';

    grid from packages {
      ID 'Tracking Identifer';
      price 'Price';
      weight 'Weight (kg)';
      status 'Package Status';
      description 'Notes';

      fast search;

      edit action EditPackage;
      view action EditPackage;
    }

    create action CreatePackage;

    actions {
      navigation;
    }
  }
}
