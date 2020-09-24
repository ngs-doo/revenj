
import { Marshaller, Serialized } from 'revenj';
import { getApplicationConfiguration } from 'revenj';
import Role from '../security/Role';
import { ISearchPackages as demoISearchPackages } from '../interface/demo.SearchPackages';
import { SearchPackagesFilter as demoSearchPackagesFilter } from './demo.SearchPackagesFilter';
import { ISearchPackagesFilter as demoISearchPackagesFilter } from '../interface/demo.SearchPackagesFilter';
import { PackageVM as demoPackageVM } from './demo.PackageVM';
import { IPackageVM as demoIPackageVM } from '../interface/demo.PackageVM';
export class SearchPackages implements demoISearchPackages {
  public static domainObjectName: string = 'demo.SearchPackages';

  public static roles: Role[] = [
	Role.PACKAGE_VIEW,
  ];




  public static serialize = (it: demoISearchPackages | undefined, isRequired: boolean = true, path?: string): Serialized<demoISearchPackages | undefined> => {
    if (SearchPackages.required.length === 0 && it == null && isRequired) {
      return {} as any;
    }

    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    return {
      filter: SearchPackages.serializefilter(it!.filter != null ? it!.filter : {}, `${path == null ? '' : `${path}.`}filter`)!,
    };
  };

  public static deserialize = (it: Serialized<demoISearchPackages> | undefined, isRequired: boolean = true, path?: string): demoISearchPackages | undefined => {
    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    return {
      filter: SearchPackages.deserializefilter(it!.filter != null ? it!.filter : {}, `${path == null ? '' : `${path}.`}filter`)!,
      packages: SearchPackages.deserializepackages(it!.packages != null ? it!.packages : [], `${path == null ? '' : `${path}.`}packages`)!,
    };
  };

  public static submit = async (obj: demoISearchPackages): Promise<demoISearchPackages> => {
    const callSubmit = getApplicationConfiguration().api.onSubmit;
    const response = await callSubmit('demo.SearchPackages', SearchPackages.serialize(obj, true)!);
    return SearchPackages.deserialize(response, true)!;
  }

  private static deserializepackagesMember = (it: any, path: string) => Marshaller.getInstance().deserializeWith(it != null ? it : {}, 'PackageVM', (it: Serialized<demoIPackageVM>) => demoPackageVM.deserialize(it, true, path), true, path);

  private static deserializepackages = (it: any, path: string) => Marshaller.getInstance().deserialize<any[], List<any>>(
    Marshaller.Util.mapSeq(it != null ? it : [], SearchPackages.deserializepackagesMember, path),
    'List',
    true,
    path,
  )

  private static deserializefilter = (it: any, path: string) => Marshaller.getInstance().deserializeWith(it != null ? it : {}, 'SearchPackagesFilter', (it: Serialized<demoISearchPackagesFilter>) => demoSearchPackagesFilter.deserialize(it, true, path), true, path);

  private static serializefilter = (it: any, path: string) => Marshaller.getInstance().serializeWith(it != null ? it : {}, 'SearchPackagesFilter', (it: demoISearchPackagesFilter) => demoSearchPackagesFilter.serialize(it, true, path), true, path);

  filter: demoISearchPackagesFilter;
  readonly packages?: Array<demoIPackageVM>;
  private static required: string[] = ['filter',]

  constructor(data: demoISearchPackages) {
	  this.filter = new demoSearchPackagesFilter(data.filter!);
	  this.packages = data.packages != null ? data.packages.map((it) => new demoPackageVM(it)) : undefined;
  }

}
