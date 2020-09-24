
import Role from '../security/Role';
import { IMarkPackageDelivered as demoIMarkPackageDelivered } from '../interface/demo.MarkPackageDelivered';
export class MarkPackageDelivered implements demoIMarkPackageDelivered {
  public static domainObjectName: string = 'demo.MarkPackageDelivered';

  public static roles: Role[] = [
	Role.PACKAGE_CHANGE_STATUS,
  ];




  packageID: UUID;

  constructor(data: demoIMarkPackageDelivered) {
	  this.packageID = data.packageID;
  }

}
