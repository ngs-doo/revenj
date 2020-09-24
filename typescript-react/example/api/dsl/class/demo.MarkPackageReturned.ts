
import Role from '../security/Role';
import { IMarkPackageReturned as demoIMarkPackageReturned } from '../interface/demo.MarkPackageReturned';
export class MarkPackageReturned implements demoIMarkPackageReturned {
  public static domainObjectName: string = 'demo.MarkPackageReturned';

  public static roles: Role[] = [
	Role.PACKAGE_CHANGE_STATUS,
  ];




  packageID: UUID;

  constructor(data: demoIMarkPackageReturned) {
	  this.packageID = data.packageID;
  }

}
