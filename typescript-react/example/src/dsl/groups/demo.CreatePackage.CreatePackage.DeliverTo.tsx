// tslint:disable trailing-comma ordered-imports quotemark
import * as React from 'react';
import { FieldRegistryContext, Group } from 'revenj';
import { Address } from '../views/demo.Address';

export interface IGroupdemoCreatePackageDeliverTo {
}


export class GroupdemoCreatePackageDeliverTo extends React.PureComponent<IGroupdemoCreatePackageDeliverTo> {
  public static contextType = FieldRegistryContext;
  public context!: React.ContextType<typeof FieldRegistryContext>;
  public render() {
	return (
	  <Group title='Deliver To' {...this.props}>

		<Group name={'deliverToAddress' as any}>
		  <Address nested />
		</Group>
	  </Group>
	);
  }
}
