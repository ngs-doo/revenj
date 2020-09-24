// tslint:disable trailing-comma ordered-imports quotemark
import * as React from 'react';
import { FieldRegistryContext, Group } from 'revenj';
import { Address } from '../views/demo.Address';

export interface IGroupdemoCreatePackageReturnAddress {
}


export class GroupdemoCreatePackageReturnAddress extends React.PureComponent<IGroupdemoCreatePackageReturnAddress> {
  public static contextType = FieldRegistryContext;
  public context!: React.ContextType<typeof FieldRegistryContext>;
  public render() {
	return (
	  <Group title='Return Address' {...this.props}>

		<Group name={'returnAddress' as any}>
		  <Address nested optional />
		</Group>
	  </Group>
	);
  }
}
