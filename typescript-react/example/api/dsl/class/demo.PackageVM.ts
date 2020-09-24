
import Role from '../security/Role';
import { IPackageVM as demoIPackageVM } from '../interface/demo.PackageVM';
import { IAddress as demoIAddress } from '../interface/demo.Address';
import { Address as demoAddress } from './demo.Address';
import { PackageStatus as demoPackageStatus } from '../enum/demo.PackageStatus';
export class PackageVM implements demoIPackageVM {
  public static domainObjectName: string = 'demo.PackageVM';

  public static roles: Role[] = [
  ];




  ID: UUID;
  price: MoneyStr;
  weight: DecimalStr;
  description?: TextStr;
  deliverToAddress: demoIAddress;
  returnAddress?: demoIAddress;
  statusChangedOn?: TimestampStr;
  status: demoPackageStatus;

  constructor(data: demoIPackageVM) {
	  this.ID = data.ID;
	  this.price = data.price;
	  this.weight = data.weight;
	  this.description = data.description;
	  this.deliverToAddress = new demoAddress(data.deliverToAddress!);
	  this.returnAddress = data.returnAddress != null ? new demoAddress(data.returnAddress) : undefined;
	  this.statusChangedOn = data.statusChangedOn;
	  this.status = data.status;
  }

}
