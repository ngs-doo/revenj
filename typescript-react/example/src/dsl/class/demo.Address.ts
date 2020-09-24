
import { Marshaller, Serialized } from 'revenj';
import Role from '../security/Role';
import { IAddress as demoIAddress } from '../interface/demo.Address';
export class Address implements demoIAddress {
  public static domainObjectName: string = 'demo.Address';

  public static roles: Role[] = [
  ];




  public static serialize = (it: demoIAddress | undefined, isRequired: boolean = true, path?: string): Serialized<demoIAddress | undefined> => {
    if (Address.required.length === 0 && it == null && isRequired) {
      return {} as any;
    }

    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    return {
      street: Address.serializestreet(it!.street != null ? it!.street : '', `${path == null ? '' : `${path}.`}street`)!,
      zipCode: Address.serializezipCode(it!.zipCode != null ? it!.zipCode : '', `${path == null ? '' : `${path}.`}zipCode`)!,
      city: Address.serializecity(it!.city != null ? it!.city : '', `${path == null ? '' : `${path}.`}city`)!,
      region: Address.serializeregion(it!.region != null ? it!.region : '', `${path == null ? '' : `${path}.`}region`)!,
      country: Address.serializecountry(it!.country != null ? it!.country : '', `${path == null ? '' : `${path}.`}country`)!,
    };
  };

  public static deserialize = (it: Serialized<demoIAddress> | undefined, isRequired: boolean = true, path?: string): demoIAddress | undefined => {
    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    return {
      street: Address.deserializestreet(it!.street != null ? it!.street : '', `${path == null ? '' : `${path}.`}street`)!,
      zipCode: Address.deserializezipCode(it!.zipCode != null ? it!.zipCode : '', `${path == null ? '' : `${path}.`}zipCode`)!,
      city: Address.deserializecity(it!.city != null ? it!.city : '', `${path == null ? '' : `${path}.`}city`)!,
      region: Address.deserializeregion(it!.region != null ? it!.region : '', `${path == null ? '' : `${path}.`}region`)!,
      country: Address.deserializecountry(it!.country != null ? it!.country : '', `${path == null ? '' : `${path}.`}country`)!,
    };
  };


  private static deserializecountry = (it: any, path: string) => Marshaller.getInstance().deserialize(it != null ? it : '', 'String', true, path);

  private static serializecountry = (it: any, path: string) => Marshaller.getInstance().serialize(it != null ? it : '', 'String', true, path);

  private static deserializeregion = (it: any, path: string) => Marshaller.getInstance().deserialize(it != null ? it : '', 'String', true, path);

  private static serializeregion = (it: any, path: string) => Marshaller.getInstance().serialize(it != null ? it : '', 'String', true, path);

  private static deserializecity = (it: any, path: string) => Marshaller.getInstance().deserialize(it != null ? it : '', 'String', true, path);

  private static serializecity = (it: any, path: string) => Marshaller.getInstance().serialize(it != null ? it : '', 'String', true, path);

  private static deserializezipCode = (it: any, path: string) => Marshaller.getInstance().deserialize(it != null ? it : '', 'String', true, path);

  private static serializezipCode = (it: any, path: string) => Marshaller.getInstance().serialize(it != null ? it : '', 'String', true, path);

  private static deserializestreet = (it: any, path: string) => Marshaller.getInstance().deserialize(it != null ? it : '', 'String', true, path);

  private static serializestreet = (it: any, path: string) => Marshaller.getInstance().serialize(it != null ? it : '', 'String', true, path);

  street: string;
  zipCode: string;
  city: string;
  region: string;
  country: string;
  private static required: string[] = ['street','zipCode','city','region','country',]

  constructor(data: demoIAddress) {
	  this.street = data.street;
	  this.zipCode = data.zipCode;
	  this.city = data.city;
	  this.region = data.region;
	  this.country = data.country;
  }

}
