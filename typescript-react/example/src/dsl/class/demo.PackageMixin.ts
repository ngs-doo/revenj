
import { Marshaller, Serialized } from 'revenj';
import Role from '../security/Role';
import { IPackageMixin as demoIPackageMixin } from '../interface/demo.PackageMixin';
import { IAddress as demoIAddress } from '../interface/demo.Address';
import { Address as demoAddress } from './demo.Address';
import { PackageStatus as demoPackageStatus } from '../enum/demo.PackageStatus';
export class PackageMixin implements demoIPackageMixin {
  public static domainObjectName: string = 'demo.PackageMixin';

  public static roles: Role[] = [
  ];




  public static serialize = (it: demoIPackageMixin | undefined, isRequired: boolean = true, path?: string): Serialized<demoIPackageMixin | undefined> => {
    if (PackageMixin.required.length === 0 && it == null && isRequired) {
      return {} as any;
    }

    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    return {
      ID: PackageMixin.serializeID(it!.ID, `${path == null ? '' : `${path}.`}ID`)!,
      price: PackageMixin.serializeprice(it!.price, `${path == null ? '' : `${path}.`}price`)!,
      weight: PackageMixin.serializeweight(it!.weight, `${path == null ? '' : `${path}.`}weight`)!,
      description: PackageMixin.serializedescription(it!.description, `${path == null ? '' : `${path}.`}description`)!,
      deliverToAddress: PackageMixin.serializedeliverToAddress(it!.deliverToAddress, `${path == null ? '' : `${path}.`}deliverToAddress`)!,
      returnAddress: PackageMixin.serializereturnAddress(it!.returnAddress, `${path == null ? '' : `${path}.`}returnAddress`)!,
      statusChangedOn: PackageMixin.serializestatusChangedOn(it!.statusChangedOn, `${path == null ? '' : `${path}.`}statusChangedOn`)!,
      status: PackageMixin.serializestatus(it!.status, `${path == null ? '' : `${path}.`}status`)!,
    };
  };

  public static deserialize = (it: Serialized<demoIPackageMixin> | undefined, isRequired: boolean = true, path?: string): demoIPackageMixin | undefined => {
    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    return {
      ID: PackageMixin.deserializeID(it!.ID, `${path == null ? '' : `${path}.`}ID`)!,
      price: PackageMixin.deserializeprice(it!.price, `${path == null ? '' : `${path}.`}price`)!,
      weight: PackageMixin.deserializeweight(it!.weight, `${path == null ? '' : `${path}.`}weight`)!,
      description: PackageMixin.deserializedescription(it!.description, `${path == null ? '' : `${path}.`}description`)!,
      deliverToAddress: PackageMixin.deserializedeliverToAddress(it!.deliverToAddress, `${path == null ? '' : `${path}.`}deliverToAddress`)!,
      returnAddress: PackageMixin.deserializereturnAddress(it!.returnAddress, `${path == null ? '' : `${path}.`}returnAddress`)!,
      statusChangedOn: PackageMixin.deserializestatusChangedOn(it!.statusChangedOn, `${path == null ? '' : `${path}.`}statusChangedOn`)!,
      status: PackageMixin.deserializestatus(it!.status, `${path == null ? '' : `${path}.`}status`)!,
    };
  };


  private static deserializestatus = (it: any, path: string) => Marshaller.getInstance().deserializeWith(it != null ? it : demoPackageStatus.Pending, 'PackageStatus', (it: Serialized<demoPackageStatus>) => demoPackageStatus.deserialize(it, true, path), true, path);

  private static serializestatus = (it: any, path: string) => Marshaller.getInstance().serializeWith(it != null ? it : demoPackageStatus.Pending, 'PackageStatus', (it: demoPackageStatus) => demoPackageStatus.serialize(it, true, path), true, path);

  private static deserializestatusChangedOn = (it: any, path: string) => Marshaller.getInstance().deserialize(it, 'Timestamp', false, path);

  private static serializestatusChangedOn = (it: any, path: string) => Marshaller.getInstance().serialize(it, 'Timestamp', false, path);

  private static deserializereturnAddress = (it: any, path: string) => Marshaller.getInstance().deserializeWith(it, 'Address', (it: Serialized<demoIAddress>) => demoAddress.deserialize(it, false, path), false, path);

  private static serializereturnAddress = (it: any, path: string) => Marshaller.getInstance().serializeWith(it, 'Address', (it: demoIAddress) => demoAddress.serialize(it, false, path), false, path);

  private static deserializedeliverToAddress = (it: any, path: string) => Marshaller.getInstance().deserializeWith(it != null ? it : {}, 'Address', (it: Serialized<demoIAddress>) => demoAddress.deserialize(it, true, path), true, path);

  private static serializedeliverToAddress = (it: any, path: string) => Marshaller.getInstance().serializeWith(it != null ? it : {}, 'Address', (it: demoIAddress) => demoAddress.serialize(it, true, path), true, path);

  private static deserializedescription = (it: any, path: string) => Marshaller.getInstance().deserialize(it, 'Text', false, path);

  private static serializedescription = (it: any, path: string) => Marshaller.getInstance().serialize(it, 'Text', false, path);

  private static deserializeweight = (it: any, path: string) => Marshaller.getInstance().deserialize(it != null ? it : 0, 'Decimal', true, path);

  private static serializeweight = (it: any, path: string) => Marshaller.getInstance().serialize(it != null ? it : 0, 'Decimal', true, path);

  private static deserializeprice = (it: any, path: string) => Marshaller.getInstance().deserialize(it != null ? it : 0, 'Money', true, path);

  private static serializeprice = (it: any, path: string) => Marshaller.getInstance().serialize(it != null ? it : 0, 'Money', true, path);

  private static deserializeID = (it: any, path: string) => Marshaller.getInstance().deserialize(it != null ? it : '00000000-0000-0000-0000-000000000000', 'UUID', true, path);

  private static serializeID = (it: any, path: string) => Marshaller.getInstance().serialize(it != null ? it : '00000000-0000-0000-0000-000000000000', 'UUID', true, path);

  ID: UUID;
  price: MoneyStr;
  weight: DecimalStr;
  description?: TextStr;
  deliverToAddress: demoIAddress;
  returnAddress?: demoIAddress;
  statusChangedOn?: TimestampStr;
  status: demoPackageStatus;
  private static required: string[] = []

  constructor(data: demoIPackageMixin) {
	  this.ID = data.ID;
	  this.price = data.price;
	  this.weight = data.weight;
	  this.description = data.description;
	  this.deliverToAddress = new demoAddress(data.deliverToAddress!);
	  this.returnAddress = data.returnAddress != null ? new demoAddress(data.returnAddress) : undefined;
	  this.statusChangedOn = data.statusChangedOn;
	  this.status = data.status;
  }

}
