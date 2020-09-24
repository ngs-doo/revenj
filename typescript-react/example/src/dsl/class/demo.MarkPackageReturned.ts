
import { Marshaller, Serialized } from 'revenj';
import { getApplicationConfiguration } from 'revenj';
import Role from '../security/Role';
import { IMarkPackageReturned as demoIMarkPackageReturned } from '../interface/demo.MarkPackageReturned';
export class MarkPackageReturned implements demoIMarkPackageReturned {
  public static domainObjectName: string = 'demo.MarkPackageReturned';

  public static roles: Role[] = [
	Role.PACKAGE_CHANGE_STATUS,
  ];




  public static serialize = (it: demoIMarkPackageReturned | undefined, isRequired: boolean = true, path?: string): Serialized<demoIMarkPackageReturned | undefined> => {
    if (MarkPackageReturned.required.length === 0 && it == null && isRequired) {
      return {} as any;
    }

    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    return {
      packageID: MarkPackageReturned.serializepackageID(it!.packageID != null ? it!.packageID : '00000000-0000-0000-0000-000000000000', `${path == null ? '' : `${path}.`}packageID`)!,
    };
  };

  public static deserialize = (it: Serialized<demoIMarkPackageReturned> | undefined, isRequired: boolean = true, path?: string): demoIMarkPackageReturned | undefined => {
    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    return {
      packageID: MarkPackageReturned.deserializepackageID(it!.packageID != null ? it!.packageID : '00000000-0000-0000-0000-000000000000', `${path == null ? '' : `${path}.`}packageID`)!,
    };
  };

  public static submit = async (obj: demoIMarkPackageReturned): Promise<demoIMarkPackageReturned> => {
    const callSubmit = getApplicationConfiguration().api.onSubmit;
    const response = await callSubmit('demo.MarkPackageReturned', MarkPackageReturned.serialize(obj, true)!);
    return MarkPackageReturned.deserialize(response, true)!;
  }

  private static deserializepackageID = (it: any, path: string) => Marshaller.getInstance().deserialize(it != null ? it : '00000000-0000-0000-0000-000000000000', 'UUID', true, path);

  private static serializepackageID = (it: any, path: string) => Marshaller.getInstance().serialize(it != null ? it : '00000000-0000-0000-0000-000000000000', 'UUID', true, path);

  packageID: UUID;
  private static required: string[] = ['packageID',]

  constructor(data: demoIMarkPackageReturned) {
	  this.packageID = data.packageID;
  }

}
