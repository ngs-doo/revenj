
import { ISearchPackagesFilter as demoISearchPackagesFilter } from './demo.SearchPackagesFilter';
import { IPackageVM as demoIPackageVM } from './demo.PackageVM';

export interface ISearchPackages {
    filter: demoISearchPackagesFilter;
    readonly packages?: Array<demoIPackageVM>;
}
