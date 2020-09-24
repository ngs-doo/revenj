
import Role from '../security/Role';
import { IPackageMixin as demoIPackageMixin } from '../interface/demo.PackageMixin';
import { Address as demoAddress } from './demo.Address';
export class PackageMixin implements demoIPackageMixin {
  public static domainObjectName: string = 'demo.PackageMixin';

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

  constructor(data: demoIPackageMixin) {
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
