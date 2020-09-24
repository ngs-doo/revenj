
import { PackageStatus as demoPackageStatus } from '../enum/demo.PackageStatus';

export interface ISearchPackagesFilter {
    minPrice?: MoneyStr;
    maxPrice?: MoneyStr;
    minWeight?: DecimalStr;
    maxWeight?: DecimalStr;
    statuses?: Array<demoPackageStatus>;
}
