import BigNumber from 'bignumber.js';
import { v4 as uuid } from 'uuid';

import { PackageStatus } from '../dsl/enum/demo.PackageStatus';
import { ICreatePackage } from '../dsl/interface/demo.CreatePackage';
import { IEditPackage } from '../dsl/interface/demo.EditPackage';
import { ILookupPackage } from '../dsl/interface/demo.LookupPackage';
import { IMarkPackageInDelivery } from '../dsl/interface/demo.MarkPackageInDelivery';
import { IMarkPackageDelivered} from '../dsl/interface/demo.MarkPackageDelivered';
import { IMarkPackageReturned} from '../dsl/interface/demo.MarkPackageReturned';
import { IPackageVM } from '../dsl/interface/demo.PackageVM';
import { ISearchPackages } from '../dsl/interface/demo.SearchPackages';
import { ISearchPackagesFilter } from '../dsl/interface/demo.SearchPackagesFilter';
import { IAddress } from '../dsl/interface/demo.Address';

const packages: IPackageVM[] = [
  {
    ID: uuid() as UUID,
    price: '100.0' as MoneyStr,
    weight: '5.25' as DecimalStr,
    description: 'Beware the dog' as TextStr,
    deliverToAddress: {
      street: 'Street 1',
      zipCode: '20024',
      region: 'My region',
      city: 'Paris',
      country: 'UK',
    },
    status: PackageStatus.InDeliver,
  },
  {
    ID: uuid() as UUID,
    price: '250.0' as MoneyStr,
    weight: '104.35' as DecimalStr,
    deliverToAddress: {
      street: 'Street 2',
      zipCode: '20024',
      region: 'My region',
      city: 'Paris',
      country: 'UK',
    },
    status: PackageStatus.Pending,
  },
];

const gt = (a?: string | number, b?: string | number) => a != null && b != null && new BigNumber(a).isGreaterThan(new BigNumber(b));
const lt = (a?: string | number, b?: string | number) => a != null && b != null && new BigNumber(a).isLessThan(new BigNumber(b));
const empty = (a?: any) => a == null || (typeof a === 'string' && a.trim().length === 0) || ((a as any[]).length === 0);

type SubmitErrors<T> = {
  [K in keyof T]?: string | SubmitErrors<T[K]>;
}

const getFilteredPackages = (filter: ISearchPackagesFilter): IPackageVM[] =>
  packages.filter((p: IPackageVM) => {
    if (gt(p.price, filter.maxPrice) || lt(p.price, filter.minPrice)) {
      return false
    }

    if (gt(p.weight, filter.maxWeight) || lt(p.weight, filter.minWeight)) {
      return false
    }

    if (filter.statuses != null && filter.statuses.length > 0 && !filter.statuses.includes(p.status)) {
      return false;
    }

    return true;
  });

export const search = (command: ISearchPackages): ISearchPackages => ({
  ...command,
  packages: getFilteredPackages(command.filter),
});

export const appendErrors = <T, K extends keyof T>(commandErrors: SubmitErrors<T>, key: K, errors: SubmitErrors<T[K]> | undefined): void => {
  if (errors != null && Object.keys(errors).length > 0) {
    commandErrors[key] = errors;
  }
}

const validateAddress = (address: Partial<IAddress>): SubmitErrors<IAddress> => {
  const errors: SubmitErrors<IAddress> = {};
  if (empty(address.city)) {
    errors.city = 'Required';
  }
  if (empty(address.country)) {
    errors.country = 'Required';
  }
  if (empty(address.region)) {
    errors.region = 'Required';
  }
  if (empty(address.street)) {
    errors.street = 'Required';
  }
  if (empty(address.zipCode)) {
    errors.zipCode = 'Required';
  }

  return errors;
}

const validateSubmit = (command: ICreatePackage | IEditPackage): void => {
  const errors: SubmitErrors<ICreatePackage | IEditPackage> = {};
  appendErrors(errors, 'deliverToAddress', validateAddress(command.deliverToAddress || {}));

  if (!empty(command.returnAddress)) {
    appendErrors(errors, 'returnAddress', validateAddress(command.returnAddress || {}));
  }

  if (empty(command.price)) {
    errors.price = 'Required';
  } else if (lt(command.price, 0)) {
    errors.price = 'Must be a positive number';
  }

  if (empty(command.weight)) {
    errors.weight = 'Required';
  } else if (lt(command.weight, 0)) {
    errors.weight = 'Must be a positive number';
  } else if (gt(command.weight, 500)) {
    errors.weight = 'Cannot be greater than 500 (kg)';
  }

  if (Object.keys(errors).length > 0) {
    throw errors;
  }
}

export const create = (command: ICreatePackage): ICreatePackage => {
  validateSubmit(command);
  // reusing FE model, so read-only, we're hacking around that
  (command as any).ID = uuid() as UUID;
  (command as any).status = PackageStatus.Pending;
  (command as any).statusChangedOn = String(new Date()) as TimestampStr;
  packages.push(command as unknown as IPackageVM);
  return command;
}

export const edit = (command: IEditPackage): IEditPackage => {
  const original = packages.find((p) => p.ID === command.ID);
  if (original == null) {
    throw new Error(`No package exists for ID ${command.ID}`);
  }
  validateSubmit(command);
  original.deliverToAddress = command.deliverToAddress;
  original.description = command.description;
  original.price = command.price;
  original.returnAddress = command.returnAddress;
  original.weight = command.weight;
  return command;
};

export const lookup = (command: ILookupPackage): ILookupPackage => {
  const match = packages.find((p) => p.ID === command.id);
  if (match == null) {
    throw new Error(`No package exists for ID ${command.id}`);
  }

  return {
    ...command,
    package: match,
  };
}

export const markInDelivery = (command: IMarkPackageInDelivery): IMarkPackageInDelivery => {
  const original = packages.find((p) => p.ID === command.packageID);
  if (original == null) {
    throw new Error(`No package exists for ID ${command.packageID}`);
  }
  if (original.status !== PackageStatus.Pending) {
    throw new Error('Package must be in pending status');
  }
  original.status = PackageStatus.InDeliver;
  original.statusChangedOn = String(new Date()) as TimestampStr;
  return command;
};

export const markDelivered = (command: IMarkPackageDelivered): IMarkPackageDelivered => {
  const original = packages.find((p) => p.ID === command.packageID);
  if (original == null) {
    throw new Error(`No package exists for ID ${command.packageID}`);
  }
  if (original.status !== PackageStatus.InDeliver) {
    throw new Error('Package must be in in delivery status');
  }
  original.status = PackageStatus.Delivered;
  original.statusChangedOn = String(new Date()) as TimestampStr;
  return command;
};

export const markReturned = (command: IMarkPackageReturned): IMarkPackageReturned => {
  const original = packages.find((p) => p.ID === command.packageID);
  if (original == null) {
    throw new Error(`No package exists for ID ${command.packageID}`);
  }
  if (original.status !== PackageStatus.InDeliver) {
    throw new Error('Package must be in in delivery status');
  }
  original.status = PackageStatus.ReturnedToSender;
  original.statusChangedOn = String(new Date()) as TimestampStr;
  return command;
};
