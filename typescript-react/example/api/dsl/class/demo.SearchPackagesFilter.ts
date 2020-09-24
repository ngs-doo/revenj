
import Role from '../security/Role';
import { ISearchPackagesFilter as demoISearchPackagesFilter } from '../interface/demo.SearchPackagesFilter';
import { PackageStatus as demoPackageStatus } from '../enum/demo.PackageStatus';
export class SearchPackagesFilter implements demoISearchPackagesFilter {
  public static domainObjectName: string = 'demo.SearchPackagesFilter';

  public static roles: Role[] = [
  ];




  minPrice?: MoneyStr;
  maxPrice?: MoneyStr;
  minWeight?: DecimalStr;
  maxWeight?: DecimalStr;
  statuses?: Array<demoPackageStatus>;

  constructor(data: demoISearchPackagesFilter) {
	  this.minPrice = data.minPrice;
	  this.maxPrice = data.maxPrice;
	  this.minWeight = data.minWeight;
	  this.maxWeight = data.maxWeight;
	  this.statuses = data.statuses;
  }

}
