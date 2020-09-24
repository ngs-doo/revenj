// tslint:disable trailing-comma ordered-imports quotemark
import * as React from 'react';
import { FieldRegistryContext, Group } from 'revenj';
import { GroupdemoPackageBasicInformation } from '../groups/demo.PackageBasicInformation';
import { GroupdemoCreatePackageDeliverTo } from '../groups/demo.CreatePackage.CreatePackage.DeliverTo';
import { GroupdemoCreatePackageReturnAddress } from '../groups/demo.CreatePackage.CreatePackage.ReturnAddress';

export interface ICreatePackage {
  
  form?: string;
  nested?: boolean;
  vertical?: boolean;
  disabled?: boolean;
  readOnly?: boolean;
  optional?: boolean;
}


export class CreatePackage extends React.PureComponent<ICreatePackage> {
  public static contextType = FieldRegistryContext;
  public context!: React.ContextType<typeof FieldRegistryContext>;
  public render() {
	return (
	  <Group {...this.props}>
        <GroupdemoPackageBasicInformation />
        <GroupdemoCreatePackageDeliverTo />
        <GroupdemoCreatePackageReturnAddress />
	  </Group>
	);
  }
}
