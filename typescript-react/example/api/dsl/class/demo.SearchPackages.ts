
import Role from '../security/Role';
import { ISearchPackages as demoISearchPackages } from '../interface/demo.SearchPackages';
import { ISearchPackagesFilter as demoISearchPackagesFilter } from '../interface/demo.SearchPackagesFilter';
import { SearchPackagesFilter as demoSearchPackagesFilter } from './demo.SearchPackagesFilter';
import { IPackageVM as demoIPackageVM } from '../interface/demo.PackageVM';
import { PackageVM as demoPackageVM } from './demo.PackageVM';
export class SearchPackages implements demoISearchPackages {
  public static domainObjectName: string = 'demo.SearchPackages';

  public static roles: Role[] = [
	Role.PACKAGE_VIEW,
  ];




  filter: demoISearchPackagesFilter;
  readonly packages?: Array<demoIPackageVM>;

  constructor(data: demoISearchPackages) {
	  this.filter = new demoSearchPackagesFilter(data.filter!);
	  this.packages = data.packages != null ? data.packages.map((it) => new demoPackageVM(it)) : undefined;
  }

}
