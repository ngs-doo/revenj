
import { Marshaller, Serialized } from 'revenj';
import { getApplicationConfiguration } from 'revenj';
import Role from '../security/Role';
import { ICreatePackage as demoICreatePackage } from '../interface/demo.CreatePackage';
import { PackageStatus as demoPackageStatus } from '../enum/demo.PackageStatus';
import { Address as demoAddress } from './demo.Address';
import { IAddress as demoIAddress } from '../interface/demo.Address';
export class CreatePackage implements demoICreatePackage {
  public static domainObjectName: string = 'demo.CreatePackage';

  public static roles: Role[] = [
	Role.PACKAGE_CREATE,
  ];




  public static serialize = (it: demoICreatePackage | undefined, isRequired: boolean = true, path?: string): Serialized<demoICreatePackage | undefined> => {
    if (CreatePackage.required.length === 0 && it == null && isRequired) {
      return {} as any;
    }

    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    return {
      price: CreatePackage.serializeprice(it!.price != null ? it!.price : 0, `${path == null ? '' : `${path}.`}price`)!,
      weight: CreatePackage.serializeweight(it!.weight != null ? it!.weight : 0, `${path == null ? '' : `${path}.`}weight`)!,
      description: CreatePackage.serializedescription(it!.description, `${path == null ? '' : `${path}.`}description`)!,
      deliverToAddress: CreatePackage.serializedeliverToAddress(it!.deliverToAddress != null ? it!.deliverToAddress : {}, `${path == null ? '' : `${path}.`}deliverToAddress`)!,
      returnAddress: CreatePackage.serializereturnAddress(it!.returnAddress, `${path == null ? '' : `${path}.`}returnAddress`)!,
    };
  };

  public static deserialize = (it: Serialized<demoICreatePackage> | undefined, isRequired: boolean = true, path?: string): demoICreatePackage | undefined => {
    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    return {
      ID: CreatePackage.deserializeID(it!.ID != null ? it!.ID : '00000000-0000-0000-0000-000000000000', `${path == null ? '' : `${path}.`}ID`)!,
      statusChangedOn: CreatePackage.deserializestatusChangedOn(it!.statusChangedOn, `${path == null ? '' : `${path}.`}statusChangedOn`)!,
      status: CreatePackage.deserializestatus(it!.status != null ? it!.status : demoPackageStatus.Pending, `${path == null ? '' : `${path}.`}status`)!,
      price: CreatePackage.deserializeprice(it!.price != null ? it!.price : 0, `${path == null ? '' : `${path}.`}price`)!,
      weight: CreatePackage.deserializeweight(it!.weight != null ? it!.weight : 0, `${path == null ? '' : `${path}.`}weight`)!,
      description: CreatePackage.deserializedescription(it!.description, `${path == null ? '' : `${path}.`}description`)!,
      deliverToAddress: CreatePackage.deserializedeliverToAddress(it!.deliverToAddress != null ? it!.deliverToAddress : {}, `${path == null ? '' : `${path}.`}deliverToAddress`)!,
      returnAddress: CreatePackage.deserializereturnAddress(it!.returnAddress, `${path == null ? '' : `${path}.`}returnAddress`)!,
    };
  };

  public static submit = async (obj: demoICreatePackage): Promise<demoICreatePackage> => {
    const callSubmit = getApplicationConfiguration().api.onSubmit;
    const response = await callSubmit('demo.CreatePackage', CreatePackage.serialize(obj, true)!);
    return CreatePackage.deserialize(response, true)!;
  }

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

  private static deserializestatus = (it: any, path: string) => Marshaller.getInstance().deserializeWith(it != null ? it : {}, 'PackageStatus', (it: Serialized<demoPackageStatus>) => demoPackageStatus.deserialize(it, true, path), true, path);

  private static deserializestatusChangedOn = (it: any, path: string) => Marshaller.getInstance().deserialize(it, 'Timestamp', false, path);

  private static deserializeID = (it: any, path: string) => Marshaller.getInstance().deserialize(it != null ? it : '00000000-0000-0000-0000-000000000000', 'UUID', true, path);

  readonly ID?: UUID;
  readonly statusChangedOn?: TimestampStr;
  readonly status?: demoPackageStatus;
  price: MoneyStr;
  weight: DecimalStr;
  description?: TextStr;
  deliverToAddress: demoIAddress;
  returnAddress?: demoIAddress;
  private static required: string[] = ['price','weight','deliverToAddress',]

  constructor(data: demoICreatePackage) {
	  this.ID = data.ID;
	  this.statusChangedOn = data.statusChangedOn;
	  this.status = data.status;
	  this.price = data.price;
	  this.weight = data.weight;
	  this.description = data.description;
	  this.deliverToAddress = new demoAddress(data.deliverToAddress!);
	  this.returnAddress = data.returnAddress != null ? new demoAddress(data.returnAddress) : undefined;
  }

}
