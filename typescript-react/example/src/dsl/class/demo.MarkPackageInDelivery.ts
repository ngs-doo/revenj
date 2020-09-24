
import { Marshaller, Serialized } from 'revenj';
import { getApplicationConfiguration } from 'revenj';
import Role from '../security/Role';
import { IMarkPackageInDelivery as demoIMarkPackageInDelivery } from '../interface/demo.MarkPackageInDelivery';
export class MarkPackageInDelivery implements demoIMarkPackageInDelivery {
  public static domainObjectName: string = 'demo.MarkPackageInDelivery';

  public static roles: Role[] = [
	Role.PACKAGE_CHANGE_STATUS,
  ];




  public static serialize = (it: demoIMarkPackageInDelivery | undefined, isRequired: boolean = true, path?: string): Serialized<demoIMarkPackageInDelivery | undefined> => {
    if (MarkPackageInDelivery.required.length === 0 && it == null && isRequired) {
      return {} as any;
    }

    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    return {
      packageID: MarkPackageInDelivery.serializepackageID(it!.packageID != null ? it!.packageID : '00000000-0000-0000-0000-000000000000', `${path == null ? '' : `${path}.`}packageID`)!,
    };
  };

  public static deserialize = (it: Serialized<demoIMarkPackageInDelivery> | undefined, isRequired: boolean = true, path?: string): demoIMarkPackageInDelivery | undefined => {
    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    return {
      packageID: MarkPackageInDelivery.deserializepackageID(it!.packageID != null ? it!.packageID : '00000000-0000-0000-0000-000000000000', `${path == null ? '' : `${path}.`}packageID`)!,
    };
  };

  public static submit = async (obj: demoIMarkPackageInDelivery): Promise<demoIMarkPackageInDelivery> => {
    const callSubmit = getApplicationConfiguration().api.onSubmit;
    const response = await callSubmit('demo.MarkPackageInDelivery', MarkPackageInDelivery.serialize(obj, true)!);
    return MarkPackageInDelivery.deserialize(response, true)!;
  }

  private static deserializepackageID = (it: any, path: string) => Marshaller.getInstance().deserialize(it != null ? it : '00000000-0000-0000-0000-000000000000', 'UUID', true, path);

  private static serializepackageID = (it: any, path: string) => Marshaller.getInstance().serialize(it != null ? it : '00000000-0000-0000-0000-000000000000', 'UUID', true, path);

  packageID: UUID;
  private static required: string[] = ['packageID',]

  constructor(data: demoIMarkPackageInDelivery) {
	  this.packageID = data.packageID;
  }

}
