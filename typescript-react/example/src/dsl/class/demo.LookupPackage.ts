
import { Marshaller, Serialized } from 'revenj';
import { getApplicationConfiguration } from 'revenj';
import Role from '../security/Role';
import { ILookupPackage as demoILookupPackage } from '../interface/demo.LookupPackage';
import { PackageVM as demoPackageVM } from './demo.PackageVM';
import { IPackageVM as demoIPackageVM } from '../interface/demo.PackageVM';
export class LookupPackage implements demoILookupPackage {
  public static domainObjectName: string = 'demo.LookupPackage';

  public static roles: Role[] = [
	Role.PACKAGE_VIEW,
  ];




  public static serialize = (it: demoILookupPackage | undefined, isRequired: boolean = true, path?: string): Serialized<demoILookupPackage | undefined> => {
    if (LookupPackage.required.length === 0 && it == null && isRequired) {
      return {} as any;
    }

    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    return {
      id: LookupPackage.serializeid(it!.id != null ? it!.id : '00000000-0000-0000-0000-000000000000', `${path == null ? '' : `${path}.`}id`)!,
    };
  };

  public static deserialize = (it: Serialized<demoILookupPackage> | undefined, isRequired: boolean = true, path?: string): demoILookupPackage | undefined => {
    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    return {
      id: LookupPackage.deserializeid(it!.id != null ? it!.id : '00000000-0000-0000-0000-000000000000', `${path == null ? '' : `${path}.`}id`)!,
      package: LookupPackage.deserializepackage(it!.package != null ? it!.package : {}, `${path == null ? '' : `${path}.`}package`)!,
    };
  };

  public static submit = async (obj: demoILookupPackage): Promise<demoILookupPackage> => {
    const callSubmit = getApplicationConfiguration().api.onSubmit;
    const response = await callSubmit('demo.LookupPackage', LookupPackage.serialize(obj, true)!);
    return LookupPackage.deserialize(response, true)!;
  }

  private static deserializepackage = (it: any, path: string) => Marshaller.getInstance().deserializeWith(it != null ? it : {}, 'PackageVM', (it: Serialized<demoIPackageVM>) => demoPackageVM.deserialize(it, true, path), true, path);

  private static deserializeid = (it: any, path: string) => Marshaller.getInstance().deserialize(it != null ? it : '00000000-0000-0000-0000-000000000000', 'UUID', true, path);

  private static serializeid = (it: any, path: string) => Marshaller.getInstance().serialize(it != null ? it : '00000000-0000-0000-0000-000000000000', 'UUID', true, path);

  id: UUID;
  readonly package?: demoIPackageVM;
  private static required: string[] = ['id',]

  constructor(data: demoILookupPackage) {
	  this.id = data.id;
	  this.package = new demoPackageVM(data.package!);
  }

}
