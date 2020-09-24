// tslint:disable trailing-comma ordered-imports quotemark
import * as React from 'react';
import { FieldRegistryContext, Group } from 'revenj';

export interface IGroupdemoSearchPackagesFilterWeight {
}


export class GroupdemoSearchPackagesFilterWeight extends React.PureComponent<IGroupdemoSearchPackagesFilterWeight> {
  public static contextType = FieldRegistryContext;
  public context!: React.ContextType<typeof FieldRegistryContext>;
  private minWeightFieldName: any = 'minWeight';
  private minWeightValidations: any = [];
  private maxWeightFieldName: any = 'maxWeight';
  private maxWeightValidations: any = [];
  public render() {
	return (
	  <Group title='Weight' {...this.props}>
		<this.context.Fields.Decimal
		  name={this.minWeightFieldName}
          className='dslField'
		  label={`From (kg)`}
		  required={false}
		  validate={this.minWeightValidations}
		/>
		<this.context.Fields.Decimal
		  name={this.maxWeightFieldName}
          className='dslField'
		  label={`To (kg)`}
		  required={false}
		  validate={this.maxWeightValidations}
		/>
	  </Group>
	);
  }
}
