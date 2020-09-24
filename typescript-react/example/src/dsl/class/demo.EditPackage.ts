
import { Marshaller, Serialized } from 'revenj';
import { getApplicationConfiguration } from 'revenj';
import Role from '../security/Role';
import { IEditPackage as demoIEditPackage } from '../interface/demo.EditPackage';
import { PackageStatus as demoPackageStatus } from '../enum/demo.PackageStatus';
import { Address as demoAddress } from './demo.Address';
import { IAddress as demoIAddress } from '../interface/demo.Address';
export class EditPackage implements demoIEditPackage {
  public static domainObjectName: string = 'demo.EditPackage';

  public static roles: Role[] = [
	Role.PACKAGE_MANAGE,
  ];




  public static serialize = (it: demoIEditPackage | undefined, isRequired: boolean = true, path?: string): Serialized<demoIEditPackage | undefined> => {
    if (EditPackage.required.length === 0 && it == null && isRequired) {
      return {} as any;
    }

    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    return {
      ID: EditPackage.serializeID(it!.ID != null ? it!.ID : '00000000-0000-0000-0000-000000000000', `${path == null ? '' : `${path}.`}ID`)!,
      price: EditPackage.serializeprice(it!.price != null ? it!.price : 0, `${path == null ? '' : `${path}.`}price`)!,
      weight: EditPackage.serializeweight(it!.weight != null ? it!.weight : 0, `${path == null ? '' : `${path}.`}weight`)!,
      description: EditPackage.serializedescription(it!.description, `${path == null ? '' : `${path}.`}description`)!,
      deliverToAddress: EditPackage.serializedeliverToAddress(it!.deliverToAddress != null ? it!.deliverToAddress : {}, `${path == null ? '' : `${path}.`}deliverToAddress`)!,
      returnAddress: EditPackage.serializereturnAddress(it!.returnAddress, `${path == null ? '' : `${path}.`}returnAddress`)!,
    };
  };

  public static deserialize = (it: Serialized<demoIEditPackage> | undefined, isRequired: boolean = true, path?: string): demoIEditPackage | undefined => {
    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    return {
      statusChangedOn: EditPackage.deserializestatusChangedOn(it!.statusChangedOn, `${path == null ? '' : `${path}.`}statusChangedOn`)!,
      status: EditPackage.deserializestatus(it!.status != null ? it!.status : demoPackageStatus.Pending, `${path == null ? '' : `${path}.`}status`)!,
      ID: EditPackage.deserializeID(it!.ID != null ? it!.ID : '00000000-0000-0000-0000-000000000000', `${path == null ? '' : `${path}.`}ID`)!,
      price: EditPackage.deserializeprice(it!.price != null ? it!.price : 0, `${path == null ? '' : `${path}.`}price`)!,
      weight: EditPackage.deserializeweight(it!.weight != null ? it!.weight : 0, `${path == null ? '' : `${path}.`}weight`)!,
      description: EditPackage.deserializedescription(it!.description, `${path == null ? '' : `${path}.`}description`)!,
      deliverToAddress: EditPackage.deserializedeliverToAddress(it!.deliverToAddress != null ? it!.deliverToAddress : {}, `${path == null ? '' : `${path}.`}deliverToAddress`)!,
      returnAddress: EditPackage.deserializereturnAddress(it!.returnAddress, `${path == null ? '' : `${path}.`}returnAddress`)!,
    };
  };

  public static submit = async (obj: demoIEditPackage): Promise<demoIEditPackage> => {
    const callSubmit = getApplicationConfiguration().api.onSubmit;
    const response = await callSubmit('demo.EditPackage', EditPackage.serialize(obj, true)!);
    return EditPackage.deserialize(response, true)!;
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

  private static deserializeID = (it: any, path: string) => Marshaller.getInstance().deserialize(it != null ? it : '00000000-0000-0000-0000-000000000000', 'UUID', true, path);

  private static serializeID = (it: any, path: string) => Marshaller.getInstance().serialize(it != null ? it : '00000000-0000-0000-0000-000000000000', 'UUID', true, path);

  private static deserializestatus = (it: any, path: string) => Marshaller.getInstance().deserializeWith(it != null ? it : {}, 'PackageStatus', (it: Serialized<demoPackageStatus>) => demoPackageStatus.deserialize(it, true, path), true, path);

  private static deserializestatusChangedOn = (it: any, path: string) => Marshaller.getInstance().deserialize(it, 'Timestamp', false, path);

  readonly statusChangedOn?: TimestampStr;
  readonly status?: demoPackageStatus;
  ID: UUID;
  price: MoneyStr;
  weight: DecimalStr;
  description?: TextStr;
  deliverToAddress: demoIAddress;
  returnAddress?: demoIAddress;
  private static required: string[] = ['ID','price','weight','deliverToAddress',]

  constructor(data: demoIEditPackage) {
	  this.statusChangedOn = data.statusChangedOn;
	  this.status = data.status;
	  this.ID = data.ID;
	  this.price = data.price;
	  this.weight = data.weight;
	  this.description = data.description;
	  this.deliverToAddress = new demoAddress(data.deliverToAddress!);
	  this.returnAddress = data.returnAddress != null ? new demoAddress(data.returnAddress) : undefined;
  }

}
