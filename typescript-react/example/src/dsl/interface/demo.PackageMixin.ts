
import { IAddress as demoIAddress } from './demo.Address';
import { PackageStatus as demoPackageStatus } from '../enum/demo.PackageStatus';

export interface IPackageMixin {
    ID: UUID;
    price: MoneyStr;
    weight: DecimalStr;
    description?: TextStr;
    deliverToAddress: demoIAddress;
    returnAddress?: demoIAddress;
    statusChangedOn?: TimestampStr;
    status: demoPackageStatus;
}
