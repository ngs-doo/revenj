
import Role from '../security/Role';
import { IEditPackage as demoIEditPackage } from '../interface/demo.EditPackage';
import { PackageStatus as demoPackageStatus } from '../enum/demo.PackageStatus';
import { IAddress as demoIAddress } from '../interface/demo.Address';
import { Address as demoAddress } from './demo.Address';
export class EditPackage implements demoIEditPackage {
  public static domainObjectName: string = 'demo.EditPackage';

  public static roles: Role[] = [
	Role.PACKAGE_MANAGE,
  ];




  readonly statusChangedOn?: TimestampStr;
  readonly status?: demoPackageStatus;
  ID: UUID;
  price: MoneyStr;
  weight: DecimalStr;
  description?: TextStr;
  deliverToAddress: demoIAddress;
  returnAddress?: demoIAddress;

  constructor(data: demoIEditPackage) {
	  this.statusChangedOn = data.statusChangedOn;
	  this.status = data.status;
	  this.ID = data.ID;
	  this.price = data.price;
	  this.weight = data.weight;
	  this.description = data.description;
	  this.deliverToAddress = new demoAddress(data.deliverToAddress!);
	  this.returnAddress = data.returnAddress != null ? new demoAddress(data.returnAddress) : undefined;
  }

}
