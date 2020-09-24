
import { Marshaller, Serialized } from 'revenj';
import Role from '../security/Role';
import { ISearchPackagesFilter as demoISearchPackagesFilter } from '../interface/demo.SearchPackagesFilter';
import { PackageStatus as demoPackageStatus } from '../enum/demo.PackageStatus';
export class SearchPackagesFilter implements demoISearchPackagesFilter {
  public static domainObjectName: string = 'demo.SearchPackagesFilter';

  public static roles: Role[] = [
  ];




  public static serialize = (it: demoISearchPackagesFilter | undefined, isRequired: boolean = true, path?: string): Serialized<demoISearchPackagesFilter | undefined> => {
    if (SearchPackagesFilter.required.length === 0 && it == null && isRequired) {
      return {} as any;
    }

    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    return {
      minPrice: SearchPackagesFilter.serializeminPrice(it!.minPrice, `${path == null ? '' : `${path}.`}minPrice`)!,
      maxPrice: SearchPackagesFilter.serializemaxPrice(it!.maxPrice, `${path == null ? '' : `${path}.`}maxPrice`)!,
      minWeight: SearchPackagesFilter.serializeminWeight(it!.minWeight, `${path == null ? '' : `${path}.`}minWeight`)!,
      maxWeight: SearchPackagesFilter.serializemaxWeight(it!.maxWeight, `${path == null ? '' : `${path}.`}maxWeight`)!,
      statuses: SearchPackagesFilter.serializestatuses(it!.statuses, `${path == null ? '' : `${path}.`}statuses`)!,
    };
  };

  public static deserialize = (it: Serialized<demoISearchPackagesFilter> | undefined, isRequired: boolean = true, path?: string): demoISearchPackagesFilter | undefined => {
    if (Marshaller.Assert.assertPresence(it, isRequired) == null && !isRequired) {
      return;
    }

    return {
      minPrice: SearchPackagesFilter.deserializeminPrice(it!.minPrice, `${path == null ? '' : `${path}.`}minPrice`)!,
      maxPrice: SearchPackagesFilter.deserializemaxPrice(it!.maxPrice, `${path == null ? '' : `${path}.`}maxPrice`)!,
      minWeight: SearchPackagesFilter.deserializeminWeight(it!.minWeight, `${path == null ? '' : `${path}.`}minWeight`)!,
      maxWeight: SearchPackagesFilter.deserializemaxWeight(it!.maxWeight, `${path == null ? '' : `${path}.`}maxWeight`)!,
      statuses: SearchPackagesFilter.deserializestatuses(it!.statuses, `${path == null ? '' : `${path}.`}statuses`)!,
    };
  };


  private static deserializestatusesMember = (it: any, path: string) => Marshaller.getInstance().deserializeWith(it != null ? it : {}, 'PackageStatus', (it: Serialized<demoPackageStatus>) => demoPackageStatus.deserialize(it, true, path), true, path);

  private static deserializestatuses = (it: any, path: string) => Marshaller.getInstance().deserialize<any[], List<any>>(
    Marshaller.Util.mapSeq(it, SearchPackagesFilter.deserializestatusesMember, path),
    'List',
    false,
    path,
  )

  private static serializestatusesMember = (it: any, path: string) => Marshaller.getInstance().serializeWith(it != null ? it : {}, 'PackageStatus', (it: demoPackageStatus) => demoPackageStatus.serialize(it, true, path), true, path);

  private static serializestatuses = (it: any, path: string) => Marshaller.Util.mapSeq(
    Marshaller.getInstance().serialize(it, 'b', false, path),
    SearchPackagesFilter.serializestatusesMember,
    path
  )

  private static deserializemaxWeight = (it: any, path: string) => Marshaller.getInstance().deserialize(it, 'Decimal', false, path);

  private static serializemaxWeight = (it: any, path: string) => Marshaller.getInstance().serialize(it, 'Decimal', false, path);

  private static deserializeminWeight = (it: any, path: string) => Marshaller.getInstance().deserialize(it, 'Decimal', false, path);

  private static serializeminWeight = (it: any, path: string) => Marshaller.getInstance().serialize(it, 'Decimal', false, path);

  private static deserializemaxPrice = (it: any, path: string) => Marshaller.getInstance().deserialize(it, 'Money', false, path);

  private static serializemaxPrice = (it: any, path: string) => Marshaller.getInstance().serialize(it, 'Money', false, path);

  private static deserializeminPrice = (it: any, path: string) => Marshaller.getInstance().deserialize(it, 'Money', false, path);

  private static serializeminPrice = (it: any, path: string) => Marshaller.getInstance().serialize(it, 'Money', false, path);

  minPrice?: MoneyStr;
  maxPrice?: MoneyStr;
  minWeight?: DecimalStr;
  maxWeight?: DecimalStr;
  statuses?: Array<demoPackageStatus>;
  private static required: string[] = []

  constructor(data: demoISearchPackagesFilter) {
	  this.minPrice = data.minPrice;
	  this.maxPrice = data.maxPrice;
	  this.minWeight = data.minWeight;
	  this.maxWeight = data.maxWeight;
	  this.statuses = data.statuses;
  }

}
