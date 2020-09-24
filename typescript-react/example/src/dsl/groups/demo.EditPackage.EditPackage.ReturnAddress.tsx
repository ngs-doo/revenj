// tslint:disable trailing-comma ordered-imports quotemark
import * as React from 'react';
import { FieldRegistryContext, Group } from 'revenj';
import { Address } from '../views/demo.Address';

export interface IGroupdemoEditPackageReturnAddress {
}


export class GroupdemoEditPackageReturnAddress extends React.PureComponent<IGroupdemoEditPackageReturnAddress> {
  public static contextType = FieldRegistryContext;
  public context!: React.ContextType<typeof FieldRegistryContext>;
  public render() {
	return (
	  <Group title='Return Address' {...this.props} visibility={this.context.visibility.hasReturnAddress}>

		<Group name={'returnAddress' as any}>
		  <Address nested optional />
		</Group>
	  </Group>
	);
  }
}
