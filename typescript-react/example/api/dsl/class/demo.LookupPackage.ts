
import Role from '../security/Role';
import { ILookupPackage as demoILookupPackage } from '../interface/demo.LookupPackage';
import { IPackageVM as demoIPackageVM } from '../interface/demo.PackageVM';
import { PackageVM as demoPackageVM } from './demo.PackageVM';
export class LookupPackage implements demoILookupPackage {
  public static domainObjectName: string = 'demo.LookupPackage';

  public static roles: Role[] = [
	Role.PACKAGE_VIEW,
  ];




  id: UUID;
  readonly package?: demoIPackageVM;

  constructor(data: demoILookupPackage) {
	  this.id = data.id;
	  this.package = new demoPackageVM(data.package!);
  }

}
