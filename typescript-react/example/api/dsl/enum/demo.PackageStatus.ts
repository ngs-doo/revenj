

export enum PackageStatus_ {
  Pending = 'Pending',
  InDeliver = 'InDeliver',
  Delivered = 'Delivered',
  ReturnedToSender = 'ReturnedToSender',
}

export type PackageStatus = PackageStatus_;

export namespace PackageStatus {

  export const Pending = PackageStatus_.Pending;
  export const InDeliver = PackageStatus_.InDeliver;
  export const Delivered = PackageStatus_.Delivered;
  export const ReturnedToSender = PackageStatus_.ReturnedToSender;

  const meta: IEnumMeta = {
  [PackageStatus.Pending]: {
	description: 'Pending',
  },
  [PackageStatus.InDeliver]: {
	description: 'In Delivery',
  },
  [PackageStatus.Delivered]: {
	description: 'Delivered',
  },
  [PackageStatus.ReturnedToSender]: {
	description: 'Returned to Sender',
  },
  };

  export function getMeta(entry: PackageStatus): IEnumEntryMeta | undefined {
	return meta[entry];
  }

  export function values(): PackageStatus[] {
	return Object.keys(PackageStatus).filter((key) => typeof PackageStatus[key as PackageStatus_] === 'string') as PackageStatus[];
  }

  export function forDescription(label: string): PackageStatus | undefined {
	return PackageStatus.values().find((value) => meta[value] && meta[value].description === label);
  }
}
