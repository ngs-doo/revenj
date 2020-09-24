// tslint:disable trailing-comma ordered-imports quotemark
import * as React from 'react';
import { FieldRegistryContext, Group } from 'revenj';
import { GroupdemoPackageBasicInformation } from '../groups/demo.PackageBasicInformation';
import { GroupdemoEditPackageDeliverTo } from '../groups/demo.EditPackage.EditPackage.DeliverTo';
import { GroupdemoEditPackageReturnAddress } from '../groups/demo.EditPackage.EditPackage.ReturnAddress';

export interface IEditPackage {
  
  form?: string;
  nested?: boolean;
  vertical?: boolean;
  disabled?: boolean;
  readOnly?: boolean;
  optional?: boolean;
}


export class EditPackage extends React.PureComponent<IEditPackage> {
  public static contextType = FieldRegistryContext;
  public context!: React.ContextType<typeof FieldRegistryContext>;
  public render() {
	return (
	  <Group {...this.props}>
        <GroupdemoPackageBasicInformation />
        <GroupdemoEditPackageDeliverTo />
        <GroupdemoEditPackageReturnAddress />
	  </Group>
	);
  }
}
