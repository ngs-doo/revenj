
import { Marshaller, Serialized } from 'revenj';
import Role from '../security/Role';
import { IPackageVM as demoIPackageVM } from '../interface/demo.PackageVM';
import { Address as demoAddress } from './demo.Address';
import { IAddress as demoIAddress } from '../interface/demo.Address';
import { PackageStatus as demoPackageStatus } from '../enum/demo.PackageStatus';
export class PackageVM implements demoIPackageVM {
  public static domainObjectName: string = 'demo.PackageVM';

  public static roles: Role[] = [
  ];




  public static serialize = (it: demoIPackageVM | undefined, isRequired: boolean = true, path?: string): Serialized<demoIPackageVM | undefined> => {
    if (PackageVM.required.length === 0 && it == null && isRequired) {
      return {} as any;
    }

    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    return {
      ID: PackageVM.serializeID(it!.ID != null ? it!.ID : '00000000-0000-0000-0000-000000000000', `${path == null ? '' : `${path}.`}ID`)!,
      price: PackageVM.serializeprice(it!.price != null ? it!.price : 0, `${path == null ? '' : `${path}.`}price`)!,
      weight: PackageVM.serializeweight(it!.weight != null ? it!.weight : 0, `${path == null ? '' : `${path}.`}weight`)!,
      description: PackageVM.serializedescription(it!.description, `${path == null ? '' : `${path}.`}description`)!,
      deliverToAddress: PackageVM.serializedeliverToAddress(it!.deliverToAddress != null ? it!.deliverToAddress : {}, `${path == null ? '' : `${path}.`}deliverToAddress`)!,
      returnAddress: PackageVM.serializereturnAddress(it!.returnAddress, `${path == null ? '' : `${path}.`}returnAddress`)!,
      statusChangedOn: PackageVM.serializestatusChangedOn(it!.statusChangedOn, `${path == null ? '' : `${path}.`}statusChangedOn`)!,
      status: PackageVM.serializestatus(it!.status != null ? it!.status : demoPackageStatus.Pending, `${path == null ? '' : `${path}.`}status`)!,
    };
  };

  public static deserialize = (it: Serialized<demoIPackageVM> | undefined, isRequired: boolean = true, path?: string): demoIPackageVM | undefined => {
    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    return {
      ID: PackageVM.deserializeID(it!.ID != null ? it!.ID : '00000000-0000-0000-0000-000000000000', `${path == null ? '' : `${path}.`}ID`)!,
      price: PackageVM.deserializeprice(it!.price != null ? it!.price : 0, `${path == null ? '' : `${path}.`}price`)!,
      weight: PackageVM.deserializeweight(it!.weight != null ? it!.weight : 0, `${path == null ? '' : `${path}.`}weight`)!,
      description: PackageVM.deserializedescription(it!.description, `${path == null ? '' : `${path}.`}description`)!,
      deliverToAddress: PackageVM.deserializedeliverToAddress(it!.deliverToAddress != null ? it!.deliverToAddress : {}, `${path == null ? '' : `${path}.`}deliverToAddress`)!,
      returnAddress: PackageVM.deserializereturnAddress(it!.returnAddress, `${path == null ? '' : `${path}.`}returnAddress`)!,
      statusChangedOn: PackageVM.deserializestatusChangedOn(it!.statusChangedOn, `${path == null ? '' : `${path}.`}statusChangedOn`)!,
      status: PackageVM.deserializestatus(it!.status != null ? it!.status : demoPackageStatus.Pending, `${path == null ? '' : `${path}.`}status`)!,
    };
  };


  private static deserializestatus = (it: any, path: string) => Marshaller.getInstance().deserializeWith(it != null ? it : {}, 'PackageStatus', (it: Serialized<demoPackageStatus>) => demoPackageStatus.deserialize(it, true, path), true, path);

  private static serializestatus = (it: any, path: string) => Marshaller.getInstance().serializeWith(it != null ? it : {}, 'PackageStatus', (it: demoPackageStatus) => demoPackageStatus.serialize(it, true, path), true, path);

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
  private static required: string[] = ['ID','price','weight','deliverToAddress','status',]

  constructor(data: demoIPackageVM) {
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
