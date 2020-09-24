
import { PackageStatus as demoPackageStatus } from '../enum/demo.PackageStatus';
import { IAddress as demoIAddress } from './demo.Address';

export interface IEditPackage {
    readonly statusChangedOn?: TimestampStr;
    readonly status?: demoPackageStatus;
    ID: UUID;
    price: MoneyStr;
    weight: DecimalStr;
    description?: TextStr;
    deliverToAddress: demoIAddress;
    returnAddress?: demoIAddress;
}
