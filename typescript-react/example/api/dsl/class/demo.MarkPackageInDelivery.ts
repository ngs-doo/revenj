
import Role from '../security/Role';
import { IMarkPackageInDelivery as demoIMarkPackageInDelivery } from '../interface/demo.MarkPackageInDelivery';
export class MarkPackageInDelivery implements demoIMarkPackageInDelivery {
  public static domainObjectName: string = 'demo.MarkPackageInDelivery';

  public static roles: Role[] = [
	Role.PACKAGE_CHANGE_STATUS,
  ];




  packageID: UUID;

  constructor(data: demoIMarkPackageInDelivery) {
	  this.packageID = data.packageID;
  }

}
