
import Role from '../security/Role';
import { ICreatePackage as demoICreatePackage } from '../interface/demo.CreatePackage';
import { PackageStatus as demoPackageStatus } from '../enum/demo.PackageStatus';
import { IAddress as demoIAddress } from '../interface/demo.Address';
import { Address as demoAddress } from './demo.Address';
export class CreatePackage implements demoICreatePackage {
  public static domainObjectName: string = 'demo.CreatePackage';

  public static roles: Role[] = [
	Role.PACKAGE_CREATE,
  ];




  readonly ID?: UUID;
  readonly statusChangedOn?: TimestampStr;
  readonly status?: demoPackageStatus;
  price: MoneyStr;
  weight: DecimalStr;
  description?: TextStr;
  deliverToAddress: demoIAddress;
  returnAddress?: demoIAddress;

  constructor(data: demoICreatePackage) {
	  this.ID = data.ID;
	  this.statusChangedOn = data.statusChangedOn;
	  this.status = data.status;
	  this.price = data.price;
	  this.weight = data.weight;
	  this.description = data.description;
	  this.deliverToAddress = new demoAddress(data.deliverToAddress!);
	  this.returnAddress = data.returnAddress != null ? new demoAddress(data.returnAddress) : undefined;
  }

}
