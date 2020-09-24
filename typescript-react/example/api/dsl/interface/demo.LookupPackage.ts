
import { IPackageVM as demoIPackageVM } from './demo.PackageVM';

export interface ILookupPackage {
    id: UUID;
    readonly package?: demoIPackageVM;
}
