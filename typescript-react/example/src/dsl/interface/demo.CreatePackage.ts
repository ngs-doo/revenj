
import { PackageStatus as demoPackageStatus } from '../enum/demo.PackageStatus';
import { IAddress as demoIAddress } from './demo.Address';

export interface ICreatePackage {
    readonly ID?: UUID;
    readonly statusChangedOn?: TimestampStr;
    readonly status?: demoPackageStatus;
    price: MoneyStr;
    weight: DecimalStr;
    description?: TextStr;
    deliverToAddress: demoIAddress;
    returnAddress?: demoIAddress;
}
