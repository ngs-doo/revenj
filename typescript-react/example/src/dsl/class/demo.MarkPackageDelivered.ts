
import { Marshaller, Serialized } from 'revenj';
import { getApplicationConfiguration } from 'revenj';
import Role from '../security/Role';
import { IMarkPackageDelivered as demoIMarkPackageDelivered } from '../interface/demo.MarkPackageDelivered';
export class MarkPackageDelivered implements demoIMarkPackageDelivered {
  public static domainObjectName: string = 'demo.MarkPackageDelivered';

  public static roles: Role[] = [
	Role.PACKAGE_CHANGE_STATUS,
  ];




  public static serialize = (it: demoIMarkPackageDelivered | undefined, isRequired: boolean = true, path?: string): Serialized<demoIMarkPackageDelivered | undefined> => {
    if (MarkPackageDelivered.required.length === 0 && it == null && isRequired) {
      return {} as any;
    }

    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    return {
      packageID: MarkPackageDelivered.serializepackageID(it!.packageID != null ? it!.packageID : '00000000-0000-0000-0000-000000000000', `${path == null ? '' : `${path}.`}packageID`)!,
    };
  };

  public static deserialize = (it: Serialized<demoIMarkPackageDelivered> | undefined, isRequired: boolean = true, path?: string): demoIMarkPackageDelivered | undefined => {
    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    return {
      packageID: MarkPackageDelivered.deserializepackageID(it!.packageID != null ? it!.packageID : '00000000-0000-0000-0000-000000000000', `${path == null ? '' : `${path}.`}packageID`)!,
    };
  };

  public static submit = async (obj: demoIMarkPackageDelivered): Promise<demoIMarkPackageDelivered> => {
    const callSubmit = getApplicationConfiguration().api.onSubmit;
    const response = await callSubmit('demo.MarkPackageDelivered', MarkPackageDelivered.serialize(obj, true)!);
    return MarkPackageDelivered.deserialize(response, true)!;
  }

  private static deserializepackageID = (it: any, path: string) => Marshaller.getInstance().deserialize(it != null ? it : '00000000-0000-0000-0000-000000000000', 'UUID', true, path);

  private static serializepackageID = (it: any, path: string) => Marshaller.getInstance().serialize(it != null ? it : '00000000-0000-0000-0000-000000000000', 'UUID', true, path);

  packageID: UUID;
  private static required: string[] = ['packageID',]

  constructor(data: demoIMarkPackageDelivered) {
	  this.packageID = data.packageID;
  }

}
